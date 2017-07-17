/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.persistence.data.sqlite.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.action.Action;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.persistence.data.sqlite.table.apartment.ApartmentGeofenceRelationTable;
import eu.power_switch.persistence.data.sqlite.table.geofence.GeofenceTable;

/**
 * Provides database methods for managing Geofences
 * <p/>
 * Created by Markus on 20.01.2016.
 */
@Singleton
class GeofenceHandler {

    @Inject
    GeofenceActionHandler geofenceActionHandler;

    @Inject
    GeofenceHandler() {
    }

    /**
     * Add a Geofence to Database
     *
     * @param database the database to use
     * @param geofence new geofence to insert
     *
     * @return ID of inserted Geofence
     */
    protected Long add(SQLiteDatabase database, Geofence geofence) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, geofence.isActive());
        values.put(GeofenceTable.COLUMN_NAME, geofence.getName());
        values.put(GeofenceTable.COLUMN_LATITUDE, geofence.getCenterLocation().latitude);
        values.put(GeofenceTable.COLUMN_LONGITUDE, geofence.getCenterLocation().longitude);
        values.put(GeofenceTable.COLUMN_RADIUS, geofence.getRadius());
        values.put(GeofenceTable.COLUMN_SNAPSHOT, getBytes(geofence.getSnapshot()));
        values.put(GeofenceTable.COLUMN_STATE, geofence.getState());

        long newId = database.insert(GeofenceTable.TABLE_NAME, null, values);

        for (Geofence.EventType eventType : Geofence.EventType.values()) {
            geofenceActionHandler.add(database, geofence.getActions(eventType), newId, eventType);
        }

        return newId;
    }

    /**
     * Get a Geofence from Database
     *
     * @param database the database to use
     * @param id       ID of Geofence
     *
     * @return Geofence
     */
    @Nullable
    protected Geofence get(SQLiteDatabase database, Long id) throws Exception {
        if (id == null) {
            return null;
        }

        Geofence geofence = null;
        Cursor cursor = database.query(GeofenceTable.TABLE_NAME,
                GeofenceTable.ALL_COLUMNS,
                GeofenceTable.COLUMN_ID + "=" + id,
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            geofence = dbToGeofence(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return geofence;
    }

    /**
     * Enables an existing Geofence
     *
     * @param database the database to use
     * @param id       ID of Geofence
     */
    protected void enable(SQLiteDatabase database, Long id) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, true);
        database.update(GeofenceTable.TABLE_NAME, values, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Disables an existing Geofence
     *
     * @param database the database to use
     * @param id       ID of Geofence
     */
    protected void disable(SQLiteDatabase database, Long id) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, false);
        database.update(GeofenceTable.TABLE_NAME, values, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    protected void updateState(SQLiteDatabase database, Long id, @Geofence.State String state) {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_STATE, state);
        database.update(GeofenceTable.TABLE_NAME, values, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Disables all existing Geofences
     *
     * @param database the database to use
     */
    protected void disableAll(SQLiteDatabase database) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, false);
        database.update(GeofenceTable.TABLE_NAME, values, null, null);
    }

    /**
     * Deletes Geofence information from Database
     *
     * @param database the database to use
     * @param id       ID of Geofence
     */
    protected void delete(SQLiteDatabase database, Long id) throws Exception {
        // delete from associations with apartments
        database.delete(ApartmentGeofenceRelationTable.TABLE_NAME, ApartmentGeofenceRelationTable.COLUMN_GEOFENCE_ID + "=" + id, null);

        geofenceActionHandler.delete(database, id);

        database.delete(GeofenceTable.TABLE_NAME, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    public void deleteByApartmentId(SQLiteDatabase database, Long apartmentId) throws Exception {
        Cursor cursor = database.query(ApartmentGeofenceRelationTable.TABLE_NAME,
                null,
                ApartmentGeofenceRelationTable.COLUMN_APARTMENT_ID + "=" + apartmentId,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long geofenceId = cursor.getLong(1);

            delete(database, geofenceId);
            cursor.moveToNext();
        }
        cursor.close();
    }

    /**
     * Gets all Geofences from Database
     *
     * @param database the database to use
     *
     * @return List of Geofences
     */
    protected List<Geofence> getAll(SQLiteDatabase database) throws Exception {
        List<Geofence> geofences = new ArrayList<>();
        Cursor         cursor    = database.query(GeofenceTable.TABLE_NAME, GeofenceTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            geofences.add(dbToGeofence(database, cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return geofences;
    }

    /**
     * Get all custom Geofences
     *
     * @param database the database to use
     *
     * @return List of custom Geofences
     */
    public List<Geofence> getCustom(SQLiteDatabase database) throws Exception {
        List<Geofence> geofences = new ArrayList<>();
        Cursor         cursor    = database.query(GeofenceTable.TABLE_NAME, GeofenceTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long geofenceId = cursor.getLong(0);
            Cursor cursor1 = database.query(ApartmentGeofenceRelationTable.TABLE_NAME,
                    null,
                    ApartmentGeofenceRelationTable.COLUMN_GEOFENCE_ID + "=" + geofenceId,
                    null,
                    null,
                    null,
                    null);

            // only add geofences that are NOT related to an Apartment
            if (!cursor1.moveToFirst()) {
                geofences.add(dbToGeofence(database, cursor));
            }
            cursor1.close();

            cursor.moveToNext();
        }

        cursor.close();
        return geofences;
    }

    /**
     * Gets all Geofences from Database
     *
     * @param database the database to use
     * @param isActive true if Geofence is enabled
     *
     * @return List of enabled/disabled Geofences
     */
    protected List<Geofence> getAll(SQLiteDatabase database, boolean isActive) throws Exception {
        List<Geofence> geofences   = new ArrayList<>();
        int            isActiveInt = isActive ? 1 : 0;
        Cursor cursor = database.query(GeofenceTable.TABLE_NAME,
                GeofenceTable.ALL_COLUMNS,
                GeofenceTable.COLUMN_ACTIVE + "=" + isActiveInt,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            geofences.add(dbToGeofence(database, cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return geofences;
    }

    /**
     * Update Geofence in Database
     *
     * @param database the database to use
     * @param geofence updated Geofence
     */
    public void update(SQLiteDatabase database, Geofence geofence) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, geofence.isActive());
        values.put(GeofenceTable.COLUMN_NAME, geofence.getName());
        values.put(GeofenceTable.COLUMN_LATITUDE, geofence.getCenterLocation().latitude);
        values.put(GeofenceTable.COLUMN_LONGITUDE, geofence.getCenterLocation().longitude);
        values.put(GeofenceTable.COLUMN_RADIUS, geofence.getRadius());
        values.put(GeofenceTable.COLUMN_SNAPSHOT, getBytes(geofence.getSnapshot()));
        values.put(GeofenceTable.COLUMN_STATE, geofence.getState());

        // delete old actions
        geofenceActionHandler.delete(database, geofence.getId());
        // add new actions
        for (Geofence.EventType eventType : Geofence.EventType.values()) {
            geofenceActionHandler.add(database, geofence.getActions(eventType), geofence.getId(), eventType);
        }

        database.update(GeofenceTable.TABLE_NAME, values, GeofenceTable.COLUMN_ID + "=" + geofence.getId(), null);
    }

    /**
     * Creates a Geofence Object out of Database information
     *
     * @param c cursor pointing to a geofence database entry
     *
     * @return Geofence, can be null
     */
    private Geofence dbToGeofence(@NonNull SQLiteDatabase database, Cursor c) throws Exception {
        Geofence geofence;
        Long     id        = c.getLong(0);
        boolean  active    = c.getInt(1) > 0;
        String   name      = c.getString(2);
        double   latitude  = c.getDouble(3);
        double   longitude = c.getDouble(4);
        double   radius    = c.getDouble(5);
        Bitmap   snapshot  = null;
        if (!c.isNull(6)) {
            snapshot = getImage(c.getBlob(6));
        }

        @Geofence.State String state = c.getString(7);

        LatLng location;
        if (latitude == Integer.MAX_VALUE || longitude == Integer.MAX_VALUE) {
            location = null;
        } else {
            location = new LatLng(latitude, longitude);
        }

        HashMap<Geofence.EventType, List<Action>> actionsMap = new HashMap<>();
        for (Geofence.EventType eventType : Geofence.EventType.values()) {
            actionsMap.put(eventType, geofenceActionHandler.get(database, id, eventType));
        }

        geofence = new Geofence(id, active, name, location, radius, snapshot, actionsMap, state);
        return geofence;
    }

    // convert from bitmap to byte array
    private byte[] getBytes(Bitmap bitmap) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    private Bitmap getImage(byte[] image) throws Exception {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}

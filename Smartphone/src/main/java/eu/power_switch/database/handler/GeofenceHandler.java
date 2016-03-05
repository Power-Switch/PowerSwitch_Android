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

package eu.power_switch.database.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.power_switch.action.Action;
import eu.power_switch.database.table.apartment.ApartmentGeofenceRelationTable;
import eu.power_switch.database.table.geofence.GeofenceTable;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.shared.log.Log;

/**
 * Provides database methods for managing Geofences
 * <p/>
 * Created by Markus on 20.01.2016.
 */
abstract class GeofenceHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private GeofenceHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Add a Geofence to Database
     *
     * @param geofence new geofence to insert
     * @return ID of inserted Geofence
     */
    protected static Long add(Geofence geofence) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, geofence.isActive());
        values.put(GeofenceTable.COLUMN_NAME, geofence.getName());
        values.put(GeofenceTable.COLUMN_LATITUDE, geofence.getCenterLocation().latitude);
        values.put(GeofenceTable.COLUMN_LONGITUDE, geofence.getCenterLocation().longitude);
        values.put(GeofenceTable.COLUMN_RADIUS, geofence.getRadius());
        values.put(GeofenceTable.COLUMN_SNAPSHOT, getBytes(geofence.getSnapshot()));

        long newId = DatabaseHandler.database.insert(GeofenceTable.TABLE_NAME, null, values);

        for (Geofence.EventType eventType : Geofence.EventType.values()) {
            GeofenceActionHandler.add(geofence.getActions(eventType), newId, eventType);
        }

        return newId;
    }

    protected static Geofence get(Long id) throws Exception {
        if (id == null) {
            return null;
        }

        Cursor cursor = DatabaseHandler.database.query(GeofenceTable.TABLE_NAME, null,
                GeofenceTable.COLUMN_ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();
        Geofence geofence = dbToGeofence(cursor);
        cursor.close();
        return geofence;
    }

    /**
     * Enables an existing Geofence
     *
     * @param id ID of Geofence
     */
    protected static void enable(Long id) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, true);
        DatabaseHandler.database.update(GeofenceTable.TABLE_NAME, values, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Disables an existing Geofence
     *
     * @param id ID of Geofence
     */
    protected static void disable(Long id) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, false);
        DatabaseHandler.database.update(GeofenceTable.TABLE_NAME, values, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Disables all existing Gateways
     */
    protected static void disableAll() throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, false);
        DatabaseHandler.database.update(GeofenceTable.TABLE_NAME, values, null, null);
    }

    /**
     * Deletes Geofence information from Database
     *
     * @param id ID of Geofence
     */
    protected static void delete(Long id) throws Exception {
        // delete from associations with apartments
        DatabaseHandler.database.delete(ApartmentGeofenceRelationTable.TABLE_NAME, ApartmentGeofenceRelationTable
                .COLUMN_GEOFENCE_ID + "=" + id, null);

        GeofenceActionHandler.delete(id);

        DatabaseHandler.database.delete(GeofenceTable.TABLE_NAME, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    public static void deleteByApartmentId(Long apartmentId) throws Exception {
        Cursor cursor = DatabaseHandler.database.query(ApartmentGeofenceRelationTable.TABLE_NAME, null, ApartmentGeofenceRelationTable
                .COLUMN_APARTMENT_ID + "=" + apartmentId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long geofenceId = cursor.getLong(1);

            delete(geofenceId);
            cursor.moveToNext();
        }
        cursor.close();
    }

    /**
     * Gets all Geofences from Database
     *
     * @return List of Geofences
     */
    protected static List<Geofence> getAll() throws Exception {
        List<Geofence> geofences = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(GeofenceTable.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            geofences.add(dbToGeofence(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return geofences;
    }

    /**
     * Get all custom Geofences
     *
     * @return List of custom Geofences
     */
    public static List<Geofence> getCustom() throws Exception {
        List<Geofence> geofences = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(GeofenceTable.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long geofenceId = cursor.getLong(0);
            Cursor cursor1 = DatabaseHandler.database.query(ApartmentGeofenceRelationTable.TABLE_NAME, null,
                    ApartmentGeofenceRelationTable.COLUMN_GEOFENCE_ID + "=" + geofenceId, null, null, null, null);

            // only add geofences that are NOT related to an Apartment
            if (!cursor1.moveToFirst()) {
                geofences.add(dbToGeofence(cursor));
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
     * @param isActive true if Geofence is enabled
     * @return List of enabled/disabled Geofences
     */
    protected static List<Geofence> getAll(boolean isActive) throws Exception {
        List<Geofence> geofences = new ArrayList<>();
        int isActiveInt = isActive ? 1 : 0;
        Cursor cursor = DatabaseHandler.database.query(GeofenceTable.TABLE_NAME, null,
                GeofenceTable.COLUMN_ACTIVE + "=" + isActiveInt, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            geofences.add(dbToGeofence(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return geofences;
    }

    /**
     * Update Geofence in Database
     *
     * @param geofence updated Geofence
     */
    public static void update(Geofence geofence) throws Exception {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, geofence.isActive());
        values.put(GeofenceTable.COLUMN_NAME, geofence.getName());
        values.put(GeofenceTable.COLUMN_LATITUDE, geofence.getCenterLocation().latitude);
        values.put(GeofenceTable.COLUMN_LONGITUDE, geofence.getCenterLocation().longitude);
        values.put(GeofenceTable.COLUMN_RADIUS, geofence.getRadius());
        values.put(GeofenceTable.COLUMN_SNAPSHOT, getBytes(geofence.getSnapshot()));

        // delete old actions
        GeofenceActionHandler.delete(geofence.getId());
        // add new actions
        for (Geofence.EventType eventType : Geofence.EventType.values()) {
            GeofenceActionHandler.add(geofence.getActions(eventType), geofence.getId(), eventType);
        }

        DatabaseHandler.database.update(GeofenceTable.TABLE_NAME, values,
                GeofenceTable.COLUMN_ID + "=" + geofence.getId(), null);
    }


    /**
     * Creates a Geofence Object out of Database information
     *
     * @param c cursor pointing to a geofence database entry
     * @return Geofence, can be null
     */
    private static Geofence dbToGeofence(Cursor c) throws Exception {
        try {
            Geofence geofence;
            Long id = c.getLong(0);
            boolean active = c.getInt(1) > 0;
            String name = c.getString(2);
            double latitude = c.getDouble(3);
            double longitude = c.getDouble(4);
            double radius = c.getDouble(5);
            Bitmap snapshot = null;
            if (!c.isNull(6)) {
                snapshot = getImage(c.getBlob(6));
            }

            LatLng location;
            if (latitude == Integer.MAX_VALUE || longitude == Integer.MAX_VALUE) {
                location = null;
            } else {
                location = new LatLng(latitude, longitude);
            }

            HashMap<Geofence.EventType, List<Action>> actionsMap = new HashMap<>();
            for (Geofence.EventType eventType : Geofence.EventType.values()) {
                actionsMap.put(eventType, GeofenceActionHandler.get(id, eventType));
            }

            geofence = new Geofence(id, active, name, location, radius, snapshot, actionsMap);
            return geofence;
        } catch (Exception e) {
            Log.e(e);
        }

        return null;
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) throws Exception {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}

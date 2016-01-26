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

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

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
     * Add a Geofence to Database
     *
     * @param geofence new geofence to insert
     * @return ID of inserted Geofence
     */
    protected static Long add(Geofence geofence) {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, true);
        values.put(GeofenceTable.COLUMN_NAME, geofence.getName());
        values.put(GeofenceTable.COLUMN_LATITUDE, geofence.getCenterLocation().latitude);
        values.put(GeofenceTable.COLUMN_LONGITUDE, geofence.getCenterLocation().longitude);
        values.put(GeofenceTable.COLUMN_RADIUS, geofence.getRadius());

        long newId = DatabaseHandler.database.insert(GeofenceTable.TABLE_NAME, null, values);
        return newId;
    }

    protected static Geofence get(Long id) {

        Cursor cursor = DatabaseHandler.database.query(GeofenceTable.TABLE_NAME, null, GeofenceTable.COLUMN_ID + "=" + id, null, null,
                null, null);
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
    protected static void enable(Long id) {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, 1);
        DatabaseHandler.database.update(GeofenceTable.TABLE_NAME, values, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Disables an existing Geofence
     *
     * @param id ID of Geofence
     */
    protected static void disable(Long id) {
        ContentValues values = new ContentValues();
        values.put(GeofenceTable.COLUMN_ACTIVE, 0);
        DatabaseHandler.database.update(GeofenceTable.TABLE_NAME, values, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Deletes Geofence information from Database
     *
     * @param id ID of Geofence
     */
    protected static void delete(Long id) {
        // delete from associations with apartments
        DatabaseHandler.database.delete(ApartmentGeofenceRelationTable.TABLE_NAME, ApartmentGeofenceRelationTable
                .COLUMN_GEOFENCE_ID + "=" + id, null);

        DatabaseHandler.database.delete(GeofenceTable.TABLE_NAME, GeofenceTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Creates a Geofence Object out of Database information
     *
     * @param c cursor pointing to a geofence database entry
     * @return Geofence, can be null
     */
    private static Geofence dbToGeofence(Cursor c) {
        try {
            Geofence geofence;
            Long id = c.getLong(0);
            boolean active = c.getInt(1) > 0;
            String name = c.getString(2);
            double latitude = c.getDouble(3);
            double longitude = c.getDouble(4);
            double radius = c.getDouble(5);

            geofence = new Geofence(id, active, name, new LatLng(latitude, longitude), radius);
            return geofence;
        } catch (Exception e) {
            Log.e(e);
        }

        return null;
    }

    /**
     * Gets all Geofences from Database
     *
     * @return List of Geofences
     */
    protected static List<Geofence> getAll() {
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
     * Gets all Geofences from Database
     *
     * @param isActive true if Geofence is enabled
     * @return List of enabled/disabled Geofences
     */
    protected static List<Geofence> getAll(boolean isActive) {
        List<Geofence> geofences = new ArrayList<>();
        int isActiveInt = isActive ? 1 : 0;
        Cursor cursor = DatabaseHandler.database.query(GeofenceTable.TABLE_NAME, null, GeofenceTable.COLUMN_ACTIVE + "=" + isActiveInt,
                null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            geofences.add(dbToGeofence(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return geofences;
    }
}

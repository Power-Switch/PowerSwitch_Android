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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eu.power_switch.database.table.apartment.ApartmentGatewayRelationTable;
import eu.power_switch.database.table.apartment.ApartmentGeofenceRelationTable;
import eu.power_switch.database.table.apartment.ApartmentTable;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * Provides database methods for managing Apartments
 */
abstract class ApartmentHandler {

    /**
     * Adds a Apartment to Database
     *
     * @param apartment Apartment
     * @return ID of inserted Apartment
     */
    protected static long add(Apartment apartment) throws Exception {
        ContentValues values = new ContentValues();
        values.put(ApartmentTable.COLUMN_NAME, apartment.getName());
        long apartmentId = DatabaseHandler.database.insert(ApartmentTable.TABLE_NAME, null, values);
        // notice that apartmentId here may be different than
        // apartment.getId() because it was just inserted into database
        addAssociatedGateways(apartmentId, apartment.getAssociatedGateways());
        addGeofence(apartmentId, apartment);

        return apartmentId;
    }

    private static void addGeofence(long apartmentId, Apartment apartment) throws Exception {
        if (apartment.getGeofence() == null) {
            return;
        }
        Long geofenceId = GeofenceHandler.add(apartment.getGeofence());

        ContentValues values = new ContentValues();
        values.put(ApartmentGeofenceRelationTable.COLUMN_APARTMENT_ID, apartmentId);
        values.put(ApartmentGeofenceRelationTable.COLUMN_GEOFENCE_ID, geofenceId);
        DatabaseHandler.database.insert(ApartmentGeofenceRelationTable.TABLE_NAME, null, values);
    }

    /**
     * Updates a Apartment in Database
     *
     * @param apartment updated Apartment
     */
    protected static void update(Apartment apartment) throws Exception {
        ContentValues values = new ContentValues();
        values.put(ApartmentTable.COLUMN_NAME, apartment.getName());
        DatabaseHandler.database.update(ApartmentTable.TABLE_NAME, values,
                ApartmentTable.COLUMN_ID + "==" + apartment.getId(), null);

        // update associated geofence (delete old, add new)
        GeofenceHandler.deleteByApartmentId(apartment.getId());
        addGeofence(apartment.getId(), apartment);

        // update associated gateways
        removeAssociatedGateways(apartment.getId());
        addAssociatedGateways(apartment.getId(), apartment.getAssociatedGateways());
    }

    /**
     * Deletes Apartment from Database
     *
     * @param apartmentId ID of Apartment
     */
    protected static void delete(Long apartmentId) throws Exception {
        LinkedList<Room> rooms = RoomHandler.getByApartment(apartmentId);
        for (Room room : rooms) {
            RoomHandler.delete(room.getId());
        }

        LinkedList<Scene> scenes = SceneHandler.getByApartment(apartmentId);
        for (Scene scene : scenes) {
            SceneHandler.delete(scene.getId());
        }

        removeAssociatedGateways(apartmentId);

        GeofenceHandler.deleteByApartmentId(apartmentId);

        DatabaseHandler.database.delete(ApartmentTable.TABLE_NAME, ApartmentTable.COLUMN_ID + "=" + apartmentId, null);
    }

    /**
     * Gets Apartment from Database
     *
     * @param name Name of Apartment
     * @return Apartment
     */
    protected static Apartment get(String name) throws Exception {
        Cursor cursor = DatabaseHandler.database.query(ApartmentTable.TABLE_NAME, null, ApartmentTable.COLUMN_NAME + "=='" +
                name + "'", null, null, null, null);
        cursor.moveToFirst();

        Apartment Apartment = dbToApartment(cursor);
        cursor.close();
        return Apartment;
    }

    /**
     * Gets Apartment from Database
     *
     * @param id ID of Apartment
     * @return Apartment
     */
    protected static Apartment get(Long id) throws Exception {
        Cursor cursor = DatabaseHandler.database.query(ApartmentTable.TABLE_NAME, null, ApartmentTable.COLUMN_ID + "==" + id,
                null, null, null, null);
        cursor.moveToFirst();

        Apartment Apartment = dbToApartment(cursor);
        cursor.close();
        return Apartment;
    }

    /**
     * Gets the containing Apartment of a receiver
     *
     * @param receiver Receiver
     * @return containing Apartment
     */
    public static Apartment get(Receiver receiver) throws Exception {
        return get(RoomHandler.get(receiver.getRoomId()).getApartmentId());
    }

    /**
     * Gets the containing Apartment of a room
     *
     * @param room Room
     * @return containing Apartment
     */
    public static Apartment get(Room room) throws Exception {
        return get(room.getApartmentId());
    }

    /**
     * Gets the containing Apartment of a scene
     *
     * @param scene Scene
     * @return containing Apartment
     */
    public static Apartment get(Scene scene) throws Exception {
        return get(scene.getApartmentId());
    }

    /**
     * Gets all Apartments from Database
     *
     * @return List of Apartments
     */
    protected static List<Apartment> getAll() throws Exception {
        List<Apartment> apartments = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(ApartmentTable.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Apartment apartment = dbToApartment(cursor);
            apartments.add(apartment);
            cursor.moveToNext();
        }
        cursor.close();
        return apartments;
    }

    /**
     * Creates a Apartment Object out of Database information
     *
     * @param c cursor pointing to a Apartment database entry
     * @return Apartment
     */
    private static Apartment dbToApartment(Cursor c) throws Exception {
        Long apartmentId = c.getLong(0);
        String name = c.getString(1);
        LinkedList<Room> rooms = RoomHandler.getByApartment(apartmentId);
        LinkedList<Scene> scenes = SceneHandler.getByApartment(apartmentId);
        LinkedList<Gateway> gateways = getAssociatedGateways(apartmentId);

        Geofence geofence = GeofenceHandler.get(getAssociatedGeofenceId(apartmentId));

        Apartment apartment = new Apartment(apartmentId, name, rooms, scenes, gateways, geofence);
        if (SmartphonePreferencesHandler.getCurrentApartmentId().equals(apartmentId)) {
            apartment.setActive(true);
        }
        return apartment;
    }

    private static Long getAssociatedGeofenceId(Long apartmentId) throws Exception {
        String[] columns = {ApartmentGeofenceRelationTable.COLUMN_APARTMENT_ID,
                ApartmentGeofenceRelationTable.COLUMN_GEOFENCE_ID};
        Cursor cursor = DatabaseHandler.database.query(ApartmentGeofenceRelationTable.TABLE_NAME,
                columns, ApartmentGeofenceRelationTable.COLUMN_APARTMENT_ID + "=" + apartmentId,
                null, null, null, null);
        if (!cursor.moveToFirst()) {
            return null;
        }

        Long geofenceId = cursor.getLong(1);
        cursor.close();
        return geofenceId;
    }

    /**
     * Get Gateways that are associated with an Apartment
     *
     * @param apartmentId ID of Apartment
     * @return List of Gateways
     */
    private static LinkedList<Gateway> getAssociatedGateways(Long apartmentId) throws Exception {
        LinkedList<Gateway> associatedGateways = new LinkedList<>();

        String[] columns = {
                ApartmentGatewayRelationTable.COLUMN_APARTMENT_ID,
                ApartmentGatewayRelationTable.COLUMN_GATEWAY_ID
        };
        Cursor cursor = DatabaseHandler.database.query(ApartmentGatewayRelationTable.TABLE_NAME, columns,
                ApartmentGatewayRelationTable.COLUMN_APARTMENT_ID + "==" + apartmentId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long gatewayId = cursor.getLong(1);
            Gateway gateway = GatewayHandler.get(gatewayId);
            associatedGateways.add(gateway);
            cursor.moveToNext();
        }

        cursor.close();
        return associatedGateways;
    }

    /**
     * Add relation info about associated Gateways to Database
     *
     * @param apartmentId        ID of Apartment
     * @param associatedGateways List of Gateways
     */
    private static void addAssociatedGateways(Long apartmentId, List<Gateway> associatedGateways) throws Exception {
        // add current
        for (Gateway gateway : associatedGateways) {
            ContentValues gatewayRelationValues = new ContentValues();
            gatewayRelationValues.put(ApartmentGatewayRelationTable.COLUMN_APARTMENT_ID, apartmentId);
            gatewayRelationValues.put(ApartmentGatewayRelationTable.COLUMN_GATEWAY_ID, gateway.getId());
            DatabaseHandler.database.insert(ApartmentGatewayRelationTable.TABLE_NAME, null, gatewayRelationValues);
        }
    }

    /**
     * Remove all current associated Gateways
     *
     * @param apartmentId ID of Apartment
     */
    private static void removeAssociatedGateways(Long apartmentId) throws Exception {
        // delete old associated gateways
        DatabaseHandler.database.delete(ApartmentGatewayRelationTable.TABLE_NAME,
                ApartmentGatewayRelationTable.COLUMN_APARTMENT_ID + "==" + apartmentId, null);
    }
}

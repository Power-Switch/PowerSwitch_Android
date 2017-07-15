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

package eu.power_switch.persistence.sqlite.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.persistence.sqlite.table.apartment.ApartmentGatewayRelationTable;
import eu.power_switch.persistence.sqlite.table.apartment.ApartmentGeofenceRelationTable;
import eu.power_switch.persistence.sqlite.table.apartment.ApartmentTable;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * Provides database methods for managing Apartments
 */
@Singleton
class ApartmentHandler {

    @Inject
    RoomHandler     roomHandler;
    @Inject
    GatewayHandler  gatewayHandler;
    @Inject
    GeofenceHandler geofenceHandler;
    @Inject
    SceneHandler    sceneHandler;

    @Inject
    ApartmentHandler() {
    }

    /**
     * Adds a Apartment to Database
     *
     * @param apartment Apartment
     *
     * @return ID of inserted Apartment
     */
    protected long add(@NonNull SQLiteDatabase database, Apartment apartment) throws Exception {
        ContentValues values = new ContentValues();
        values.put(ApartmentTable.COLUMN_NAME, apartment.getName());
        long apartmentId = database.insert(ApartmentTable.TABLE_NAME, null, values);
        // notice that id here may be different than
        // apartment.getId() because it was just inserted into database
        addAssociatedGateways(database, apartmentId, apartment.getAssociatedGateways());
        addGeofence(database, apartmentId, apartment);

        return apartmentId;
    }

    private void addGeofence(@NonNull SQLiteDatabase database, long apartmentId, Apartment apartment) throws Exception {
        if (apartment.getGeofence() == null) {
            return;
        }
        Long geofenceId = geofenceHandler.add(database, apartment.getGeofence());

        ContentValues values = new ContentValues();
        values.put(ApartmentGeofenceRelationTable.COLUMN_APARTMENT_ID, apartmentId);
        values.put(ApartmentGeofenceRelationTable.COLUMN_GEOFENCE_ID, geofenceId);
        database.insert(ApartmentGeofenceRelationTable.TABLE_NAME, null, values);
    }

    /**
     * Updates a Apartment in Database
     *
     * @param apartment updated Apartment
     */
    protected void update(@NonNull SQLiteDatabase database, Apartment apartment) throws Exception {
        ContentValues values = new ContentValues();
        values.put(ApartmentTable.COLUMN_NAME, apartment.getName());
        database.update(ApartmentTable.TABLE_NAME, values, ApartmentTable.COLUMN_ID + "==" + apartment.getId(), null);

        // update associated geofence (delete old, add new)
        geofenceHandler.deleteByApartmentId(database, apartment.getId());
        addGeofence(database, apartment.getId(), apartment);

        // update associated gateways
        removeAssociatedGateways(database, apartment.getId());
        addAssociatedGateways(database, apartment.getId(), apartment.getAssociatedGateways());
    }

    /**
     * Deletes Apartment from Database
     *
     * @param apartmentId ID of Apartment
     */
    protected void delete(@NonNull SQLiteDatabase database, Long apartmentId) throws Exception {
        LinkedList<Room> rooms = roomHandler.getByApartment(database, apartmentId);
        for (Room room : rooms) {
            roomHandler.delete(database, room.getId());
        }

        LinkedList<Scene> scenes = sceneHandler.getByApartment(database, apartmentId);
        for (Scene scene : scenes) {
            sceneHandler.delete(database, scene.getId());
        }

        removeAssociatedGateways(database, apartmentId);

        geofenceHandler.deleteByApartmentId(database, apartmentId);

        database.delete(ApartmentTable.TABLE_NAME, ApartmentTable.COLUMN_ID + "=" + apartmentId, null);
    }

    /**
     * Gets Apartment from Database
     *
     * @param name Name of Apartment
     *
     * @return Apartment
     */
    @NonNull
    protected Apartment get(@NonNull SQLiteDatabase database, String name) throws Exception {
        Apartment apartment = null;
        Cursor cursor = database.query(ApartmentTable.TABLE_NAME,
                ApartmentTable.ALL_COLUMNS,
                ApartmentTable.COLUMN_NAME + "=='" + name + "'",
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            apartment = dbToApartment(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(name);
        }

        cursor.close();
        return apartment;
    }

    /**
     * Gets Apartment from Database, ignoring case
     *
     * @param name Name of Apartment
     *
     * @return Apartment
     */
    @NonNull
    protected Apartment getCaseInsensitive(@NonNull SQLiteDatabase database, String name) throws Exception {
        Apartment apartment = null;
        Cursor cursor = database.query(ApartmentTable.TABLE_NAME,
                ApartmentTable.ALL_COLUMNS,
                ApartmentTable.COLUMN_NAME + "=='" + name + "' COLLATE NOCASE",
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            apartment = dbToApartment(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(name);
        }

        cursor.close();
        return apartment;
    }

    /**
     * Gets Apartment from Database
     *
     * @param id ID of Apartment
     *
     * @return Apartment
     */
    @NonNull
    protected Apartment get(@NonNull SQLiteDatabase database, Long id) throws Exception {
        Apartment apartment = null;
        Cursor cursor = database.query(ApartmentTable.TABLE_NAME,
                ApartmentTable.ALL_COLUMNS,
                ApartmentTable.COLUMN_ID + "==" + id,
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            apartment = dbToApartment(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return apartment;
    }

    /**
     * Gets all Apartments that are associated with the given gateway id
     *
     * @param gatewayId ID of gateway
     *
     * @return list of apartments
     */
    public List<Apartment> getAssociated(@NonNull SQLiteDatabase database, long gatewayId) throws Exception {
        List<Apartment> apartments = new ArrayList<>();

        Cursor cursor = database.query(ApartmentGatewayRelationTable.TABLE_NAME,
                ApartmentGatewayRelationTable.ALL_COLUMNS,
                ApartmentGatewayRelationTable.COLUMN_GATEWAY_ID + "==" + gatewayId,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Apartment apartment = get(database, cursor.getLong(0));
            apartments.add(apartment);
            cursor.moveToNext();
        }

        cursor.close();
        return apartments;
    }

    /**
     * Gets the containing Apartment of a receiver
     *
     * @param receiver Receiver
     *
     * @return containing Apartment
     */
    public Apartment get(@NonNull SQLiteDatabase database, Receiver receiver) throws Exception {
        return get(database,
                roomHandler.get(database, receiver.getRoomId())
                        .getApartmentId());
    }

    /**
     * Gets the containing Apartment of a room
     *
     * @param room Room
     *
     * @return containing Apartment
     */
    public Apartment get(@NonNull SQLiteDatabase database, Room room) throws Exception {
        return get(database, room.getApartmentId());
    }

    /**
     * Gets the containing Apartment of a scene
     *
     * @param scene Scene
     *
     * @return containing Apartment
     */
    public Apartment get(@NonNull SQLiteDatabase database, Scene scene) throws Exception {
        return get(database, scene.getApartmentId());
    }

    /**
     * Get Name of Apartment
     *
     * @param apartmentId ID of Apartment
     *
     * @return Name of Apartment, null if not found
     */
    @NonNull
    protected String getName(@NonNull SQLiteDatabase database, Long apartmentId) throws Exception {
        String[] columns = {ApartmentTable.COLUMN_NAME};
        Cursor   cursor  = database.query(ApartmentTable.TABLE_NAME, columns, ApartmentTable.COLUMN_ID + "==" + apartmentId, null, null, null, null);

        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(apartmentId));
        }

        cursor.close();
        return name;
    }

    /**
     * Get ID of an Apartment by its name
     *
     * @param name Name of Apartment, ignoring case
     *
     * @return ID of matching Apartment, might be null
     */
    @NonNull
    public Long getId(@NonNull SQLiteDatabase database, String name) throws Exception {
        String[] columns = {ApartmentTable.COLUMN_ID};
        Cursor cursor = database.query(ApartmentTable.TABLE_NAME,
                columns,
                ApartmentTable.COLUMN_NAME + "=='" + name + "' COLLATE NOCASE",
                null,
                null,
                null,
                null);

        Long id = null;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        } else {
            cursor.close();
            throw new NoSuchElementException(name);
        }

        cursor.close();
        return id;
    }

    /**
     * Get Name of all Apartment
     *
     * @return List of Apartment names
     */
    public List<String> getAllNames(@NonNull SQLiteDatabase database) throws Exception {
        List<String> apartmentNames = new ArrayList<>();

        String[] columns = {ApartmentTable.COLUMN_NAME};
        Cursor   cursor  = database.query(ApartmentTable.TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            apartmentNames.add(cursor.getString(0));
            cursor.moveToNext();
        }

        cursor.close();
        return apartmentNames;
    }

    /**
     * Gets all Apartments from Database
     *
     * @return List of Apartments
     */
    protected List<Apartment> getAll(@NonNull SQLiteDatabase database) throws Exception {
        List<Apartment> apartments = new ArrayList<>();
        Cursor          cursor     = database.query(ApartmentTable.TABLE_NAME, ApartmentTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Apartment apartment = dbToApartment(database, cursor);
            apartments.add(apartment);
            cursor.moveToNext();
        }
        cursor.close();
        return apartments;
    }

    private Long getAssociatedGeofenceId(@NonNull SQLiteDatabase database, Long apartmentId) throws Exception {
        String[] columns = {ApartmentGeofenceRelationTable.COLUMN_APARTMENT_ID, ApartmentGeofenceRelationTable.COLUMN_GEOFENCE_ID};
        Cursor cursor = database.query(ApartmentGeofenceRelationTable.TABLE_NAME,
                columns,
                ApartmentGeofenceRelationTable.COLUMN_APARTMENT_ID + "=" + apartmentId,
                null,
                null,
                null,
                null);
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
     *
     * @return List of Gateways
     */
    private LinkedList<Gateway> getAssociatedGateways(@NonNull SQLiteDatabase database, Long apartmentId) throws Exception {
        LinkedList<Gateway> associatedGateways = new LinkedList<>();

        String[] columns = {ApartmentGatewayRelationTable.COLUMN_APARTMENT_ID, ApartmentGatewayRelationTable.COLUMN_GATEWAY_ID};
        Cursor cursor = database.query(ApartmentGatewayRelationTable.TABLE_NAME,
                columns,
                ApartmentGatewayRelationTable.COLUMN_APARTMENT_ID + "==" + apartmentId,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long    gatewayId = cursor.getLong(1);
            Gateway gateway   = gatewayHandler.get(database, gatewayId);
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
    private void addAssociatedGateways(@NonNull SQLiteDatabase database, Long apartmentId, List<Gateway> associatedGateways) throws Exception {
        // add current
        for (Gateway gateway : associatedGateways) {
            ContentValues gatewayRelationValues = new ContentValues();
            gatewayRelationValues.put(ApartmentGatewayRelationTable.COLUMN_APARTMENT_ID, apartmentId);
            gatewayRelationValues.put(ApartmentGatewayRelationTable.COLUMN_GATEWAY_ID, gateway.getId());
            database.insert(ApartmentGatewayRelationTable.TABLE_NAME, null, gatewayRelationValues);
        }
    }

    /**
     * Remove all current associated Gateways
     *
     * @param apartmentId ID of Apartment
     */
    private void removeAssociatedGateways(@NonNull SQLiteDatabase database, Long apartmentId) throws Exception {
        // delete old associated gateways
        database.delete(ApartmentGatewayRelationTable.TABLE_NAME, ApartmentGatewayRelationTable.COLUMN_APARTMENT_ID + "==" + apartmentId, null);
    }

    /**
     * Creates a Apartment Object out of Database information
     *
     * @param c cursor pointing to a Apartment database entry
     *
     * @return Apartment
     */
    private Apartment dbToApartment(@NonNull SQLiteDatabase database, Cursor c) throws Exception {
        Long                apartmentId = c.getLong(0);
        String              name        = c.getString(1);
        LinkedList<Room>    rooms       = roomHandler.getByApartment(database, apartmentId);
        LinkedList<Scene>   scenes      = sceneHandler.getByApartment(database, apartmentId);
        LinkedList<Gateway> gateways    = getAssociatedGateways(database, apartmentId);

        Geofence geofence = geofenceHandler.get(database, getAssociatedGeofenceId(database, apartmentId));

        boolean isActive = SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID).equals(apartmentId);

        Apartment apartment = new Apartment(apartmentId, isActive, name, rooms, scenes, gateways, geofence);
        return apartment;
    }
}

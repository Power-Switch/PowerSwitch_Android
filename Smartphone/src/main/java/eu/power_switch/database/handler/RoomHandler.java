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
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import eu.power_switch.database.table.room.RoomGatewayRelationTable;
import eu.power_switch.database.table.room.RoomTable;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * Provides database methods for managing Rooms
 */
abstract class RoomHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private RoomHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Adds a Room to Database
     *
     * @param room Room
     */
    protected static long add(Room room) throws Exception {
        ContentValues values = new ContentValues();
        values.put(RoomTable.COLUMN_NAME, room.getName());
        values.put(RoomTable.COLUMN_APARTMENT_ID, room.getApartmentId());
        values.put(RoomTable.COLUMN_COLLAPSED, room.isCollapsed());
        long roomId = DatabaseHandler.database.insert(RoomTable.TABLE_NAME, null, values);

        addAssociatedGateways(roomId, room.getAssociatedGateways());

        return roomId;
    }

    /**
     * Updates a Room in Database
     *
     * @param id      ID of Room
     * @param newName new Room name
     */
    protected static void update(Long id, String newName, List<Gateway> associatedGateways) throws Exception {
        ContentValues values = new ContentValues();
        values.put(RoomTable.COLUMN_NAME, newName);
        DatabaseHandler.database.update(RoomTable.TABLE_NAME, values, RoomTable.COLUMN_ID + "==" + id, null);

        // update associated Gateways
        removeAssociatedGateways(id);
        addAssociatedGateways(id, associatedGateways);
    }

    /**
     * Updates the collapsed state of a Room in Database
     *
     * @param id          ID of Room
     * @param isCollapsed new collapsed state of Room
     */
    protected static void updateCollapsed(Long id, boolean isCollapsed) throws Exception {
        ContentValues values = new ContentValues();
        values.put(RoomTable.COLUMN_COLLAPSED, isCollapsed);
        DatabaseHandler.database.update(RoomTable.TABLE_NAME, values, RoomTable.COLUMN_ID + "==" + id, null);
    }


    /**
     * Sets the position of a Room
     *
     * @param roomId   ID of Room
     * @param position position in apartment
     */
    protected static void setPosition(Long roomId, Long position) {
        ContentValues values = new ContentValues();
        values.put(RoomTable.COLUMN_POSITION, position);

        DatabaseHandler.database.update(RoomTable.TABLE_NAME, values,
                RoomTable.COLUMN_ID + "=" + roomId, null);
    }

    /**
     * Deletes Room from Database
     *
     * @param id ID of Room
     */
    protected static void delete(Long id) throws Exception {
        ActionHandler.deleteByRoomId(id);

        deleteReceiversOfRoom(id);

        removeAssociatedGateways(id);
        DatabaseHandler.database.delete(RoomTable.TABLE_NAME, RoomTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Deletes Receivers contained in a Room
     *
     * @param roomId ID of Room
     */
    private static void deleteReceiversOfRoom(Long roomId) throws Exception {
        ArrayList<Receiver> receivers = ReceiverHandler.getByRoom(roomId);
        for (Receiver receiver : receivers) {
            ReceiverHandler.delete(receiver.getId());
        }
    }

    /**
     * Gets Room from Database
     *
     * @param name Name of Room
     * @return Room
     */
    @NonNull
    protected static Room get(String name) throws Exception {
        Room room = null;
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, RoomTable.ALL_COLUMNS, RoomTable.COLUMN_NAME + "=='" +
                name + "'", null, null, null, null);

        if (cursor.moveToFirst()) {
            room = dbToRoom(cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(name);
        }

        cursor.close();
        return room;
    }

    /**
     * Gets Room from Database, ignoring case
     *
     * @param name Name of Room
     * @return Room
     */
    @NonNull
    protected static Room getCaseInsensitive(String name) throws Exception {
        Room room = null;
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, RoomTable.ALL_COLUMNS, RoomTable.COLUMN_NAME + "=='" +
                name.toLowerCase() + "' COLLATE NOCASE", null, null, null, null);
        cursor.moveToFirst();

        if (cursor.moveToFirst()) {
            room = dbToRoom(cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(name);
        }

        cursor.close();
        return room;
    }

    /**
     * Gets Room from Database
     *
     * @param id ID of Room
     * @return Room
     */
    @NonNull
    protected static Room get(Long id) throws Exception {
        Room room = null;
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, RoomTable.ALL_COLUMNS,
                RoomTable.COLUMN_ID + "==" + id, null, null, null, null);

        if (cursor.moveToFirst()) {
            room = dbToRoom(cursor);
            room.setCollapsed(SmartphonePreferencesHandler.getAutoCollapseRooms());
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return room;
    }

    /**
     * Get Rooms of a specific Apartment
     *
     * @param apartmentId ID of Apartment
     * @return list of Rooms
     */
    public static LinkedList<Room> getByApartment(Long apartmentId) throws Exception {
        LinkedList<Room> rooms = new LinkedList<>();
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, RoomTable.ALL_COLUMNS, RoomTable.COLUMN_APARTMENT_ID +
                        "==" + apartmentId,
                null, null, null, RoomTable.COLUMN_POSITION + " ASC");
        cursor.moveToFirst();

        boolean autoCollapseRooms = SmartphonePreferencesHandler.getAutoCollapseRooms();

        while (!cursor.isAfterLast()) {
            Room room = dbToRoom(cursor);
            room.setCollapsed(autoCollapseRooms);
            rooms.add(room);
            cursor.moveToNext();
        }
        cursor.close();
        return rooms;
    }

    /**
     * Get all room IDs of a specific Apartment.
     *
     * @return a list of room IDs
     */
    public static ArrayList<Long> getIdsByApartment(Long apartmentId) throws Exception {
        ArrayList<Long> roomIds = new ArrayList<>();
        String[] columns = new String[]{RoomTable.COLUMN_ID};
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, columns, RoomTable.COLUMN_APARTMENT_ID +
                "==" + apartmentId, null, null, null, RoomTable.COLUMN_POSITION + " ASC");
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            roomIds.add(cursor.getLong(0));
            cursor.moveToNext();
        }
        cursor.close();
        return roomIds;
    }

    /**
     * Gets all Rooms from Database
     *
     * @return List of Rooms
     */
    protected static List<Room> getAll() throws Exception {
        List<Room> rooms = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, RoomTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        boolean autoCollapseRooms = SmartphonePreferencesHandler.getAutoCollapseRooms();

        while (!cursor.isAfterLast()) {
            Room room = dbToRoom(cursor);
            room.setCollapsed(autoCollapseRooms);
            rooms.add(room);
            cursor.moveToNext();
        }
        cursor.close();
        return rooms;
    }

    /**
     * Add relation info about associated Gateways to Database
     *
     * @param roomId             ID of Room
     * @param associatedGateways List of Gateways
     */
    private static void addAssociatedGateways(Long roomId, List<Gateway> associatedGateways) throws Exception {
        // add current
        for (Gateway gateway : associatedGateways) {
            ContentValues gatewayRelationValues = new ContentValues();
            gatewayRelationValues.put(RoomGatewayRelationTable.COLUMN_ROOM_ID, roomId);
            gatewayRelationValues.put(RoomGatewayRelationTable.COLUMN_GATEWAY_ID, gateway.getId());
            DatabaseHandler.database.insert(RoomGatewayRelationTable.TABLE_NAME, null, gatewayRelationValues);
        }
    }

    /**
     * Remove all current associated Gateways
     *
     * @param roomId ID of Room
     */
    private static void removeAssociatedGateways(Long roomId) throws Exception {
        DatabaseHandler.database.delete(RoomGatewayRelationTable.TABLE_NAME,
                RoomGatewayRelationTable.COLUMN_ROOM_ID + "==" + roomId, null);
    }

    /**
     * Get Gateways that are associated with a Room
     *
     * @param roomId ID of Room
     * @return List of Gateways
     */
    @NonNull
    private static List<Gateway> getAssociatedGateways(long roomId) throws Exception {
        List<Gateway> associatedGateways = new ArrayList<>();

        String[] columns = {
                RoomGatewayRelationTable.COLUMN_ROOM_ID,
                RoomGatewayRelationTable.COLUMN_GATEWAY_ID
        };
        Cursor cursor = DatabaseHandler.database.query(RoomGatewayRelationTable.TABLE_NAME, columns,
                RoomGatewayRelationTable.COLUMN_ROOM_ID + "==" + roomId, null, null, null, null);
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
     * Creates a Room Object out of Database information
     *
     * @param c cursor pointing to a Room database entry
     * @return Room
     */
    private static Room dbToRoom(Cursor c) throws Exception {
        long id = c.getLong(0);
        long apartmentId = c.getLong(1);
        String name = c.getString(2);
        int position = c.getInt(3);
        boolean isCollapsed = c.getInt(4) > 0;
        List<Gateway> associatedGateways = getAssociatedGateways(id);

        Room room = new Room(id, apartmentId, name, position, isCollapsed, associatedGateways);
        room.addReceivers(ReceiverHandler.getByRoom(room.getId()));
        return room;
    }
}

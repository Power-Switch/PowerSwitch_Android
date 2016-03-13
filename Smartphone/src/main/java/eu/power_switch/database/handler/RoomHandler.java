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

import eu.power_switch.database.table.room.RoomTable;
import eu.power_switch.obj.Room;
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
        return DatabaseHandler.database.insert(RoomTable.TABLE_NAME, null, values);
    }

    /**
     * Updates a Room in Database
     *
     * @param id      ID of Room
     * @param newName new Room name
     */
    protected static void update(Long id, String newName) throws Exception {
        ContentValues values = new ContentValues();
        values.put(RoomTable.COLUMN_NAME, newName);
        DatabaseHandler.database.update(RoomTable.TABLE_NAME, values, RoomTable.COLUMN_ID + "==" + id, null);
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
     * Deletes Room from Database
     *
     * @param id ID of Room
     */
    protected static void delete(Long id) throws Exception {
        ActionHandler.deleteByRoomId(id);

        deleteReceiversOfRoom(id);
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
    protected static Room get(String name) throws Exception {
        Room room = null;
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, null, RoomTable.COLUMN_NAME + "=='" +
                name + "'", null, null, null, null);

        if (cursor.moveToFirst()) {
            room = dbToRoom(cursor);
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
    protected static Room getCaseInsensitive(String name) throws Exception {
        Room room = null;
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, null, RoomTable.COLUMN_NAME + "=='" +
                name.toLowerCase() + "' COLLATE NOCASE", null, null, null, null);
        cursor.moveToFirst();

        if (cursor.moveToFirst()) {
            room = dbToRoom(cursor);
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
    protected static Room get(Long id) throws Exception {
        Room room = null;
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, null,
                RoomTable.COLUMN_ID + "==" + id, null, null, null, null);

        if (cursor.moveToFirst()) {
            room = dbToRoom(cursor);
            room.setCollapsed(SmartphonePreferencesHandler.getAutoCollapseRooms());
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
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, null, RoomTable.COLUMN_APARTMENT_ID +
                        "==" + apartmentId,
                null, null, null, null);
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

    public static ArrayList<Long> getIdsByApartment(Long apartmentId) throws Exception {
        ArrayList<Long> roomIds = new ArrayList<>();
        String[] columns = new String[]{RoomTable.COLUMN_ID};
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, columns, RoomTable.COLUMN_APARTMENT_ID +
                "==" + apartmentId, null, null, null, null);
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
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, null, null, null, null, null, null);
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
     * Creates a Room Object out of Database information
     *
     * @param c cursor pointing to a Room database entry
     * @return Room
     */
    private static Room dbToRoom(Cursor c) throws Exception {
        boolean isCollapsed = c.getInt(4) > 0;

        Room room = new Room(c.getLong(0), c.getLong(3), c.getString(1), c.getInt(2), isCollapsed);
        room.addReceivers(ReceiverHandler.getByRoom(room.getId()));
        return room;
    }

}

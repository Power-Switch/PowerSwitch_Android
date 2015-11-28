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
import java.util.List;

import eu.power_switch.database.table.room.RoomTable;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.settings.SharedPreferencesHandler;

/**
 * Provides database methods for managing Rooms
 */
public abstract class RoomHandler {

    /**
     * Adds a Room to Database
     *
     * @param room Room
     */
    protected static void add(Room room) {
        ContentValues values = new ContentValues();
        values.put(RoomTable.COLUMN_NAME, room.getName());
        DatabaseHandler.database.insert(RoomTable.TABLE_NAME, null, values);
    }

    /**
     * Updates a Room in Database
     *
     * @param id      ID of Room
     * @param newName new Room name
     */
    protected static void update(Long id, String newName) {
        ContentValues values = new ContentValues();
        values.put(RoomTable.COLUMN_NAME, newName);
        DatabaseHandler.database.update(RoomTable.TABLE_NAME, values, RoomTable.COLUMN_ID + "==" + id, null);
    }

    /**
     * Deletes Room from Database
     *
     * @param id ID of Room
     */
    protected static void delete(Long id) {
        TimerActionHandler.deleteByRoomId(id);

        deleteReceiversOfRoom(id);
        DatabaseHandler.database.delete(RoomTable.TABLE_NAME, RoomTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Deletes Receivers contained in a Room
     *
     * @param roomId ID of Room
     */
    private static void deleteReceiversOfRoom(Long roomId) {
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
    protected static Room get(String name) {
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, null, RoomTable.COLUMN_NAME + "=='" +
                name + "'", null, null, null, null);
        cursor.moveToFirst();

        Room room = dbToRoom(cursor);
        cursor.close();
        return room;
    }

    /**
     * Gets Room from Database
     *
     * @param id ID of Room
     * @return Room
     */
    protected static Room get(Long id) {
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, null, RoomTable.COLUMN_ID + "==" + id,
                null, null, null, null);
        cursor.moveToFirst();

        boolean autoCollapseRooms = SharedPreferencesHandler.getAutoCollapseRooms();

        Room room = dbToRoom(cursor);
        room.setCollapsed(autoCollapseRooms);
        cursor.close();
        return room;
    }

    /**
     * Gets all Rooms from Database
     *
     * @return List of Rooms
     */
    protected static List<Room> getAll() {
        List<Room> rooms = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(RoomTable.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();

        boolean autoCollapseRooms = SharedPreferencesHandler.getAutoCollapseRooms();

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
    private static Room dbToRoom(Cursor c) {
        Room room = new Room(c.getLong(0), c.getString(1));
        room.addReceivers(ReceiverHandler.getByRoom(room.getId()));
        return room;
    }
}

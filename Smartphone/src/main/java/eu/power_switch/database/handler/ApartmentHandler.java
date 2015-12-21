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

import eu.power_switch.database.table.apartment.ApartmentTable;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;

/**
 * Provides database methods for managing Apartments
 */
abstract class ApartmentHandler {

    /**
     * Adds a Apartment to Database
     *
     * @param apartment Apartment
     */
    protected static void add(Apartment apartment) {
        ContentValues values = new ContentValues();
        values.put(ApartmentTable.COLUMN_NAME, apartment.getName());
        DatabaseHandler.database.insert(ApartmentTable.TABLE_NAME, null, values);
    }

    /**
     * Updates a Apartment in Database
     *
     * @param id      ID of Apartment
     * @param newName new Apartment name
     */
    protected static void update(Long id, String newName) {
        ContentValues values = new ContentValues();
        values.put(ApartmentTable.COLUMN_NAME, newName);
        DatabaseHandler.database.update(ApartmentTable.TABLE_NAME, values, ApartmentTable.COLUMN_ID + "==" + id, null);
    }

    /**
     * Deletes Apartment from Database
     *
     * @param id ID of Apartment
     */
    protected static void delete(Long id) throws Exception {
        LinkedList<Room> rooms = RoomHandler.getByApartment(id);
        for (Room room : rooms) {
            RoomHandler.delete(room.getId());
        }

        DatabaseHandler.database.delete(ApartmentTable.TABLE_NAME, ApartmentTable.COLUMN_ID + "=" + id, null);
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
        Long id = c.getLong(0);
        String name = c.getString(1);
        LinkedList<Room> rooms = RoomHandler.getByApartment(id);
        LinkedList<Scene> scenes = SceneHandler.getByApartment(id);

        Apartment apartment = new Apartment(id, name, rooms, scenes);
        return apartment;
    }
}

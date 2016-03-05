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

import eu.power_switch.database.table.receiver.UniversalButtonTable;
import eu.power_switch.obj.UniversalButton;

/**
 * Provides database methods for managing Universal Buttons (used on Universal Receivers)
 */
abstract class UniversalButtonHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private UniversalButtonHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Adds UniversalButton to Database
     *
     * @param receiverId ID of Receiver
     * @param button     Button
     * @return ID of Database Button entry
     */
    protected static long addUniversalButton(Long receiverId, UniversalButton button) throws Exception {
        ContentValues values = new ContentValues();
        values.put(UniversalButtonTable.COLUMN_RECEIVER_ID, receiverId);
        values.put(UniversalButtonTable.COLUMN_NAME, button.getName());
        values.put(UniversalButtonTable.COLUMN_SIGNAL, button.getSignal());

        long newId = DatabaseHandler.database.insert(UniversalButtonTable.TABLE_NAME, null, values);
        return newId;
    }

    /**
     * Adds a list of Buttons to Database
     *
     * @param receiverId ID of Receiver*
     * @param buttons    List of Buttons
     */
    protected static void addUniversalButtons(Long receiverId, List<UniversalButton> buttons) throws Exception {
        for (UniversalButton button : buttons) {
            addUniversalButton(receiverId, button);
        }
    }

    /**
     * Deletes Button from Database
     *
     * @param id ID of Button
     */
    protected static void deleteUniversalButton(Long id) throws Exception {
        DatabaseHandler.database.delete(UniversalButtonTable.TABLE_NAME, UniversalButtonTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Deletes all Buttons associated with given Receiver from Database
     *
     * @param receiverId ID of Receiver
     */
    protected static void deleteUniversalButtons(Long receiverId) throws Exception {
        DatabaseHandler.database.delete(UniversalButtonTable.TABLE_NAME, UniversalButtonTable.COLUMN_RECEIVER_ID + "=" + receiverId, null);
    }

    /**
     * Gets Button from Database
     *
     * @param id ID of Button
     * @return Button
     */
    protected static UniversalButton getUniversalButton(Long id) throws Exception {


        Cursor cursor = DatabaseHandler.database.query(UniversalButtonTable.TABLE_NAME, null, UniversalButtonTable.COLUMN_ID + "=" + id, null, null,
                null, null);
        cursor.moveToFirst();
        UniversalButton universalButton = dbToUniversalButton(cursor);
        cursor.close();
        return universalButton;
    }

    /**
     * Gets all Buttons associated with a Receiver
     *
     * @param receiverId ID of Receiver
     * @return List of Buttons
     */
    protected static List<UniversalButton> getUniversalButtons(Long receiverId) throws Exception {
        List<UniversalButton> buttons = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(UniversalButtonTable.TABLE_NAME, null, UniversalButtonTable.COLUMN_RECEIVER_ID +
                "=" + receiverId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            buttons.add(dbToUniversalButton(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return buttons;
    }

    /**
     * Creates a Button Object out of Database information
     *
     * @param c cursor pointing to a gateway database entry
     * @return Gateway, can be null
     */
    private static UniversalButton dbToUniversalButton(Cursor c) throws Exception {
        Long id = c.getLong(0);
        Long receiverId = c.getLong(1);
        String name = c.getString(2);
        String signal = c.getString(3);

        return new UniversalButton(id, name, receiverId, signal);
    }
}
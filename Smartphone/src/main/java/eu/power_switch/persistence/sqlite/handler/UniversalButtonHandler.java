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
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.button.Button;
import eu.power_switch.persistence.sqlite.table.receiver.UniversalButtonTable;

/**
 * Provides database methods for managing Universal Buttons (used on Universal Receivers)
 */
@Singleton
class UniversalButtonHandler {

    @Inject
    UniversalButtonHandler() {
    }

    /**
     * Adds UniversalButton to Database
     *
     * @param receiverId ID of Receiver
     * @param button     Button
     *
     * @return ID of Database Button entry
     */
    protected long addUniversalButton(@NonNull SQLiteDatabase database, Long receiverId, UniversalButton button) throws Exception {
        ContentValues values = new ContentValues();
        values.put(UniversalButtonTable.COLUMN_RECEIVER_ID, receiverId);
        values.put(UniversalButtonTable.COLUMN_NAME, button.getName());
        values.put(UniversalButtonTable.COLUMN_SIGNAL, button.getSignal());

        long newId = database.insert(UniversalButtonTable.TABLE_NAME, null, values);
        return newId;
    }

    /**
     * Adds a list of Buttons to Database
     *
     * @param receiverId ID of Receiver*
     * @param buttons    List of Buttons
     */
    protected void addUniversalButtons(@NonNull SQLiteDatabase database, Long receiverId, List<Button> buttons) throws Exception {
        for (Button button : buttons) {
            addUniversalButton(database, receiverId, (UniversalButton) button);
        }
    }

    /**
     * Deletes Button from Database
     *
     * @param id ID of Button
     */
    protected void deleteUniversalButton(@NonNull SQLiteDatabase database, Long id) throws Exception {
        database.delete(UniversalButtonTable.TABLE_NAME, UniversalButtonTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Deletes all Buttons associated with given Receiver from Database
     *
     * @param receiverId ID of Receiver
     */
    protected void deleteUniversalButtons(@NonNull SQLiteDatabase database, Long receiverId) throws Exception {
        database.delete(UniversalButtonTable.TABLE_NAME, UniversalButtonTable.COLUMN_RECEIVER_ID + "=" + receiverId, null);
    }

    /**
     * Gets Button from Database
     *
     * @param id ID of Button
     *
     * @return Button
     */
    protected UniversalButton getUniversalButton(@NonNull SQLiteDatabase database, Long id) throws Exception {
        UniversalButton universalButton;
        Cursor cursor = database.query(UniversalButtonTable.TABLE_NAME,
                UniversalButtonTable.ALL_COLUMNS,
                UniversalButtonTable.COLUMN_ID + "=" + id,
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            universalButton = dbToUniversalButton(cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return universalButton;
    }

    /**
     * Gets all Buttons associated with a Receiver
     *
     * @param receiverId ID of Receiver
     *
     * @return List of Buttons
     */
    protected List<UniversalButton> getUniversalButtons(@NonNull SQLiteDatabase database, Long receiverId) throws Exception {
        List<UniversalButton> buttons = new ArrayList<>();
        Cursor cursor = database.query(UniversalButtonTable.TABLE_NAME,
                UniversalButtonTable.ALL_COLUMNS,
                UniversalButtonTable.COLUMN_RECEIVER_ID + "=" + receiverId,
                null,
                null,
                null,
                null);
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
     *
     * @return Gateway, can be null
     */
    private UniversalButton dbToUniversalButton(Cursor c) throws Exception {
        Long   id         = c.getLong(0);
        Long   receiverId = c.getLong(1);
        String name       = c.getString(2);
        String signal     = c.getString(3);

        return new UniversalButton(id, name, receiverId, signal);
    }
}
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

import java.util.NoSuchElementException;

import eu.power_switch.database.table.phone.call.CallTable;
import eu.power_switch.phone.call.CallEvent;

/**
 * Provides database methods for managing Calls
 * <p/>
 * Created by Markus on 05.04.2016.
 */
abstract class CallHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private CallHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Add a CallEvent to Database
     *
     * @param callEvent new CallEvent to insert
     * @return ID of inserted CallEvent
     */
    protected static Long add(CallEvent callEvent) throws Exception {
        ContentValues values = new ContentValues();
        values.put(CallTable.COLUMN_ACTIVE, callEvent.isActive());
        values.put(CallTable.COLUMN_NAME, callEvent.getName());

        long newId = DatabaseHandler.database.insert(CallTable.TABLE_NAME, null, values);

        // TODO: add contacts and actions to database

        return newId;
    }

    protected static CallEvent get(Long id) throws Exception {
        CallEvent callEvent = null;

        Cursor cursor = DatabaseHandler.database.query(CallTable.TABLE_NAME, CallTable.ALL_COLUMNS,
                CallTable.COLUMN_ID + "=" + id, null, null, null, null);

        if (cursor.moveToFirst()) {
            callEvent = dbToCallEvent(cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return callEvent;

    }

    private static CallEvent dbToCallEvent(Cursor c) {
        long id = c.getLong(0);
        boolean active = c.getInt(1) > 0;
        String name = c.getString(2);

        // TODO:
//        List<Contact> contacts = CallContactHandler.get(id);
//        List<Action> actions = CallActionHandler.get(id);

//        CallEvent = new CallEvent(id, active, name, contacts, actions);

        return null;
    }

}

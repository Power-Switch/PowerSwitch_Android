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
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import eu.power_switch.action.Action;
import eu.power_switch.database.table.phone.call.CallEventTable;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.PhoneConstants;

/**
 * Provides database methods for managing Calls
 * <p/>
 * Created by Markus on 05.04.2016.
 */
abstract class CallEventHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private CallEventHandler() {
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
        values.put(CallEventTable.COLUMN_ACTIVE, callEvent.isActive());
        values.put(CallEventTable.COLUMN_NAME, callEvent.getName());

        long newId = DatabaseHandler.database.insert(CallEventTable.TABLE_NAME, null, values);

        for (PhoneConstants.CallType eventCallType : PhoneConstants.CallType.values()) {
            CallEventPhoneNumberHandler.add(callEvent.getPhoneNumbers(eventCallType), newId, eventCallType);
        }

        for (PhoneConstants.CallType eventCallType : PhoneConstants.CallType.values()) {
            CallEventActionHandler.add(callEvent.getActions(eventCallType), newId, eventCallType);
        }

        return newId;
    }

    /**
     * Get a CallEvent from Database
     *
     * @param id ID of CallEvent
     * @return CallEvent
     */
    protected static CallEvent get(Long id) throws Exception {
        CallEvent callEvent = null;

        Cursor cursor = DatabaseHandler.database.query(CallEventTable.TABLE_NAME, CallEventTable.ALL_COLUMNS,
                CallEventTable.COLUMN_ID + "=" + id, null, null, null, null);

        if (cursor.moveToFirst()) {
            callEvent = dbToCallEvent(cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return callEvent;
    }

    /**
     * Get all CallEvents from Database
     *
     * @return List of CallEvents
     */
    protected static List<CallEvent> getAll() throws Exception {
        List<CallEvent> timers = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(CallEventTable.TABLE_NAME, CallEventTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            timers.add(dbToCallEvent(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return timers;
    }

    /**
     * Delete Call Event from Database
     *
     * @param id ID of CallEvent
     */
    protected static void delete(Long id) throws Exception {
        // TODO:
    }

    private static CallEvent dbToCallEvent(Cursor c) throws Exception {
        long id = c.getLong(0);
        boolean active = c.getInt(1) > 0;
        String name = c.getString(2);

        HashMap<PhoneConstants.CallType, Set<String>> phoneNumbersMap = new HashMap<>();
        for (PhoneConstants.CallType callType : PhoneConstants.CallType.values()) {
            phoneNumbersMap.put(callType, CallEventPhoneNumberHandler.get(id, callType));
        }

        HashMap<PhoneConstants.CallType, List<Action>> actionsMap = new HashMap<>();
        for (PhoneConstants.CallType callType : PhoneConstants.CallType.values()) {
            actionsMap.put(callType, CallEventActionHandler.get(id, callType));
        }

        CallEvent callEvent = new CallEvent(id, active, name, phoneNumbersMap, actionsMap);
        return callEvent;
    }
}

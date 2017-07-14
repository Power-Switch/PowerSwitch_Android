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
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.telephony.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.action.Action;
import eu.power_switch.database.table.phone.PhoneNumberTable;
import eu.power_switch.database.table.phone.call.CallEventPhoneNumberTable;
import eu.power_switch.database.table.phone.call.CallEventTable;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.PhoneConstants;

/**
 * Provides database methods for managing Calls
 * <p/>
 * Created by Markus on 05.04.2016.
 */
@Singleton
class CallEventHandler {

    private CallEventPhoneNumberHandler callEventPhoneNumberHandler;
    private CallEventActionHandler      callEventActionHandler;


    @Inject
    CallEventHandler() {
    }

    /**
     * Add a CallEvent to Database
     *
     * @param callEvent new CallEvent to insert
     *
     * @return ID of inserted CallEvent
     */
    protected Long add(@NonNull SQLiteDatabase database, CallEvent callEvent) throws Exception {
        ContentValues values = new ContentValues();
        values.put(CallEventTable.COLUMN_ACTIVE, callEvent.isActive());
        values.put(CallEventTable.COLUMN_NAME, callEvent.getName());

        long newId = database.insert(CallEventTable.TABLE_NAME, null, values);

        for (PhoneConstants.CallType eventCallType : PhoneConstants.CallType.values()) {
            callEventPhoneNumberHandler.add(database, callEvent.getPhoneNumbers(eventCallType), newId, eventCallType);
        }

        for (PhoneConstants.CallType eventCallType : PhoneConstants.CallType.values()) {
            callEventActionHandler.add(database, callEvent.getActions(eventCallType), newId, eventCallType);
        }

        return newId;
    }

    /**
     * Get a CallEvent from Database
     *
     * @param id ID of CallEvent
     *
     * @return CallEvent
     */
    protected CallEvent get(@NonNull SQLiteDatabase database, Long id) throws Exception {
        CallEvent callEvent = null;

        Cursor cursor = database.query(CallEventTable.TABLE_NAME,
                CallEventTable.ALL_COLUMNS,
                CallEventTable.COLUMN_ID + "=" + id,
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            callEvent = dbToCallEvent(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return callEvent;
    }

    /**
     * Get all CallEvents that are associated with the specified phoneNumber
     *
     * @param phoneNumber phone number used in CallEvents
     *
     * @return List of CallEvents, may be empty but never null
     */
    @NonNull
    protected List<CallEvent> get(@NonNull SQLiteDatabase database, String phoneNumber) throws Exception {
        List<CallEvent> callEvents = new ArrayList<>();

        Cursor cursor = database.query(PhoneNumberTable.TABLE_NAME, PhoneNumberTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long phoneNumberId = cursor.getLong(0);

            if (PhoneNumberUtils.compare(phoneNumber, cursor.getString(1))) {
                Cursor cursor1 = database.query(CallEventPhoneNumberTable.TABLE_NAME,
                        CallEventPhoneNumberTable.ALL_COLUMNS,
                        CallEventPhoneNumberTable.COLUMN_PHONE_NUMBER_ID + "=" + phoneNumberId,
                        null,
                        null,
                        null,
                        null);
                cursor1.moveToFirst();
                long callEventId = cursor.getLong(0);
                callEvents.add(get(database, callEventId));
                cursor1.close();
            }

            cursor.moveToNext();
        }

        cursor.close();
        return callEvents;
    }

    /**
     * Get all CallEvents from Database
     *
     * @return List of CallEvents
     */
    @NonNull
    protected List<CallEvent> getAll(@NonNull SQLiteDatabase database) throws Exception {
        List<CallEvent> callEvents = new ArrayList<>();
        Cursor          cursor     = database.query(CallEventTable.TABLE_NAME, CallEventTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            callEvents.add(dbToCallEvent(database, cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return callEvents;
    }

    /**
     * @param callEvent
     */
    protected void update(@NonNull SQLiteDatabase database, CallEvent callEvent) {
        // TODO:
    }

    /**
     * Delete Call Event from Database
     *
     * @param id ID of CallEvent
     */
    protected void delete(@NonNull SQLiteDatabase database, Long id) throws Exception {
        callEventPhoneNumberHandler.deleteByCallEvent(database, id);
        callEventActionHandler.deleteByCallEvent(database, id);

        database.delete(CallEventTable.TABLE_NAME, CallEventTable.COLUMN_ID + "=" + id, null);
    }

    private CallEvent dbToCallEvent(@NonNull SQLiteDatabase database, Cursor c) throws Exception {
        long    id     = c.getLong(0);
        boolean active = c.getInt(1) > 0;
        String  name   = c.getString(2);

        HashMap<PhoneConstants.CallType, Set<String>> phoneNumbersMap = new HashMap<>();
        for (PhoneConstants.CallType callType : PhoneConstants.CallType.values()) {
            phoneNumbersMap.put(callType, callEventPhoneNumberHandler.get(database, id, callType));
        }

        HashMap<PhoneConstants.CallType, List<Action>> actionsMap = new HashMap<>();
        for (PhoneConstants.CallType callType : PhoneConstants.CallType.values()) {
            actionsMap.put(callType, callEventActionHandler.get(database, id, callType));
        }

        CallEvent callEvent = new CallEvent(id, active, name, phoneNumbersMap, actionsMap);
        return callEvent;
    }
}

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

package eu.power_switch.persistence.data.sqlite.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.persistence.data.sqlite.table.phone.PhoneNumberTable;
import eu.power_switch.persistence.data.sqlite.table.phone.call.CallEventPhoneNumberTable;
import eu.power_switch.shared.constants.PhoneConstants;
import timber.log.Timber;

/**
 * Created by Markus on 12.04.2016.
 */
@Singleton
class CallEventPhoneNumberHandler {

    @Inject
    PhoneNumberHandler phoneNumberHandler;

    @Inject
    CallEventPhoneNumberHandler() {
    }

    protected void add(@NonNull SQLiteDatabase database, Set<String> phoneNumbers, long callEventId,
                       PhoneConstants.CallType eventCallType) throws Exception {
        if (phoneNumbers == null) {
            Timber.w("phoneNumbers was null! nothing added to database");
            return;
        }

        for (String phoneNumber : phoneNumbers) {
            long phoneNumberId = phoneNumberHandler.add(database, phoneNumber);

            // add to relational table
            ContentValues values = new ContentValues();
            values.put(CallEventPhoneNumberTable.COLUMN_CALL_EVENT_ID, callEventId);
            values.put(CallEventPhoneNumberTable.COLUMN_EVENT_TYPE_ID, eventCallType.getId());
            values.put(CallEventPhoneNumberTable.COLUMN_PHONE_NUMBER_ID, phoneNumberId);
            database.insert(CallEventPhoneNumberTable.TABLE_NAME, null, values);
        }
    }

    /**
     * Get list of phone numbers
     *
     * @param callEventId ID of call event
     * @param callType    type
     *
     * @return Set of phone numbers
     */
    protected Set<String> get(@NonNull SQLiteDatabase database, long callEventId, PhoneConstants.CallType callType) throws Exception {
        Set<String> phoneNumbers = new HashSet<>();

        Cursor cursor = database.query(CallEventPhoneNumberTable.TABLE_NAME,
                CallEventPhoneNumberTable.ALL_COLUMNS,
                CallEventPhoneNumberTable.COLUMN_CALL_EVENT_ID + "==" + callEventId + " AND " + CallEventPhoneNumberTable.COLUMN_EVENT_TYPE_ID + "==" + callType.getId(),
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long phoneNumberId = cursor.getLong(2);
            phoneNumbers.add(phoneNumberHandler.get(database, phoneNumberId));
            cursor.moveToNext();
        }

        cursor.close();
        return phoneNumbers;
    }

    /**
     * Delete phone numbers associated with a specific CallEvent
     *
     * @param callEventId ID of CallEvent
     */
    protected void deleteByCallEvent(@NonNull SQLiteDatabase database, Long callEventId) throws Exception {
        Cursor cursor = database.query(CallEventPhoneNumberTable.TABLE_NAME,
                CallEventPhoneNumberTable.ALL_COLUMNS,
                CallEventPhoneNumberTable.COLUMN_CALL_EVENT_ID + "==" + callEventId,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long phoneNumberId = cursor.getLong(2);
            database.delete(PhoneNumberTable.TABLE_NAME, PhoneNumberTable.COLUMN_ID + "==" + phoneNumberId, null);
            cursor.moveToNext();
        }

        cursor.close();
        database.delete(CallEventPhoneNumberTable.TABLE_NAME, CallEventPhoneNumberTable.COLUMN_CALL_EVENT_ID + "==" + callEventId, null);
    }
}

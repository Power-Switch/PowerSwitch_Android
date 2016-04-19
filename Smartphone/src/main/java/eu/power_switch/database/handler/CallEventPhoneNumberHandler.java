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

import java.util.HashSet;
import java.util.Set;

import eu.power_switch.database.table.phone.call.CallEventPhoneNumberTable;
import eu.power_switch.shared.constants.PhoneConstants;

/**
 * Created by Markus on 12.04.2016.
 */
abstract class CallEventPhoneNumberHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private CallEventPhoneNumberHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    protected static void add(Set<String> phoneNumbers, long callEventId, PhoneConstants.CallType eventCallType) throws Exception {
        for (String phoneNumber : phoneNumbers) {
            long phoneNumberId = PhoneNumberHandler.add(phoneNumber);

            // add to relational table
            ContentValues values = new ContentValues();
            values.put(CallEventPhoneNumberTable.COLUMN_CALL_EVENT_ID, callEventId);
            values.put(CallEventPhoneNumberTable.COLUMN_EVENT_TYPE_ID, eventCallType.getId());
            values.put(CallEventPhoneNumberTable.COLUMN_PHONE_NUMBER_ID, phoneNumberId);
            DatabaseHandler.database.insert(CallEventPhoneNumberTable.TABLE_NAME, null, values);
        }
    }

    protected static Set<String> get(long callEventId, PhoneConstants.CallType callType) throws Exception {
        // TODO:
        return new HashSet<>();
    }
}

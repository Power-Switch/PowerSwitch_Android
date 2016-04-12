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

import eu.power_switch.database.table.phone.PhoneNumberTable;

/**
 * Created by Markus on 11.04.2016.
 */
abstract class PhoneNumberHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PhoneNumberHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    protected static Long add(String phoneNumber) throws Exception {
        ContentValues values = new ContentValues();
        values.put(PhoneNumberTable.COLUMN_PHONE_NUMBER, phoneNumber);
        return DatabaseHandler.database.insert(PhoneNumberTable.TABLE_NAME, null, values);
    }

    protected static String get(long id) throws Exception {
        String phoneNumber = null;
        Cursor cursor = DatabaseHandler.database.query(PhoneNumberTable.TABLE_NAME, PhoneNumberTable.ALL_COLUMNS,
                PhoneNumberTable.COLUMN_ID + "=" + id, null, null, null, null);

        if (cursor.moveToFirst()) {
            phoneNumber = cursor.getString(1);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return phoneNumber;
    }

}

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

package eu.power_switch.persistence.table.phone.call;

import android.database.sqlite.SQLiteDatabase;

import eu.power_switch.persistence.table.phone.ContactTable;

/**
 * GeofenceAction table description
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class CallEventPhoneNumberTable {

    public static final String TABLE_NAME = "call_event_phone_numbers";
    public static final String COLUMN_CALL_EVENT_ID = "call_event_id";
    public static final String COLUMN_PHONE_NUMBER_ID = "phone_number_id";
    public static final String COLUMN_EVENT_TYPE_ID = "event_type_id";

    public static final String[] ALL_COLUMNS = {COLUMN_CALL_EVENT_ID, COLUMN_EVENT_TYPE_ID, COLUMN_PHONE_NUMBER_ID};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_CALL_EVENT_ID    + " integer not null," +
            COLUMN_PHONE_NUMBER_ID  + " integer not null," +
            COLUMN_EVENT_TYPE_ID    + " text not null," +
            "FOREIGN KEY(" + COLUMN_CALL_EVENT_ID + ") REFERENCES " +
                CallEventTable.TABLE_NAME + "(" + CallEventTable.COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_PHONE_NUMBER_ID+ ") REFERENCES " +
                ContactTable.TABLE_NAME + "(" + ContactTable.COLUMN_ID + "), " +
            "PRIMARY KEY (" + COLUMN_CALL_EVENT_ID + ", " + COLUMN_PHONE_NUMBER_ID + ")" +
        ");";
    //@formatter:on

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 19) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}

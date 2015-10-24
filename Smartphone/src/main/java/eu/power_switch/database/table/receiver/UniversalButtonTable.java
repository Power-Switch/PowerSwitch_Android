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

package eu.power_switch.database.table.receiver;

import android.database.sqlite.SQLiteDatabase;

/**
 * Universal Button table description
 */
public class UniversalButtonTable {

    public static final String TABLE_NAME = "universal_button";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_RECEIVER_ID = "receiver_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SIGNAL = "signal";

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " integer primary key autoincrement," +
            COLUMN_RECEIVER_ID + " integer not null," +
            COLUMN_NAME + " text not null," +
            COLUMN_SIGNAL + " text not null," +
                "FOREIGN KEY(" + COLUMN_RECEIVER_ID + ") REFERENCES " +
                ReceiverTable.TABLE_NAME + "(" + ReceiverTable.COLUMN_ID +
            ")" +
    ");";
    //@formatter:on

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
            case 2:
            case 3:
            case 4:
                onCreate(db);
                break;

        }
    }
}

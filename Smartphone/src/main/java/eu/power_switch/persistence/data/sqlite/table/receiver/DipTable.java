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

package eu.power_switch.persistence.data.sqlite.table.receiver;

import android.database.sqlite.SQLiteDatabase;

/**
 * Dip table description
 */
public class DipTable {

    public static final String TABLE_NAME = "dips";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_STATE = "active";
    public static final String COLUMN_RECEIVER_ID = "receiver";

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID           + " integer primary key autoincrement," +
            COLUMN_POSITION     + " integer not null," +
            COLUMN_STATE        + " integer not null," +
            COLUMN_RECEIVER_ID  + " integer not null," +
                "FOREIGN KEY(" + COLUMN_RECEIVER_ID  + ") REFERENCES " +
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
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
                break;
            case 2:
                break;

        }
    }
}

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

import eu.power_switch.database.table.room.RoomTable;

/**
 * Receiver table description
 */
public class ReceiverTable {

    public static final String TABLE_NAME = "receiver";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_MODEL = "model";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CLASSNAME = "className";
    public static final String COLUMN_ROOM_ID = "room";
    public static final String COLUMN_POSITION_IN_ROOM = "positionInRoom";
    public static final String COLUMN_LAST_ACTIVATED_BUTTON_ID = "lastActivatedButton";

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " integer primary key autoincrement," +
            COLUMN_NAME + " text not null," +
            COLUMN_MODEL + " text not null," +
            COLUMN_TYPE + " text not null," +
            COLUMN_CLASSNAME + " text not null," +
            COLUMN_ROOM_ID + " integer not null," +
            COLUMN_POSITION_IN_ROOM + " integer," +
            COLUMN_LAST_ACTIVATED_BUTTON_ID + " integer," +
                "FOREIGN KEY(" + COLUMN_ROOM_ID + ") REFERENCES " +
                RoomTable.TABLE_NAME + "(" + RoomTable.COLUMN_ID +
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
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LAST_ACTIVATED_BUTTON_ID + " int;");
                break;
        }
    }
}

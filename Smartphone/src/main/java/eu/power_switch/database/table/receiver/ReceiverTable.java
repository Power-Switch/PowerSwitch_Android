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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import eu.power_switch.database.table.room.RoomTable;
import eu.power_switch.obj.receiver.Receiver;
import timber.log.Timber;

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
    public static final String COLUMN_REPETITION_AMOUNT = "repetitionAmount";

    public static final String[] ALL_COLUMNS = {
            COLUMN_ID, COLUMN_NAME, COLUMN_MODEL, COLUMN_TYPE, COLUMN_CLASSNAME, COLUMN_ROOM_ID,
            COLUMN_POSITION_IN_ROOM, COLUMN_LAST_ACTIVATED_BUTTON_ID, COLUMN_REPETITION_AMOUNT};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID                       + " integer primary key autoincrement," +
            COLUMN_NAME                     + " text not null," +
            COLUMN_MODEL                    + " text not null," +
            COLUMN_TYPE                     + " text not null," +
            COLUMN_CLASSNAME                + " text not null," +
            COLUMN_ROOM_ID                  + " integer not null," +
            COLUMN_POSITION_IN_ROOM         + " integer," +
            COLUMN_LAST_ACTIVATED_BUTTON_ID + " integer," +
            COLUMN_REPETITION_AMOUNT        + " integer not null," +
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
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LAST_ACTIVATED_BUTTON_ID + " int;");
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                // update receiver classpath
                String[] columns = {COLUMN_ID, COLUMN_CLASSNAME, COLUMN_TYPE};
                Cursor cursor = db.query(TABLE_NAME, columns,
                        null, null, null, null, null);
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    long id = cursor.getLong(0);
                    String className = cursor.getString(1);
                    String type = cursor.getString(2);

                    String newClassName;
                    if (Receiver.Type.UNIVERSAL.toString().equals(type)) {
                        newClassName = "eu.power_switch.obj.receiver.UniversalReceiver";
                    } else {
                        newClassName = className.replace("eu.power_switch.obj.device.", "eu.power_switch.obj.receiver.device.");
                    }

                    Timber.d("old className: " + className);
                    Timber.d("new className: " + newClassName);

                    ContentValues values = new ContentValues();
                    values.put(COLUMN_CLASSNAME, newClassName);
                    db.update(TABLE_NAME, values, COLUMN_ID + "=" + id, null);

                    cursor.moveToNext();
                }

                cursor.close();
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_REPETITION_AMOUNT + " int not null DEFAULT 1;");
        }
    }
}

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

package eu.power_switch.database.table.timer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import eu.power_switch.database.table.action.ActionTable;
import eu.power_switch.database.table.action.ReceiverActionTable;
import eu.power_switch.database.table.action.RoomActionTable;
import eu.power_switch.database.table.action.SceneActionTable;

/**
 * TimerAction table description
 * <p/>
 * Created by Markus on 24.09.2015.
 */
public class TimerActionTable {

    public static final String TABLE_NAME = "timer_actions";
    public static final String COLUMN_TIMER_ID = "timer_id";
    public static final String COLUMN_ACTION_ID = "action_id";

    public static final String[] ALL_COLUMNS = {COLUMN_TIMER_ID, COLUMN_ACTION_ID};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_TIMER_ID + " integer not null," +
            COLUMN_ACTION_ID + " integer not null," +
            "FOREIGN KEY(" + COLUMN_TIMER_ID + ") REFERENCES " +
                TimerTable.TABLE_NAME + "(" + TimerTable.COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_ACTION_ID+ ") REFERENCES " +
                ActionTable.TABLE_NAME + "(" + ActionTable.COLUMN_ID + "), " +
            "PRIMARY KEY (" + COLUMN_TIMER_ID + ", " + COLUMN_ACTION_ID + ")" +
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
            case 5:
            case 6:
                db.execSQL("DROP TABLE IF EXISTS " + "timer_action");
                onCreate(db);
                break;
            case 7:
            case 8:
                // upgrading data happens in Database.java
                onCreate(db);
                break;
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_ACTION_ID}, null, null, null, null, null);
                c.moveToFirst();

                while (!c.isAfterLast()) {
                    long actionId = c.getLong(0);

                    Cursor cursor = db.query(ActionTable.TABLE_NAME, ActionTable.ALL_COLUMNS,
                            ActionTable.COLUMN_ID + "=" + actionId, null, null, null, null);

                    if (!cursor.moveToFirst()) {
                        db.delete(ActionTable.TABLE_NAME, ActionTable.COLUMN_ID + "=" + actionId, null);
                        db.delete(ReceiverActionTable.TABLE_NAME, ReceiverActionTable.COLUMN_ACTION_ID + "=" + actionId, null);
                        db.delete(RoomActionTable.TABLE_NAME, RoomActionTable.COLUMN_ACTION_ID + "=" + actionId, null);
                        db.delete(SceneActionTable.TABLE_NAME, SceneActionTable.COLUMN_ACTION_ID + "=" + actionId, null);
                    }

                    cursor.close();

                    c.moveToNext();
                }

                c.close();
            case 19:

        }
    }
}

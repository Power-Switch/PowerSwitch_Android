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

package eu.power_switch.persistence.table.alarm_clock.sleep_as_android;

import android.database.sqlite.SQLiteDatabase;

/**
 * SleepAsAndroidAction table description
 * <p/>
 * Created by Markus on 24.09.2015.
 */
public class SleepAsAndroidActionTable {

    public static final String TABLE_NAME = "sleep_as_android_action";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ALARM_TYPE_ID = "alarm_type_id";
    public static final String COLUMN_ACTION_ID = "action_id";

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ALARM_TYPE_ID    + " integer not null," +
            COLUMN_ACTION_ID        + " integer not null" +
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
            case 7:
            case 8:
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
                break;

        }
    }
}

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

package eu.power_switch.database.table.scene;

import android.database.sqlite.SQLiteDatabase;

/**
 * Scene table description
 */
public class SceneTable {

    public static final String TABLE_NAME = "scenes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_APARTMENT_ID = "apartment_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_POSITION = "position";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_APARTMENT_ID, COLUMN_NAME, COLUMN_POSITION};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " integer primary key autoincrement," +
            COLUMN_NAME + " text not null, " +
            COLUMN_POSITION + " integer," +
            COLUMN_APARTMENT_ID + " integer not null " +
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
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
                break;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_APARTMENT_ID +
                        " integer not null DEFAULT 0");
        }
    }
}
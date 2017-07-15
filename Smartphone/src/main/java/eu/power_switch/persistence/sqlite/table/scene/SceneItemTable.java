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

package eu.power_switch.persistence.table.scene;

import android.database.sqlite.SQLiteDatabase;

/**
 * SceneItem table description
 */
public class SceneItemTable {

    public static final String TABLE_NAME              = "scene_items";
    public static final String COLUMN_ID               = "_id";
    public static final String COLUMN_SCENE_ID         = "scene_id";
    public static final String COLUMN_RECEIVER_ID      = "receiver_id";
    public static final String COLUMN_ACTIVE_BUTTON_ID = "active_button_id";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_SCENE_ID, COLUMN_RECEIVER_ID, COLUMN_ACTIVE_BUTTON_ID};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID               + " integer primary key autoincrement," +
            COLUMN_RECEIVER_ID      + " integer not null," +
            COLUMN_ACTIVE_BUTTON_ID + " integer not null," +
            COLUMN_SCENE_ID         + " integer," +
                "FOREIGN KEY(" + COLUMN_SCENE_ID + ") REFERENCES " +
                SceneTable.TABLE_NAME + "(" + SceneTable.COLUMN_ID +
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
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
                break;
            case 3:
                break;
        }
    }
}
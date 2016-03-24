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

package eu.power_switch.database.table.geofence;

import android.database.sqlite.SQLiteDatabase;

import eu.power_switch.database.table.action.ActionTable;

/**
 * GeofenceAction table description
 * <p/>
 * Created by Markus on 24.09.2015.
 */
public class GeofenceActionTable {

    public static final String TABLE_NAME = "geofence_actions";
    public static final String COLUMN_GEOFENCE_ID = "geofence_id";
    public static final String COLUMN_ACTION_ID = "action_id";
    public static final String COLUMN_EVENT_TYPE = "event_type";

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_GEOFENCE_ID + " integer not null," +
            COLUMN_ACTION_ID + " integer not null," +
            COLUMN_EVENT_TYPE + " text not null," +
            "FOREIGN KEY(" + COLUMN_GEOFENCE_ID + ") REFERENCES " +
                GeofenceTable.TABLE_NAME + "(" + GeofenceTable.COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_ACTION_ID+ ") REFERENCES " +
                ActionTable.TABLE_NAME + "(" + ActionTable.COLUMN_ID + "), " +
            "PRIMARY KEY (" + COLUMN_GEOFENCE_ID + ", " + COLUMN_ACTION_ID + ")" +
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
            case 9:
            case 10:
            case 11:
            case 12:
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
                break;
        }
    }
}

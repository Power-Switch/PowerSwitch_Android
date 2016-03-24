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

import eu.power_switch.google_play_services.geofence.Geofence;

/**
 * Geofence table description
 */
public class GeofenceTable {

    public static final String TABLE_NAME = "geofences";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_RADIUS = "radius";
    public static final String COLUMN_SNAPSHOT = "snapshot";
    public static final String COLUMN_STATE = "state";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_ACTIVE, COLUMN_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_RADIUS, COLUMN_SNAPSHOT, COLUMN_STATE};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " integer primary key autoincrement," +
            COLUMN_ACTIVE + " integer not null, " +
            COLUMN_NAME + " text not null, " +
            COLUMN_LATITUDE + " real not null, " +
            COLUMN_LONGITUDE + " real not null, " +
            COLUMN_RADIUS + " real not null, " +
            COLUMN_SNAPSHOT + " blob," +
            COLUMN_STATE + " text not null" +
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
            case 13:
            case 14:
            case 15:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_STATE + " text not null DEFAULT " + Geofence.STATE_NONE + ";");
        }
    }
}

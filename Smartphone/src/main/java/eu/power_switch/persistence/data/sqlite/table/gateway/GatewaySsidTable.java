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

package eu.power_switch.persistence.data.sqlite.table.gateway;

import android.database.sqlite.SQLiteDatabase;

/**
 * Gateway SSID Table description
 * <p/>
 * Created by Markus on 26.03.2016.
 */
public class GatewaySsidTable {

    public static final String TABLE_NAME = "gateway_ssids";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_GATEWAY_ID = "gateway_id";
    public static final String COLUMN_SSID = "ssid";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_GATEWAY_ID, COLUMN_SSID};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID           + " integer primary key autoincrement," +
            COLUMN_GATEWAY_ID   + " integer not null, " +
            COLUMN_SSID         + " text not null " +
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
            case 13:
            case 14:
            case 15:
                onCreate(db);
                break;
        }
    }

}

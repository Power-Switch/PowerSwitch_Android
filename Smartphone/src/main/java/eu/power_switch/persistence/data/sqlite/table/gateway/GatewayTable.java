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

import eu.power_switch.shared.constants.DatabaseConstants;

/**
 * Gateway table description
 */
public class GatewayTable {

    public static final String TABLE_NAME = "gateways";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_MODEL = "model";
    public static final String COLUMN_FIRMWARE = "firmware";
    public static final String COLUMN_LAN_ADDRESS = "address";
    public static final String COLUMN_LAN_PORT = "port";
    public static final String COLUMN_WAN_ADDRESS = "wan_address";
    public static final String COLUMN_WAN_PORT = "wan_port";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_ACTIVE, COLUMN_NAME, COLUMN_MODEL, COLUMN_FIRMWARE, COLUMN_LAN_ADDRESS, COLUMN_LAN_PORT, COLUMN_WAN_ADDRESS, COLUMN_WAN_PORT};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID           + " integer primary key autoincrement," +
            COLUMN_ACTIVE       + " integer not null, " +
            COLUMN_NAME         + " text not null, " +
            COLUMN_MODEL        + " text not null, " +
            COLUMN_FIRMWARE     + " text not null, " +
            COLUMN_LAN_ADDRESS  + " text not null, " +
            COLUMN_LAN_PORT     + " integer not null," +
            COLUMN_WAN_ADDRESS  + " text not null, " +
            COLUMN_WAN_PORT     + " integer not null" +
            ");";
    //@formatter:on

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 16) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_WAN_ADDRESS + " text not null DEFAULT '';");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_WAN_PORT + " integer not null DEFAULT " + DatabaseConstants.INVALID_GATEWAY_PORT + ";");
        }
    }
}

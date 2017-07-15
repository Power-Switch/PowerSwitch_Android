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

package eu.power_switch.persistence.sqlite.table.room;

import android.database.sqlite.SQLiteDatabase;

import eu.power_switch.persistence.sqlite.table.gateway.GatewayTable;

/**
 * Room Gateway relation table description
 * <p/>
 * Created by Markus on 24.04.2016.
 */
public class RoomGatewayRelationTable {

    public static final String TABLE_NAME = "room_gateway_relation";
    public static final String COLUMN_ROOM_ID = "room_id";
    public static final String COLUMN_GATEWAY_ID = "gateway_id";

    public static final String[] ALL_COLUMNS = {COLUMN_ROOM_ID, COLUMN_GATEWAY_ID};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ROOM_ID      + " integer not null," +
            COLUMN_GATEWAY_ID   + " integer not null," +
            "FOREIGN KEY(" + COLUMN_ROOM_ID + ") REFERENCES " +
                RoomTable.TABLE_NAME + "(" + RoomTable.COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_GATEWAY_ID + ") REFERENCES " +
                GatewayTable.TABLE_NAME + "(" + GatewayTable.COLUMN_ID + "), " +
            "PRIMARY KEY (" + COLUMN_ROOM_ID + ", " + COLUMN_GATEWAY_ID + ")" +
            ");";
    //@formatter:on

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 17) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
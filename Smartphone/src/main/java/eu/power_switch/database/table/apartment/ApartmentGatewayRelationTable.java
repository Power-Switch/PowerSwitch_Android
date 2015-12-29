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

package eu.power_switch.database.table.apartment;

import android.database.sqlite.SQLiteDatabase;

import eu.power_switch.database.table.gateway.GatewayTable;

/**
 * Apartment Gateway relation table description
 */
public class ApartmentGatewayRelationTable {

    public static final String TABLE_NAME = "apartment_gateway_relation";
    public static final String COLUMN_APARTMENT_ID = "apartment_id";
    public static final String COLUMN_GATEWAY_ID = "gateway_id";

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_APARTMENT_ID + " integer not null," +
            COLUMN_GATEWAY_ID + " integer not null," +
            "FOREIGN KEY(" + COLUMN_APARTMENT_ID + ") REFERENCES " +
                ApartmentTable.TABLE_NAME + "(" + ApartmentTable.COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_GATEWAY_ID + ") REFERENCES " +
                GatewayTable.TABLE_NAME + "(" + GatewayTable.COLUMN_ID + "), " +
            "PRIMARY KEY (" + COLUMN_APARTMENT_ID + ", " + COLUMN_GATEWAY_ID + ")" +
            ");";
    //formatter:on

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 11) {
            onCreate(db);
        }
    }
}
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

package eu.power_switch.persistence.sqlite.table.apartment;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * Apartment table description
 */
public class ApartmentTable {

    public static final String TABLE_NAME      = "apartments";
    public static final String COLUMN_ID       = "_id";
    public static final String COLUMN_NAME     = "name";
    public static final String COLUMN_POSITION = "position";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_POSITION};

    //@formatter:off
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID           + " integer primary key autoincrement," +
            COLUMN_NAME         + " text not null, " +
            COLUMN_POSITION     + " integer" +
            ");";
    //@formatter:on

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, SmartphonePreferencesHandler smartphonePreferencesHandler) {
        if (oldVersion <= 10) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);

            String        apartmentName = "Home";
            ContentValues values        = new ContentValues();
            values.put(COLUMN_ID, 0);
            values.put(COLUMN_NAME, apartmentName);
            values.put(COLUMN_POSITION, 0);
            db.insert(TABLE_NAME, null, values);

            smartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID, (long) 0);
        }
    }
}
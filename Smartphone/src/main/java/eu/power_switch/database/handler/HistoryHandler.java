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

package eu.power_switch.database.handler;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import eu.power_switch.database.table.history.HistoryTable;
import eu.power_switch.history.HistoryItem;

/**
 * Created by Markus on 08.12.2015.
 */
class HistoryHandler {

    private HistoryHandler() {
    }

    /**
     * Gets all history items from database
     *
     * @return List of History Items
     */
    public static LinkedList<HistoryItem> getHistory() {
        LinkedList<HistoryItem> historyItems = new LinkedList<>();

        String[] columns = {HistoryTable.COLUMN_ID, HistoryTable.COLUMN_DESCRIPTION, HistoryTable.COLUMN_TIME};
        Cursor cursor = DatabaseHandler.database.query(HistoryTable.TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            historyItems.add(dbToHistoryItem(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        Collections.sort(historyItems, new Comparator<HistoryItem>() {
            @Override
            public int compare(HistoryItem t0, HistoryItem t1) {
                if (t0.getTime().getTimeInMillis() - t1.getTime().getTimeInMillis() < 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        return historyItems;
    }

    /**
     * Adds a HistoryItem to Database
     *
     * @return List of History Items
     */
    public static Long add(HistoryItem historyItem) {
        ContentValues values = new ContentValues();
        values.put(HistoryTable.COLUMN_DESCRIPTION, historyItem.getDescription());
        values.put(HistoryTable.COLUMN_TIME, historyItem.getTime().getTimeInMillis());
        return DatabaseHandler.database.insert(HistoryTable.TABLE_NAME, null, values);
    }

    private static HistoryItem dbToHistoryItem(Cursor cursor) {
        Long id = cursor.getLong(0);
        String description = cursor.getString(1);
        Long time = cursor.getLong(2);

        return new HistoryItem(id, time, description);
    }

}

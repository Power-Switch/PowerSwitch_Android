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

package eu.power_switch.persistence.sqlite.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.LinkedList;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.history.HistoryItem;
import eu.power_switch.persistence.sqlite.table.history.HistoryTable;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import timber.log.Timber;

/**
 * Handler for History related Database actions
 * <p/>
 * Created by Markus on 08.12.2015.
 */
@Singleton
class HistoryHandler {

    @Inject
    HistoryHandler() {
    }

    /**
     * Gets all history items from database, sorted by date/time
     *
     * @return List of History Items
     */
    public LinkedList<HistoryItem> getHistory(@NonNull SQLiteDatabase database) throws Exception {
        LinkedList<HistoryItem> historyItems = new LinkedList<>();

        Cursor cursor = database.query(HistoryTable.TABLE_NAME, HistoryTable.ALL_COLUMNS, null, null, null, null, HistoryTable.COLUMN_TIME + " ASC");
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            historyItems.add(dbToHistoryItem(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return historyItems;
    }

    /**
     * Adds a HistoryItem to Database
     *
     * @return List of History Items
     */
    public Long add(@NonNull SQLiteDatabase database, HistoryItem historyItem) throws Exception {
        ContentValues values = new ContentValues();
        values.put(HistoryTable.COLUMN_DESCRIPTION, historyItem.getShortDescription());
        values.put(HistoryTable.COLUMN_DESCRIPTION_LONG, historyItem.getLongDescription());
        values.put(HistoryTable.COLUMN_TIME,
                historyItem.getTime()
                        .getTimeInMillis());
        long id = database.insert(HistoryTable.TABLE_NAME, null, values);
        deleteOldEntries(database);
        return id;
    }

    private void deleteOldEntries(@NonNull SQLiteDatabase database) throws Exception {
        Calendar calendar = Calendar.getInstance();

        switch (SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION)) {
            case SettingsConstants.KEEP_HISTORY_FOREVER:
                // dont delete anything
                return;
            case SettingsConstants.KEEP_HISTORY_1_YEAR:
                calendar.add(Calendar.YEAR, -1);
                break;
            case SettingsConstants.KEEP_HISTORY_6_MONTHS:
                calendar.add(Calendar.MONTH, -6);
                break;
            case SettingsConstants.KEEP_HISTORY_1_MONTH:
                calendar.add(Calendar.MONTH, -1);
                break;
            case SettingsConstants.KEEP_HISTORY_14_DAYS:
                calendar.add(Calendar.DAY_OF_YEAR, -14);
                break;
            default:
                Timber.w("Unknown \"Keep History\" duration selection! Nothing will be deleted.");
                return;
        }

        database.delete(HistoryTable.TABLE_NAME, HistoryTable.COLUMN_TIME + " <= " + calendar.getTimeInMillis(), null);
    }

    /**
     * Delete the entire History from Database
     */
    public void clear(@NonNull SQLiteDatabase database) throws Exception {
        database.delete(HistoryTable.TABLE_NAME, null, null);
    }

    private HistoryItem dbToHistoryItem(Cursor cursor) throws Exception {
        Long   id               = cursor.getLong(0);
        String shortDescription = cursor.getString(1);
        String longDescription  = !cursor.isNull(2) ? cursor.getString(2) : "";
        Long   time             = cursor.getLong(3);

        return new HistoryItem(id, time, shortDescription, longDescription);
    }

}

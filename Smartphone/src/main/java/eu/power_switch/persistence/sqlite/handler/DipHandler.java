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

import java.util.LinkedList;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.persistence.sqlite.table.receiver.DipTable;

/**
 * Provides database methods for managing DipReceivers
 */
@Singleton
class DipHandler {

    @Inject
    DipHandler() {
    }

    /**
     * Adds Dips from a DipReceiver to Database
     *
     * @param receiverID The ID of the receiver in database (can differ from the one in the newly created object)
     * @param receiver   The DipReceiver containing the dip information.
     */
    protected void add(@NonNull SQLiteDatabase database, Long receiverID, DipReceiver receiver) throws Exception {
        int position = 0;
        for (DipSwitch dip : receiver.getDips()) {
            ContentValues values = new ContentValues();
            values.put(DipTable.COLUMN_POSITION, position);
            values.put(DipTable.COLUMN_STATE, dip.isChecked());
            values.put(DipTable.COLUMN_RECEIVER_ID, receiverID);
            database.insert(DipTable.TABLE_NAME, null, values);
            position++;
        }
    }

    /**
     * Deletes Dips of a DipReceiver from Database
     *
     * @param receiverId The ID of the receiver
     */
    protected void delete(@NonNull SQLiteDatabase database, Long receiverId) throws Exception {
        database.delete(DipTable.TABLE_NAME, DipTable.COLUMN_RECEIVER_ID + "=" + receiverId, null);
    }

    /**
     * Gets associated Dips of a DipReceiver
     *
     * @param receiverId The ID of the receiver
     *
     * @return List of Dip positions
     */
    protected LinkedList<Boolean> getDips(@NonNull SQLiteDatabase database, Long receiverId) throws Exception {
        LinkedList<Boolean> dips = new LinkedList<>();

        String[] dipColumns = {DipTable.COLUMN_STATE};
        Cursor dipCursor = database.query(DipTable.TABLE_NAME,
                dipColumns,
                DipTable.COLUMN_RECEIVER_ID + "==" + receiverId,
                null,
                null,
                null,
                DipTable.COLUMN_POSITION);
        dipCursor.moveToFirst();
        while (!dipCursor.isAfterLast()) {
            int state = dipCursor.getInt(0);
            if (state == 0) {
                dips.add(false);
            } else {
                dips.add(true);
            }
            dipCursor.moveToNext();
        }

        dipCursor.close();
        return dips;
    }
}

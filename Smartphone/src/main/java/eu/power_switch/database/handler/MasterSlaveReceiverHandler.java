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

import eu.power_switch.database.table.receiver.MasterSlaveTable;

/**
 * Provides database methods for managing MasterSlaveReceivers
 */
abstract class MasterSlaveReceiverHandler {

    /**
     * Adds the MasterSlaveReceiver details of a new receiver.
     *
     * @param receiverID The ID of the receiver.
     * @param master     Master channel of the receiver.
     * @param slave      Slave channel of the receiver.
     */
    protected static void add(Long receiverID, Character master, int slave) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MasterSlaveTable.COLUMN_MASTER, master.toString());
        values.put(MasterSlaveTable.COLUMN_SLAVE, slave);
        values.put(MasterSlaveTable.COLUMN_RECEIVER_ID, receiverID);
        DatabaseHandler.database.insert(MasterSlaveTable.TABLE_NAME, null, values);
    }

    /**
     * Deletes the MasterSlaveReceiver details of a receiver.
     *
     * @param receiverID ID of the deleted receiver
     */
    protected static void delete(Long receiverID) throws Exception {
        DatabaseHandler.database.delete(MasterSlaveTable.TABLE_NAME, MasterSlaveTable.COLUMN_RECEIVER_ID + "=" + receiverID, null);
    }

    /**
     * Returns the master channel of a MasterSlaveReceiver.
     *
     * @param receiverID The ID of the receiver.
     * @return The master channel of the receiver.
     */
    protected static Character getMaster(Long receiverID) throws Exception {
        String[] columns = {MasterSlaveTable.COLUMN_MASTER};
        Cursor cursor = DatabaseHandler.database.query(MasterSlaveTable.TABLE_NAME, columns,
                MasterSlaveTable.COLUMN_RECEIVER_ID + "==" + receiverID, null, null, null, null);
        cursor.moveToFirst();
        Character master = cursor.getString(0).charAt(0);
        cursor.close();
        return master;
    }

    /**
     * Returns the slave channel of a MasterSlaveReceiver.
     *
     * @param receiverID The ID of the receiver.
     * @return The slave channel of the receiver.
     */
    protected static int getSlave(Long receiverID) throws Exception {
        String[] columns = {MasterSlaveTable.COLUMN_SLAVE};
        Cursor cursor = DatabaseHandler.database.query(MasterSlaveTable.TABLE_NAME, columns,
                MasterSlaveTable.COLUMN_RECEIVER_ID + "==" + receiverID, null, null, null, null);
        cursor.moveToFirst();
        int slave = cursor.getInt(0);
        cursor.close();
        return slave;
    }
}

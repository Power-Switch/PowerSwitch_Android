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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.power_switch.database.table.receiver.ReceiverTable;
import eu.power_switch.obj.receiver.device.AutoPairReceiver;
import eu.power_switch.obj.receiver.device.DipReceiver;
import eu.power_switch.obj.receiver.device.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.device.Receiver;
import eu.power_switch.obj.receiver.device.UniversalReceiver;
import eu.power_switch.shared.log.Log;

/**
 * Provides database methods for managing Receivers of any type
 */
class ReceiverHandler {

    /**
     * Adds Receiver to Database
     *
     * @param receiver Receiver
     */
    protected static void add(Receiver receiver) {
        ContentValues values = new ContentValues();
        values.put(ReceiverTable.COLUMN_NAME, receiver.getName());
        values.put(ReceiverTable.COLUMN_ROOM_ID, receiver.getRoomId());
        values.put(ReceiverTable.COLUMN_MODEL, receiver.getModel());
        values.put(ReceiverTable.COLUMN_CLASSNAME, receiver.getClass().getName());
        values.put(ReceiverTable.COLUMN_TYPE, receiver.getType());
        values.put(ReceiverTable.COLUMN_POSITION_IN_ROOM, RoomHandler.get(receiver.getRoomId()).getReceivers().size());

        Long dbInsertReturnValue = DatabaseHandler.database.insert(ReceiverTable.TABLE_NAME, null, values);

        if (dbInsertReturnValue > -1) {
            insertDetails(receiver, dbInsertReturnValue);
        } else {
            // TODO Fehlermeldung ausgeben
        }
    }

    /**
     * Insert Receiver details into related database tables
     *
     * @param receiver   the new Receiver
     * @param receiverId ID of the new Receiver in database
     */
    private static void insertDetails(Receiver receiver, Long receiverId) {
        String type = receiver.getType();
        if (type.equals(Receiver.TYPE_MASTER_SLAVE)) {
            MasterSlaveReceiver receiverAsMasterSlave = (MasterSlaveReceiver) receiver;
            MasterSlaveReceiverHandler.add(receiverId, receiverAsMasterSlave.getMaster(),
                    receiverAsMasterSlave.getSlave());
        } else if (type.equals(Receiver.TYPE_DIPS)) {
            DipReceiver receiverAsDipReceiver = (DipReceiver) receiver;
            DipHandler.add(receiverId, receiverAsDipReceiver);
        } else if (type.equals(Receiver.TYPE_UNIVERSAL)) {
            UniversalReceiver receiverAsUniversalReceiver = (UniversalReceiver) receiver;
            UniversalButtonHandler.addUniversalButtons(receiverId, receiverAsUniversalReceiver.getUniversalButtons());
        } else if (type.equals(Receiver.TYPE_AUTOPAIR)) {
            AutoPairReceiver receiverAsAutoPairReceiver = (AutoPairReceiver) receiver;
            AutoPairHandler.add(receiverId, receiverAsAutoPairReceiver.getSeed());
        }
    }

    /**
     * Updates a Receiver in Database
     *
     * @param receiver Receiver
     */
    protected static void update(Receiver receiver) {
        updateDetails(receiver);

        ContentValues values = new ContentValues();
        values.put(ReceiverTable.COLUMN_NAME, receiver.getName());
        values.put(ReceiverTable.COLUMN_ROOM_ID, receiver.getRoomId());
        values.put(ReceiverTable.COLUMN_MODEL, receiver.getModel());
        values.put(ReceiverTable.COLUMN_CLASSNAME, receiver.getClass().getName());
        values.put(ReceiverTable.COLUMN_TYPE, receiver.getType());

        DatabaseHandler.database.update(ReceiverTable.TABLE_NAME, values,
                ReceiverTable.COLUMN_ID + "=" + receiver.getId(), null);

        SceneItemHandler.update(receiver);
    }

    /**
     * Updates Receiver DB details
     *
     * @param receiver the edited Receiver
     */
    private static void updateDetails(Receiver receiver) {
        long receiverId = receiver.getId();

        deleteDetails(receiverId);
        insertDetails(receiver, receiverId);
    }

    /**
     * Gets Receiver from Database
     *
     * @param id ID of Receiver
     * @return Receiver
     */
    protected static Receiver get(Long id) {
        String[] columns = {ReceiverTable.COLUMN_ID, ReceiverTable.COLUMN_NAME, ReceiverTable.COLUMN_MODEL,
                ReceiverTable.COLUMN_TYPE, ReceiverTable.COLUMN_CLASSNAME, ReceiverTable.COLUMN_ROOM_ID,
                ReceiverTable.COLUMN_POSITION_IN_ROOM, ReceiverTable.COLUMN_LAST_ACTIVATED_BUTTON_ID};
        Cursor cursor = DatabaseHandler.database.query(ReceiverTable.TABLE_NAME, columns, ReceiverTable.COLUMN_ID + "="
                + id, null, null, null, null);
        cursor.moveToFirst();
        Receiver receiver = dbToReceiver(cursor);
        cursor.close();
        return receiver;
    }

    /**
     * Gets a Receiver in a Room
     *
     * @param roomId       ID of Room
     * @param receiverName Name of Receiver
     * @return Receiver
     */
    protected static Receiver getByRoom(Long roomId, String receiverName) {
        for (Receiver receiver : getByRoom(roomId)) {
            if (receiverName.equals(receiver.getName())) {
                return receiver;
            }
        }

        return null;
    }

    /**
     * Gets all Receivers in a Room
     *
     * @param roomId ID of Room
     * @return List of Receivers
     */
    protected static ArrayList<Receiver> getByRoom(Long roomId) {
        ArrayList<Receiver> receivers = new ArrayList<>();
        String[] columns = {ReceiverTable.COLUMN_ID, ReceiverTable.COLUMN_NAME, ReceiverTable.COLUMN_MODEL,
                ReceiverTable.COLUMN_TYPE, ReceiverTable.COLUMN_CLASSNAME, ReceiverTable.COLUMN_ROOM_ID,
                ReceiverTable.COLUMN_POSITION_IN_ROOM, ReceiverTable.COLUMN_LAST_ACTIVATED_BUTTON_ID};
        Cursor cursor = DatabaseHandler.database.query(ReceiverTable.TABLE_NAME, columns, ReceiverTable.COLUMN_ROOM_ID +
                "=" + roomId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            receivers.add(dbToReceiver(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        Collections.sort(receivers, new Comparator<Receiver>() {
            @Override
            public int compare(Receiver t0, Receiver t1) {
                return t0.getPositionInRoom() - t1.getPositionInRoom();
            }
        });

        return receivers;
    }

    /**
     * Gets all Receivers in Database
     *
     * @return List of Receivers
     */
    protected static List<Receiver> getAll() {
        List<Receiver> receivers = new ArrayList<>();
        String[] columns = {ReceiverTable.COLUMN_ID, ReceiverTable.COLUMN_NAME, ReceiverTable.COLUMN_MODEL,
                ReceiverTable.COLUMN_TYPE, ReceiverTable.COLUMN_CLASSNAME, ReceiverTable.COLUMN_ROOM_ID,
                ReceiverTable.COLUMN_POSITION_IN_ROOM, ReceiverTable.COLUMN_LAST_ACTIVATED_BUTTON_ID};
        Cursor cursor = DatabaseHandler.database.query(ReceiverTable.TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            receivers.add(dbToReceiver(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return receivers;
    }

    /**
     * Deletes Receiver from Database
     *
     * @param id ID of Receiver
     */
    protected static void delete(Long id) {
        Log.d(ReceiverHandler.class, "Delete Receiver: " + id);
        // CAREFUL ABOUT DELETE ORDER, SOME THINGS DEPEND ON EXISTING DATA
        // delete depending things first!

        // delete sceneItems where receiver was used
        SceneItemHandler.deleteByReceiverId(id);
        // delete timers where receiver was used
        TimerActionHandler.deleteByReceiverId(id);

        deleteDetails(id);
        DatabaseHandler.database.delete(ReceiverTable.TABLE_NAME, ReceiverTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Deletes Receiver DB details
     *
     * @param id ID of Receiver
     */
    private static void deleteDetails(Long id) {
        String type = getType(id);
        if (type.equals(Receiver.TYPE_MASTER_SLAVE)) {
            MasterSlaveReceiverHandler.delete(id);
        } else if (type.equals(Receiver.TYPE_DIPS)) {
            DipHandler.delete(id);
        } else if (type.equals(Receiver.TYPE_UNIVERSAL)) {
            UniversalButtonHandler.deleteUniversalButtons(id);
        } else if (type.equals(Receiver.TYPE_AUTOPAIR)) {
            AutoPairHandler.delete(id);
        }
    }

    /**
     * Gets Type of a Receiver
     *
     * @param id ID of Receiver
     * @return Type of Receiver
     */
    protected static String getType(Long id) {
        String[] columns = {ReceiverTable.COLUMN_ID, ReceiverTable.COLUMN_TYPE};
        Cursor cursor = DatabaseHandler.database.query(ReceiverTable.TABLE_NAME, columns, ReceiverTable.COLUMN_ID +
                "=" + id, null, null, null, null);
        cursor.moveToFirst();
        String type = cursor.getString(1);
        cursor.close();
        return type;
    }

//    /**
//     * Gets ID of last activated Button of a Receiver
//     *
//     * @param id ID of Receiver
//     * @return ID of last activated Button, -1 if not set
//     */
//    protected static long getLastActivatedButtonId(Long id) {
//        String[] columns = {ReceiverTable.COLUMN_LAST_ACTIVATED_BUTTON_ID};
//        Cursor cursor = DatabaseHandler.database.query(ReceiverTable.TABLE_NAME, columns, ReceiverTable.COLUMN_ID + "="
//                + id, null, null, null, null);
//        cursor.moveToFirst();
//
//        long buttonId = -1;
//        if (!cursor.isNull(0)) {
//            buttonId = cursor.getLong(0);
//        }
//
//        cursor.close();
//        return buttonId;
//    }

    /**
     * Sets ID of last activated Button of a Receiver
     *
     * @param receiverId ID of Receiver
     * @param buttonId   ID of Button
     */
    protected static void setLastActivatedButtonId(Long receiverId, Long buttonId) {
        ContentValues values = new ContentValues();
        values.put(ReceiverTable.COLUMN_LAST_ACTIVATED_BUTTON_ID, buttonId);

        DatabaseHandler.database.update(ReceiverTable.TABLE_NAME, values,
                ReceiverTable.COLUMN_ID + "=" + receiverId, null);
    }

//    /**
//     * Gets position in a Room of a Receiver
//     *
//     * @param id ID of Receiver
//     * @return Position in Room
//     */
//    protected static Long getPositionInRoom(Long id) {
//        String[] columns = {ReceiverTable.COLUMN_POSITION_IN_ROOM};
//        Cursor cursor = DatabaseHandler.database.query(ReceiverTable.TABLE_NAME, columns, ReceiverTable.COLUMN_ID + "="
//                + id, null, null, null, null);
//        cursor.moveToFirst();
//
//        long positionInRoom = -1;
//        if (!cursor.isNull(0)) {
//            positionInRoom = cursor.getLong(0);
//        }
//
//        cursor.close();
//        return positionInRoom;
//    }

    /**
     * Sets position in a Room of a Receiver
     *
     * @param receiverId     ID of Receiver
     * @param positionInRoom Position in Room
     */
    protected static void setPositionInRoom(Long receiverId, Long positionInRoom) {
        ContentValues values = new ContentValues();
        values.put(ReceiverTable.COLUMN_POSITION_IN_ROOM, positionInRoom);

        DatabaseHandler.database.update(ReceiverTable.TABLE_NAME, values,
                ReceiverTable.COLUMN_ID + "=" + receiverId, null);
    }

    /**
     * Creates a Receiver Object out of Database information
     *
     * @param c cursor pointing to a Receiver database entry
     * @return Receiver
     */
    private static Receiver dbToReceiver(Cursor c) {
        return ReceiverReflectionMagic.fromDatabase(DatabaseHandler.context, c);
    }
}

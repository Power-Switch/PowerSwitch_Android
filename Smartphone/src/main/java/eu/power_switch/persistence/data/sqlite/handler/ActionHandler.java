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

package eu.power_switch.persistence.data.sqlite.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.action.Action;
import eu.power_switch.action.ReceiverAction;
import eu.power_switch.action.RoomAction;
import eu.power_switch.action.SceneAction;
import eu.power_switch.persistence.data.sqlite.table.action.ActionTable;
import eu.power_switch.persistence.data.sqlite.table.action.ReceiverActionTable;
import eu.power_switch.persistence.data.sqlite.table.action.RoomActionTable;
import eu.power_switch.persistence.data.sqlite.table.action.SceneActionTable;
import eu.power_switch.persistence.data.sqlite.table.alarm_clock.sleep_as_android.SleepAsAndroidActionTable;
import eu.power_switch.persistence.data.sqlite.table.alarm_clock.stock.AlarmClockActionTable;
import eu.power_switch.persistence.data.sqlite.table.apartment.ApartmentTable;
import eu.power_switch.persistence.data.sqlite.table.geofence.GeofenceActionTable;
import eu.power_switch.persistence.data.sqlite.table.receiver.ReceiverTable;
import eu.power_switch.persistence.data.sqlite.table.room.RoomTable;
import eu.power_switch.persistence.data.sqlite.table.scene.SceneTable;
import eu.power_switch.persistence.data.sqlite.table.timer.TimerActionTable;
import timber.log.Timber;

/**
 * Provides database methods for managing Actions
 * <p/>
 * Created by Markus on 01.12.2015.
 */
@Singleton
class ActionHandler {

    @Inject
    ActionHandler() {
    }

    /**
     * Inserts Actions into database
     *
     * @param actions list of actions
     *
     * @return List of IDs of inserted Actions
     */
    protected List<Long> add(@NonNull SQLiteDatabase database, @NonNull List<Action> actions) throws Exception {
        List<Long> ids = new ArrayList<>();
        for (Action action : actions) {
            ContentValues values = new ContentValues();
            values.put(ActionTable.COLUMN_ACTION_TYPE, action.getActionType());
            long actionId = database.insert(ActionTable.TABLE_NAME, null, values);
            ids.add(actionId);

            if (Action.ACTION_TYPE_RECEIVER.equals(action.getActionType())) {
                insertActionDetails(database, (ReceiverAction) action, actionId);
            } else if (Action.ACTION_TYPE_ROOM.equals(action.getActionType())) {
                insertActionDetails(database, (RoomAction) action, actionId);
            } else if (Action.ACTION_TYPE_SCENE.equals(action.getActionType())) {
                insertActionDetails(database, (SceneAction) action, actionId);
            }
        }

        return ids;
    }

    private void insertActionDetails(@NonNull SQLiteDatabase database, @NonNull ReceiverAction receiverAction,
                                     @NonNull Long actionId) throws Exception {
        ContentValues values = new ContentValues();
        values.put(ReceiverActionTable.COLUMN_ACTION_ID, actionId);
        values.put(ReceiverActionTable.COLUMN_APARTMENT_ID, receiverAction.getApartmentId());
        values.put(ReceiverActionTable.COLUMN_ROOM_ID, receiverAction.getRoomId());
        values.put(ReceiverActionTable.COLUMN_RECEIVER_ID, receiverAction.getReceiverId());
        values.put(ReceiverActionTable.COLUMN_BUTTON_ID, receiverAction.getButtonId());
        database.insert(ReceiverActionTable.TABLE_NAME, null, values);
    }

    private void insertActionDetails(@NonNull SQLiteDatabase database, @NonNull RoomAction roomAction, @NonNull Long actionId) throws Exception {
        ContentValues values = new ContentValues();
        values.put(RoomActionTable.COLUMN_ACTION_ID, actionId);
        values.put(RoomActionTable.COLUMN_APARTMENT_ID, roomAction.getApartmentId());
        values.put(RoomActionTable.COLUMN_ROOM_ID, roomAction.getRoomId());
        values.put(RoomActionTable.COLUMN_BUTTON_NAME, roomAction.getButtonName());
        database.insert(RoomActionTable.TABLE_NAME, null, values);
    }

    private void insertActionDetails(@NonNull SQLiteDatabase database, @NonNull SceneAction sceneAction, @NonNull Long actionId) throws Exception {
        ContentValues values = new ContentValues();
        values.put(SceneActionTable.COLUMN_ACTION_ID, actionId);
        values.put(SceneActionTable.COLUMN_APARTMENT_ID, sceneAction.getApartmentId());
        values.put(SceneActionTable.COLUMN_SCENE_ID, sceneAction.getSceneId());
        database.insert(SceneActionTable.TABLE_NAME, null, values);
    }

    /**
     * Gets an Action
     *
     * @param id ID of Action
     *
     * @return Action
     */
    @NonNull
    protected Action get(@NonNull SQLiteDatabase database, long id) throws Exception {
        Action action = null;
        Cursor cursor = database.query(ActionTable.TABLE_NAME, ActionTable.ALL_COLUMNS, ActionTable.COLUMN_ID + "=" + id, null, null, null, null);

        if (cursor.moveToFirst()) {
            action = dbToAction(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return action;
    }

    /**
     * Deletes all Actions using a specific Receiver
     *
     * @param receiverId ID of Receiver
     */
    protected void deleteByReceiverId(@NonNull SQLiteDatabase database, @NonNull Long receiverId) throws Exception {
        Timber.d("Delete TimerActions by ReceiverId: " + receiverId);
        String[] columns = {ReceiverActionTable.COLUMN_ID, ReceiverActionTable.COLUMN_ACTION_ID};
        Cursor cursor = database.query(ReceiverActionTable.TABLE_NAME,
                columns,
                ReceiverActionTable.COLUMN_RECEIVER_ID + "=" + receiverId,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long actionId = cursor.getLong(1);
            delete(database, actionId);

            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Deletes all Actions using a specific Room
     *
     * @param roomId ID of Room
     */
    protected void deleteByRoomId(@NonNull SQLiteDatabase database, @NonNull Long roomId) throws Exception {
        String[] columns = {RoomActionTable.COLUMN_ID, RoomActionTable.COLUMN_ACTION_ID};
        Cursor   cursor  = database.query(RoomActionTable.TABLE_NAME, columns, RoomActionTable.COLUMN_ROOM_ID + "=" + roomId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long actionId = cursor.getLong(1);
            delete(database, actionId);

            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Deletes all Actions using a specific Scene
     *
     * @param sceneId ID of Scene
     */
    protected void deleteBySceneId(@NonNull SQLiteDatabase database, @NonNull Long sceneId) throws Exception {
        String[] columns = {SceneActionTable.COLUMN_ID, SceneActionTable.COLUMN_ACTION_ID};
        Cursor cursor = database.query(SceneActionTable.TABLE_NAME,
                columns,
                SceneActionTable.COLUMN_SCENE_ID + "=" + sceneId,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long actionId = cursor.getLong(1);
            delete(database, actionId);

            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Deletes Action by ID
     *
     * @param actionId ID od Action
     */
    public void delete(@NonNull SQLiteDatabase database, long actionId) throws Exception {
        database.delete(ActionTable.TABLE_NAME, ActionTable.COLUMN_ID + "=" + actionId, null);

        // delete specific information
        database.delete(ReceiverActionTable.TABLE_NAME, ReceiverActionTable.COLUMN_ACTION_ID + "=" + actionId, null);
        database.delete(RoomActionTable.TABLE_NAME, RoomActionTable.COLUMN_ACTION_ID + "=" + actionId, null);
        database.delete(SceneActionTable.TABLE_NAME, SceneActionTable.COLUMN_ID + "=" + actionId, null);

        // delete from every relational table too
        database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ACTION_ID + "=" + actionId, null);
        database.delete(AlarmClockActionTable.TABLE_NAME, AlarmClockActionTable.COLUMN_ACTION_ID + "=" + actionId, null);
        database.delete(SleepAsAndroidActionTable.TABLE_NAME, SleepAsAndroidActionTable.COLUMN_ACTION_ID + "=" + actionId, null);
        database.delete(GeofenceActionTable.TABLE_NAME, GeofenceActionTable.COLUMN_ACTION_ID + "=" + actionId, null);
    }

    private Action dbToAction(@NonNull SQLiteDatabase database, @NonNull Cursor cursor) throws Exception {
        long   actionId   = cursor.getLong(0);
        String actionType = cursor.getString(1);

        if (Action.ACTION_TYPE_RECEIVER.equals(actionType)) {
            //@formatter:off
            String select = "SELECT " +
                    ReceiverActionTable.TABLE_NAME + "." + ReceiverActionTable.COLUMN_APARTMENT_ID + ", " +
                    ReceiverActionTable.TABLE_NAME + "." + ReceiverActionTable.COLUMN_ROOM_ID + ", " +
                    ReceiverActionTable.TABLE_NAME + "." + ReceiverActionTable.COLUMN_RECEIVER_ID + ", " +
                    ReceiverActionTable.TABLE_NAME + "." + ReceiverActionTable.COLUMN_BUTTON_ID + ", " +
                    ApartmentTable.TABLE_NAME + "." + ApartmentTable.COLUMN_NAME + ", " +
                    RoomTable.TABLE_NAME + "." + RoomTable.COLUMN_NAME + ", " +
                    ReceiverTable.TABLE_NAME + "." + ReceiverTable.COLUMN_NAME + " " +
                    " FROM " +
                    ReceiverActionTable.TABLE_NAME +
                    " INNER JOIN " + ApartmentTable.TABLE_NAME + " ON "
                      + ReceiverActionTable.TABLE_NAME + "." + ReceiverActionTable.COLUMN_APARTMENT_ID + "=" +
                    ApartmentTable.TABLE_NAME +"." + ApartmentTable.COLUMN_ID +
                    " INNER JOIN " + RoomTable.TABLE_NAME + " ON "
                    + ReceiverActionTable.TABLE_NAME + "." + ReceiverActionTable.COLUMN_ROOM_ID + "=" +
                    RoomTable.TABLE_NAME +"." + RoomTable.COLUMN_ID +
                    " INNER JOIN " + ReceiverTable.TABLE_NAME + " ON "
                    + ReceiverActionTable.TABLE_NAME + "." + ReceiverActionTable.COLUMN_RECEIVER_ID + "=" +
                    ReceiverTable.TABLE_NAME +"." + ReceiverTable.COLUMN_ID +
                    " WHERE " + ReceiverActionTable.TABLE_NAME + "." + ReceiverActionTable.COLUMN_ACTION_ID + "=" + actionId +
                    ";";
            //@formatter:on

            Cursor cursor1 = database.rawQuery(select, null);
            cursor1.moveToFirst();

            long apartmentId = cursor1.getLong(0);
            long roomId      = cursor1.getLong(1);
            long receiverId  = cursor1.getLong(2);
            long buttonId    = cursor1.getLong(3);

            String apartmentName = cursor1.getString(4);
            String roomName      = cursor1.getString(5);
            String receiverName  = cursor1.getString(6);

            cursor1.close();

            return new ReceiverAction(actionId, apartmentId, apartmentName, roomId, roomName, receiverId, receiverName, buttonId);
        } else if (Action.ACTION_TYPE_ROOM.equals(actionType)) {
            //@formatter:off
            String select = "SELECT " +
                    RoomActionTable.TABLE_NAME + "." + RoomActionTable.COLUMN_APARTMENT_ID + ", " +
                    RoomActionTable.TABLE_NAME + "." + RoomActionTable.COLUMN_ROOM_ID + ", " +
                    RoomActionTable.TABLE_NAME + "." + RoomActionTable.COLUMN_BUTTON_NAME + ", " +
                    ApartmentTable.TABLE_NAME + "." + ApartmentTable.COLUMN_NAME + ", " +
                    RoomTable.TABLE_NAME + "." + RoomTable.COLUMN_NAME + " " +
                    " FROM " +
                    RoomActionTable.TABLE_NAME +
                    " INNER JOIN " + ApartmentTable.TABLE_NAME + " ON "
                      + RoomActionTable.TABLE_NAME + "." + RoomActionTable.COLUMN_APARTMENT_ID + "=" +
                    ApartmentTable.TABLE_NAME +"." + ApartmentTable.COLUMN_ID +
                    " INNER JOIN " + RoomTable.TABLE_NAME + " ON "
                    + RoomActionTable.TABLE_NAME + "." + RoomActionTable.COLUMN_ROOM_ID + "=" +
                    RoomTable.TABLE_NAME +"." + RoomTable.COLUMN_ID +
                    " WHERE " + RoomActionTable.TABLE_NAME + "." + RoomActionTable.COLUMN_ACTION_ID + "=" + actionId +
                    ";";
            //@formatter:on

            Cursor cursor1 = database.rawQuery(select, null);
            cursor1.moveToFirst();

            long   apartmentId = cursor1.getLong(0);
            long   roomId      = cursor1.getLong(1);
            String buttonName  = cursor1.getString(2);

            String apartmentName = cursor1.getString(3);
            String roomName      = cursor1.getString(4);
            cursor1.close();

            return new RoomAction(actionId, apartmentId, apartmentName, roomId, roomName, buttonName);
        } else if (Action.ACTION_TYPE_SCENE.equals(actionType)) {
            //@formatter:off
            String select = "SELECT " +
                    SceneActionTable.TABLE_NAME + "." + SceneActionTable.COLUMN_APARTMENT_ID + ", " +
                    SceneActionTable.TABLE_NAME + "." + SceneActionTable.COLUMN_SCENE_ID + ", " +
                    ApartmentTable.TABLE_NAME + "." + ApartmentTable.COLUMN_NAME + ", " +
                    SceneTable.TABLE_NAME + "." + SceneTable.COLUMN_NAME + " " +
                    " FROM " +
                    SceneActionTable.TABLE_NAME +
                    " INNER JOIN " + ApartmentTable.TABLE_NAME + " ON "
                      + SceneActionTable.TABLE_NAME + "." + SceneActionTable.COLUMN_APARTMENT_ID + "=" +
                    ApartmentTable.TABLE_NAME +"." + ApartmentTable.COLUMN_ID +
                    " INNER JOIN " + SceneTable.TABLE_NAME + " ON "
                    + SceneActionTable.TABLE_NAME + "." + SceneActionTable.COLUMN_SCENE_ID + "=" +
                    SceneTable.TABLE_NAME +"." + SceneTable.COLUMN_ID +
                    " WHERE " + SceneActionTable.TABLE_NAME + "." + SceneActionTable.COLUMN_ACTION_ID + "=" + actionId +
                    ";";
            //@formatter:on

            Cursor cursor1 = database.rawQuery(select, null);
            cursor1.moveToFirst();

            long apartmentId = cursor1.getLong(0);
            long sceneId     = cursor1.getLong(1);

            String apartmentName = cursor1.getString(2);
            String sceneName     = cursor1.getString(3);

            cursor1.close();

            return new SceneAction(actionId, apartmentId, apartmentName, sceneId, sceneName);
        } else {
            Timber.e("Unknown ActionType!");
            throw new RuntimeException("Unknown ActionType: " + actionType);
        }
    }
}

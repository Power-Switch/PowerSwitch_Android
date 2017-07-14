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
import eu.power_switch.database.table.action.ActionTable;
import eu.power_switch.database.table.action.ReceiverActionTable;
import eu.power_switch.database.table.action.RoomActionTable;
import eu.power_switch.database.table.action.SceneActionTable;
import eu.power_switch.database.table.alarm_clock.sleep_as_android.SleepAsAndroidActionTable;
import eu.power_switch.database.table.alarm_clock.stock.AlarmClockActionTable;
import eu.power_switch.database.table.geofence.GeofenceActionTable;
import eu.power_switch.database.table.timer.TimerActionTable;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import timber.log.Timber;

/**
 * Provides database methods for managing Actions
 * <p/>
 * Created by Markus on 01.12.2015.
 */
@Singleton
class ActionHandler {

    private RoomHandler  roomHandler;
    private SceneHandler sceneHandler;

    @Inject
    ActionHandler() {
        this.roomHandler = new RoomHandler();
        this.sceneHandler = new SceneHandler();
    }

    /**
     * Inserts Actions into database
     *
     * @param actions list of actions
     *
     * @return List of IDs of inserted Actions
     */
    protected ArrayList<Long> add(@NonNull SQLiteDatabase database, @NonNull List<Action> actions) throws Exception {
        ArrayList<Long> ids = new ArrayList<>();
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
        values.put(ReceiverActionTable.COLUMN_ROOM_ID, receiverAction.getRoomId());
        values.put(ReceiverActionTable.COLUMN_RECEIVER_ID, receiverAction.getReceiverId());
        values.put(ReceiverActionTable.COLUMN_BUTTON_ID, receiverAction.getButtonId());
        database.insert(ReceiverActionTable.TABLE_NAME, null, values);
    }

    private void insertActionDetails(@NonNull SQLiteDatabase database, @NonNull RoomAction roomAction, @NonNull Long actionId) throws Exception {
        ContentValues values = new ContentValues();
        values.put(RoomActionTable.COLUMN_ACTION_ID, actionId);
        values.put(RoomActionTable.COLUMN_ROOM_ID, roomAction.getRoomId());
        values.put(RoomActionTable.COLUMN_BUTTON_NAME, roomAction.getButtonName());
        database.insert(RoomActionTable.TABLE_NAME, null, values);
    }

    private void insertActionDetails(@NonNull SQLiteDatabase database, @NonNull SceneAction sceneAction, @NonNull Long actionId) throws Exception {
        ContentValues values = new ContentValues();
        values.put(SceneActionTable.COLUMN_ACTION_ID, actionId);
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
            String[] columns1 = {ReceiverActionTable.COLUMN_ROOM_ID, ReceiverActionTable.COLUMN_RECEIVER_ID, ReceiverActionTable.COLUMN_BUTTON_ID};
            Cursor cursor1 = database.query(ReceiverActionTable.TABLE_NAME,
                    columns1,
                    ReceiverActionTable.COLUMN_ACTION_ID + "=" + actionId,
                    null,
                    null,
                    null,
                    null);
            cursor1.moveToFirst();

            long roomId     = cursor1.getLong(0);
            long receiverId = cursor1.getLong(1);
            long buttonId   = cursor1.getLong(2);

            cursor1.close();

            Room room = roomHandler.get(database, roomId);

            return new ReceiverAction(actionId, room.getApartmentId(), roomId, receiverId, buttonId);
        } else if (Action.ACTION_TYPE_ROOM.equals(actionType)) {
            String[] columns1 = {RoomActionTable.COLUMN_ROOM_ID, RoomActionTable.COLUMN_BUTTON_NAME};
            Cursor cursor1 = database.query(RoomActionTable.TABLE_NAME,
                    columns1,
                    RoomActionTable.COLUMN_ACTION_ID + "=" + actionId,
                    null,
                    null,
                    null,
                    null);
            cursor1.moveToFirst();

            long   roomId     = cursor1.getLong(0);
            String buttonName = cursor1.getString(1);
            cursor1.close();

            Room room = roomHandler.get(database, roomId);

            return new RoomAction(actionId, room.getApartmentId(), roomId, buttonName);
        } else if (Action.ACTION_TYPE_SCENE.equals(actionType)) {
            String[] columns1 = {SceneActionTable.COLUMN_SCENE_ID};
            Cursor cursor1 = database.query(SceneActionTable.TABLE_NAME,
                    columns1,
                    SceneActionTable.COLUMN_ACTION_ID + "=" + actionId,
                    null,
                    null,
                    null,
                    null);
            cursor1.moveToFirst();

            long sceneId = cursor1.getLong(0);
            cursor1.close();

            Scene scene = sceneHandler.get(database, sceneId);

            return new SceneAction(actionId, scene.getApartmentId(), sceneId);
        } else {
            Timber.e("Unknown ActionType!");
            throw new RuntimeException("Unknown ActionType: " + actionType);
        }
    }
}

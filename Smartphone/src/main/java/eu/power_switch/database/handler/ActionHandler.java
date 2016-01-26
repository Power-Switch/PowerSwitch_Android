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
import java.util.List;

import eu.power_switch.action.Action;
import eu.power_switch.action.ReceiverAction;
import eu.power_switch.action.RoomAction;
import eu.power_switch.action.SceneAction;
import eu.power_switch.database.table.action.ActionTable;
import eu.power_switch.database.table.action.ReceiverActionTable;
import eu.power_switch.database.table.action.RoomActionTable;
import eu.power_switch.database.table.action.SceneActionTable;
import eu.power_switch.database.table.sleep_as_android.SleepAsAndroidActionTable;
import eu.power_switch.database.table.timer.TimerActionTable;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.log.Log;

/**
 * Provides database methods for managing Actions
 * <p/>
 * Created by Markus on 01.12.2015.
 */
abstract class ActionHandler {

    /**
     * Inserts Actions into database
     *
     * @param actions
     * @return List of IDs of inserted Actions
     */
    protected static ArrayList<Long> add(List<Action> actions) {
        ArrayList<Long> ids = new ArrayList<>();
        for (Action action : actions) {
            ContentValues values = new ContentValues();
            values.put(ActionTable.COLUMN_ACTION_TYPE, action.getActionType());
            long actionId = DatabaseHandler.database.insert(ActionTable.TABLE_NAME, null, values);
            ids.add(actionId);

            if (Action.ACTION_TYPE_RECEIVER.equals(action.getActionType())) {
                insertActionDetails((ReceiverAction) action, actionId);
            } else if (Action.ACTION_TYPE_ROOM.equals(action.getActionType())) {
                insertActionDetails((RoomAction) action, actionId);
            } else if (Action.ACTION_TYPE_SCENE.equals(action.getActionType())) {
                insertActionDetails((SceneAction) action, actionId);
            }
        }

        return ids;
    }

    private static void insertActionDetails(ReceiverAction receiverAction, Long actionId) {
        ContentValues values = new ContentValues();
        values.put(ReceiverActionTable.COLUMN_ACTION_ID, actionId);
        values.put(ReceiverActionTable.COLUMN_ROOM_ID, receiverAction.getRoom().getId());
        values.put(ReceiverActionTable.COLUMN_RECEIVER_ID, receiverAction.getReceiver().getId());
        values.put(ReceiverActionTable.COLUMN_BUTTON_ID, receiverAction.getButton().getId());
        DatabaseHandler.database.insert(ReceiverActionTable.TABLE_NAME, null, values);
    }

    private static void insertActionDetails(RoomAction roomAction, Long actionId) {
        ContentValues values = new ContentValues();
        values.put(RoomActionTable.COLUMN_ACTION_ID, actionId);
        values.put(RoomActionTable.COLUMN_ROOM_ID, roomAction.getRoom().getId());
        values.put(RoomActionTable.COLUMN_BUTTON_NAME, roomAction.getButtonName());
        DatabaseHandler.database.insert(RoomActionTable.TABLE_NAME, null, values);
    }

    private static void insertActionDetails(SceneAction sceneAction, Long actionId) {
        ContentValues values = new ContentValues();
        values.put(SceneActionTable.COLUMN_ACTION_ID, actionId);
        values.put(SceneActionTable.COLUMN_SCENE_ID, sceneAction.getScene().getId());
        DatabaseHandler.database.insert(SceneActionTable.TABLE_NAME, null, values);
    }

    /**
     * Gets an Action
     *
     * @param id ID of Action
     * @return Action
     */
    protected static Action get(long id) throws Exception {
        String[] columns = {ActionTable.COLUMN_ID, ActionTable.COLUMN_ACTION_TYPE};
        Cursor cursor = DatabaseHandler.database.query(ActionTable.TABLE_NAME, columns,
                ActionTable.COLUMN_ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.isAfterLast()) {
            cursor.close();
            return null;
        }

        Action action = dbToAction(cursor);
        cursor.close();
        return action;
    }

    private static Action dbToAction(Cursor cursor) throws Exception {
        long actionId = cursor.getLong(0);
        String actionType = cursor.getString(1);

        if (Action.ACTION_TYPE_RECEIVER.equals(actionType)) {
            String[] columns1 = {ReceiverActionTable.COLUMN_ROOM_ID,
                    ReceiverActionTable.COLUMN_RECEIVER_ID, ReceiverActionTable.COLUMN_BUTTON_ID};
            Cursor cursor1 = DatabaseHandler.database.query(ReceiverActionTable.TABLE_NAME, columns1,
                    ReceiverActionTable.COLUMN_ACTION_ID + "=" + actionId, null, null, null, null);
            cursor1.moveToFirst();

            long roomId = cursor1.getLong(0);
            long receiverId = cursor1.getLong(1);
            long buttonId = cursor1.getLong(2);

            Room room = RoomHandler.get(roomId);
            Receiver receiver = ReceiverHandler.get(receiverId);
            Button button = null;

            for (Button currentButton : receiver.getButtons()) {
                if (currentButton.getId() == buttonId) {
                    button = currentButton;
                    break;
                }
            }

            cursor1.close();

            return new ReceiverAction(actionId, room, receiver, button);
        } else if (Action.ACTION_TYPE_ROOM.equals(actionType)) {
            String[] columns1 = {RoomActionTable.COLUMN_ROOM_ID, RoomActionTable.COLUMN_BUTTON_NAME};
            Cursor cursor1 = DatabaseHandler.database.query(RoomActionTable.TABLE_NAME, columns1,
                    RoomActionTable.COLUMN_ACTION_ID + "=" + actionId, null, null, null, null);
            cursor1.moveToFirst();

            long roomId = cursor1.getLong(0);
            Room room = RoomHandler.get(roomId);
            String buttonName = cursor1.getString(1);

            cursor1.close();
            return new RoomAction(actionId, room, buttonName);
        } else if (Action.ACTION_TYPE_SCENE.equals(actionType)) {
            String[] columns1 = {SceneActionTable.COLUMN_SCENE_ID};
            Cursor cursor1 = DatabaseHandler.database.query(SceneActionTable.TABLE_NAME, columns1,
                    SceneActionTable.COLUMN_ACTION_ID + "=" + actionId, null, null, null, null);
            cursor1.moveToFirst();

            long sceneId = cursor1.getLong(0);
            Scene scene = SceneHandler.get(sceneId);

            cursor1.close();
            return new SceneAction(actionId, scene);
        } else {
            Log.e("Unknown ActionType!");
            throw new RuntimeException();
        }
    }

    /**
     * Deletes all Actions using a specific Receiver
     *
     * @param receiverId ID of Receiver
     */
    protected static void deleteByReceiverId(Long receiverId) {
        Log.d(TimerActionHandler.class, "Delete TimerActions by ReceiverId: " + receiverId);
        String[] columns = {ReceiverActionTable.COLUMN_ID, ReceiverActionTable.COLUMN_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(ReceiverActionTable.TABLE_NAME, columns,
                ReceiverActionTable.COLUMN_RECEIVER_ID + "=" + receiverId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long receiverActionId = cursor.getLong(0);
            long actionId = cursor.getLong(1);

            DatabaseHandler.database.delete(ActionTable.TABLE_NAME, ActionTable.COLUMN_ID +
                    "=" + actionId, null);
            DatabaseHandler.database.delete(ReceiverActionTable.TABLE_NAME, ReceiverActionTable.COLUMN_ID +
                    "=" + receiverActionId, null);
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ACTION_ID +
                    "=" + actionId, null);
            DatabaseHandler.database.delete(SleepAsAndroidActionTable.TABLE_NAME, SleepAsAndroidActionTable.COLUMN_ACTION_ID +
                    "=" + actionId, null);

            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Deletes all Actions using a specific Room
     *
     * @param roomId ID of Room
     */
    protected static void deleteByRoomId(Long roomId) {
        String[] columns = {RoomActionTable.COLUMN_ID, RoomActionTable.COLUMN_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(RoomActionTable.TABLE_NAME, columns,
                RoomActionTable.COLUMN_ROOM_ID + "=" + roomId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long roomActionId = cursor.getLong(0);
            long actionId = cursor.getLong(1);

            DatabaseHandler.database.delete(ActionTable.TABLE_NAME, ActionTable.COLUMN_ID +
                    "=" + actionId, null);
            DatabaseHandler.database.delete(RoomActionTable.TABLE_NAME, RoomActionTable.COLUMN_ID +
                    "=" + roomActionId, null);
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ACTION_ID +
                    "=" + actionId, null);
            DatabaseHandler.database.delete(SleepAsAndroidActionTable.TABLE_NAME, SleepAsAndroidActionTable.COLUMN_ACTION_ID +
                    "=" + actionId, null);

            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Deletes all Timer Actions using a specific Scene
     *
     * @param sceneId ID of Scene
     */
    protected static void deleteBySceneId(Long sceneId) {
        String[] columns = {SceneActionTable.COLUMN_ID, SceneActionTable.COLUMN_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(SceneActionTable.TABLE_NAME, columns,
                SceneActionTable.COLUMN_SCENE_ID + "=" + sceneId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long sceneActionId = cursor.getLong(0);
            long actionId = cursor.getLong(1);

            DatabaseHandler.database.delete(ActionTable.TABLE_NAME, ActionTable.COLUMN_ID +
                    "=" + actionId, null);
            DatabaseHandler.database.delete(SceneActionTable.TABLE_NAME, SceneActionTable.COLUMN_ID +
                    "=" + sceneActionId, null);
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ACTION_ID +
                    "=" + actionId, null);
            DatabaseHandler.database.delete(SleepAsAndroidActionTable.TABLE_NAME, SleepAsAndroidActionTable.COLUMN_ACTION_ID +
                    "=" + actionId, null);

            cursor.moveToNext();
        }

        cursor.close();
    }
}

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

import eu.power_switch.database.table.timer.TimerActionTable;
import eu.power_switch.database.table.timer.TimerReceiverActionTable;
import eu.power_switch.database.table.timer.TimerRoomActionTable;
import eu.power_switch.database.table.timer.TimerSceneActionTable;
import eu.power_switch.log.Log;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.action.TimerAction;
import eu.power_switch.timer.action.TimerReceiverAction;
import eu.power_switch.timer.action.TimerRoomAction;
import eu.power_switch.timer.action.TimerSceneAction;

/**
 * Provides database methods for managing TimerActions
 */
public abstract class TimerActionHandler {

    protected static void add(ArrayList<TimerAction> actions, Long timerId) {
        for (TimerAction action : actions) {
            ContentValues values = new ContentValues();
            values.put(TimerActionTable.COLUMN_TIMER_ID, timerId);
            values.put(TimerActionTable.COLUMN_ACTION_TYPE, action.getActionType());
            long actionId = DatabaseHandler.database.insert(TimerActionTable.TABLE_NAME, null, values);

            if (TimerAction.ACTION_TYPE_RECEIVER.equals(action.getActionType())) {
                insertActionDetails((TimerReceiverAction) action, actionId);
            } else if (TimerAction.ACTION_TYPE_ROOM.equals(action.getActionType())) {
                insertActionDetails((TimerRoomAction) action, actionId);
            } else if (TimerAction.ACTION_TYPE_SCENE.equals(action.getActionType())) {
                insertActionDetails((TimerSceneAction) action, actionId);
            }
        }
    }

    private static void insertActionDetails(TimerReceiverAction timerReceiverAction, Long actionId) {
        ContentValues values = new ContentValues();
        values.put(TimerReceiverActionTable.COLUMN_TIMER_ACTION_ID, actionId);
        values.put(TimerReceiverActionTable.COLUMN_ROOM_ID, timerReceiverAction.getRoom().getId());
        values.put(TimerReceiverActionTable.COLUMN_RECEIVER_ID, timerReceiverAction.getReceiver().getId());
        values.put(TimerReceiverActionTable.COLUMN_BUTTON_ID, timerReceiverAction.getButton().getId());
        DatabaseHandler.database.insert(TimerReceiverActionTable.TABLE_NAME, null, values);
    }

    private static void insertActionDetails(TimerRoomAction timerRoomAction, Long actionId) {
        ContentValues values = new ContentValues();
        values.put(TimerRoomActionTable.COLUMN_TIMER_ACTION_ID, actionId);
        values.put(TimerRoomActionTable.COLUMN_ROOM_ID, timerRoomAction.getRoom().getId());
        values.put(TimerRoomActionTable.COLUMN_BUTTON_NAME, timerRoomAction.getButtonName());
        DatabaseHandler.database.insert(TimerRoomActionTable.TABLE_NAME, null, values);
    }

    private static void insertActionDetails(TimerSceneAction timerSceneAction, Long actionId) {
        ContentValues values = new ContentValues();
        values.put(TimerSceneActionTable.COLUMN_TIMER_ACTION_ID, actionId);
        values.put(TimerSceneActionTable.COLUMN_SCENE_ID, timerSceneAction.getScene().getId());
        DatabaseHandler.database.insert(TimerSceneActionTable.TABLE_NAME, null, values);
    }

    /**
     * Deletes all TimerActions using Timer ID
     *
     * @param timerId ID of Timer
     */
    protected static void delete(Long timerId) {
        ArrayList<TimerAction> timerActions = getByTimerId(timerId);

        for (TimerAction timerAction : timerActions) {
            // delete timerXXXactions
            DatabaseHandler.database.delete(TimerReceiverActionTable.TABLE_NAME, TimerReceiverActionTable.COLUMN_TIMER_ACTION_ID +
                    "=" + timerAction.getId(), null);
            DatabaseHandler.database.delete(TimerRoomActionTable.TABLE_NAME, TimerRoomActionTable.COLUMN_TIMER_ACTION_ID +
                    "=" + timerAction.getId(), null);
            DatabaseHandler.database.delete(TimerSceneActionTable.TABLE_NAME, TimerSceneActionTable.COLUMN_TIMER_ACTION_ID +
                    "=" + timerAction.getId(), null);

            // then delete timerAction
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_TIMER_ID +
                    "=" + timerId, null);
        }
    }

    /**
     * Deletes all TimerActions using a specific Receiver
     *
     * @param receiverId ID of Receiver
     */
    protected static void deleteByReceiverId(Long receiverId) {
        Log.d("Delete TimerActions by ReceiverId: " + receiverId);
        String[] columns = {TimerReceiverActionTable.COLUMN_ID, TimerReceiverActionTable.COLUMN_TIMER_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(TimerReceiverActionTable.TABLE_NAME, columns,
                TimerReceiverActionTable.COLUMN_RECEIVER_ID + "=" + receiverId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long timerReceiverActionId = cursor.getLong(0);
            long timerActionId = cursor.getLong(1);

            DatabaseHandler.database.delete(TimerReceiverActionTable.TABLE_NAME, TimerReceiverActionTable.COLUMN_TIMER_ACTION_ID +
                    "=" + timerReceiverActionId, null);
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ID +
                    "=" + timerActionId, null);

            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Deletes all TimerActions using a specific Room
     *
     * @param roomId ID of Room
     */
    protected static void deleteByRoomId(Long roomId) {
        String[] columns = {TimerRoomActionTable.COLUMN_ID, TimerRoomActionTable.COLUMN_TIMER_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(TimerRoomActionTable.TABLE_NAME, columns,
                TimerRoomActionTable.COLUMN_ROOM_ID + "=" + roomId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long timerRoomActionId = cursor.getLong(0);
            long timerActionId = cursor.getLong(1);

            DatabaseHandler.database.delete(TimerRoomActionTable.TABLE_NAME, TimerRoomActionTable
                    .COLUMN_TIMER_ACTION_ID + "=" + timerRoomActionId, null);
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ID +
                    "=" + timerActionId, null);

            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Deletes all TimerActions using a specific Scene
     *
     * @param sceneId ID of Scene
     */
    protected static void deleteBySceneId(Long sceneId) {
        String[] columns = {TimerSceneActionTable.COLUMN_ID, TimerSceneActionTable.COLUMN_TIMER_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(TimerSceneActionTable.TABLE_NAME, columns,
                TimerSceneActionTable.COLUMN_SCENE_ID + "=" + sceneId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            long timerSceneActionId = cursor.getLong(0);
            long timerActionId = cursor.getLong(1);

            DatabaseHandler.database.delete(TimerSceneActionTable.TABLE_NAME, TimerSceneActionTable.COLUMN_TIMER_ACTION_ID +
                    "=" + timerSceneActionId, null);
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ID +
                    "=" + timerActionId, null);

            cursor.moveToNext();
        }

        cursor.close();
    }

    protected static ArrayList<TimerAction> getByTimerId(long timerId) {
        ArrayList<TimerAction> timerActions = new ArrayList<>();

        String[] columns = {TimerActionTable.COLUMN_ID, TimerActionTable.COLUMN_ACTION_TYPE};
        Cursor cursor = DatabaseHandler.database.query(TimerActionTable.TABLE_NAME, columns,
                TimerActionTable.COLUMN_TIMER_ID + "=" + timerId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            timerActions.add(dbToTimerAction(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return timerActions;
    }

    /**
     * Gets a TimerAction
     *
     * @param id ID of TimerAction
     * @return TimerAction
     */
    protected static TimerAction get(long id) {
        String[] columns = {TimerActionTable.COLUMN_ID, TimerActionTable.COLUMN_ACTION_TYPE};
        Cursor cursor = DatabaseHandler.database.query(TimerActionTable.TABLE_NAME, columns,
                TimerActionTable.COLUMN_ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();

        while (cursor.isAfterLast()) {
            cursor.close();
            return null;
        }

        TimerAction timerAction = dbToTimerAction(cursor);
        cursor.close();
        return timerAction;

    }

    private static TimerAction dbToTimerAction(Cursor cursor) {
        long timerActionId = cursor.getLong(0);
        String actionType = cursor.getString(1);

        if (TimerAction.ACTION_TYPE_RECEIVER.equals(actionType)) {
            String[] columns1 = {TimerReceiverActionTable.COLUMN_ROOM_ID,
                    TimerReceiverActionTable.COLUMN_RECEIVER_ID, TimerReceiverActionTable.COLUMN_BUTTON_ID};
            Cursor cursor1 = DatabaseHandler.database.query(TimerReceiverActionTable.TABLE_NAME, columns1,
                    TimerReceiverActionTable.COLUMN_TIMER_ACTION_ID + "=" + timerActionId, null, null, null, null);
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

            return new TimerReceiverAction(timerActionId, room, receiver, button);
        } else if (TimerAction.ACTION_TYPE_ROOM.equals(actionType)) {
            String[] columns1 = {TimerRoomActionTable.COLUMN_ROOM_ID, TimerRoomActionTable.COLUMN_BUTTON_NAME};
            Cursor cursor1 = DatabaseHandler.database.query(TimerRoomActionTable.TABLE_NAME, columns1,
                    TimerRoomActionTable.COLUMN_TIMER_ACTION_ID + "=" + timerActionId, null, null, null, null);
            cursor1.moveToFirst();

            long roomId = cursor1.getLong(0);
            Room room = RoomHandler.get(roomId);
            String buttonName = cursor1.getString(1);

            cursor1.close();
            return new TimerRoomAction(timerActionId, room, buttonName);
        } else if (TimerAction.ACTION_TYPE_SCENE.equals(actionType)) {
            String[] columns1 = {TimerSceneActionTable.COLUMN_SCENE_ID};
            Cursor cursor1 = DatabaseHandler.database.query(TimerSceneActionTable.TABLE_NAME, columns1,
                    TimerSceneActionTable.COLUMN_TIMER_ACTION_ID + "=" + timerActionId, null, null, null, null);
            cursor1.moveToFirst();

            long sceneId = cursor1.getLong(0);
            Scene scene = SceneHandler.get(sceneId);

            cursor1.close();
            return new TimerSceneAction(timerActionId, scene);
        }

        return null;
    }

    /**
     * Update TimerActions for an existing Timer
     *
     * @param timer new Timer
     */
    protected static void update(Timer timer) {
        // delete current actions
        TimerActionHandler.delete(timer.getId());
        // add new actions
        TimerActionHandler.add(timer.getActions(), timer.getId());
    }
}
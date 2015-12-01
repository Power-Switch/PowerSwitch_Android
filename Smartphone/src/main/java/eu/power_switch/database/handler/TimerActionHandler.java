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

import eu.power_switch.database.table.action.ActionTable;
import eu.power_switch.database.table.action.ReceiverActionTable;
import eu.power_switch.database.table.action.RoomActionTable;
import eu.power_switch.database.table.action.SceneActionTable;
import eu.power_switch.database.table.timer.TimerActionTable;
import eu.power_switch.shared.log.Log;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.action.Action;

/**
 * Provides database methods for managing Timer Actions
 */
public abstract class TimerActionHandler {

    /**
     * Adds Actions to a specific Timer
     *
     * @param actions Actions to be added to the Timer
     * @param timerId ID of Timer
     */
    protected static void add(ArrayList<Action> actions, Long timerId) {
        // add actions to database
        ArrayList<Long> actionIds = ActionHandler.add(actions);

        // add timer <-> action relation
        for (Long actionId : actionIds) {
            ContentValues values = new ContentValues();
            values.put(TimerActionTable.COLUMN_TIMER_ID, timerId);
            values.put(TimerActionTable.COLUMN_ACTION_ID, actionId);
            DatabaseHandler.database.insert(TimerActionTable.TABLE_NAME, null, values);
        }
    }


    /**
     * Deletes all Actions using Timer ID
     *
     * @param timerId ID of Timer
     */
    protected static void delete(Long timerId) {
        ArrayList<Action> actions = getByTimerId(timerId);

        for (Action action : actions) {
            DatabaseHandler.database.delete(ActionTable.TABLE_NAME, ActionTable.COLUMN_ID + "=" + action.getId(), null);
            // delete timerXXXactions
            DatabaseHandler.database.delete(ReceiverActionTable.TABLE_NAME, ReceiverActionTable.COLUMN_ACTION_ID +
                    "=" + action.getId(), null);
            DatabaseHandler.database.delete(RoomActionTable.TABLE_NAME, RoomActionTable.COLUMN_ACTION_ID +
                    "=" + action.getId(), null);
            DatabaseHandler.database.delete(SceneActionTable.TABLE_NAME, SceneActionTable.COLUMN_ACTION_ID +
                    "=" + action.getId(), null);

            // then delete timerAction
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_TIMER_ID +
                    "=" + timerId, null);
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
            DatabaseHandler.database.delete(ReceiverActionTable.TABLE_NAME, ReceiverActionTable.COLUMN_ACTION_ID +
                    "=" + receiverActionId, null);
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ACTION_ID +
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
            DatabaseHandler.database.delete(RoomActionTable.TABLE_NAME, RoomActionTable.COLUMN_ACTION_ID +
                    "=" + roomActionId, null);
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ACTION_ID +
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
            DatabaseHandler.database.delete(SceneActionTable.TABLE_NAME, SceneActionTable.COLUMN_ACTION_ID +
                    "=" + sceneActionId, null);
            DatabaseHandler.database.delete(TimerActionTable.TABLE_NAME, TimerActionTable.COLUMN_ACTION_ID +
                    "=" + actionId, null);

            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Get all Actions associated with a specific Timer
     *
     * @param timerId ID of Timer
     * @return List of Actions
     */
    protected static ArrayList<Action> getByTimerId(long timerId) {
        ArrayList<Action> actions = new ArrayList<>();

        String[] columns = {TimerActionTable.COLUMN_TIMER_ID, TimerActionTable.COLUMN_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(TimerActionTable.TABLE_NAME, columns,
                TimerActionTable.COLUMN_TIMER_ID + "=" + timerId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long actionId = cursor.getLong(1);
            actions.add(ActionHandler.get(actionId));
            cursor.moveToNext();
        }

        cursor.close();
        return actions;
    }

    /**
     * Update Actions for an existing Timer
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
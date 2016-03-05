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

import eu.power_switch.action.Action;
import eu.power_switch.database.table.timer.TimerActionTable;
import eu.power_switch.timer.Timer;

/**
 * Provides database methods for managing Timer Actions
 */
abstract class TimerActionHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private TimerActionHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Adds Actions to a specific Timer
     *
     * @param actions Actions to be added to the Timer
     * @param timerId ID of Timer
     */
    protected static void add(ArrayList<Action> actions, Long timerId) throws Exception {
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
    protected static void delete(Long timerId) throws Exception {
        ArrayList<Action> actions = getByTimerId(timerId);

        for (Action action : actions) {
            ActionHandler.delete(action.getId());
        }
    }

    /**
     * Get all Actions associated with a specific Timer
     *
     * @param timerId ID of Timer
     * @return List of Actions
     */
    protected static ArrayList<Action> getByTimerId(long timerId) throws Exception {
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
    protected static void update(Timer timer) throws Exception {
        // delete current actions
        delete(timer.getId());
        // add new actions
        add(timer.getActions(), timer.getId());
    }
}
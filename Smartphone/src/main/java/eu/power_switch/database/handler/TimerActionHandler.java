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

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.action.Action;
import eu.power_switch.database.table.timer.TimerActionTable;
import eu.power_switch.timer.Timer;

/**
 * Provides database methods for managing Timer Actions
 */
@Singleton
class TimerActionHandler {

    @Inject
    ActionHandler actionHandler;

    @Inject
    TimerActionHandler() {
    }

    /**
     * Adds Actions to a specific Timer
     *
     * @param actions Actions to be added to the Timer
     * @param timerId ID of Timer
     */
    protected void add(@NonNull SQLiteDatabase database, List<Action> actions, Long timerId) throws Exception {
        // add actions to database
        List<Long> actionIds = actionHandler.add(database, actions);

        // add timer <-> action relation
        for (Long actionId : actionIds) {
            ContentValues values = new ContentValues();
            values.put(TimerActionTable.COLUMN_TIMER_ID, timerId);
            values.put(TimerActionTable.COLUMN_ACTION_ID, actionId);
            database.insert(TimerActionTable.TABLE_NAME, null, values);
        }
    }


    /**
     * Deletes all Actions using Timer ID
     *
     * @param timerId ID of Timer
     */
    protected void delete(@NonNull SQLiteDatabase database, Long timerId) throws Exception {
        List<Action> actions = getByTimerId(database, timerId);

        for (Action action : actions) {
            actionHandler.delete(database, action.getId());
        }
    }

    /**
     * Get all Actions associated with a specific Timer
     *
     * @param timerId ID of Timer
     *
     * @return List of Actions
     */
    protected List<Action> getByTimerId(@NonNull SQLiteDatabase database, long timerId) throws Exception {
        List<Action> actions = new ArrayList<>();

        String[] columns = {TimerActionTable.COLUMN_TIMER_ID, TimerActionTable.COLUMN_ACTION_ID};
        Cursor cursor = database.query(TimerActionTable.TABLE_NAME,
                columns,
                TimerActionTable.COLUMN_TIMER_ID + "=" + timerId,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long actionId = cursor.getLong(1);
            actions.add(actionHandler.get(database, actionId));
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
    protected void update(@NonNull SQLiteDatabase database, Timer timer) throws Exception {
        // delete current actions
        delete(database, timer.getId());
        // add new actions
        add(database, timer.getActions(), timer.getId());
    }
}
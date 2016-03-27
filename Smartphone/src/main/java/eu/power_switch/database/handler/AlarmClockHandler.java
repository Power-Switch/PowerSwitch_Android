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
import eu.power_switch.database.table.alarm_clock.stock.AlarmClockActionTable;
import eu.power_switch.shared.constants.AlarmClockConstants;

/**
 * Provides database methods for managing Sleep As Android related Actions
 * <p/>
 * Created by Markus on 30.11.2015.
 */
abstract class AlarmClockHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private AlarmClockHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    protected static List<Action> getAlarmActions(AlarmClockConstants.Event event) throws Exception {
        ArrayList<Action> actions = new ArrayList<>();

        String[] columns = {AlarmClockActionTable.COLUMN_ALARM_TYPE_ID, AlarmClockActionTable.COLUMN_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(AlarmClockActionTable.TABLE_NAME, columns,
                AlarmClockActionTable.COLUMN_ALARM_TYPE_ID + "==" + event.getId(),
                null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long actionId = cursor.getLong(1);
            actions.add(ActionHandler.get(actionId));
            cursor.moveToNext();
        }

        cursor.close();
        return actions;
    }

    protected static void setAlarmActions(AlarmClockConstants.Event event, ArrayList<Action> actions) throws Exception {
        deleteAlarmActions(event);
        addAlarmActions(event, actions);
    }

    private static void addAlarmActions(AlarmClockConstants.Event event, ArrayList<Action> actions) throws Exception {
        // add actions to database
        ArrayList<Long> actionIds = ActionHandler.add(actions);

        // add AlarmTriggered <-> action relation
        for (Long actionId : actionIds) {
            ContentValues values = new ContentValues();
            values.put(AlarmClockActionTable.COLUMN_ALARM_TYPE_ID, event.getId());
            values.put(AlarmClockActionTable.COLUMN_ACTION_ID, actionId);
            DatabaseHandler.database.insert(AlarmClockActionTable.TABLE_NAME, null, values);
        }
    }

    private static void deleteAlarmActions(AlarmClockConstants.Event event) throws Exception {
        for (Action action : getAlarmActions(event)) {
            ActionHandler.delete(action.getId());
        }
    }
}

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
import eu.power_switch.database.table.action.ActionTable;
import eu.power_switch.database.table.action.ReceiverActionTable;
import eu.power_switch.database.table.action.RoomActionTable;
import eu.power_switch.database.table.action.SceneActionTable;
import eu.power_switch.database.table.sleep_as_android.SleepAsAndroidActionTable;
import eu.power_switch.shared.constants.ExternalAppConstants;

/**
 * Provides database methods for managing Sleep As Android related Actions
 * <p/>
 * Created by Markus on 30.11.2015.
 */
abstract class SleepAsAndroidHandler {

    protected static List<Action> getAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT event) {
        ArrayList<Action> actions = new ArrayList<>();

        String[] columns = {SleepAsAndroidActionTable.COLUMN_ALARM_TYPE_ID, SleepAsAndroidActionTable.COLUMN_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(SleepAsAndroidActionTable.TABLE_NAME, columns,
                SleepAsAndroidActionTable.COLUMN_ALARM_TYPE_ID + "==" + event.getId(),
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

    protected static void setAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT event, ArrayList<Action> actions) {
        deleteAlarmActions(event);
        addAlarmActions(event, actions);
    }

    private static void addAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT event, ArrayList<Action> actions) {
        // add actions to database
        ArrayList<Long> actionIds = ActionHandler.add(actions);

        // add AlarmTriggered <-> action relation
        for (Long actionId : actionIds) {
            ContentValues values = new ContentValues();
            values.put(SleepAsAndroidActionTable.COLUMN_ALARM_TYPE_ID, event.getId());
            values.put(SleepAsAndroidActionTable.COLUMN_ACTION_ID, actionId);
            DatabaseHandler.database.insert(SleepAsAndroidActionTable.TABLE_NAME, null, values);
        }
    }

    private static void deleteAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT event) {
        for (Action action : getAlarmActions(event)) {
            DatabaseHandler.database.delete(ActionTable.TABLE_NAME, ActionTable.COLUMN_ID + "=" + action.getId(), null);
            // delete alarmXXXactions
            DatabaseHandler.database.delete(ReceiverActionTable.TABLE_NAME, ReceiverActionTable.COLUMN_ACTION_ID +
                    "=" + action.getId(), null);
            DatabaseHandler.database.delete(RoomActionTable.TABLE_NAME, RoomActionTable.COLUMN_ACTION_ID +
                    "=" + action.getId(), null);
            DatabaseHandler.database.delete(SceneActionTable.TABLE_NAME, SceneActionTable.COLUMN_ACTION_ID +
                    "=" + action.getId(), null);

            // then delete AlarmTriggered relation
            DatabaseHandler.database.delete(SleepAsAndroidActionTable.TABLE_NAME, SleepAsAndroidActionTable.COLUMN_ALARM_TYPE_ID +
                    "=" + event.getId(), null);
        }
    }

}

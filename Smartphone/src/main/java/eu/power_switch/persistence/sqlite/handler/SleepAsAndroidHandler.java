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

package eu.power_switch.persistence.sqlite.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.action.Action;
import eu.power_switch.persistence.table.alarm_clock.sleep_as_android.SleepAsAndroidActionTable;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import timber.log.Timber;

/**
 * Provides database methods for managing Sleep As Android related Actions
 * <p/>
 * Created by Markus on 30.11.2015.
 */
@Singleton
class SleepAsAndroidHandler {

    @Inject
    ActionHandler actionHandler;

    @Inject
    SleepAsAndroidHandler() {
    }

    protected List<Action> getAlarmActions(@NonNull SQLiteDatabase database, SleepAsAndroidConstants.Event event) throws Exception {
        List<Action> actions = new ArrayList<>();

        String[] columns = {SleepAsAndroidActionTable.COLUMN_ALARM_TYPE_ID, SleepAsAndroidActionTable.COLUMN_ACTION_ID};
        Cursor cursor = database.query(SleepAsAndroidActionTable.TABLE_NAME,
                columns,
                SleepAsAndroidActionTable.COLUMN_ALARM_TYPE_ID + "==" + event.getId(),
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

    protected void setAlarmActions(@NonNull SQLiteDatabase database, SleepAsAndroidConstants.Event event, List<Action> actions) throws Exception {
        deleteAlarmActions(database, event);
        addAlarmActions(database, event, actions);
    }

    private void addAlarmActions(@NonNull SQLiteDatabase database, SleepAsAndroidConstants.Event event, List<Action> actions) throws Exception {
        if (actions == null) {
            Timber.w("actions was null! nothing added to database");
            return;
        }

        // add actions to database
        List<Long> actionIds = actionHandler.add(database, actions);

        // add AlarmTriggered <-> action relation
        for (Long actionId : actionIds) {
            ContentValues values = new ContentValues();
            values.put(SleepAsAndroidActionTable.COLUMN_ALARM_TYPE_ID, event.getId());
            values.put(SleepAsAndroidActionTable.COLUMN_ACTION_ID, actionId);
            database.insert(SleepAsAndroidActionTable.TABLE_NAME, null, values);
        }
    }

    private void deleteAlarmActions(@NonNull SQLiteDatabase database, SleepAsAndroidConstants.Event event) throws Exception {
        for (Action action : getAlarmActions(database, event)) {
            actionHandler.delete(database, action.getId());
        }
    }
}

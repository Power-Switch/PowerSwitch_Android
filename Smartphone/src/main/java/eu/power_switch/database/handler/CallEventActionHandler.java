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
import eu.power_switch.database.table.phone.call.CallEventActionTable;
import eu.power_switch.shared.constants.PhoneConstants;
import timber.log.Timber;

/**
 * Created by Markus on 12.04.2016.
 */
@Singleton
class CallEventActionHandler {

    @Inject
    CallEventActionHandler() {
    }

    /**
     * Add Actions to database
     *
     * @param actions     list of actions
     * @param callEventId ID of CallEvent
     */
    protected void add(@NonNull SQLiteDatabase database, List<Action> actions, long callEventId, PhoneConstants.CallType callType) throws Exception {
        if (actions == null) {
            Timber.w("actions was null! nothing added to database");
            return;
        }

        // add actions to database
        ArrayList<Long> actionIds = ActionHandler.add(database, actions);

        // add to relational table
        for (long actionId : actionIds) {
            ContentValues values = new ContentValues();
            values.put(CallEventActionTable.COLUMN_CALL_EVENT_ID, callEventId);
            values.put(CallEventActionTable.COLUMN_ACTION_ID, actionId);
            values.put(CallEventActionTable.COLUMN_EVENT_TYPE_ID, callType.getId());
            database.insert(CallEventActionTable.TABLE_NAME, null, values);
        }
    }

    /**
     * Get a list of Actions
     *
     * @param callEventId ID of CallEvent
     * @param callType    Event Type
     *
     * @return List of Actions
     */
    @NonNull
    protected List<Action> get(@NonNull SQLiteDatabase database, long callEventId, PhoneConstants.CallType callType) throws Exception {
        List<Action> actions = new ArrayList<>();

        Cursor cursor = database.query(CallEventActionTable.TABLE_NAME,
                CallEventActionTable.ALL_COLUMNS,
                CallEventActionTable.COLUMN_CALL_EVENT_ID + "==" + callEventId + " AND " + CallEventActionTable.COLUMN_EVENT_TYPE_ID + "==" + callType.getId(),
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long actionId = cursor.getLong(2);
            actions.add(ActionHandler.get(database, actionId));
            cursor.moveToNext();
        }

        cursor.close();
        return actions;
    }

    /**
     * Delete all actions of a specific CallEvent
     *
     * @param callEventId ID of CallEvent
     */
    protected void deleteByCallEvent(@NonNull SQLiteDatabase database, Long callEventId) throws Exception {
        Cursor cursor = database.query(CallEventActionTable.TABLE_NAME, CallEventActionTable.ALL_COLUMNS,
                CallEventActionTable.COLUMN_CALL_EVENT_ID + "==" + callEventId, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long actionId = cursor.getLong(2);
            ActionHandler.delete(database, actionId);
            cursor.moveToNext();
        }

        cursor.close();
    }
}

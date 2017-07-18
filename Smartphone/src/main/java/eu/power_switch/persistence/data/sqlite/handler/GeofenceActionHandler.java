/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.persistence.data.sqlite.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.action.Action;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.persistence.data.sqlite.table.geofence.GeofenceActionTable;
import timber.log.Timber;

/**
 * Provides database methods for managing Geofence Actions
 */
@Singleton
class GeofenceActionHandler {

    @Inject
    ActionHandler actionHandler;

    @Inject
    GeofenceActionHandler() {
    }

    /**
     * Adds Actions to a specific Geofence and EventType
     *
     * @param actions    Actions to be added to the Geofence
     * @param geofenceId ID of Geofence
     * @param eventType  {@link Geofence.EventType}
     */
    protected void add(@NonNull SQLiteDatabase database, List<Action> actions, Long geofenceId, Geofence.EventType eventType) throws Exception {
        if (actions == null) {
            Timber.w("actions was null! nothing added to database");
            return;
        }

        // add actions to database
        List<Long> actionIds = actionHandler.add(database, actions);

        // add geofence <-> action relation
        for (Long actionId : actionIds) {
            ContentValues values = new ContentValues();
            values.put(GeofenceActionTable.COLUMN_GEOFENCE_ID, geofenceId);
            values.put(GeofenceActionTable.COLUMN_EVENT_TYPE, eventType.name());
            values.put(GeofenceActionTable.COLUMN_ACTION_ID, actionId);
            database.insert(GeofenceActionTable.TABLE_NAME, null, values);
        }
    }


    /**
     * Deletes all Actions using Geofence ID
     *
     * @param geofenceId ID of Geofence
     */
    protected void delete(@NonNull SQLiteDatabase database, Long geofenceId) throws Exception {
        List<Action> actions = get(database, geofenceId);

        for (Action action : actions) {
            actionHandler.delete(database, action.getId());
        }
    }

    /**
     * Get all Actions associated with a specific Geofence
     *
     * @param geofenceId ID of Geofence
     *
     * @return List of Actions
     */
    protected List<Action> get(@NonNull SQLiteDatabase database, long geofenceId) throws Exception {
        List<Action> actions = new ArrayList<>();

        String[] columns = {GeofenceActionTable.COLUMN_GEOFENCE_ID, GeofenceActionTable.COLUMN_ACTION_ID};
        Cursor cursor = database.query(GeofenceActionTable.TABLE_NAME,
                columns,
                GeofenceActionTable.COLUMN_GEOFENCE_ID + "=" + geofenceId,
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
     * Get all Actions associated with a specific Geofence
     *
     * @param geofenceId ID of Geofence
     *
     * @return List of Actions
     */
    protected List<Action> get(@NonNull SQLiteDatabase database, long geofenceId, Geofence.EventType eventType) throws Exception {
        List<Action> actions = new ArrayList<>();

        String[] columns = {GeofenceActionTable.COLUMN_GEOFENCE_ID, GeofenceActionTable.COLUMN_ACTION_ID, GeofenceActionTable.COLUMN_EVENT_TYPE};
        Cursor cursor = database.query(GeofenceActionTable.TABLE_NAME,
                columns,
                GeofenceActionTable.COLUMN_GEOFENCE_ID + "=" + geofenceId + " AND " + GeofenceActionTable.COLUMN_EVENT_TYPE + "=" + "\"" + eventType.name() + "\"",
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
     * Update Actions for an existing Geofence
     *
     * @param geofence new Geofence
     */
    protected void update(@NonNull SQLiteDatabase database, Geofence geofence) throws Exception {
        // delete current actions
        delete(database, geofence.getId());
        // add new actions
        for (Geofence.EventType eventType : Geofence.EventType.values()) {
            add(database, geofence.getActions(eventType), geofence.getId(), eventType);
        }
    }
}
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
import eu.power_switch.database.table.geofence.GeofenceActionTable;
import eu.power_switch.google_play_services.geofence.Geofence;

/**
 * Provides database methods for managing Geofence Actions
 */
abstract class GeofenceActionHandler {

    /**
     * Adds Actions to a specific Geofence and EventType
     *
     * @param actions    Actions to be added to the Geofence
     * @param geofenceId ID of Geofence
     * @param eventType  {@link Geofence.EventType}
     */
    protected static void add(List<Action> actions, Long geofenceId, Geofence.EventType eventType) {
        // add actions to database
        ArrayList<Long> actionIds = ActionHandler.add(actions);

        // add timer <-> action relation
        for (Long actionId : actionIds) {
            ContentValues values = new ContentValues();
            values.put(GeofenceActionTable.COLUMN_GEOFENCE_ID, geofenceId);
            values.put(GeofenceActionTable.COLUMN_EVENT_TYPE, eventType.name());
            values.put(GeofenceActionTable.COLUMN_ACTION_ID, actionId);
            DatabaseHandler.database.insert(GeofenceActionTable.TABLE_NAME, null, values);
        }
    }


    /**
     * Deletes all Actions using Geofence ID
     *
     * @param geofenceId ID of Geofence
     */
    protected static void delete(Long geofenceId) throws Exception {
        ArrayList<Action> actions = get(geofenceId);

        for (Action action : actions) {
            DatabaseHandler.database.delete(ActionTable.TABLE_NAME, ActionTable.COLUMN_ID + "=" + action.getId(), null);
            // delete timerXXXactions
            DatabaseHandler.database.delete(ReceiverActionTable.TABLE_NAME, ReceiverActionTable.COLUMN_ACTION_ID +
                    "=" + action.getId(), null);
            DatabaseHandler.database.delete(RoomActionTable.TABLE_NAME, RoomActionTable.COLUMN_ACTION_ID +
                    "=" + action.getId(), null);
            DatabaseHandler.database.delete(SceneActionTable.TABLE_NAME, SceneActionTable.COLUMN_ACTION_ID +
                    "=" + action.getId(), null);

            // then delete Geofence relation
            DatabaseHandler.database.delete(GeofenceActionTable.TABLE_NAME, GeofenceActionTable.COLUMN_GEOFENCE_ID +
                    "=" + geofenceId, null);
        }
    }

    /**
     * Get all Actions associated with a specific Geofence
     *
     * @param geofenceId ID of Geofence
     * @return List of Actions
     */
    protected static ArrayList<Action> get(long geofenceId) throws Exception {
        ArrayList<Action> actions = new ArrayList<>();

        String[] columns = {GeofenceActionTable.COLUMN_GEOFENCE_ID, GeofenceActionTable.COLUMN_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(GeofenceActionTable.TABLE_NAME, columns,
                GeofenceActionTable.COLUMN_GEOFENCE_ID + "=" + geofenceId, null, null, null, null);
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
     * Get all Actions associated with a specific Geofence
     *
     * @param geofenceId ID of Geofence
     * @return List of Actions
     */
    protected static ArrayList<Action> get(long geofenceId, Geofence.EventType eventType) throws Exception {
        ArrayList<Action> actions = new ArrayList<>();

        String[] columns = {GeofenceActionTable.COLUMN_GEOFENCE_ID, GeofenceActionTable.COLUMN_ACTION_ID};
        Cursor cursor = DatabaseHandler.database.query(GeofenceActionTable.TABLE_NAME, columns,
                GeofenceActionTable.COLUMN_GEOFENCE_ID + "=" + geofenceId
                        + " and " +
                        GeofenceActionTable.COLUMN_EVENT_TYPE + "=" + eventType.name(), null, null, null, null);
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
     * Update Actions for an existing Geofence
     *
     * @param geofence new Geofence
     */
    protected static void update(Geofence geofence) throws Exception {
        // delete current actions
        delete(geofence.getId());
        // add new actions
        for (Geofence.EventType eventType : Geofence.EventType.values()) {
            add(geofence.getActions(eventType), geofence.getId(), eventType);
        }
    }
}
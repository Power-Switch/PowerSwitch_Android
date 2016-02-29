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
import java.util.LinkedList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.table.scene.SceneItemTable;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.DatabaseConstants;
import eu.power_switch.shared.log.Log;

/**
 * Provides database methods for managing SceneItems
 */
abstract class SceneItemHandler {

    /**
     * Adds a List of SceneItems to database
     *
     * @param sceneId ID of Scene the SceneItems will be associated with
     * @param items   list of SceneItems
     */
    protected static void add(Long sceneId, ArrayList<SceneItem> items) throws Exception {
        for (SceneItem item : items) {
            add(sceneId, item);
        }
    }

    /**
     * Adds a SceneItem to database
     *
     * @param sceneId ID of Scene the SceneItems will be associated with
     * @param item    SceneItem
     */
    private static void add(Long sceneId, SceneItem item) throws Exception {
        ContentValues values = new ContentValues();
        values.put(SceneItemTable.COLUMN_SCENE_ID, sceneId);
        values.put(SceneItemTable.COLUMN_RECEIVER_ID, item.getReceiver().getId());
        values.put(SceneItemTable.COLUMN_ACTIVE_BUTTON_ID, item.getActiveButton().getId());
        DatabaseHandler.database.insert(SceneItemTable.TABLE_NAME, null, values);
    }

    /**
     * Update all existing SceneItems related to a specific Receiver
     *
     * @param receiver Receiver used in Scene(s)
     */
    protected static void update(Receiver receiver) throws Exception {
        // TODO:
    }

    /**
     * Update all SceneItems related to a specific Scene
     *
     * @param scene Scene
     */
    protected static void update(Scene scene) throws Exception {
        deleteSceneItems(scene.getId());
        add(scene.getId(), scene.getSceneItems());
    }

    /**
     * Get a list of all SceneItems associated with a Scene
     *
     * @param sceneId ID of Scene
     * @return list of SceneItems
     */
    protected static List<SceneItem> getSceneItems(Long sceneId) throws Exception {
        LinkedList<SceneItem> sceneItems = new LinkedList<>();

        Cursor cursor = DatabaseHandler.database.query(SceneItemTable.TABLE_NAME, null,
                SceneItemTable.COLUMN_SCENE_ID + "==" + sceneId, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            sceneItems.add(dbToSceneItem(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return sceneItems;
    }

    /**
     * Deletes all SceneItems using ReceiverID
     *
     * @param receiverId ID of Receiver
     */
    protected static void deleteByReceiverId(Long receiverId) throws Exception {
        Log.d(SceneItemHandler.class, "Delete SceneItem by ReceiverId: " + receiverId);
        DatabaseHandler.database.delete(SceneItemTable.TABLE_NAME, SceneItemTable.COLUMN_RECEIVER_ID + "=" + receiverId, null);
    }

    /**
     * Deletes all SceneItems related to a specific Scene from Database
     *
     * @param sceneId ID of Scene
     */
    protected static void deleteSceneItems(Long sceneId) throws Exception {
        DatabaseHandler.database.delete(SceneItemTable.TABLE_NAME, SceneItemTable.COLUMN_SCENE_ID + "==" + sceneId, null);
    }

    /**
     * Creates a SceneItem Object out of Database information
     *
     * @param c cursor pointing to a SceneItem database entry
     * @return SceneItem
     */
    private static SceneItem dbToSceneItem(Cursor c) throws Exception {
        long receiverId = c.getLong(1);
        long activeButtonId = c.getLong(2);

        String activeButtonName = getActiveButtonName(activeButtonId);

        Receiver receiver = ReceiverHandler.get(receiverId);

        Button activeButton = null;
        for (Button button : receiver.getButtons()) {
            if (button.getName().equals(activeButtonName)) {
                activeButton = button;
            }
        }

        SceneItem sceneItem = new SceneItem(receiver, activeButton);
        return sceneItem;
    }

    /**
     * Gets name of active Button in SceneItem
     *
     * @param buttonId ID of Button
     * @return Name of active Button
     */
    private static String getActiveButtonName(Long buttonId) throws Exception {
        if (buttonId == DatabaseConstants.BUTTON_ON_ID) {
            return DatabaseHandler.context.getString(R.string.on);
        } else if (buttonId == DatabaseConstants.BUTTON_OFF_ID) {
            return DatabaseHandler.context.getString(R.string.off);
        } else if (buttonId == DatabaseConstants.BUTTON_UP_ID) {
            return DatabaseHandler.context.getString(R.string.up);
        } else if (buttonId == DatabaseConstants.BUTTON_STOP_ID) {
            return DatabaseHandler.context.getString(R.string.stop);
        } else if (buttonId == DatabaseConstants.BUTTON_DOWN_ID) {
            return DatabaseHandler.context.getString(R.string.down);
        } else {
            UniversalButton button = UniversalButtonHandler.getUniversalButton(buttonId);
            return button.getName();
        }
    }
}
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

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import eu.power_switch.database.table.scene.SceneItemTable;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.receiver.Receiver;
import timber.log.Timber;

/**
 * Provides database methods for managing SceneItems
 */
abstract class SceneItemHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private SceneItemHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Adds a List of SceneItems to database
     *
     * @param sceneId ID of Scene the SceneItems will be associated with
     * @param items   list of SceneItems
     */
    protected static void add(Long sceneId, List<SceneItem> items) throws Exception {
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
     * @param receiverId ID of Receiver used in Scene(s)
     */
    protected static void update(Long receiverId) throws Exception {
        Cursor cursor = DatabaseHandler.database.query(SceneItemTable.TABLE_NAME, SceneItemTable.ALL_COLUMNS,
                SceneItemTable.COLUMN_RECEIVER_ID + "==" + receiverId, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long sceneItemId = cursor.getLong(0);
            long buttonId = cursor.getLong(3);
            try {
                Receiver receiver = ReceiverHandler.get(receiverId);
                receiver.getButton(buttonId);

                // all is good, dont change sceneItem
            } catch (NoSuchElementException e) {
                // sceneItem has reference to missing buttonId, remove sceneItem from scene
                delete(sceneItemId);
            }
            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Update all SceneItems related to a specific Scene
     *
     * @param scene Scene
     */
    protected static void update(Scene scene) throws Exception {
        deleteBySceneId(scene.getId());
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

        Cursor cursor = DatabaseHandler.database.query(SceneItemTable.TABLE_NAME, SceneItemTable.ALL_COLUMNS,
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
     * Deletes a SceneItem
     *
     * @param sceneItemId ID of SceneItem
     */
    protected static void delete(Long sceneItemId) throws Exception {
        Timber.d("Delete SceneItem by Id: " + sceneItemId);
        DatabaseHandler.database.delete(SceneItemTable.TABLE_NAME, SceneItemTable.COLUMN_ID + "=" + sceneItemId, null);
    }

    /**
     * Deletes all SceneItems using ReceiverID
     *
     * @param receiverId ID of Receiver
     */
    protected static void deleteByReceiverId(Long receiverId) throws Exception {
        Timber.d("Delete SceneItem by ReceiverId: " + receiverId);
        DatabaseHandler.database.delete(SceneItemTable.TABLE_NAME, SceneItemTable.COLUMN_RECEIVER_ID + "=" + receiverId, null);
    }

    /**
     * Deletes all SceneItems related to a specific Scene from Database
     *
     * @param sceneId ID of Scene
     */
    protected static void deleteBySceneId(Long sceneId) throws Exception {
        DatabaseHandler.database.delete(SceneItemTable.TABLE_NAME, SceneItemTable.COLUMN_SCENE_ID + "==" + sceneId, null);
    }

    /**
     * Creates a SceneItem Object out of Database information
     *
     * @param c cursor pointing to a SceneItem database entry
     * @return SceneItem
     */
    private static SceneItem dbToSceneItem(Cursor c) throws Exception {
        long receiverId = c.getLong(2);
        long activeButtonId = c.getLong(3);

        Receiver receiver = ReceiverHandler.get(receiverId);

        SceneItem sceneItem = new SceneItem(receiver, receiver.getButton(activeButtonId));
        return sceneItem;
    }

}
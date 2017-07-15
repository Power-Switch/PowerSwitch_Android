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

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.database.table.scene.SceneItemTable;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.receiver.Receiver;
import timber.log.Timber;

/**
 * Provides database methods for managing SceneItems
 */
@Singleton
class SceneItemHandler {

    @Inject
    SceneItemHandler() {
    }

    /**
     * Adds a List of SceneItems to database
     *
     * @param sceneId ID of Scene the SceneItems will be associated with
     * @param items   list of SceneItems
     */
    protected void add(@NonNull SQLiteDatabase database, Long sceneId, List<SceneItem> items) throws Exception {
        for (SceneItem item : items) {
            add(database, sceneId, item);
        }
    }

    /**
     * Adds a SceneItem to database
     *
     * @param sceneId ID of Scene the SceneItems will be associated with
     * @param item    SceneItem
     */
    private void add(@NonNull SQLiteDatabase database, Long sceneId, SceneItem item) throws Exception {
        ContentValues values = new ContentValues();
        values.put(SceneItemTable.COLUMN_SCENE_ID, sceneId);
        values.put(SceneItemTable.COLUMN_RECEIVER_ID, item.getReceiverId());
        values.put(SceneItemTable.COLUMN_ACTIVE_BUTTON_ID, item.getButtonId());
        database.insert(SceneItemTable.TABLE_NAME, null, values);
    }

    /**
     * Update all existing SceneItems related to a specific Receiver
     *
     * @param receiver Receiver used in Scene(s)
     */
    protected void update(@NonNull SQLiteDatabase database, Receiver receiver) throws Exception {
        Cursor cursor = database.query(SceneItemTable.TABLE_NAME,
                SceneItemTable.ALL_COLUMNS, SceneItemTable.COLUMN_RECEIVER_ID + "==" + receiver.getId(),
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long sceneItemId = cursor.getLong(0);
            long buttonId    = cursor.getLong(3);

            try {
                receiver.getButton(buttonId);
                // all is good, dont change sceneItem
            } catch (NoSuchElementException e) {
                // sceneItem has reference to missing buttonId, remove sceneItem from scene
                delete(database, sceneItemId);
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
    protected void update(@NonNull SQLiteDatabase database, Scene scene) throws Exception {
        deleteBySceneId(database, scene.getId());
        add(database, scene.getId(), scene.getSceneItems());
    }

    /**
     * Get a list of all SceneItems associated with a Scene
     *
     * @param sceneId ID of Scene
     *
     * @return list of SceneItems
     */
    protected List<SceneItem> getSceneItems(@NonNull SQLiteDatabase database, Long sceneId) throws Exception {
        LinkedList<SceneItem> sceneItems = new LinkedList<>();

        Cursor cursor = database.query(SceneItemTable.TABLE_NAME,
                SceneItemTable.ALL_COLUMNS,
                SceneItemTable.COLUMN_SCENE_ID + "==" + sceneId,
                null,
                null,
                null,
                null);
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
    protected void delete(@NonNull SQLiteDatabase database, Long sceneItemId) throws Exception {
        Timber.d("Delete SceneItem by Id: " + sceneItemId);
        database.delete(SceneItemTable.TABLE_NAME, SceneItemTable.COLUMN_ID + "=" + sceneItemId, null);
    }

    /**
     * Deletes all SceneItems using ReceiverID
     *
     * @param receiverId ID of Receiver
     */
    protected void deleteByReceiverId(@NonNull SQLiteDatabase database, Long receiverId) throws Exception {
        Timber.d("Delete SceneItem by ReceiverId: " + receiverId);
        database.delete(SceneItemTable.TABLE_NAME, SceneItemTable.COLUMN_RECEIVER_ID + "=" + receiverId, null);
    }

    /**
     * Deletes all SceneItems related to a specific Scene from Database
     *
     * @param sceneId ID of Scene
     */
    protected void deleteBySceneId(@NonNull SQLiteDatabase database, Long sceneId) throws Exception {
        database.delete(SceneItemTable.TABLE_NAME, SceneItemTable.COLUMN_SCENE_ID + "==" + sceneId, null);
    }

    /**
     * Creates a SceneItem Object out of Database information
     *
     * @param c cursor pointing to a SceneItem database entry
     *
     * @return SceneItem
     */
    private SceneItem dbToSceneItem(Cursor c) throws Exception {
        long receiverId     = c.getLong(2);
        long activeButtonId = c.getLong(3);

        SceneItem sceneItem = new SceneItem(receiverId, activeButtonId);
        return sceneItem;
    }

}
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

import eu.power_switch.database.table.scene.SceneTable;
import eu.power_switch.obj.receiver.Scene;
import eu.power_switch.obj.receiver.SceneItem;
import eu.power_switch.shared.log.Log;

/**
 * Provides database methods for managing Scenes
 */
abstract class SceneHandler {

    /**
     * Adds a Scene to Database
     *
     * @param scene Scene
     */
    protected static void add(Scene scene) {
        ContentValues values = new ContentValues();
        values.put(SceneTable.COLUMN_NAME, scene.getName());
        long sceneId = DatabaseHandler.database.insert(SceneTable.TABLE_NAME, null, values);
        if (sceneId == -1) {
            Log.e("Error inserting Scene to database");
        }
        SceneItemHandler.add(sceneId, scene.getSceneItems());
    }

    /**
     * Updates a Scene in Database
     *
     * @param scene Scene
     */
    protected static void update(Scene scene) {
        updateName(scene.getId(), scene.getName());
        SceneItemHandler.update(scene);
    }

    /**
     * Updates Scene Name in Database
     *
     * @param id      ID of Scene
     * @param newName new Scene name
     */
    private static void updateName(Long id, String newName) {
        ContentValues values = new ContentValues();
        values.put(SceneTable.COLUMN_NAME, newName);
        DatabaseHandler.database.update(SceneTable.TABLE_NAME, values, SceneTable.COLUMN_ID + "==" + id, null);
    }

    /**
     * Deletes a Scene from Database
     *
     * @param id ID of Scene
     */
    protected static void delete(Long id) {
        ActionHandler.deleteBySceneId(id);

        SceneItemHandler.deleteSceneItems(id);
        DatabaseHandler.database.delete(SceneTable.TABLE_NAME, SceneTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Gets a Scene from Database
     *
     * @param name Name of Scene
     * @return Scene
     */
    protected static Scene get(String name) {
        Cursor cursor = DatabaseHandler.database.query(SceneTable.TABLE_NAME, null, SceneTable.COLUMN_NAME + "=='" + name + "'", null,
                null, null, null);
        cursor.moveToFirst();
        Scene scene = dbToScene(cursor);
        cursor.close();
        return scene;
    }

    /**
     * Gets a Scene from Database
     *
     * @param id ID of Scene
     * @return Scene
     */
    protected static Scene get(Long id) {
        Cursor cursor = DatabaseHandler.database.query(SceneTable.TABLE_NAME, null, SceneTable.COLUMN_ID + "==" + id, null, null, null,
                null);
        cursor.moveToFirst();
        Scene scene = dbToScene(cursor);
        cursor.close();
        return scene;
    }

    /**
     * Gets all Scenes from Database
     *
     * @return List of Scene
     */
    protected static List<Scene> getAll() {
        List<Scene> scenes = new ArrayList<>();
        Cursor cursor = DatabaseHandler.database.query(SceneTable.TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            scenes.add(dbToScene(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return scenes;
    }

    /**
     * Creates a Scene Object out of Database information
     *
     * @param c cursor pointing to a Scene database entry
     * @return Scene
     */
    private static Scene dbToScene(Cursor c) {
        Scene scene = new Scene(c.getLong(0), c.getString(1));
        for (SceneItem item : SceneItemHandler.getSceneItems(scene.getId())) {
            scene.addSceneItem(item);
        }
        return scene;
    }


}
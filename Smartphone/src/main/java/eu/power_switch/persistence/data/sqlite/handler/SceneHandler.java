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
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.persistence.data.sqlite.table.scene.SceneTable;
import timber.log.Timber;

/**
 * Provides database methods for managing Scenes
 */
@Singleton
class SceneHandler {

    @Inject
    ActionHandler    actionHandler;
    @Inject
    SceneItemHandler sceneItemHandler;

    @Inject
    SceneHandler() {
    }

    /**
     * Adds a Scene to Database
     *
     * @param scene Scene
     */
    protected void add(@NonNull SQLiteDatabase database, Scene scene) throws Exception {
        ContentValues values = new ContentValues();
        values.put(SceneTable.COLUMN_APARTMENT_ID, scene.getApartmentId());
        values.put(SceneTable.COLUMN_NAME, scene.getName());
        long sceneId = database.insert(SceneTable.TABLE_NAME, null, values);
        if (sceneId == -1) {
            Timber.e("Error inserting Scene to database");
        }
        sceneItemHandler.add(database, sceneId, scene.getSceneItems());
    }

    /**
     * Updates a Scene in Database
     *
     * @param scene Scene
     */
    protected void update(@NonNull SQLiteDatabase database, Scene scene) throws Exception {
        updateName(database, scene.getId(), scene.getName());
        sceneItemHandler.update(database, scene);
    }

    /**
     * Updates Scene Name in Database
     *
     * @param id      ID of Scene
     * @param newName new Scene name
     */
    private void updateName(@NonNull SQLiteDatabase database, Long id, String newName) throws Exception {
        ContentValues values = new ContentValues();
        values.put(SceneTable.COLUMN_NAME, newName);
        database.update(SceneTable.TABLE_NAME, values, SceneTable.COLUMN_ID + "==" + id, null);
    }

    /**
     * Deletes a Scene from Database
     *
     * @param id ID of Scene
     */
    protected void delete(@NonNull SQLiteDatabase database, Long id) throws Exception {
        actionHandler.deleteBySceneId(database, id);

        sceneItemHandler.deleteBySceneId(database, id);
        database.delete(SceneTable.TABLE_NAME, SceneTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Gets a Scene from Database
     *
     * @param name Name of Scene
     *
     * @return Scene
     */
    @NonNull
    protected Scene get(@NonNull SQLiteDatabase database, String name) throws Exception {
        Scene scene = null;
        Cursor cursor = database.query(SceneTable.TABLE_NAME,
                SceneTable.ALL_COLUMNS,
                SceneTable.COLUMN_NAME + "=='" + name + "'",
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            scene = dbToScene(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(name);
        }

        cursor.close();
        return scene;
    }

    /**
     * Gets a Scene from Database
     *
     * @param id ID of Scene
     *
     * @return Scene
     */
    @NonNull
    protected Scene get(@NonNull SQLiteDatabase database, Long id) throws Exception {
        Scene  scene  = null;
        Cursor cursor = database.query(SceneTable.TABLE_NAME, SceneTable.ALL_COLUMNS, SceneTable.COLUMN_ID + "==" + id, null, null, null, null);

        if (cursor.moveToFirst()) {
            scene = dbToScene(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return scene;
    }

    public String getName(SQLiteDatabase database, Long id) {
        String[] columns = {SceneTable.COLUMN_NAME};
        Cursor   cursor  = database.query(SceneTable.TABLE_NAME, columns, SceneTable.COLUMN_ID + "==" + id, null, null, null, null);

        String name;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return name;
    }

    public LinkedList<Scene> getByApartment(@NonNull SQLiteDatabase database, Long id) throws Exception {
        LinkedList<Scene> scenes = new LinkedList<>();
        Cursor cursor = database.query(SceneTable.TABLE_NAME,
                SceneTable.ALL_COLUMNS,
                SceneTable.COLUMN_APARTMENT_ID + "==" + id,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            scenes.add(dbToScene(database, cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return scenes;
    }

    /**
     * Gets all Scenes from Database
     *
     * @return List of Scene
     */
    protected List<Scene> getAll(@NonNull SQLiteDatabase database) throws Exception {
        List<Scene> scenes = new ArrayList<>();
        Cursor      cursor = database.query(SceneTable.TABLE_NAME, SceneTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            scenes.add(dbToScene(database, cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return scenes;
    }

    /**
     * Creates a Scene Object out of Database information
     *
     * @param c cursor pointing to a Scene database entry
     *
     * @return Scene
     */
    private Scene dbToScene(@NonNull SQLiteDatabase database, Cursor c) throws Exception {
        long   id          = c.getLong(0);
        long   apartmentId = c.getLong(1);
        String name        = c.getString(2);
        int    position    = c.getInt(3);

        Scene scene = new Scene(id, apartmentId, name);
        for (SceneItem item : sceneItemHandler.getSceneItems(database, scene.getId())) {
            scene.addSceneItem(item);
        }
        return scene;
    }

}
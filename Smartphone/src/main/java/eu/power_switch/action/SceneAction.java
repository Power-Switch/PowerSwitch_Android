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

package eu.power_switch.action;

import android.content.Context;
import android.support.annotation.NonNull;

import eu.power_switch.obj.Scene;

/**
 * SceneAction that holds a specific scene to activate on execution
 * <p/>
 * Created by Markus on 24.09.2015.
 */
public class SceneAction extends Action {

    private Scene scene;

    public SceneAction(long id, @NonNull Scene scene) {
        this.id = id;
        this.scene = scene;
    }

    public
    @NonNull
    Scene getScene() {
        return scene;
    }

    @Override
    @ActionType
    @NonNull
    public String getActionType() {
        return ACTION_TYPE_SCENE;
    }

    @Override
    public
    @NonNull
    String toString() {
        return scene.getName();
    }

    @Override
    public void execute(@NonNull Context context) {
        ActionHandler.execute(context, scene);
    }


}

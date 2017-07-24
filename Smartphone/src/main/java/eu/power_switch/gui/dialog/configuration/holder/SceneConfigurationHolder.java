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

package eu.power_switch.gui.dialog.configuration.holder;

import android.text.TextUtils;

import java.util.List;

import eu.power_switch.gui.dialog.configuration.ConfigurationHolder;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Markus on 03.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SceneConfigurationHolder extends ConfigurationHolder {

    private Scene scene;

    private String name;

    private List<Room> checkedReceivers;

    private List<SceneItem> sceneItems;

    @Override
    public boolean isValid() throws Exception {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        if (checkedReceivers == null || checkedReceivers.isEmpty()) {
            return false;
        }

        if (sceneItems == null || sceneItems.isEmpty()) {
            return false;
        }

        return true;
    }

}

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

package eu.power_switch.obj;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import lombok.Data;
import lombok.ToString;

/**
 * Represents a Scene
 * A Scene is a set of SceneItems
 */
@ToString
@Data
public class Scene {

    /**
     * List containing all SceneItems
     */
    private List<SceneItem> sceneItems;

    /**
     * ID of this Scene
     */
    private Long id;

    /**
     * ID of Apartment this Scene belongs to
     */
    private Long apartmentId;

    /**
     * Name of this Scene
     */
    private String name;

    /**
     * Constructor
     *
     * @param id          ID of this Scene
     * @param apartmentId ID of Apartment
     * @param name        Name of this Scene
     */
    public Scene(Long id, Long apartmentId, String name) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.name = name;
        sceneItems = new ArrayList<>();
    }

    /**
     * Add a receiver and one of its buttons as a SceneItem to this Scene
     *
     * @param receiver
     * @param activeButton
     */
    public void addSceneItem(Receiver receiver, Button activeButton) {
        sceneItems.add(new SceneItem(receiver.getId(), activeButton.getId()));
    }

    /**
     * Add a receiver and the first of its Buttons as a SceneItem to this Scene
     *
     * @param receiver
     */
    public void addSceneItem(Receiver receiver) {
        sceneItems.add(new SceneItem(receiver.getId(),
                receiver.getButtons()
                        .get(0)
                        .getId()));
    }

    /**
     * Add a SceneItem to this Scene
     *
     * @param sceneItem
     */
    public void addSceneItem(SceneItem sceneItem) {
        sceneItems.add(sceneItem);
    }

    /**
     * Add a List of SceneItem to this Scene
     *
     * @param sceneItems
     */
    public void addSceneItems(List<SceneItem> sceneItems) {
        this.sceneItems.addAll(sceneItems);
    }

}

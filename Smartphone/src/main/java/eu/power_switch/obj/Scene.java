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

package eu.power_switch.obj;

import java.util.ArrayList;

import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;

/**
 * Represents a Scene
 * A Scene is a set of SceneItems
 */
public class Scene {

    /**
     * List containing all SceneItems
     */
    ArrayList<SceneItem> items;

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
        items = new ArrayList<>();
    }

    /**
     * Get ID of this Scene
     *
     * @return ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Get ID of Apartment this Scene belongs to
     *
     * @return ID of Apartment
     */
    public Long getApartmentId() {
        return apartmentId;
    }

    /**
     * Get name of this Scene
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Add a receiver and one of its buttons as a SceneItem to this Scene
     *
     * @param receiver
     * @param activeButton
     */
    public void addSceneItem(Receiver receiver, Button activeButton) {
        items.add(new SceneItem(receiver, activeButton));
    }

    /**
     * Add a receiver and the first of its Buttons as a SceneItem to this Scene
     *
     * @param receiver
     */
    public void addSceneItem(Receiver receiver) {
        items.add(new SceneItem(receiver, receiver.getButtons().getFirst()));
    }

    /**
     * Add a SceneItem to this Scene
     *
     * @param sceneItem
     */
    public void addSceneItem(SceneItem sceneItem) {
        items.add(sceneItem);
    }

    /**
     * Add a List of SceneItem to this Scene
     *
     * @param sceneItems
     */
    public void addSceneItems(ArrayList<SceneItem> sceneItems) {
        items.addAll(sceneItems);
    }

    /**
     * Get all SceneItems in this Scene
     *
     * @return SceneItem list
     */
    public ArrayList<SceneItem> getSceneItems() {
        return items;
    }


}

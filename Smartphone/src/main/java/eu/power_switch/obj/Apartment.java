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

import android.location.Location;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an Apartment that contains Rooms and Scenes
 * <p/>
 * Created by Markus on 21.12.2015.
 */
public class Apartment {
    /**
     * ID of this Apartment
     */
    private Long id;

    /**
     * Name of this Apartment
     */
    private String name;

    /**
     * List of all Rooms that this Apartment contains
     */
    private List<Room> rooms;

    /**
     * List of all Scenes that this Apartment contains
     */
    private List<Scene> scenes;

    /**
     * Flag to indicate that this Apartment is the currently active one
     */
    private boolean isActive;

    private Location location;

    public Apartment(Long id, String name) {
        this.id = id;
        this.name = name;
        this.rooms = Collections.EMPTY_LIST;
        this.scenes = Collections.EMPTY_LIST;
    }

    public Apartment(Long id, String name, List<Room> rooms, List<Scene> scenes) {
        this.id = id;
        this.name = name;
        this.rooms = rooms;
        this.scenes = scenes;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Room getRoom(String name) {
        for (Room room : rooms) {
            if (room.getName().equals(name)) {
                return room;
            }
        }
        return null;
    }

    public Scene getScene(String name) {
        for (Scene scene : scenes) {
            if (scene.getName().equals(name)) {
                return scene;
            }
        }
        return null;
    }
}

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

import java.util.LinkedList;

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
    private LinkedList<Room> rooms;

    /**
     * List of all Scenes that this Apartment contains
     */
    private LinkedList<Scene> scenes;

    private Location location;

    public Apartment(Long id, String name, LinkedList<Room> rooms, LinkedList<Scene> scenes) {
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

    public LinkedList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(LinkedList<Room> rooms) {
        this.rooms = rooms;
    }

    public LinkedList<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(LinkedList<Scene> scenes) {
        this.scenes = scenes;
    }
}

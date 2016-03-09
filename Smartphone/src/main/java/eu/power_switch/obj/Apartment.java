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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.log.LogHandler;

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
     * List of associated Gateways that will send network signals
     */
    private List<Gateway> gateways;

    /**
     * Flag to indicate that this Apartment is the currently active one
     */
    private boolean isActive;

    /**
     * Associated Geofence for this Apartment
     */
    private Geofence geofence;

    public Apartment(@NonNull Long id, @NonNull String name) {
        this.id = id;
        this.name = name;
        this.rooms = Collections.EMPTY_LIST;
        this.scenes = Collections.EMPTY_LIST;
        this.gateways = Collections.EMPTY_LIST;
    }

    public Apartment(@NonNull Long id, @NonNull String name, @NonNull List<Gateway> gateways, @Nullable Geofence geofence) {
        this.id = id;
        this.name = name;
        this.rooms = Collections.EMPTY_LIST;
        this.scenes = Collections.EMPTY_LIST;
        this.gateways = gateways;
        this.geofence = geofence;
    }

    public Apartment(@NonNull Long id, @NonNull String name, @NonNull List<Room> rooms, @NonNull List<Scene> scenes, @NonNull List<Gateway> gateways, @Nullable Geofence geofence) {
        this.id = id;
        this.name = name;
        this.rooms = rooms;
        this.scenes = scenes;
        this.gateways = gateways;
        this.geofence = geofence;
    }

    /**
     * Get the ID of this apartment
     *
     * @return id of this apartment
     */
    public Long getId() {
        return id;
    }

    /**
     * Get the name of this apartment
     *
     * @return name of this apartment
     */
    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    /**
     * Get the list of rooms of this apartment
     *
     * @return list of rooms
     */
    @NonNull
    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(@NonNull List<Room> rooms) {
        this.rooms = rooms;
    }

    /**
     * Get the list of scenes of this apartment
     *
     * @return list of scenes
     */
    @NonNull
    public List<Scene> getScenes() {
        return scenes;
    }

    /**
     * Set the list of scenes of this apartment
     *
     * @param scenes list of scenes
     */
    public void setScenes(@NonNull List<Scene> scenes) {
        this.scenes = scenes;
    }

    /**
     * Get associated Gateways of this apartment
     *
     * @return list of Gateways
     */
    @NonNull
    public List<Gateway> getAssociatedGateways() {
        return gateways;
    }

    /**
     * Get active state of this apartment
     *
     * @return true if the apartment is currently active in gui, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Set active state of this apartment
     *
     * @param active true if the apartment is currently active in gui, false otherwise
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Get a room in this apartment by its name
     *
     * @param name name of room
     * @return Room
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Room getRoom(@Nullable String name) {
        for (Room room : rooms) {
            if (room.getName().equals(name)) {
                return room;
            }
        }
        throw new NoSuchElementException("Room \"" + name + "\" not found");
    }

    /**
     * Get a room in this apartment by its name, ignoring case
     *
     * @param name name of room
     * @return Room
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Room getRoomCaseInsensitive(@Nullable String name) {
        for (Room room : rooms) {
            if (room.getName().equalsIgnoreCase(name)) {
                return room;
            }
        }
        throw new NoSuchElementException("Room \"" + name + "\" not found");
    }

    /**
     * Get a room in this apartment by its id
     *
     * @param id id of room
     * @return Room
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Room getRoom(@Nullable Long id) {
        for (Room room : rooms) {
            if (room.getId().equals(id)) {
                return room;
            }
        }
        throw new NoSuchElementException("Room with ID  \"" + id + "\" not found");
    }

    /**
     * Get a scene in this apartment by its name
     *
     * @param name name of scene
     * @return Scene
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Scene getScene(@Nullable String name) {
        for (Scene scene : scenes) {
            if (scene.getName().equals(name)) {
                return scene;
            }
        }
        throw new NoSuchElementException("Scene \"" + name + "\" not found");
    }

    /**
     * Get a scene in this apartment by its name, ignoring case
     *
     * @param name name of scene
     * @return Scene
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Scene getSceneCaseInsensitive(@Nullable String name) {
        for (Scene scene : scenes) {
            if (scene.getName().equalsIgnoreCase(name)) {
                return scene;
            }
        }
        throw new NoSuchElementException("Scene \"" + name + "\" not found");
    }

    /**
     * Get a scene in this apartment by its id
     *
     * @param id id of scene
     * @return Scene
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Scene getScene(@Nullable Long id) {
        for (Scene scene : scenes) {
            if (scene.getId().equals(id)) {
                return scene;
            }
        }
        throw new NoSuchElementException("Scene with ID \"" + id + "\" not found");
    }

    /**
     * Get geofence of this apartment
     *
     * @return Geofence, null if none exists
     */
    @Nullable
    public Geofence getGeofence() {
        return geofence;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Apartment: ").
                append(getName())
                .append("(").append(getId()).append(")")
                .append(" {\n");

        for (Room room : getRooms()) {
            stringBuilder.append(LogHandler.addIndentation(room.toString())).append("\n");
        }

        for (Scene scene : getScenes()) {
            stringBuilder.append(LogHandler.addIndentation(scene.toString())).append("\n");
        }

        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}

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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.obj.gateway.Gateway;
import lombok.Data;
import lombok.ToString;

/**
 * Represents an Apartment that contains Rooms and Scenes
 * <p/>
 * Created by Markus on 21.12.2015.
 */
@Data
@ToString
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
    private List<Gateway> associatedGateways;

    /**
     * Flag to indicate that this Apartment is the currently active one
     */
    private boolean isActive;

    /**
     * Associated Geofence for this Apartment
     */
    private Geofence geofence;

    public Apartment(@NonNull Long id, boolean isActive, @NonNull String name, @NonNull List<Room> rooms, @NonNull List<Scene> scenes,
                     @NonNull List<Gateway> associatedGateways, @Nullable Geofence geofence) {
        this.id = id;
        this.isActive = isActive;
        this.name = name;
        this.rooms = rooms;
        this.scenes = scenes;
        this.associatedGateways = associatedGateways;
        this.geofence = geofence;
    }

    public Apartment(@NonNull Long id, boolean isActive, @NonNull String name, @NonNull List<Gateway> associatedGateways,
                     @Nullable Geofence geofence) {
        this(id, isActive, name, Collections.EMPTY_LIST, Collections.EMPTY_LIST, associatedGateways, geofence);
    }

    /**
     * Checks if this Apartment is associated with the given Gateway
     *
     * @param gatewayId Gateway ID to check
     *
     * @return true if this apartment is associated with the given Gateway, false otherwise
     */
    public boolean isAssociatedWith(long gatewayId) {
        for (Gateway associatedGateway : getAssociatedGateways()) {
            if (associatedGateway.getId()
                    .equals(gatewayId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get a room in this apartment by its name
     *
     * @param name name of room
     *
     * @return Room
     *
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Room getRoom(@Nullable String name) {
        for (Room room : rooms) {
            if (room.getName()
                    .equals(name)) {
                return room;
            }
        }
        throw new NoSuchElementException("Room \"" + name + "\" not found");
    }

    /**
     * Get a room in this apartment by its name, ignoring case
     *
     * @param name name of room
     *
     * @return Room
     *
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Room getRoomCaseInsensitive(@Nullable String name) {
        for (Room room : rooms) {
            if (room.getName()
                    .equalsIgnoreCase(name)) {
                return room;
            }
        }
        throw new NoSuchElementException("Room \"" + name + "\" not found");
    }

    /**
     * Get a room in this apartment by its id
     *
     * @param id id of room
     *
     * @return Room
     *
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Room getRoom(@Nullable Long id) {
        for (Room room : rooms) {
            if (room.getId()
                    .equals(id)) {
                return room;
            }
        }
        throw new NoSuchElementException("Room with ID  \"" + id + "\" not found");
    }

    /**
     * Get a scene in this apartment by its name
     *
     * @param name name of scene
     *
     * @return Scene
     *
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Scene getScene(@Nullable String name) {
        for (Scene scene : scenes) {
            if (scene.getName()
                    .equals(name)) {
                return scene;
            }
        }
        throw new NoSuchElementException("Scene \"" + name + "\" not found");
    }

    /**
     * Get a scene in this apartment by its name, ignoring case
     *
     * @param name name of scene
     *
     * @return Scene
     *
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Scene getSceneCaseInsensitive(@Nullable String name) {
        for (Scene scene : scenes) {
            if (scene.getName()
                    .equalsIgnoreCase(name)) {
                return scene;
            }
        }
        throw new NoSuchElementException("Scene \"" + name + "\" not found");
    }

    /**
     * Get a scene in this apartment by its id
     *
     * @param id id of scene
     *
     * @return Scene
     *
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Scene getScene(@Nullable Long id) {
        for (Scene scene : scenes) {
            if (scene.getId()
                    .equals(id)) {
                return scene;
            }
        }
        throw new NoSuchElementException("Scene with ID \"" + id + "\" not found");
    }
}

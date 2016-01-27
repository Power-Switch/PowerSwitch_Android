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

package eu.power_switch.google_play_services.geofence;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.power_switch.action.Action;

/**
 * Internal representation of a Geofence
 * <p/>
 * Created by Markus on 26.01.2016.
 */
public class Geofence {

    public static final double INVALID_LAT = Integer.MAX_VALUE;
    public static final double INVALID_LON = Integer.MAX_VALUE;

    /**
     * ID of this Geofence
     */
    private long id;
    /**
     * Flag if this Geofence is in active use
     */
    private boolean active;
    /**
     * Name of this Geofence
     */
    private String name;
    /**
     * Center location of this Geofence
     */
    private LatLng centerLocation;
    /**
     * Radius of this Geofence
     */
    private double radius;
    private HashMap<EventType, List<Action>> actionsMap;

    public Geofence(Long id, boolean active, String name, LatLng centerLocation, double radius) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.centerLocation = centerLocation;
        this.radius = radius;
        actionsMap = new HashMap<>();

        for (EventType eventType : EventType.values()) {
            actionsMap.put(eventType, new ArrayList<Action>());
        }
    }

    /**
     * Get ID of this Geofence
     *
     * @return ID of this Geofence
     */
    public long getId() {
        return id;
    }

    /**
     * Returns if this Geofence is active or not
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set the active state of this Geofence
     *
     * @param active true if active, false otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Get name of this Geofence
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the center location of this Geofence
     *
     * @return center location
     */
    public LatLng getCenterLocation() {
        return centerLocation;
    }

    /**
     * Get radius of this Geofence
     *
     * @return radius
     */
    public double getRadius() {
        return radius;
    }

    public List<Action> getActions(EventType eventType) {
        return actionsMap.get(eventType);
    }

    /**
     * Possible event types for actions
     */
    public enum EventType {
        ENTER,
        EXIT
    }
}

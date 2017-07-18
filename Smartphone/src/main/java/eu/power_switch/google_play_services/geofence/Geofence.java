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

package eu.power_switch.google_play_services.geofence;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.google.android.gms.maps.model.LatLng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.power_switch.action.Action;
import eu.power_switch.shared.log.LogHelper;

/**
 * Internal representation of a Geofence
 * <p/>
 * Created by Markus on 26.01.2016.
 */
public class Geofence {


    public static final String STATE_INSIDE = "Inside";
    public static final String STATE_OUTSIDE = "Outside";
    public static final String STATE_NONE = "none";
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
    /**
     * Map of Actions per EventType
     */
    private Map<EventType, List<Action>> actionsMap;
    /**
     * Snapshot of this Geofence
     */
    private Bitmap snapshot;
    /**
     * State of this Geofence (Inside, Outside, "")
     */
    @State
    private String state;

    public Geofence(@NonNull Long id, boolean active, @NonNull String name, @NonNull LatLng centerLocation, double radius, @Nullable Bitmap snapshot, @Nullable List<Action> enterActions, @Nullable List<Action> exitActions, @NonNull @State String state) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.centerLocation = centerLocation;
        this.radius = radius;
        this.snapshot = snapshot;

        this.actionsMap = new HashMap<>();
        if (enterActions != null) {
            actionsMap.put(EventType.ENTER, enterActions);
        } else {
            actionsMap.put(EventType.ENTER, new ArrayList<Action>());
        }
        if (exitActions != null) {
            actionsMap.put(EventType.EXIT, exitActions);
        } else {
            actionsMap.put(EventType.EXIT, new ArrayList<Action>());
        }

        this.state = state;
    }

    public Geofence(@NonNull Long id, boolean active, @NonNull String name, @NonNull LatLng centerLocation, double radius, @Nullable Bitmap snapshot, @NonNull Map<EventType, List<Action>> actionsMap, @NonNull @State String state) {
        this(id, active, name, centerLocation, radius, snapshot, actionsMap.get(EventType.ENTER), actionsMap.get(EventType.EXIT), state);
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
     * Set ID of this Geofence
     *
     * @param id new ID of this Gateway
     */
    public void setId(long id) {
        this.id = id;
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
     * Set name of this Geofence
     *
     * @param name Name of this Geofence
     */
    public void setName(String name) {
        this.name = name;
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
     * Set the center location of this Geofence
     *
     * @param centerLocation center location
     */
    public void setCenterLocation(LatLng centerLocation) {
        this.centerLocation = centerLocation;
    }

    /**
     * Get radius of this Geofence
     *
     * @return radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Set radius of this Geofence
     *
     * @param radius radius
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Get actions of a specific EventType of this Geofence
     *
     * @param eventType EventType
     * @return List of Actions
     */
    public List<Action> getActions(EventType eventType) {
        return actionsMap.get(eventType);
    }

    /**
     * Get snapshot of this Geofence
     *
     * @return snapshot
     */
    public Bitmap getSnapshot() {
        return snapshot;
    }

    /**
     * Set snapshot of this Geofence
     *
     * @param snapshot snapshot
     */
    public void setSnapshot(Bitmap snapshot) {
        this.snapshot = snapshot;
    }

    @State
    @NonNull
    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Geofence: ");
        if (active) {
            stringBuilder.append("(enabled) ");
        } else {
            stringBuilder.append("(disabled) ");
        }
        stringBuilder.append("(").append(state).append(") ");
        stringBuilder.append(getName())
                .append("(").append(getId()).append(") Location: ")
                .append(getCenterLocation().toString())
                .append(" {\n");

        StringBuilder eventActions = new StringBuilder();
        for (EventType eventType : actionsMap.keySet()) {
            eventActions.append("EventType: ").append(eventType.toString()).append(" {\n");
            for (Action action : actionsMap.get(eventType)) {
                eventActions.append(LogHelper.addIndentation(action.toString()))
                        .append("\n");
            }
            eventActions.append("}\n");
        }
        stringBuilder.append(LogHelper.addIndentation(eventActions.toString()));

        stringBuilder.append("\n}");
        return stringBuilder.toString();
    }

    /**
     * Possible event types for actions
     */
    public enum EventType {
        ENTER,
        EXIT
    }

    @StringDef({STATE_INSIDE, STATE_OUTSIDE, STATE_NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }
}

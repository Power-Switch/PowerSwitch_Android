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

package eu.power_switch.phone.call;

import java.util.List;

import eu.power_switch.action.Action;

/**
 * Internal representation of a Call Object
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class CallEvent {

    /**
     * ID of this CallEvent
     */
    private long id;

    /**
     * Active state of this CallEvent
     */
    private boolean active;

    /**
     * Name of this CallEvent
     */
    private String name;

    /**
     * List of phone numbers
     */
    private List<String> phoneNumbers;

    /**
     * List of actions
     */
    private List<Action> actions;

    /**
     * Constructor
     *
     * @param id           ID
     * @param phoneNumbers list of phone numbers
     * @param actions      list of actions
     */
    public CallEvent(long id, boolean active, String name, List<String> phoneNumbers, List<Action> actions) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.phoneNumbers = phoneNumbers;
        this.actions = actions;
    }

    /**
     * Get ID of this CallEvent
     *
     * @return id of this CallEvent
     */
    public long getId() {
        return id;
    }

    /**
     * Get active state of this CallEvent
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Get name of this CallEvent
     *
     * @return name of this CallEvent
     */
    public String getName() {
        return name;
    }

    /**
     * Get phone numbers this CallEvent is associated with
     *
     * @return list of phone numbers
     */
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * Get list of actions for this CallEvent
     *
     * @return list of actions
     */
    public List<Action> getActions() {
        return actions;
    }
}

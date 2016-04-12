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
import java.util.Map;
import java.util.Set;

import eu.power_switch.action.Action;
import eu.power_switch.shared.constants.PhoneConstants.Type;

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
     * Map of phone numbers per EventType
     */
    private Map<Type, Set<String>> phoneNumbersMap;

    /**
     * Map of Actions per EventType
     */
    private Map<Type, List<Action>> actionsMap;

    /**
     * Constructor
     *
     * @param id
     * @param active
     * @param name
     * @param phoneNumbersMap
     * @param actionsMap
     */
    public CallEvent(long id, boolean active, String name, Map<Type, Set<String>> phoneNumbersMap, Map<Type, List<Action>> actionsMap) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.phoneNumbersMap = phoneNumbersMap;
        this.actionsMap = actionsMap;
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
     * @param type EventType
     * @return list of phone numbers
     */
    public Set<String> getPhoneNumbers(Type type) {
        return phoneNumbersMap.get(type);
    }

    /**
     * Get list of actions for this CallEvent
     *
     * @param type EventType
     * @return list of actions
     */
    public List<Action> getActions(Type type) {
        return actionsMap.get(type);
    }
}

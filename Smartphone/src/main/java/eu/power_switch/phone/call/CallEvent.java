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

import eu.power_switch.shared.action.Action;
import eu.power_switch.shared.constants.PhoneConstants.CallType;
import lombok.Data;
import lombok.ToString;

/**
 * Internal representation of a Call Object
 * <p/>
 * Created by Markus on 05.04.2016.
 */
@Data
@ToString
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
    private Map<CallType, Set<String>> phoneNumbersMap;

    /**
     * Map of Actions per EventType
     */
    private Map<CallType, List<Action>> actionsMap;

    /**
     * Constructor
     *
     * @param id              ID
     * @param active          active state
     * @param name            name
     * @param phoneNumbersMap phone numbers per EventType
     * @param actionsMap      actions per EventType
     */
    public CallEvent(long id, boolean active, String name, Map<CallType, Set<String>> phoneNumbersMap, Map<CallType, List<Action>> actionsMap) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.phoneNumbersMap = phoneNumbersMap;
        this.actionsMap = actionsMap;
    }

    /**
     * Get phone numbers this CallEvent is associated with
     *
     * @param callType EventType
     * @return Set of phone numbers
     */
    public Set<String> getPhoneNumbers(CallType callType) {
        return phoneNumbersMap.get(callType);
    }

    /**
     * Set phone numbers this CallEvent is associated with
     *
     * @param callType     EventType
     * @param phoneNumbers Set of phone numbers
     */
    public void setPhoneNumbers(CallType callType, Set<String> phoneNumbers) {
        phoneNumbersMap.put(callType, phoneNumbers);
    }

    /**
     * Get list of actions for this CallEvent
     *
     * @param callType EventType
     * @return list of actions
     */
    public List<Action> getActions(CallType callType) {
        return actionsMap.get(callType);
    }

    /**
     * Set list of actions for this CallEvent
     *
     * @param callType EventType
     * @param actions  list of actions
     */
    public void setActions(CallType callType, List<Action> actions) {
        actionsMap.put(callType, actions);
    }

}

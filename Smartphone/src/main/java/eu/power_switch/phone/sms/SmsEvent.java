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

package eu.power_switch.phone.sms;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.power_switch.shared.action.Action;
import eu.power_switch.shared.constants.PhoneConstants;

/**
 * Created by Markus on 19.04.2016.
 */
public class SmsEvent {

    /**
     * ID of this SmsEvent
     */
    private long id;

    /**
     * Active state of this SmsEvent
     */
    private boolean active;

    /**
     * Map of phone numbers per EventType
     */
    private Map<PhoneConstants.SmsType, Set<String>> phoneNumbersMap;

    /**
     * Map of Actions per EventType
     */
    private Map<PhoneConstants.SmsType, List<Action>> actionsMap;

    /**
     * Constructor
     *
     * @param id              ID
     * @param isActive        Active state
     * @param phoneNumbersMap phone numbers per EventType
     * @param actionsMap      actions per EventType
     */
    public SmsEvent(long id, boolean isActive, Map<PhoneConstants.SmsType, Set<String>> phoneNumbersMap, Map<PhoneConstants.SmsType, List<Action>> actionsMap) {
        this.id = id;
        this.active = isActive;
        this.phoneNumbersMap = phoneNumbersMap;
        this.actionsMap = actionsMap;
    }

    /**
     * Get ID of this SmsEvent
     *
     * @return ID of this SmsEvent
     */
    public long getId() {
        return id;
    }

    /**
     * Get active state of this SmsEvent
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Get phone numbers this SmsEvent is associated with
     *
     * @param type EventType
     * @return list of phone numbers
     */
    public Set<String> getPhoneNumbers(PhoneConstants.SmsType type) {
        return phoneNumbersMap.get(type);
    }

    /**
     * Get list of actions for this SmsEvent
     *
     * @param type EventType
     * @return list of actions
     */
    public List<Action> getActions(PhoneConstants.SmsType type) {
        return actionsMap.get(type);
    }
}

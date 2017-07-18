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

package eu.power_switch.shared.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class holding constants related to internal broadcasts for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 11.11.2015.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalBroadcastConstants {

    // LocalBroadcastConstants


    public static final String INTENT_CALL_EVENT_PHONE_NUMBERS_CHANGED = "eu.power_switch.call_event_phone_numbers_changed";
    public static final String INTENT_CALL_EVENT_ACTIONS_CHANGED       = "eu.power_switch.call_event_actions_changed";

    public static final String INTENT_SMS_EVENTS_CHANGED           = "eu.power_switch.sms_events_changed";
    public static final String INTENT_SMS_EVENT_ACTION_ADDED       = "eu.power_switch.sms_event_action_added";

}

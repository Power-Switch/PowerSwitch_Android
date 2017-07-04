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

package eu.power_switch.shared.constants;

/**
 * Class holding constants related to internal broadcasts for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 11.11.2015.
 */
public class LocalBroadcastConstants {

    // LocalBroadcastConstants
    public static final String INTENT_GATEWAY_SSID_ADDED         = "eu.power_switch.gateway_ssid_added";

    public static final String INTENT_ROOM_ADDED                 = "eu.power_switch.room_added";

    public static final String INTENT_GEOFENCE_LOCATION_CHANGED      = "eu.power_switch.geofence_location_changed";
    public static final String INTENT_GEOFENCE_ENTER_ACTION_ADDED    = "eu.power_switch.geofence_enter_action_added";
    public static final String INTENT_GEOFENCE_ENTER_ACTIONS_CHANGED = "eu.power_switch.geofence_enter_actions_changed";
    public static final String INTENT_GEOFENCE_EXIT_ACTION_ADDED     = "eu.power_switch.geofence_exit_action_added";
    public static final String INTENT_GEOFENCE_EXIT_ACTIONS_CHANGED  = "eu.power_switch.geofence_exit_actions_changed";

    public static final String INTENT_TIMER_NAME_EXECUTION_TIME_CHANGED = "eu.power_switch.timer_name_execution_time_changed";
    public static final String INTENT_TIMER_EXECUTION_INTERVAL_CHANGED  = "eu.power_switch.timer_execution_interval_changed";
    public static final String INTENT_TIMER_ACTIONS_CHANGED             = "eu.power_switch.timer_actions_changed";

    public static final String INTENT_TIMER_ACTION_ADDED       = "eu.power_switch.timer_action_added";

    public static final String INTENT_CALL_EVENT_PHONE_NUMBERS_CHANGED = "eu.power_switch.call_event_phone_numbers_changed";
    public static final String INTENT_CALL_EVENT_ACTIONS_CHANGED       = "eu.power_switch.call_event_actions_changed";

    public static final String INTENT_SMS_EVENTS_CHANGED           = "eu.power_switch.sms_events_changed";
    public static final String INTENT_SMS_EVENT_ACTION_ADDED       = "eu.power_switch.sms_event_action_added";


    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private LocalBroadcastConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}

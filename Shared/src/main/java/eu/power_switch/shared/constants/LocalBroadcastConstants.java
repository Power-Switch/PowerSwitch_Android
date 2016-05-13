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
    public static final String INTENT_CONFIGURATION_DIALOG_CHANGED = "eu.power_switch.configuration_dialog_changed";

    public static final String INTENT_GATEWAY_CHANGED = "eu.power_switch.gateway_changed";
    public static final String INTENT_GATEWAY_SETUP_CHANGED = "eu.power_switch.gateway_setup_changed";
    public static final String INTENT_GATEWAY_SSID_ADDED = "eu.power_switch.gateway_ssid_added";

    public static final String INTENT_RECEIVER_CHANGED = "eu.power_switch.receiver_changed";
    public static final String INTENT_APARTMENT_CHANGED = "eu.power_switch.apartment_changed";
    public static final String INTENT_ROOM_CHANGED = "eu.power_switch.room_changed";
    public static final String INTENT_ROOM_ADDED = "eu.power_switch.room_added";
    public static final String INTENT_SCENE_CHANGED = "eu.power_switch.scene_changed";
    public static final String INTENT_TIMER_CHANGED = "eu.power_switch.timer_changed";
    public static final String INTENT_APARTMENT_GEOFENCE_CHANGED = "eu.power_switch.apartment_geofence_changed";
    public static final String INTENT_CUSTOM_GEOFENCE_CHANGED = "eu.power_switch.custom_geofence_changed";
    public static final String INTENT_BACKUP_CHANGED = "eu.power_switch.backup_changed";
    public static final String INTENT_HISTORY_CHANGED = "eu.power_switch.history_changed";

    public static final String INTENT_SETUP_APARTMENT_CHANGED = "eu.power_switch.setup_apartment_changed";

    public static final String INTENT_SETUP_GEOFENCE_CHANGED = "eu.power_switch.setup_geofence_changed";
    public static final String INTENT_GEOFENCE_LOCATION_CHANGED = "eu.power_switch.geofence_location_changed";
    public static final String INTENT_GEOFENCE_ENTER_ACTION_ADDED = "eu.power_switch.geofence_enter_action_added";
    public static final String INTENT_GEOFENCE_ENTER_ACTIONS_CHANGED = "eu.power_switch.geofence_enter_actions_changed";
    public static final String INTENT_GEOFENCE_EXIT_ACTION_ADDED = "eu.power_switch.geofence_exit_action_added";
    public static final String INTENT_GEOFENCE_EXIT_ACTIONS_CHANGED = "eu.power_switch.geofence_exit_actions_changed";

    public static final String INTENT_BRAND_MODEL_CHANGED = "eu.power_switch.brand_model_changed";
    public static final String INTENT_NAME_ROOM_CHANGED = "eu.power_switch.name_room_changed";
    public static final String INTENT_CHANNEL_DETAILS_CHANGED = "eu.power_switch.channel_details_changed";
    public static final String INTENT_GATEWAY_DETAILS_CHANGED = "eu.power_switch.gateway_details_changed";
    public static final String INTENT_GATEWAY_ADDED = "eu.power_switch.gateway_added";

    public static final String INTENT_ROOM_NAME_CHANGED = "eu.power_switch.room_name_changed";

    public static final String INTENT_NAME_APARTMENT_CHANGED = "eu.power_switch.name_apartment_changed";

    public static final String INTENT_RECEIVER_SUMMARY_CHANGED = "eu.power_switch.receiver_summary_changed";
    public static final String INTENT_NAME_SCENE_CHANGED = "eu.power_switch.name_scene_changed";

    public static final String INTENT_SETUP_SCENE_CHANGED = "eu.power_switch.setup_scene_changed";
    public static final String INTENT_TIMER_NAME_EXECUTION_TIME_CHANGED =
            "eu.power_switch.timer_name_execution_time_changed";
    public static final String INTENT_TIMER_EXECUTION_INTERVAL_CHANGED =
            "eu.power_switch.timer_execution_interval_changed";
    public static final String INTENT_TIMER_ACTIONS_CHANGED = "eu.power_switch.timer_actions_changed";
    public static final String INTENT_TIMER_SUMMARY_CHANGED = "eu.power_switch.timer_summary_changed";

    public static final String INTENT_TIMER_ACTION_ADDED = "eu.power_switch.timer_action_added";
    public static final String INTENT_ALARM_EVENT_ACTION_ADDED = "eu.power_switch.alarm_event_action_added";

    public static final String INTENT_CALL_EVENTS_CHANGED = "eu.power_switch.call_events_changed";
    public static final String INTENT_CALL_EVENT_PHONE_NUMBER_ADDED = "eu.power_switch.call_event_phone_number_added";
    public static final String INTENT_CALL_EVENT_PHONE_NUMBERS_CHANGED = "eu.power_switch.call_event_phone_numbers_changed";
    public static final String INTENT_CALL_EVENT_ACTION_ADDED = "eu.power_switch.call_event_action_added";
    public static final String INTENT_CALL_EVENT_ACTIONS_CHANGED = "eu.power_switch.call_event_actions_changed";

    public static final String INTENT_SMS_EVENTS_CHANGED = "eu.power_switch.sms_events_changed";
    public static final String INTENT_SMS_EVENT_PHONE_NUMBER_ADDED = "eu.power_switch.sms_event_phone_number_added";
    public static final String INTENT_SMS_EVENT_ACTION_ADDED = "eu.power_switch.sms_event_action_added";


    public static final String INTENT_PERMISSION_CHANGED = "eu.power_switch.permission_changed";


    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private LocalBroadcastConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}

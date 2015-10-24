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

package eu.power_switch.shared;

/**
 * Class holding constants for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 04.06.2015.
 */
public class Constants {

    // SharedPreferences
    public static final String SHARED_PREFS_NAME = "eu.power_switch.prefs";

    public static final int DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK = 75;

    // Theme
    public static final int THEME_DARK_BLUE = 0;
    public static final int THEME_DARK_RED = 1;
    public static final int THEME_LIGHT_BLUE = 2;
    public static final int THEME_LIGHT_RED = 3;


    // LocalBroadcasts
    public static final String INTENT_GATEWAY_CHANGED = "eu.power_switch.gateway_changed";
    public static final String INTENT_RECEIVER_CHANGED = "eu.power_switch.receiver_changed";
    public static final String INTENT_ROOM_CHANGED = "eu.power_switch.room_changed";
    public static final String INTENT_SCENE_CHANGED = "eu.power_switch.scene_changed";
    public static final String INTENT_TIMER_CHANGED = "eu.power_switch.timer_changed";
    public static final String INTENT_BACKUP_CHANGED = "eu.power_switch.backup_changed";
    public static final String INTENT_STATUS_UPDATE_SNACKBAR = "eu.power_switch.status_update.snackbar";
    public static final String INTENT_STATUS_UPDATE_TOAST = "eu.power_switch.status_update.toast";

    public static final String INTENT_BRAND_MODEL_CHANGED = "eu.power_switch.brand_model_changed";
    public static final String INTENT_NAME_ROOM_CHANGED = "eu.power_switch.name_room_changed";
    public static final String INTENT_CHANNEL_DETAILS_CHANGED = "eu.power_switch.channel_details_changed";
    public static final String INTENT_RECEIVER_SUMMARY_CHANGED = "eu.power_switch.receiver_summary_changed";

    public static final String INTENT_NAME_SCENE_CHANGED = "eu.power_switch.name_scene_changed";
    public static final String INTENT_SETUP_SCENE_CHANGED = "eu.power_switch.setup_scene_changed";

    public static final String INTENT_TIMER_NAME_EXECUTION_TIME_CHANGED = "eu.power_switch" +
            ".timer_name_execution_time_changed";
    public static final String INTENT_TIMER_EXECUTION_INTERVAL_CHANGED = "eu.power_switch" +
            ".timer_execution_interval_changed";
    public static final String INTENT_TIMER_ACTIONS_CHANGED = "eu.power_switch.timer_actions_changed";
    public static final String INTENT_TIMER_SUMMARY_CHANGED = "eu.power_switch.timer_summary_changed";
    public static final String INTENT_TIMER_ACTION_ADDED = "eu.power_switch.timer_action_added";

    // GoogleApi
    public static final int GOOGLE_API_CLIENT_TIMEOUT = 10;
    // Wear DataApi
    public static final String START_ACTIVITY_PATH = "/start_activity";
    public static final String RECEIVER_ACTION_TRIGGER_PATH = "/receiver_action_trigger";
    public static final String REQUEST_DATA_UPDATE_PATH = "/request_data_update";
    public static final String DATA_PATH = "/data";
    public static final String EXTRA_DATA = "extra_data";
    // DataMap Keys
    public static final String ROOM_ID_DATAMAP_KEY = "ROOM_ID_DATAMAP_KEY";
    public static final String ROOM_NAME_DATAMAP_KEY = "ROOM_NAME_DATAMAP_KEY";
    public static final String RECEIVER_ID_DATAMAP_KEY = "RECEIVER_ID_DATAMAP_KEY";
    public static final String RECEIVER_NAME_DATAMAP_KEY = "RECEIVER_NAME_DATAMAP_KEY";
    public static final String RECEIVER_ROOM_ID_DATAMAP_KEY = "RECEIVER_ROOM_ID_DATAMAP_KEY";
    public static final String BUTTON_ID_DATAMAP_KEY = "BUTTON_ID_DATAMAP_KEY";
    public static final String BUTTON_NAME_DATAMAP_KEY = "BUTTON_NAME_DATAMAP_KEY";
    public static final String BUTTON_RECEIVER_ID_DATAMAP_KEY = "BUTTON_RECEIVER_ID_DATAMAP_KEY";
    public static final String SCENE_ID_DATAMAP_KEY = "SCENE_ID_DATAMAP_KEY";
    public static final String SCENE_NAME_DATAMAP_KEY = "SCENE_NAME_DATAMAP_KEY";

    // Api
    public static final String UNIVERSAL_ACTION_INTENT = "eu.power_switch.action";

    // Timer
    public static final String TIMER_ACTIVATION_INTENT = "eu.power_switch.alarm";
    public static final String TIMER_URI_SCHEME = "timer";

    // Sleep As Android
    public static final String ALARM_TRIGGERED_INTENT = "com.urbandroid.sleep.alarmclock.ALARM_ALERT_START";
    public static final String ALARM_SNOOZED_INTENT = "com.urbandroid.sleep.alarmclock.ALARM_SNOOZE_CLICKED_ACTION";
    public static final String ALARM_DISMISSED_INTENT = "com.urbandroid.sleep.alarmclock.ALARM_ALERT_DISMISS";

    /**
     * Private Constructor
     */
    private Constants() {
    }
}
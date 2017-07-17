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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class holding constants related to Wearable app and communication for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 11.11.2015.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WearableConstants {

    public static final String ANDROID_WEAR_PACKAGE_NAME = "com.google.android.wearable.app";

    // Wear DataApi
    public static final String START_ACTIVITY_PATH          = "/start_activity";
    public static final String RECEIVER_ACTION_TRIGGER_PATH = "/receiver_action_trigger";
    public static final String REQUEST_DATA_UPDATE_PATH     = "/request_data_update";
    public static final String REQUEST_SETTINGS_UPDATE_PATH = "/request_settings_update";
    public static final String DATA_PATH                    = "/data";
    public static final String EXTRA_DATA                   = "extra_data";
    public static final String SETTINGS_PATH                = "/settings";
    public static final String EXTRA_SETTINGS               = "extra_settings";


    // DataMap Keys
    public static final String DATAMAP_KEY_APARTMENT_ID   = "DATAMAP_KEY_APARTMENT_ID";
    public static final String DATAMAP_KEY_APARTMENT_NAME = "DATAMAP_KEY_APARTMENT_NAME";

    public static final String DATAMAP_KEY_ROOM_ID           = "DATAMAP_KEY_ROOM_ID";
    public static final String DATAMAP_KEY_ROOM_NAME         = "DATAMAP_KEY_ROOM_NAME";
    public static final String DATAMAP_KEY_ROOM_APARTMENT_ID = "DATAMAP_KEY_ROOM_APARTMENT_ID";

    public static final String DATAMAP_KEY_RECEIVER_ID                       = "DATAMAP_KEY_RECEIVER_ID";
    public static final String DATAMAP_KEY_RECEIVER_NAME                     = "DATAMAP_KEY_RECEIVER_NAME";
    public static final String DATAMAP_KEY_RECEIVER_ROOM_ID                  = "DATAMAP_KEY_RECEIVER_ROOM_ID";
    public static final String DATAMAP_KEY_RECEIVER_POSITION_IN_ROOM         = "DATAMAP_KEY_RECEIVER_POSITION_IN_ROOM";
    public static final String DATAMAP_KEY_RECEIVER_LAST_ACTIVATED_BUTTON_ID = "DATAMAP_KEY_RECEIVER_LAST_ACTIVATED_BUTTON_ID";

    public static final String DATAMAP_KEY_BUTTON_ID          = "DATAMAP_KEY_BUTTON_ID";
    public static final String DATAMAP_KEY_BUTTON_NAME        = "DATAMAP_KEY_BUTTON_NAME";
    public static final String DATAMAP_KEY_BUTTON_RECEIVER_ID = "DATAMAP_KEY_BUTTON_RECEIVER_ID";

    public static final String DATAMAP_KEY_SCENE_ID   = "DATAMAP_KEY_SCENE_ID";
    public static final String DATAMAP_KEY_SCENE_NAME = "DATAMAP_KEY_SCENE_NAME";

    // Action Intent Constants
    public static final String KEY_APARTMENT_ID = "[ApartmentId]";
    public static final String KEY_ROOM_ID      = "[RoomId]";
    public static final String KEY_RECEIVER_ID  = "[ReceiverId]";
    public static final String KEY_BUTTON_ID    = "[ButtonId]";
    public static final String KEY_SCENE_ID     = "[SceneId]";

}

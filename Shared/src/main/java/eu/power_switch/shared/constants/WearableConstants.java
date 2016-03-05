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
 * Class holding constants related to Wearable app and communication for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 11.11.2015.
 */
public class WearableConstants {

    // Wear DataApi
    public static final String START_ACTIVITY_PATH = "/start_activity";
    public static final String RECEIVER_ACTION_TRIGGER_PATH = "/receiver_action_trigger";
    public static final String REQUEST_DATA_UPDATE_PATH = "/request_data_update";
    public static final String REQUEST_SETTINGS_UPDATE_PATH = "/request_settings_update";
    public static final String DATA_PATH = "/data";
    public static final String EXTRA_DATA = "extra_data";
    public static final String SETTINGS_PATH = "/settings";
    public static final String EXTRA_SETTINGS = "extra_settings";


    // DataMap Keys
    public static final String ROOM_ID_DATAMAP_KEY = "ROOM_ID_DATAMAP_KEY";
    public static final String ROOM_NAME_DATAMAP_KEY = "ROOM_NAME_DATAMAP_KEY";

    public static final String RECEIVER_ID_DATAMAP_KEY = "RECEIVER_ID_DATAMAP_KEY";
    public static final String RECEIVER_NAME_DATAMAP_KEY = "RECEIVER_NAME_DATAMAP_KEY";
    public static final String RECEIVER_ROOM_ID_DATAMAP_KEY = "RECEIVER_ROOM_ID_DATAMAP_KEY";
    public static final String RECEIVER_POSITION_IN_ROOM_DATAMAP_KEY = "RECEIVER_POSITION_IN_ROOM_DATAMAP_KEY";
    public static final String RECEIVER_LAST_ACTIVATED_BUTTON_ID_DATAMAP_KEY = "RECEIVER_LAST_ACTIVATED_BUTTON_ID_DATAMAP_KEY";

    public static final String BUTTON_ID_DATAMAP_KEY = "BUTTON_ID_DATAMAP_KEY";
    public static final String BUTTON_NAME_DATAMAP_KEY = "BUTTON_NAME_DATAMAP_KEY";
    public static final String BUTTON_RECEIVER_ID_DATAMAP_KEY = "BUTTON_RECEIVER_ID_DATAMAP_KEY";

    public static final String SCENE_ID_DATAMAP_KEY = "SCENE_ID_DATAMAP_KEY";
    public static final String SCENE_NAME_DATAMAP_KEY = "SCENE_NAME_DATAMAP_KEY";

    // Action Intent Constants
    public static final String APARTMENT_ID_KEY = "[ApartmentId]";
    public static final String ROOM_ID_KEY = "[RoomId]";
    public static final String RECEIVER_ID_KEY = "[ReceiverId]";
    public static final String BUTTON_ID_KEY = "[ButtonId]";
    public static final String SCENE_ID_KEY = "[SceneId]";

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private WearableConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}

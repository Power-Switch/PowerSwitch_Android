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
 * Class holding constants related to Wearable app settings
 * <p/>
 * Created by Markus on 13.11.2015.
 */
public class WearableSettingsConstants {

    // SharedPreferences
    public static final String WEARABLE_SHARED_PREFS_NAME = "eu.power_switch.wearable.prefs";

    // app settings
    public static final String SHOW_ROOM_ALL_ON_OFF_KEY = "showRoomAllOnOff";
    public static final String HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY = "highlightLastActivatedButton";
    public static final String AUTO_COLLAPSE_ROOMS_KEY = "autoCollapseRooms";
    public static final String THEME_KEY = "theme";
    public static final String VIBRATE_ON_BUTTON_PRESS_KEY = "vibrateOnButtonPress";
    public static final String VIBRATION_DURATION_KEY = "vibrationDuration";

    public static final String WEARABLE_SETTINGS_CHANGED = "WEARABLE_SETTINGS_CHANGED";

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private WearableSettingsConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}

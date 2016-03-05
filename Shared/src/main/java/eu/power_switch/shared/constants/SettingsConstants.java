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
 * Class holding constants related to Settings for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 11.11.2015.
 */
public class SettingsConstants {

    // SharedPreferences
    public static final String SHARED_PREFS_NAME = "eu.power_switch.prefs";

    // app settings
    public static final String AUTO_DISCOVER_KEY = "autoDiscover";
    public static final String BACKUP_PATH_KEY = "backupPath";
    public static final String STARTUP_DEFAULT_TAB_KEY = "startupDefaultTab";
    public static final String SHOW_ROOM_ALL_ON_OFF_KEY = "showRoomAllOnOff";
    public static final String HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY = "highlightLastActivatedButton";
    public static final String HIDE_ADD_FAB_KEY = "hideAddFAB";
    public static final String AUTO_COLLAPSE_ROOMS_KEY = "autoCollapseRooms";
    public static final String AUTO_COLLAPSE_TIMERS_KEY = "autoCollapseTimers";
    public static final String THEME_KEY = "theme";
    public static final String USE_COMPACT_DRAWER_KEY = "useCompactDrawer";
    public static final String VIBRATE_ON_BUTTON_PRESS_KEY = "vibrateOnButtonPress";
    public static final String VIBRATION_DURATION_KEY = "vibrationDuration";
    public static final String CURRENT_APARTMENT_ID_KEY = "currentApartmentId";

    public static final int DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK = 40;

    public static final long INVALID_APARTMENT_ID = -1;

    // Main Tabs
    public static final int ROOMS_TAB_INDEX = 0;
    public static final int SCENES_TAB_INDEX = 1;

    // Settings Tabs
    public static final int GENERAL_SETTINGS_TAB_INDEX = 0;
    public static final int GATEWAYS_TAB_INDEX = 1;
    public static final int WEARABLE_TAB_INDEX = 2;


    // Theme
    public static final int THEME_DARK_BLUE = 0;
    public static final int THEME_DARK_RED = 1;
    public static final int THEME_LIGHT_BLUE = 2;
    public static final int THEME_LIGHT_RED = 3;

    // Google API
    public static final String VOK_ZWEQ =
            "jVMU2RnWW1oelVoMVF4ZXJkV1B1cDcwWFRYc3g0c0hScmN2WjhmR21NN3R0V3E5YlF4cWtVSUFiYjFUQ0EzcW9ScU00bUxNY0E0T29ZVjVsTE1hSGxXanVyVHdJREFRQUI=";

    public static final String KDH_SDSA =
            "TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF3UDlkSk9QYVZQNnBHZ1kxeUYzRVBVVHRRbkJMaHVwN2xYVnNyTzAyMFdXZlp4YmFSRnQ5c1I";

    public static final String DJA_IOVJ =
            "VvWE9iYnB2NTNJMmJVeEFkSkZyUm9pWVNaa3BQV1hXb201dVN4UHdSQ2x5cVdDZXlmeFZTYlN6NGdSNFAwOVlyODlIMXFzNFBQdHRIZ2k1cDNsd2FVT2pwNzlGSVFZb1pmZ";

    public static final String JKD_COAP =
            "0K0RzZDlHS3EvbjYyLzMySFFydkJXcVVQK1FrOE1FNDUvYWM2UTh2YmNtdmlCV0h1T3hUSVB2d1RucU5mdzNpMjJXd1VTZVV0WHRReURLVVpZODJYVjJwY0ZoSGkydnpmWW";

    public static final int GOOGLE_API_CLIENT_TIMEOUT = 10;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private SettingsConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}

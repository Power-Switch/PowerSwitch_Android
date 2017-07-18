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

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class holding constants related to Settings for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 11.11.2015.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SettingsConstants {

    // SharedPreferences
    public static final String SHARED_PREFS_NAME = "eu.power_switch.prefs";

    public static final int DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK = 40;

    public static final long INVALID_APARTMENT_ID = -1;

    // Main Tabs
    public static final int ROOMS_TAB_INDEX  = 0;
    public static final int SCENES_TAB_INDEX = 1;

    // Settings Tabs
    public static final int GENERAL_SETTINGS_TAB_INDEX = 0;
    public static final int GATEWAYS_TAB_INDEX         = 1;
    public static final int WEARABLE_TAB_INDEX         = 2;

    // Keep History items
    public static final int KEEP_HISTORY_FOREVER  = 0;
    public static final int KEEP_HISTORY_1_YEAR   = 1;
    public static final int KEEP_HISTORY_6_MONTHS = 2;
    public static final int KEEP_HISTORY_1_MONTH  = 3;
    public static final int KEEP_HISTORY_14_DAYS  = 4;

    // Theme
    public static final int THEME_DARK_BLUE      = 0;
    public static final int THEME_DARK_RED       = 1;
    public static final int THEME_LIGHT_BLUE     = 2;
    public static final int THEME_LIGHT_RED      = 3;
    public static final int THEME_DAY_NIGHT_BLUE = 4;

    // Google API
    public static final String VOK_ZWEQ                  = "jVMU2RnWW1oelVoMVF4ZXJkV1B1cDcwWFRYc3g0c0hScmN2WjhmR21NN3R0V3E5YlF4cWtVSUFiYjFUQ0EzcW9ScU00bUxNY0E0T29ZVjVsTE1hSGxXanVyVHdJREFRQUI=";
    public static final String KDH_SDSA                  = "TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF3UDlkSk9QYVZQNnBHZ1kxeUYzRVBVVHRRbkJMaHVwN2xYVnNyTzAyMFdXZlp4YmFSRnQ5c1I";
    public static final String DJA_IOVJ                  = "VvWE9iYnB2NTNJMmJVeEFkSkZyUm9pWVNaa3BQV1hXb201dVN4UHdSQ2x5cVdDZXlmeFZTYlN6NGdSNFAwOVlyODlIMXFzNFBQdHRIZ2k1cDNsd2FVT2pwNzlGSVFZb1pmZ";
    public static final String JKD_COAP                  = "0K0RzZDlHS3EvbjYyLzMySFFydkJXcVVQK1FrOE1FNDUvYWM2UTh2YmNtdmlCV0h1T3hUSVB2d1RucU5mdzNpMjJXd1VTZVV0WHRReURLVVpZODJYVjJwY0ZoSGkydnpmWW";
    public static final int    GOOGLE_API_CLIENT_TIMEOUT = 10;

    // Theme
    @IntDef({THEME_DARK_BLUE, THEME_DARK_RED, THEME_LIGHT_BLUE, THEME_LIGHT_RED, THEME_DAY_NIGHT_BLUE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Theme {
    }
}

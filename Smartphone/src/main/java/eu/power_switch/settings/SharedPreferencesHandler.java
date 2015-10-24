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

package eu.power_switch.settings;

import android.content.Context;
import android.content.SharedPreferences;

import eu.power_switch.log.Log;
import eu.power_switch.shared.Constants;

/**
 * Preference handler used to store general app settings
 */
public class SharedPreferencesHandler {

    // app settings
    private static final String AUTO_DISCOVER_KEY = "autoDiscover";
    private static final String SHOW_ROOM_ALL_ON_OFF_KEY = "showRoomAllOnOff";
    private static final String HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY = "highlightLastActivatedButton";
    private static final String HIDE_ADD_FAB_KEY = "hideAddFAB";
    private static final String AUTO_COLLAPSE_ROOMS_KEY = "autoCollapseRooms";
    private static final String THEME_KEY = "theme";
    private static final String VIBRATE_ON_BUTTON_PRESS_KEY = "vibrateOnButtonPress";
    private static final String VIBRATION_DURATION_KEY = "vibrationDuration";

    // Google API
    private static final String VOK_ZWEQ =
            "jVMU2RnWW1oelVoMVF4ZXJkV1B1cDcwWFRYc3g0c0hScmN2WjhmR21NN3R0V3E5YlF4cWtVSUFiYjFUQ0EzcW9ScU00bUxNY0E0T29ZVjVsTE1hSGxXanVyVHdJREFRQUI=";

    private static final String KDH_SDSA =
            "TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF3UDlkSk9QYVZQNnBHZ1kxeUYzRVBVVHRRbkJMaHVwN2xYVnNyTzAyMFdXZlp4YmFSRnQ5c1I";

    private static final String DJA_IOVJ =
            "VvWE9iYnB2NTNJMmJVeEFkSkZyUm9pWVNaa3BQV1hXb201dVN4UHdSQ2x5cVdDZXlmeFZTYlN6NGdSNFAwOVlyODlIMXFzNFBQdHRIZ2k1cDNsd2FVT2pwNzlGSVFZb1pmZ";

    private static final String JKD_COAP =
            "0K0RzZDlHS3EvbjYyLzMySFFydkJXcVVQK1FrOE1FNDUvYWM2UTh2YmNtdmlCV0h1T3hUSVB2d1RucU5mdzNpMjJXd1VTZVV0WHRReURLVVpZODJYVjJwY0ZoSGkydnpmWW";

    // developer settings
    private final String PLAY_STORE_MODE_KEY = "playStoreMode";

    SharedPreferences sharedPreferences;

    public SharedPreferencesHandler(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getPublicKeyString() {
        String string = KDH_SDSA + JKD_COAP + DJA_IOVJ + VOK_ZWEQ;
        return string;
    }

    /**
     * Retrieves setting for AutoDiscovery of Gateways
     *
     * @return true if enabled
     */
    public boolean getAutoDiscover() {
        boolean value = sharedPreferences.getBoolean(AUTO_DISCOVER_KEY, true);
        Log.d(this, "getAutoDiscover: " + value);
        return value;
    }

    /**
     * Sets setting for AutoDiscovery of Gateways
     *
     * @param bool true if enabled
     */
    public void setAutoDiscover(boolean bool) {
        Log.d(this, "setAutoDiscover: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(AUTO_DISCOVER_KEY, bool);
        editor.apply();
    }

    public boolean getShowRoomAllOnOff() {
        boolean value = sharedPreferences.getBoolean(SHOW_ROOM_ALL_ON_OFF_KEY, true);
        Log.d(this, "getShowRoomAllOnOff: " + value);
        return value;
    }

    /**
     * Sets setting for automatic collapsing of Rooms
     *
     * @param bool true if enabled
     */
    public void setShowRoomAllOnOff(boolean bool) {
        Log.d(this, "setShowRoomAllOnOff: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHOW_ROOM_ALL_ON_OFF_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for hidden Play Store Mode (used to take Screenshots)
     *
     * @return true if enabled
     */
    public boolean getPlayStoreMode() {
        boolean value = sharedPreferences.getBoolean(PLAY_STORE_MODE_KEY, false);
        Log.d(this, "getPlayStoreMode: " + value);
        return value;
    }

    /**
     * Sets setting for hidden Play Store Mode (used to take Screenshots)
     *
     * @param bool true if enabled
     */
    public void setPlayStoreMode(boolean bool) {
        Log.d(this, "setPlayStoreMode: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PLAY_STORE_MODE_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for automatic collapsing of Rooms
     *
     * @return true if enabled
     */
    public boolean getAutoCollapseRooms() {
        boolean value = sharedPreferences.getBoolean(AUTO_COLLAPSE_ROOMS_KEY, false);
        Log.d(this, "getAutoCollapseRooms: " + value);
        return value;
    }

    /**
     * Sets setting for automatic collapsing of Rooms
     *
     * @param bool true if enabled
     */
    public void setAutoCollapseRooms(boolean bool) {
        Log.d(this, "setAutoCollapseRooms: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(AUTO_COLLAPSE_ROOMS_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for current Theme
     *
     * @return ID (internal) of Theme
     */
    public int getTheme() {
        int value = sharedPreferences.getInt(THEME_KEY, Constants.THEME_DARK_BLUE);
        Log.d(this, "getTheme: " + value);
        return value;
    }

    /**
     * Sets setting for current Theme
     *
     * @param theme ID (internal) of Theme
     */
    public void setTheme(int theme) {
        Log.d(this, "setTheme: " + theme);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(THEME_KEY, theme);
        editor.apply();
    }

    /**
     * Retrieves setting for vibration feedback
     *
     * @return true if enabled
     */
    public boolean getVibrateOnButtonPress() {
        boolean value = sharedPreferences.getBoolean(VIBRATE_ON_BUTTON_PRESS_KEY, true);
        Log.d(this, "getVibrateOnButtonPress: " + value);
        return value;
    }

    /**
     * Sets setting for vibration feedback
     *
     * @param bool true if enabled
     */
    public void setVibrateOnButtonPress(boolean bool) {
        Log.d(this, "setVibrateOnButtonPress: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VIBRATE_ON_BUTTON_PRESS_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for vibration feedback duration
     *
     * @return time in ms
     */
    public int getVibrationDuration() {
        int value = sharedPreferences.getInt(VIBRATION_DURATION_KEY, Constants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK);
        Log.d(this, "getVibrationDuration: " + value);
        return value;
    }

    /**
     * Sets setting for vibration feedback duration
     *
     * @param milliseconds time in ms
     */
    public void setVibrationDuration(int milliseconds) {
        Log.d(this, "setVibrationDuration: " + milliseconds);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(VIBRATION_DURATION_KEY, milliseconds);
        editor.apply();
    }

    /**
     * Retrieves setting for highlighting last activated button
     *
     * @return true if enabled
     */
    public boolean getHighlightLastActivatedButton() {
        boolean value = sharedPreferences.getBoolean(HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, false);
        Log.d(this, "getHighlightLastActivatedButton: " + value);
        return value;
    }

    /**
     * Sets setting for highlighting last activated button
     *
     * @param bool true if enabled
     */
    public void setHighlightLastActivatedButton(boolean bool) {
        Log.d(this, "setHighlightLastActivatedButton: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for hiding FAB buttons
     *
     * @return true if enabled
     */
    public boolean getHideAddFAB() {
        boolean value = sharedPreferences.getBoolean(HIDE_ADD_FAB_KEY, false);
        Log.d(this, "getHideAddFAB: " + value);
        return value;
    }

    /**
     * Retrieves setting for hiding FAB buttons
     *
     * @param bool true if enabled
     */
    public void setHideAddFAB(boolean bool) {
        Log.d(this, "setHideAddFAB: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HIDE_ADD_FAB_KEY, bool);
        editor.apply();
    }
}

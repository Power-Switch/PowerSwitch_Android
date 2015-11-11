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
import eu.power_switch.shared.constants.SettingsConstants;

/**
 * Preference handler used to store general app settings
 */
public class SharedPreferencesHandler {

    SharedPreferences sharedPreferences;

    public SharedPreferencesHandler(Context context) {
        sharedPreferences = context.getSharedPreferences(SettingsConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getPublicKeyString() {
        String string = SettingsConstants.KDH_SDSA + SettingsConstants.JKD_COAP + SettingsConstants.DJA_IOVJ + SettingsConstants.VOK_ZWEQ;
        return string;
    }

    /**
     * Retrieves setting for AutoDiscovery of Gateways
     *
     * @return true if enabled
     */
    public boolean getAutoDiscover() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.AUTO_DISCOVER_KEY, true);
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
        editor.putBoolean(SettingsConstants.AUTO_DISCOVER_KEY, bool);
        editor.apply();
    }

    public boolean getShowRoomAllOnOff() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, true);
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
        editor.putBoolean(SettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for hidden Play Store Mode (used to take Screenshots)
     *
     * @return true if enabled
     */
    public boolean getPlayStoreMode() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.PLAY_STORE_MODE_KEY, false);
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
        editor.putBoolean(SettingsConstants.PLAY_STORE_MODE_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for automatic collapsing of Rooms
     *
     * @return true if enabled
     */
    public boolean getAutoCollapseRooms() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.AUTO_COLLAPSE_ROOMS_KEY, false);
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
        editor.putBoolean(SettingsConstants.AUTO_COLLAPSE_ROOMS_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for automatic collapsing of Timers
     *
     * @return true if enabled
     */
    public boolean getAutoCollapseTimers() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.AUTO_COLLAPSE_TIMERS_KEY, false);
        Log.d(this, "getAutoCollapseTimers: " + value);
        return value;
    }

    /**
     * Sets setting for automatic collapsing of Timers
     *
     * @param bool true if enabled
     */
    public void setAutoCollapseTimers(boolean bool) {
        Log.d(this, "setAutoCollapseTimers: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.AUTO_COLLAPSE_TIMERS_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for current Theme
     *
     * @return ID (internal) of Theme
     */
    public int getTheme() {
        int value = sharedPreferences.getInt(SettingsConstants.THEME_KEY, SettingsConstants.THEME_DARK_BLUE);
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
        editor.putInt(SettingsConstants.THEME_KEY, theme);
        editor.apply();
    }

    /**
     * Retrieves setting for vibration feedback
     *
     * @return true if enabled
     */
    public boolean getVibrateOnButtonPress() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, true);
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
        editor.putBoolean(SettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for vibration feedback duration
     *
     * @return time in ms
     */
    public int getVibrationDuration() {
        int value = sharedPreferences.getInt(SettingsConstants.VIBRATION_DURATION_KEY, SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK);
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
        editor.putInt(SettingsConstants.VIBRATION_DURATION_KEY, milliseconds);
        editor.apply();
    }

    /**
     * Retrieves setting for highlighting last activated button
     *
     * @return true if enabled
     */
    public boolean getHighlightLastActivatedButton() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, false);
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
        editor.putBoolean(SettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for hiding FAB buttons
     *
     * @return true if enabled
     */
    public boolean getHideAddFAB() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.HIDE_ADD_FAB_KEY, false);
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
        editor.putBoolean(SettingsConstants.HIDE_ADD_FAB_KEY, bool);
        editor.apply();
    }
}

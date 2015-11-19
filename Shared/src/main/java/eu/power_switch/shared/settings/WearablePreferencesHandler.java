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

package eu.power_switch.shared.settings;

import android.content.Context;
import android.content.SharedPreferences;

import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.log.Log;

/**
 * This class is responsible for accessing and modifying Wear App Settings
 * <p/>
 * Note: Most (if not all) Settings can not be changed on the Wearable itself but only using the Smartphone
 * <p/>
 * <p/>
 * Created by Markus on 13.11.2015.
 */
public class WearablePreferencesHandler {

    SharedPreferences sharedPreferences;

    public WearablePreferencesHandler(Context context) {
        sharedPreferences = context.getSharedPreferences(WearableConstants.WEARABLE_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Retrieves setting for Room On/Off Buttons
     *
     * @return true if enabled
     */
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

}

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
import eu.power_switch.shared.constants.WearableSettingsConstants;
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

    private boolean showRoomAllOnOffCache;
    private boolean autoCollapseRoomsCache;
    private int themeCache;
    private boolean vibrateOnButtonPressCache;
    private int vibrationDurationCache;
    private boolean highlightLastActivatedButtonCache;

    public WearablePreferencesHandler(Context context) {
        sharedPreferences = context.getSharedPreferences(WearableSettingsConstants.WEARABLE_SHARED_PREFS_NAME,
                Context.MODE_PRIVATE);

        initCache();
    }

    private void initCache() {
        showRoomAllOnOffCache = sharedPreferences.getBoolean(WearableSettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, true);
        autoCollapseRoomsCache = sharedPreferences.getBoolean(WearableSettingsConstants.AUTO_COLLAPSE_ROOMS_KEY, false);
        themeCache = sharedPreferences.getInt(WearableSettingsConstants.THEME_KEY, SettingsConstants.THEME_DARK_BLUE);
        vibrateOnButtonPressCache = sharedPreferences.getBoolean(WearableSettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, true);
        vibrationDurationCache = sharedPreferences.getInt(WearableSettingsConstants.VIBRATION_DURATION_KEY, SettingsConstants
                .DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK);
        highlightLastActivatedButtonCache = sharedPreferences.getBoolean(WearableSettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, false);
    }

    /**
     * Retrieves setting for Room On/Off Buttons
     *
     * @return true if enabled
     */
    public boolean getShowRoomAllOnOff() {
        Log.d(this, "getShowRoomAllOnOff: " + showRoomAllOnOffCache);
        return showRoomAllOnOffCache;
    }

    /**
     * Sets setting for automatic collapsing of Rooms
     *
     * @param bool true if enabled
     */
    public void setShowRoomAllOnOff(boolean bool) {
        Log.d(this, "setShowRoomAllOnOff: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(WearableSettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, bool);
        editor.apply();

        showRoomAllOnOffCache = bool;
    }

    /**
     * Retrieves setting for automatic collapsing of Rooms
     *
     * @return true if enabled
     */
    public boolean getAutoCollapseRooms() {
        Log.d(this, "getAutoCollapseRooms: " + autoCollapseRoomsCache);
        return autoCollapseRoomsCache;
    }

    /**
     * Sets setting for automatic collapsing of Rooms
     *
     * @param bool true if enabled
     */
    public void setAutoCollapseRooms(boolean bool) {
        Log.d(this, "setAutoCollapseRooms: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(WearableSettingsConstants.AUTO_COLLAPSE_ROOMS_KEY, bool);
        editor.apply();

        autoCollapseRoomsCache = bool;
    }

    /**
     * Retrieves setting for current Theme
     *
     * @return ID (internal) of Theme
     */
    public int getTheme() {
        Log.d(this, "getTheme: " + themeCache);
        return themeCache;
    }

    /**
     * Sets setting for current Theme
     *
     * @param theme ID (internal) of Theme
     */
    public void setTheme(int theme) {
        Log.d(this, "setTheme: " + theme);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(WearableSettingsConstants.THEME_KEY, theme);
        editor.apply();

        themeCache = theme;
    }

    /**
     * Retrieves setting for vibration feedback
     *
     * @return true if enabled
     */
    public boolean getVibrateOnButtonPress() {
        Log.d(this, "getVibrateOnButtonPress: " + vibrateOnButtonPressCache);
        return vibrateOnButtonPressCache;
    }

    /**
     * Sets setting for vibration feedback
     *
     * @param bool true if enabled
     */
    public void setVibrateOnButtonPress(boolean bool) {
        Log.d(this, "setVibrateOnButtonPress: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(WearableSettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, bool);
        editor.apply();

        vibrateOnButtonPressCache = bool;
    }

    /**
     * Retrieves setting for vibration feedback duration
     *
     * @return time in ms
     */
    public int getVibrationDuration() {
        Log.d(this, "getVibrationDuration: " + vibrationDurationCache);
        return vibrationDurationCache;
    }

    /**
     * Sets setting for vibration feedback duration
     *
     * @param milliseconds time in ms
     */
    public void setVibrationDuration(int milliseconds) {
        Log.d(this, "setVibrationDuration: " + milliseconds);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(WearableSettingsConstants.VIBRATION_DURATION_KEY, milliseconds);
        editor.apply();

        vibrationDurationCache = milliseconds;
    }

    /**
     * Retrieves setting for highlighting last activated button
     *
     * @return true if enabled
     */
    public boolean getHighlightLastActivatedButton() {
        Log.d(this, "getHighlightLastActivatedButton: " + highlightLastActivatedButtonCache);
        return highlightLastActivatedButtonCache;
    }

    /**
     * Sets setting for highlighting last activated button
     *
     * @param bool true if enabled
     */
    public void setHighlightLastActivatedButton(boolean bool) {
        Log.d(this, "setHighlightLastActivatedButton: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(WearableSettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, bool);
        editor.apply();

        highlightLastActivatedButtonCache = bool;
    }

}

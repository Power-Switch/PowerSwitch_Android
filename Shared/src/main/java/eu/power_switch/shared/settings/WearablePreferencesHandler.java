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
import android.support.annotation.NonNull;

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

    private static SharedPreferences sharedPreferences;

    // cached values
    private static boolean showRoomAllOnOffCache;
    private static boolean autoCollapseRoomsCache;
    private static int themeCache;
    private static boolean vibrateOnButtonPressCache;
    private static int vibrationDurationCache;
    private static boolean highlightLastActivatedButtonCache;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private WearablePreferencesHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable. Use static one time initialization vie init() method instead.");
    }

    /**
     * One time initialization of the PreferenceHandler
     *
     * @param context any suitable context
     */
    public static void init(@NonNull Context context) {
        if (sharedPreferences != null) {
            forceRefresh();
        } else {
            sharedPreferences = context.getSharedPreferences(
                    WearableSettingsConstants.WEARABLE_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            initCache();
        }
    }

    /**
     * First time initialization of cached values
     */
    private static void initCache() {
        showRoomAllOnOffCache = sharedPreferences.getBoolean(WearableSettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, true);
        autoCollapseRoomsCache = sharedPreferences.getBoolean(WearableSettingsConstants.AUTO_COLLAPSE_ROOMS_KEY, false);
        themeCache = sharedPreferences.getInt(WearableSettingsConstants.THEME_KEY, SettingsConstants.THEME_DARK_BLUE);
        vibrateOnButtonPressCache = sharedPreferences.getBoolean(WearableSettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, true);
        vibrationDurationCache = sharedPreferences.getInt(WearableSettingsConstants.VIBRATION_DURATION_KEY, SettingsConstants
                .DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK);
        highlightLastActivatedButtonCache = sharedPreferences.getBoolean(WearableSettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, false);

        Log.d(WearablePreferencesHandler.class, "getShowRoomAllOnOff: " + showRoomAllOnOffCache);
        Log.d(WearablePreferencesHandler.class, "getAutoCollapseRooms: " + autoCollapseRoomsCache);
        Log.d(WearablePreferencesHandler.class, "getTheme: " + themeCache);
        Log.d(WearablePreferencesHandler.class, "getVibrateOnButtonPress: " + vibrateOnButtonPressCache);
        Log.d(WearablePreferencesHandler.class, "getVibrationDuration: " + vibrationDurationCache);
        Log.d(WearablePreferencesHandler.class, "getHighlightLastActivatedButton: " + highlightLastActivatedButtonCache);
    }

    /**
     * Forces an update of the cached values
     */
    public static void forceRefresh() {
        initCache();
    }

    /**
     * Retrieves setting for Room On/Off Buttons
     *
     * @return true if enabled
     */
    public static boolean getShowRoomAllOnOff() {
        return showRoomAllOnOffCache;
    }

    /**
     * Sets setting for automatic collapsing of Rooms
     *
     * @param bool true if enabled
     */
    public static void setShowRoomAllOnOff(boolean bool) {
        Log.d(WearablePreferencesHandler.class, "setShowRoomAllOnOff: " + bool);
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
    public static boolean getAutoCollapseRooms() {
        return autoCollapseRoomsCache;
    }

    /**
     * Sets setting for automatic collapsing of Rooms
     *
     * @param bool true if enabled
     */
    public static void setAutoCollapseRooms(boolean bool) {
        Log.d(WearablePreferencesHandler.class, "setAutoCollapseRooms: " + bool);
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
    public static int getTheme() {
        return themeCache;
    }

    /**
     * Sets setting for current Theme
     *
     * @param theme ID (internal) of Theme
     */
    public static void setTheme(int theme) {
        Log.d(WearablePreferencesHandler.class, "setTheme: " + theme);
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
    public static boolean getVibrateOnButtonPress() {
        return vibrateOnButtonPressCache;
    }

    /**
     * Sets setting for vibration feedback
     *
     * @param bool true if enabled
     */
    public static void setVibrateOnButtonPress(boolean bool) {
        Log.d(WearablePreferencesHandler.class, "setVibrateOnButtonPress: " + bool);
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
    public static int getVibrationDuration() {
        return vibrationDurationCache;
    }

    /**
     * Sets setting for vibration feedback duration
     *
     * @param milliseconds time in ms
     */
    public static void setVibrationDuration(int milliseconds) {
        Log.d(WearablePreferencesHandler.class, "setVibrationDuration: " + milliseconds);
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
    public static boolean getHighlightLastActivatedButton() {
        return highlightLastActivatedButtonCache;
    }

    /**
     * Sets setting for highlighting last activated button
     *
     * @param bool true if enabled
     */
    public static void setHighlightLastActivatedButton(boolean bool) {
        Log.d(WearablePreferencesHandler.class, "setHighlightLastActivatedButton: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(WearableSettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, bool);
        editor.apply();

        highlightLastActivatedButtonCache = bool;
    }

}

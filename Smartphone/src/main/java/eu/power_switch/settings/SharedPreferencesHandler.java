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
import android.os.Environment;

import java.io.File;

import eu.power_switch.backup.BackupHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;

/**
 * Preference handler used to store general app settings
 */
public class SharedPreferencesHandler {

    // cached values
    private boolean autoDiscoverCache;
    private String backupPathCache;
    private boolean showRoomAllOnOffCache;
    private boolean playStoreModeCache;
    private int startupDefaultTabCache;
    private boolean hideAddFABCache;
    private boolean autoCollapseRoomsCache;
    private boolean autoCollapseTimersCache;
    private int themeCache;
    private boolean vibrateOnButtonPressCache;
    private int vibrationDurationCache;
    private boolean highlightLastActivatedButtonCache;


    private SharedPreferences sharedPreferences;

    public SharedPreferencesHandler(Context context) {
        sharedPreferences = context.getSharedPreferences(SettingsConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        initCache();
    }

    public static String getPublicKeyString() {
        String string = SettingsConstants.KDH_SDSA + SettingsConstants.JKD_COAP + SettingsConstants.DJA_IOVJ + SettingsConstants.VOK_ZWEQ;
        return string;
    }

    private void initCache() {
        autoDiscoverCache = sharedPreferences.getBoolean(SettingsConstants.AUTO_DISCOVER_KEY, true);
        backupPathCache = sharedPreferences.getString(SettingsConstants.BACKUP_PATH_KEY,
                Environment.getExternalStorageDirectory()
                        .getPath() + File.separator + BackupHandler.MAIN_BACKUP_FOLDERNAME);
        showRoomAllOnOffCache = sharedPreferences.getBoolean(SettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, true);
        playStoreModeCache = sharedPreferences.getBoolean(SettingsConstants.PLAY_STORE_MODE_KEY, false);
        startupDefaultTabCache = sharedPreferences.getInt(SettingsConstants.STARTUP_DEFAULT_TAB_KEY, SettingsConstants.ROOMS_TAB_INDEX);
        hideAddFABCache = sharedPreferences.getBoolean(SettingsConstants.HIDE_ADD_FAB_KEY, false);
        autoCollapseRoomsCache = sharedPreferences.getBoolean(SettingsConstants.AUTO_COLLAPSE_ROOMS_KEY, false);
        autoCollapseTimersCache = sharedPreferences.getBoolean(SettingsConstants.AUTO_COLLAPSE_TIMERS_KEY, false);
        themeCache = sharedPreferences.getInt(SettingsConstants.THEME_KEY, SettingsConstants.THEME_DARK_BLUE);
        vibrateOnButtonPressCache = sharedPreferences.getBoolean(SettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, true);
        vibrationDurationCache = sharedPreferences.getInt(SettingsConstants.VIBRATION_DURATION_KEY, SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK);
        highlightLastActivatedButtonCache = sharedPreferences.getBoolean(SettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, false);
    }

    /**
     * Forces an update of the cached values
     */
    public void forceRefresh() {
        initCache();
    }

    /**
     * Retrieves setting for AutoDiscovery of Gateways
     *
     * @return true if enabled
     */
    public boolean getAutoDiscover() {
        Log.d(this, "getAutoDiscover: " + autoDiscoverCache);
        return autoDiscoverCache;
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

        autoDiscoverCache = bool;
    }

    /**
     * Retrieves setting for Backup Path
     *
     * @return Backup Path
     */
    public String getBackupPath() {
        Log.d(this, "getBackupPath: " + backupPathCache);
        return backupPathCache;
    }

    /**
     * Sets setting for Backup Path
     *
     * @param path Backup Path
     */
    public void setBackupPath(String path) {
        Log.d(this, "setBackupPath: " + path);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SettingsConstants.BACKUP_PATH_KEY, path);
        editor.apply();

        backupPathCache = path;
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
        editor.putBoolean(SettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, bool);
        editor.apply();

        showRoomAllOnOffCache = bool;
    }

    /**
     * Retrieves setting for hidden Play Store Mode (used to take Screenshots)
     *
     * @return true if enabled
     */
    public boolean getPlayStoreMode() {
        Log.d(this, "getPlayStoreMode: " + playStoreModeCache);
        return playStoreModeCache;
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

        playStoreModeCache = bool;
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
        editor.putBoolean(SettingsConstants.AUTO_COLLAPSE_ROOMS_KEY, bool);
        editor.apply();

        autoCollapseRoomsCache = bool;
    }

    /**
     * Retrieves setting for automatic collapsing of Timers
     *
     * @return true if enabled
     */
    public boolean getAutoCollapseTimers() {
        Log.d(this, "getAutoCollapseTimers: " + autoCollapseTimersCache);
        return autoCollapseTimersCache;
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

        autoCollapseTimersCache = bool;
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
        editor.putInt(SettingsConstants.THEME_KEY, theme);
        editor.apply();

        this.themeCache = theme;
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
        editor.putBoolean(SettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, bool);
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
        editor.putInt(SettingsConstants.VIBRATION_DURATION_KEY, milliseconds);
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
        editor.putBoolean(SettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, bool);
        editor.apply();

        highlightLastActivatedButtonCache = bool;
    }

    /**
     * Retrieves setting for hiding FAB buttons
     *
     * @return true if enabled
     */
    public boolean getHideAddFAB() {
        Log.d(this, "getHideAddFAB: " + hideAddFABCache);
        return hideAddFABCache;
    }

    /**
     * Sets setting for hiding FAB buttons
     *
     * @param bool true if enabled
     */
    public void setHideAddFAB(boolean bool) {
        Log.d(this, "setHideAddFAB: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.HIDE_ADD_FAB_KEY, bool);
        editor.apply();

        hideAddFABCache = bool;
    }

    /**
     * Retrieves setting for startup default tab
     *
     * @return tab index
     */
    public int getStartupDefaultTab() {
        Log.d(this, "getStartupDefaultTab: " + startupDefaultTabCache);
        return startupDefaultTabCache;
    }

    /**
     * Sets setting for startup default tab
     *
     * @param tabIndex index of tab
     */
    public void setStartupDefaultTab(int tabIndex) {
        Log.d(this, "setStartupDefaultTab: " + tabIndex);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SettingsConstants.STARTUP_DEFAULT_TAB_KEY, tabIndex);
        editor.apply();

        startupDefaultTabCache = tabIndex;
    }
}

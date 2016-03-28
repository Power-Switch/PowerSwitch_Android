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
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;

/**
 * Preference handler used to store general app settings
 */
public class SmartphonePreferencesHandler {

    private static Context context;
    private static SharedPreferences sharedPreferences;

    // cached values
    private static boolean autoDiscoverCache;
    private static String backupPathCache;
    private static boolean showRoomAllOnOffCache;
    private static int startupDefaultTabCache;
    private static boolean hideUseOptionsMenuInsteadOfFABCache;
    private static boolean autoCollapseRoomsCache;
    private static boolean autoCollapseTimersCache;
    private static int themeCache;
    private static boolean vibrateOnButtonPressCache;
    private static int vibrationDurationCache;
    private static boolean highlightLastActivatedButtonCache;
    private static boolean useCompactDrawerCache;
    private static long currentApartmentIdCache;
    private static int keepHistoryDurationCache;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private SmartphonePreferencesHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable. Use static one time initialization via init() method instead.");
    }

    public static void init(Context context) {
        if (sharedPreferences != null) {
            forceRefresh();
            return;
        }

        SmartphonePreferencesHandler.context = context;
        sharedPreferences = context.getSharedPreferences(SettingsConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        initCache();
    }

    public static String getPublicKeyString() {
        String string = SettingsConstants.KDH_SDSA + SettingsConstants.JKD_COAP + SettingsConstants.DJA_IOVJ + SettingsConstants.VOK_ZWEQ;
        return string;
    }

    /**
     * First time initialization of cached values
     */
    private static void initCache() {
        autoDiscoverCache = sharedPreferences.getBoolean(SettingsConstants.AUTO_DISCOVER_KEY, true);
        backupPathCache = sharedPreferences.getString(SettingsConstants.BACKUP_PATH_KEY,
                Environment.getExternalStorageDirectory()
                        .getPath() + File.separator + BackupHandler.MAIN_BACKUP_FOLDERNAME);
        showRoomAllOnOffCache = sharedPreferences.getBoolean(SettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, true);
        startupDefaultTabCache = sharedPreferences.getInt(SettingsConstants.STARTUP_DEFAULT_TAB_KEY, SettingsConstants.ROOMS_TAB_INDEX);
        hideUseOptionsMenuInsteadOfFABCache = sharedPreferences.getBoolean(SettingsConstants.USE_OPTIONS_MENU_INSTEAD_OF_FAB_KEY, false);
        autoCollapseRoomsCache = sharedPreferences.getBoolean(SettingsConstants.AUTO_COLLAPSE_ROOMS_KEY, false);
        autoCollapseTimersCache = sharedPreferences.getBoolean(SettingsConstants.AUTO_COLLAPSE_TIMERS_KEY, false);
        themeCache = sharedPreferences.getInt(SettingsConstants.THEME_KEY, SettingsConstants.THEME_DARK_BLUE);
        vibrateOnButtonPressCache = sharedPreferences.getBoolean(SettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, true);
        vibrationDurationCache = sharedPreferences.getInt(SettingsConstants.VIBRATION_DURATION_KEY, SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK);
        highlightLastActivatedButtonCache = sharedPreferences.getBoolean(SettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, false);
        useCompactDrawerCache = sharedPreferences.getBoolean(SettingsConstants.USE_COMPACT_DRAWER_KEY, false);
        keepHistoryDurationCache = sharedPreferences.getInt(SettingsConstants.KEEP_HISTORY_DURATION, SettingsConstants.KEEP_HISTORY_FOREVER);

        if (!DeveloperPreferencesHandler.getPlayStoreMode()) {
            currentApartmentIdCache = sharedPreferences.getLong(SettingsConstants.CURRENT_APARTMENT_ID_KEY, SettingsConstants.INVALID_APARTMENT_ID);
        } else {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(SmartphonePreferencesHandler.context);
            currentApartmentIdCache = playStoreModeDataModel.getApartments().get(0).getId();
        }

        Log.d(SmartphonePreferencesHandler.class, "AutoDiscover: " + autoDiscoverCache);
        Log.d(SmartphonePreferencesHandler.class, "BackupPath: " + backupPathCache);
        Log.d(SmartphonePreferencesHandler.class, "ShowRoomAllOnOff: " + showRoomAllOnOffCache);
        Log.d(SmartphonePreferencesHandler.class, "AutoCollapseRooms: " + autoCollapseRoomsCache);
        Log.d(SmartphonePreferencesHandler.class, "AutoCollapseTimers: " + autoCollapseTimersCache);
        Log.d(SmartphonePreferencesHandler.class, "Theme: " + themeCache);
        Log.d(SmartphonePreferencesHandler.class, "VibrateOnButtonPress: " + vibrateOnButtonPressCache);
        Log.d(SmartphonePreferencesHandler.class, "VibrationDuration: " + vibrationDurationCache);
        Log.d(SmartphonePreferencesHandler.class, "HighlightLastActivatedButton: " + highlightLastActivatedButtonCache);
        Log.d(SmartphonePreferencesHandler.class, "HideAddFAB: " + hideUseOptionsMenuInsteadOfFABCache);
        Log.d(SmartphonePreferencesHandler.class, "StartupDefaultTab: " + startupDefaultTabCache);
        Log.d(SmartphonePreferencesHandler.class, "UseCompactDrawer: " + useCompactDrawerCache);
        Log.d(SmartphonePreferencesHandler.class, "CurrentApartmentId: " + currentApartmentIdCache);
        Log.d(SmartphonePreferencesHandler.class, "KeepHistoryDuration: " + keepHistoryDurationCache);
    }

    /**
     * Forces an update of the cached values
     */
    public static void forceRefresh() {
        initCache();
    }

    /**
     * Retrieves setting for AutoDiscovery of Gateways
     *
     * @return true if enabled
     */
    public static boolean getAutoDiscover() {
        return autoDiscoverCache;
    }

    /**
     * Sets setting for AutoDiscovery of Gateways
     *
     * @param bool true if enabled
     */
    public static void setAutoDiscover(boolean bool) {
        Log.d(SmartphonePreferencesHandler.class, "setAutoDiscover: " + bool);
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
    public static String getBackupPath() {
        return backupPathCache;
    }

    /**
     * Sets setting for Backup Path
     *
     * @param path Backup Path
     */
    public static void setBackupPath(String path) {
        Log.d(SmartphonePreferencesHandler.class, "setBackupPath: " + path);
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
    public static boolean getShowRoomAllOnOff() {
        return showRoomAllOnOffCache;
    }

    /**
     * Sets setting for automatic collapsing of Rooms
     *
     * @param bool true if enabled
     */
    public static void setShowRoomAllOnOff(boolean bool) {
        Log.d(SmartphonePreferencesHandler.class, "setShowRoomAllOnOff: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, bool);
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
        Log.d(SmartphonePreferencesHandler.class, "setAutoCollapseRooms: " + bool);
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
    public static boolean getAutoCollapseTimers() {
        return autoCollapseTimersCache;
    }

    /**
     * Sets setting for automatic collapsing of Timers
     *
     * @param bool true if enabled
     */
    public static void setAutoCollapseTimers(boolean bool) {
        Log.d(SmartphonePreferencesHandler.class, "setAutoCollapseTimers: " + bool);
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
    @SettingsConstants.Theme
    public static int getTheme() {
        return themeCache;
    }

    /**
     * Sets setting for current Theme
     *
     * @param theme ID (internal) of Theme
     */
    public static void setTheme(@SettingsConstants.Theme int theme) {
        Log.d(SmartphonePreferencesHandler.class, "setTheme: " + theme);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SettingsConstants.THEME_KEY, theme);
        editor.apply();

        SmartphonePreferencesHandler.themeCache = theme;
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
        Log.d(SmartphonePreferencesHandler.class, "setVibrateOnButtonPress: " + bool);
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
    public static int getVibrationDuration() {
        return vibrationDurationCache;
    }

    /**
     * Sets setting for vibration feedback duration
     *
     * @param milliseconds time in ms
     */
    public static void setVibrationDuration(int milliseconds) {
        Log.d(SmartphonePreferencesHandler.class, "setVibrationDuration: " + milliseconds);
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
    public static boolean getHighlightLastActivatedButton() {
        return highlightLastActivatedButtonCache;
    }

    /**
     * Sets setting for highlighting last activated button
     *
     * @param bool true if enabled
     */
    public static void setHighlightLastActivatedButton(boolean bool) {
        Log.d(SmartphonePreferencesHandler.class, "setHighlightLastActivatedButton: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, bool);
        editor.apply();

        highlightLastActivatedButtonCache = bool;
    }

    /**
     * Retrieves setting for using options menu instead of FAB buttons
     *
     * @return true if enabled
     */
    public static boolean getUseOptionsMenuInsteadOfFAB() {
        return hideUseOptionsMenuInsteadOfFABCache;
    }

    /**
     * Sets setting for using options menu instead of FAB buttons
     *
     * @param bool true if enabled (using options menu)
     */
    public static void setUseOptionsMenuInsteadOfFAB(boolean bool) {
        Log.d(SmartphonePreferencesHandler.class, "setUseOptionsMenuInsteadOfFAB: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.USE_OPTIONS_MENU_INSTEAD_OF_FAB_KEY, bool);
        editor.apply();

        hideUseOptionsMenuInsteadOfFABCache = bool;
    }

    /**
     * Retrieves setting for startup default tab
     *
     * @return tab index
     */
    public static int getStartupDefaultTab() {
        return startupDefaultTabCache;
    }

    /**
     * Sets setting for startup default tab
     *
     * @param tabIndex index of tab
     */
    public static void setStartupDefaultTab(int tabIndex) {
        Log.d(SmartphonePreferencesHandler.class, "setStartupDefaultTab: " + tabIndex);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SettingsConstants.STARTUP_DEFAULT_TAB_KEY, tabIndex);
        editor.apply();

        startupDefaultTabCache = tabIndex;
    }

    /**
     * Retrieves setting for the compact drawer style
     *
     * @return tab index
     */
    public static boolean getUseCompactDrawer() {
        return useCompactDrawerCache;
    }

    /**
     * Sets setting for the compact drawer style
     *
     * @param useCompactDrawer true if compact drawer style should be used
     */
    public static void setUseCompactDrawer(boolean useCompactDrawer) {
        Log.d(SmartphonePreferencesHandler.class, "setUseCompactDrawer: " + useCompactDrawer);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.USE_COMPACT_DRAWER_KEY, useCompactDrawer);
        editor.apply();

        useCompactDrawerCache = useCompactDrawer;
    }

    public static Long getCurrentApartmentId() {
        return currentApartmentIdCache;
    }

    public static void setCurrentApartmentId(Long apartmentId) {
        if (DeveloperPreferencesHandler.getPlayStoreMode()) {
            Log.w("PlayStoreMode is active, cant set current apartment ID!");
            return;
        }

        Log.d(SmartphonePreferencesHandler.class, "setCurrentApartmentId: " + apartmentId);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SettingsConstants.CURRENT_APARTMENT_ID_KEY, apartmentId);
        editor.apply();

        currentApartmentIdCache = apartmentId;
    }

    /**
     * Retrieves setting for duration to keep history elements
     *
     * @return duration
     */
    public static int getKeepHistoryDuration() {
        return keepHistoryDurationCache;
    }

    public static void setKeepHistoryDuration(int durationSelection) {
        Log.d(SmartphonePreferencesHandler.class, "setKeepHistoryDuration: " + durationSelection);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SettingsConstants.KEEP_HISTORY_DURATION, durationSelection);
        editor.apply();

        keepHistoryDurationCache = durationSelection;
    }
}

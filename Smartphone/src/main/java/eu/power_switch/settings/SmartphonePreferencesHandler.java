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
import android.widget.Toast;

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
    private static boolean sleepAsAndroidEnabledCache;
    private static boolean stockAlarmClockEnabledCache;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private SmartphonePreferencesHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable. Use static one time initialization via init() method instead.");
    }

    /**
     * Initialize this Handler
     * <p/>
     * Only one call per Application launch is needed
     *
     * @param context any suitable context
     */
    public static void init(Context context) {
        if (sharedPreferences != null) {
            forceRefresh(context);
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
        autoDiscoverCache = sharedPreferences.getBoolean(SettingsConstants.KEY_AUTO_DISCOVER, true);
        backupPathCache = sharedPreferences.getString(SettingsConstants.KEY_BACKUP_PATH,
                Environment.getExternalStorageDirectory()
                        .getPath() + File.separator + BackupHandler.MAIN_BACKUP_FOLDERNAME);
        showRoomAllOnOffCache = sharedPreferences.getBoolean(SettingsConstants.KEY_SHOW_ROOM_ALL_ON_OFF, true);
        startupDefaultTabCache = sharedPreferences.getInt(SettingsConstants.KEY_STARTUP_DEFAULT_TAB, SettingsConstants.ROOMS_TAB_INDEX);
        hideUseOptionsMenuInsteadOfFABCache = sharedPreferences.getBoolean(SettingsConstants.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB, false);
        autoCollapseRoomsCache = sharedPreferences.getBoolean(SettingsConstants.KEY_AUTO_COLLAPSE_ROOMS, false);
        autoCollapseTimersCache = sharedPreferences.getBoolean(SettingsConstants.KEY_AUTO_COLLAPSE_TIMERS, false);
        themeCache = sharedPreferences.getInt(SettingsConstants.KEY_THEME, SettingsConstants.THEME_DARK_BLUE);
        vibrateOnButtonPressCache = sharedPreferences.getBoolean(SettingsConstants.KEY_VIBRATE_ON_BUTTON_PRESS, true);
        vibrationDurationCache = sharedPreferences.getInt(SettingsConstants.KEY_VIBRATION_DURATION, SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK);
        highlightLastActivatedButtonCache = sharedPreferences.getBoolean(SettingsConstants.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, false);
        useCompactDrawerCache = sharedPreferences.getBoolean(SettingsConstants.KEY_USE_COMPACT_DRAWER, false);
        keepHistoryDurationCache = sharedPreferences.getInt(SettingsConstants.KEY_KEEP_HISTORY_DURATION, SettingsConstants.KEEP_HISTORY_FOREVER);
        sleepAsAndroidEnabledCache = sharedPreferences.getBoolean(SettingsConstants.KEY_SLEEP_AS_ANDROID_ENABLED, true);
        stockAlarmClockEnabledCache = sharedPreferences.getBoolean(SettingsConstants.KEY_STOCK_ALARM_CLOCK_ENABLED, true);

        if (!DeveloperPreferencesHandler.getPlayStoreMode()) {
            currentApartmentIdCache = sharedPreferences.getLong(SettingsConstants.KEY_CURRENT_APARTMENT_ID, SettingsConstants.INVALID_APARTMENT_ID);
        } else {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(SmartphonePreferencesHandler.context);
            currentApartmentIdCache = PlayStoreModeDataModel.getApartments().get(0).getId();
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
        Log.d(SmartphonePreferencesHandler.class, "SleepAsAndroidEnabled: " + sleepAsAndroidEnabledCache);
        Log.d(SmartphonePreferencesHandler.class, "StockAlarmClockEnabled: " + stockAlarmClockEnabledCache);
    }

    /**
     * Forces an update of the cached values
     */
    public static void forceRefresh(Context context) {
        sharedPreferences = context.getSharedPreferences(SettingsConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
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
        editor.putBoolean(SettingsConstants.KEY_AUTO_DISCOVER, bool);
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
        editor.putString(SettingsConstants.KEY_BACKUP_PATH, path);
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
        editor.putBoolean(SettingsConstants.KEY_SHOW_ROOM_ALL_ON_OFF, bool);
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
        editor.putBoolean(SettingsConstants.KEY_AUTO_COLLAPSE_ROOMS, bool);
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
        editor.putBoolean(SettingsConstants.KEY_AUTO_COLLAPSE_TIMERS, bool);
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
        editor.putInt(SettingsConstants.KEY_THEME, theme);
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
        editor.putBoolean(SettingsConstants.KEY_VIBRATE_ON_BUTTON_PRESS, bool);
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
        editor.putInt(SettingsConstants.KEY_VIBRATION_DURATION, milliseconds);
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
        editor.putBoolean(SettingsConstants.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, bool);
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
        editor.putBoolean(SettingsConstants.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB, bool);
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
        editor.putInt(SettingsConstants.KEY_STARTUP_DEFAULT_TAB, tabIndex);
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
        editor.putBoolean(SettingsConstants.KEY_USE_COMPACT_DRAWER, useCompactDrawer);
        editor.apply();

        useCompactDrawerCache = useCompactDrawer;
    }

    /**
     * Get ID of currently active Apartment
     *
     * @return ID of apartment, may be {@see SettingsConstants.INVALID_APARTMENT_ID} if none is active
     */
    public static Long getCurrentApartmentId() {
        return currentApartmentIdCache;
    }

    /**
     * Set ID of currently active Apartment
     *
     * @param apartmentId ID of Apartment
     */
    public static void setCurrentApartmentId(Long apartmentId) {
        if (DeveloperPreferencesHandler.getPlayStoreMode()) {
            Toast.makeText(context, "PlayStoreMode is active, cant set current apartment ID!", Toast.LENGTH_LONG).show();
            Log.w("PlayStoreMode is active, cant set current apartment ID!");
            return;
        }

        Log.d(SmartphonePreferencesHandler.class, "setCurrentApartmentId: " + apartmentId);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SettingsConstants.KEY_CURRENT_APARTMENT_ID, apartmentId);
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

    /**
     * Set duration to keep history elements in preferences
     *
     * @param durationSelection
     */
    public static void setKeepHistoryDuration(int durationSelection) {
        Log.d(SmartphonePreferencesHandler.class, "setKeepHistoryDuration: " + durationSelection);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SettingsConstants.KEY_KEEP_HISTORY_DURATION, durationSelection);
        editor.apply();

        keepHistoryDurationCache = durationSelection;
    }

    /**
     * Retrieves setting for stock alarm clock
     *
     * @return true if enabled, false otherwise
     */
    public static boolean getStockAlarmClockEnabled() {
        return stockAlarmClockEnabledCache;
    }

    /**
     * Set enabled state of stock alarm clock
     *
     * @param enabled true if enabled, false otherwise
     */
    public static void setStockAlarmClockEnabled(boolean enabled) {
        Log.d(SmartphonePreferencesHandler.class, "setStockAlarmClockEnabled: " + enabled);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.KEY_STOCK_ALARM_CLOCK_ENABLED, enabled);
        editor.apply();

        stockAlarmClockEnabledCache = enabled;
    }

    /**
     * Retrieves setting for Sleep As Android alarm clock
     *
     * @return true if enabled, false otherwise
     */
    public static boolean getSleepAsAndroidEnabled() {
        return sleepAsAndroidEnabledCache;
    }

    /**
     * Set enabled state of Sleep As Android alarm clock
     *
     * @param enabled true if enabled, false otherwise
     */
    public static void setSleepAsAndroidEnabled(boolean enabled) {
        Log.d(SmartphonePreferencesHandler.class, "setSleepAsAndroidEnabled: " + enabled);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.KEY_SLEEP_AS_ANDROID_ENABLED, enabled);
        editor.apply();

        sleepAsAndroidEnabledCache = enabled;
    }
}

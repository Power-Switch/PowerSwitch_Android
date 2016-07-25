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
import android.support.annotation.StringDef;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import eu.power_switch.backup.BackupHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;

/**
 * Preference handler used to store general app settings
 */
public class SmartphonePreferencesHandler {

    // setting keys
    public static final String KEY_AUTO_DISCOVER = "autoDiscover";
    public static final String KEY_BACKUP_PATH = "backupPath";
    public static final String KEY_STARTUP_DEFAULT_TAB = "startupDefaultTab";
    public static final String KEY_SHOW_ROOM_ALL_ON_OFF = "showRoomAllOnOff";
    public static final String KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON = "highlightLastActivatedButton";
    public static final String KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB = "hideAddFAB";
    public static final String KEY_AUTO_COLLAPSE_ROOMS = "autoCollapseRooms";
    public static final String KEY_AUTO_COLLAPSE_TIMERS = "autoCollapseTimers";
    public static final String KEY_THEME = "theme";
    public static final String KEY_USE_COMPACT_DRAWER = "useCompactDrawer";
    public static final String KEY_VIBRATE_ON_BUTTON_PRESS = "vibrateOnButtonPress";
    public static final String KEY_VIBRATION_DURATION = "vibrationDuration";
    public static final String KEY_CURRENT_APARTMENT_ID = "currentApartmentId";
    public static final String KEY_KEEP_HISTORY_DURATION = "keepHistoryDuration";
    public static final String KEY_SLEEP_AS_ANDROID_ENABLED = "SLEEP_AS_ANDROID_ENABLED_KEY";
    public static final String KEY_STOCK_ALARM_CLOCK_ENABLED = "STOCK_ALARM_CLOCK_ENABLED_KEY";
    public static final String KEY_SHOW_TOAST_IN_BACKGROUND = "KEY_SHOW_TOAST_IN_BACKGROUND";

    // default values
    private static final boolean DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF = true;
    private static final boolean DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON = false;
    private static final boolean DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS = false;
    private static final int DEFAULT_VALUE_THEME = SettingsConstants.THEME_DARK_BLUE;
    private static final boolean DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS = true;
    private static final int DEFAULT_VALUE_VIBRATION_DURATION = SettingsConstants
            .DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK;
    private static final int DEFAULT_VALUE_STARTUP_TAB = SettingsConstants.ROOMS_TAB_INDEX;
    private static final boolean DEFAULT_VALUE_STOCK_ALARM_CLOCK_ENABLED = true;
    private static final boolean DEFAULT_VALUE_SLEEP_AS_ANDROID_ENABLED = true;
    private static final int DEFAULT_VALUE_KEEP_HISTORY_DURATION = SettingsConstants.KEEP_HISTORY_FOREVER;
    private static final long DEFAULT_VALUE_CURRENT_APARTMENT_ID = SettingsConstants.INVALID_APARTMENT_ID;
    private static final boolean DEFAULT_VALUE_USE_COMPACT_DRAWER = false;
    private static final boolean DEFAULT_VALUE_AUTO_COLLAPSE_TIMERS = false;
    private static final boolean DEFAULT_VALUE_USE_OPTIONS_MENU_INSTEAD_OF_FAB = false;
    private static final String DEFAULT_VALUE_BACKUP_PATH = Environment.getExternalStorageDirectory()
            .getPath() + File.separator + BackupHandler.MAIN_BACKUP_FOLDERNAME;
    private static final boolean DEFAULT_VALUE_AUTO_DISCOVER = true;
    private static final boolean DEFAULT_VALUE_TOAST_IN_BACKGROUND = true;


    private static Context context;
    private static SharedPreferences sharedPreferences;
    private static Map<String, ?> cachedValues;

    // default values for each settings key
    private static Map<String, Object> defaultValueMap;

    static {
        defaultValueMap = new HashMap<>();
        defaultValueMap.put(KEY_SHOW_ROOM_ALL_ON_OFF, DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF);
        defaultValueMap.put(KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        defaultValueMap.put(KEY_AUTO_COLLAPSE_ROOMS, DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS);
        defaultValueMap.put(KEY_THEME, DEFAULT_VALUE_THEME);
        defaultValueMap.put(KEY_VIBRATE_ON_BUTTON_PRESS, DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS);
        defaultValueMap.put(KEY_VIBRATION_DURATION, DEFAULT_VALUE_VIBRATION_DURATION);
        defaultValueMap.put(KEY_STARTUP_DEFAULT_TAB, DEFAULT_VALUE_STARTUP_TAB);

        defaultValueMap.put(KEY_STOCK_ALARM_CLOCK_ENABLED, DEFAULT_VALUE_STOCK_ALARM_CLOCK_ENABLED);
        defaultValueMap.put(KEY_SLEEP_AS_ANDROID_ENABLED, DEFAULT_VALUE_SLEEP_AS_ANDROID_ENABLED);
        defaultValueMap.put(KEY_KEEP_HISTORY_DURATION, DEFAULT_VALUE_KEEP_HISTORY_DURATION);
        defaultValueMap.put(KEY_CURRENT_APARTMENT_ID, DEFAULT_VALUE_CURRENT_APARTMENT_ID);
        defaultValueMap.put(KEY_USE_COMPACT_DRAWER, DEFAULT_VALUE_USE_COMPACT_DRAWER);
        defaultValueMap.put(KEY_AUTO_COLLAPSE_TIMERS, DEFAULT_VALUE_AUTO_COLLAPSE_TIMERS);
        defaultValueMap.put(KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB, DEFAULT_VALUE_USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        defaultValueMap.put(KEY_BACKUP_PATH, DEFAULT_VALUE_BACKUP_PATH);
        defaultValueMap.put(KEY_AUTO_DISCOVER, DEFAULT_VALUE_AUTO_DISCOVER);
        defaultValueMap.put(KEY_SHOW_TOAST_IN_BACKGROUND, DEFAULT_VALUE_TOAST_IN_BACKGROUND);
    }

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
        SmartphonePreferencesHandler.context = context;
        sharedPreferences = context.getSharedPreferences(
                SettingsConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        forceRefresh();

        for (String key : cachedValues.keySet()) {
            Log.d(SmartphonePreferencesHandler.class, key + ": " + get(key));
        }
    }

    /**
     * Forces an update of the cached values
     */
    public static void forceRefresh() {
        cachedValues = sharedPreferences.getAll();
    }

    public static String getPublicKeyString() {
        String string = SettingsConstants.KDH_SDSA + SettingsConstants.JKD_COAP + SettingsConstants.DJA_IOVJ + SettingsConstants.VOK_ZWEQ;
        return string;
    }

    /**
     * Get a settings value by key
     *
     * @param settingsKey Key of setting
     * @param <T>         type of expected return value
     * @return
     */
    public static <T> T get(@Key String settingsKey) throws ClassCastException {
        // Log.d(WearablePreferencesHandler.class, "retrieving current value for key \"" + settingsKey + "\"");

        Object value = cachedValues.get(settingsKey);

        if (value == null) {
            return (T) getDefaultValue(settingsKey);
        } else {
            // special treatment for this key, to make playstore mode possible
            if (KEY_CURRENT_APARTMENT_ID.equals(settingsKey)) {
                if (!DeveloperPreferencesHandler.getPlayStoreMode()) {
                    return (T) value;
                } else {
                    PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(SmartphonePreferencesHandler.context);
                    return (T) playStoreModeDataModel.getApartments().get(0).getId();
                }
            }

            return (T) value;
        }
    }

    private static Object getDefaultValue(@Key String settingsKey) {
        return defaultValueMap.get(settingsKey);
    }

    /**
     * Set a settings value by key
     *
     * @param settingsKey Key of setting
     * @param newValue    new value
     * @param <T>
     */
    public static <T> void set(@Key String settingsKey, T newValue) {
        Log.d(SmartphonePreferencesHandler.class, "setting new value \"" + newValue + "\" for key \"" + settingsKey + "\"");

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (newValue instanceof Boolean) {
            editor.putBoolean(settingsKey, (Boolean) newValue);
        } else if (newValue instanceof String) {
            editor.putString(settingsKey, (String) newValue);
        } else if (newValue instanceof Integer) {
            editor.putInt(settingsKey, (Integer) newValue);
        } else if (newValue instanceof Float) {
            editor.putFloat(settingsKey, (Float) newValue);
        } else if (newValue instanceof Long) {
            editor.putLong(settingsKey, (Long) newValue);
        } else {
            throw new IllegalArgumentException("Cant save objects of type " + newValue.getClass());
        }

        editor.apply();

        forceRefresh();
    }

    @StringDef({KEY_SHOW_ROOM_ALL_ON_OFF,
            KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON,
            KEY_AUTO_COLLAPSE_ROOMS,
            KEY_THEME,
            KEY_VIBRATE_ON_BUTTON_PRESS,
            KEY_VIBRATION_DURATION,
            KEY_STARTUP_DEFAULT_TAB,
            KEY_STOCK_ALARM_CLOCK_ENABLED,
            KEY_SLEEP_AS_ANDROID_ENABLED,
            KEY_KEEP_HISTORY_DURATION,
            KEY_CURRENT_APARTMENT_ID,
            KEY_USE_COMPACT_DRAWER,
            KEY_AUTO_COLLAPSE_TIMERS,
            KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB,
            KEY_BACKUP_PATH,
            KEY_AUTO_DISCOVER,
            KEY_SHOW_TOAST_IN_BACKGROUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Key {
    }
}

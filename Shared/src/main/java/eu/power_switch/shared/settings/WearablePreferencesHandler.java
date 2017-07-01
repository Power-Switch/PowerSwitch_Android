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

import java.util.HashMap;
import java.util.Map;

import eu.power_switch.shared.R;
import eu.power_switch.shared.constants.SettingsConstants;
import timber.log.Timber;

/**
 * This class is responsible for accessing and modifying Wear App Settings
 * <p/>
 * Note: Most (if not all) Settings can not be changed on the Wearable itself but only using the Smartphone
 * <p/>
 * <p/>
 * Created by Markus on 13.11.2015.
 */
public class WearablePreferencesHandler {

    // SharedPreferences
    public static final String  WEARABLE_SHARED_PREFS_NAME                    = "eu.power_switch.wearable.prefs";
    // default values
    public static final boolean DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF            = true;
    public static final boolean DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON = false;
    public static final boolean DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS             = false;
    public static final int     DEFAULT_VALUE_THEME                           = SettingsConstants.THEME_DARK_BLUE;
    public static final boolean DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS         = true;
    public static final int     DEFAULT_VALUE_VIBRATION_DURATION              = SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK;
    public static final int     DEFAULT_VALUE_STARTUP_TAB                     = 0;
    // setting keys
    public static  String            KEY_SHOW_ROOM_ALL_ON_OFF;
    public static  String            KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON;
    public static  String            KEY_AUTO_COLLAPSE_ROOMS;
    public static  String            KEY_STARTUP_DEFAULT_TAB;
    public static  String            KEY_THEME;
    public static  String            KEY_VIBRATE_ON_BUTTON_PRESS;
    public static  String            KEY_VIBRATION_DURATION;
    private static SharedPreferences sharedPreferences;
    private static Map<String, ?>    cachedValues;

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
    }

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
        sharedPreferences = context.getSharedPreferences(WEARABLE_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        forceRefresh();

        initializePublicKeys(context);
        initializeDefaultValueMap();

        for (String key : cachedValues.keySet()) {
            Timber.d(key + ": " + get(key));
        }
    }

    private static void initializePublicKeys(Context context) {
        KEY_STARTUP_DEFAULT_TAB = context.getString(R.string.key_startupDefaultTab);
        KEY_SHOW_ROOM_ALL_ON_OFF = context.getString(R.string.key_showRoomAllOnOff);
        KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON = context.getString(R.string.key_highlightLastActivatedButton);
        KEY_AUTO_COLLAPSE_ROOMS = context.getString(R.string.key_autoCollapseRooms);
        KEY_THEME = context.getString(R.string.key_theme);
        KEY_VIBRATE_ON_BUTTON_PRESS = context.getString(R.string.key_vibrateOnButtonPress);
        KEY_VIBRATION_DURATION = context.getString(R.string.key_vibrationDuration);
    }

    private static void initializeDefaultValueMap() {
        defaultValueMap = new HashMap<>();
        defaultValueMap.put(KEY_SHOW_ROOM_ALL_ON_OFF, DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF);
        defaultValueMap.put(KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        defaultValueMap.put(KEY_AUTO_COLLAPSE_ROOMS, DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS);
        defaultValueMap.put(KEY_THEME, DEFAULT_VALUE_THEME);
        defaultValueMap.put(KEY_VIBRATE_ON_BUTTON_PRESS, DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS);
        defaultValueMap.put(KEY_VIBRATION_DURATION, DEFAULT_VALUE_VIBRATION_DURATION);
        defaultValueMap.put(KEY_STARTUP_DEFAULT_TAB, DEFAULT_VALUE_STARTUP_TAB);

        for (String key : defaultValueMap.keySet()) {
            // initialize missing default values
            get(key);
        }
    }

    /**
     * Forces an update of the cached values
     */
    public static void forceRefresh() {
        cachedValues = sharedPreferences.getAll();
    }

    /**
     * Get a settings value by key
     *
     * @param settingsKey Key of setting
     * @param <T>         expected type of return value
     *
     * @return value
     */
    public static <T> T get(String settingsKey) throws ClassCastException {
        // Timber.d("retrieving current value for key \"" + settingsKey + "\"");

        Object value = cachedValues.get(settingsKey);

        if (value == null) {
            return (T) getDefaultValue(settingsKey);
        } else {
            return (T) value;
        }
    }

    private static Object getDefaultValue(String settingsKey) {
        return defaultValueMap.get(settingsKey);
    }

    /**
     * Set a settings value by key
     *
     * @param settingsKey Key of setting
     * @param newValue    new value
     */
    public static void set(String settingsKey, Object newValue) {
        Timber.d("setting new value \"" + newValue + "\" for key \"" + settingsKey + "\"");

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
}

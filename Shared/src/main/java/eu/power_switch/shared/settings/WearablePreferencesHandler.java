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
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import eu.power_switch.shared.constants.SettingsConstants;
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

    // SharedPreferences
    public static final String WEARABLE_SHARED_PREFS_NAME = "eu.power_switch.wearable.prefs";

    // setting keys
    public static final String KEY_SHOW_ROOM_ALL_ON_OFF = "showRoomAllOnOff";
    public static final String KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON = "highlightLastActivatedButton";
    public static final String KEY_AUTO_COLLAPSE_ROOMS = "autoCollapseRooms";
    public static final String KEY_STARTUP_DEFAULT_TAB = "startupDefaultTab";
    public static final String KEY_THEME = "theme";
    public static final String KEY_VIBRATE_ON_BUTTON_PRESS = "vibrateOnButtonPress";
    public static final String KEY_VIBRATION_DURATION = "vibrationDuration";

    // default values
    public static final boolean DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF = true;
    public static final boolean DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON = false;
    public static final boolean DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS = false;
    public static final int DEFAULT_VALUE_THEME = SettingsConstants.THEME_DARK_BLUE;
    public static final boolean DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS = true;
    public static final int DEFAULT_VALUE_VIBRATION_DURATION = SettingsConstants
            .DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK;
    public static final int DEFAULT_VALUE_STARTUP_TAB = 0;

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
        sharedPreferences = context.getSharedPreferences(
                WEARABLE_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        forceRefresh();

        for (String key : cachedValues.keySet()) {
            Log.d(WearablePreferencesHandler.class, key + ": " + get(key));
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
     * @param <T>         type of expected return value
     * @return
     */
    public static <T> T get(@Key String settingsKey) throws ClassCastException {
        // Log.d(WearablePreferencesHandler.class, "retrieving current value for key \"" + settingsKey + "\"");

        Object value = cachedValues.get(settingsKey);

        if (value == null) {
            return (T) getDefaultValue(settingsKey);
        } else {
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
        Log.d(WearablePreferencesHandler.class, "setting new value \"" + newValue + "\" for key \"" + settingsKey + "\"");

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
            KEY_STARTUP_DEFAULT_TAB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Key {
    }
}

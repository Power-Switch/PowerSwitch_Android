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

package eu.power_switch.persistence.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.io.File;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.persistence.demo_mode.DemoModePersistenceHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import lombok.Getter;
import timber.log.Timber;

import static eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler.PreferenceItem.KEY_CURRENT_APARTMENT_ID;

/**
 * Preference handler used to store general app settings
 */
@Singleton
public class SmartphonePreferencesHandler {

    // default values
    private static final boolean DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF                 = true;
    private static final boolean DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON      = false;
    private static final boolean DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS                  = false;
    private static final int     DEFAULT_VALUE_THEME                                = SettingsConstants.THEME_DARK_BLUE;
    private static final boolean DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS              = true;
    private static final int     DEFAULT_VALUE_VIBRATION_DURATION                   = SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK;
    private static final int     DEFAULT_VALUE_STARTUP_TAB                          = SettingsConstants.ROOMS_TAB_INDEX;
    private static final boolean DEFAULT_VALUE_STOCK_ALARM_CLOCK_ENABLED            = true;
    private static final boolean DEFAULT_VALUE_SLEEP_AS_ANDROID_ENABLED             = true;
    private static final int     DEFAULT_VALUE_KEEP_HISTORY_DURATION                = SettingsConstants.KEEP_HISTORY_FOREVER;
    private static final long    DEFAULT_VALUE_CURRENT_APARTMENT_ID                 = SettingsConstants.INVALID_APARTMENT_ID;
    private static final boolean DEFAULT_VALUE_USE_COMPACT_DRAWER                   = false;
    private static final boolean DEFAULT_VALUE_AUTO_COLLAPSE_TIMERS                 = false;
    private static final boolean DEFAULT_VALUE_USE_OPTIONS_MENU_INSTEAD_OF_FAB      = false;
    private static final String  DEFAULT_VALUE_BACKUP_PATH                          = Environment.getExternalStorageDirectory()
            .getPath() + File.separator + BackupHandler.MAIN_BACKUP_FOLDERNAME;
    private static final boolean DEFAULT_VALUE_AUTO_DISCOVER                        = true;
    private static final boolean DEFAULT_VALUE_SHOW_TOAST_IN_BACKGROUND             = true;
    private static final boolean DEFAULT_VALUE_SEND_ANONYMOUS_CRASH_DATA            = true;
    private static final int     DEFAULT_VALUE_LOG_DESTINATION                      = 0;
    private static final boolean DEFAULT_VALUE_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA = true;
    private static final boolean DEFAULT_VALUE_SHOW_GEOFENCE_NOTIFICATIONS          = true;
    private static final boolean DEFAULT_VALUE_SHOW_TIMER_NOTIFICATIONS             = true;
    private static final boolean DEFAULT_VALUE_SHOULD_SHOW_WIZARD                   = true;
    private static final int     DEFAULT_VALUE_LAUNCHER_ICON                        = 0;

    @Getter
    public enum PreferenceItem {
        // TODO: use enum instead of string constants

        KEY_AUTO_DISCOVER(R.string.key_autodiscover, DEFAULT_VALUE_AUTO_DISCOVER),
        KEY_BACKUP_PATH(R.string.key_backupPath, DEFAULT_VALUE_BACKUP_PATH),
        KEY_STARTUP_DEFAULT_TAB(R.string.key_startupDefaultTab, DEFAULT_VALUE_STARTUP_TAB),
        KEY_SHOW_ROOM_ALL_ON_OFF(R.string.key_showRoomAllOnOff, DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF),
        KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON(R.string.key_highlightLastActivatedButton, DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON),
        KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB(R.string.key_useOptionsMenuInsteadOfFab, DEFAULT_VALUE_USE_OPTIONS_MENU_INSTEAD_OF_FAB),
        KEY_AUTO_COLLAPSE_ROOMS(R.string.key_autoCollapseRooms, DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS),
        KEY_AUTO_COLLAPSE_TIMERS(R.string.key_autoCollapseTimers, DEFAULT_VALUE_AUTO_COLLAPSE_TIMERS),
        KEY_THEME(R.string.key_theme, DEFAULT_VALUE_THEME),
        KEY_USE_COMPACT_DRAWER(R.string.key_useCompactDrawer, DEFAULT_VALUE_USE_COMPACT_DRAWER),
        KEY_VIBRATE_ON_BUTTON_PRESS(R.string.key_vibrateOnButtonPress, DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS),
        KEY_VIBRATION_DURATION(R.string.key_vibrationDuration, DEFAULT_VALUE_VIBRATION_DURATION),
        KEY_CURRENT_APARTMENT_ID(R.string.key_currentApartmentId, DEFAULT_VALUE_CURRENT_APARTMENT_ID),
        KeekpHistoryDuration(R.string.key_keepHistoryDuration, DEFAULT_VALUE_KEEP_HISTORY_DURATION),
        KEY_SLEEP_AS_ANDROID_ENABLED(R.string.key_sleepAsAndroidEnabled, DEFAULT_VALUE_SLEEP_AS_ANDROID_ENABLED),
        KEY_STOCK_ALARM_CLOCK_ENABLED(R.string.key_stockAlarmClockEnabled, DEFAULT_VALUE_STOCK_ALARM_CLOCK_ENABLED),
        KEY_SHOW_TOAST_IN_BACKGROUND(R.string.key_showBackgroundActionToast, DEFAULT_VALUE_SHOW_TOAST_IN_BACKGROUND),
        KEY_SEND_ANONYMOUS_CRASH_DATA(R.string.key_sendAnonymousCrashData, DEFAULT_VALUE_SEND_ANONYMOUS_CRASH_DATA),
        KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA(R.string.key_shouldAskSendAnonymousCrashData, DEFAULT_VALUE_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA),
        KEY_LOG_DESTINATION(R.string.key_logDestination, DEFAULT_VALUE_LOG_DESTINATION),
        KEY_SHOW_GEOFENCE_NOTIFICATIONS(R.string.key_showGeofenceNotifications, DEFAULT_VALUE_SHOW_GEOFENCE_NOTIFICATIONS),
        KEY_SHOW_TIMER_NOTIFICATIONS(R.string.key_showTimerNotifications, DEFAULT_VALUE_SHOW_TIMER_NOTIFICATIONS),
        KEY_SHOULD_SHOW_WIZARD(R.string.key_shouldShowWizard, DEFAULT_VALUE_SHOULD_SHOW_WIZARD),
        KEY_LAUNCHER_ICON(R.string.key_launcher_icon, DEFAULT_VALUE_LAUNCHER_ICON);

        @StringRes
        private final int    keyRes;
        private final Class  type;
        private final Object defaultValue;

        <T> PreferenceItem(@StringRes int keyRes, T defaultValue) {
            this.keyRes = keyRes;
            this.type = defaultValue.getClass();
            this.defaultValue = defaultValue;
        }

        /**
         * Get the key of this preference as string
         *
         * @param context application context
         *
         * @return Preference key
         */
        public String getKey(Context context) {
            return context.getString(keyRes);
        }

        /**
         * Get an enum constant by using it's key
         *
         * @param context application context
         * @param key     key
         *
         * @return PreferenceItem
         */
        public static PreferenceItem fromKey(Context context, String key) {
            for (PreferenceItem preferenceItem : values()) {
                if (preferenceItem.getKey(context)
                        .equals(key)) {
                    return preferenceItem;
                }
            }

            throw new NoSuchElementException("No PreferenceItem found for key: " + key);
        }
    }

    // setting keys
    private SharedPreferences sharedPreferences;
    private Map<String, ?>    cachedValues;

    private Context context;

    @Inject
    public SmartphonePreferencesHandler(Context context) {
        this.context = context;

        sharedPreferences = context.getSharedPreferences(SettingsConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        forceRefreshCache();

        // doesnt work when logger isnt initialized yet
        for (PreferenceItem preferenceItem : PreferenceItem.values()) {
            Timber.d(preferenceItem.getKey(context) + ": " + get(preferenceItem));
        }
    }

    /**
     * Forces an update of the cached values
     */
    public void forceRefreshCache() {
        cachedValues = sharedPreferences.getAll();
    }

    public String getPublicKeyString() {
        return SettingsConstants.KDH_SDSA + SettingsConstants.JKD_COAP + SettingsConstants.DJA_IOVJ + SettingsConstants.VOK_ZWEQ;
    }

    /**
     * Get a settings value by key
     * <p>
     * Note: Be sure to assign the return value of this method to variable with your expected return type.
     *
     * @param preferenceItem Key of setting
     * @param <T>            expected type of return value (optional)
     *
     * @return settings value
     */
    @SuppressWarnings("unchecked")
    @CheckResult
    public <T extends Object> T get(@NonNull PreferenceItem preferenceItem) throws ClassCastException {
        String key = preferenceItem.getKey(context);

        Object value = cachedValues.get(key);

        // if no value was set, return preference default
        if (value == null) {
            value = preferenceItem.getDefaultValue();
            // save default value in file
            set(preferenceItem, value);
        } else {
            // special treatment for this key, to make playstore mode possible
            if (KEY_CURRENT_APARTMENT_ID.equals(preferenceItem) && DeveloperPreferencesHandler.getPlayStoreMode()) {
                DemoModePersistenceHandler demoModePersistanceHandler = new DemoModePersistenceHandler(context);
                try {
                    value = demoModePersistanceHandler.getAllApartments()
                            .get(0)
                            .getId();
                } catch (Exception e) {
                    throw new RuntimeException("Error fetching apartment id for demo mode");
                }
            }
        }

        Timber.v("retrieving value \"" + value + "\" for key \"" + key + "\"");

        return (T) preferenceItem.getType()
                .cast(value);
    }

    /**
     * Set a settings value by key
     *
     * @param preferenceItem the preference to set a new value for
     * @param newValue       new value
     */
    public void set(@NonNull PreferenceItem preferenceItem, @NonNull Object newValue) {
        String key = preferenceItem.getKey(context);

        Timber.d("setting new value \"" + newValue + "\" for key \"" + key + "\"");

        // check if the passed in type matches the expected one
        if (!newValue.getClass()
                .isAssignableFrom(preferenceItem.getType())) {
            throw new IllegalArgumentException("Invalid type! Should be " + preferenceItem.getType()
                    .getCanonicalName() + " but was " + newValue.getClass()
                    .getCanonicalName());
        }


        // store the new value
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (newValue instanceof Boolean) {
            editor.putBoolean(key, (Boolean) newValue);
        } else if (newValue instanceof String) {
            editor.putString(key, (String) newValue);
        } else if (newValue instanceof Integer) {
            editor.putInt(key, (Integer) newValue);
        } else if (newValue instanceof Float) {
            editor.putFloat(key, (Float) newValue);
        } else if (newValue instanceof Long) {
            editor.putLong(key, (Long) newValue);
        } else {
            throw new IllegalArgumentException("Cant save objects of type " + newValue.getClass());
        }

        editor.apply();

        forceRefreshCache();
    }

}

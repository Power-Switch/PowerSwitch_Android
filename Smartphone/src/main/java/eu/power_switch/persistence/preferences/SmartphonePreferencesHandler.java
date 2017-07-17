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

package eu.power_switch.persistence.preferences;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.persistence.data.demo_mode.DemoModePersistenceHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.persistence.preferences.PreferenceItem;
import eu.power_switch.shared.persistence.preferences.PreferencesHandlerBase;

/**
 * Preference handler used to store general app settings
 */
@Singleton
public class SmartphonePreferencesHandler extends PreferencesHandlerBase {

    public static final PreferenceItem<Boolean> GATEWAY_AUTO_DISCOVERY = new SmartphonePreferenceItem<>(R.string.key_autodiscover, true);

    public static final PreferenceItem<String> BACKUP_PATH = new SmartphonePreferenceItem<>(R.string.key_backupPath,
            Environment.getExternalStorageDirectory()
                    .getPath() + File.separator + BackupHandler.MAIN_BACKUP_FOLDERNAME);

    public static final PreferenceItem<Integer> STARTUP_DEFAULT_TAB = new SmartphonePreferenceItem<>(R.string.key_startupDefaultTab,
            SettingsConstants.ROOMS_TAB_INDEX);

    public static final PreferenceItem<Boolean> SHOW_ROOM_ALL_ON_OFF            = new SmartphonePreferenceItem<>(R.string.key_showRoomAllOnOff, true);
    public static final PreferenceItem<Boolean> HIGHLIGHT_LAST_ACTIVATED_BUTTON = new SmartphonePreferenceItem<>(R.string.key_highlightLastActivatedButton,
            false);
    public static final PreferenceItem<Boolean> USE_OPTIONS_MENU_INSTEAD_OF_FAB = new SmartphonePreferenceItem<>(R.string.key_useOptionsMenuInsteadOfFab,
            false);
    public static final PreferenceItem<Boolean> KEY_AUTO_COLLAPSE_ROOMS         = new SmartphonePreferenceItem<>(R.string.key_autoCollapseRooms,
            false);
    public static final PreferenceItem<Boolean> KEY_AUTO_COLLAPSE_TIMERS        = new SmartphonePreferenceItem<>(R.string.key_autoCollapseTimers,
            false);
    public static final PreferenceItem<Integer> KEY_THEME                       = new SmartphonePreferenceItem<>(R.string.key_theme,
            SettingsConstants.THEME_DARK_BLUE);
    public static final PreferenceItem<Boolean> KEY_USE_COMPACT_DRAWER          = new SmartphonePreferenceItem<>(R.string.key_useCompactDrawer,
            false);
    public static final PreferenceItem<Boolean> KEY_VIBRATE_ON_BUTTON_PRESS     = new SmartphonePreferenceItem<>(R.string.key_vibrateOnButtonPress,
            true);
    public static final PreferenceItem<Integer> KEY_VIBRATION_DURATION          = new SmartphonePreferenceItem<>(R.string.key_vibrationDuration,
            SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK);
    public static final PreferenceItem<Long>    KEY_CURRENT_APARTMENT_ID        = new SmartphonePreferenceItem<>(R.string.key_currentApartmentId,
            SettingsConstants.INVALID_APARTMENT_ID);
    public static final PreferenceItem<Integer> KEEP_HISTORY_DURATION           = new SmartphonePreferenceItem<>(R.string.key_keepHistoryDuration,
            SettingsConstants.KEEP_HISTORY_FOREVER);
    public static final PreferenceItem<Boolean> KEY_SLEEP_AS_ANDROID_ENABLED    = new SmartphonePreferenceItem<>(R.string.key_sleepAsAndroidEnabled,
            true);
    public static final PreferenceItem<Boolean> KEY_STOCK_ALARM_CLOCK_ENABLED   = new SmartphonePreferenceItem<>(R.string.key_stockAlarmClockEnabled,
            true);
    public static final PreferenceItem<Boolean> KEY_SHOW_TOAST_IN_BACKGROUND    = new SmartphonePreferenceItem<>(R.string.key_showBackgroundActionToast,
            true);
    public static final PreferenceItem<Boolean> KEY_SEND_ANONYMOUS_CRASH_DATA   = new SmartphonePreferenceItem<>(R.string.key_sendAnonymousCrashData,
            true);

    public static final PreferenceItem<Boolean> KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA = new SmartphonePreferenceItem<>(R.string.key_shouldAskSendAnonymousCrashData,
            true);

    public static final PreferenceItem<Integer> KEY_LOG_DESTINATION             = new SmartphonePreferenceItem<>(R.string.key_logDestination, 0);
    public static final PreferenceItem<Boolean> KEY_SHOW_GEOFENCE_NOTIFICATIONS = new SmartphonePreferenceItem<>(R.string.key_showGeofenceNotifications,
            true);
    public static final PreferenceItem<Boolean> KEY_SHOW_TIMER_NOTIFICATIONS    = new SmartphonePreferenceItem<>(R.string.key_showTimerNotifications,
            true);
    public static final PreferenceItem<Boolean> KEY_SHOULD_SHOW_WIZARD          = new SmartphonePreferenceItem<>(R.string.key_shouldShowWizard, true);
    public static final PreferenceItem<Integer> KEY_LAUNCHER_ICON               = new SmartphonePreferenceItem<>(R.string.key_launcher_icon, 0);

    DeveloperPreferencesHandler developerPreferencesHandler;

    @Inject
    public SmartphonePreferencesHandler(Context context, DeveloperPreferencesHandler developerPreferencesHandler) {
        super(context);
        this.developerPreferencesHandler = developerPreferencesHandler;
    }

    @Override
    @NonNull
    protected String getSharedPreferencesName() {
        return SettingsConstants.SHARED_PREFS_NAME;
    }

    @Override
    @NonNull
    public List<PreferenceItem> getAllPreferenceItems() {
        List<PreferenceItem> allPreferences = new ArrayList<>();
        allPreferences.add(GATEWAY_AUTO_DISCOVERY);
        allPreferences.add(BACKUP_PATH);
        allPreferences.add(STARTUP_DEFAULT_TAB);
        allPreferences.add(SHOW_ROOM_ALL_ON_OFF);
        allPreferences.add(HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        allPreferences.add(USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        allPreferences.add(KEY_AUTO_COLLAPSE_ROOMS);
        allPreferences.add(KEY_AUTO_COLLAPSE_TIMERS);
        allPreferences.add(KEY_THEME);
        allPreferences.add(KEY_USE_COMPACT_DRAWER);
        allPreferences.add(KEY_VIBRATE_ON_BUTTON_PRESS);
        allPreferences.add(KEY_VIBRATION_DURATION);
        allPreferences.add(KEY_CURRENT_APARTMENT_ID);
        allPreferences.add(KEEP_HISTORY_DURATION);
        allPreferences.add(KEY_SLEEP_AS_ANDROID_ENABLED);
        allPreferences.add(KEY_STOCK_ALARM_CLOCK_ENABLED);
        allPreferences.add(KEY_SHOW_TOAST_IN_BACKGROUND);
        allPreferences.add(KEY_SEND_ANONYMOUS_CRASH_DATA);
        allPreferences.add(KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA);
        allPreferences.add(KEY_LOG_DESTINATION);
        allPreferences.add(KEY_SHOW_GEOFENCE_NOTIFICATIONS);
        allPreferences.add(KEY_SHOW_TIMER_NOTIFICATIONS);
        allPreferences.add(KEY_SHOULD_SHOW_WIZARD);
        allPreferences.add(KEY_LAUNCHER_ICON);

        return allPreferences;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(@NonNull PreferenceItem<T> preferenceItem) throws ClassCastException {
        // special treatment for this key, to make playstore mode possible

        if (KEY_CURRENT_APARTMENT_ID.equals(preferenceItem)) {
            boolean playStoreModeEnabled = developerPreferencesHandler.getValue(DeveloperPreferencesHandler.PLAY_STORE_MODE);
            if (playStoreModeEnabled) {
                DemoModePersistenceHandler demoModePersistanceHandler = new DemoModePersistenceHandler(context);
                try {
                    Long value = demoModePersistanceHandler.getAllApartments()
                            .get(0)
                            .getId();
                    return (T) value;
                } catch (Exception e) {
                    throw new RuntimeException("Error fetching apartment id for demo mode");
                }
            }
        }

        return super.getValue(preferenceItem);
    }

    public String getPublicKeyString() {
        return SettingsConstants.KDH_SDSA + SettingsConstants.JKD_COAP + SettingsConstants.DJA_IOVJ + SettingsConstants.VOK_ZWEQ;
    }
}

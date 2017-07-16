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
import android.os.Environment;
import android.support.annotation.StringRes;

import java.io.File;
import java.util.NoSuchElementException;

import eu.power_switch.R;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import lombok.Getter;

/**
 * Created by Markus on 16.07.2017.
 */
@Getter
public enum SmartphonePreferenceItem {
    KEY_AUTO_DISCOVER(R.string.key_autodiscover, true),
    KEY_BACKUP_PATH(R.string.key_backupPath,
            Environment.getExternalStorageDirectory()
                    .getPath() + File.separator + BackupHandler.MAIN_BACKUP_FOLDERNAME),
    KEY_STARTUP_DEFAULT_TAB(R.string.key_startupDefaultTab, SettingsConstants.ROOMS_TAB_INDEX),
    KEY_SHOW_ROOM_ALL_ON_OFF(R.string.key_showRoomAllOnOff, true),
    KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON(R.string.key_highlightLastActivatedButton, false),
    KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB(R.string.key_useOptionsMenuInsteadOfFab, false),
    KEY_AUTO_COLLAPSE_ROOMS(R.string.key_autoCollapseRooms, false),
    KEY_AUTO_COLLAPSE_TIMERS(R.string.key_autoCollapseTimers, false),
    KEY_THEME(R.string.key_theme, SettingsConstants.THEME_DARK_BLUE),
    KEY_USE_COMPACT_DRAWER(R.string.key_useCompactDrawer, false),
    KEY_VIBRATE_ON_BUTTON_PRESS(R.string.key_vibrateOnButtonPress, true),
    KEY_VIBRATION_DURATION(R.string.key_vibrationDuration, SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK),
    KEY_CURRENT_APARTMENT_ID(R.string.key_currentApartmentId, SettingsConstants.INVALID_APARTMENT_ID),
    KeekpHistoryDuration(R.string.key_keepHistoryDuration, SettingsConstants.KEEP_HISTORY_FOREVER),
    KEY_SLEEP_AS_ANDROID_ENABLED(R.string.key_sleepAsAndroidEnabled, true),
    KEY_STOCK_ALARM_CLOCK_ENABLED(R.string.key_stockAlarmClockEnabled, true),
    KEY_SHOW_TOAST_IN_BACKGROUND(R.string.key_showBackgroundActionToast, true),
    KEY_SEND_ANONYMOUS_CRASH_DATA(R.string.key_sendAnonymousCrashData, true),
    KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA(R.string.key_shouldAskSendAnonymousCrashData, true),
    KEY_LOG_DESTINATION(R.string.key_logDestination, 0),
    KEY_SHOW_GEOFENCE_NOTIFICATIONS(R.string.key_showGeofenceNotifications, true),
    KEY_SHOW_TIMER_NOTIFICATIONS(R.string.key_showTimerNotifications, true),
    KEY_SHOULD_SHOW_WIZARD(R.string.key_shouldShowWizard, true),
    KEY_LAUNCHER_ICON(R.string.key_launcher_icon, 0);

    @StringRes
    private final int    keyRes;
    private final Class  type;
    private final Object defaultValue;

    <T> SmartphonePreferenceItem(@StringRes int keyRes, T defaultValue) {
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
     * @return SmartphonePreferenceItem
     */
    public static SmartphonePreferenceItem fromKey(Context context, String key) {
        for (SmartphonePreferenceItem preferenceItem : values()) {
            if (preferenceItem.getKey(context)
                    .equals(key)) {
                return preferenceItem;
            }
        }

        throw new NoSuchElementException("No SmartphonePreferenceItem found for key: " + key);
    }
}
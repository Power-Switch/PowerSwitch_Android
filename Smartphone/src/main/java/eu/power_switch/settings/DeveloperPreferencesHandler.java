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

import java.util.Locale;

import eu.power_switch.shared.constants.DeveloperSettingsConstants;
import eu.power_switch.shared.log.Log;

/**
 * Preference handler used to store developer settings
 */
public class DeveloperPreferencesHandler {

    private static SharedPreferences sharedPreferences;

    // cached values
    private static boolean playStoreModeCache;
    private static boolean forceLanguageCache;
    private static String localeCache;


    private DeveloperPreferencesHandler() {
    }

    public static void init(Context context) {
        if (sharedPreferences != null) {
            forceRefresh();
            return;
        }
        sharedPreferences = context.getSharedPreferences(
                DeveloperSettingsConstants.DEVELOPER_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        initCache();
    }

    /**
     * First time initialization of cached values
     */
    private static void initCache() {
        playStoreModeCache = sharedPreferences.getBoolean(DeveloperSettingsConstants.PLAY_STORE_MODE_KEY, false);
        forceLanguageCache = sharedPreferences.getBoolean(DeveloperSettingsConstants.FORCE_LANGUAGE_KEY, false);
        localeCache = sharedPreferences.getString(DeveloperSettingsConstants.LOCALE_KEY, Locale.GERMAN.toString());
    }

    /**
     * Forces an update of the cached values
     */
    public static void forceRefresh() {
        initCache();
    }

    /**
     * Retrieves setting for hidden Play Store Mode (used to take Screenshots)
     *
     * @return true if enabled
     */
    public static boolean getPlayStoreMode() {
        Log.d(DeveloperPreferencesHandler.class, "getPlayStoreMode: " + playStoreModeCache);
        return playStoreModeCache;
    }

    /**
     * Sets setting for hidden Play Store Mode (used to take Screenshots)
     *
     * @param bool true if enabled
     */
    public static void setPlayStoreMode(boolean bool) {
        Log.d(DeveloperPreferencesHandler.class, "setPlayStoreMode: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DeveloperSettingsConstants.PLAY_STORE_MODE_KEY, bool);
        editor.apply();

        playStoreModeCache = bool;
    }

    public static boolean getForceLanguage() {
        Log.d(DeveloperPreferencesHandler.class, "getForceLanguage: " + forceLanguageCache);
        return forceLanguageCache;
    }

    public static void setForceLanguage(boolean bool) {
        Log.d(DeveloperPreferencesHandler.class, "setForceLanguage: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DeveloperSettingsConstants.FORCE_LANGUAGE_KEY, bool);
        editor.apply();

        forceLanguageCache = bool;
    }

    public static Locale getLocale() {
        Log.d(DeveloperPreferencesHandler.class, "getLocale: " + localeCache);

        return new Locale(localeCache);
    }

    public static void setLocale(Locale locale) {
        Log.d(DeveloperPreferencesHandler.class, "setLocale: " + locale.toString());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DeveloperSettingsConstants.LOCALE_KEY, locale.toString());
        editor.apply();

        localeCache = locale.toString();
    }
}

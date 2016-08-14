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
    private static boolean forceFabricEnabledCache;
    private static String localeCache;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private DeveloperPreferencesHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable. Use static one time initialization via init() method instead.");
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
        playStoreModeCache = sharedPreferences.getBoolean(DeveloperSettingsConstants.KEY_PLAY_STORE_MODE, false);
        forceLanguageCache = sharedPreferences.getBoolean(DeveloperSettingsConstants.KEY_FORCE_LANGUAGE, false);
        forceFabricEnabledCache = sharedPreferences.getBoolean(DeveloperSettingsConstants.KEY_FORCE_ENABLE_FABRIC, false);
        localeCache = sharedPreferences.getString(DeveloperSettingsConstants.KEY_LOCALE, Locale.GERMAN.toString());

        Log.d(DeveloperPreferencesHandler.class, "PlayStoreMode: " + playStoreModeCache);
        Log.d(DeveloperPreferencesHandler.class, "ForceLanguage: " + forceLanguageCache);
        Log.d(DeveloperPreferencesHandler.class, "ForceEnableFabric: " + forceFabricEnabledCache);
        Log.d(DeveloperPreferencesHandler.class, "Locale: " + localeCache);
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
        editor.putBoolean(DeveloperSettingsConstants.KEY_PLAY_STORE_MODE, bool);
        editor.apply();

        playStoreModeCache = bool;
    }

    public static boolean getForceLanguage() {
        return forceLanguageCache;
    }

    public static void setForceLanguage(boolean bool) {
        Log.d(DeveloperPreferencesHandler.class, "setForceLanguage: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DeveloperSettingsConstants.KEY_FORCE_LANGUAGE, bool);
        editor.apply();

        forceLanguageCache = bool;
    }

    public static Locale getLocale() {
        return new Locale(localeCache);
    }

    public static void setLocale(Locale locale) {
        Log.d(DeveloperPreferencesHandler.class, "setLocale: " + locale.toString());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DeveloperSettingsConstants.KEY_LOCALE, locale.toString());
        editor.apply();

        localeCache = locale.toString();
    }

    /**
     * Retrieves setting to force enable Fabric
     *
     * @return true if enabled
     */
    public static boolean getForceFabricEnabled() {
        return forceFabricEnabledCache;
    }

    /**
     * Sets setting to force enable Fabric
     *
     * @param bool true if enabled
     */
    public static void setForceFabricEnabled(boolean bool) {
        Log.d(DeveloperPreferencesHandler.class, "setForceFabricEnabled: " + bool);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DeveloperSettingsConstants.KEY_FORCE_ENABLE_FABRIC, bool);
        editor.apply();

        forceFabricEnabledCache = bool;
    }
}

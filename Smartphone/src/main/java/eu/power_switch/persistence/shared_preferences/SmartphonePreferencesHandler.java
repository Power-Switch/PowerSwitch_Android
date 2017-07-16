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
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.persistence.demo_mode.DemoModePersistenceHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import timber.log.Timber;

import static eu.power_switch.persistence.shared_preferences.SmartphonePreferenceItem.KEY_CURRENT_APARTMENT_ID;

/**
 * Preference handler used to store general app settings
 */
@Singleton
public class SmartphonePreferencesHandler {

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
        for (SmartphonePreferenceItem preferenceItem : SmartphonePreferenceItem.values()) {
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
    public <T> T get(@NonNull SmartphonePreferenceItem preferenceItem) throws ClassCastException {
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
    public void set(@NonNull SmartphonePreferenceItem preferenceItem, @NonNull Object newValue) {
        String key = preferenceItem.getKey(context);

        // check if the passed in type matches the expected one
        if (!newValue.getClass()
                .isAssignableFrom(preferenceItem.getType())) {
            throw new IllegalArgumentException("Invalid type! Should be " + preferenceItem.getType()
                    .getCanonicalName() + " but was " + newValue.getClass()
                    .getCanonicalName());
        }

        Timber.d("setting new value \"" + newValue + "\" for key \"" + key + "\"");

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
            throw new IllegalArgumentException("Can't save objects of type " + newValue.getClass()
                    .getCanonicalName());
        }

        editor.apply();

        forceRefreshCache();
    }

}

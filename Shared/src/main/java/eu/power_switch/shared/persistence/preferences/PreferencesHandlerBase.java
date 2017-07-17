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

package eu.power_switch.shared.persistence.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Preference handler base class used to store general app settings
 */
public abstract class PreferencesHandlerBase {

    protected Context context;

    private SharedPreferences sharedPreferences;
    private Map<String, ?>    cachedValues;

    public PreferencesHandlerBase(Context context) {
        this.context = context;

        sharedPreferences = context.getSharedPreferences(getSharedPreferencesName(), Context.MODE_PRIVATE);
        forceRefreshCache();
    }

    /**
     * @return the name of the preferences (file) to use
     */
    @NonNull
    @CheckResult
    protected abstract String getSharedPreferencesName();

    /**
     * @return a list of all PreferenceItems used by this PreferenceHandler
     */
    @NonNull
    @CheckResult
    public abstract List<PreferenceItem> getAllPreferenceItems();

    @Nullable
    @CheckResult
    public PreferenceItem getPreferenceItem(@NonNull String key) {
        for (PreferenceItem preferenceItem : getAllPreferenceItems()) {
            if (preferenceItem.getKey(context)
                    .equals(key)) {
                return preferenceItem;
            }
        }
        return null;
    }

    /**
     * Forces an update of the cached values
     */
    public void forceRefreshCache() {
        cachedValues = sharedPreferences.getAll();
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
    @CallSuper
    public <T> T getValue(@NonNull PreferenceItem<T> preferenceItem) throws ClassCastException {
        String key = preferenceItem.getKey(context);

        T value = (T) cachedValues.get(key);

        // if no value was set, return preference default
        if (value == null) {
            value = preferenceItem.getDefaultValue();
            // save default value in file
            setValue(preferenceItem, value);
        }

        Timber.v("retrieving value \"" + value + "\" for key \"" + key + "\"");

        return value;
    }

    /**
     * Set a settings value by key
     *
     * @param preferenceItem the preference to set a new value for
     * @param newValue       new value
     */
    public <T> void setValue(@NonNull PreferenceItem<T> preferenceItem, @NonNull T newValue) {
        String key = preferenceItem.getKey(context);

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

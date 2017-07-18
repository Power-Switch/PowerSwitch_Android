/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.mikepenz.iconics.IconicsDrawable;

import de.markusressel.typedpreferences.PreferenceItem;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;

/**
 * Settings item used for settings list view
 * <p/>
 * Created by Markus on 08.06.2016.
 */
public abstract class SettingsItem<T> {

    protected Context                    context;
    private   String                     description;
    private   IconicsDrawable            icon;
    private   PreferenceItem<T>          preferenceItem;
    private   WearablePreferencesHandler wearablePreferencesHandler;

    /**
     * Constructor
     *
     * @param context
     * @param iconDrawable
     * @param description
     * @param preferenceItem
     * @param wearablePreferencesHandler
     */
    public SettingsItem(@NonNull Context context, @NonNull IconicsDrawable iconDrawable, @StringRes int description,
                        @NonNull PreferenceItem<T> preferenceItem, @NonNull WearablePreferencesHandler wearablePreferencesHandler) {
        this.context = context;
        this.icon = iconDrawable;
        this.description = context.getString(description);
        this.preferenceItem = preferenceItem;
        this.wearablePreferencesHandler = wearablePreferencesHandler;
    }

    /**
     * Get a short text description for this setting
     *
     * @return text description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the icon for this setting
     *
     * @return icon
     */
    public Drawable getIcon() {
        return icon;
    }

    /**
     * Get the current value of this SettingsItem
     *
     * @return value
     */
    public T getValue() {
        return wearablePreferencesHandler.getValue(preferenceItem);
    }

    /**
     * Set the new value of this SettingsItem
     *
     * @param newValue
     */
    public void setValue(T newValue) {
        wearablePreferencesHandler.setValue(preferenceItem, newValue);
    }

    /**
     * Get a description for the currently set value
     * <p/>
     * This method should always return something
     *
     * @return value description
     */
    public abstract String getCurrentValueDescription();

    /**
     * Get a description for the passed value
     * <p/>
     * This method should always return something
     *
     * @param value value to get description for
     *
     * @return value description
     */
    public abstract String getValueDescription(T value);
}

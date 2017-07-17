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

package eu.power_switch.gui;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import eu.power_switch.R;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import timber.log.Timber;

/**
 * Created by Markus on 07.03.2016.
 */
public class WearableThemeHelper {

    /**
     * Apply a Theme to an Activity
     *
     * @param activity Activity to apply theme on
     */
    public static void applyTheme(@Nullable Activity activity, @NonNull WearablePreferencesHandler wearablePreferencesHandler) {
        if (activity == null) {
            Timber.w("activity was null!");
            return;
        }

        int theme = wearablePreferencesHandler.getValue(WearablePreferencesHandler.THEME);
        switch (theme) {
            case SettingsConstants.THEME_DARK_BLUE:
                activity.setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
                activity.setTheme(R.style.PowerSwitchWearTheme_Dark_Red);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                activity.setTheme(R.style.PowerSwitchWearTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_RED:
                activity.setTheme(R.style.PowerSwitchWearTheme_Light_Red);
                break;
            default:
                activity.setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                break;
        }
    }
}

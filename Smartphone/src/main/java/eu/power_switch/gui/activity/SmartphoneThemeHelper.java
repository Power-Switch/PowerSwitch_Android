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

package eu.power_switch.gui.activity;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDelegate;

import eu.power_switch.R;
import eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;

import static eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler.PreferenceItem.KEY_THEME;

/**
 * Created by Markus on 25.07.2016.
 */
public class SmartphoneThemeHelper {

    /**
     * Apply a Theme to an Activity
     *
     * @param activity Activity to apply theme on
     */
    public static void applyTheme(Activity activity, SmartphonePreferencesHandler smartphonePreferencesHandler) {
        int theme = smartphonePreferencesHandler.get(KEY_THEME);
        switch (theme) {
            case SettingsConstants.THEME_DARK_BLUE:
                activity.getApplicationContext()
                        .setTheme(R.style.PowerSwitchTheme_Dark_Blue);
                activity.setTheme(R.style.PowerSwitchTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
                activity.getApplicationContext()
                        .setTheme(R.style.PowerSwitchTheme_Dark_Red);
                activity.setTheme(R.style.PowerSwitchTheme_Dark_Red);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                activity.getApplicationContext()
                        .setTheme(R.style.PowerSwitchTheme_Light_Blue);
                activity.setTheme(R.style.PowerSwitchTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_RED:
                activity.getApplicationContext()
                        .setTheme(R.style.PowerSwitchTheme_Light_Red);
                activity.setTheme(R.style.PowerSwitchTheme_Light_Red);
                break;
            case SettingsConstants.THEME_DAY_NIGHT_BLUE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);

                activity.getApplicationContext()
                        .setTheme(R.style.PowerSwitchTheme_DayNight_Blue);
                activity.setTheme(R.style.PowerSwitchTheme_DayNight_Blue);
                break;
            default:
                activity.setTheme(R.style.PowerSwitchTheme_Dark_Blue);
                break;
        }
    }

    /**
     * Apply a Theme to an Activity
     *
     * @param activity Activity to apply theme on
     */
    public static void applyDialogTheme(Activity activity, SmartphonePreferencesHandler smartphonePreferencesHandler) {
        int theme = smartphonePreferencesHandler.get(KEY_THEME);
        switch (theme) {
            case SettingsConstants.THEME_DARK_BLUE:
                activity.getApplicationContext()
                        .setTheme(R.style.PowerSwitchDialogTheme_Dark_Blue);
                activity.setTheme(R.style.PowerSwitchDialogTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                activity.getApplicationContext()
                        .setTheme(R.style.PowerSwitchDialogTheme_Light_Blue);
                activity.setTheme(R.style.PowerSwitchDialogTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
//                activity.getApplicationContext().setTheme(R.style.PowerSwitchTheme_Dark_Red);
//                activity.setTheme(R.style.PowerSwitchTheme_Dark_Red);
//                break;
            case SettingsConstants.THEME_LIGHT_RED:
//                activity.getApplicationContext().setTheme(R.style.PowerSwitchTheme_Light_Red);
//                activity.setTheme(R.style.PowerSwitchTheme_Light_Red);
//                break;
            case SettingsConstants.THEME_DAY_NIGHT_BLUE:
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
//
//                activity.getApplicationContext().setTheme(R.style.PowerSwitchTheme_DayNight_Blue);
//                activity.setTheme(R.style.PowerSwitchTheme_DayNight_Blue);
//                break;
            default:
                activity.setTheme(R.style.PowerSwitchDialogTheme_Dark_Blue);
                break;
        }
    }

    /**
     * Apply a Theme to a Fragment
     *
     * @param dialogFragment Fragment to apply theme on
     */
    public static void applyDialogTheme(DialogFragment dialogFragment, SmartphonePreferencesHandler smartphonePreferencesHandler) {
        int theme = smartphonePreferencesHandler.get(KEY_THEME);
        switch (theme) {
            case SettingsConstants.THEME_DARK_BLUE:
                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.PowerSwitchDialogTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.PowerSwitchDialogTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
//                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.PowerSwitchDialogTheme_Light_Blue);
//                break;
            case SettingsConstants.THEME_LIGHT_RED:
//                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.PowerSwitchDialogTheme_Light_Blue);
//                break;
            case SettingsConstants.THEME_DAY_NIGHT_BLUE:
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
//
//                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.PowerSwitchDialogTheme_Light_Blue);
//                break;
            default:
                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.PowerSwitchDialogTheme_Dark_Blue);
                break;
        }
    }

}

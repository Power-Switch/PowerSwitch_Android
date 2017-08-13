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
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.application.PowerSwitch;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_THEME;

/**
 * Created by Markus on 25.07.2016.
 */
@Singleton
public class SmartphoneThemeHelper {

    @Inject
    SmartphonePreferencesHandler smartphonePreferencesHandler;

    @Inject
    public SmartphoneThemeHelper() {
    }

    /**
     * Apply a Theme to an Activity
     *
     * @param activity Activity to apply theme on
     */
    public void applyTheme(Activity activity) {
        int theme = smartphonePreferencesHandler.getValue(KEY_THEME);
        switch (theme) {
            case SettingsConstants.THEME_DARK_BLUE:
                setTheme(activity, R.style.PowerSwitchTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
                setTheme(activity, R.style.PowerSwitchTheme_Dark_Red);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                setTheme(activity, R.style.PowerSwitchTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_RED:
                setTheme(activity, R.style.PowerSwitchTheme_Light_Red);
                break;
            case SettingsConstants.THEME_DAY_NIGHT_BLUE:
                // Day/Night mode not working yet

//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
//                setTheme(activity, R.style.PowerSwitchTheme_DayNight_Blue);

                if (PowerSwitch.isNightModeActive(smartphonePreferencesHandler)) {
                    setTheme(activity, R.style.PowerSwitchTheme_Dark_Blue);
                } else {
                    setTheme(activity, R.style.PowerSwitchTheme_Light_Blue);
                }

                break;
            default:
                setTheme(activity, R.style.PowerSwitchTheme_Dark_Blue);
                break;
        }
    }

    /**
     * Apply a Theme to an Activity
     *
     * @param activity Activity to apply theme on
     */
    public void applyDialogTheme(Activity activity) {
        int theme = smartphonePreferencesHandler.getValue(KEY_THEME);
        switch (theme) {
            case SettingsConstants.THEME_DARK_BLUE:
                setTheme(activity, R.style.PowerSwitchDialogTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                setTheme(activity, R.style.PowerSwitchDialogTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
//                setTheme(activity, R.style.PowerSwitchTheme_Dark_Red);
//                break;
            case SettingsConstants.THEME_LIGHT_RED:
//                setTheme(activity, R.style.PowerSwitchTheme_Light_Red);
//                break;
            case SettingsConstants.THEME_DAY_NIGHT_BLUE:
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
//                setTheme(activity, R.style.PowerSwitchTheme_DayNight_Blue);
//
                if (PowerSwitch.isNightModeActive(smartphonePreferencesHandler)) {
                    setTheme(activity, R.style.PowerSwitchDialogTheme_Dark_Blue);
                } else {
                    setTheme(activity, R.style.PowerSwitchDialogTheme_Light_Blue);
                }

                break;
            default:
                setTheme(activity, R.style.PowerSwitchDialogTheme_Dark_Blue);
                break;
        }
    }

    private void setTheme(Activity activity, @StyleRes int themeRes) {
        activity.getApplicationContext()
                .setTheme(themeRes);
        activity.setTheme(themeRes);
    }

    /**
     * Apply a Theme to a Fragment
     *
     * @param dialogFragment Fragment to apply theme on
     */
    public void applyDialogTheme(DialogFragment dialogFragment) {
        int theme = smartphonePreferencesHandler.getValue(KEY_THEME);
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

                if (PowerSwitch.isNightModeActive(smartphonePreferencesHandler)) {
                    dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.PowerSwitchDialogTheme_Dark_Blue);
                } else {
                    dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.PowerSwitchDialogTheme_Light_Blue);
                }

                break;
            default:
                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.PowerSwitchDialogTheme_Dark_Blue);
                break;
        }
    }

}

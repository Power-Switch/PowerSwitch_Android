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

import eu.power_switch.shared.constants.SettingsConstants;

/**
 * This class is responsible for accessing and modifying Wear App Settings
 * <p/>
 * Note: Most (if not all) Settings can not be changed on the Wearable itself but only using the Smartphone
 * <p/>
 * Created by Markus on 11.11.2015.
 */
public class SharedPreferencesHandler {

    SharedPreferences sharedPreferences;

    public SharedPreferencesHandler(Context context) {
        sharedPreferences = context.getSharedPreferences(SettingsConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Retrieves setting for current Wear Theme
     *
     * @return ID (internal) of Wear Theme
     */
    public int getWearTheme() {
        int value = sharedPreferences.getInt(SettingsConstants.WEAR_THEME_KEY, SettingsConstants.THEME_DARK_BLUE);
        return value;
    }

    /**
     * Sets setting for current Wear Theme
     *
     * @param theme ID (internal) of Wear Theme
     */
    public void setWearTheme(int theme) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SettingsConstants.WEAR_THEME_KEY, theme);
        editor.apply();
    }

    /**
     * Retrieves setting for vibration feedback
     *
     * @return true if enabled
     */
    public boolean getVibrateOnButtonPress() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, true);
        return value;
    }

    /**
     * Sets setting for vibration feedback
     *
     * @param bool true if enabled
     */
    public void setVibrateOnButtonPress(boolean bool) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, bool);
        editor.apply();
    }

    /**
     * Retrieves setting for highlighting last activated button
     *
     * @return true if enabled
     */
    public boolean getHighlightLastActivatedButton() {
        boolean value = sharedPreferences.getBoolean(SettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, false);
        return value;
    }

    /**
     * Sets setting for highlighting last activated button
     *
     * @param bool true if enabled
     */
    public void setHighlightLastActivatedButton(boolean bool) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, bool);
        editor.apply();
    }

}

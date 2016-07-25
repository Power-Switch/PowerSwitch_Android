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

package eu.power_switch.shared.wearable;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;

import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Created by Markus on 25.07.2016.
 */
public class CommunicationHelper {

    /**
     * This method extracts settings data contained in a DataMap Array and saves it into the local PreferenceHandler.
     *
     * @param settings received settings data
     */
    public static void extractSettings(ArrayList<DataMap> settings) {
        // save map values to local preferenceHandler
        for (DataMap dataMapItem : settings) {
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB)) {
                int value = dataMapItem.getInt(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB, value);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS)) {
                boolean bool = dataMapItem.getBoolean(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS, bool);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
                boolean bool = dataMapItem.getBoolean(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, bool);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF)) {
                boolean bool = dataMapItem.getBoolean(WearablePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF, bool);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_THEME)) {
                int value = dataMapItem.getInt(WearablePreferencesHandler.KEY_THEME);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_THEME, value);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS)) {
                boolean bool = dataMapItem.getBoolean(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS, bool);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_VIBRATION_DURATION)) {
                int value = dataMapItem.getInt(WearablePreferencesHandler.KEY_VIBRATION_DURATION);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_VIBRATION_DURATION, value);
            }
        }
    }

    /**
     * Get Wearable settings and put them into a DataMap
     *
     * @return DataMap containing all Wearable settings
     */
    public static DataMap getSettingsDataMap() {
        DataMap settingsDataMap = new DataMap();
        settingsDataMap.putInt(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB,
                WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB));
        settingsDataMap.putBoolean(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS,
                WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS));
        settingsDataMap.putBoolean(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON,
                WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON));
        settingsDataMap.putBoolean(WearablePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF,
                WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF));
        settingsDataMap.putInt(WearablePreferencesHandler.KEY_THEME,
                WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_THEME));
        settingsDataMap.putBoolean(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS,
                WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS));
        settingsDataMap.putInt(WearablePreferencesHandler.KEY_VIBRATION_DURATION,
                WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_VIBRATION_DURATION));

        return settingsDataMap;
    }


}

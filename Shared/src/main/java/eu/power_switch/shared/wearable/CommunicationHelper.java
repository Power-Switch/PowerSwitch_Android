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

import android.content.Context;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;

import eu.power_switch.shared.persistence.preferences.PreferenceItem;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Created by Markus on 25.07.2016.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommunicationHelper {

    /**
     * This method extracts settings data contained in a DataMap Array and saves it into the local PreferenceHandler.
     *
     * @param settings received settings data
     */
    public static void extractSettings(Context context, WearablePreferencesHandler wearablePreferencesHandler, ArrayList<DataMap> settings) {
        // save map values to local preferenceHandler
        PreferenceItem preferenceItem;
        String         key;
        for (DataMap dataMapItem : settings) {
            // TODO: a lot of redundancy here, should be cleaned up

            preferenceItem = WearablePreferencesHandler.STARTUP_DEFAULT_TAB;
            key = preferenceItem.getKey(context);
            if (dataMapItem.containsKey(key)) {
                int value = dataMapItem.getInt(key);
                wearablePreferencesHandler.setValue(preferenceItem, value);
            }

            preferenceItem = WearablePreferencesHandler.AUTO_COLLAPSE_ROOMS;
            key = preferenceItem.getKey(context);
            if (dataMapItem.containsKey(key)) {
                boolean value = dataMapItem.getBoolean(key);
                wearablePreferencesHandler.setValue(preferenceItem, value);
            }

            preferenceItem = WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON;
            key = preferenceItem.getKey(context);
            if (dataMapItem.containsKey(key)) {
                boolean value = dataMapItem.getBoolean(key);
                wearablePreferencesHandler.setValue(preferenceItem, value);
            }

            preferenceItem = WearablePreferencesHandler.SHOW_ROOM_ALL_ON_OFF;
            key = preferenceItem.getKey(context);
            if (dataMapItem.containsKey(key)) {
                boolean value = dataMapItem.getBoolean(key);
                wearablePreferencesHandler.setValue(preferenceItem, value);
            }

            preferenceItem = WearablePreferencesHandler.THEME;
            key = preferenceItem.getKey(context);
            if (dataMapItem.containsKey(key)) {
                int value = dataMapItem.getInt(key);
                wearablePreferencesHandler.setValue(preferenceItem, value);
            }

            preferenceItem = WearablePreferencesHandler.VIBRATE_ON_BUTTON_PRESS;
            key = preferenceItem.getKey(context);
            if (dataMapItem.containsKey(key)) {
                boolean value = dataMapItem.getBoolean(key);
                wearablePreferencesHandler.setValue(preferenceItem, value);
            }

            preferenceItem = WearablePreferencesHandler.VIBRATION_DURATION;
            key = preferenceItem.getKey(context);
            if (dataMapItem.containsKey(key)) {
                int value = dataMapItem.getInt(key);
                wearablePreferencesHandler.setValue(preferenceItem, value);
            }
        }
    }

    /**
     * Get Wearable settings and put them into a DataMap
     *
     * @return DataMap containing all Wearable settings
     */
    public static DataMap getSettingsDataMap(Context context, WearablePreferencesHandler wearablePreferencesHandler) {
        DataMap settingsDataMap = new DataMap();

        for (PreferenceItem preferenceItem : wearablePreferencesHandler.getAllPreferenceItems()) {
            Object value = wearablePreferencesHandler.getValue(preferenceItem);

            if (value instanceof Boolean) {
                settingsDataMap.putBoolean(preferenceItem.getKey(context), (boolean) value);
            } else if (value instanceof Integer) {
                settingsDataMap.putInt(preferenceItem.getKey(context), (int) value);
            }
        }

        return settingsDataMap;
    }


}

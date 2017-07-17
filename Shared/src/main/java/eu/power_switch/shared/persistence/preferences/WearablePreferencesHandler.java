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
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.shared.R;
import eu.power_switch.shared.constants.SettingsConstants;

/**
 * This class is responsible for accessing and modifying Wear App Settings
 * <p/>
 * Note: Most (if not all) Settings can not be changed on the Wearable itself but only using the Smartphone
 * <p/>
 * <p/>
 * Created by Markus on 13.11.2015.
 */
public class WearablePreferencesHandler extends PreferencesHandlerBase {

    // SharedPreferences
    public static final String WEARABLE_SHARED_PREFS_NAME = "eu.power_switch.wearable.prefs";

    public static final PreferenceItem<Boolean> SHOW_ROOM_ALL_ON_OFF            = new WearablePreferenceItem<>(R.string.key_showRoomAllOnOff, true);
    public static final PreferenceItem<Boolean> HIGHLIGHT_LAST_ACTIVATED_BUTTON = new WearablePreferenceItem<>(R.string.key_highlightLastActivatedButton,
            false);
    public static final PreferenceItem<Boolean> AUTO_COLLAPSE_ROOMS             = new WearablePreferenceItem<>(R.string.key_autoCollapseRooms, false);
    public static final PreferenceItem<Integer> THEME                           = new WearablePreferenceItem<>(R.string.key_theme,
            SettingsConstants.THEME_DARK_BLUE);
    public static final PreferenceItem<Integer> STARTUP_DEFAULT_TAB             = new WearablePreferenceItem<>(R.string.key_startupDefaultTab, 0);
    public static final PreferenceItem<Boolean> VIBRATE_ON_BUTTON_PRESS         = new WearablePreferenceItem<>(R.string.key_vibrateOnButtonPress,
            true);
    public static final PreferenceItem<Integer> VIBRATION_DURATION              = new WearablePreferenceItem<>(R.string.key_vibrationDuration,
            SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK);

    public WearablePreferencesHandler(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected String getSharedPreferencesName() {
        return WEARABLE_SHARED_PREFS_NAME;
    }

    @NonNull
    @Override
    public List<PreferenceItem> getAllPreferenceItems() {
        List<PreferenceItem> allPreferenceItems = new ArrayList<>();

        allPreferenceItems.add(SHOW_ROOM_ALL_ON_OFF);
        allPreferenceItems.add(HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        allPreferenceItems.add(AUTO_COLLAPSE_ROOMS);
        allPreferenceItems.add(THEME);
        allPreferenceItems.add(STARTUP_DEFAULT_TAB);
        allPreferenceItems.add(VIBRATE_ON_BUTTON_PRESS);
        allPreferenceItems.add(VIBRATION_DURATION);

        return allPreferenceItems;
    }

}

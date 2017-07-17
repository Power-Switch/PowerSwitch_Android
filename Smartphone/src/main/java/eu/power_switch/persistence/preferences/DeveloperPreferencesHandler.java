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

package eu.power_switch.persistence.preferences;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.shared.constants.DeveloperSettingsConstants;
import eu.power_switch.shared.persistence.preferences.PreferenceItem;
import eu.power_switch.shared.persistence.preferences.PreferencesHandlerBase;

/**
 * Preference handler used to store developer settings
 */
@Singleton
public class DeveloperPreferencesHandler extends PreferencesHandlerBase {

    public static final PreferenceItem<Boolean> PLAY_STORE_MODE     = new DeveloperPreferenceItem<>(R.string.key_playStoreMode, false);
    public static final PreferenceItem<Boolean> FORCE_ENABLE_FABRIC = new DeveloperPreferenceItem<>(R.string.key_forceEnableFabric, false);
    public static final PreferenceItem<Boolean> FORCE_LANGUAGE      = new DeveloperPreferenceItem<>(R.string.key_forceLanguage, false);
    public static final PreferenceItem<String>  LOCALE              = new DeveloperPreferenceItem<>(R.string.key_locale, Locale.GERMAN.toString());

    @Inject
    public DeveloperPreferencesHandler(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected String getSharedPreferencesName() {
        return DeveloperSettingsConstants.DEVELOPER_SHARED_PREFS_NAME;
    }

    @NonNull
    @Override
    public List<PreferenceItem> getAllPreferenceItems() {
        List<PreferenceItem> allPreferenceItems = new ArrayList<>();

        allPreferenceItems.add(PLAY_STORE_MODE);
        allPreferenceItems.add(FORCE_ENABLE_FABRIC);
        allPreferenceItems.add(FORCE_LANGUAGE);
        allPreferenceItems.add(LOCALE);

        return allPreferenceItems;
    }

}

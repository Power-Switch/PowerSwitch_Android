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

package eu.power_switch.gui.fragment.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import eu.power_switch.R;
import eu.power_switch.event.WearableSettingsChangedEvent;
import eu.power_switch.settings.IntListPreference;
import eu.power_switch.settings.SliderPreference;
import eu.power_switch.settings.SliderPreferenceFragmentCompat;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import eu.power_switch.wear.service.UtilityService;

/**
 * Created by Markus on 31.07.2016.
 */
public class WearableSettingsPreferenceFragment extends EventBusPreferenceFragment {

    private IntListPreference startupDefaultTab;
    private SwitchPreference  autoCollapseRooms;
    private SwitchPreference  highlightLastActivatedButton;
    private SwitchPreference  vibrateOnButtonPress;
    private SliderPreference  vibrationDuration;
    private IntListPreference theme;

    @Inject
    WearablePreferencesHandler wearablePreferencesHandler;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // set preferences file name
        getPreferenceManager().setSharedPreferencesName(WearablePreferencesHandler.WEARABLE_SHARED_PREFS_NAME);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_wearable);

        initializePreferenceItems();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onWearableSettingsChanged(WearableSettingsChangedEvent wearableSettingsChangedEvent) {
        updateUI();
    }

    private void initializePreferenceItems() {
        startupDefaultTab = (IntListPreference) findPreference(WearablePreferencesHandler.STARTUP_DEFAULT_TAB.getKey(getContext()));
        startupDefaultTab.setDefaultValue(WearablePreferencesHandler.STARTUP_DEFAULT_TAB.getDefaultValue());

        autoCollapseRooms = (SwitchPreference) findPreference(WearablePreferencesHandler.AUTO_COLLAPSE_ROOMS.getKey(getContext()));
        autoCollapseRooms.setDefaultValue(WearablePreferencesHandler.AUTO_COLLAPSE_ROOMS.getDefaultValue());
        autoCollapseRooms.setSummaryOn(R.string.summary_autoCollapseRooms_enabled);
        autoCollapseRooms.setSummaryOff(R.string.summary_autoCollapseRooms_disabled);

        highlightLastActivatedButton = (SwitchPreference) findPreference(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON.getKey(getContext()));
        highlightLastActivatedButton.setDefaultValue(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON.getDefaultValue());
        highlightLastActivatedButton.setSummaryOn(R.string.summary_highlightLastActivatedButton_enabled);
        highlightLastActivatedButton.setSummaryOff(R.string.summary_highlightLastActivatedButton_disabled);

        vibrateOnButtonPress = (SwitchPreference) findPreference(WearablePreferencesHandler.VIBRATE_ON_BUTTON_PRESS.getKey(getContext()));
        vibrateOnButtonPress.setDefaultValue(WearablePreferencesHandler.VIBRATE_ON_BUTTON_PRESS.getDefaultValue());
        vibrateOnButtonPress.setSummaryOn(R.string.summary_vibrateOnButtonPress_enabled);
        vibrateOnButtonPress.setSummaryOff(R.string.summary_vibrateOnButtonPress_disabled);

        vibrationDuration = (SliderPreference) findPreference(WearablePreferencesHandler.VIBRATION_DURATION.getKey(getContext()));
        vibrationDuration.setDefaultValue(WearablePreferencesHandler.VIBRATION_DURATION.getDefaultValue());

        theme = (IntListPreference) findPreference(WearablePreferencesHandler.THEME.getKey(getContext()));
        theme.setDefaultValue(WearablePreferencesHandler.THEME.getDefaultValue());

        updateUI();
    }

    private void updateUI() {

        int startupDefaultTabIndex = wearablePreferencesHandler.getValue(WearablePreferencesHandler.STARTUP_DEFAULT_TAB);
        startupDefaultTab.setValueIndex(startupDefaultTabIndex);
        String[] mainTabNames = getResources().getStringArray(R.array.wear_tab_names);
        startupDefaultTab.setSummary(mainTabNames[startupDefaultTabIndex]);

        boolean autoCollapseRoomsEnabled = wearablePreferencesHandler.getValue(WearablePreferencesHandler.AUTO_COLLAPSE_ROOMS);
        autoCollapseRooms.setChecked(autoCollapseRoomsEnabled);

        boolean highlightLastActivatedButtonEnabled = wearablePreferencesHandler.getValue(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        highlightLastActivatedButton.setChecked(highlightLastActivatedButtonEnabled);

        boolean vibrateOnButtonPressEnabled = wearablePreferencesHandler.getValue(WearablePreferencesHandler.VIBRATE_ON_BUTTON_PRESS);
        vibrateOnButtonPress.setChecked(vibrateOnButtonPressEnabled);

        int vibrationDurationMs = wearablePreferencesHandler.getValue(WearablePreferencesHandler.VIBRATION_DURATION);
        vibrationDuration.setSummary(vibrationDurationMs + " ms");

        int themeIndex = wearablePreferencesHandler.getValue(WearablePreferencesHandler.THEME);
        theme.setValueIndex(themeIndex);
        String[] themeNames = getResources().getStringArray(R.array.theme_names_wear);
        theme.setSummary(themeNames[themeIndex]);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        wearablePreferencesHandler.forceRefreshCache();

        updateUI();

        // sync settings with wearable app
        UtilityService.forceWearSettingsUpdate(getContext());
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment;
        if (preference instanceof SliderPreference) {
            fragment = SliderPreferenceFragmentCompat.newInstance(preference.getKey());
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else
            super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }
}

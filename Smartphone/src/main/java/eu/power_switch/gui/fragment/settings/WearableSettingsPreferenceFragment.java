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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import eu.power_switch.R;
import eu.power_switch.settings.IntListPreference;
import eu.power_switch.settings.SliderPreference;
import eu.power_switch.settings.SliderPreferenceFragmentCompat;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.settings.WearablePreferencesHandler;
import eu.power_switch.wear.service.UtilityService;
import timber.log.Timber;

/**
 * Created by Markus on 31.07.2016.
 */
public class WearableSettingsPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private IntListPreference startupDefaultTab;
    private SwitchPreference autoCollapseRooms;
    private SwitchPreference highlightLastActivatedButton;
    private SwitchPreference vibrateOnButtonPress;
    private SliderPreference vibrationDuration;
    private IntListPreference theme;

    private BroadcastReceiver broadcastReceiver;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // set preferences file name
        getPreferenceManager().setSharedPreferencesName(WearablePreferencesHandler.WEARABLE_SHARED_PREFS_NAME);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_wearable);

        initializePreferenceItems();

        // Listen for preference item actions
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("received intent: " + intent.getAction());

                updateUI();
            }
        };
    }

    private void initializePreferenceItems() {

        startupDefaultTab = (IntListPreference) findPreference(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB);
        startupDefaultTab.setDefaultValue(WearablePreferencesHandler.DEFAULT_VALUE_STARTUP_TAB);
        String[] mainTabNames = getResources().getStringArray(R.array.wear_tab_names);
        startupDefaultTab.setSummary(mainTabNames[WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB)]);

        autoCollapseRooms = (SwitchPreference) findPreference(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS);
        autoCollapseRooms.setDefaultValue(WearablePreferencesHandler.DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS);
        autoCollapseRooms.setSummaryOn(R.string.summary_autoCollapseRooms_enabled);
        autoCollapseRooms.setSummaryOff(R.string.summary_autoCollapseRooms_disabled);

        highlightLastActivatedButton = (SwitchPreference) findPreference(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        highlightLastActivatedButton.setDefaultValue(WearablePreferencesHandler.DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        highlightLastActivatedButton.setSummaryOn(R.string.summary_highlightLastActivatedButton_enabled);
        highlightLastActivatedButton.setSummaryOff(R.string.summary_highlightLastActivatedButton_disabled);

        vibrateOnButtonPress = (SwitchPreference) findPreference(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS);
        vibrateOnButtonPress.setDefaultValue(WearablePreferencesHandler.DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS);
        vibrateOnButtonPress.setSummaryOn(R.string.summary_vibrateOnButtonPress_enabled);
        vibrateOnButtonPress.setSummaryOff(R.string.summary_vibrateOnButtonPress_disabled);

        vibrationDuration = (SliderPreference) findPreference(WearablePreferencesHandler.KEY_VIBRATION_DURATION);
        vibrationDuration.setDefaultValue(WearablePreferencesHandler.DEFAULT_VALUE_VIBRATION_DURATION);
        vibrationDuration.setSummary(WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_VIBRATION_DURATION) + " ms");

        theme = (IntListPreference) findPreference(WearablePreferencesHandler.KEY_THEME);
        theme.setDefaultValue(WearablePreferencesHandler.DEFAULT_VALUE_THEME);
        String[] themeNames = getResources().getStringArray(R.array.theme_names_wear);
        theme.setSummary(themeNames[WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_THEME)]);

    }

    private void updateUI() {
        startupDefaultTab.setValueIndex(WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB));
        String[] mainTabNames = getResources().getStringArray(R.array.wear_tab_names);
        startupDefaultTab.setSummary(mainTabNames[WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB)]);

        autoCollapseRooms.setChecked(WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS));
        highlightLastActivatedButton.setChecked(WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON));
        vibrateOnButtonPress.setChecked(WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS));
        vibrationDuration.setSummary(WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_VIBRATION_DURATION) + " ms");

        theme.setValueIndex(WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_THEME));
        String[] themeNames = getResources().getStringArray(R.array.theme_names_wear);
        theme.setSummary(themeNames[WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_THEME)]);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        WearablePreferencesHandler.forceRefresh();

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
            fragment.show(getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_WEARABLE_SETTINGS_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);

        super.onPause();
    }
}

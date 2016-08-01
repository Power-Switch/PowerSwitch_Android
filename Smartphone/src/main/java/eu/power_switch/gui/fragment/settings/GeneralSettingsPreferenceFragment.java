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

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.PathChooserDialog;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.settings.IntListPreference;
import eu.power_switch.settings.SliderPreference;
import eu.power_switch.settings.SliderPreferenceFragmentCompat;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.exception.permission.MissingPermissionException;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;
import eu.power_switch.shared.permission.PermissionHelper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Created by Markus on 31.07.2016.
 */
public class GeneralSettingsPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private IntListPreference startupDefaultTab;
    private SwitchPreference autodiscover;
    private SwitchPreference autoCollapseRooms;
    private SwitchPreference autoCollapseTimers;
    private SwitchPreference showRoomOnOff;
    private SwitchPreference hideFab;
    private SwitchPreference highlightLastActivatedButton;
    private SwitchPreference showBackgroundActionToast;
    private SwitchPreference vibrateOnButtonPress;
    private SliderPreference vibrationDuration;
    private IntListPreference keepHistoryDuration;
    private Preference backupPath;
    private IntListPreference theme;
    private Preference resetTutial;
    private SwitchPreference sendAnonymousCrashData;
    private Preference sendLogsEmail;

    private BroadcastReceiver broadcastReceiver;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // set preferences file name
        getPreferenceManager().setSharedPreferencesName(SettingsConstants.SHARED_PREFS_NAME);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_general);

        initializePreferenceItems();

        // Listen for preference item actions
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());

                if (LocalBroadcastConstants.INTENT_BACKUP_PATH_CHANGED.equals(intent.getAction())) {
                    backupPath.setSummary(SmartphonePreferencesHandler.<String>get(SmartphonePreferencesHandler.KEY_BACKUP_PATH));
                }
            }
        };
    }

    private void initializePreferenceItems() {

        startupDefaultTab = (IntListPreference) findPreference(SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB);
        startupDefaultTab.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_STARTUP_TAB);
        String[] mainTabNames = getResources().getStringArray(R.array.main_tab_names);
        startupDefaultTab.setSummary(mainTabNames[SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB)]);

        autodiscover = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_AUTO_DISCOVER);
        autodiscover.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_AUTO_DISCOVER);
        autodiscover.setSummaryOn(R.string.summary_autodiscover_enabled);
        autodiscover.setSummaryOff(R.string.summary_autodiscover_disabled);

        autoCollapseRooms = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS);
        autoCollapseRooms.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS);
        autoCollapseRooms.setSummaryOn(R.string.summary_autoCollapseRooms_enabled);
        autoCollapseRooms.setSummaryOff(R.string.summary_autoCollapseRooms_disabled);

        autoCollapseTimers = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_TIMERS);
        autoCollapseTimers.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_AUTO_COLLAPSE_TIMERS);
        autoCollapseTimers.setSummaryOn(R.string.summary_autoCollapseTimers_enabled);
        autoCollapseTimers.setSummaryOff(R.string.summary_autoCollapseTimers_disabled);

        showRoomOnOff = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF);
        showRoomOnOff.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF);
        showRoomOnOff.setSummaryOn(R.string.summary_showRoomAllOnOff_enabled);
        showRoomOnOff.setSummaryOff(R.string.summary_showRoomAllOnOff_disabled);

        hideFab = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        hideFab.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        hideFab.setSummaryOn(R.string.summary_useOptionsMenuInsteadOfFab_enabled);
        hideFab.setSummaryOff(R.string.summary_useOptionsMenuInsteadOfFab_disabled);

        highlightLastActivatedButton = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        highlightLastActivatedButton.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        highlightLastActivatedButton.setSummaryOn(R.string.summary_highlightLastActivatedButton_enabled);
        highlightLastActivatedButton.setSummaryOff(R.string.summary_highlightLastActivatedButton_disabled);

        showBackgroundActionToast = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SHOW_TOAST_IN_BACKGROUND);
        showBackgroundActionToast.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_SHOW_TOAST_IN_BACKGROUND);
        showBackgroundActionToast.setSummaryOn(R.string.summary_showBackgroundActionToast_enabled);
        showBackgroundActionToast.setSummaryOff(R.string.summary_showBackgroundActionToast_disabled);

        vibrateOnButtonPress = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS);
        vibrateOnButtonPress.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS);
        vibrateOnButtonPress.setSummaryOn(R.string.summary_vibrateOnButtonPress_enabled);
        vibrateOnButtonPress.setSummaryOff(R.string.summary_vibrateOnButtonPress_disabled);

        vibrationDuration = (SliderPreference) findPreference(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION);
        vibrationDuration.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_VIBRATION_DURATION);
        vibrationDuration.setSummary(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION) + " ms");

        keepHistoryDuration = (IntListPreference) findPreference(SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION);
        keepHistoryDuration.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_KEEP_HISTORY_DURATION);
        String[] historyValues = getResources().getStringArray(R.array.entries_history);
        keepHistoryDuration.setSummary(historyValues[SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION)]);

        final Fragment fragment = this;
        backupPath = findPreference(SmartphonePreferencesHandler.KEY_BACKUP_PATH);
        backupPath.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_BACKUP_PATH);
        backupPath.setSummary(SmartphonePreferencesHandler.<String>get(SmartphonePreferencesHandler.KEY_BACKUP_PATH));
        backupPath.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // open edit dialog
                if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(getActivity())) {
                    Snackbar snackbar = Snackbar.make(getListView(), R.string.missing_external_storage_permission, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.grant, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.getActivity(), new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION);
                        }
                    });
                    snackbar.show();
                    return true;
                }

                PathChooserDialog pathChooserDialog = PathChooserDialog.newInstance();
                pathChooserDialog.setTargetFragment(fragment, 0);
                pathChooserDialog.show(getActivity().getSupportFragmentManager(), null);

                return true;
            }
        });

        theme = (IntListPreference) findPreference(SmartphonePreferencesHandler.KEY_THEME);
        theme.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_THEME);
        String[] themeNames = getResources().getStringArray(R.array.theme_names);
        theme.setSummary(themeNames[SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_THEME)]);

        resetTutial = findPreference(getString(R.string.key_resetTutorial));
        resetTutial.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MaterialShowcaseView.resetAll(getActivity());

                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.title_resetTutorial)
                        .setMessage(R.string.tutorial_was_reset)
                        .setNeutralButton(R.string.close, null)
                        .show();
                return true;
            }
        });

        sendAnonymousCrashData = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA);
        sendAnonymousCrashData.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_SEND_ANONYMOUS_CRASH_DATA);
        sendAnonymousCrashData.setSummaryOn(R.string.summary_sendAnonymousCrashData_enabled);
        sendAnonymousCrashData.setSummaryOff(R.string.summary_sendAnonymousCrashData_disabled);

        sendLogsEmail = findPreference(getString(R.string.key_sendLogsEmail));
        sendLogsEmail.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sendLogsEmail.setEnabled(false);
//                sendLogsProgress.setVisibility(View.VISIBLE);

                new AsyncTask<Void, Void, AsyncTaskResult<Boolean>>() {
                    @Override
                    protected AsyncTaskResult<Boolean> doInBackground(Void... params) {
                        try {
                            LogHandler.sendLogsAsMail(getActivity());
                            return new AsyncTaskResult<>(true);
                        } catch (Exception e) {
                            return new AsyncTaskResult<>(e);
                        }
                    }

                    @Override
                    protected void onPostExecute(AsyncTaskResult<Boolean> booleanAsyncTaskResult) {

                        if (booleanAsyncTaskResult.isSuccess()) {
                            // all is good
                        } else {
                            if (booleanAsyncTaskResult.getException() instanceof MissingPermissionException) {
                                Snackbar snackbar = Snackbar.make(getListView(), R.string.missing_external_storage_permission, Snackbar.LENGTH_LONG);
                                snackbar.setAction(R.string.grant, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(MainActivity.getActivity(), new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION);
                                    }
                                });
                                snackbar.show();
                            } else {
                                StatusMessageHandler.showErrorMessage(getActivity(), booleanAsyncTaskResult.getException());
                            }
                        }

                        sendLogsEmail.setEnabled(true);
//                        sendLogsProgress.setVisibility(View.GONE);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                return true;
            }
        });

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION.equals(key)) {
            String[] historyValues = getResources().getStringArray(R.array.entryValues_history);
            keepHistoryDuration.setSummary(historyValues[sharedPreferences.getInt(SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION, SmartphonePreferencesHandler.DEFAULT_VALUE_KEEP_HISTORY_DURATION)]);
        } else if (SmartphonePreferencesHandler.KEY_BACKUP_PATH.equals(key)) {
            backupPath.setSummary(sharedPreferences.getString(SmartphonePreferencesHandler.KEY_BACKUP_PATH, SmartphonePreferencesHandler.DEFAULT_VALUE_BACKUP_PATH));
        } else if (SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB.equals(key)) {
            String[] mainTabNames = getResources().getStringArray(R.array.main_tab_names);
            startupDefaultTab.setSummary(mainTabNames[sharedPreferences.getInt(SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB, SmartphonePreferencesHandler.DEFAULT_VALUE_STARTUP_TAB)]);
        } else if (SmartphonePreferencesHandler.KEY_VIBRATION_DURATION.equals(key)) {
            vibrationDuration.setSummary(sharedPreferences.getInt(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION, SmartphonePreferencesHandler.DEFAULT_VALUE_VIBRATION_DURATION) + " ms");
        } else if (SmartphonePreferencesHandler.KEY_THEME.equals(key)) {
            String[] themeNames = getResources().getStringArray(R.array.theme_names);
            theme.setSummary(themeNames[sharedPreferences.getInt(SmartphonePreferencesHandler.KEY_THEME, SmartphonePreferencesHandler.DEFAULT_VALUE_THEME)]);
        }
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
        intentFilter.addAction(LocalBroadcastConstants.INTENT_BACKUP_PATH_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);

        super.onPause();
    }
}

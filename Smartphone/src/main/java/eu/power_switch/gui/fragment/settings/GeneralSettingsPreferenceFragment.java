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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.design.widget.Snackbar;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.markusressel.android.library.tutorialtooltip.TutorialTooltip;
import eu.power_switch.BuildConfig;
import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.DeveloperOptionsDialog;
import eu.power_switch.gui.dialog.PathChooserDialog;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.settings.IntListPreference;
import eu.power_switch.settings.SliderPreference;
import eu.power_switch.settings.SliderPreferenceFragmentCompat;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.application.ApplicationHelper;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.exception.permission.MissingPermissionException;
import eu.power_switch.shared.log.LogHelper;
import eu.power_switch.shared.permission.PermissionHelper;
import eu.power_switch.wizard.gui.WizardActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Created by Markus on 31.07.2016.
 */
public class GeneralSettingsPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private IntListPreference startupDefaultTab;
    private SwitchPreference  autodiscover;
    private SwitchPreference  autoCollapseRooms;
    private SwitchPreference  autoCollapseTimers;
    private SwitchPreference  showRoomOnOff;
    private SwitchPreference  hideFab;
    private SwitchPreference  highlightLastActivatedButton;
    private SwitchPreference  showBackgroundActionToast;
    private SwitchPreference  vibrateOnButtonPress;
    private SliderPreference  vibrationDuration;
    private SwitchPreference  showGeofenceNotifications;
    private SwitchPreference  showTimerNotifications;
    private IntListPreference keepHistoryDuration;
    private Preference        backupPath;
    private IntListPreference theme;
    private IntListPreference launcherIcon;
    private Preference        resetTutorial;
    private Preference        relaunchWizard;
    private SwitchPreference  sendAnonymousCrashData;
    private IntListPreference logDestination;
    private Preference        sendLogsEmail;

    private Calendar             devMenuFirstClickTime;
    private int                  devMenuClickCounter;
    private Map<Integer, String> mainTabsMap;
    private Map<Integer, String> keepHistoryMap;
    private Map<Integer, String> themeMap;
    private Map<Integer, String> logDestinationMap;
    private Map<Integer, String> launcherIconMap;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // set preferences file name
        getPreferenceManager().setSharedPreferencesName(SettingsConstants.SHARED_PREFS_NAME);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_general);

        initializePreferenceItems();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onBackupPathChanged() {
        backupPath.setSummary(SmartphonePreferencesHandler.<String>get(SmartphonePreferencesHandler.KEY_BACKUP_PATH));
    }

    private void initializePreferenceItems() {

        startupDefaultTab = (IntListPreference) findPreference(SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB);
        startupDefaultTab.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_STARTUP_TAB);

        mainTabsMap = getListPreferenceEntryValueMap(R.array.main_tab_values, R.array.main_tab_names);
        startupDefaultTab.setSummary(mainTabsMap.get(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB)));

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

        showGeofenceNotifications = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SHOW_GEOFENCE_NOTIFICATIONS);
        showGeofenceNotifications.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_SHOW_GEOFENCE_NOTIFICATIONS);
        showGeofenceNotifications.setSummaryOn(R.string.summary_showGeofenceNotifications_enabled);
        showGeofenceNotifications.setSummaryOff(R.string.summary_showGeofenceNotifications_disabled);

        showTimerNotifications = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SHOW_TIMER_NOTIFICATIONS);
        showTimerNotifications.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_SHOW_TIMER_NOTIFICATIONS);
        showTimerNotifications.setSummaryOn(R.string.summary_showTimerNotifications_enabled);
        showTimerNotifications.setSummaryOff(R.string.summary_showTimerNotifications_disabled);

        keepHistoryDuration = (IntListPreference) findPreference(SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION);
        keepHistoryDuration.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_KEEP_HISTORY_DURATION);
        keepHistoryMap = getListPreferenceEntryValueMap(R.array.entryValues_history, R.array.entries_history);
        keepHistoryDuration.setSummary(keepHistoryMap.get(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION)));

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
                            ActivityCompat.requestPermissions(MainActivity.getActivity(),
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION);
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
        themeMap = getListPreferenceEntryValueMap(R.array.theme_values, R.array.theme_names);
        theme.setSummary(themeMap.get(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_THEME)));

        launcherIcon = (IntListPreference) findPreference(SmartphonePreferencesHandler.KEY_LAUNCHER_ICON);
        launcherIcon.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_LAUNCHER_ICON);
        launcherIconMap = getListPreferenceEntryValueMap(R.array.entryValues_launcher_icon, R.array.entries_launcher_icon);
        launcherIcon.setSummary(launcherIconMap.get(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_LAUNCHER_ICON)));

        resetTutorial = findPreference(getString(R.string.key_resetTutorial));
        resetTutorial.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MaterialShowcaseView.resetAll(getActivity());

                TutorialTooltip.resetAllShowCount(getActivity());

                new AlertDialog.Builder(getContext()).setTitle(R.string.title_resetTutorial)
                        .setMessage(R.string.tutorial_was_reset)
                        .setNeutralButton(R.string.close, null)
                        .show();
                return true;
            }
        });

        relaunchWizard = findPreference(getString(R.string.key_relaunchWizard));
        relaunchWizard.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent wizardIntent = new Intent(getActivity(), WizardActivity.class);
                startActivity(wizardIntent);

                return true;
            }
        });

        sendAnonymousCrashData = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA);
        sendAnonymousCrashData.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_SEND_ANONYMOUS_CRASH_DATA);
        sendAnonymousCrashData.setSummaryOn(R.string.summary_sendAnonymousCrashData_enabled);
        sendAnonymousCrashData.setSummaryOff(R.string.summary_sendAnonymousCrashData_disabled);

        logDestination = (IntListPreference) findPreference(SmartphonePreferencesHandler.KEY_LOG_DESTINATION);
        logDestination.setDefaultValue(SmartphonePreferencesHandler.DEFAULT_VALUE_LOG_DESTINATION);
        logDestinationMap = getListPreferenceEntryValueMap(R.array.logDestination_values, R.array.logDestination_names);
        logDestination.setSummary(logDestinationMap.get(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_LOG_DESTINATION)));

        sendLogsEmail = findPreference(getString(R.string.key_sendLogsEmail));
        sendLogsEmail.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new AlertDialog.Builder(getPreferenceManagerContext()).setTitle(R.string.title_sendLogsEmail)
                        .setMessage(R.string.dialogMessage_sendLogsEmail)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new AsyncTask<Void, Void, AsyncTaskResult<Boolean>>() {
                                    @Override
                                    protected AsyncTaskResult<Boolean> doInBackground(Void... params) {
                                        try {
                                            LogHelper.sendLogsAsMail(getActivity());
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
                                                Snackbar snackbar = Snackbar.make(getListView(),
                                                        R.string.missing_external_storage_permission,
                                                        Snackbar.LENGTH_LONG);
                                                snackbar.setAction(R.string.grant, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        ActivityCompat.requestPermissions(MainActivity.getActivity(),
                                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                                PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION);
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
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();

                sendLogsEmail.setEnabled(false);
                //                sendLogsProgress.setVisibility(View.VISIBLE);

                return true;
            }
        });

        if (BuildConfig.DEBUG) {
            PreferenceScreen preferenceScreen = getPreferenceScreen();

            PreferenceCategory developerCategory = new PreferenceCategory(getPreferenceManagerContext());
            developerCategory.setTitle("Developer Options");
            // add category to parent first, then add items to category!
            preferenceScreen.addPreference(developerCategory);

            Preference developerOptionsPreference = new Preference(getPreferenceManagerContext());
            developerOptionsPreference.setTitle("Developer Options");
            developerOptionsPreference.setSummary("CAUTION! Using options in this menu can IRREVERSIBLY CORRUPT this app!");
            developerOptionsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Calendar currentTime = Calendar.getInstance();
                    if (devMenuFirstClickTime != null) {
                        Calendar latestTime = Calendar.getInstance();
                        latestTime.setTime(devMenuFirstClickTime.getTime());
                        latestTime.add(Calendar.SECOND, 5);
                        if (currentTime.after(latestTime)) {
                            devMenuClickCounter = 0;
                        }
                    }

                    devMenuClickCounter++;
                    if (devMenuClickCounter == 1) {
                        devMenuFirstClickTime = currentTime;
                    }
                    if (devMenuClickCounter >= 5) {
                        devMenuClickCounter = 0;

                        DeveloperOptionsDialog developerOptionsDialog = new DeveloperOptionsDialog();
                        developerOptionsDialog.show(getActivity().getSupportFragmentManager(), null);
                    }
                    return true;
                }
            });

            developerCategory.addPreference(developerOptionsPreference);
        }
    }

    /**
     * Gets a Map from two array resources
     *
     * @param valueRes values stored in preferences
     * @param nameRes  name/description of this option used in view
     *
     * @return Map from stored value -> display name
     */
    private Map<Integer, String> getListPreferenceEntryValueMap(@ArrayRes int valueRes, @ArrayRes int nameRes) {
        Map<Integer, String> map = new HashMap<>();

        String[] values = getResources().getStringArray(valueRes);
        String[] names  = getResources().getStringArray(nameRes);

        for (int i = 0; i < values.length; i++) {
            map.put(Integer.valueOf(values[i]), names[i]);
        }

        return map;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SmartphonePreferencesHandler.KEY_KEEP_HISTORY_DURATION.equals(key)) {
            keepHistoryDuration.setSummary(keepHistoryMap.get(sharedPreferences.getInt(key,
                    SmartphonePreferencesHandler.DEFAULT_VALUE_KEEP_HISTORY_DURATION)));
        } else if (SmartphonePreferencesHandler.KEY_BACKUP_PATH.equals(key)) {
            backupPath.setSummary(sharedPreferences.getString(key, SmartphonePreferencesHandler.DEFAULT_VALUE_BACKUP_PATH));
        } else if (SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB.equals(key)) {
            startupDefaultTab.setSummary(mainTabsMap.get(sharedPreferences.getInt(key, SmartphonePreferencesHandler.DEFAULT_VALUE_STARTUP_TAB)));
        } else if (SmartphonePreferencesHandler.KEY_VIBRATION_DURATION.equals(key)) {
            vibrationDuration.setSummary(sharedPreferences.getInt(key, SmartphonePreferencesHandler.DEFAULT_VALUE_VIBRATION_DURATION) + " ms");
        } else if (SmartphonePreferencesHandler.KEY_THEME.equals(key)) {
            theme.setSummary(themeMap.get(sharedPreferences.getInt(key, SmartphonePreferencesHandler.DEFAULT_VALUE_THEME)));

            // restart activity
            getActivity().finish();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (SmartphonePreferencesHandler.KEY_LOG_DESTINATION.equals(key)) {
            logDestination.setSummary(logDestinationMap.get(sharedPreferences.getInt(key,
                    SmartphonePreferencesHandler.DEFAULT_VALUE_LOG_DESTINATION)));
        } else if (SmartphonePreferencesHandler.KEY_LAUNCHER_ICON.equals(key)) {
            ApplicationHelper.setLauncherIcon(getContext(),
                    ApplicationHelper.LauncherIcon.valueOf(sharedPreferences.getInt(key, SmartphonePreferencesHandler.DEFAULT_VALUE_LAUNCHER_ICON)));
            launcherIcon.setSummary(launcherIconMap.get(sharedPreferences.getInt(key, SmartphonePreferencesHandler.DEFAULT_VALUE_LAUNCHER_ICON)));

            new AlertDialog.Builder(getActivity()).setTitle(R.string.attention)
                    .setMessage(R.string.changes_may_only_show_up_after_device_restart)
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
        }

        SmartphonePreferencesHandler.forceRefresh();
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

    private Context getPreferenceManagerContext() {
        // use this Context to correctly inflate new Preferences added in code
        return getPreferenceManager().getContext();
    }
}

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
import android.support.v7.preference.PreferenceScreen;
import android.view.View;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.markusressel.android.library.tutorialtooltip.TutorialTooltip;
import eu.power_switch.BuildConfig;
import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.DeveloperOptionsDialog;
import eu.power_switch.gui.dialog.PathChooserDialog;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.settings.IntListPreference;
import eu.power_switch.settings.SliderPreference;
import eu.power_switch.settings.SliderPreferenceFragmentCompat;
import eu.power_switch.shared.application.ApplicationHelper;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.exception.permission.MissingPermissionException;
import eu.power_switch.shared.log.LogHelper;
import eu.power_switch.shared.permission.PermissionHelper;
import eu.power_switch.shared.persistence.preferences.PreferenceItem;
import eu.power_switch.wizard.gui.WizardActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.BACKUP_PATH;
import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_LAUNCHER_ICON;
import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_LOG_DESTINATION;

/**
 * Created by Markus on 31.07.2016.
 */
public class GeneralSettingsPreferenceFragment extends EventBusPreferenceFragment {

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

    @Inject
    SmartphonePreferencesHandler smartphonePreferencesHandler;

    @Inject
    StatusMessageHandler statusMessageHandler;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // set preferences file name
        getPreferenceManager().setSharedPreferencesName(SettingsConstants.SHARED_PREFS_NAME);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_general);

        initializePreferenceItems();
    }

    private void initializePreferenceItems() {

        startupDefaultTab = (IntListPreference) findPreference(SmartphonePreferencesHandler.STARTUP_DEFAULT_TAB.getKey(getContext()));
        startupDefaultTab.setDefaultValue(SmartphonePreferencesHandler.STARTUP_DEFAULT_TAB.getDefaultValue());
        mainTabsMap = getListPreferenceEntryValueMap(R.array.main_tab_values, R.array.main_tab_names);
        startupDefaultTab.setSummary(mainTabsMap.get(smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.STARTUP_DEFAULT_TAB)));

        autodiscover = (SwitchPreference) findPreference(SmartphonePreferencesHandler.GATEWAY_AUTO_DISCOVERY.getKey(getContext()));
        autodiscover.setDefaultValue(SmartphonePreferencesHandler.GATEWAY_AUTO_DISCOVERY.getDefaultValue());
        autodiscover.setSummaryOn(R.string.summary_autodiscover_enabled);
        autodiscover.setSummaryOff(R.string.summary_autodiscover_disabled);

        autoCollapseRooms = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS.getKey(getContext()));
        autoCollapseRooms.setDefaultValue(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS.getDefaultValue());
        autoCollapseRooms.setSummaryOn(R.string.summary_autoCollapseRooms_enabled);
        autoCollapseRooms.setSummaryOff(R.string.summary_autoCollapseRooms_disabled);

        autoCollapseTimers = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_TIMERS.getKey(getContext()));
        autoCollapseTimers.setDefaultValue(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_TIMERS.getDefaultValue());
        autoCollapseTimers.setSummaryOn(R.string.summary_autoCollapseTimers_enabled);
        autoCollapseTimers.setSummaryOff(R.string.summary_autoCollapseTimers_disabled);

        showRoomOnOff = (SwitchPreference) findPreference(SmartphonePreferencesHandler.SHOW_ROOM_ALL_ON_OFF.getKey(getContext()));
        showRoomOnOff.setDefaultValue(SmartphonePreferencesHandler.SHOW_ROOM_ALL_ON_OFF.getDefaultValue());
        showRoomOnOff.setSummaryOn(R.string.summary_showRoomAllOnOff_enabled);
        showRoomOnOff.setSummaryOff(R.string.summary_showRoomAllOnOff_disabled);

        hideFab = (SwitchPreference) findPreference(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB.getKey(getContext()));
        hideFab.setDefaultValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB.getDefaultValue());
        hideFab.setSummaryOn(R.string.summary_useOptionsMenuInsteadOfFab_enabled);
        hideFab.setSummaryOff(R.string.summary_useOptionsMenuInsteadOfFab_disabled);

        highlightLastActivatedButton = (SwitchPreference) findPreference(SmartphonePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON.getKey(
                getContext()));
        highlightLastActivatedButton.setDefaultValue(SmartphonePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON.getDefaultValue());
        highlightLastActivatedButton.setSummaryOn(R.string.summary_highlightLastActivatedButton_enabled);
        highlightLastActivatedButton.setSummaryOff(R.string.summary_highlightLastActivatedButton_disabled);

        showBackgroundActionToast = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SHOW_TOAST_IN_BACKGROUND.getKey(getContext()));
        showBackgroundActionToast.setDefaultValue(SmartphonePreferencesHandler.KEY_SHOW_TOAST_IN_BACKGROUND.getDefaultValue());
        showBackgroundActionToast.setSummaryOn(R.string.summary_showBackgroundActionToast_enabled);
        showBackgroundActionToast.setSummaryOff(R.string.summary_showBackgroundActionToast_disabled);

        vibrateOnButtonPress = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS.getKey(getContext()));
        vibrateOnButtonPress.setDefaultValue(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS.getDefaultValue());
        vibrateOnButtonPress.setSummaryOn(R.string.summary_vibrateOnButtonPress_enabled);
        vibrateOnButtonPress.setSummaryOff(R.string.summary_vibrateOnButtonPress_disabled);

        vibrationDuration = (SliderPreference) findPreference(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION.getKey(getContext()));
        vibrationDuration.setDefaultValue(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION.getDefaultValue());
        vibrationDuration.setSummary(smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION) + " ms");

        Object defaultValue = SmartphonePreferencesHandler.KEY_SHOW_GEOFENCE_NOTIFICATIONS.getDefaultValue();

        showGeofenceNotifications = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SHOW_GEOFENCE_NOTIFICATIONS.getKey(getContext()));
        showGeofenceNotifications.setDefaultValue(defaultValue);
        showGeofenceNotifications.setSummaryOn(R.string.summary_showGeofenceNotifications_enabled);
        showGeofenceNotifications.setSummaryOff(R.string.summary_showGeofenceNotifications_disabled);

        showTimerNotifications = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SHOW_TIMER_NOTIFICATIONS.getKey(getContext()));
        showTimerNotifications.setDefaultValue(SmartphonePreferencesHandler.KEY_SHOW_TIMER_NOTIFICATIONS.getDefaultValue());
        showTimerNotifications.setSummaryOn(R.string.summary_showTimerNotifications_enabled);
        showTimerNotifications.setSummaryOff(R.string.summary_showTimerNotifications_disabled);

        keepHistoryDuration = (IntListPreference) findPreference(SmartphonePreferencesHandler.KEEP_HISTORY_DURATION.getKey(getContext()));
        keepHistoryDuration.setDefaultValue(SmartphonePreferencesHandler.KEEP_HISTORY_DURATION.getDefaultValue());
        keepHistoryMap = getListPreferenceEntryValueMap(R.array.entryValues_history, R.array.entries_history);
        keepHistoryDuration.setSummary(keepHistoryMap.get(smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.KEEP_HISTORY_DURATION)));

        final Fragment fragment = this;
        backupPath = findPreference(SmartphonePreferencesHandler.BACKUP_PATH.getKey(getContext()));
        backupPath.setDefaultValue(SmartphonePreferencesHandler.BACKUP_PATH.getDefaultValue());
        String summary = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.BACKUP_PATH);
        backupPath.setSummary(summary);
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

        theme = (IntListPreference) findPreference(SmartphonePreferencesHandler.KEY_THEME.getKey(getContext()));
        theme.setDefaultValue(SmartphonePreferencesHandler.KEY_THEME.getDefaultValue());
        themeMap = getListPreferenceEntryValueMap(R.array.theme_values, R.array.theme_names);
        theme.setSummary(themeMap.get(smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.KEY_THEME)));

        launcherIcon = (IntListPreference) findPreference(KEY_LAUNCHER_ICON.getKey(getContext()));
        launcherIcon.setDefaultValue(KEY_LAUNCHER_ICON.getDefaultValue());
        launcherIconMap = getListPreferenceEntryValueMap(R.array.entryValues_launcher_icon, R.array.entries_launcher_icon);
        launcherIcon.setSummary(launcherIconMap.get(smartphonePreferencesHandler.getValue(KEY_LAUNCHER_ICON)));

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

        sendAnonymousCrashData = (SwitchPreference) findPreference(SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA.getKey(getContext()));
        sendAnonymousCrashData.setDefaultValue(SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA.getDefaultValue());
        sendAnonymousCrashData.setSummaryOn(R.string.summary_sendAnonymousCrashData_enabled);
        sendAnonymousCrashData.setSummaryOff(R.string.summary_sendAnonymousCrashData_disabled);

        logDestination = (IntListPreference) findPreference(KEY_LOG_DESTINATION.getKey(getContext()));
        logDestination.setDefaultValue(KEY_LOG_DESTINATION.getDefaultValue());
        logDestinationMap = getListPreferenceEntryValueMap(R.array.logDestination_values, R.array.logDestination_names);
        logDestination.setSummary(logDestinationMap.get(smartphonePreferencesHandler.getValue(KEY_LOG_DESTINATION)));

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
                                                statusMessageHandler.showErrorMessage(getActivity(), booleanAsyncTaskResult.getException());
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
        smartphonePreferencesHandler.forceRefreshCache();

        PreferenceItem preferenceItem = smartphonePreferencesHandler.getPreferenceItem(key);
        if (preferenceItem == null) {
            return;
        }

        if (preferenceItem == SmartphonePreferencesHandler.KEEP_HISTORY_DURATION) {
            keepHistoryDuration.setSummary(keepHistoryMap.get(smartphonePreferencesHandler.getValue(preferenceItem)));
        } else if (preferenceItem == BACKUP_PATH) {
            backupPath.setSummary(String.valueOf(smartphonePreferencesHandler.getValue(preferenceItem)));
        } else if (preferenceItem == SmartphonePreferencesHandler.STARTUP_DEFAULT_TAB) {
            startupDefaultTab.setSummary(mainTabsMap.get(smartphonePreferencesHandler.getValue(preferenceItem)));
        } else if (preferenceItem == SmartphonePreferencesHandler.KEY_VIBRATION_DURATION) {
            vibrationDuration.setSummary(smartphonePreferencesHandler.getValue(preferenceItem) + " ms");
        } else if (preferenceItem == SmartphonePreferencesHandler.KEY_THEME) {
            theme.setSummary(themeMap.get(smartphonePreferencesHandler.getValue(preferenceItem)));

            // restart activity
            getActivity().finish();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else if (preferenceItem == SmartphonePreferencesHandler.KEY_LAUNCHER_ICON) {
            logDestination.setSummary(logDestinationMap.get(smartphonePreferencesHandler.getValue(preferenceItem)));
        } else if (preferenceItem == SmartphonePreferencesHandler.KEY_LAUNCHER_ICON) {
            ApplicationHelper.setLauncherIcon(getContext(),
                    ApplicationHelper.LauncherIcon.valueOf((int) smartphonePreferencesHandler.getValue(preferenceItem)));
            launcherIcon.setSummary(launcherIconMap.get(smartphonePreferencesHandler.getValue(preferenceItem)));

            new AlertDialog.Builder(getActivity()).setTitle(R.string.attention)
                    .setMessage(R.string.changes_may_only_show_up_after_device_restart)
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
        }
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

    private Context getPreferenceManagerContext() {
        // use this Context to correctly inflate new Preferences added in code
        return getPreferenceManager().getContext();
    }
}

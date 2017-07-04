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

package eu.power_switch.gui.dialog.configuration;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ConfigurationDialogTabAdapter;
import eu.power_switch.gui.dialog.configuration.holder.TimerConfigurationHolder;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage1Time;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage2Days;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage3Action;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage4TabbedSummary;
import eu.power_switch.timer.Timer;
import timber.log.Timber;

/**
 * Dialog to create or modify a Timer
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureTimerDialog extends ConfigurationDialogTabbed<TimerConfigurationHolder> {

    public static ConfigureTimerDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(-1, targetFragment);
    }

    public static ConfigureTimerDialog newInstance(long timerId, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureTimerDialog     fragment                 = new ConfigureTimerDialog();
        TimerConfigurationHolder timerConfigurationHolder = new TimerConfigurationHolder();
        if (timerId != -1) {
            timerConfigurationHolder.setId(timerId);
        }
        fragment.setConfiguration(timerConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("Opening " + getClass().getSimpleName() + "...");
    }

    @Override
    protected boolean initializeFromExistingData(Bundle arguments) {
        Long timerId = getConfiguration().getId();

        if (timerId != null) {
            try {
                Timer timer = DatabaseHandler.getTimer(timerId);
                getConfiguration().setTimer(timer);

                getConfiguration().setActive(timer.isActive());
                getConfiguration().setId(timerId);
                getConfiguration().setName(timer.getName());

                getConfiguration().setExecutionTime(timer.getExecutionTime());
                getConfiguration().setRandomizerValue(timer.getRandomizerValue());

                getConfiguration().setExecutionInterval(timer.getExecutionInterval());
                getConfiguration().setExecutionType(timer.getExecutionType());

                getConfiguration().setActions(timer.getActions());

            } catch (Exception e) {
                Timber.e(e);
            }

            // init dialog using existing scene
            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment()));
            return true;
        } else {
            // Create the adapter that will return a fragment
            // for each of the two primary sections of the app.
            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment()));
            return false;
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_timer;
    }

    @Override
    protected void saveCurrentConfigurationToDatabase() {
        Timber.d("Saving timer");
        super.saveCurrentConfigurationToDatabase();
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                .setMessage(R.string.timer_will_be_gone_forever)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DatabaseHandler.deleteTimer(getConfiguration().getId());

                            // notify scenes fragment
                            TimersFragment.notifyTimersChanged();

                            StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.timer_deleted, Snackbar.LENGTH_LONG);
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                        }

                        // close dialog
                        getDialog().dismiss();
                    }
                })
                .setNeutralButton(android.R.string.cancel, null)
                .show();
    }

    private static class CustomTabAdapter extends ConfigurationDialogTabAdapter {

        private ConfigurationDialogTabbed<TimerConfigurationHolder> parentDialog;
        private ConfigurationDialogTabbedSummaryFragment            summaryFragment;
        private Fragment                                            targetFragment;

        public CustomTabAdapter(ConfigurationDialogTabbed<TimerConfigurationHolder> parentDialog, FragmentManager fm, Fragment targetFragment) {
            super(fm);
            this.parentDialog = parentDialog;
            this.targetFragment = targetFragment;
        }

        public ConfigurationDialogTabbedSummaryFragment getSummaryFragment() {
            return summaryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return parentDialog.getString(R.string.time);
                case 1:
                    return parentDialog.getString(R.string.days);
                case 2:
                    return parentDialog.getString(R.string.actions);
                case 3:
                    return parentDialog.getString(R.string.summary);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureTimerDialogPage1Time.class, parentDialog);
                    break;
                case 1:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureTimerDialogPage2Days.class, parentDialog);
                    break;
                case 2:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureTimerDialogPage3Action.class, parentDialog);
                    break;
                case 3:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureTimerDialogPage4TabbedSummary.class, parentDialog);
                    fragment.setTargetFragment(targetFragment, 0);
                    summaryFragment = (ConfigurationDialogTabbedSummaryFragment) fragment;
            }

            return fragment;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 4;
        }
    }

}

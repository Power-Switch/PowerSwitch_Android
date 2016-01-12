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

package eu.power_switch.gui.dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage1TimeFragment;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage2DaysFragment;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage3ActionFragment;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage4SummaryFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to create or modify a Timer
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureTimerDialog extends ConfigurationDialogTabbed {

    /**
     * ID of existing Timer to Edit
     */
    public static final String TIMER_ID_KEY = "TimerId";

    private BroadcastReceiver broadcastReceiver;

    private long timerId = -1;

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening ConfigureTimerDialog...");

        setDialogTitle(getString(R.string.configure_timer));

        setDeleteAction(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                        .timer_will_be_gone_forever)
                        .setPositiveButton
                                (android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            DatabaseHandler.deleteTimer(timerId);

                                            // notify scenes fragment
                                            TimersFragment.sendTimersChangedBroadcast(getActivity());

                                            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                                                    R.string.timer_deleted, Snackbar.LENGTH_LONG);
                                        } catch (Exception e) {
                                            Log.e(e);
                                            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
                                        }

                                        // close dialog
                                        getDialog().dismiss();
                                    }
                                }).setNeutralButton(android.R.string.cancel, null).show();
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    setModified(true);
                    setSaveButtonState(checkValidity());
                } catch (Exception e) {
                    setSaveButtonState(false);
                }
            }
        };
    }

    @Override
    protected void initExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(TIMER_ID_KEY)) {
            // init dialog using existing scene
            timerId = arguments.getLong(TIMER_ID_KEY);
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment(), timerId));
            imageButtonDelete.setVisibility(View.VISIBLE);
            setSaveButtonState(true);
        } else {
            // Create the adapter that will return a fragment
            // for each of the two primary sections of the app.
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment()));
            imageButtonDelete.setVisibility(View.GONE);
            setSaveButtonState(false);
        }
    }

    /**
     * checks validity of current configuration
     */
    private boolean checkValidity() {
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigureTimerDialogPage4SummaryFragment summaryFragment =
                customTabAdapter.getSummaryFragment();

        return summaryFragment.checkValidity();
    }

    @Override
    protected void saveCurrentConfigurationToDatabase() {
        Log.d("Saving timer");
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigureTimerDialogPage4SummaryFragment summaryFragment =
                customTabAdapter.getSummaryFragment();

        if (summaryFragment.checkValidity()) {
            summaryFragment.saveCurrentConfigurationToDatabase();
            getDialog().dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_TIMER_SUMMARY_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    private static class CustomTabAdapter extends FragmentPagerAdapter {

        private Context context;
        private long timerId;
        private ConfigureTimerDialogPage4SummaryFragment summaryFragment;
        private RecyclerViewFragment recyclerViewFragment;

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment) {
            super(fm);
            this.context = context;
            this.timerId = -1;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment, long
                id) {
            super(fm);
            this.context = context;
            this.timerId = id;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public ConfigureTimerDialogPage4SummaryFragment getSummaryFragment() {
            return summaryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return context.getString(R.string.time);
                case 1:
                    return context.getString(R.string.days);
                case 2:
                    return context.getString(R.string.actions);
                case 3:
                    return context.getString(R.string.summary);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = new ConfigureTimerDialogPage1TimeFragment();
                    break;
                case 1:
                    fragment = new ConfigureTimerDialogPage2DaysFragment();
                    break;
                case 2:
                    fragment = new ConfigureTimerDialogPage3ActionFragment();
                    break;
                case 3:
                    fragment = new ConfigureTimerDialogPage4SummaryFragment();
                    fragment.setTargetFragment(recyclerViewFragment, 0);
                    summaryFragment = (ConfigureTimerDialogPage4SummaryFragment) fragment;
            }

            if (fragment != null && timerId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong(TIMER_ID_KEY, timerId);
                fragment.setArguments(bundle);
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

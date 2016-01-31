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
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage1LocationFragment;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage2EnterActionsFragment;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage3ExitActionsFragment;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage4SummaryFragment;
import eu.power_switch.gui.fragment.geofences.CustomGeofencesFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to create or modify a Geofence
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureGeofenceDialog extends ConfigurationDialogTabbed {

    /**
     * ID of existing Geofence to Edit
     */
    public static final String GEOFENCE_ID_KEY = "GeofenceId";

    protected long geofenceId = -1;

    private BroadcastReceiver broadcastReceiver;
    private GeofenceApiHandler geofenceApiHandler;

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening " + getClass().getSimpleName() + "...");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyConfigurationChanged();
            }
        };

        geofenceApiHandler = new GeofenceApiHandler(getActivity());
    }

    @Override
    protected boolean isValid() {
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigurationDialogTabbedSummaryFragment summaryFragment =
                customTabAdapter.getSummaryFragment();

        return summaryFragment.checkSetupValidity();
    }

    @Override
    protected boolean initializeFromExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(GEOFENCE_ID_KEY)) {
            // init dialog using existing geofence
            geofenceId = arguments.getLong(GEOFENCE_ID_KEY);
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment(), geofenceId));
            imageButtonDelete.setVisibility(View.VISIBLE);
            return true;
        } else {
            // Create the adapter that will return a fragment
            // for each of the two primary sections of the app.
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment()));
            imageButtonDelete.setVisibility(View.GONE);
            return false;
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_geofence;
    }

    @Override
    protected void saveCurrentConfigurationToDatabase() {
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigurationDialogTabbedSummaryFragment summaryFragment =
                customTabAdapter.getSummaryFragment();
        if (summaryFragment.checkSetupValidity()) {
            try {
                summaryFragment.saveCurrentConfigurationToDatabase();
            } catch (Exception e) {
                StatusMessageHandler.showStatusMessage(getActivity(), R.string.unknown_error, Snackbar.LENGTH_LONG);
            }
            getDialog().dismiss();
        }
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).
                setMessage(R.string.geofence_will_be_gone_forever)
                .setPositiveButton
                        (android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DatabaseHandler.deleteGeofence(geofenceId);
                                    geofenceApiHandler.removeGeofence(geofenceId);

                                    // same for timers
                                    CustomGeofencesFragment.sendCustomGeofencesChangedBroadcast(getActivity());

                                    StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                                            R.string.geofence_deleted, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    Log.e(e);
                                    StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
                                }

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_SETUP_GEOFENCE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
        geofenceApiHandler.onStart();
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        geofenceApiHandler.onStop();
        super.onStop();
    }

    protected static class CustomTabAdapter extends FragmentPagerAdapter {

        private Context context;
        private long geofenceId;
        private ConfigurationDialogTabbedSummaryFragment summaryFragment;
        private RecyclerViewFragment recyclerViewFragment;

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment) {
            super(fm);
            this.context = context;
            this.geofenceId = -1;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment, long id) {
            super(fm);
            this.context = context;
            this.geofenceId = id;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public ConfigurationDialogTabbedSummaryFragment getSummaryFragment() {
            return summaryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return context.getString(R.string.location);
                case 1:
                    return context.getString(R.string.enter);
                case 2:
                    return context.getString(R.string.exit);
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
                    fragment = new ConfigureGeofenceDialogPage1LocationFragment();
                    break;
                case 1:
                    fragment = new ConfigureGeofenceDialogPage2EnterActionsFragment();
                    break;
                case 2:
                    fragment = new ConfigureGeofenceDialogPage3ExitActionsFragment();
                    break;
                case 3:
                    fragment = new ConfigureGeofenceDialogPage4SummaryFragment();
                    fragment.setTargetFragment(recyclerViewFragment, 0);

                    summaryFragment = (ConfigurationDialogTabbedSummaryFragment) fragment;
                    break;
                default:
                    break;
            }

            if (fragment != null && geofenceId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong(GEOFENCE_ID_KEY, geofenceId);
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
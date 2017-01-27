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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ConfigurationDialogTabAdapter;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage1LocationFragment;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage2EnterActionsFragment;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage3ExitActionsFragment;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage4SummaryFragment;
import eu.power_switch.gui.fragment.geofences.CustomGeofencesFragment;
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
    protected GeofenceApiHandler geofenceApiHandler;

    public static ConfigureGeofenceDialog newInstance(long geofenceId) {
        Bundle args = new Bundle();
        args.putLong(GEOFENCE_ID_KEY, geofenceId);

        ConfigureGeofenceDialog fragment = new ConfigureGeofenceDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening " + getClass().getSimpleName() + "...");

        geofenceApiHandler = new GeofenceApiHandler(getActivity());
    }

    @Override
    protected boolean initializeFromExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(GEOFENCE_ID_KEY)) {
            // init dialog using existing geofence
            geofenceId = arguments.getLong(GEOFENCE_ID_KEY);
            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment(), geofenceId));
            return true;
        } else {
            // Create the adapter that will return a fragment
            // for each of the two primary sections of the app.
            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment()));
            return false;
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_geofence;
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

                                    StatusMessageHandler.showInfoMessage(getTargetFragment(),
                                            R.string.geofence_deleted, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                                }

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        geofenceApiHandler.onStart();
    }

    @Override
    public void onStop() {
        geofenceApiHandler.onStop();
        super.onStop();
    }

    protected static class CustomTabAdapter extends ConfigurationDialogTabAdapter {

        private ConfigurationDialogTabbed parentDialog;
        private long geofenceId;
        private ConfigurationDialogTabbedSummaryFragment summaryFragment;
        private RecyclerViewFragment recyclerViewFragment;

        public CustomTabAdapter(ConfigurationDialogTabbed parentDialog, FragmentManager fm, RecyclerViewFragment recyclerViewFragment) {
            super(fm);
            this.parentDialog = parentDialog;
            this.geofenceId = -1;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public CustomTabAdapter(ConfigurationDialogTabbed parentDialog, FragmentManager fm, RecyclerViewFragment recyclerViewFragment, long id) {
            super(fm);
            this.parentDialog = parentDialog;
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
                    return parentDialog.getString(R.string.location);
                case 1:
                    return parentDialog.getString(R.string.enter);
                case 2:
                    return parentDialog.getString(R.string.exit);
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
                    fragment = ConfigurationDialogFragment.newInstance(ConfigureGeofenceDialogPage1LocationFragment.class, parentDialog);
                    break;
                case 1:
                    fragment = ConfigurationDialogFragment.newInstance(ConfigureGeofenceDialogPage2EnterActionsFragment.class, parentDialog);
                    break;
                case 2:
                    fragment = ConfigurationDialogFragment.newInstance(ConfigureGeofenceDialogPage3ExitActionsFragment.class, parentDialog);
                    break;
                case 3:
                    fragment = ConfigurationDialogFragment.newInstance(ConfigureGeofenceDialogPage4SummaryFragment.class, parentDialog);
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
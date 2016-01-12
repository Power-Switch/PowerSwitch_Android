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
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage1NameFragment;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage2TypeFragment;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage3SetupFragment;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage4SummaryFragment;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;

/**
 * Dialog to create or modify a Receiver
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialog extends ConfigurationDialogTabbed {

    /**
     * ID of existing Receiver to Edit
     */
    public static final String RECEIVER_ID_KEY = "ReceiverId";

    private long receiverId = -1;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening ConfigureReceiverDialog...");

        setDialogTitle(getString(R.string.configure_receiver));

        setDeleteAction(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                        .receiver_will_be_gone_forever)
                        .setPositiveButton
                                (android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            DatabaseHandler.deleteReceiver(receiverId);

                                            // notify rooms fragment
                                            RoomsFragment.sendReceiverChangedBroadcast(getActivity());

                                            // scenes could change too if receiver was used in a scene
                                            ScenesFragment.sendScenesChangedBroadcast(getActivity());
                                            // same for timers
                                            TimersFragment.sendTimersChangedBroadcast(getActivity());

                                            // update receiver widgets
                                            ReceiverWidgetProvider.forceWidgetUpdate(getActivity());

                                            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                                                    R.string.receiver_deleted, Snackbar.LENGTH_LONG);
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
                    Log.e(e);
                    setSaveButtonState(false);
                }
            }
        };
    }

    private boolean checkValidity() {
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigureReceiverDialogPage4SummaryFragment summaryFragment =
                customTabAdapter.getSummaryFragment();

        return summaryFragment.checkSetupValidity();
    }

    @Override
    protected void initExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(RECEIVER_ID_KEY)) {
            // init dialog using existing receiver
            receiverId = arguments.getLong(RECEIVER_ID_KEY);
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment(), receiverId));
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

    @Override
    protected void saveCurrentConfigurationToDatabase() {
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigureReceiverDialogPage4SummaryFragment summaryFragment =
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
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_RECEIVER_SUMMARY_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    private static class CustomTabAdapter extends FragmentPagerAdapter {

        private Context context;
        private long receiverId;
        private ConfigureReceiverDialogPage4SummaryFragment summaryFragment;
        private RecyclerViewFragment recyclerViewFragment;

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment) {
            super(fm);
            this.context = context;
            this.receiverId = -1;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment, long id) {
            super(fm);
            this.context = context;
            this.receiverId = id;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public ConfigureReceiverDialogPage4SummaryFragment getSummaryFragment() {
            return summaryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return context.getString(R.string.name);
                case 1:
                    return context.getString(R.string.type);
                case 2:
                    return context.getString(R.string.setup);
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
                    fragment = new ConfigureReceiverDialogPage1NameFragment();
                    break;
                case 1:
                    fragment = new ConfigureReceiverDialogPage2TypeFragment();
                    break;
                case 2:
                    fragment = new ConfigureReceiverDialogPage3SetupFragment();
                    break;
                case 3:
                    fragment = new ConfigureReceiverDialogPage4SummaryFragment();
                    fragment.setTargetFragment(recyclerViewFragment, 0);

                    summaryFragment = (ConfigureReceiverDialogPage4SummaryFragment) fragment;
                    break;
                default:
                    break;
            }

            if (fragment != null && receiverId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong(RECEIVER_ID_KEY, receiverId);
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
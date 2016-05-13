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

import android.content.Context;
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
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ConfigurationDialogTabAdapter;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.configure_call.ConfigureCallDialogPage1ContactsFragment;
import eu.power_switch.gui.fragment.configure_call.ConfigureCallDialogPage2ActionsFragment;
import eu.power_switch.gui.fragment.configure_call.ConfigureCallDialogPage3SummaryFragment;
import eu.power_switch.gui.fragment.phone.CallEventsFragment;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to create or modify a Call Event
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class ConfigureCallEventDialog extends ConfigurationDialogTabbed {

    /**
     * ID of existing Call Event to Edit
     */
    public static final String CALL_EVENT_ID_KEY = "CallEventId";

    private long callEventId = -1;

    public static ConfigureCallEventDialog newInstance(long callEventId) {
        Bundle args = new Bundle();
        args.putLong(CALL_EVENT_ID_KEY, callEventId);

        ConfigureCallEventDialog fragment = new ConfigureCallEventDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening " + getClass().getSimpleName() + "...");
    }

    @Override
    protected boolean initializeFromExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(CALL_EVENT_ID_KEY)) {
            // init dialog using existing scene
            callEventId = arguments.getLong(CALL_EVENT_ID_KEY);
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment(), callEventId));
            return true;
        } else {
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment()));
            return false;
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_scene;
    }

    @Override
    protected void saveCurrentConfigurationToDatabase() {
        Log.d(this, "Saving call event");
        super.saveCurrentConfigurationToDatabase();
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                .call_event_will_be_gone_forever)
                .setPositiveButton
                        (android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DatabaseHandler.deleteCallEvent(callEventId);

                                    // notify scenes fragment
                                    CallEventsFragment.sendCallEventsChangedBroadcast(getActivity());

                                    StatusMessageHandler.showInfoMessage(((RecyclerViewFragment) getTargetFragment()).getRecyclerView(),
                                            R.string.call_deleted, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                                }

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
    }

    private static class CustomTabAdapter extends ConfigurationDialogTabAdapter {
        private Context context;
        private long callEventId;
        private ConfigurationDialogTabbedSummaryFragment setupFragment;
        private RecyclerViewFragment recyclerViewFragment;

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment) {
            super(fm);
            this.context = context;
            this.callEventId = -1;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment, long id) {
            super(fm);
            this.context = context;
            this.callEventId = id;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public ConfigurationDialogTabbedSummaryFragment getSummaryFragment() {
            return setupFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return context.getString(R.string.contacts);
                case 1:
                    return context.getString(R.string.actions);
                case 2:
                    return context.getString(R.string.summary);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = new ConfigureCallDialogPage1ContactsFragment();
                    break;
                case 1:
                    fragment = new ConfigureCallDialogPage2ActionsFragment();
                    break;
                case 2:
                    fragment = new ConfigureCallDialogPage3SummaryFragment();
                    fragment.setTargetFragment(recyclerViewFragment, 0);

                    setupFragment = (ConfigurationDialogTabbedSummaryFragment) fragment;
            }

            if (fragment != null && callEventId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong(CALL_EVENT_ID_KEY, callEventId);
                fragment.setArguments(bundle);
            }

            return fragment;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 3;
        }
    }

}

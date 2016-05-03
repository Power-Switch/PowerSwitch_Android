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
import eu.power_switch.gui.fragment.configure_room.ConfigureRoomDialogPage1Fragment;
import eu.power_switch.gui.fragment.configure_room.ConfigureRoomDialogPage2SummaryFragment;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.shared.log.Log;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.RoomWidgetProvider;

/**
 * Dialog to configure a Room
 */
public class ConfigureRoomDialog extends ConfigurationDialogTabbed {

    /**
     * ID of existing Room to Edit
     */
    public static final String ROOM_ID_KEY = "RoomId";

    private long roomId = -1;

    public static ConfigureRoomDialog newInstance(long roomId) {
        Bundle args = new Bundle();
        args.putLong(ROOM_ID_KEY, roomId);

        ConfigureRoomDialog fragment = new ConfigureRoomDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening " + getClass().getSimpleName() + "...");
    }

    @Override
    protected boolean initializeFromExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(ROOM_ID_KEY)) {
            // init dialog using existing receiver
            roomId = arguments.getLong(ROOM_ID_KEY);
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment(), roomId));
            return true;
        } else {
            // Create the adapter that will return a fragment
            // for each of the two primary sections of the app.
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment()));
            return false;
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_room;
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                .room_will_be_gone_forever)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DatabaseHandler.deleteRoom(roomId);

                            // notify rooms fragment
                            RoomsFragment.sendRoomChangedBroadcast(getActivity());
                            // scenes could change too if room was used in a scene
                            ScenesFragment.sendScenesChangedBroadcast(getActivity());

                            // update room widgets
                            RoomWidgetProvider.forceWidgetUpdate(getActivity());

                            // update wear data
                            UtilityService.forceWearDataUpdate(getActivity());

                            StatusMessageHandler.showInfoMessage(((RecyclerViewFragment) getTargetFragment()).getRecyclerView(),
                                    R.string.room_deleted, Snackbar.LENGTH_LONG);
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
        private long roomId;
        private ConfigurationDialogTabbedSummaryFragment summaryFragment;
        private RecyclerViewFragment recyclerViewFragment;

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment) {
            super(fm);
            this.context = context;
            this.roomId = -1;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment, long id) {
            super(fm);
            this.context = context;
            this.roomId = id;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public ConfigurationDialogTabbedSummaryFragment getSummaryFragment() {
            return summaryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return context.getString(R.string.name);
                case 1:
                    return context.getString(R.string.network);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = new ConfigureRoomDialogPage1Fragment();
                    break;
                case 1:
                    fragment = new ConfigureRoomDialogPage2SummaryFragment();
                    fragment.setTargetFragment(recyclerViewFragment, 0);

                    summaryFragment = (ConfigurationDialogTabbedSummaryFragment) fragment;
                    break;
                default:
                    break;
            }

            if (fragment != null && roomId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong(ROOM_ID_KEY, roomId);
                fragment.setArguments(bundle);
            }

            return fragment;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 2;
        }
    }

}
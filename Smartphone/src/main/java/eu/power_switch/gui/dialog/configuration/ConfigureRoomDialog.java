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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.adapter.ConfigurationDialogTabAdapter;
import eu.power_switch.gui.dialog.configuration.holder.RoomConfigurationHolder;
import eu.power_switch.gui.fragment.configure_room.ConfigureRoomDialogPage1;
import eu.power_switch.gui.fragment.configure_room.ConfigureRoomDialogPage2Gateways;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.RoomWidgetProvider;
import timber.log.Timber;

/**
 * Dialog to configure a Room
 */
public class ConfigureRoomDialog extends ConfigurationDialogTabbed<RoomConfigurationHolder> {

    public static ConfigureRoomDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(null, targetFragment);
    }

    public static ConfigureRoomDialog newInstance(Room room, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureRoomDialog     fragment                = new ConfigureRoomDialog();
        RoomConfigurationHolder roomConfigurationHolder = new RoomConfigurationHolder();
        if (room != null) {
            roomConfigurationHolder.setRoom(room);
        }
        fragment.setConfiguration(roomConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("Opening " + getClass().getSimpleName() + "...");
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) {
        Room room = getConfiguration().getRoom();

        if (room != null) {
            // init dialog using existing receiver
            try {
                List<Room> rooms = persistenceHandler.getAllRooms();

                getConfiguration().setExistingRooms(rooms);

                getConfiguration().setName(room.getName());
                getConfiguration().setReceivers(room.getReceivers());
                getConfiguration().setAssociatedGateways(room.getAssociatedGateways());

            } catch (Exception e) {
                Timber.e(e);
            }
        }

        setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment()));
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_room;
    }

    @Override
    protected void saveConfiguration() throws Exception {
        Timber.d("Saving Room...");
        persistenceHandler.updateRoom(getConfiguration().getRoom()
                .getId(), getConfiguration().getName(), getConfiguration().getAssociatedGateways());

        // save receiver order
        List<Receiver> receivers = getConfiguration().getReceivers();
        for (int position = 0; position < receivers.size(); position++) {
            Receiver receiver = receivers.get(position);
            persistenceHandler.setPositionOfReceiver(receiver.getId(), (long) position);
        }

        RoomsFragment.notifyRoomChanged();
        // scenes could change too if room was used in a scene
        ScenesFragment.notifySceneChanged();

        // update room widgets
        RoomWidgetProvider.forceWidgetUpdate(getActivity());

        // update wear data
        UtilityService.forceWearDataUpdate(getActivity());

        statusMessageHandler.showInfoMessage(getTargetFragment(), R.string.room_saved, Snackbar.LENGTH_LONG);
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                .setMessage(R.string.room_will_be_gone_forever)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            persistenceHandler.deleteRoom(getConfiguration().getRoom()
                                    .getId());

                            // notify rooms fragment
                            RoomsFragment.notifyRoomChanged();
                            // scenes could change too if room was used in a scene
                            ScenesFragment.notifySceneChanged();

                            // update room widgets
                            RoomWidgetProvider.forceWidgetUpdate(getActivity());

                            // update wear data
                            UtilityService.forceWearDataUpdate(getActivity());

                            statusMessageHandler.showInfoMessage(getTargetFragment(), R.string.room_deleted, Snackbar.LENGTH_LONG);
                        } catch (Exception e) {
                            statusMessageHandler.showErrorMessage(getActivity(), e);
                        }

                        // close dialog
                        getDialog().dismiss();
                    }
                })
                .setNeutralButton(android.R.string.cancel, null)
                .show();
    }

    private static class CustomTabAdapter extends ConfigurationDialogTabAdapter {

        private Context                                            context;
        private ConfigurationDialogTabbed<RoomConfigurationHolder> parentDialog;
        private Fragment                                           targetFragment;

        public CustomTabAdapter(ConfigurationDialogTabbed<RoomConfigurationHolder> parentDialog, FragmentManager fm, Fragment targetFragment) {
            super(fm);
            this.context = parentDialog.getActivity();
            this.parentDialog = parentDialog;
            this.targetFragment = targetFragment;
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
            Fragment fragment;

            switch (i) {
                case 0:
                default:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureRoomDialogPage1.class, parentDialog);
                    break;
                case 1:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureRoomDialogPage2Gateways.class, parentDialog);
                    break;
            }

            fragment.setTargetFragment(targetFragment, 0);

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
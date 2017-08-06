/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog.configuration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.List;

import eu.power_switch.R;
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
public class ConfigureRoomDialog extends ConfigurationDialog<RoomConfigurationHolder> {

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
    protected void initializeFromExistingData(Bundle arguments) throws Exception {
        Room room = getConfiguration().getRoom();

        if (room != null) {
            // init dialog using existing receiver
            List<Room> rooms = persistenceHandler.getAllRooms();

            getConfiguration().setExistingRooms(rooms);

            getConfiguration().setName(room.getName());
            getConfiguration().setReceivers(room.getReceivers());
            getConfiguration().setAssociatedGateways(room.getAssociatedGateways());
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_room;
    }

    @Override
    protected void addPageEntries(List<PageEntry<RoomConfigurationHolder>> pageEntries) {
        pageEntries.add(new PageEntry<>(R.string.name, ConfigureRoomDialogPage1.class));
        pageEntries.add(new PageEntry<>(R.string.network, ConfigureRoomDialogPage2Gateways.class));
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
    }

    @Override
    protected void deleteConfiguration() throws Exception {
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
    }

}
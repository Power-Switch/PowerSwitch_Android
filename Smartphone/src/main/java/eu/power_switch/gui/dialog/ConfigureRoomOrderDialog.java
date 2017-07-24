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

package eu.power_switch.gui.dialog;

import android.os.Bundle;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogTabbed;
import eu.power_switch.gui.dialog.configuration.PageEntry;
import eu.power_switch.gui.dialog.configuration.holder.RoomOrderConfigurationHolder;
import eu.power_switch.gui.fragment.configure_room.ConfigureRoomOrderDialogPage;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.wear.service.UtilityService;

/**
 * Dialog to edit a Room
 */
public class ConfigureRoomOrderDialog extends ConfigurationDialogTabbed<RoomOrderConfigurationHolder> {

    public static ConfigureRoomOrderDialog newInstance(long apartmentId) {
        Bundle args = new Bundle();

        ConfigureRoomOrderDialog     fragment                     = new ConfigureRoomOrderDialog();
        RoomOrderConfigurationHolder roomOrderConfigurationHolder = new RoomOrderConfigurationHolder();
        roomOrderConfigurationHolder.setApartmentId(apartmentId);
        fragment.setConfiguration(roomOrderConfigurationHolder);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) throws Exception {
        Long apartmentId = getConfiguration().getApartmentId();

        List<Room> rooms = persistenceHandler.getRooms(apartmentId);
        getConfiguration().setRooms(rooms);
    }


    @Override
    protected int getDialogTitle() {
        return R.string.reorder_rooms;
    }

    @Override
    protected void addPageEntries(List<PageEntry<RoomOrderConfigurationHolder>> pageEntries) {
        pageEntries.add(new PageEntry<>(R.string.order, ConfigureRoomOrderDialogPage.class));
    }


    @Override
    protected void saveConfiguration() throws Exception {
        // save room order
        List<Room> rooms = getConfiguration().getRooms();

        for (int position = 0; position < rooms.size(); position++) {
            Room room = rooms.get(position);
            persistenceHandler.setPositionOfRoom(room.getId(), (long) position);
        }

        // notify rooms fragment
        RoomsFragment.notifyRoomChanged();

        // update wear data
        UtilityService.forceWearDataUpdate(getActivity());
    }

    @Override
    protected boolean isDeletable() {
        return false;
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        // nothing to do here
    }

}
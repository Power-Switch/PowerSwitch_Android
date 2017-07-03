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

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.OnStartDragListener;
import eu.power_switch.gui.adapter.RoomNameRecyclerViewAdapter;
import eu.power_switch.gui.adapter.SimpleItemTouchHelperCallback;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialog;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.wear.service.UtilityService;

/**
 * Dialog to edit a Room
 */
public class EditRoomOrderDialog extends ConfigurationDialog implements OnStartDragListener {

    /**
     * ID of existing Apartment to edit room order
     */
    public static final String APARTMENT_ID_KEY = "ApartmentId";

    private long apartmentId = -1;

    private ArrayList<Room> rooms = new ArrayList<>();
    private RoomNameRecyclerViewAdapter roomNameRecyclerViewAdapter;
    private ItemTouchHelper             itemTouchHelper;

    private ButterKnifeViewHolder viewHolder = new ButterKnifeViewHolder();

    public static EditRoomOrderDialog newInstance(long apartmentId) {
        Bundle args = new Bundle();
        args.putLong(APARTMENT_ID_KEY, apartmentId);

        EditRoomOrderDialog fragment = new EditRoomOrderDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_edit_room_order_content, container);
        ButterKnife.bind(viewHolder, contentView);

        roomNameRecyclerViewAdapter = new RoomNameRecyclerViewAdapter(getContext(), rooms, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        viewHolder.recyclerViewRooms.setLayoutManager(linearLayoutManager);
        viewHolder.recyclerViewRooms.setAdapter(roomNameRecyclerViewAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(roomNameRecyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(viewHolder.recyclerViewRooms);

        return contentView;
    }

    @Override
    protected boolean initExistingData(Bundle arguments) {
        apartmentId = arguments.getLong(APARTMENT_ID_KEY);

        try {
            List<Room> rooms = DatabaseHandler.getRooms(apartmentId);
            this.rooms.addAll(rooms);

            roomNameRecyclerViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }

        return false;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        setModified(true);
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    protected int getDialogTitle() {
        return R.string.reorder_rooms;
    }

    @Override
    protected boolean isValid() {
        return true;
    }

    @Override
    protected void saveCurrentConfigurationToDatabase() {
        try {
            // save room order
            for (int position = 0; position < rooms.size(); position++) {
                Room room = rooms.get(position);
                DatabaseHandler.setPositionOfRoom(room.getId(), (long) position);
            }

            // notify rooms fragment
            RoomsFragment.notifyRoomChanged();

            // update wear data
            UtilityService.forceWearDataUpdate(getActivity());

            StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.room_saved, Snackbar.LENGTH_LONG);
            getDialog().dismiss();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        // nothing to delete here
    }

    class ButterKnifeViewHolder {
        @BindView(R.id.recyclerView)
        RecyclerView recyclerViewRooms;
    }
}
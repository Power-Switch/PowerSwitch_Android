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

package eu.power_switch.gui.fragment.configure_room;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.adapter.OnItemMovedListener;
import eu.power_switch.gui.adapter.OnStartDragListener;
import eu.power_switch.gui.adapter.RoomNameRecyclerViewAdapter;
import eu.power_switch.gui.adapter.SimpleItemTouchHelperCallback;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.RoomOrderConfigurationHolder;
import eu.power_switch.obj.Room;

/**
 * Dialog to edit a Room
 */
public class ConfigureRoomOrderDialogPage extends ConfigurationDialogPage<RoomOrderConfigurationHolder> implements OnStartDragListener, OnItemMovedListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerViewRooms;

    private ArrayList<Room> rooms = new ArrayList<>();
    private RoomNameRecyclerViewAdapter roomNameRecyclerViewAdapter;
    private ItemTouchHelper             itemTouchHelper;

    @Override
    protected void onRootViewInflated(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        roomNameRecyclerViewAdapter = new RoomNameRecyclerViewAdapter(getContext(), rooms);
        roomNameRecyclerViewAdapter.setOnStartDragListener(this);
        roomNameRecyclerViewAdapter.setOnItemMovedListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerViewRooms.setLayoutManager(linearLayoutManager);
        recyclerViewRooms.setAdapter(roomNameRecyclerViewAdapter);


        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(roomNameRecyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerViewRooms);

        initExistingData();
    }

    @Override
    protected void showTutorial() {
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_edit_room_order_content;
    }

    private void initExistingData() {
        List<Room> rooms = getConfiguration().getRooms();

        if (rooms != null) {
            this.rooms.addAll(rooms);
            roomNameRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        getConfiguration().setRooms(rooms);
        notifyConfigurationChanged();
    }
}
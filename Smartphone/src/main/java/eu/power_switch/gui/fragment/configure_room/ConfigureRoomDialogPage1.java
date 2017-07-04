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

package eu.power_switch.gui.fragment.configure_room;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.OnItemMovedListener;
import eu.power_switch.gui.adapter.OnStartDragListener;
import eu.power_switch.gui.adapter.ReceiverNameRecyclerViewAdapter;
import eu.power_switch.gui.adapter.SimpleItemTouchHelperCallback;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.RoomConfigurationHolder;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * Dialog to edit a Room
 */
public class ConfigureRoomDialogPage1 extends ConfigurationDialogPage<RoomConfigurationHolder> implements OnStartDragListener {

    @BindView(R.id.editText_room_name)
    EditText        name;
    @BindView(R.id.room_name_text_input_layout)
    TextInputLayout floatingName;

    @BindView(R.id.recyclerview_list_of_receivers)
    RecyclerView listOfReceivers;

    private String originalName;

    private LinkedList<String> roomNames;
    private ItemTouchHelper    itemTouchHelper;

    private ArrayList<Receiver>             receivers;
    private ReceiverNameRecyclerViewAdapter receiverNameRecyclerViewAdapter;

    /**
     * Used to notify the summary page that some info has changed
     *
     * @param name      name of room
     * @param receivers list of receivers (with changed order)
     */
    public void updateConfiguration(String name, List<Receiver> receivers) {
        getConfiguration().setName(name);
        getConfiguration().setReceivers(receivers);

        notifyConfigurationChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // restore name
        name.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateConfiguration(getCurrentRoomName(), receivers);
            }
        });

        receivers = new ArrayList<>();
        receiverNameRecyclerViewAdapter = new ReceiverNameRecyclerViewAdapter(getContext(), receivers, this);
        receiverNameRecyclerViewAdapter.setOnItemMovedListener(new OnItemMovedListener() {
            @Override
            public void onItemMoved(int fromPosition, int toPosition) {
                updateConfiguration(getCurrentRoomName(), receivers);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        listOfReceivers.setLayoutManager(linearLayoutManager);
        listOfReceivers.setAdapter(receiverNameRecyclerViewAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(receiverNameRecyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listOfReceivers);

        initExistingData();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_room_page_1;
    }

    private void initExistingData() {
        Long roomId = getConfiguration().getId();
        if (roomId != null) {
            try {
                Room currentRoom = DatabaseHandler.getRoom(roomId);
                originalName = currentRoom.getName();
                name.setText(originalName);

                receivers.addAll(currentRoom.getReceivers());
                receiverNameRecyclerViewAdapter.notifyDataSetChanged();

                List<Room> rooms = DatabaseHandler.getRooms(SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID));
                roomNames = new LinkedList<>();
                for (Room room : rooms) {
                    roomNames.add(room.getName());
                }
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    private boolean isValid() {
        if (getCurrentRoomName().equals(originalName)) {
            floatingName.setError(null);
            return true;
        } else if (getCurrentRoomName().length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            return false;
        } else if (checkRoomAlreadyExists()) {
            floatingName.setError(getString(R.string.room_already_exists));
            return false;
        } else {
            floatingName.setError(null);
            return true;
        }
    }

    private boolean checkRoomAlreadyExists() {
        for (String roomName : roomNames) {
            if (roomName.equalsIgnoreCase(getCurrentRoomName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    private String getCurrentRoomName() {
        return name.getText()
                .toString()
                .trim();
    }

}
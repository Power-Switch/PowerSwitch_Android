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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
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

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.OnItemMovedListener;
import eu.power_switch.gui.adapter.OnStartDragListener;
import eu.power_switch.gui.adapter.ReceiverNameRecyclerViewAdapter;
import eu.power_switch.gui.adapter.SimpleItemTouchHelperCallback;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigureRoomDialog;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Dialog to edit a Room
 */
public class ConfigureRoomDialogPage1Fragment extends ConfigurationDialogFragment implements OnStartDragListener {

    public static final String KEY_NAME = "name";
    public static final String KEY_RECEIVERS = "receivers";

    private View rootView;
    private String originalName;
    private EditText name;
    private TextInputLayout floatingName;

    private Room currentRoom;
    private LinkedList<String> roomNames;
    private ItemTouchHelper itemTouchHelper;

    private ArrayList<Receiver> receivers;
    private ReceiverNameRecyclerViewAdapter receiverNameRecyclerViewAdapter;
    private RecyclerView listOfReceivers;

    /**
     * Used to notify the summary page that some info has changed
     *
     * @param context   any suitable context
     * @param name      name of room
     * @param receivers list of receivers (with changed order)
     */
    public static void sendGatewayDetailsChangedBroadcast(Context context, String name, ArrayList<Receiver> receivers) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_ROOM_NAME_CHANGED);
        intent.putExtra(KEY_NAME, name);
        intent.putExtra(KEY_RECEIVERS, receivers);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_room_page_1, container, false);

        // restore name
        floatingName = (TextInputLayout) rootView.findViewById(R.id.room_name_text_input_layout);
        name = (EditText) rootView.findViewById(R.id.editText_room_name);
        name.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sendGatewayDetailsChangedBroadcast(getContext(), getCurrentRoomName(), receivers);
            }
        });

        receivers = new ArrayList<>();
        listOfReceivers = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_receivers);
        receiverNameRecyclerViewAdapter = new ReceiverNameRecyclerViewAdapter(getContext(), receivers, this);
        receiverNameRecyclerViewAdapter.setOnItemMovedListener(new OnItemMovedListener() {
            @Override
            public void onItemMoved(int fromPosition, int toPosition) {
                sendGatewayDetailsChangedBroadcast(getContext(), getCurrentRoomName(), receivers);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        listOfReceivers.setLayoutManager(linearLayoutManager);
        listOfReceivers.setAdapter(receiverNameRecyclerViewAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(receiverNameRecyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listOfReceivers);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureRoomDialog.ROOM_ID_KEY)) {
            long roomId = args.getLong(ConfigureRoomDialog.ROOM_ID_KEY);
            initExistingData(roomId);
        }

        return rootView;
    }

    private boolean initExistingData(long roomId) {

        try {
            currentRoom = DatabaseHandler.getRoom(roomId);
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

        return true;
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
        return name.getText().toString().trim();
    }

}
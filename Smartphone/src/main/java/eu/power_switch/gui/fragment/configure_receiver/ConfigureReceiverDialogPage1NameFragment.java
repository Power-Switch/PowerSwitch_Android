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

package eu.power_switch.gui.fragment.configure_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.receiver.ReceiverAlreadyExistsException;
import eu.power_switch.gui.dialog.CreateRoomDialog;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * "Name" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage1NameFragment extends Fragment {

    private View rootView;

    private TextInputLayout floatingName;
    private EditText name;

    private ListView roomsListView;
    private ArrayAdapter roomNamesAdapter;
    private ArrayList<String> roomList = new ArrayList<>();

    private FloatingActionButton addRoomFAB;
    private String originalName;
    private BroadcastReceiver broadcastReceiver;

    /**
     * Used to notify the summary page that some info has changed
     *
     * @param context  any suitable context
     * @param name     Current name of the Receiver
     * @param roomName Current name of Room
     */
    public static void sendNameRoomChangedBroadcast(Context context, String name, String roomName) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_NAME_ROOM_CHANGED);
        intent.putExtra("name", name);
        intent.putExtra("roomName", roomName);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Used to notify this page that a room has been added to the list
     *
     * @param context     any suitable context
     * @param newRoomName name of added room
     */
    public static void sendRoomAddedBroadcast(Context context, String newRoomName) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_ROOM_ADDED);
        intent.putExtra("newRoomName", newRoomName);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_receiver_page_1, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateRoomNamesList();

                if (intent.hasExtra("newRoomName")) {
                    String newRoomName = intent.getStringExtra("newRoomName");
                    roomsListView.setItemChecked(roomNamesAdapter.getPosition(newRoomName), true);
                }
            }
        };

        floatingName = (TextInputLayout) rootView.findViewById(R.id.receiver_name_text_input_layout);
        name = (EditText) rootView.findViewById(R.id.editText_receiver_name);
        name.requestFocus();
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidity();
            }
        });

        roomsListView = (ListView) rootView.findViewById(R.id.listView_rooms);
        roomNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, roomList);
        roomsListView.setAdapter(roomNamesAdapter);
        roomsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkValidity();
            }
        });

        updateRoomNamesList();

        addRoomFAB = (FloatingActionButton) rootView.findViewById(R.id.add_room_fab);
        final Fragment fragment = this;
        addRoomFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateRoomDialog createRoomDialog = new CreateRoomDialog();
                createRoomDialog.setTargetFragment(fragment, 0);
                createRoomDialog.show(getFragmentManager(), null);
            }
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey("ReceiverId")) {
            long receiverId = args.getLong("ReceiverId");
            initializeReceiverData(receiverId);
        }
        checkValidity();

        return rootView;
    }

    private void initializeReceiverData(long receiverId) {
        Receiver receiver = DatabaseHandler.getReceiver(receiverId);
        Room room = DatabaseHandler.getRoom(receiver.getRoomId());

        originalName = receiver.getName();
        name.setText(receiver.getName());
        roomsListView.setItemChecked(roomNamesAdapter.getPosition(room.getName()), true);
    }

    private void updateRoomNamesList() {
        // Get Rooms
        roomList.clear();
        List<Room> rooms = DatabaseHandler.getAllRooms();
        for (Room room : rooms) {
            roomList.add(room.getName());
        }
        roomNamesAdapter.notifyDataSetChanged();
    }

    private boolean checkValidity() {
        // TODO: Performance Optimierung
        String currentReceiverName = getCurrentName();
        String currentRoomName = getCheckedRoomName();

        if (currentReceiverName.length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            floatingName.setErrorEnabled(true);
            sendNameRoomChangedBroadcast(getActivity(), null, getCheckedRoomName());
            return false;
        }

        if (currentRoomName == null) {
            floatingName.setError(getString(R.string.no_room_selected));
            floatingName.setErrorEnabled(true);
            sendNameRoomChangedBroadcast(getActivity(), getCurrentName(), null);
            return false;
        }

        if (!currentReceiverName.equals(originalName)) {
            try {
                Room selectedRoom = DatabaseHandler.getRoom(currentRoomName);
                for (Receiver receiver : selectedRoom.getReceivers()) {
                    if (receiver.getName().equals(currentReceiverName)) {
                        throw new ReceiverAlreadyExistsException();
                    }
                }
            } catch (ReceiverAlreadyExistsException e) {
                Log.e(e);
                floatingName.setError(getString(R.string.receiver_already_exists));
                floatingName.setErrorEnabled(true);
                sendNameRoomChangedBroadcast(getActivity(), null, getCheckedRoomName());
                return false;
            } catch (Exception e) {
                Log.e(e);
                floatingName.setError(getString(R.string.unknown_error));
                floatingName.setErrorEnabled(true);
                sendNameRoomChangedBroadcast(getActivity(), null, null);
                return false;
            }
        }

        floatingName.setError(null);
        floatingName.setErrorEnabled(false);
        sendNameRoomChangedBroadcast(getActivity(), getCurrentName(), getCheckedRoomName());
        return true;
    }

    private String getCurrentName() {
        return name.getText().toString().trim();
    }

    private String getCheckedRoomName() {
        try {
            int checkedPosition = roomsListView.getCheckedItemPosition();
            return roomNamesAdapter.getItem(checkedPosition).toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_ROOM_ADDED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

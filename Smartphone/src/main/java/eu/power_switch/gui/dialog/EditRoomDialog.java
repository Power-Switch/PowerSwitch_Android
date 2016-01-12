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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
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
import eu.power_switch.gui.adapter.OnStartDragListener;
import eu.power_switch.gui.adapter.ReceiverNameRecyclerViewAdapter;
import eu.power_switch.gui.adapter.SimpleItemTouchHelperCallback;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.log.Log;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import eu.power_switch.widget.provider.RoomWidgetProvider;
import eu.power_switch.widget.provider.SceneWidgetProvider;

/**
 * Dialog to edit a Room
 */
public class EditRoomDialog extends ConfigurationDialog implements OnStartDragListener {

    /**
     * ID of existing Room to Edit
     */
    public static final String ROOM_ID_KEY = "RoomId";

    private View rootView;
    private String originalName;
    private EditText name;
    private TextInputLayout floatingName;

    private Room currentRoom;
    private LinkedList<String> roomNames;
    private ItemTouchHelper itemTouchHelper;
    private long roomId;

    private ArrayList<Receiver> receivers;
    private ReceiverNameRecyclerViewAdapter receiverNameRecyclerViewAdapter;
    private RecyclerView listOfReceivers;

    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_edit_room_content, null);

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
                notifyConfigurationChanged();
            }
        });

        receivers = new ArrayList<>();
        listOfReceivers = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_receivers);
        receiverNameRecyclerViewAdapter = new ReceiverNameRecyclerViewAdapter(getContext(), receivers, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        listOfReceivers.setLayoutManager(linearLayoutManager);
        listOfReceivers.setAdapter(receiverNameRecyclerViewAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(receiverNameRecyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listOfReceivers);

        setDeleteAction(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                        .room_will_be_gone_forever)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DatabaseHandler.deleteRoom(roomId);

                                    // notify rooms fragment
                                    RoomsFragment.sendReceiverChangedBroadcast(getActivity());
                                    // notify scenes fragment
                                    ScenesFragment.sendScenesChangedBroadcast(getActivity());
                                    // notify timers fragment
                                    TimersFragment.sendTimersChangedBroadcast(getActivity());

                                    // update receiver widgets
                                    ReceiverWidgetProvider.forceWidgetUpdate(getActivity());
                                    // update room widgets
                                    RoomWidgetProvider.forceWidgetUpdate(getActivity());
                                    // update scene widgets
                                    SceneWidgetProvider.forceWidgetUpdate(getActivity());

                                    StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                                            R.string.room_deleted, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    Log.e(e);
                                    StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
                                }

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
            }
        });

        return rootView;
    }

    @Override
    protected void initExistingData(Bundle arguments) {
        roomId = arguments.getLong(ROOM_ID_KEY);

        try {
            currentRoom = DatabaseHandler.getRoom(roomId);
            originalName = currentRoom.getName();
            name.setText(originalName);

            receivers.addAll(currentRoom.getReceivers());
            receiverNameRecyclerViewAdapter.notifyDataSetChanged();

            List<Room> rooms = DatabaseHandler.getRooms(SmartphonePreferencesHandler.getCurrentApartmentId());
            roomNames = new LinkedList<>();
            for (Room room : rooms) {
                roomNames.add(room.getName());
            }
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_room;
    }

    @Override
    protected boolean checkValidity() {
        if (getCurrentRoomName().equals(originalName)) {
            floatingName.setError(null);
            floatingName.setErrorEnabled(false);
            return true;
        } else if (getCurrentRoomName().length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            floatingName.setErrorEnabled(true);
            return false;
        } else if (roomNames.contains(getCurrentRoomName())) {
            floatingName.setError(getString(R.string.room_already_exists));
            floatingName.setErrorEnabled(true);
            return false;
        } else {
            floatingName.setError(null);
            floatingName.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    protected void saveCurrentConfigurationToDatabase() {
        try {
            DatabaseHandler.updateRoom(roomId, getCurrentRoomName());

            // save receiver order
            for (int position = 0; position < receivers.size(); position++) {
                Receiver receiver = receivers.get(position);
                DatabaseHandler.setPositionInRoom(receiver.getId(), (long) position);
            }

            RoomsFragment.sendReceiverChangedBroadcast(getActivity());

            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(), R.string.room_saved, Snackbar.LENGTH_LONG);
            getDialog().dismiss();
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        setModified(true);
        itemTouchHelper.startDrag(viewHolder);
    }

    private String getCurrentRoomName() {
        return name.getText().toString().trim();
    }
}
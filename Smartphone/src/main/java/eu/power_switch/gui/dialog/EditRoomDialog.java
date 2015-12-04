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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

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
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.gui.fragment.main.TimersFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.widget.activity.ConfigureReceiverWidgetActivity;
import eu.power_switch.widget.activity.ConfigureRoomWidgetActivity;
import eu.power_switch.widget.activity.ConfigureSceneWidgetActivity;

/**
 * Dialog to edit a Room
 */
public class EditRoomDialog extends DialogFragment implements OnStartDragListener {

    private boolean modified = false;

    private View rootView;
    private String originalName;
    private AppCompatEditText name;
    private TextInputLayout floatingName;
    private ImageButton imageButtonSave;
    private ImageButton imageButtonCancel;
    private ImageButton imageButtonDelete;

    private Room currentRoom;
    private LinkedList<String> roomNames;
    private ItemTouchHelper itemTouchHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle roomData = getArguments();
        final long roomId = roomData.getLong("id");
        originalName = roomData.getString("name");
        List<Room> rooms = DatabaseHandler.getAllRooms();
        roomNames = new LinkedList<>();
        for (Room room : rooms) {
            roomNames.add(room.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        rootView = inflater.inflate(R.layout.dialog_edit_room, null);
        builder.setView(rootView);

        // restore name
        floatingName = (TextInputLayout) rootView.findViewById(R.id.room_name_text_input_layout);
        name = (AppCompatEditText) rootView.findViewById(R.id.editText_room_name);
        name.setText(originalName);
        name.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                modified = true;
                checkValidity();
            }
        });

        currentRoom = DatabaseHandler.getRoom(roomId);

        final ArrayList<Receiver> receiverList = new ArrayList<>(currentRoom.getReceivers());
        RecyclerView listOfReceivers = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_receivers);
        ReceiverNameRecyclerViewAdapter adapter = new ReceiverNameRecyclerViewAdapter(getContext(), receiverList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        listOfReceivers.setLayoutManager(linearLayoutManager);
        listOfReceivers.setAdapter(adapter);

        // TODO: interface evtl. in ArrayAdapter verschieben?
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listOfReceivers);

        imageButtonDelete = (ImageButton) rootView.findViewById(R.id.imageButton_delete);
        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                        .room_will_be_gone_forever)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseHandler.deleteRoom(roomId);

                                // notify rooms fragment
                                RoomsFragment.sendReceiverChangedBroadcast(getActivity());
                                // notify scenes fragment
                                ScenesFragment.sendScenesChangedBroadcast(getActivity());
                                // notify timers fragment
                                TimersFragment.sendTimersChangedBroadcast(getActivity());

                                // update receiver widgets
                                ConfigureReceiverWidgetActivity.forceWidgetUpdate(getActivity());
                                // update room widgets
                                ConfigureRoomWidgetActivity.forceWidgetUpdate(getActivity());
                                // update scene widgets
                                ConfigureSceneWidgetActivity.forceWidgetUpdate(getActivity());

                                StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                                        R.string.room_deleted, Snackbar.LENGTH_LONG);

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
            }
        });

        imageButtonCancel = (ImageButton) rootView.findViewById(R.id.imageButton_cancel);
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        imageButtonSave = (ImageButton) rootView.findViewById(R.id.imageButton_save);
        imageButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!modified) {
                    getDialog().dismiss();
                } else {
                    DatabaseHandler.updateRoom(roomId, getRoomName());

                    // save receiver order
                    for (int position = 0; position < receiverList.size(); position++) {
                        Receiver receiver = receiverList.get(position);
                        DatabaseHandler.setPositionInRoom(receiver.getId(), (long) position);
                    }

                    RoomsFragment.sendReceiverChangedBroadcast(getActivity());

                    StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(), R.string.room_saved, Snackbar.LENGTH_LONG);
                    getDialog().dismiss();
                }
            }
        });

        checkValidity();

        return rootView;
    }

    private void checkValidity() {
        if (getRoomName().equals(originalName)) {
            setSaveButtonState(true);
            floatingName.setError(null);
            floatingName.setErrorEnabled(false);
        } else if (getRoomName().length() <= 0) {
            setSaveButtonState(false);
            floatingName.setError(getString(R.string.please_enter_name));
            floatingName.setErrorEnabled(true);
        } else if (roomNames.contains(getRoomName())) {
            setSaveButtonState(false);
            floatingName.setError(getString(R.string.room_already_exists));
            floatingName.setErrorEnabled(true);
        } else {
            setSaveButtonState(true);
            floatingName.setError(null);
            floatingName.setErrorEnabled(false);
        }
    }

    private void setSaveButtonState(boolean enabled) {
        if (enabled) {
            imageButtonSave.setColorFilter(getResources().getColor(eu.power_switch.shared.R.color
                    .active_green));
            imageButtonSave.setClickable(true);
        } else {
            imageButtonSave.setColorFilter(getResources().getColor(eu.power_switch.shared.R.color
                    .inactive_gray));
            imageButtonSave.setClickable(false);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setTitle(R.string.edit_room);
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        return dialog;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        modified = true;
        itemTouchHelper.startDrag(viewHolder);
    }

    private String getRoomName() {
        return name.getText().toString().trim();
    }
}
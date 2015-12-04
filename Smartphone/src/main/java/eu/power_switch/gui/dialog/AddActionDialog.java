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
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.shared.log.Log;
import eu.power_switch.timer.action.Action;
import eu.power_switch.timer.action.ReceiverAction;
import eu.power_switch.timer.action.RoomAction;
import eu.power_switch.timer.action.SceneAction;

/**
 * Dialog to select an action configuration
 * <p/>
 * Created by Markus on 28.09.2015.
 */
public abstract class AddActionDialog extends DialogFragment {

    private Dialog dialog;
    private View rootView;

    private int defaultTextColor;
    private String currentActionType = Action.ACTION_TYPE_RECEIVER;
    private RadioButton radioButtonReceiverAction;
    private RadioButton radioButtonRoomAction;
    private RadioButton radioButtonSceneAction;

    private LinearLayout linearLayoutReceiverAction;
    private LinearLayout linearLayoutRoomAction;
    private LinearLayout linearLayoutSceneAction;
    private AppCompatSpinner spinner_receiver_action_room;
    private AppCompatSpinner spinner_receiver_action_receiver;
    private AppCompatSpinner spinner_receiver_action_button;
    private AppCompatSpinner spinner_room_action_room;
    private AppCompatSpinner spinner_room_action_button;
    private AppCompatSpinner spinner_scene_action_scene;
    private ArrayList<String> buttonNamesReceiver;
    private ArrayAdapter<String> receiverSpinnerArrayAdapter;
    private ArrayAdapter<String> buttonSpinnerArrayAdapter;
    private ArrayList<String> receiverNames;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.dialog_add_action, null);
        builder.setView(rootView);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.radioButton_receiver_action) {
                    radioButtonRoomAction.setChecked(false);
                    radioButtonSceneAction.setChecked(false);
                    updateActionType(Action.ACTION_TYPE_RECEIVER);
                } else if (v.getId() == R.id.radioButton_room_action) {
                    radioButtonReceiverAction.setChecked(false);
                    radioButtonSceneAction.setChecked(false);
                    updateActionType(Action.ACTION_TYPE_ROOM);
                } else if (v.getId() == R.id.radioButton_scene_action) {
                    radioButtonReceiverAction.setChecked(false);
                    radioButtonRoomAction.setChecked(false);
                    updateActionType(Action.ACTION_TYPE_SCENE);
                }
                setPositiveButtonVisibility(checkValidity());
            }
        };

        // TimerAction Type Selection
        radioButtonReceiverAction = (RadioButton) rootView.findViewById(R.id.radioButton_receiver_action);
        radioButtonReceiverAction.setOnClickListener(onClickListener);
        radioButtonRoomAction = (RadioButton) rootView.findViewById(R.id.radioButton_room_action);
        radioButtonRoomAction.setOnClickListener(onClickListener);
        radioButtonSceneAction = (RadioButton) rootView.findViewById(R.id.radioButton_scene_action);
        radioButtonSceneAction.setOnClickListener(onClickListener);

        ArrayList<Room> availableRooms = (ArrayList<Room>) DatabaseHandler.getAllRooms();
        ArrayList<String> roomNames = new ArrayList<>();
        for (Room room : availableRooms) {
            roomNames.add(room.getName());
        }

        ArrayList<Receiver> availableReceivers = (ArrayList<Receiver>) DatabaseHandler.getAllReceivers();
        receiverNames = new ArrayList<>();
        for (Receiver receiver : availableReceivers) {
            receiverNames.add(receiver.getName());
        }

        buttonNamesReceiver = new ArrayList<>();
        final ArrayList<String> buttonNamesAll = new ArrayList<>();
        for (Receiver receiver : availableReceivers) {
            for (Button button : receiver.getButtons()) {
                if (!buttonNamesAll.contains(button.getName())) {
                    buttonNamesAll.add(button.getName());
                }
            }
        }

        ArrayList<Scene> availableScenes = (ArrayList<Scene>) DatabaseHandler.getAllScenes();
        ArrayList<String> sceneNames = new ArrayList<>();
        for (Scene scene : availableScenes) {
            sceneNames.add(scene.getName());
        }

        // Receiver Action
        linearLayoutReceiverAction = (LinearLayout) rootView.findViewById(R.id.linearLayout_receiver_action);

        spinner_receiver_action_room = (AppCompatSpinner) rootView.findViewById(R.id.spinner_receiver_action_room);
        ArrayAdapter<String> roomSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, roomNames);
        roomSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_receiver_action_room.setAdapter(roomSpinnerArrayAdapter);
        spinner_receiver_action_room.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateReceiverList();
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        });

        spinner_receiver_action_receiver = (AppCompatSpinner) rootView.findViewById(R.id.spinner_receiver_action_receiver);
        receiverSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, receiverNames);
        receiverSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_receiver_action_receiver.setAdapter(receiverSpinnerArrayAdapter);
        spinner_receiver_action_receiver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateReceiverButtonList();
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        });

        spinner_receiver_action_button = (AppCompatSpinner) rootView.findViewById(R.id.spinner_receiver_action_button);
        buttonSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, buttonNamesReceiver);
        buttonSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_receiver_action_button.setAdapter(buttonSpinnerArrayAdapter);

        updateReceiverButtonList();

        // Room Action
        linearLayoutRoomAction = (LinearLayout) rootView.findViewById(R.id.linearLayout_room_action);

        spinner_room_action_room = (AppCompatSpinner) rootView.findViewById(R.id.spinner_room_action_room);
        spinner_room_action_room.setAdapter(roomSpinnerArrayAdapter);

        spinner_room_action_button = (AppCompatSpinner) rootView.findViewById(R.id.spinner_room_action_button);
        ArrayAdapter<String> buttonAllSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, buttonNamesAll);
        buttonAllSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_room_action_button.setAdapter(buttonAllSpinnerArrayAdapter);

        // Scene Action
        linearLayoutSceneAction = (LinearLayout) rootView.findViewById(R.id.linearLayout_scene_action);

        spinner_scene_action_scene = (AppCompatSpinner) rootView.findViewById(R.id.spinner_scene_action_scene);
        ArrayAdapter<String> sceneSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sceneNames);
        sceneSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_scene_action_scene.setAdapter(sceneSpinnerArrayAdapter);

        builder.setTitle(R.string.add_action);
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addCurrentSelection();
                sendDataChangedBroadcast(getContext());
            }
        });

        builder.setNeutralButton(android.R.string.cancel, null);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        defaultTextColor = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).getTextColors()
                .getDefaultColor();

        return dialog;
    }

    private void updateReceiverList() {
        receiverNames.clear();

        try {
            Room selectedRoom = DatabaseHandler.getRoom(spinner_receiver_action_room.getSelectedItem().toString());
            if (selectedRoom != null) {
                for (Receiver receiver : selectedRoom.getReceivers()) {
                    receiverNames.add(receiver.getName());
                }
                spinner_receiver_action_receiver.setSelection(0);
                updateReceiverButtonList();
            }
        } catch (Exception e) {
            Log.e(e);
        }

        receiverSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private void updateReceiverButtonList() {
        buttonNamesReceiver.clear();

        try {
            Room selectedRoom = DatabaseHandler.getRoom(spinner_receiver_action_room.getSelectedItem().toString());
            Receiver selectedReceiver = DatabaseHandler.getReceiverByRoomId(selectedRoom.getId(), spinner_receiver_action_receiver
                    .getSelectedItem().toString());

            if (selectedReceiver != null) {
                for (Button button : selectedReceiver.getButtons()) {
                    buttonNamesReceiver.add(button.getName());
                }
                if (buttonNamesReceiver.size() > 0) {
                    spinner_receiver_action_button.setSelection(0);
                }
            }
        } catch (Exception e) {
            Log.e(e);
        }

        buttonSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private void updateActionType(String timerActionType) {
        currentActionType = timerActionType;
        if (Action.ACTION_TYPE_RECEIVER.equals(timerActionType)) {
            linearLayoutReceiverAction.setVisibility(View.VISIBLE);
            linearLayoutRoomAction.setVisibility(View.GONE);
            linearLayoutSceneAction.setVisibility(View.GONE);
        } else if (Action.ACTION_TYPE_ROOM.equals(timerActionType)) {
            linearLayoutReceiverAction.setVisibility(View.GONE);
            linearLayoutRoomAction.setVisibility(View.VISIBLE);
            linearLayoutSceneAction.setVisibility(View.GONE);
        } else if (Action.ACTION_TYPE_SCENE.equals(timerActionType)) {
            linearLayoutReceiverAction.setVisibility(View.GONE);
            linearLayoutRoomAction.setVisibility(View.GONE);
            linearLayoutSceneAction.setVisibility(View.VISIBLE);
        }
    }

    protected Action getCurrentSelection() {
        Action action = null;
        if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
            Log.d(spinner_receiver_action_room.getSelectedItem().toString());
            Log.d(spinner_receiver_action_receiver.getSelectedItem().toString());
            Log.d(spinner_receiver_action_button.getSelectedItem().toString());

            Room selectedRoom = DatabaseHandler.getRoom(spinner_receiver_action_room.getSelectedItem().toString());
            Receiver selectedReceiver = DatabaseHandler.getReceiverByRoomId(selectedRoom.getId(), spinner_receiver_action_receiver
                    .getSelectedItem().toString());
            Button selectedButton = null;
            for (Button button : selectedReceiver.getButtons()) {
                if (button.getName().equals(spinner_receiver_action_button.getSelectedItem().toString())) {
                    selectedButton = button;
                }
            }

            action = new ReceiverAction(-1, selectedRoom, selectedReceiver, selectedButton);
        } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
            Log.d(spinner_room_action_room.getSelectedItem().toString());
            Log.d(spinner_room_action_button.getSelectedItem().toString());

            Room selectedRoom = DatabaseHandler.getRoom(spinner_room_action_room.getSelectedItem().toString());

            action = new RoomAction(-1, selectedRoom, spinner_room_action_button.getSelectedItem()
                    .toString());
        } else if (Action.ACTION_TYPE_SCENE.equals(currentActionType)) {
            Log.d(spinner_scene_action_scene.getSelectedItem().toString());

            Scene selectedScene = DatabaseHandler.getScene(spinner_scene_action_scene.getSelectedItem().toString());

            action = new SceneAction(-1, selectedScene);
        }

        return action;
    }

    private boolean checkValidity() {
        if (currentActionType == null) {
            return false;
        }
        if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
            if (spinner_receiver_action_room.getSelectedItem() == null
                    || spinner_receiver_action_receiver.getSelectedItem() == null
                    || spinner_receiver_action_button.getSelectedItem() == null) {
                return false;
            }
        } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
            if (spinner_room_action_room.getSelectedItem() == null
                    || spinner_room_action_button.getSelectedItem() == null) {
                return false;
            }
        } else if (Action.ACTION_TYPE_SCENE.equals(currentActionType)) {
            if (spinner_scene_action_scene.getSelectedItem() == null) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    protected abstract void addCurrentSelection();

    protected abstract void sendDataChangedBroadcast(Context context);

    @Override
    public void onResume() {
        super.onResume();
        setPositiveButtonVisibility(checkValidity());
    }

    private void setPositiveButtonVisibility(boolean visibility) {
        if (dialog != null) {
            if (visibility) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(defaultTextColor);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
            } else {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
            }
        }
    }
}

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashSet;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.action.ReceiverAction;
import eu.power_switch.action.RoomAction;
import eu.power_switch.action.SceneAction;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.log.Log;

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

    private LinearLayout linearLayoutReceiver;
    private LinearLayout linearLayoutRoom;
    private LinearLayout linearLayoutButton;
    private LinearLayout linearLayoutScene;
    private Spinner spinner_apartment;
    private Spinner spinner_room;
    private Spinner spinner_receiver;
    private Spinner spinner_button;
    private Spinner spinner_scene;
    private ArrayList<String> buttonNames;
    private ArrayAdapter<String> receiverSpinnerArrayAdapter;
    private ArrayAdapter<String> buttonSpinnerArrayAdapter;
    private ArrayList<String> receiverNames;
    private ArrayList<String> roomNames;
    private ArrayList<String> sceneNames;
    private ArrayList<String> apartmentNames;
    private Apartment currentApartment;
    private ArrayAdapter<String> roomSpinnerArrayAdapter;
    private ArrayAdapter<String> sceneSpinnerArrayAdapter;

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

                updateLists();
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

        try {
            ArrayList<Apartment> availableApartments = (ArrayList<Apartment>) DatabaseHandler.getAllApartments();
            apartmentNames = new ArrayList<>();
            for (Apartment apartment : availableApartments) {
                apartmentNames.add(apartment.getName());
            }

            ArrayList<Room> availableRooms = (ArrayList<Room>) DatabaseHandler.getAllRooms();
            roomNames = new ArrayList<>();
            for (Room room : availableRooms) {
                roomNames.add(room.getName());
            }

            ArrayList<Receiver> availableReceivers = (ArrayList<Receiver>) DatabaseHandler.getAllReceivers();
            receiverNames = new ArrayList<>();
            for (Receiver receiver : availableReceivers) {
                receiverNames.add(receiver.getName());
            }

            buttonNames = new ArrayList<>();

            ArrayList<Scene> availableScenes = (ArrayList<Scene>) DatabaseHandler.getAllScenes();
            sceneNames = new ArrayList<>();
            for (Scene scene : availableScenes) {
                sceneNames.add(scene.getName());
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }

        spinner_apartment = (Spinner) rootView.findViewById(R.id.spinner_apartment);
        ArrayAdapter<String> apartmentSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, apartmentNames);
        apartmentSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_apartment.setAdapter(apartmentSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateLists();
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        };
        spinner_apartment.setOnTouchListener(spinnerInteractionListener);
        spinner_apartment.setOnItemSelectedListener(spinnerInteractionListener);

        linearLayoutRoom = (LinearLayout) rootView.findViewById(R.id.linearLayout_room);
        linearLayoutReceiver = (LinearLayout) rootView.findViewById(R.id.linearLayout_receiver);
        linearLayoutButton = (LinearLayout) rootView.findViewById(R.id.linearLayout_button);
        linearLayoutScene = (LinearLayout) rootView.findViewById(R.id.linearLayout_scene);


        spinner_room = (Spinner) rootView.findViewById(R.id.spinner_room);
        roomSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, roomNames);
        roomSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_room.setAdapter(roomSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener2 = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateReceiverList();
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        };
        spinner_room.setOnTouchListener(spinnerInteractionListener2);
        spinner_room.setOnItemSelectedListener(spinnerInteractionListener2);

        spinner_receiver = (Spinner) rootView.findViewById(R.id.spinner_receiver);
        receiverSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, receiverNames);
        receiverSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_receiver.setAdapter(receiverSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener3 = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateButtonList();
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        };
        spinner_receiver.setOnTouchListener(spinnerInteractionListener3);
        spinner_receiver.setOnItemSelectedListener(spinnerInteractionListener3);

        spinner_button = (Spinner) rootView.findViewById(R.id.spinner_button);
        buttonSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, buttonNames);
        buttonSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_button.setAdapter(buttonSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener4 = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        };
        spinner_button.setOnTouchListener(spinnerInteractionListener4);
        spinner_button.setOnItemSelectedListener(spinnerInteractionListener4);

        spinner_scene = (Spinner) rootView.findViewById(R.id.spinner_scene);
        sceneSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sceneNames);
        sceneSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_scene.setAdapter(sceneSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener7 = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        };
        spinner_scene.setOnTouchListener(spinnerInteractionListener7);
        spinner_scene.setOnItemSelectedListener(spinnerInteractionListener7);

        updateActionType(Action.ACTION_TYPE_RECEIVER);
        updateLists();

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

    protected void updateLists() {
        try {
            currentApartment = DatabaseHandler.getApartment(spinner_apartment.getSelectedItem().toString());

            updateRoomList();
            updateScenesList();
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private void updateScenesList() {
        sceneNames.clear();

        for (Scene scene : currentApartment.getScenes()) {
            sceneNames.add(scene.getName());
        }

        spinner_scene.setSelection(0);
        sceneSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private void updateRoomList() {
        roomNames.clear();

        for (Room room : currentApartment.getRooms()) {
            roomNames.add(room.getName());
        }

        spinner_room.setSelection(0);

        roomSpinnerArrayAdapter.notifyDataSetChanged();

        updateReceiverList();
    }

    private void updateReceiverList() {
        receiverNames.clear();

        try {
            Room selectedRoom = getSelectedRoom();
            if (selectedRoom != null) {
                for (Receiver receiver : selectedRoom.getReceivers()) {
                    receiverNames.add(receiver.getName());
                }
                spinner_receiver.setSelection(0);
            }
        } catch (Exception e) {
            Log.e(e);
        }

        updateButtonList();

        receiverSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private void updateButtonList() {
        buttonNames.clear();

        if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
            updateReceiverButtonList();
        } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
            updateRoomButtonsList();
        }

        buttonSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private void updateReceiverButtonList() {
        try {
            Room selectedRoom = getSelectedRoom();
            Receiver selectedReceiver = selectedRoom.getReceiver(spinner_receiver.getSelectedItem()
                    .toString());

            if (selectedReceiver != null) {
                for (Button button : selectedReceiver.getButtons()) {
                    buttonNames.add(button.getName());
                }
            }

            spinner_button.setSelection(0);
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private void updateRoomButtonsList() {
        try {
            Room selectedRoom = getSelectedRoom();

            HashSet<String> uniqueButtonNames = new HashSet<>();
            for (Receiver receiver : selectedRoom.getReceivers()) {
                for (Button button : receiver.getButtons()) {
                    uniqueButtonNames.add(button.getName());
                }
            }
            buttonNames.addAll(uniqueButtonNames);

            spinner_button.setSelection(0);
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private Room getSelectedRoom() {
        return currentApartment.getRoom(spinner_room.getSelectedItem().toString());
    }

    private void updateActionType(String timerActionType) {
        currentActionType = timerActionType;

        if (Action.ACTION_TYPE_RECEIVER.equals(timerActionType)) {
            radioButtonReceiverAction.setChecked(true);
            radioButtonRoomAction.setChecked(false);
            radioButtonSceneAction.setChecked(false);

            linearLayoutRoom.setVisibility(View.VISIBLE);
            linearLayoutReceiver.setVisibility(View.VISIBLE);
            linearLayoutButton.setVisibility(View.VISIBLE);
            linearLayoutScene.setVisibility(View.GONE);
        } else if (Action.ACTION_TYPE_ROOM.equals(timerActionType)) {
            radioButtonReceiverAction.setChecked(false);
            radioButtonRoomAction.setChecked(true);
            radioButtonSceneAction.setChecked(false);

            linearLayoutRoom.setVisibility(View.VISIBLE);
            linearLayoutReceiver.setVisibility(View.GONE);
            linearLayoutButton.setVisibility(View.VISIBLE);
            linearLayoutScene.setVisibility(View.GONE);
        } else if (Action.ACTION_TYPE_SCENE.equals(timerActionType)) {
            radioButtonReceiverAction.setChecked(false);
            radioButtonRoomAction.setChecked(false);
            radioButtonSceneAction.setChecked(true);

            linearLayoutRoom.setVisibility(View.GONE);
            linearLayoutReceiver.setVisibility(View.GONE);
            linearLayoutButton.setVisibility(View.GONE);
            linearLayoutScene.setVisibility(View.VISIBLE);
        }
    }

    protected Action getCurrentSelection() {
        Action action = null;

        try {
            if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
                Log.d(spinner_room.getSelectedItem().toString());
                Log.d(spinner_receiver.getSelectedItem().toString());
                Log.d(spinner_button.getSelectedItem().toString());

                Apartment selectedApartment = currentApartment;
                Room selectedRoom = getSelectedRoom();
                Receiver selectedReceiver = selectedRoom.getReceiver(spinner_receiver.getSelectedItem()
                        .toString());
                Button selectedButton = null;
                for (Button button : selectedReceiver.getButtons()) {
                    if (button.getName().equals(spinner_button.getSelectedItem().toString())) {
                        selectedButton = button;
                    }
                }

                action = new ReceiverAction(-1, currentApartment.getName(), selectedRoom, selectedReceiver, selectedButton);
            } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
                Log.d(spinner_room.getSelectedItem().toString());
                Log.d(spinner_button.getSelectedItem().toString());

                Room selectedRoom = getSelectedRoom();

                action = new RoomAction(-1, currentApartment.getName(), selectedRoom, spinner_button.getSelectedItem()
                        .toString());
            } else if (Action.ACTION_TYPE_SCENE.equals(currentActionType)) {
                Log.d(spinner_scene.getSelectedItem().toString());

                Scene selectedScene = DatabaseHandler.getScene(spinner_scene.getSelectedItem().toString());

                action = new SceneAction(-1, currentApartment.getName(), selectedScene);
            }

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }

        return action;
    }

    private boolean checkValidity() {
        if (currentActionType == null) {
            return false;
        }

        if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
            if (spinner_room.getSelectedItem() == null
                    || spinner_receiver.getSelectedItem() == null
                    || spinner_button.getSelectedItem() == null) {
                return false;
            }
        } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
            if (spinner_room.getSelectedItem() == null
                    || spinner_button.getSelectedItem() == null) {
                return false;
            }
        } else if (Action.ACTION_TYPE_SCENE.equals(currentActionType)) {
            if (spinner_scene.getSelectedItem() == null) {
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

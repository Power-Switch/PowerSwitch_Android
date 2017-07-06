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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NoSuchElementException;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.ReceiverAction;
import eu.power_switch.action.RoomAction;
import eu.power_switch.action.SceneAction;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.eventbus.EventBusSupportDialogFragment;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.action.Action;
import eu.power_switch.shared.event.ActionAddedEvent;
import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

/**
 * Dialog to select an action configuration
 * <p/>
 * Created by Markus on 28.09.2015.
 */
public class AddActionDialog extends EventBusSupportDialogFragment {

    private static final Comparator<String> compareToIgnoreCase = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            return lhs.compareToIgnoreCase(rhs);
        }
    };

    @BindView(R.id.radioButton_receiver_action)
    RadioButton radioButtonReceiverAction;
    @BindView(R.id.radioButton_room_action)
    RadioButton radioButtonRoomAction;
    @BindView(R.id.radioButton_scene_action)
    RadioButton radioButtonSceneAction;

    @BindView(R.id.linearLayout_receiver)
    LinearLayout linearLayoutReceiver;
    @BindView(R.id.linearLayout_room)
    LinearLayout linearLayoutRoom;
    @BindView(R.id.linearLayout_button)
    LinearLayout linearLayoutButton;
    @BindView(R.id.linearLayout_scene)
    LinearLayout linearLayoutScene;

    @BindView(R.id.spinner_apartment)
    Spinner spinner_apartment;
    @BindView(R.id.spinner_room)
    Spinner spinner_room;
    @BindView(R.id.spinner_receiver)
    Spinner spinner_receiver;
    @BindView(R.id.spinner_button)
    Spinner spinner_button;
    @BindView(R.id.spinner_scene)
    Spinner spinner_scene;

    @BindView(R.id.progressApartment)
    ProgressBar progressApartment;
    @BindView(R.id.progressRoom)
    ProgressBar progressRoom;
    @BindView(R.id.progressReceiver)
    ProgressBar progressReceiver;
    @BindView(R.id.progressButton)
    ProgressBar progressButton;
    @BindView(R.id.progressScene)
    ProgressBar progressScene;

    @Getter
    @Setter
    private int requestCode;

    private Dialog dialog;

    private int defaultTextColor;
    private String currentActionType = Action.ACTION_TYPE_RECEIVER;

    private Apartment currentApartment;

    private ArrayList<String> apartmentNames = new ArrayList<>();
    private ArrayList<String> roomNames      = new ArrayList<>();
    private ArrayList<String> receiverNames  = new ArrayList<>();
    private ArrayList<String> buttonNames    = new ArrayList<>();
    private ArrayList<String> sceneNames     = new ArrayList<>();

    private ArrayAdapter<String> receiverSpinnerArrayAdapter;
    private ArrayAdapter<String> buttonSpinnerArrayAdapter;
    private ArrayAdapter<String> roomSpinnerArrayAdapter;
    private ArrayAdapter<String> sceneSpinnerArrayAdapter;
    private ArrayAdapter<String> apartmentSpinnerArrayAdapter;

    public static AddActionDialog newInstance(@NonNull Fragment targetFragment, int requestCode) {
        AddActionDialog addActionDialog = new AddActionDialog();
        addActionDialog.setTargetFragment(targetFragment, 0);
        addActionDialog.setRequestCode(requestCode);
        return addActionDialog;
    }

    public static AddActionDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(targetFragment, -1);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            }
        };

        // Action Type Selection
        radioButtonReceiverAction.setOnClickListener(onClickListener);
        radioButtonRoomAction.setOnClickListener(onClickListener);
        radioButtonSceneAction.setOnClickListener(onClickListener);

        apartmentSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, apartmentNames);
        apartmentSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_apartment.setAdapter(apartmentSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateLists();
            }
        };
        spinner_apartment.setOnTouchListener(spinnerInteractionListener);
        spinner_apartment.setOnItemSelectedListener(spinnerInteractionListener);

        roomSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, roomNames);
        roomSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_room.setAdapter(roomSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener2 = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateReceiverList();
            }
        };
        spinner_room.setOnTouchListener(spinnerInteractionListener2);
        spinner_room.setOnItemSelectedListener(spinnerInteractionListener2);

        receiverSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, receiverNames);
        receiverSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_receiver.setAdapter(receiverSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener3 = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateButtonList();
            }
        };
        spinner_receiver.setOnTouchListener(spinnerInteractionListener3);
        spinner_receiver.setOnItemSelectedListener(spinnerInteractionListener3);

        buttonSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, buttonNames);
        buttonSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_button.setAdapter(buttonSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener4 = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updatePositiveButton();
            }
        };
        spinner_button.setOnTouchListener(spinnerInteractionListener4);
        spinner_button.setOnItemSelectedListener(spinnerInteractionListener4);

        sceneSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sceneNames);
        sceneSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_scene.setAdapter(sceneSpinnerArrayAdapter);
        SpinnerInteractionListener spinnerInteractionListener7 = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updatePositiveButton();
            }
        };
        spinner_scene.setOnTouchListener(spinnerInteractionListener7);
        spinner_scene.setOnItemSelectedListener(spinnerInteractionListener7);

        updateActionType(Action.ACTION_TYPE_RECEIVER);
        updateApartmentList();

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
        dialog.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        defaultTextColor = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                .getTextColors()
                .getDefaultColor();

        return dialog;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_add_action;
    }

    protected void updateLists() {
        try {
            setPositiveButtonVisibility(false);

            currentApartment = getSelectedApartment();

            if (Action.ACTION_TYPE_SCENE.equals(currentActionType)) {
                updateSceneList();
            } else {
                updateRoomList();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void updateApartmentList() {
        setPositiveButtonVisibility(false);

        progressApartment.setVisibility(View.VISIBLE);
        spinner_apartment.setVisibility(View.GONE);

        progressRoom.setVisibility(View.VISIBLE);
        spinner_room.setVisibility(View.GONE);

        progressReceiver.setVisibility(View.VISIBLE);
        spinner_receiver.setVisibility(View.GONE);

        progressButton.setVisibility(View.VISIBLE);
        spinner_button.setVisibility(View.GONE);

        progressScene.setVisibility(View.VISIBLE);
        spinner_scene.setVisibility(View.GONE);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                apartmentNames.clear();

                try {
                    ArrayList<Apartment> availableApartments = (ArrayList<Apartment>) DatabaseHandler.getAllApartments();
                    for (Apartment apartment : availableApartments) {
                        apartmentNames.add(apartment.getName());
                    }

                } catch (Exception e) {
                }

                Collections.sort(apartmentNames, compareToIgnoreCase);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressApartment.setVisibility(View.GONE);
                spinner_apartment.setVisibility(View.VISIBLE);

                spinner_apartment.setSelection(0);
                apartmentSpinnerArrayAdapter.notifyDataSetChanged();

                currentApartment = getSelectedApartment();
                updateRoomList();
                updateSceneList();
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void updateSceneList() {
        setPositiveButtonVisibility(false);

        progressScene.setVisibility(View.VISIBLE);
        spinner_scene.setVisibility(View.GONE);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    sceneNames.clear();

                    for (Scene scene : currentApartment.getScenes()) {
                        sceneNames.add(scene.getName());
                    }

                } catch (Exception e) {
                }

                Collections.sort(sceneNames, compareToIgnoreCase);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressScene.setVisibility(View.GONE);
                spinner_scene.setVisibility(View.VISIBLE);

                spinner_scene.setSelection(0);
                sceneSpinnerArrayAdapter.notifyDataSetChanged();

                updatePositiveButton();
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void updateRoomList() {
        setPositiveButtonVisibility(false);

        progressRoom.setVisibility(View.VISIBLE);
        spinner_room.setVisibility(View.GONE);

        progressReceiver.setVisibility(View.VISIBLE);
        spinner_receiver.setVisibility(View.GONE);

        progressButton.setVisibility(View.VISIBLE);
        spinner_button.setVisibility(View.GONE);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    roomNames.clear();

                    for (Room room : currentApartment.getRooms()) {
                        roomNames.add(room.getName());
                    }
                } catch (Exception e) {
                }

                Collections.sort(roomNames, compareToIgnoreCase);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressRoom.setVisibility(View.GONE);
                spinner_room.setVisibility(View.VISIBLE);

                spinner_room.setSelection(0);

                roomSpinnerArrayAdapter.notifyDataSetChanged();

                updateReceiverList();
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void updateReceiverList() {
        setPositiveButtonVisibility(false);

        progressReceiver.setVisibility(View.VISIBLE);
        spinner_receiver.setVisibility(View.GONE);

        progressButton.setVisibility(View.VISIBLE);
        spinner_button.setVisibility(View.GONE);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                receiverNames.clear();

                try {
                    Room selectedRoom = getSelectedRoom();
                    if (selectedRoom != null) {
                        for (Receiver receiver : selectedRoom.getReceivers()) {
                            receiverNames.add(receiver.getName());
                        }
                    }
                } catch (NoSuchElementException e) {

                } catch (Exception e) {
                }

                Collections.sort(receiverNames, compareToIgnoreCase);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressReceiver.setVisibility(View.GONE);
                spinner_receiver.setVisibility(View.VISIBLE);

                spinner_receiver.setSelection(0);
                receiverSpinnerArrayAdapter.notifyDataSetChanged();

                updateButtonList();
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void updateButtonList() {
        setPositiveButtonVisibility(false);

        progressButton.setVisibility(View.VISIBLE);
        spinner_button.setVisibility(View.GONE);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                buttonNames.clear();

                if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
                    updateReceiverButtonList();
                } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
                    updateRoomButtonsList();
                }

                Collections.sort(buttonNames, compareToIgnoreCase);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressButton.setVisibility(View.GONE);
                spinner_button.setVisibility(View.VISIBLE);

                spinner_button.setSelection(0);

                buttonSpinnerArrayAdapter.notifyDataSetChanged();

                updatePositiveButton();
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
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

        } catch (Exception e) {
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
        } catch (Exception e) {
        }
    }

    private Apartment getSelectedApartment() {
        try {
            return DatabaseHandler.getApartment(spinner_apartment.getSelectedItem()
                    .toString());
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    private Room getSelectedRoom() throws Exception {
        return currentApartment.getRoom(spinner_room.getSelectedItem()
                .toString());
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
                Timber.d(spinner_room.getSelectedItem()
                        .toString());
                Timber.d(spinner_receiver.getSelectedItem()
                        .toString());
                Timber.d(spinner_button.getSelectedItem()
                        .toString());

                Apartment selectedApartment = currentApartment;
                Room      selectedRoom      = getSelectedRoom();
                Receiver selectedReceiver = selectedRoom.getReceiver(spinner_receiver.getSelectedItem()
                        .toString());
                Button selectedButton = null;
                for (Button button : selectedReceiver.getButtons()) {
                    if (button.getName()
                            .equals(spinner_button.getSelectedItem()
                                    .toString())) {
                        selectedButton = button;
                    }
                }

                action = new ReceiverAction(-1, currentApartment.getName(), selectedRoom, selectedReceiver, selectedButton);
            } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
                Timber.d(spinner_room.getSelectedItem()
                        .toString());
                Timber.d(spinner_button.getSelectedItem()
                        .toString());

                Room selectedRoom = getSelectedRoom();

                action = new RoomAction(-1,
                        currentApartment.getName(),
                        selectedRoom,
                        spinner_button.getSelectedItem()
                                .toString());
            } else if (Action.ACTION_TYPE_SCENE.equals(currentActionType)) {
                Timber.d(spinner_scene.getSelectedItem()
                        .toString());

                Scene selectedScene = DatabaseHandler.getScene(spinner_scene.getSelectedItem()
                        .toString());

                action = new SceneAction(-1, currentApartment.getName(), selectedScene);
            }

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(rootView, e);
        }

        return action;
    }

    private boolean checkValidity() {
        if (currentActionType == null) {
            return false;
        }

        if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
            if (spinner_room.getSelectedItem() == null || spinner_receiver.getSelectedItem() == null || spinner_button.getSelectedItem() == null) {
                return false;
            }
        } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
            if (spinner_room.getSelectedItem() == null || spinner_button.getSelectedItem() == null) {
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

    /**
     * Notifies listeners of the action to be added
     */
    protected void addCurrentSelection() {
        EventBus.getDefault()
                .post(new ActionAddedEvent(getRequestCode(), getCurrentSelection()));
    }

    @Deprecated
    protected void sendDataChangedBroadcast(Context context) {
        Timber.w("deprecated method");
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePositiveButton();
    }

    private void setPositiveButtonVisibility(boolean visibility) {
        if (dialog != null) {
            if (visibility) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(defaultTextColor);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setClickable(true);
            } else {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(Color.GRAY);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setClickable(false);
            }
        }
    }

    private void updatePositiveButton() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return checkValidity();
            }

            @Override
            protected void onPostExecute(Boolean bool) {
                setPositiveButtonVisibility(bool);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}

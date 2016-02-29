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

package eu.power_switch.api.taskerplugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import java.util.ArrayList;
import java.util.HashSet;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.action.ReceiverAction;
import eu.power_switch.action.RoomAction;
import eu.power_switch.action.SceneAction;
import eu.power_switch.api.taskerplugin.bundle.BundleScrubber;
import eu.power_switch.api.taskerplugin.bundle.PluginBundleManager;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.ApiConstants;
import eu.power_switch.shared.log.Log;

/**
 * Tasker Plugin Configuration Activity
 * Created by Markus on 22.02.2016.
 */
public class EditActivity extends AbstractPluginActivity {

    private RadioButton radioButtonReceiverAction;
    private RadioButton radioButtonRoomAction;
    private RadioButton radioButtonSceneAction;

    private ArrayList<String> apartmentNames;
    private ArrayList<String> roomNames;
    private ArrayList<String> receiverNames;
    private ArrayList<String> buttonNamesReceiver;
    private ArrayList<String> buttonNamesAll;
    private ArrayList<String> sceneNames;

    private Spinner spinner_apartment;
    private EditText editText_apartment;

    private LinearLayout linearLayoutRoom;
    private LinearLayout linearLayoutReceiver;
    private LinearLayout linearLayoutButton;
    private LinearLayout linearLayoutScene;

    private Spinner spinner_room;
    private EditText editText_room;

    private Spinner spinner_receiver;
    private EditText editText_receiver;

    private Spinner spinner_button;
    private EditText editText_button;

    private Spinner spinner_scene;
    private EditText editText_scene;

    private ArrayAdapter roomSpinnerArrayAdapter;
    private ArrayAdapter receiverSpinnerArrayAdapter;
    private ArrayAdapter buttonSpinnerArrayAdapter;
    private ArrayAdapter sceneSpinnerArrayAdapter;

    private Apartment currentApartment;
    private String currentActionType = Action.ACTION_TYPE_RECEIVER;

    private android.widget.Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasker_plugin);

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

        // Action Type Selection
        radioButtonReceiverAction = (RadioButton) findViewById(R.id.radioButton_receiver_action);
        radioButtonReceiverAction.setOnClickListener(onClickListener);
        radioButtonRoomAction = (RadioButton) findViewById(R.id.radioButton_room_action);
        radioButtonRoomAction.setOnClickListener(onClickListener);
        radioButtonSceneAction = (RadioButton) findViewById(R.id.radioButton_scene_action);
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

            buttonNamesReceiver = new ArrayList<>();
            buttonNamesAll = new ArrayList<>();
            for (Receiver receiver : availableReceivers) {
                for (Button button : receiver.getButtons()) {
                    if (!buttonNamesAll.contains(button.getName())) {
                        buttonNamesAll.add(button.getName());
                    }
                }
            }

            ArrayList<Scene> availableScenes = (ArrayList<Scene>) DatabaseHandler.getAllScenes();
            sceneNames = new ArrayList<>();
            for (Scene scene : availableScenes) {
                sceneNames.add(scene.getName());
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(this, e);
        }

        ImageButton imageButtonSwitchApartment = (ImageButton) findViewById(R.id.imageButton_switchApartment);
        imageButtonSwitchApartment.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle)
                .sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchApartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (View.VISIBLE == spinner_apartment.getVisibility()) {
                    spinner_apartment.setVisibility(View.GONE);
                    editText_apartment.setVisibility(View.VISIBLE);
                } else {
                    spinner_apartment.setVisibility(View.VISIBLE);
                    editText_apartment.setVisibility(View.GONE);
                }
            }
        });

        spinner_apartment = (Spinner) findViewById(R.id.spinner_apartment);
        ArrayAdapter<String> apartmentSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, apartmentNames);
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

        editText_apartment = (EditText) findViewById(R.id.editText_apartment);

        linearLayoutRoom = (LinearLayout) findViewById(R.id.linearLayout_room);
        linearLayoutReceiver = (LinearLayout) findViewById(R.id.linearLayout_receiver);
        linearLayoutButton = (LinearLayout) findViewById(R.id.linearLayout_button);
        linearLayoutScene = (LinearLayout) findViewById(R.id.linearLayout_scene);

        spinner_room = (Spinner) findViewById(R.id.spinner_room);
        roomSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomNames);
        roomSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_room.setAdapter(roomSpinnerArrayAdapter);
        spinnerInteractionListener = new SpinnerInteractionListener() {
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
        spinner_room.setOnTouchListener(spinnerInteractionListener);
        spinner_room.setOnItemSelectedListener(spinnerInteractionListener);

        spinner_receiver = (Spinner) findViewById(R.id.spinner_receiver);
        receiverSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, receiverNames);
        receiverSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_receiver.setAdapter(receiverSpinnerArrayAdapter);
        spinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateReceiverButtonList();
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        };
        spinner_receiver.setOnTouchListener(spinnerInteractionListener);
        spinner_receiver.setOnItemSelectedListener(spinnerInteractionListener);


        spinner_button = (Spinner) findViewById(R.id.spinner_button);
        buttonSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, buttonNamesReceiver);
        buttonSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_button.setAdapter(buttonSpinnerArrayAdapter);
        spinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        };
        spinner_button.setOnTouchListener(spinnerInteractionListener);
        spinner_button.setOnItemSelectedListener(spinnerInteractionListener);

        updateReceiverButtonList();

        spinner_scene = (Spinner) findViewById(R.id.spinner_scene);
        sceneSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sceneNames);
        sceneSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_scene.setAdapter(sceneSpinnerArrayAdapter);
        spinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                setPositiveButtonVisibility(checkValidity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPositiveButtonVisibility(checkValidity());
            }
        };
        spinner_scene.setOnTouchListener(spinnerInteractionListener);
        spinner_scene.setOnItemSelectedListener(spinnerInteractionListener);


        buttonSave = (android.widget.Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        updateLists();

        BundleScrubber.scrub(getIntent());
        final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(localeBundle);

        if (null == savedInstanceState) {
            if (PluginBundleManager.isBundleValid(localeBundle)) {
                final String message =
                        localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);
                ((EditText) findViewById(android.R.id.text1)).setText(message);
            }
        }

    }

    protected void updateLists() {
        try {
            currentApartment = DatabaseHandler.getApartment(spinner_apartment.getSelectedItem().toString());

            updateRoomList();
            updateScenesList();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(this, e);
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
        updateRoomButtonsList();
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
            Room selectedRoom = getSelectedRoom();
            Receiver selectedReceiver = selectedRoom.getReceiver(
                    spinner_receiver.getSelectedItem().toString());

            if (selectedReceiver != null) {
                for (Button button : selectedReceiver.getButtons()) {
                    buttonNamesReceiver.add(button.getName());
                }
            }

            spinner_button.setSelection(0);
        } catch (Exception e) {
            Log.e(e);
        }

        buttonSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private void updateRoomButtonsList() {
        buttonNamesAll.clear();

        try {
            Room selectedRoom = getSelectedRoom();

            HashSet<String> uniqueButtonNames = new HashSet<>();
            for (Receiver receiver : selectedRoom.getReceivers()) {
                for (Button button : receiver.getButtons()) {
                    uniqueButtonNames.add(button.getName());
                }
            }
            buttonNamesAll.addAll(uniqueButtonNames);

            spinner_button.setSelection(0);
        } catch (Exception e) {
            Log.e(e);
        }

        buttonSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private Room getSelectedRoom() {
        if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
            return currentApartment.getRoom(spinner_room.getSelectedItem().toString());
        } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
            return currentApartment.getRoom(spinner_room.getSelectedItem().toString());
        } else {
            return null;
        }
    }

    private void updateActionType(String timerActionType) {
        currentActionType = timerActionType;
        if (Action.ACTION_TYPE_RECEIVER.equals(timerActionType)) {
            linearLayoutRoom.setVisibility(View.VISIBLE);
            linearLayoutReceiver.setVisibility(View.VISIBLE);
            linearLayoutButton.setVisibility(View.VISIBLE);
            linearLayoutScene.setVisibility(View.GONE);
        } else if (Action.ACTION_TYPE_ROOM.equals(timerActionType)) {
            linearLayoutRoom.setVisibility(View.VISIBLE);
            linearLayoutReceiver.setVisibility(View.GONE);
            linearLayoutButton.setVisibility(View.VISIBLE);
            linearLayoutScene.setVisibility(View.GONE);
        } else if (Action.ACTION_TYPE_SCENE.equals(timerActionType)) {
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

                Room selectedRoom = getSelectedRoom();
                Receiver selectedReceiver =
                        selectedRoom.getReceiver(
                                spinner_receiver.getSelectedItem().toString());
                Button selectedButton = null;
                for (Button button : selectedReceiver.getButtons()) {
                    if (button.getName().equals(spinner_button.getSelectedItem().toString())) {
                        selectedButton = button;
                    }
                }

                action = new ReceiverAction(-1, selectedRoom, selectedReceiver, selectedButton);
            } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
                Log.d(spinner_room.getSelectedItem().toString());
                Log.d(spinner_button.getSelectedItem().toString());

                Room selectedRoom = getSelectedRoom();

                action = new RoomAction(-1, selectedRoom, spinner_button.getSelectedItem()
                        .toString());
            } else if (Action.ACTION_TYPE_SCENE.equals(currentActionType)) {
                Log.d(spinner_scene.getSelectedItem().toString());

                Scene selectedScene = DatabaseHandler.getScene(spinner_scene.getSelectedItem().toString());

                action = new SceneAction(-1, selectedScene);
            }

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(this, e);
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

    private void setPositiveButtonVisibility(boolean isValid) {
        buttonSave.setEnabled(isValid);
    }

    @Override
    public void finish() {
        if (!isCanceled() && checkValidity()) {
            Action action = getCurrentSelection();

            final Intent resultIntent = new Intent();

//          This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note
//          that anything placed in this Bundle must be available to Locale's class loader. So storing
//          String, int, and other standard objects will work just fine. Parcelable objects are not
//          acceptable, unless they also implement Serializable. Serializable objects must be standard
//          Android platform objects (A Serializable class private to this plug-in's APK cannot be
//          stored in the Bundle, as Locale's classloader will not recognize it).
            final Bundle resultBundle = new Bundle();
            if (action instanceof ReceiverAction) {
                resultBundle.putString(ApiConstants.KEY_APARTMENT, currentApartment.getName());
                resultBundle.putString(ApiConstants.KEY_ROOM, ((ReceiverAction) action).getRoom().getName());
                resultBundle.putString(ApiConstants.KEY_RECEIVER, ((ReceiverAction) action).getReceiver().getName());
                resultBundle.putString(ApiConstants.KEY_BUTTON, ((ReceiverAction) action).getButton().getName());
            } else if (action instanceof RoomAction) {
                resultBundle.putString(ApiConstants.KEY_APARTMENT, currentApartment.getName());
                resultBundle.putString(ApiConstants.KEY_ROOM, ((RoomAction) action).getRoom().getName());
                resultBundle.putString(ApiConstants.KEY_BUTTON, ((RoomAction) action).getButtonName());
            } else if (action instanceof SceneAction) {
                resultBundle.putString(ApiConstants.KEY_APARTMENT, currentApartment.getName());
                resultBundle.putString(ApiConstants.KEY_SCENE, ((SceneAction) action).getScene().getName());
            }

            if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this)) {
                TaskerPlugin.Setting.setVariableReplaceKeys(resultBundle,
                        new String[]{
                                ApiConstants.KEY_APARTMENT,
                                ApiConstants.KEY_ROOM,
                                ApiConstants.KEY_RECEIVER,
                                ApiConstants.KEY_BUTTON,
                                ApiConstants.KEY_SCENE});
            }

            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

            // The blurb is concise status text to be displayed in the host's UI.
            final String blurb = currentApartment.getName() + ": " + action.toString();
            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);

            setResult(RESULT_OK, resultIntent);
        }

        super.finish();
    }
}
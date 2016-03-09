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
import android.text.TextUtils;
import android.view.MenuItem;
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

    private ArrayList<String> apartmentNames = new ArrayList<>();
    private ArrayList<String> roomNames = new ArrayList<>();
    private ArrayList<String> receiverNames = new ArrayList<>();
    private ArrayList<String> buttonNames = new ArrayList<>();
    private ArrayList<String> sceneNames = new ArrayList<>();

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

    private boolean useManualApartmentInput = false;
    private boolean useManualRoomInput = false;
    private boolean useManualReceiverInput = false;
    private boolean useManualButtonInput = false;
    private boolean useManualSceneInput = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasker_plugin);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.radioButton_receiver_action) {
                    updateActionType(Action.ACTION_TYPE_RECEIVER);
                } else if (v.getId() == R.id.radioButton_room_action) {
                    updateActionType(Action.ACTION_TYPE_ROOM);
                } else if (v.getId() == R.id.radioButton_scene_action) {
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
            for (Apartment apartment : availableApartments) {
                apartmentNames.add(apartment.getName());
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
                if (useManualApartmentInput) {
                    setApartmentInputType(InputType.LIST);
                } else {
                    setApartmentInputType(InputType.MANUAL);
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

        ImageButton imageButtonSwitchRoom = (ImageButton) findViewById(R.id.imageButton_switchRoom);
        imageButtonSwitchRoom.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle)
                .sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useManualRoomInput) {
                    setRoomInputType(InputType.LIST);
                } else {
                    setRoomInputType(InputType.MANUAL);
                }
            }
        });

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

        editText_room = (EditText) findViewById(R.id.editText_room);

        ImageButton imageButtonSwitchReceiver = (ImageButton) findViewById(R.id.imageButton_switchReceiver);
        imageButtonSwitchReceiver.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle)
                .sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useManualReceiverInput) {
                    setReceiverInputType(InputType.LIST);
                } else {
                    setReceiverInputType(InputType.MANUAL);
                }
            }
        });

        spinner_receiver = (Spinner) findViewById(R.id.spinner_receiver);
        receiverSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, receiverNames);
        receiverSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_receiver.setAdapter(receiverSpinnerArrayAdapter);
        spinnerInteractionListener = new SpinnerInteractionListener() {
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
        spinner_receiver.setOnTouchListener(spinnerInteractionListener);
        spinner_receiver.setOnItemSelectedListener(spinnerInteractionListener);

        editText_receiver = (EditText) findViewById(R.id.editText_receiver);

        ImageButton imageButtonSwitchButton = (ImageButton) findViewById(R.id.imageButton_switchButton);
        imageButtonSwitchButton.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle)
                .sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useManualButtonInput) {
                    setButtonInputType(InputType.LIST);
                } else {
                    setButtonInputType(InputType.MANUAL);
                }
            }
        });

        spinner_button = (Spinner) findViewById(R.id.spinner_button);
        buttonSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, buttonNames);
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

        editText_button = (EditText) findViewById(R.id.editText_button);

        ImageButton imageButtonSwitchScene = (ImageButton) findViewById(R.id.imageButton_switchScene);
        imageButtonSwitchScene.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle)
                .sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useManualSceneInput) {
                    setSceneInputType(InputType.LIST);
                } else {
                    setSceneInputType(InputType.MANUAL);
                }
            }
        });

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

        editText_scene = (EditText) findViewById(R.id.editText_scene);


        updateLists();

        BundleScrubber.scrub(getIntent());
        final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(localeBundle);

        if (null == savedInstanceState && PluginBundleManager.isBundleValid(this, localeBundle)) {
            initData(localeBundle);
        } else {
            updateActionType(Action.ACTION_TYPE_RECEIVER);
        }
    }

    private void initData(Bundle localeBundle) {
        if (localeBundle.getBoolean(ApiConstants.KEY_REPLACE_VARIABLES_APARTMENT)) {
            setApartmentInputType(InputType.MANUAL);
            editText_apartment.setText(localeBundle.getString(ApiConstants.KEY_APARTMENT));
        } else {
            spinner_apartment.setSelection(apartmentNames.indexOf(localeBundle.getString(ApiConstants.KEY_APARTMENT)));
            updateLists();
        }

        if (localeBundle.containsKey(ApiConstants.KEY_ROOM) && localeBundle.containsKey(ApiConstants.KEY_RECEIVER) && localeBundle.containsKey(ApiConstants.KEY_BUTTON)) {
            updateActionType(Action.ACTION_TYPE_RECEIVER);

            if (localeBundle.getBoolean(ApiConstants.KEY_REPLACE_VARIABLES_ROOM)) {
                setRoomInputType(InputType.MANUAL);
                editText_room.setText(localeBundle.getString(ApiConstants.KEY_ROOM));
            } else {
                spinner_room.setSelection(roomNames.indexOf(localeBundle.getString(ApiConstants.KEY_ROOM)));
            }

            updateReceiverList();

            if (localeBundle.getBoolean(ApiConstants.KEY_REPLACE_VARIABLES_RECEIVER)) {
                setReceiverInputType(InputType.MANUAL);
                editText_receiver.setText(localeBundle.getString(ApiConstants.KEY_RECEIVER));
            } else {
                spinner_receiver.setSelection(receiverNames.indexOf(localeBundle.getString(ApiConstants.KEY_RECEIVER)));
            }

            updateButtonList();

            if (localeBundle.getBoolean(ApiConstants.KEY_REPLACE_VARIABLES_BUTTON)) {
                setButtonInputType(InputType.MANUAL);
                editText_button.setText(localeBundle.getString(ApiConstants.KEY_BUTTON));
            } else {
                spinner_button.setSelection(buttonNames.indexOf(localeBundle.getString(ApiConstants.KEY_BUTTON)));
            }
        } else if (localeBundle.containsKey(ApiConstants.KEY_ROOM) && localeBundle.containsKey(ApiConstants.KEY_BUTTON)) {
            updateActionType(Action.ACTION_TYPE_ROOM);

            if (localeBundle.getBoolean(ApiConstants.KEY_REPLACE_VARIABLES_ROOM)) {
                setRoomInputType(InputType.MANUAL);
                editText_room.setText(localeBundle.getString(ApiConstants.KEY_ROOM));
            } else {
                spinner_room.setSelection(roomNames.indexOf(localeBundle.getString(ApiConstants.KEY_ROOM)));
            }

            updateButtonList();

            if (localeBundle.getBoolean(ApiConstants.KEY_REPLACE_VARIABLES_BUTTON)) {
                setButtonInputType(InputType.MANUAL);
                editText_button.setText(localeBundle.getString(ApiConstants.KEY_BUTTON));
            } else {
                spinner_button.setSelection(buttonNames.indexOf(localeBundle.getString(ApiConstants.KEY_BUTTON)));
            }
        } else if (localeBundle.containsKey(ApiConstants.KEY_SCENE)) {
            updateActionType(Action.ACTION_TYPE_SCENE);
            if (localeBundle.getBoolean(ApiConstants.KEY_REPLACE_VARIABLES_SCENE)) {
                setSceneInputType(InputType.MANUAL);
                editText_scene.setText(localeBundle.getString(ApiConstants.KEY_SCENE));
            } else {
                spinner_scene.setSelection(sceneNames.indexOf(localeBundle.getString(ApiConstants.KEY_SCENE)));
            }
        }

    }

    private void setApartmentInputType(InputType inputType) {
        useManualApartmentInput = InputType.MANUAL.equals(inputType);

        if (useManualApartmentInput) {
            spinner_apartment.setVisibility(View.GONE);
            editText_apartment.setVisibility(View.VISIBLE);
        } else {
            spinner_apartment.setVisibility(View.VISIBLE);
            editText_apartment.setVisibility(View.GONE);
        }
    }

    private void setRoomInputType(InputType inputType) {
        useManualRoomInput = InputType.MANUAL.equals(inputType);

        if (useManualRoomInput) {
            spinner_room.setVisibility(View.GONE);
            editText_room.setVisibility(View.VISIBLE);
        } else {
            spinner_room.setVisibility(View.VISIBLE);
            editText_room.setVisibility(View.GONE);
        }
    }

    private void setReceiverInputType(InputType inputType) {
        useManualReceiverInput = InputType.MANUAL.equals(inputType);

        if (useManualReceiverInput) {
            spinner_receiver.setVisibility(View.GONE);
            editText_receiver.setVisibility(View.VISIBLE);
        } else {
            spinner_receiver.setVisibility(View.VISIBLE);
            editText_receiver.setVisibility(View.GONE);
        }
    }

    private void setButtonInputType(InputType inputType) {
        useManualButtonInput = InputType.MANUAL.equals(inputType);

        if (useManualButtonInput) {
            spinner_button.setVisibility(View.GONE);
            editText_button.setVisibility(View.VISIBLE);
        } else {
            spinner_button.setVisibility(View.VISIBLE);
            editText_button.setVisibility(View.GONE);
        }
    }

    private void setSceneInputType(InputType inputType) {
        useManualSceneInput = InputType.MANUAL.equals(inputType);

        if (useManualSceneInput) {
            spinner_scene.setVisibility(View.GONE);
            editText_scene.setVisibility(View.VISIBLE);
        } else {
            spinner_scene.setVisibility(View.VISIBLE);
            editText_scene.setVisibility(View.GONE);
        }
    }

    protected void updateLists() {
        try {
            currentApartment = getSelectedApartment();

            updateRoomList();
            updateSceneList();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(this, e);
        }
    }

    private void updateSceneList() {
        sceneNames.clear();

        try {
            for (Scene scene : currentApartment.getScenes()) {
                sceneNames.add(scene.getName());
            }

            spinner_scene.setSelection(0);
        } catch (Exception e) {
            Log.e(e);
        }

        sceneSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private void updateRoomList() {
        roomNames.clear();

        try {
            for (Room room : currentApartment.getRooms()) {
                roomNames.add(room.getName());
            }

            spinner_room.setSelection(0);
        } catch (Exception e) {
            Log.e(e);
        }

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
        receiverSpinnerArrayAdapter.notifyDataSetChanged();

        updateButtonList();
    }

    private void updateButtonList() {
        buttonNames.clear();

        try {
            if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
                updateReceiverButtonList();
            } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
                updateRoomButtonsList();
            }

            spinner_button.setSelection(0);
        } catch (Exception e) {
            Log.e(e);
        }

        buttonSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private void updateReceiverButtonList() throws Exception {
        Room selectedRoom = getSelectedRoom();
        Receiver selectedReceiver = selectedRoom.getReceiver(
                spinner_receiver.getSelectedItem().toString());

        if (selectedReceiver != null) {
            for (Button button : selectedReceiver.getButtons()) {
                buttonNames.add(button.getName());
            }
        }
    }

    private void updateRoomButtonsList() throws Exception {
        Room selectedRoom = getSelectedRoom();

        HashSet<String> uniqueButtonNames = new HashSet<>();
        for (Receiver receiver : selectedRoom.getReceivers()) {
            for (Button button : receiver.getButtons()) {
                uniqueButtonNames.add(button.getName());
            }
        }
        buttonNames.addAll(uniqueButtonNames);
    }

    private Apartment getSelectedApartment() throws Exception {
        try {
            return DatabaseHandler.getApartment(getApartmentName());
        } catch (Exception e) {
            Log.e(e);
        }

        return null;
    }

    private Room getSelectedRoom() throws Exception {
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

    private boolean checkValidity() {
        if (currentActionType == null) {
            return false;
        }

        // check other values based on action type
        if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
            return checkApartmentValidity() && checkRoomValidity() && checkReceiverValidity() && checkButtonValidity();
        } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
            return checkApartmentValidity() && checkRoomValidity() && checkButtonValidity();
        } else if (Action.ACTION_TYPE_SCENE.equals(currentActionType)) {
            return checkApartmentValidity() && checkSceneValidity();
        } else {
            return false;
        }
    }

    private boolean checkApartmentValidity() {
        if (useManualApartmentInput) {
            if (TextUtils.isEmpty(editText_apartment.getText())) {
                return false;
            }
        } else {
            if (spinner_apartment.getSelectedItem() == null) {
                return false;
            }
        }

        return true;
    }

    private boolean checkRoomValidity() {
        if (useManualRoomInput) {
            if (TextUtils.isEmpty(editText_room.getText())) {
                return false;
            }
        } else {
            if (spinner_room.getSelectedItem() == null) {
                return false;
            }
        }

        return true;
    }

    private boolean checkReceiverValidity() {
        if (useManualReceiverInput) {
            if (TextUtils.isEmpty(editText_receiver.getText())) {
                return false;
            }
        } else {
            if (spinner_receiver.getSelectedItem() == null) {
                return false;
            }
        }

        return true;
    }

    private boolean checkButtonValidity() {
        if (useManualButtonInput) {
            if (TextUtils.isEmpty(editText_button.getText())) {
                return false;
            }
        } else {
            if (spinner_button.getSelectedItem() == null) {
                return false;
            }
        }

        return true;
    }

    private boolean checkSceneValidity() {
        if (useManualSceneInput) {
            if (TextUtils.isEmpty(editText_scene.getText())) {
                return false;
            }
        } else {
            if (spinner_scene.getSelectedItem() == null) {
                return false;
            }
        }

        return true;
    }

    private void setPositiveButtonVisibility(boolean isValid) {
        if (getOptionsMenu() != null) {
            MenuItem saveButton = getOptionsMenu().findItem(R.id.twofortyfouram_locale_menu_save);
            saveButton.setEnabled(isValid);
            saveButton.setVisible(isValid);

            onPrepareOptionsMenu(getOptionsMenu());
        }
    }

    private String getApartmentName() {
        if (useManualApartmentInput) {
            return editText_apartment.getText().toString().trim();
        } else {
            return spinner_apartment.getSelectedItem().toString().trim();
        }
    }

    private String getRoomName() {
        if (useManualRoomInput) {
            return editText_room.getText().toString().trim();
        } else {
            return spinner_room.getSelectedItem().toString().trim();
        }
    }

    private String getReceiverName() {
        if (useManualReceiverInput) {
            return editText_receiver.getText().toString().trim();
        } else {
            return spinner_receiver.getSelectedItem().toString().trim();
        }
    }

    private String getButtonName() {
        if (useManualButtonInput) {
            return editText_button.getText().toString().trim();
        } else {
            return spinner_button.getSelectedItem().toString().trim();
        }
    }

    private String getSceneName() {
        if (useManualSceneInput) {
            return editText_scene.getText().toString().trim();
        } else {
            return spinner_scene.getSelectedItem().toString().trim();
        }
    }

    @Override
    public void finish() {
        if (!isCanceled() && checkValidity()) {
            final Intent resultIntent = new Intent();

//          This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note
//          that anything placed in this Bundle must be available to Locale's class loader. So storing
//          String, int, and other standard objects will work just fine. Parcelable objects are not
//          acceptable, unless they also implement Serializable. Serializable objects must be standard
//          Android platform objects (A Serializable class private to this plug-in's APK cannot be
//          stored in the Bundle, as Locale's classloader will not recognize it).
            final Bundle resultBundle = new Bundle();

            // The blurb is concise status text to be displayed in the host's UI.
            String blurb = getApartmentName() + ": ";

            if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
                resultBundle.putBoolean(ApiConstants.KEY_REPLACE_VARIABLES_APARTMENT, useManualApartmentInput);
                resultBundle.putString(ApiConstants.KEY_APARTMENT, getApartmentName());

                resultBundle.putBoolean(ApiConstants.KEY_REPLACE_VARIABLES_ROOM, useManualRoomInput);
                resultBundle.putString(ApiConstants.KEY_ROOM, getRoomName());
                blurb += getRoomName() + ": ";

                resultBundle.putBoolean(ApiConstants.KEY_REPLACE_VARIABLES_RECEIVER, useManualReceiverInput);
                resultBundle.putString(ApiConstants.KEY_RECEIVER, getReceiverName());
                blurb += getReceiverName() + ": ";

                resultBundle.putBoolean(ApiConstants.KEY_REPLACE_VARIABLES_BUTTON, useManualButtonInput);
                resultBundle.putString(ApiConstants.KEY_BUTTON, getButtonName());
                blurb += getButtonName();
            } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
                resultBundle.putBoolean(ApiConstants.KEY_REPLACE_VARIABLES_APARTMENT, useManualApartmentInput);
                resultBundle.putString(ApiConstants.KEY_APARTMENT, getApartmentName());

                resultBundle.putBoolean(ApiConstants.KEY_REPLACE_VARIABLES_ROOM, useManualRoomInput);
                resultBundle.putString(ApiConstants.KEY_ROOM, getRoomName());
                blurb += getRoomName() + ": ";

                resultBundle.putBoolean(ApiConstants.KEY_REPLACE_VARIABLES_BUTTON, useManualButtonInput);
                resultBundle.putString(ApiConstants.KEY_BUTTON, getButtonName());
                blurb += getButtonName();
            } else if (Action.ACTION_TYPE_SCENE.equals(currentActionType)) {
                resultBundle.putBoolean(ApiConstants.KEY_REPLACE_VARIABLES_APARTMENT, useManualApartmentInput);
                resultBundle.putString(ApiConstants.KEY_APARTMENT, getApartmentName());

                resultBundle.putBoolean(ApiConstants.KEY_REPLACE_VARIABLES_SCENE, useManualSceneInput);
                resultBundle.putString(ApiConstants.KEY_SCENE, getSceneName());
                blurb += getSceneName();
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
            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);

            setResult(RESULT_OK, resultIntent);
        }

        super.finish();
    }

    private enum InputType {
        LIST,
        MANUAL
    }
}
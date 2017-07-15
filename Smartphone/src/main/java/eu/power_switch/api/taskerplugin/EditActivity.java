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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.api.taskerplugin.bundle.BundleScrubber;
import eu.power_switch.api.taskerplugin.bundle.PluginBundleManager;
import eu.power_switch.api.taskerplugin.gui.AbstractPluginActivity;
import eu.power_switch.api.taskerplugin.gui.SelectVariableDialog;
import eu.power_switch.api.taskerplugin.tasker_api.TaskerPlugin;
import eu.power_switch.event.VariableSelectedEvent;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.persistence.PersistanceHandler;
import eu.power_switch.shared.constants.ApiConstants;
import timber.log.Timber;

/**
 * Tasker Plugin Configuration Activity
 * <p/>
 * This class <b>MAY NOT</b> be refactored to another package or classname!
 * If the classpath of this class changes Tasker will not identify this as the same Plugin as before
 * and therefore tell existing users that the plugin is missing for their (existing) configuration.
 * <p/>
 * Created by Markus on 22.02.2016.
 */
public class EditActivity extends AbstractPluginActivity {

    @Inject
    PersistanceHandler persistanceHandler;

    private static final Comparator<String> compareToIgnoreCase = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            return lhs.compareToIgnoreCase(rhs);
        }
    };
    private RadioButton radioButtonReceiverAction;
    private RadioButton radioButtonRoomAction;
    private RadioButton radioButtonSceneAction;
    private ArrayList<String> apartmentNames = new ArrayList<>();
    private ArrayList<String> roomNames      = new ArrayList<>();
    private ArrayList<String> receiverNames  = new ArrayList<>();
    private ArrayList<String> buttonNames    = new ArrayList<>();
    private ArrayList<String> sceneNames     = new ArrayList<>();
    private Spinner              spinner_apartment;
    private EditText             editText_apartment;
    private LinearLayout         linearLayoutRoom;
    private LinearLayout         linearLayoutReceiver;
    private LinearLayout         linearLayoutButton;
    private LinearLayout         linearLayoutScene;
    private Spinner              spinner_room;
    private EditText             editText_room;
    private Spinner              spinner_receiver;
    private EditText             editText_receiver;
    private Spinner              spinner_button;
    private EditText             editText_button;
    private Spinner              spinner_scene;
    private EditText             editText_scene;
    private ArrayAdapter<String> roomSpinnerArrayAdapter;
    private ArrayAdapter<String> receiverSpinnerArrayAdapter;
    private ArrayAdapter<String> buttonSpinnerArrayAdapter;
    private ArrayAdapter<String> sceneSpinnerArrayAdapter;
    private Apartment            currentApartment;
    private       String       currentActionType       = Action.ACTION_TYPE_RECEIVER;
    private       boolean      useManualApartmentInput = false;
    private       boolean      useManualRoomInput      = false;
    private       boolean      useManualReceiverInput  = false;
    private       boolean      useManualButtonInput    = false;
    private       boolean      useManualSceneInput     = false;
    private final TextWatcher  editTextTextWatcher     = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            setPositiveButtonVisibility(checkValidity());
        }
    };
    private       List<String> relevantVariables       = new ArrayList<>();
    private ImageButton imageButtonApartmentVariablePicker;
    private ImageButton imageButtonReceiverVariablePicker;
    private ImageButton imageButtonRoomVariablePicker;
    private ImageButton imageButtonButtonVariablePicker;
    private ImageButton imageButtonSceneVariablePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasker_plugin);

        View.OnClickListener actionTypeOnClickListener = new View.OnClickListener() {
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
        radioButtonReceiverAction = findViewById(R.id.radioButton_receiver_action);
        radioButtonReceiverAction.setOnClickListener(actionTypeOnClickListener);
        radioButtonRoomAction = findViewById(R.id.radioButton_room_action);
        radioButtonRoomAction.setOnClickListener(actionTypeOnClickListener);
        radioButtonSceneAction = findViewById(R.id.radioButton_scene_action);
        radioButtonSceneAction.setOnClickListener(actionTypeOnClickListener);

        try {
            ArrayList<Apartment> availableApartments = (ArrayList<Apartment>) persistanceHandler.getAllApartments();
            for (Apartment apartment : availableApartments) {
                apartmentNames.add(apartment.getName());
            }
            Collections.sort(apartmentNames, compareToIgnoreCase);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(this, e);
        }

        imageButtonApartmentVariablePicker = findViewById(R.id.imageButton_apartmentVariablePicker);
        imageButtonApartmentVariablePicker.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_tag_more).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonApartmentVariablePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectVariableDialog selectVariableDialog = SelectVariableDialog.newInstance(relevantVariables,
                        VariableSelectedEvent.Field.Apartment);
                selectVariableDialog.show(getFragmentManager(), null);
            }
        });

        ImageButton imageButtonSwitchApartment = findViewById(R.id.imageButton_switchApartment);
        imageButtonSwitchApartment.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchApartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useManualApartmentInput) {
                    setApartmentInputType(InputType.LIST);

                } else {
                    setApartmentInputType(InputType.MANUAL);
                    imageButtonApartmentVariablePicker.setVisibility(View.VISIBLE);
                }

                setPositiveButtonVisibility(checkValidity());
            }
        });

        spinner_apartment = findViewById(R.id.spinner_apartment);
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

        editText_apartment = findViewById(R.id.editText_apartment);
        editText_apartment.addTextChangedListener(editTextTextWatcher);

        linearLayoutRoom = findViewById(R.id.linearLayout_room);
        linearLayoutReceiver = findViewById(R.id.linearLayout_receiver);
        linearLayoutButton = findViewById(R.id.linearLayout_button);
        linearLayoutScene = findViewById(R.id.linearLayout_scene);

        imageButtonRoomVariablePicker = findViewById(R.id.imageButton_roomVariablePicker);
        imageButtonRoomVariablePicker.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_tag_more).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonRoomVariablePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectVariableDialog selectVariableDialog = SelectVariableDialog.newInstance(relevantVariables, VariableSelectedEvent.Field.Room);
                selectVariableDialog.show(getFragmentManager(), null);
            }
        });

        ImageButton imageButtonSwitchRoom = findViewById(R.id.imageButton_switchRoom);
        imageButtonSwitchRoom.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useManualRoomInput) {
                    setRoomInputType(InputType.LIST);
                    imageButtonRoomVariablePicker.setVisibility(View.GONE);
                } else {
                    setRoomInputType(InputType.MANUAL);
                    imageButtonRoomVariablePicker.setVisibility(View.VISIBLE);
                }

                setPositiveButtonVisibility(checkValidity());
            }
        });

        spinner_room = findViewById(R.id.spinner_room);
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

        editText_room = findViewById(R.id.editText_room);
        editText_room.addTextChangedListener(editTextTextWatcher);

        imageButtonReceiverVariablePicker = findViewById(R.id.imageButton_receiverVariablePicker);
        imageButtonReceiverVariablePicker.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_tag_more).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonReceiverVariablePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectVariableDialog selectVariableDialog = SelectVariableDialog.newInstance(relevantVariables, VariableSelectedEvent.Field.Receiver);
                selectVariableDialog.show(getFragmentManager(), null);
            }
        });

        ImageButton imageButtonSwitchReceiver = findViewById(R.id.imageButton_switchReceiver);
        imageButtonSwitchReceiver.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useManualReceiverInput) {
                    setReceiverInputType(InputType.LIST);
                    imageButtonReceiverVariablePicker.setVisibility(View.GONE);
                } else {
                    setReceiverInputType(InputType.MANUAL);
                    imageButtonReceiverVariablePicker.setVisibility(View.VISIBLE);
                }

                setPositiveButtonVisibility(checkValidity());
            }
        });

        spinner_receiver = findViewById(R.id.spinner_receiver);
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

        editText_receiver = findViewById(R.id.editText_receiver);
        editText_receiver.addTextChangedListener(editTextTextWatcher);

        imageButtonButtonVariablePicker = findViewById(R.id.imageButton_buttonVariablePicker);
        imageButtonButtonVariablePicker.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_tag_more).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonButtonVariablePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectVariableDialog selectVariableDialog = SelectVariableDialog.newInstance(relevantVariables, VariableSelectedEvent.Field.Button);
                selectVariableDialog.show(getFragmentManager(), null);
            }
        });

        ImageButton imageButtonSwitchButton = findViewById(R.id.imageButton_switchButton);
        imageButtonSwitchButton.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useManualButtonInput) {
                    setButtonInputType(InputType.LIST);
                    imageButtonButtonVariablePicker.setVisibility(View.GONE);
                } else {
                    setButtonInputType(InputType.MANUAL);
                    imageButtonButtonVariablePicker.setVisibility(View.VISIBLE);
                }

                setPositiveButtonVisibility(checkValidity());
            }
        });

        spinner_button = findViewById(R.id.spinner_button);
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

        editText_button = findViewById(R.id.editText_button);
        editText_button.addTextChangedListener(editTextTextWatcher);

        imageButtonSceneVariablePicker = findViewById(R.id.imageButton_sceneVariablePicker);
        imageButtonSceneVariablePicker.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_tag_more).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSceneVariablePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectVariableDialog selectVariableDialog = SelectVariableDialog.newInstance(relevantVariables, VariableSelectedEvent.Field.Scene);
                selectVariableDialog.show(getFragmentManager(), null);
            }
        });

        ImageButton imageButtonSwitchScene = findViewById(R.id.imageButton_switchScene);
        imageButtonSwitchScene.setImageDrawable(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_shuffle).sizeDp(24)
                .color(ContextCompat.getColor(this, android.R.color.white)));
        imageButtonSwitchScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useManualSceneInput) {
                    setSceneInputType(InputType.LIST);
                    imageButtonSceneVariablePicker.setVisibility(View.GONE);
                } else {
                    setSceneInputType(InputType.MANUAL);
                    imageButtonSceneVariablePicker.setVisibility(View.VISIBLE);
                }

                setPositiveButtonVisibility(checkValidity());
            }
        });

        spinner_scene = findViewById(R.id.spinner_scene);
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

        editText_scene = findViewById(R.id.editText_scene);
        editText_scene.addTextChangedListener(editTextTextWatcher);


        updateLists();

        BundleScrubber.scrub(getIntent());
        final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(localeBundle);

        if (TaskerPlugin.hostSupportsRelevantVariables(getIntent().getExtras())) {
            relevantVariables.clear();
            relevantVariables.addAll(Arrays.asList(TaskerPlugin.getRelevantVariableList(getIntent().getExtras())));
        }

        if (null == savedInstanceState && PluginBundleManager.isBundleValid(this, localeBundle)) {
            initData(localeBundle);
        } else {
            updateActionType(Action.ACTION_TYPE_RECEIVER);
        }

        setPositiveButtonVisibility(checkValidity());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onVariableSelected(VariableSelectedEvent variableSelectedEvent) {
        VariableSelectedEvent.Field field            = variableSelectedEvent.getField();
        String                      selectedVariable = variableSelectedEvent.getVariable();

        switch (field) {
            case Apartment:
                editText_apartment.setText(selectedVariable);
                break;
            case Room:
                editText_room.setText(selectedVariable);
                break;
            case Receiver:
                editText_receiver.setText(selectedVariable);
                break;
            case Button:
                editText_button.setText(selectedVariable);
                break;
            case Scene:
                editText_scene.setText(selectedVariable);
                break;
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

        if (localeBundle.containsKey(ApiConstants.KEY_ROOM) && localeBundle.containsKey(ApiConstants.KEY_RECEIVER) && localeBundle.containsKey(
                ApiConstants.KEY_BUTTON)) {
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
            imageButtonApartmentVariablePicker.setVisibility(View.VISIBLE);
        } else {
            spinner_apartment.setVisibility(View.VISIBLE);
            editText_apartment.setVisibility(View.GONE);
            imageButtonApartmentVariablePicker.setVisibility(View.GONE);
        }
    }

    private void setRoomInputType(InputType inputType) {
        useManualRoomInput = InputType.MANUAL.equals(inputType);

        if (useManualRoomInput) {
            spinner_room.setVisibility(View.GONE);
            editText_room.setVisibility(View.VISIBLE);
            imageButtonRoomVariablePicker.setVisibility(View.VISIBLE);
        } else {
            spinner_room.setVisibility(View.VISIBLE);
            editText_room.setVisibility(View.GONE);
            imageButtonRoomVariablePicker.setVisibility(View.GONE);
        }
    }

    private void setReceiverInputType(InputType inputType) {
        useManualReceiverInput = InputType.MANUAL.equals(inputType);

        if (useManualReceiverInput) {
            spinner_receiver.setVisibility(View.GONE);
            editText_receiver.setVisibility(View.VISIBLE);
            imageButtonReceiverVariablePicker.setVisibility(View.VISIBLE);
        } else {
            spinner_receiver.setVisibility(View.VISIBLE);
            editText_receiver.setVisibility(View.GONE);
            imageButtonReceiverVariablePicker.setVisibility(View.GONE);
        }
    }

    private void setButtonInputType(InputType inputType) {
        useManualButtonInput = InputType.MANUAL.equals(inputType);

        if (useManualButtonInput) {
            spinner_button.setVisibility(View.GONE);
            editText_button.setVisibility(View.VISIBLE);
            imageButtonButtonVariablePicker.setVisibility(View.VISIBLE);
        } else {
            spinner_button.setVisibility(View.VISIBLE);
            editText_button.setVisibility(View.GONE);
            imageButtonButtonVariablePicker.setVisibility(View.GONE);
        }
    }

    private void setSceneInputType(InputType inputType) {
        useManualSceneInput = InputType.MANUAL.equals(inputType);

        if (useManualSceneInput) {
            spinner_scene.setVisibility(View.GONE);
            editText_scene.setVisibility(View.VISIBLE);
            imageButtonSceneVariablePicker.setVisibility(View.VISIBLE);
        } else {
            spinner_scene.setVisibility(View.VISIBLE);
            editText_scene.setVisibility(View.GONE);
            imageButtonSceneVariablePicker.setVisibility(View.GONE);
        }
    }

    protected void updateLists() {
        try {
            currentApartment = getSelectedApartment();

            updateRoomList();
            updateSceneList();
        } catch (Exception e) {
            Timber.e(e);
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
            Timber.e(e);
        }

        Collections.sort(sceneNames, compareToIgnoreCase);
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
            Timber.e(e);
        }

        Collections.sort(roomNames, compareToIgnoreCase);
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
            Timber.e(e);
        }

        Collections.sort(receiverNames, compareToIgnoreCase);
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
            Timber.e(e);
        }

        Collections.sort(buttonNames, compareToIgnoreCase);

        buttonSpinnerArrayAdapter.notifyDataSetChanged();
    }

    private void updateReceiverButtonList() throws Exception {
        Room selectedRoom = getSelectedRoom();
        Receiver selectedReceiver = selectedRoom.getReceiver(spinner_receiver.getSelectedItem()
                .toString());

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
        return persistanceHandler.getApartment(getApartmentName());
    }

    private Room getSelectedRoom() throws Exception {
        if (Action.ACTION_TYPE_RECEIVER.equals(currentActionType)) {
            return currentApartment.getRoom(spinner_room.getSelectedItem()
                    .toString());
        } else if (Action.ACTION_TYPE_ROOM.equals(currentActionType)) {
            return currentApartment.getRoom(spinner_room.getSelectedItem()
                    .toString());
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
        try {
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
        } catch (Exception e) {
            Timber.e(e);
            return false;
        }
    }

    private boolean checkApartmentValidity() {
        return !TextUtils.isEmpty(getApartmentName());
    }

    private boolean checkRoomValidity() {
        return !TextUtils.isEmpty(getRoomName());
    }

    private boolean checkReceiverValidity() {
        return !TextUtils.isEmpty(getReceiverName());
    }

    private boolean checkButtonValidity() {
        return !TextUtils.isEmpty(getButtonName());
    }

    private boolean checkSceneValidity() {
        return !TextUtils.isEmpty(getSceneName());
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
            return editText_apartment.getText()
                    .toString()
                    .trim();
        } else {
            Object selectedItem = spinner_apartment.getSelectedItem();
            if (selectedItem != null) {
                return selectedItem.toString()
                        .trim();
            } else {
                return spinner_apartment.getItemAtPosition(0)
                        .toString()
                        .trim();
            }
        }
    }

    private String getRoomName() {
        if (useManualRoomInput) {
            return editText_room.getText()
                    .toString()
                    .trim();
        } else {
            Object selectedItem = spinner_room.getSelectedItem();
            if (selectedItem != null) {
                return selectedItem.toString()
                        .trim();
            } else {
                return spinner_room.getItemAtPosition(0)
                        .toString()
                        .trim();
            }
        }
    }

    private String getReceiverName() {
        if (useManualReceiverInput) {
            return editText_receiver.getText()
                    .toString()
                    .trim();
        } else {
            Object selectedItem = spinner_receiver.getSelectedItem();
            if (selectedItem != null) {
                return selectedItem.toString()
                        .trim();
            } else {
                return spinner_receiver.getItemAtPosition(0)
                        .toString()
                        .trim();
            }
        }
    }

    private String getButtonName() {
        if (useManualButtonInput) {
            return editText_button.getText()
                    .toString()
                    .trim();
        } else {
            Object selectedItem = spinner_button.getSelectedItem();
            if (selectedItem != null) {
                return selectedItem.toString()
                        .trim();
            } else {
                return spinner_button.getItemAtPosition(0)
                        .toString()
                        .trim();
            }
        }
    }

    private String getSceneName() {
        if (useManualSceneInput) {
            return editText_scene.getText()
                    .toString()
                    .trim();
        } else {
            Object selectedItem = spinner_scene.getSelectedItem();
            if (selectedItem != null) {
                return selectedItem.toString()
                        .trim();
            } else {
                return spinner_scene.getItemAtPosition(0)
                        .toString()
                        .trim();
            }
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

            // The blurb is concise status text to be displayed in the localHost's UI.
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

//            if ( TaskerPlugin.hostSupportsRelevantVariables( getIntent().getExtras() ) )
//                TaskerPlugin.addRelevantVariableList( resultIntent, new String [] {
//                        "%pcolour\nPet Colour\nThe colour of the pet <B>last bought</B>"
//                } );

            if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this)) {
                TaskerPlugin.Setting.setVariableReplaceKeys(resultBundle,
                        new String[]{ApiConstants.KEY_APARTMENT,
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
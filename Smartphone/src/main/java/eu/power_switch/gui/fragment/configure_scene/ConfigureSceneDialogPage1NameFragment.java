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

package eu.power_switch.gui.fragment.configure_scene;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigureSceneDialog;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * "Name" Fragment used in Configure Scene Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureSceneDialogPage1NameFragment extends ConfigurationDialogFragment {

    private View rootView;

    private TextInputLayout floatingName;
    private EditText name;

    private LinearLayout linearLayout_selectableReceivers;

    private ArrayList<CheckBox> receiverCheckboxList = new ArrayList<>();
    private long sceneId;
    private List<Scene> existingScenes;


    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context           any suitable context
     * @param name              Current Name of Scene
     * @param selectedReceivers Currently selected Receivers to include in Scene
     */
    public static void sendNameSceneChangedBroadcast(Context context, String name, ArrayList<Room> selectedReceivers) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_NAME_SCENE_CHANGED);
        intent.putExtra("name", name);
        intent.putExtra("selectedReceivers", selectedReceivers);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_scene_page_1, container, false);

        floatingName = (TextInputLayout) rootView.findViewById(R.id.scene_name_text_input_layout);
        name = (EditText) rootView.findViewById(R.id.editText_scene_name);
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

        try {
            existingScenes = DatabaseHandler.getScenes(SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID));
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }

        linearLayout_selectableReceivers = (LinearLayout) rootView.findViewById(R.id.linearLayout_selectableReceivers);
        addReceiversToLayout();

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureSceneDialog.SCENE_ID_KEY)) {
            sceneId = args.getLong(ConfigureSceneDialog.SCENE_ID_KEY);
            initializeSceneData(sceneId);
        }

        checkValidity();

        return rootView;
    }

    private void addReceiversToLayout() {
        String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(inflaterString);

        try {
            for (Room room : DatabaseHandler.getRooms(SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID))) {
                LinearLayout roomLayout = new LinearLayout(getActivity());
                roomLayout.setOrientation(LinearLayout.VERTICAL);
                roomLayout.setPadding(0, 8, 0, 8);
                linearLayout_selectableReceivers.addView(roomLayout);

                TextView roomName = new TextView(getActivity());
                roomName.setText(room.getName());
                roomName.setTextColor(ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary));
                roomLayout.addView(roomName);

                for (Receiver receiver : room.getReceivers()) {
                    LinearLayout receiverLayout = new LinearLayout(getActivity());
                    receiverLayout.setOrientation(LinearLayout.HORIZONTAL);
                    roomLayout.addView(receiverLayout);

                    final CheckBox checkBox = (CheckBox) inflater.inflate(R.layout.simple_checkbox, receiverLayout, false);
                    checkBox.setTag(R.string.room, room);
                    checkBox.setTag(R.string.receiver, receiver);
                    receiverLayout.addView(checkBox);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            checkValidity();
                        }
                    });
                    receiverCheckboxList.add(checkBox);

                    TextView textView_receiverName = new TextView(getActivity());
                    textView_receiverName.setText(receiver.getName());
                    receiverLayout.addView(textView_receiverName);

                    receiverLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkBox.setChecked(!checkBox.isChecked());
                        }
                    });
                }
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    private void initializeSceneData(long sceneId) {
        try {
            Scene scene = DatabaseHandler.getScene(sceneId);

            name.setText(scene.getName());

            ArrayList<Receiver> activeReceivers = new ArrayList<>();
            for (SceneItem sceneItem : scene.getSceneItems()) {
                Receiver receiver = DatabaseHandler.getReceiver(sceneItem.getActiveButton().getReceiverId());
                activeReceivers.add(receiver);
            }

            for (Receiver receiver : activeReceivers) {
                for (CheckBox checkBox : receiverCheckboxList) {
                    Receiver associatedReceiver = (Receiver) checkBox.getTag(R.string.receiver);
                    Room associatedRoom = (Room) checkBox.getTag(R.string.room);
                    if (associatedReceiver.getId().equals(receiver.getId()) && associatedRoom.getId().equals(receiver
                            .getRoomId())) {
                        checkBox.setChecked(true);
                    }
                }
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    private boolean checkValidity() {
        // TODO: Performance Optimierung

        if (!checkNameValidity()) {
            sendNameSceneChangedBroadcast(getActivity(), null, getCheckedReceivers());
            return false;
        }

        if (getCheckedReceivers().isEmpty()) {
            floatingName.setError(getString(R.string.please_select_receivers));
            sendNameSceneChangedBroadcast(getActivity(), getCurrentSceneName(), getCheckedReceivers());
            return false;
        }

        floatingName.setError(null);
        sendNameSceneChangedBroadcast(getActivity(), getCurrentSceneName(), getCheckedReceivers());
        return true;
    }

    private boolean checkNameValidity() {
        if (getCurrentSceneName().length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            return false;
        } else {
            for (Scene scene : existingScenes) {
                if (!scene.getId().equals(sceneId) && scene.getName().equalsIgnoreCase(getCurrentSceneName())) {
                    floatingName.setError(getString(R.string.scene_name_already_exists));
                    return false;
                }
            }
        }

        return true;
    }

    private ArrayList<Room> getCheckedReceivers() {
        ArrayList<Room> checkedReceivers = new ArrayList<>();

        for (CheckBox checkBox : receiverCheckboxList) {
            if (checkBox.isChecked()) {
                Room originalRoom = (Room) checkBox.getTag(R.string.room);
                Room room = null;
                for (Room currentRoom : checkedReceivers) {
                    if (currentRoom.getName().equals(originalRoom.getName())) {
                        room = currentRoom;
                        break;
                    }
                }

                if (room == null) {
                    // copy room
                    room = new Room(originalRoom.getId(), originalRoom.getApartmentId(),
                            originalRoom.getName(), originalRoom.getPositionInApartment(),
                            originalRoom.isCollapsed(), originalRoom.getAssociatedGateways());
                    // add room to list
                    checkedReceivers.add(room);
                }

                // add checked receiver
                room.addReceiver((Receiver) checkBox.getTag(R.string.receiver));
            }
        }

        return checkedReceivers;
    }

    private String getCurrentSceneName() {
        return name.getText().toString().trim();
    }
}

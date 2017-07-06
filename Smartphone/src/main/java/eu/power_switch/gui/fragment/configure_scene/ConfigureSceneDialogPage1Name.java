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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.markusressel.android.library.tutorialtooltip.builder.IndicatorBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.MessageBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.TutorialTooltipBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.TutorialTooltipChainBuilder;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnIndicatorClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnMessageClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipIndicator;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipMessage;
import de.markusressel.android.library.tutorialtooltip.view.TooltipId;
import de.markusressel.android.library.tutorialtooltip.view.TutorialTooltipView;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.SceneConfigurationHolder;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.event.SceneSelectedReceiversChangedEvent;

/**
 * "Name" Fragment used in Configure Scene Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureSceneDialogPage1Name extends ConfigurationDialogPage<SceneConfigurationHolder> {

    @BindView(R.id.scene_name_text_input_layout)
    TextInputLayout floatingName;
    @BindView(R.id.editText_scene_name)
    EditText        name;

    @BindView(R.id.linearLayout_selectableReceivers)
    LinearLayout linearLayout_selectableReceivers;

    private ArrayList<CheckBox> receiverCheckboxList = new ArrayList<>();
    private List<Scene> existingScenes;


    /**
     * Used to notify the setup page that some info has changed
     */
    public void notifySelectedReceiversChanged() {
        EventBus.getDefault()
                .post(new SceneSelectedReceiversChangedEvent());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

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
                getConfiguration().setName(s.toString());

                notifyConfigurationChanged();
                checkValidity();
            }
        });

        try {
            existingScenes = DatabaseHandler.getScenes(SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID));
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }

        addReceiversToLayout();

        initializeSceneData();

        checkValidity();

        createTutorial();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_scene_page_1;
    }

    private void createTutorial() {
        OnMessageClickedListener onClickListener = new OnMessageClickedListener() {
            @Override
            public void onMessageClicked(TooltipId id, TutorialTooltipView tutorialTooltipView, TutorialTooltipMessage tutorialTooltipMessage,
                                         View view) {
                tutorialTooltipView.remove(true);
            }
        };

        OnIndicatorClickedListener onIndicatorClickedListener = new OnIndicatorClickedListener() {
            @Override
            public void onIndicatorClicked(TooltipId tooltipId, TutorialTooltipView tutorialTooltipView,
                                           TutorialTooltipIndicator tutorialTooltipIndicator, View view) {
                tutorialTooltipView.remove(true);
            }
        };

        TutorialTooltipBuilder message1 = new TutorialTooltipBuilder(getActivity()).attachToDialog(getParentConfigurationDialog().getDialog())
                .anchor(name, TutorialTooltipView.Gravity.CENTER)
                .indicator(new IndicatorBuilder().onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_scene_name__text)
                        .gravity(TutorialTooltipView.Gravity.BOTTOM)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_scene_name__id)
                .build();

        TutorialTooltipBuilder message2 = new TutorialTooltipBuilder(getActivity()).attachToDialog(getParentConfigurationDialog().getDialog())
                .anchor(linearLayout_selectableReceivers, TutorialTooltipView.Gravity.CENTER)
                .indicator(new IndicatorBuilder().onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_scene_devices__text)
                        .gravity(TutorialTooltipView.Gravity.BOTTOM)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_scene_devices__id)
                .build();

        new TutorialTooltipChainBuilder().addItem(message1)
                .addItem(message2)
                .execute();
    }

    private void addReceiversToLayout() {
        String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater       = (LayoutInflater) getActivity().getSystemService(inflaterString);

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
                            getConfiguration().setCheckedReceivers(getCheckedReceivers());

                            notifySelectedReceiversChanged();

                            checkValidity();
                            notifyConfigurationChanged();
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

    private void initializeSceneData() {
        Scene scene = getConfiguration().getScene();

        if (scene != null) {
            try {
                name.setText(scene.getName());

                ArrayList<Receiver> activeReceivers = new ArrayList<>();
                for (SceneItem sceneItem : scene.getSceneItems()) {
                    Receiver receiver = DatabaseHandler.getReceiver(sceneItem.getActiveButton()
                            .getReceiverId());
                    activeReceivers.add(receiver);
                }

                for (Receiver receiver : activeReceivers) {
                    for (CheckBox checkBox : receiverCheckboxList) {
                        Receiver associatedReceiver = (Receiver) checkBox.getTag(R.string.receiver);
                        Room     associatedRoom     = (Room) checkBox.getTag(R.string.room);
                        if (associatedReceiver.getId()
                                .equals(receiver.getId()) && associatedRoom.getId()
                                .equals(receiver.getRoomId())) {
                            checkBox.setChecked(true);
                        }
                    }
                }
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    private boolean checkValidity() {
        // TODO: Performance Optimierung
        if (!checkNameValidity()) {
            return false;
        }

        if (getCheckedReceivers().isEmpty()) {
            floatingName.setError(getString(R.string.please_select_receivers));
            return false;
        }

        floatingName.setError(null);
        return true;
    }

    private boolean checkNameValidity() {
        if (getCurrentSceneName().length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            return false;
        } else {
            // TODO: nullpointer on getId()
            for (Scene scene : existingScenes) {
                if (!scene.getId()
                        .equals(getConfiguration().getScene()
                                .getId()) && scene.getName()
                        .equalsIgnoreCase(getCurrentSceneName())) {
                    floatingName.setError(getString(R.string.scene_name_already_exists));
                    return false;
                }
            }
        }

        return true;
    }

    private List<Room> getCheckedReceivers() {
        ArrayList<Room> checkedReceivers = new ArrayList<>();

        for (CheckBox checkBox : receiverCheckboxList) {
            if (checkBox.isChecked()) {
                Room originalRoom = (Room) checkBox.getTag(R.string.room);
                Room room         = null;
                for (Room currentRoom : checkedReceivers) {
                    if (currentRoom.getName()
                            .equals(originalRoom.getName())) {
                        room = currentRoom;
                        break;
                    }
                }

                if (room == null) {
                    // copy room
                    room = new Room(originalRoom.getId(),
                            originalRoom.getApartmentId(),
                            originalRoom.getName(),
                            originalRoom.getPositionInApartment(),
                            originalRoom.isCollapsed(),
                            originalRoom.getAssociatedGateways());
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
        return name.getText()
                .toString()
                .trim();
    }
}

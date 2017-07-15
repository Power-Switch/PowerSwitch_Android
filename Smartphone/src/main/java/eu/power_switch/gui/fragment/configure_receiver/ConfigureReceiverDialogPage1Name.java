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

package eu.power_switch.gui.fragment.configure_receiver;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import eu.power_switch.event.ReceiverParentRoomChangedEvent;
import eu.power_switch.event.RoomAddedEvent;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.dialog.CreateRoomDialog;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.ReceiverConfigurationHolder;
import eu.power_switch.obj.Room;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import timber.log.Timber;

/**
 * "Name" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage1Name extends ConfigurationDialogPage<ReceiverConfigurationHolder> {

    @BindView(R.id.receiver_name_text_input_layout)
    TextInputLayout      floatingName;
    @BindView(R.id.editText_receiver_name)
    EditText             name;
    @BindView(R.id.listView_rooms)
    ListView             roomsListView;
    @BindView(R.id.add_room_fab)
    FloatingActionButton addRoomFAB;

    private ArrayAdapter<String> roomNamesAdapter;
    private ArrayList<String> roomList = new ArrayList<>();

    private String originalName;

    boolean initialized = false;

    /**
     * Used to notify this page that a room has been added to the list
     *
     * @param newRoomName name of added room
     */
    public static void notifyRoomAdded(String newRoomName) {
        EventBus.getDefault()
                .post(new RoomAddedEvent(newRoomName));
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
                getConfiguration().setName(getCurrentName());

                checkValidity();
                if (initialized) {
                    notifyConfigurationChanged();
                }
            }
        });

        roomNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, roomList);
        roomsListView.setAdapter(roomNamesAdapter);
        roomsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getConfiguration().setParentRoomName(getCheckedRoomName());
                notifyParentRoomChanged();

                checkValidity();
                notifyConfigurationChanged();
            }
        });

        updateRoomNamesList();

        addRoomFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        final Fragment fragment = this;
        addRoomFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateRoomDialog createRoomDialog = new CreateRoomDialog();
                createRoomDialog.setTargetFragment(fragment, 0);
                createRoomDialog.show(getFragmentManager(), null);
            }
        });

        initializeReceiverData();

        checkValidity();

        createTutorial();

        initialized = true;

        return rootView;
    }

    private void notifyParentRoomChanged() {
        EventBus.getDefault()
                .post(new ReceiverParentRoomChangedEvent());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_receiver_page_1;
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
                .anchor(name, TutorialTooltipView.Gravity.LEFT)
                .indicator(new IndicatorBuilder().offset(50, 0)
                        .onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_receiver_name__text)
                        .gravity(TutorialTooltipView.Gravity.RIGHT)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_receiver_name__id)
                .build();

        TutorialTooltipBuilder message2 = new TutorialTooltipBuilder(getActivity()).attachToDialog(getParentConfigurationDialog().getDialog())
                .anchor(addRoomFAB, TutorialTooltipView.Gravity.CENTER)
                .indicator(new IndicatorBuilder().color(Color.WHITE)
                        .onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_receiver_room_add__text)
                        .gravity(TutorialTooltipView.Gravity.LEFT)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_receiver_room_add__id)
                .build();

        TutorialTooltipBuilder message3 = new TutorialTooltipBuilder(getActivity()).attachToDialog(getParentConfigurationDialog().getDialog())
                .anchor(roomsListView, TutorialTooltipView.Gravity.CENTER)
                .indicator(new IndicatorBuilder().onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_receiver_room_select__text)
                        .gravity(TutorialTooltipView.Gravity.BOTTOM)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_receiver_room_select__id)
                .build();

        new TutorialTooltipChainBuilder().addItem(message1)
                .addItem(message2)
                .addItem(message3)
                .execute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onRoomAdded(RoomAddedEvent e) {
        updateRoomNamesList();

        String newRoomName = e.getRoomName();
        roomsListView.setItemChecked(roomNamesAdapter.getPosition(newRoomName), true);
    }

    private void initializeReceiverData() {
        if (getConfiguration().getReceiver() != null) {
            originalName = getConfiguration().getName();
            name.setText(originalName);
            roomsListView.setItemChecked(roomNamesAdapter.getPosition(getConfiguration().getParentRoomName()), true);
        }
    }

    private void updateRoomNamesList() {
        try {
            // Get Rooms
            roomList.clear();
            List<Room> rooms = persistanceHandler.getRooms(smartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID));
            for (Room room : rooms) {
                roomList.add(room.getName());
            }
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(getContentView(), e);
        }
        roomNamesAdapter.notifyDataSetChanged();
    }

    private String getCurrentName() {
        return name.getText()
                .toString()
                .trim();
    }

    private String getCheckedRoomName() {
        try {
            int checkedPosition = roomsListView.getCheckedItemPosition();
            return roomNamesAdapter.getItem(checkedPosition)
                    .trim();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkValidity() {
        // TODO: Performance Optimierung
        String currentReceiverName = getConfiguration().getName();
        String currentRoomName     = getConfiguration().getParentRoomName();

        if (currentReceiverName == null || currentReceiverName.length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            return false;
        }

        if (currentRoomName == null) {
            floatingName.setError(getString(R.string.no_room_selected));
            return false;
        }

        if (!currentReceiverName.equalsIgnoreCase(originalName)) {
            try {
                Room selectedRoom = persistanceHandler.getRoom(currentRoomName);
                if (getConfiguration().receiverNameAlreadyExists(selectedRoom)) {
                    floatingName.setError(getString(R.string.receiver_already_exists));
                    return false;
                }
            } catch (Exception e) {
                Timber.e(e);
                floatingName.setError(getString(R.string.unknown_error));
                return false;
            }
        }

        floatingName.setError(null);
        return true;
    }

}

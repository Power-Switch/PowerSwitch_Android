/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.fragment.configure_scene;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import de.markusressel.android.library.tutorialtooltip.TutorialTooltip;
import de.markusressel.android.library.tutorialtooltip.builder.IndicatorBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.MessageBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.TutorialTooltipBuilder;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnIndicatorClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnMessageClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipIndicator;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipMessage;
import de.markusressel.android.library.tutorialtooltip.view.TooltipId;
import de.markusressel.android.library.tutorialtooltip.view.TutorialTooltipView;
import eu.power_switch.R;
import eu.power_switch.event.SceneSelectedReceiversChangedEvent;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.SceneConfigurationHolder;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.ThemeHelper;

/**
 * "Setup" Fragment used in Configure Scene Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureSceneDialogTabbedPage2Setup extends ConfigurationDialogPage<SceneConfigurationHolder> {

    @BindView(R.id.recyclerview_list_of_receivers)
    RecyclerView recyclerViewSelectedReceivers;

    private CustomRecyclerViewAdapter customRecyclerViewAdapter;
    private ArrayList<Room>           rooms;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rooms = new ArrayList<>();
        customRecyclerViewAdapter = new CustomRecyclerViewAdapter(getActivity(), rooms);
        recyclerViewSelectedReceivers.setAdapter(customRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getResources().getInteger(R.integer.scene_grid_span_count),
                StaggeredGridLayoutManager.VERTICAL);
        recyclerViewSelectedReceivers.setLayoutManager(layoutManager);

        initializeSceneData();

        createTutorial();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_scene_page_2;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onSelectedReceiversChanged(SceneSelectedReceiversChangedEvent e) {
        rooms.clear();
        rooms.addAll(getConfiguration().getCheckedReceivers());

        updateSceneItemList();
        getConfiguration().setSceneItems(customRecyclerViewAdapter.getSceneItems());
        notifyConfigurationChanged();
    }

    private void createTutorial() {
        OnMessageClickedListener onMessageClickListener = new OnMessageClickedListener() {
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
                .anchor(recyclerViewSelectedReceivers, TutorialTooltipView.Gravity.CENTER)
                .indicator(new IndicatorBuilder().onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_scene_states__text)
                        .gravity(TutorialTooltipView.Gravity.BOTTOM)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onMessageClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_scene_states__id)
                .build();

        TutorialTooltip.show(message1);
    }

    @Nullable
    @Override
    public View getContentView() {
        return rootView.findViewById(R.id.recyclerview_list_of_receivers);
    }

    private void initializeSceneData() {
        Scene scene = getConfiguration().getScene();

        if (scene != null) {
            try {
                ArrayList<Room>          checkedReceivers = new ArrayList<>();
                HashMap<Long, SceneItem> map              = new HashMap<>();

                for (SceneItem sceneItem : scene.getSceneItems()) {
                    map.put(sceneItem.getReceiverId(), sceneItem);

                    Receiver receiver = persistenceHandler.getReceiver(sceneItem.getReceiverId());

                    boolean roomFound = false;
                    for (Room room : checkedReceivers) {
                        if (room.getId()
                                .equals(receiver.getRoomId())) {
                            room.addReceiver(receiver);
                            roomFound = true;
                        }
                    }

                    if (!roomFound) {
                        Room room = persistenceHandler.getRoom(receiver.getRoomId());
                        room.getReceivers()
                                .clear();
                        room.addReceiver(receiver);
                        checkedReceivers.add(room);
                    }
                }

                rooms.clear();
                rooms.addAll(checkedReceivers);

                customRecyclerViewAdapter.setReceiverSceneItemHashMap(map);
            } catch (Exception e) {
                statusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    private void updateSceneItemList() {
        customRecyclerViewAdapter.notifyDataSetChanged();
    }

    private class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {
        private Context                  context;
        private ArrayList<Room>          rooms;
        private HashMap<Long, SceneItem> receiverSceneItemHashMap;

        CustomRecyclerViewAdapter(Context context, ArrayList<Room> rooms) {
            this.context = context;
            this.rooms = rooms;
            receiverSceneItemHashMap = new HashMap<>();
        }

        void setReceiverSceneItemHashMap(HashMap<Long, SceneItem> receiverSceneItemHashMap) {
            this.receiverSceneItemHashMap = receiverSceneItemHashMap;
        }

        ArrayList<SceneItem> getSceneItems() {
            ArrayList<SceneItem> sceneItems = new ArrayList<>();
            for (Room room : rooms) {
                for (Receiver receiver : room.getReceivers()) {
                    SceneItem sceneItem = receiverSceneItemHashMap.get(receiver.getId());
                    if (sceneItem == null) {
                        sceneItem = new SceneItem(receiver.getId(),
                                receiver.getButtons()
                                        .get(0)
                                        .getId());
                        receiverSceneItemHashMap.put(receiver.getId(), sceneItem);
                    }

                    sceneItems.add(sceneItem);
                }
            }
            return sceneItems;
        }

        @Override
        public CustomRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_room_dialog, parent, false);
            return new CustomRecyclerViewAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CustomRecyclerViewAdapter.ViewHolder holder, int position) {
            final Room room = rooms.get(position);

            String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater inflater       = (LayoutInflater) context.getSystemService(inflaterString);

            holder.roomName.setText(room.getName());

            // clear previous items
            holder.linearLayoutOfReceivers.removeAllViews();
            // add items
            for (final Receiver receiver : room.getReceivers()) {
                // create a new receiverRow for our current receiver and add it
                // to our table of all devices of our current room
                // the row will contain the device name and all buttons
                LinearLayout receiverRow = new LinearLayout(context);
                receiverRow.setOrientation(LinearLayout.HORIZONTAL);
                holder.linearLayoutOfReceivers.addView(receiverRow);

                // setup TextView to display device name
                AppCompatTextView receiverName = new AppCompatTextView(context);
                receiverName.setText(receiver.getName());
                receiverName.setTextSize(18);
                receiverName.setTextColor(ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary));
                receiverName.setGravity(Gravity.CENTER_VERTICAL);
                receiverRow.addView(receiverName,
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));

                TableLayout buttonLayout = new TableLayout(context);
                receiverRow.addView(buttonLayout,
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                int buttonsPerRow;
                if (receiver.getButtons()
                        .size() % 3 == 0) {
                    buttonsPerRow = 3;
                } else {
                    buttonsPerRow = 2;
                }

                int      i         = 0;
                TableRow buttonRow = null;

//                if (!receiverSceneItemHashMap.containsKey(receiver.getId())) {
//                    receiverSceneItemHashMap.put(receiver.getId(),
//                            new SceneItem(receiver,
//                                    receiver.getButtons()
//                                            .get(0)));
//                }
                final ArrayList<android.widget.Button> buttonList = new ArrayList<>();
                for (Button button : receiver.getButtons()) {
                    @SuppressLint("InflateParams") android.widget.Button buttonView = (android.widget.Button) inflater.inflate(R.layout.simple_button,
                            null,
                            false);
                    buttonList.add(buttonView);

                    final int accentColor   = ThemeHelper.getThemeAttrColor(getActivity(), R.attr.colorAccent);
                    final int inactiveColor = ThemeHelper.getThemeAttrColor(getActivity(), R.attr.textColorInactive);
                    if (receiverSceneItemHashMap.get(receiver.getId())
                            .getButtonId()
                            .equals(button.getId())) {
                        buttonView.setTextColor(accentColor);
                    } else {
                        buttonView.setTextColor(inactiveColor);
                    }
                    buttonView.setText(button.getName());
                    buttonView.setOnClickListener(new android.widget.Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            notifyConfigurationChanged();
                            for (android.widget.Button button : buttonList) {
                                if (button == v) {
                                    button.setTextColor(accentColor);

                                    for (Button receiverButton : receiver.getButtons()) {
                                        if (receiverButton.getName()
                                                .equals(button.getText())) {
                                            receiverSceneItemHashMap.get(receiver.getId())
                                                    .setButtonId(receiverButton.getId());
                                            break;
                                        }
                                    }
                                } else {
                                    button.setTextColor(inactiveColor);
                                }
                            }

                            getConfiguration().setSceneItems(getSceneItems());
                        }
                    });

                    if (i == 0 || i % buttonsPerRow == 0) {
                        buttonRow = new TableRow(context);
                        buttonRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                        buttonRow.addView(buttonView);
                        buttonLayout.addView(buttonRow);
                    } else {
                        buttonRow.addView(buttonView);
                    }

                    i++;
                }
            }
        }

        @Override
        public int getItemCount() {
            return rooms.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView roomName;
            LinearLayout linearLayoutOfReceivers;

            public ViewHolder(final View itemView) {
                super(itemView);
                this.roomName = itemView.findViewById(R.id.txt_room_name);
                this.linearLayoutOfReceivers = itemView.findViewById(R.id.layout_of_receivers);
            }
        }
    }
}

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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
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

import java.util.ArrayList;
import java.util.HashMap;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureSceneDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.SceneWidgetProvider;

/**
 * "Setup" Fragment used in Configure Scene Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureSceneDialogTabbedPage2SetupFragment extends ConfigurationDialogFragment implements ConfigurationDialogTabbedSummaryFragment {

    private BroadcastReceiver broadcastReceiver;
    private View rootView;
    private RecyclerView recyclerViewSelectedReceivers;
    private ArrayList<Room> rooms;
    private CustomRecyclerViewAdapter customRecyclerViewAdapter;

    private long currentId = -1;
    private String currentName = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_scene_page_2, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                currentName = intent.getStringExtra("name");
                rooms.clear();
                rooms.addAll((ArrayList<Room>) intent.getSerializableExtra("selectedReceivers"));
                updateSceneItemList();

                notifyConfigurationChanged();
            }
        };

        rooms = new ArrayList<>();
        customRecyclerViewAdapter = new CustomRecyclerViewAdapter(getActivity(), rooms);
        recyclerViewSelectedReceivers = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_receivers);
        recyclerViewSelectedReceivers.setAdapter(customRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.scene_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewSelectedReceivers.setLayoutManager(layoutManager);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureSceneDialog.SCENE_ID_KEY)) {
            currentId = args.getLong(ConfigureSceneDialog.SCENE_ID_KEY);
            initializeSceneData(currentId);
        }
        checkSetupValidity();

        return rootView;
    }

    private void initializeSceneData(long sceneId) {
        try {
            Scene scene = DatabaseHandler.getScene(sceneId);

            currentName = scene.getName();

            ArrayList<Room> checkedReceivers = new ArrayList<>();
            HashMap<Long, SceneItem> map = new HashMap<>();

            for (SceneItem sceneItem : scene.getSceneItems()) {
                map.put(sceneItem.getReceiver().getId(), sceneItem);

                boolean roomFound = false;
                for (Room room : checkedReceivers) {
                    if (room.getId() == sceneItem.getReceiver().getRoomId()) {
                        room.addReceiver(sceneItem.getReceiver());
                        roomFound = true;
                    }
                }

                if (!roomFound) {
                    Room room = DatabaseHandler.getRoom(sceneItem.getReceiver().getRoomId());
                    room.getReceivers().clear();
                    room.addReceiver(sceneItem.getReceiver());
                    checkedReceivers.add(room);
                }
            }

            rooms.clear();
            rooms.addAll(checkedReceivers);

            customRecyclerViewAdapter.setReceiverSceneItemHashMap(map);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    private void updateSceneItemList() {
        customRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void saveCurrentConfigurationToDatabase() {
        Scene newScene = new Scene(currentId, SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID), currentName);
        newScene.addSceneItems(customRecyclerViewAdapter.getSceneItems());

        try {
            if (currentId == -1) {
                DatabaseHandler.addScene(newScene);
            } else {
                DatabaseHandler.updateScene(newScene);
            }

            // notify scenes fragment
            ScenesFragment.sendScenesChangedBroadcast(getActivity());

            // update scene widgets
            SceneWidgetProvider.forceWidgetUpdate(getActivity());

            // update wear data
            UtilityService.forceWearDataUpdate(getActivity());

            StatusMessageHandler.showInfoMessage(((RecyclerViewFragment) getTargetFragment()).getRecyclerView(), R.string.scene_saved, Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    public boolean checkSetupValidity() {
        if (currentName == null || currentName.trim().isEmpty()) {
            return false;
        }

        return !rooms.isEmpty();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_NAME_SCENE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    private class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {
        private Context context;
        private ArrayList<Room> rooms;
        private HashMap<Long, SceneItem> receiverSceneItemHashMap;

        public CustomRecyclerViewAdapter(Context context, ArrayList<Room> rooms) {
            this.context = context;
            this.rooms = rooms;
            receiverSceneItemHashMap = new HashMap<>();
        }

        public void setReceiverSceneItemHashMap(HashMap<Long, SceneItem> receiverSceneItemHashMap) {
            this.receiverSceneItemHashMap = receiverSceneItemHashMap;
        }

        public ArrayList<SceneItem> getSceneItems() {
            ArrayList<SceneItem> sceneItems = new ArrayList<>();
            for (Room room : rooms) {
                for (Receiver receiver : room.getReceivers()) {
                    sceneItems.add(receiverSceneItemHashMap.get(receiver.getId()));
                }
            }
            return sceneItems;
        }

        @Override
        public CustomRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_room_dialog, parent, false);
            return new CustomRecyclerViewAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CustomRecyclerViewAdapter.ViewHolder holder, int position) {
            final Room room = rooms.get(position);

            String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(inflaterString);

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
                receiverRow.addView(receiverName, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));

                TableLayout buttonLayout = new TableLayout(context);
                receiverRow.addView(buttonLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                int buttonsPerRow;
                if (receiver.getButtons().size() % 3 == 0) {
                    buttonsPerRow = 3;
                } else {
                    buttonsPerRow = 2;
                }

                int i = 0;
                TableRow buttonRow = null;

                if (!receiverSceneItemHashMap.containsKey(receiver.getId())) {
                    receiverSceneItemHashMap.put(receiver.getId(), new SceneItem(receiver, receiver.getButtons()
                            .getFirst()));
                }
                final ArrayList<android.widget.Button> buttonList = new ArrayList<>();
                for (Button button : receiver.getButtons()) {
                    @SuppressLint("InflateParams")
                    android.widget.Button buttonView = (android.widget.Button) inflater.inflate(R.layout.simple_button, null, false);
                    buttonList.add(buttonView);

                    final int accentColor = ThemeHelper.getThemeAttrColor(getActivity(), R.attr.colorAccent);
                    final int inactiveColor = ThemeHelper.getThemeAttrColor(getActivity(), R.attr.textColorInactive);
                    if (receiverSceneItemHashMap.get(receiver.getId()).getActiveButton().getId().equals(button.getId())) {
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
                                        if (receiverButton.getName().equals(button.getText())) {
                                            receiverSceneItemHashMap.get(receiver.getId())
                                                    .setActiveButton(receiverButton);
                                            break;
                                        }
                                    }
                                } else {
                                    button.setTextColor(inactiveColor);
                                }
                            }
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

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView roomName;
            public LinearLayout linearLayoutOfReceivers;

            public ViewHolder(final View itemView) {
                super(itemView);
                this.roomName = (TextView) itemView.findViewById(R.id.txt_room_name);
                this.linearLayoutOfReceivers = (LinearLayout) itemView.findViewById(R.id.layout_of_receivers);
            }
        }
    }
}

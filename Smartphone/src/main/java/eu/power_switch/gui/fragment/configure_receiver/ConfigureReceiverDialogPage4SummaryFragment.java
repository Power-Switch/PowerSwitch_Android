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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.database.handler.ReceiverReflectionMagic;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.log.Log;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.device.AutoPairReceiver;
import eu.power_switch.obj.device.DipReceiver;
import eu.power_switch.obj.device.DipSwitch;
import eu.power_switch.obj.device.MasterSlaveReceiver;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.obj.device.UniversalReceiver;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.widget.activity.ConfigureReceiverWidgetActivity;
import eu.power_switch.widget.activity.ConfigureSceneWidgetActivity;

/**
 * "Summary" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage4SummaryFragment extends Fragment {

    private View rootView;
    private long currentId;
    private String currentName;
    private String currentRoomName;
    private String currentBrand;
    private String currentModel;
    private String currentType;
    private List<DipSwitch> currentDips;
    private char currentMaster;
    private int currentSlave;
    private long currentSeed;
    private List<UniversalButton> currentUniversalButtons;

    private BroadcastReceiver broadcastReceiver;

    private TextView name;
    private TextView roomName;
    private TextView brand;
    private TextView model;
    private TextView channelMaster;
    private TextView channelSlave;
    private LinearLayout linearLayoutMasterSlaveReceiver;
    private LinearLayout linearLayoutDipReceiver;
    private LinearLayout linearLayoutUniversalReceiver;
    private LinearLayout linearLayoutDips;
    private LinearLayout linearLayoutUniversalButtons;
    private LinearLayout linearLayoutAutoPairReceiver;
    private TextView seed;

    /**
     * Used to notify parent Dialog that configuration has changed
     *
     * @param context
     */
    public static void sendSummaryChangedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_RECEIVER_SUMMARY_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_receiver_page_4_summary, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(LocalBroadcastConstants.INTENT_BRAND_MODEL_CHANGED)) {
                    String brand = intent.getStringExtra("brand");
                    String model = intent.getStringExtra("model");

                    currentBrand = brand;
                    currentModel = model;

                    Receiver receiver = ReceiverReflectionMagic.getDummy(getActivity(), Receiver.getJavaPath(currentModel));
                    currentType = receiver.getType();
                } else if (intent.getAction().equals(LocalBroadcastConstants.INTENT_NAME_ROOM_CHANGED)) {
                    String name = intent.getStringExtra("name");
                    String roomName = intent.getStringExtra("roomName");

                    currentName = name;
                    currentRoomName = roomName;
                } else if (intent.getAction().equals(LocalBroadcastConstants.INTENT_CHANNEL_DETAILS_CHANGED)) {
                    char channelMaster = intent.getCharExtra("channelMaster", 'A');
                    int channelSlave = intent.getIntExtra("channelSlave", 0);
                    ArrayList<DipSwitch> dips = (ArrayList<DipSwitch>) intent.getSerializableExtra("dips");

                    long seed = intent.getLongExtra("seed", -1);

                    ArrayList<UniversalButton> universalButtons =
                            (ArrayList<UniversalButton>) intent.getSerializableExtra("universalButtons");

                    currentMaster = channelMaster;
                    currentSlave = channelSlave;
                    currentDips = dips;
                    currentSeed = seed;
                    currentUniversalButtons = universalButtons;
                }

                updateUi();
                sendSummaryChangedBroadcast(getActivity());
            }
        };

        name = (TextView) rootView.findViewById(R.id.textView_name);
        roomName = (TextView) rootView.findViewById(R.id.textView_roomName);
        brand = (TextView) rootView.findViewById(R.id.textView_brand);
        model = (TextView) rootView.findViewById(R.id.textView_model);
        channelMaster = (TextView) rootView.findViewById(R.id.textView_channelMaster);
        channelSlave = (TextView) rootView.findViewById(R.id.textView_channelSlave);

        linearLayoutMasterSlaveReceiver = (LinearLayout) rootView.findViewById(R.id.linearLayout_masterSlaveReceiver);

        linearLayoutDipReceiver = (LinearLayout) rootView.findViewById(R.id.linearLayout_dipReceiver);
        linearLayoutDips = (LinearLayout) rootView.findViewById(R.id.linearLayout_dips);

        linearLayoutAutoPairReceiver = (LinearLayout) rootView.findViewById(R.id.linearLayout_autoPair);
        seed = (TextView) rootView.findViewById(R.id.textView_seed);

        linearLayoutUniversalReceiver = (LinearLayout) rootView.findViewById(R.id.linearLayout_universalReceiver);
        linearLayoutUniversalButtons = (LinearLayout) rootView.findViewById(R.id.linearLayout_universalButtons);

        updateUi();

        Bundle args = getArguments();
        long receiverId = args.getLong("ReceiverId");
        currentId = receiverId;

        if (currentId != -1) {
            initializeReceiverData(receiverId);
        }

        return rootView;
    }

    private void initializeReceiverData(long receiverId) {
        final Receiver receiver = DatabaseHandler.getReceiver(receiverId);

        currentId = receiverId;
        currentName = receiver.getName();
        currentRoomName = DatabaseHandler.getRoom(receiver.getRoomId()).getName();
        currentType = receiver.getType();
        currentBrand = receiver.getBrand();
        currentModel = receiver.getModel();

        if (currentType.equals(Receiver.TYPE_MASTER_SLAVE)) {
            currentMaster = ((MasterSlaveReceiver) receiver).getMaster();
            currentSlave = ((MasterSlaveReceiver) receiver).getSlave();
        } else if (currentType.equals(Receiver.TYPE_DIPS)) {
            currentDips = ((DipReceiver) receiver).getDips();
        } else if (currentType.equals(Receiver.TYPE_AUTOPAIR)) {
            currentSeed = ((AutoPairReceiver) receiver).getSeed();
        } else if (currentType.equals(Receiver.TYPE_UNIVERSAL)) {
            currentUniversalButtons = ((UniversalReceiver) receiver).getUniversalButtons();
        }

        updateUi();
    }

    private void updateUi() {
        updateUiValues();
        updateUiVisibility();
    }

    private void updateUiValues() {
        name.setText(currentName);
        roomName.setText(currentRoomName);
        brand.setText(currentBrand);
        model.setText(currentModel);
        channelMaster.setText("" + currentMaster);
        channelSlave.setText("" + currentSlave);

        String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(inflaterString);

        if (currentDips != null) {
            linearLayoutDips.removeAllViews();
            for (DipSwitch dipSwitch : currentDips) {
                SwitchCompat switchCompat = (SwitchCompat) inflater.inflate(R.layout.default_switch_compat, null, false);
                switchCompat.setText(dipSwitch.getName());
                switchCompat.setChecked(dipSwitch.isChecked());
                switchCompat.setClickable(false);

                linearLayoutDips.addView(switchCompat, new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }

        seed.setText(currentSeed + "");

        if (currentUniversalButtons != null) {
            linearLayoutUniversalButtons.removeAllViews();
            for (UniversalButton universalButton : currentUniversalButtons) {

                LinearLayout linearLayout = new LinearLayout(getActivity());
                TextView textView = new TextView(getActivity());
                textView.setText("Name: " + universalButton.getName() + "\n"
                        + "Signal: " + universalButton.getSignal());
                linearLayout.addView(textView);

                linearLayoutUniversalButtons.addView(linearLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                        .MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    private void updateUiVisibility() {
        if (currentType != null) {
            if (currentType.equals(Receiver.TYPE_UNIVERSAL)) {
                linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
                linearLayoutDipReceiver.setVisibility(View.GONE);
                linearLayoutAutoPairReceiver.setVisibility(View.GONE);
                linearLayoutUniversalReceiver.setVisibility(View.VISIBLE);
            } else if (currentType.equals(Receiver.TYPE_AUTOPAIR)) {
                linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
                linearLayoutDipReceiver.setVisibility(View.GONE);
                linearLayoutAutoPairReceiver.setVisibility(View.VISIBLE);
                linearLayoutUniversalReceiver.setVisibility(View.GONE);
            } else if (currentType.equals(Receiver.TYPE_DIPS)) {
                linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
                linearLayoutDipReceiver.setVisibility(View.VISIBLE);
                linearLayoutAutoPairReceiver.setVisibility(View.GONE);
                linearLayoutUniversalReceiver.setVisibility(View.GONE);
            } else if (currentType.equals(Receiver.TYPE_MASTER_SLAVE)) {
                linearLayoutMasterSlaveReceiver.setVisibility(View.VISIBLE);
                linearLayoutDipReceiver.setVisibility(View.GONE);
                linearLayoutAutoPairReceiver.setVisibility(View.GONE);
                linearLayoutUniversalReceiver.setVisibility(View.GONE);
            }
        } else {
            linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
            linearLayoutDipReceiver.setVisibility(View.GONE);
            linearLayoutAutoPairReceiver.setVisibility(View.GONE);
            linearLayoutUniversalReceiver.setVisibility(View.GONE);
        }
    }

    public boolean checkSetupValidity() {
        if (currentName == null || currentName.trim().equals("")) {
            return false;
        }
        if (currentRoomName == null) {
            return false;
        }
        if (currentBrand == null) {
            return false;
        }
        if (currentModel == null) {
            return false;
        }

        if (currentType == null) {
            return false;
        }

        if (currentType.equals(Receiver.TYPE_DIPS)) {
            if (currentDips == null) {
                return false;
            }
            return true;
        } else if (currentType.equals(Receiver.TYPE_MASTER_SLAVE)) {
            if (currentMaster == '\u0000') {
                return false;
            }
            if (currentSlave <= 0) {
                return false;
            }
            return true;
        } else if (currentType.equals(Receiver.TYPE_AUTOPAIR)) {
            if (currentSeed == -1) {
                return false;
            }
            return true;
        } else if (currentType.equals(Receiver.TYPE_UNIVERSAL)) {
            if (currentUniversalButtons == null || currentUniversalButtons.size() == 0) {
                return false;
            } else {
                for (UniversalButton universalButton : currentUniversalButtons) {
                    if (universalButton.getName().length() == 0 || universalButton.getSignal().length() == 0) {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    public void saveCurrentConfigurationToDatabase() {
        Room room = DatabaseHandler.getRoom(currentRoomName);
        String receiverName = currentName;
        String modelName = currentModel;

        String className = Receiver.receiverMap.get(modelName);
        String type = ReceiverReflectionMagic.getType(className);

        Constructor<?> constructor = null;
        try {
            constructor = ReceiverReflectionMagic.getConstructor(className, type);
        } catch (Exception e) {
            Log.e("AddReceiverDialog", e);
        }

        Receiver receiver = null;
        if (Receiver.TYPE_MASTER_SLAVE.equals(type)) {
            try {
                receiver = (Receiver) constructor.newInstance(getActivity(), currentId, receiverName, currentMaster, currentSlave, room
                        .getId());
            } catch (Exception e) {
                Log.e("AddReceiverDialog", e);
            }
        } else if (Receiver.TYPE_DIPS.equals(type)) {
            LinkedList<Boolean> dipValues = new LinkedList<>();
            for (DipSwitch dipSwitch : currentDips) {
                dipValues.add(dipSwitch.isChecked());
            }

            try {
                receiver = (Receiver) constructor.newInstance(getActivity(), currentId, receiverName, dipValues, room.getId());
            } catch (Exception e) {
                Log.e("AddReceiverDialog", e);
            }
        } else if (Receiver.TYPE_AUTOPAIR.equals(type)) {
            try {
                receiver = (Receiver) constructor.newInstance(getActivity(), currentId, receiverName, currentSeed, room.getId());
            } catch (Exception e) {
                Log.e("AddReceiverDialog", e);
            }
        } else if (Receiver.TYPE_UNIVERSAL.equals(type)) {
            receiver = new UniversalReceiver(getActivity(), currentId, currentName, currentUniversalButtons,
                    room.getId());
        }

        if (receiver != null) {
            if (currentId == -1) {
                DatabaseHandler.addReceiver(receiver);
            } else {
                DatabaseHandler.updateReceiver(receiver);
            }
        }


        RoomsFragment.sendReceiverChangedBroadcast(getActivity());
        // scenes could change too if receiver was used in a scene
        ScenesFragment.sendScenesChangedBroadcast(getActivity());

        // update receiver widgets
        ConfigureReceiverWidgetActivity.forceWidgetUpdate(getActivity());
        // update scene widgets
        ConfigureSceneWidgetActivity.forceWidgetUpdate(getActivity());

        StatusMessageHandler.showStatusMessage(getActivity(), getString(R.string.receiver_saved), Snackbar.LENGTH_LONG);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_NAME_ROOM_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_BRAND_MODEL_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_CHANNEL_DETAILS_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

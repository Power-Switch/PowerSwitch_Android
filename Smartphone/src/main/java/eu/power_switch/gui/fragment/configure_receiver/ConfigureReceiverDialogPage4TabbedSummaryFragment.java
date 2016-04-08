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
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureReceiverDialog;
import eu.power_switch.gui.dialog.WriteNfcTagDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.UniversalReceiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;

/**
 * "Summary" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage4TabbedSummaryFragment extends ConfigurationDialogFragment implements ConfigurationDialogTabbedSummaryFragment {

    private View rootView;
    private long currentId = -1;
    private String currentName;
    private String currentRoomName;
    private Receiver.Brand currentBrand;
    private String currentModel;
    private Receiver.Type currentType;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_receiver_page_4_summary, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(LocalBroadcastConstants.INTENT_BRAND_MODEL_CHANGED)) {
                    String brand = intent.getStringExtra("brand");
                    String model = intent.getStringExtra("model");

                    currentBrand = Receiver.Brand.getEnum(brand);
                    currentModel = model;

                    try {
                        Receiver receiver = ReceiverReflectionMagic.getDummy(getActivity(), Receiver.getJavaPath(currentModel));
                        currentType = receiver.getType();
                    } catch (Exception e) {
                        StatusMessageHandler.showErrorMessage(getActivity(), e);
                    }
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
                notifyConfigurationChanged();
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

        Button buttonWriteNfcTag = (Button) rootView.findViewById(R.id.button_write_nfc_tag);
        buttonWriteNfcTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(WriteNfcTagDialog.getNewInstanceIntent(String.valueOf(currentId)));
            }
        });

        updateUi();

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureReceiverDialog.RECEIVER_ID_KEY)) {
            long receiverId = args.getLong(ConfigureReceiverDialog.RECEIVER_ID_KEY);
            currentId = receiverId;
            initializeReceiverData(receiverId);
        }

        return rootView;
    }

    private void initializeReceiverData(long receiverId) {
        try {
            final Receiver receiver = DatabaseHandler.getReceiver(receiverId);

            currentId = receiverId;
            currentName = receiver.getName();
            currentRoomName = DatabaseHandler.getRoom(receiver.getRoomId()).getName();
            currentType = receiver.getType();
            currentBrand = receiver.getBrand();
            currentModel = receiver.getModel();

            switch (currentType) {
                case DIPS:
                    currentDips = ((DipReceiver) receiver).getDips();
                    break;
                case MASTER_SLAVE:
                    currentMaster = ((MasterSlaveReceiver) receiver).getMaster();
                    currentSlave = ((MasterSlaveReceiver) receiver).getSlave();
                    break;
                case UNIVERSAL:
                    currentUniversalButtons = ((UniversalReceiver) receiver).getUniversalButtons();
                    break;
                case AUTOPAIR:
                    currentSeed = ((AutoPairReceiver) receiver).getSeed();
                    break;
            }

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
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
        if (currentBrand == null) {
            brand.setText("");
        } else {
            brand.setText(currentBrand.toString());
        }
        model.setText(currentModel);
        channelMaster.setText(String.valueOf(currentMaster));
        channelSlave.setText(String.valueOf(currentSlave));

        String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(inflaterString);

        if (currentDips != null) {
            linearLayoutDips.removeAllViews();
            for (DipSwitch dipSwitch : currentDips) {
                @SuppressLint("InflateParams")
                SwitchCompat switchCompat = (SwitchCompat) inflater.inflate(R.layout.default_switch_compat, null, false);
                switchCompat.setText(dipSwitch.getName());
                switchCompat.setChecked(dipSwitch.isChecked());
                switchCompat.setClickable(false);

                linearLayoutDips.addView(switchCompat, new LinearLayout.LayoutParams(LinearLayout.LayoutParams
                        .WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        }

        seed.setText(String.valueOf(currentSeed));

        if (currentUniversalButtons != null) {
            linearLayoutUniversalButtons.removeAllViews();
            for (UniversalButton universalButton : currentUniversalButtons) {

                LinearLayout linearLayout = new LinearLayout(getActivity());
                AppCompatTextView textView = new AppCompatTextView(getActivity());
                textView.setText("Name: " + universalButton.getName() + "\n"
                        + "Signal: " + universalButton.getSignal());
                linearLayout.addView(textView);

                linearLayoutUniversalButtons.addView(linearLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    private void updateUiVisibility() {
        if (currentType != null) {
            switch (currentType) {
                case DIPS:
                    linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
                    linearLayoutDipReceiver.setVisibility(View.VISIBLE);
                    linearLayoutAutoPairReceiver.setVisibility(View.GONE);
                    linearLayoutUniversalReceiver.setVisibility(View.GONE);
                    break;
                case MASTER_SLAVE:
                    linearLayoutMasterSlaveReceiver.setVisibility(View.VISIBLE);
                    linearLayoutDipReceiver.setVisibility(View.GONE);
                    linearLayoutAutoPairReceiver.setVisibility(View.GONE);
                    linearLayoutUniversalReceiver.setVisibility(View.GONE);
                    break;
                case UNIVERSAL:
                    linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
                    linearLayoutDipReceiver.setVisibility(View.GONE);
                    linearLayoutAutoPairReceiver.setVisibility(View.GONE);
                    linearLayoutUniversalReceiver.setVisibility(View.VISIBLE);
                    break;
                case AUTOPAIR:
                    linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
                    linearLayoutDipReceiver.setVisibility(View.GONE);
                    linearLayoutAutoPairReceiver.setVisibility(View.VISIBLE);
                    linearLayoutUniversalReceiver.setVisibility(View.GONE);
                    break;
            }
        } else {
            linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
            linearLayoutDipReceiver.setVisibility(View.GONE);
            linearLayoutAutoPairReceiver.setVisibility(View.GONE);
            linearLayoutUniversalReceiver.setVisibility(View.GONE);
        }
    }

    @Override
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

        switch (currentType) {
            case DIPS:
                return currentDips != null;
            case MASTER_SLAVE:
                if (currentMaster == '\u0000') {
                    return false;
                }
                return currentSlave > 0;
            case UNIVERSAL:
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
            case AUTOPAIR:
                return currentSeed != -1;
        }

        return false;
    }

    @Override
    public void saveCurrentConfigurationToDatabase() throws Exception {
        Apartment apartment = DatabaseHandler.getApartment(SmartphonePreferencesHandler.getCurrentApartmentId());
        Room room = apartment.getRoom(currentRoomName);
        String receiverName = currentName;
        String modelName = currentModel;

        String className = Receiver.receiverMap.get(modelName);
        Receiver.Type type = ReceiverReflectionMagic.getType(className);

        Constructor<?> constructor = ReceiverReflectionMagic.getConstructor(className, type);

        Receiver receiver = null;
        switch (type) {
            case DIPS:
                LinkedList<Boolean> dipValues = new LinkedList<>();
                for (DipSwitch dipSwitch : currentDips) {
                    dipValues.add(dipSwitch.isChecked());
                }

                receiver = (Receiver) constructor.newInstance(
                        getActivity(), currentId, receiverName, dipValues, room.getId());
                break;
            case MASTER_SLAVE:
                receiver = (Receiver) constructor.newInstance(
                        getActivity(), currentId, receiverName, currentMaster, currentSlave, room.getId());
                break;
            case UNIVERSAL:
                receiver = new UniversalReceiver(
                        getActivity(), currentId, currentName, currentUniversalButtons, room.getId());
                break;
            case AUTOPAIR:
                receiver = (Receiver) constructor.newInstance(
                        getActivity(), currentId, receiverName, currentSeed, room.getId());
                break;
        }

        if (currentId == -1) {
            DatabaseHandler.addReceiver(receiver);
        } else {
            DatabaseHandler.updateReceiver(receiver);
        }

        RoomsFragment.sendReceiverChangedBroadcast(getActivity());

        // update receiver widgets
        ReceiverWidgetProvider.forceWidgetUpdate(getActivity());

        StatusMessageHandler.showInfoMessage(((RecyclerViewFragment) getTargetFragment()).getRecyclerView(), R.string.receiver_saved, Snackbar.LENGTH_LONG);
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

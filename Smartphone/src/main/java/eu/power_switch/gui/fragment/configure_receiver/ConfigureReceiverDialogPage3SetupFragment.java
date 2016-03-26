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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.database.handler.ReceiverReflectionMagic;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigureReceiverDialog;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.UniversalReceiver;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * "Setup" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage3SetupFragment extends ConfigurationDialogFragment {

    private View rootView;

    private ListView channelMasterListView;
    private ArrayAdapter<String> channelMasterNamesAdapter;

    private ListView channelSlaveListView;
    private ArrayAdapter<String> channelSlaveNamesAdapter;

    private TableLayout layoutMasterSlave;
    private NestedScrollView layoutDip;
    private NestedScrollView layoutAutoPair;
    private LinearLayout layoutUniversal;
    private LinearLayout buttonsList;

    private ArrayList<SwitchCompat> dipViewList;
    private BroadcastReceiver broadcastReceiver;
    private ArrayList<DipSwitch> dipSwitchArrayList;

    private Receiver currentAutoPairReceiver;


    /**
     * Used to notify the summary page that some info has changed
     *
     * @param context          any suitable context
     * @param channelMaster    Current selected Master Channel
     * @param channelSlave     Current selected Slave Channel
     * @param dips             Current Dip configuration
     * @param universalButtons Current Universal Buttons
     */
    public static void sendChannelDetailsChangedBroadcast(Context context, Character channelMaster, Integer channelSlave,
                                                          ArrayList<DipSwitch> dips, long seed,
                                                          ArrayList<UniversalButton> universalButtons) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_CHANNEL_DETAILS_CHANGED);
        intent.putExtra("channelMaster", channelMaster);
        intent.putExtra("channelSlave", channelSlave);
        intent.putExtra("dips", dips);
        intent.putExtra("seed", seed);
        intent.putExtra("universalButtons", universalButtons);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_receiver_page_3, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(LocalBroadcastConstants.INTENT_BRAND_MODEL_CHANGED)) {
                    String model = intent.getStringExtra("model");

                    try {
                        Receiver receiver = ReceiverReflectionMagic.getDummy(getActivity(), Receiver.getJavaPath(model));
                        initType(receiver);

                        sendChannelDetailsChangedBroadcast(getActivity(), getSelectedChannelMaster(), getSelectedChannelSlave(),
                                dipSwitchArrayList, getCurrentSeed(), getCurrentUniversalButtons());
                    } catch (Exception e) {
                        StatusMessageHandler.showErrorMessage(getActivity(), e);
                    }
                }
            }
        };

        // Master/Slave
        layoutMasterSlave = (TableLayout) rootView.findViewById(R.id.tableLayout_MasterSlave);
        channelMasterListView = (ListView) rootView.findViewById(R.id.listView_channelMaster);
        channelMasterNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice);
        channelMasterListView.setAdapter(channelMasterNamesAdapter);
        channelMasterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendChannelDetailsChangedBroadcast(getActivity(), getSelectedChannelMaster(), getSelectedChannelSlave
                        (), null, getCurrentSeed(), null);
            }
        });

        channelSlaveListView = (ListView) rootView.findViewById(R.id.listView_channelSlave);
        channelSlaveNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice);
        channelSlaveListView.setAdapter(channelSlaveNamesAdapter);
        channelSlaveListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendChannelDetailsChangedBroadcast(getActivity(), getSelectedChannelMaster(), getSelectedChannelSlave
                        (), null, getCurrentSeed(), null);
            }
        });

        // Dips
        layoutDip = (NestedScrollView) rootView.findViewById(R.id.scrollView_dip);
        dipViewList = new ArrayList<>();
        SwitchCompat dip0 = (SwitchCompat) rootView.findViewById(R.id.switch_dip0);
        SwitchCompat dip1 = (SwitchCompat) rootView.findViewById(R.id.switch_dip1);
        SwitchCompat dip2 = (SwitchCompat) rootView.findViewById(R.id.switch_dip2);
        SwitchCompat dip3 = (SwitchCompat) rootView.findViewById(R.id.switch_dip3);
        SwitchCompat dip4 = (SwitchCompat) rootView.findViewById(R.id.switch_dip4);
        SwitchCompat dip5 = (SwitchCompat) rootView.findViewById(R.id.switch_dip5);
        SwitchCompat dip6 = (SwitchCompat) rootView.findViewById(R.id.switch_dip6);
        SwitchCompat dip7 = (SwitchCompat) rootView.findViewById(R.id.switch_dip7);
        SwitchCompat dip8 = (SwitchCompat) rootView.findViewById(R.id.switch_dip8);
        SwitchCompat dip9 = (SwitchCompat) rootView.findViewById(R.id.switch_dip9);
        dipViewList.add(dip0);
        dipViewList.add(dip1);
        dipViewList.add(dip2);
        dipViewList.add(dip3);
        dipViewList.add(dip4);
        dipViewList.add(dip5);
        dipViewList.add(dip6);
        dipViewList.add(dip7);
        dipViewList.add(dip8);
        dipViewList.add(dip9);

        CompoundButton.OnCheckedChangeListener dipCheckedChangedListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ArrayList<DipSwitch> dips = new ArrayList<>();
                for (SwitchCompat switchCompat : dipViewList) {
                    if (switchCompat.getVisibility() == View.VISIBLE) {
                        dips.add(new DipSwitch(switchCompat.getText().toString(), switchCompat.isChecked()));
                    }
                }

                sendChannelDetailsChangedBroadcast(getActivity(), '\n', 0, dips, getCurrentSeed(), null);
            }
        };

        for (SwitchCompat switchCompat : dipViewList) {
            switchCompat.setOnCheckedChangeListener(dipCheckedChangedListener);
        }


        // AutoPair
        layoutAutoPair = (NestedScrollView) rootView.findViewById(R.id.scrollView_autoPair);
        android.widget.Button buttonPair = (android.widget.Button) rootView.findViewById(R.id.button_pair);
        buttonPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Gateway> activeGateways = DatabaseHandler.getAllGateways(true);

                    if (activeGateways.isEmpty()) {
                        StatusMessageHandler.showNoActiveGatewayMessage(getActivity());
                        return;
                    }

                    ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
                    for (Gateway gateway : activeGateways) {
                        try {
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(gateway, getString(R.string.pair)));
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(gateway, getString(R.string.pair)));
                        } catch (Exception e) {
                            Log.e(e);
                        }
                    }

                    NetworkHandler.init(getContext());
                    NetworkHandler.send(networkPackages);

                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                }
            }
        });
        android.widget.Button buttonUnpair = (android.widget.Button) rootView.findViewById(R.id.button_unpair);
        buttonUnpair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Gateway> activeGateways = DatabaseHandler.getAllGateways(true);

                    if (activeGateways.isEmpty()) {
                        StatusMessageHandler.showNoActiveGatewayMessage(getActivity());
                        return;
                    }

                    ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
                    for (Gateway gateway : activeGateways) {
                        try {
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(gateway, getString(R.string.unpair)));
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(gateway, getString(R.string.unpair)));
                        } catch (Exception e) {
                            Log.e(e);
                        }
                    }

                    NetworkHandler.init(getContext());
                    NetworkHandler.send(networkPackages);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                }
            }
        });
        android.widget.Button buttonUnpairAll = (android.widget.Button) rootView.findViewById(R.id.button_unpairAll);
        buttonUnpairAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Gateway> activeGateways = DatabaseHandler.getAllGateways(true);

                    if (activeGateways.isEmpty()) {
                        StatusMessageHandler.showNoActiveGatewayMessage(getActivity());
                        return;
                    }

                    ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
                    for (Gateway gateway : activeGateways) {
                        try {
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(gateway, getString(R.string.unpair_all)));
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(gateway, getString(R.string.unpair_all)));
                        } catch (Exception e) {
                            Log.e(e);
                        }
                    }

                    NetworkHandler.init(getContext());
                    NetworkHandler.send(networkPackages);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                }
            }
        });

        // Universal
        layoutUniversal = (LinearLayout) rootView.findViewById(R.id.linearLayout_universalButtons);
        FloatingActionButton addUniversalButtonFAB = (FloatingActionButton) rootView.findViewById(R.id
                .add_universalButton_fab);
        addUniversalButtonFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        buttonsList = (LinearLayout) rootView.findViewById(R.id.universalButtons_List);

        addUniversalButtonFAB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addUniversalButtonLayoutToDialogView();
                sendChannelDetailsChangedBroadcast(getActivity(), null, 0, null, -1, getCurrentUniversalButtons());
            }
        });

        updateUi(null);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureReceiverDialog.RECEIVER_ID_KEY)) {
            long receiverId = args.getLong(ConfigureReceiverDialog.RECEIVER_ID_KEY);
            initializeReceiverData(receiverId);
        }

        return rootView;
    }

    private long getCurrentSeed() {
        try {
            return ((AutoPairReceiver) currentAutoPairReceiver).getSeed();
        } catch (Exception e) {
            return -1;
        }
    }

    private void initializeReceiverData(long receiverId) {
        try {
            Receiver receiver = DatabaseHandler.getReceiver(receiverId);
            initType(receiver);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    private void initData(final MasterSlaveReceiver receiver) {
        channelMasterNamesAdapter.clear();
        channelMasterNamesAdapter.addAll(receiver.getMasterNames());
        channelMasterNamesAdapter.notifyDataSetChanged();


        channelSlaveNamesAdapter.clear();
        channelSlaveNamesAdapter.addAll(receiver.getSlaveNames());
        channelSlaveNamesAdapter.notifyDataSetChanged();

        int channelMasterPosition = channelMasterNamesAdapter.getPosition(receiver.getMaster() + "");
        if (channelMasterPosition == -1) {
            channelMasterPosition = 0;
        }
        channelMasterListView.setItemChecked(channelMasterPosition, true);
        channelMasterListView.smoothScrollToPosition(channelMasterPosition);

        int channelSlavePosition = channelSlaveNamesAdapter.getPosition(receiver.getSlave() + "");
        if (channelSlavePosition == -1) {
            channelSlavePosition = 0;
        }
        channelSlaveListView.setItemChecked(channelSlavePosition, true);
        channelSlaveListView.smoothScrollToPosition(channelSlavePosition);
    }

    private void initData(DipReceiver receiver) {
        LinkedList<DipSwitch> dips = receiver.getDips();

        int i;
        for (i = 0; i < receiver.getDipNames().size(); i++) {
            SwitchCompat currentSwitch = dipViewList.get(i);
            currentSwitch.setText(receiver.getDipNames().get(i));
            currentSwitch.setVisibility(View.VISIBLE);
            currentSwitch.setChecked(dips.get(i).isChecked());
        }
        while (i < 10) {
            SwitchCompat currentSwitch = dipViewList.get(i);
            currentSwitch.setVisibility(View.INVISIBLE);
            i++;
        }

        dipSwitchArrayList = new ArrayList<>();
        for (SwitchCompat switchCompat : dipViewList) {
            if (switchCompat.getVisibility() == View.VISIBLE) {
                dipSwitchArrayList.add(new DipSwitch(switchCompat.getText().toString(), switchCompat.isChecked()));
            }
        }
    }

    private void initData(UniversalReceiver receiver) {
        for (UniversalButton universalButton : receiver.getUniversalButtons()) {
            addUniversalButtonLayoutToDialogView(universalButton.getName(), universalButton.getSignal());
        }
    }

    private void initType(Receiver receiver) {
        switch (receiver.getType()) {
            case DIPS:
                updateUi(receiver);
                initData((DipReceiver) receiver);
                break;
            case MASTER_SLAVE:
                updateUi(receiver);
                initData((MasterSlaveReceiver) receiver);
                break;
            case UNIVERSAL:
                updateUi(receiver);
                initData((UniversalReceiver) receiver);
                break;
            case AUTOPAIR:
                currentAutoPairReceiver = receiver;
                updateUi(receiver);
                break;
        }
    }

    private void updateUi(Receiver receiver) {
        if (receiver == null) {
            layoutMasterSlave.setVisibility(View.GONE);
            layoutDip.setVisibility(View.GONE);
            layoutAutoPair.setVisibility(View.GONE);
            layoutUniversal.setVisibility(View.GONE);
        } else {
            switch (receiver.getType()) {
                case DIPS:
                    layoutMasterSlave.setVisibility(View.GONE);
                    layoutDip.setVisibility(View.VISIBLE);
                    layoutAutoPair.setVisibility(View.GONE);
                    layoutUniversal.setVisibility(View.GONE);
                    break;
                case MASTER_SLAVE:
                    layoutMasterSlave.setVisibility(View.VISIBLE);
                    layoutDip.setVisibility(View.GONE);
                    layoutAutoPair.setVisibility(View.GONE);
                    layoutUniversal.setVisibility(View.GONE);
                    break;
                case UNIVERSAL:
                    layoutMasterSlave.setVisibility(View.GONE);
                    layoutDip.setVisibility(View.GONE);
                    layoutAutoPair.setVisibility(View.GONE);
                    layoutUniversal.setVisibility(View.VISIBLE);
                    break;
                case AUTOPAIR:
                    layoutMasterSlave.setVisibility(View.GONE);
                    layoutDip.setVisibility(View.GONE);
                    layoutAutoPair.setVisibility(View.VISIBLE);
                    layoutUniversal.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void addUniversalButtonLayoutToDialogView() {
        addUniversalButtonLayoutToDialogView("", "");
    }

    private void addUniversalButtonLayoutToDialogView(String name, String signal) {
        TextWatcher tw = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sendChannelDetailsChangedBroadcast(getActivity(), null, 0, null, -1, getCurrentUniversalButtons());
            }
        };
        LinearLayout newUniversalButtonLayout = new LinearLayout(getActivity());
        newUniversalButtonLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout nameLayout = new LinearLayout(getActivity());
        nameLayout.setOrientation(LinearLayout.HORIZONTAL);

        AppCompatEditText universalButtonNameEditText = new AppCompatEditText(getActivity());
        universalButtonNameEditText.setHint(R.string.name);
        universalButtonNameEditText.setText(name);
        universalButtonNameEditText.addTextChangedListener(tw);

        ImageButton deleteUniversalButton = new ImageButton(getActivity());
        deleteUniversalButton.setBackgroundResource(android.R.color.transparent);
        deleteUniversalButton.setImageResource(android.R.drawable.ic_menu_delete);
        deleteUniversalButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                buttonsList.removeView((View) v.getParent().getParent());
                sendChannelDetailsChangedBroadcast(getActivity(), null, 0, null, -1, getCurrentUniversalButtons());

            }
        });

        nameLayout.addView(universalButtonNameEditText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
        nameLayout.addView(deleteUniversalButton);

        AppCompatEditText universalButtonSignalEditText = new AppCompatEditText(getActivity());
        universalButtonSignalEditText.setHint(R.string.enter_network_signal);
        universalButtonSignalEditText.setText(signal);
        universalButtonSignalEditText.addTextChangedListener(tw);

        newUniversalButtonLayout.addView(nameLayout);
        newUniversalButtonLayout.addView(universalButtonSignalEditText);

        buttonsList.addView(newUniversalButtonLayout);
    }

    private Character getSelectedChannelMaster() {
        try {
            if (channelMasterListView.getCheckedItemPosition() > channelMasterListView.getCount() || channelMasterListView
                    .getCheckedItemPosition() == -1) {
                channelMasterListView.setItemChecked(0, true);
            }
            return channelMasterNamesAdapter.getItem(channelMasterListView.getCheckedItemPosition())
                    .toString().charAt(0);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getSelectedChannelSlave() {
        try {
            if (channelSlaveListView.getCheckedItemPosition() > channelSlaveListView.getCount() || channelSlaveListView
                    .getCheckedItemPosition() == -1) {
                channelSlaveListView.setItemChecked(0, true);
            }

            return Integer.valueOf(channelSlaveNamesAdapter.getItem(channelSlaveListView.getCheckedItemPosition())
                    .toString());
        } catch (Exception e) {
            return null;
        }
    }

    private ArrayList<UniversalButton> getCurrentUniversalButtons() {
        ArrayList<UniversalButton> buttons = new ArrayList<>();

        for (int i = 0; i < buttonsList.getChildCount(); i++) {
            LinearLayout universalButtonLayout = (LinearLayout) buttonsList.getChildAt(i);

            LinearLayout nameLayout = (LinearLayout) universalButtonLayout.getChildAt(0);
            AppCompatEditText nameEditText = (AppCompatEditText) nameLayout.getChildAt(0);
            AppCompatEditText signalEditText = (AppCompatEditText) universalButtonLayout.getChildAt(1);

            buttons.add(new UniversalButton(null, nameEditText.getText().toString(), null, signalEditText.getText()
                    .toString()));
        }

        return buttons;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_BRAND_MODEL_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

}

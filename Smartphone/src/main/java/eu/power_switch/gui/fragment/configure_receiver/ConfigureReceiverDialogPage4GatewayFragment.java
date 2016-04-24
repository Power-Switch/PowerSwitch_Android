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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.GatewayNameRecyclerViewAdapter;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigureReceiverDialog;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * "Gateway"/"Network" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 24.04.2016.
 */
public class ConfigureReceiverDialogPage4GatewayFragment extends ConfigurationDialogFragment {

    public static final String KEY_REPEAT_AMOUNT = "repeatAmount";
    public static final String KEY_ASSOCIATED_GATEWAYS = "associatedGateways";

    private View rootView;

    private int repeatAmount = 0;
    private List<Gateway> gateways = new ArrayList<>();

    private BroadcastReceiver broadcastReceiver;
    private TextView textView_repeatAmount;
    private Button buttonPlus;
    private Button buttonMinus;
    private RecyclerView recyclerViewGateways;
    private GatewayNameRecyclerViewAdapter gatewayNameRecyclerViewAdapter;


    /**
     * Used to notify the summary page that some info has changed
     *
     * @param context      any suitable context
     * @param repeatAmount repeat amount
     * @param gateways     list of gateways
     */
    public static void sendGatewayDetailsChangedBroadcast(Context context, Integer repeatAmount,
                                                          List<Gateway> gateways) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_GATEWAY_DETAILS_CHANGED);
        intent.putExtra(KEY_REPEAT_AMOUNT, repeatAmount);
        intent.putExtra(KEY_ASSOCIATED_GATEWAYS, new ArrayList<>(gateways));

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_receiver_page_4, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocalBroadcastConstants.INTENT_GATEWAY_ADDED.equals(intent.getAction())) {
//                    ArrayList<Gateway> newGateways = intent.getStringArrayListExtra(AddGatewayDialog.KEY_SSID);
//                    gateways.addAll(newGateways);
                    gatewayNameRecyclerViewAdapter.notifyDataSetChanged();
                    sendGatewayDetailsChangedBroadcast(getContext(), repeatAmount, gateways);
                }
            }
        };

        textView_repeatAmount = (TextView) rootView.findViewById(R.id.textView_repeatAmount);
        textView_repeatAmount.setText(String.valueOf(repeatAmount));

        buttonPlus = (Button) rootView.findViewById(R.id.button_plus);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatAmount++;
                textView_repeatAmount.setText(String.valueOf(repeatAmount));

                buttonMinus.setEnabled(true);

                if (repeatAmount >= 3) {
                    buttonPlus.setEnabled(false);
                }

                sendGatewayDetailsChangedBroadcast(getContext(), repeatAmount, gateways);
            }
        });

        buttonMinus = (Button) rootView.findViewById(R.id.button_minus);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatAmount--;
                textView_repeatAmount.setText(String.valueOf(repeatAmount));

                buttonPlus.setEnabled(true);

                if (repeatAmount <= 0) {
                    buttonMinus.setEnabled(false);
                }

                sendGatewayDetailsChangedBroadcast(getContext(), repeatAmount, gateways);
            }
        });

        recyclerViewGateways = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        gatewayNameRecyclerViewAdapter = new GatewayNameRecyclerViewAdapter(getActivity(), gateways);
        recyclerViewGateways.setAdapter(gatewayNameRecyclerViewAdapter);
        gatewayNameRecyclerViewAdapter.setOnDeleteClickListener(new GatewayNameRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, final int position) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.delete)
                        .setMessage(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    gateways.remove(position);
                                    gatewayNameRecyclerViewAdapter.notifyDataSetChanged();
                                    notifyConfigurationChanged();
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                                }
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();
            }
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewGateways.setLayoutManager(layoutManager);

        FloatingActionButton addGatewayFAB = (FloatingActionButton) rootView.findViewById(R.id.add_fab);
        addGatewayFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        final Fragment fragment = this;
        addGatewayFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AddSsidDialog addSsidDialog = new AddSsidDialog();
//                addSsidDialog.setTargetFragment(fragment, 0);
//                addSsidDialog.show(getFragmentManager(), null);
            }
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureReceiverDialog.RECEIVER_ID_KEY)) {
            long receiverId = args.getLong(ConfigureReceiverDialog.RECEIVER_ID_KEY);
            initializeReceiverData(receiverId);
        }

        return rootView;
    }

    private void initializeReceiverData(long receiverId) {
        try {
            Receiver receiver = DatabaseHandler.getReceiver(receiverId);
            repeatAmount = receiver.getRepeatAmount();
            textView_repeatAmount.setText(String.valueOf(repeatAmount));

            gateways = receiver.getAssociatedGateways();
            gatewayNameRecyclerViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GATEWAY_ADDED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

}

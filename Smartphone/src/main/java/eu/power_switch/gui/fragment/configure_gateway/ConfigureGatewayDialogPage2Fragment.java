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

package eu.power_switch.gui.fragment.configure_gateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.SsidRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddSsidDialog;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureGatewayDialog;
import eu.power_switch.gui.fragment.settings.GatewaySettingsFragment;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.EZControl_XS1;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.gateway.RaspyRFM;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.shared.exception.gateway.GatewayUnknownException;

/**
 * "Name" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureGatewayDialogPage2Fragment extends ConfigurationDialogFragment implements ConfigurationDialogTabbedSummaryFragment {

    private View rootView;
    private long gatewayId = -1;

    private String currentName;
    private String currentModel;
    private String currentLocalAddress;
    private int currentLocalPort = -1;
    private String currentWanAddress;
    private int currentWanPort = -1;
    private ArrayList<String> ssids = new ArrayList<>();

    private BroadcastReceiver broadcastReceiver;

    private FloatingActionButton addSsidFAB;
    private RecyclerView recyclerViewSsids;
    private SsidRecyclerViewAdapter ssidRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_gateway_page_2, container, false);

        recyclerViewSsids = (RecyclerView) rootView.findViewById(R.id.recyclerView_ssids);
        ssidRecyclerViewAdapter = new SsidRecyclerViewAdapter(getActivity(), ssids);
        recyclerViewSsids.setAdapter(ssidRecyclerViewAdapter);
        ssidRecyclerViewAdapter.setOnDeleteClickListener(new SsidRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, final int position) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.delete)
                        .setMessage(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    ssids.remove(position);
                                    ssidRecyclerViewAdapter.notifyDataSetChanged();
                                    notifyConfigurationChanged();
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getContentView(), e);
                                }
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();
            }
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewSsids.setLayoutManager(layoutManager);

        addSsidFAB = (FloatingActionButton) rootView.findViewById(R.id.add_ssid_fab);
        addSsidFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        final Fragment fragment = this;
        addSsidFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddSsidDialog addSsidDialog = new AddSsidDialog();
                addSsidDialog.setTargetFragment(fragment, 0);
                addSsidDialog.show(getFragmentManager(), null);
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocalBroadcastConstants.INTENT_GATEWAY_SSID_ADDED.equals(intent.getAction())) {
                    ArrayList<String> newSsids = intent.getStringArrayListExtra(AddSsidDialog.KEY_SSID);
                    ssids.addAll(newSsids);
                    ssidRecyclerViewAdapter.notifyDataSetChanged();
                }

                if (LocalBroadcastConstants.INTENT_GATEWAY_SETUP_CHANGED.equals(intent.getAction())) {
                    currentName = intent.getStringExtra("name");
                    currentModel = intent.getStringExtra("model");
                    currentLocalAddress = intent.getStringExtra("localAddress");
                    currentLocalPort = intent.getIntExtra("localPort", -1);
                    currentWanAddress = intent.getStringExtra("wanAddress");
                    currentWanPort = intent.getIntExtra("wanPort", -1);
                }

                notifyConfigurationChanged();
            }
        };

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureGatewayDialog.GATEWAY_ID_KEY)) {
            gatewayId = args.getLong(ConfigureGatewayDialog.GATEWAY_ID_KEY);
            initializeGatewayData(gatewayId);
        }

        return rootView;
    }

    /**
     * Loads existing gateway data into fields
     *
     * @param gatewayId ID of existing Gateway
     */
    private void initializeGatewayData(long gatewayId) {
        try {
            Gateway gateway = DatabaseHandler.getGateway(gatewayId);

            currentName = gateway.getName();
            currentModel = gateway.getModel();
            currentLocalAddress = gateway.getLocalHost();
            currentLocalPort = gateway.getLocalPort();
            currentWanAddress = gateway.getWanHost();
            currentWanPort = gateway.getWanPort();

            ssids.clear();
            ssids.addAll(gateway.getSsids());
            ssidRecyclerViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    @Override
    public boolean checkSetupValidity() {

        if (TextUtils.isEmpty(currentName)) {
            return false;
        }

        if (TextUtils.isEmpty(currentModel)) {
            return false;
        }

        // as long as one of the address fields is filled in its ok
        return !(TextUtils.isEmpty(currentLocalAddress) && TextUtils.isEmpty(currentWanAddress));

    }

    @Override
    public void saveCurrentConfigurationToDatabase() throws Exception {
        if (gatewayId == -1) {
            Gateway newGateway;

            switch (currentModel) {
                case BrematicGWY433.MODEL:
                    newGateway = new BrematicGWY433((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(ssids));
                    break;
                case ConnAir.MODEL:
                    newGateway = new ConnAir((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(ssids));
                    break;
                case EZControl_XS1.MODEL:
                    newGateway = new EZControl_XS1((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(ssids));
                    break;
                case ITGW433.MODEL:
                    newGateway = new ITGW433((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(ssids));
                    break;
                case RaspyRFM.MODEL:
                    newGateway = new RaspyRFM((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(ssids));
                    break;
                default:
                    throw new GatewayUnknownException();
            }

            try {
                DatabaseHandler.addGateway(newGateway);
            } catch (GatewayAlreadyExistsException e) {
                StatusMessageHandler.showInfoMessage(rootView.getContext(),
                        R.string.gateway_already_exists, Snackbar.LENGTH_LONG);
            }
        } else {
            DatabaseHandler.updateGateway(gatewayId, currentName, currentModel, currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(ssids));
        }

        GatewaySettingsFragment.sendGatewaysChangedBroadcast(getActivity());
        StatusMessageHandler.showInfoMessage(getTargetFragment(),
                R.string.gateway_saved, Snackbar.LENGTH_LONG);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GATEWAY_SSID_ADDED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GATEWAY_SETUP_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

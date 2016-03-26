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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.AddSsidDialog;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureGatewayDialog;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * "Name" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureGatewayDialogPage2Fragment extends ConfigurationDialogFragment implements ConfigurationDialogTabbedSummaryFragment {

    private static ArrayList<String> ssidList = new ArrayList<>();
    private View rootView;
    private long gatewayId = -1;
    private ListView ssidListView;
    private ArrayAdapter<String> ssidAdapter;
    private FloatingActionButton addSsidFAB;

    private BroadcastReceiver broadcastReceiver;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_gateway_page_2, container, false);

        ssidListView = (ListView) rootView.findViewById(R.id.listView);
        ssidAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, ssidList);
        ssidListView.setAdapter(ssidAdapter);

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
                ArrayList<String> ssids = intent.getStringArrayListExtra(AddSsidDialog.KEY_SSID);
                ssidList.addAll(ssids);
                ssidAdapter.notifyDataSetChanged();
            }
        };

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureGatewayDialog.GATEWAY_ID_KEY)) {
            gatewayId = args.getLong(ConfigureGatewayDialog.GATEWAY_ID_KEY);
            initializeGatewayData(gatewayId);
        }
        checkSetupValidity();

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

            ssidList.clear();
            ssidList.addAll(gateway.getSsids());
            ssidList.add("Hallo Welt");
            ssidAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    public boolean checkSetupValidity() {
        return false;
    }

    @Override
    public void saveCurrentConfigurationToDatabase() throws Exception {
//        String model = this.model.getSelectedItem().toString();
//        String name = getCurrentName();
//        String localAddress = getCurrentLocalAddress();
//        int localPort = DatabaseConstants.INVALID_GATEWAY_PORT;
//        if (getCurrentLocalPortText().length() > 0) {
//            localPort = Integer.parseInt(getCurrentLocalPortText());
//        }
//        String wanAddress = getCurrentWanAddress();
//        int wanPort = DatabaseConstants.INVALID_GATEWAY_PORT;
//        if (getCurrentWanPortText().length() > 0) {
//            wanPort = Integer.parseInt(getCurrentWanPortText());
//        }
//
//        if (gatewayId == -1) {
//            Gateway newGateway;
//
//            switch (model) {
//                case BrematicGWY433.MODEL:
//                    newGateway = new BrematicGWY433((long) -1, true, name, "", localAddress, localPort, wanAddress, wanPort);
//                    break;
//                case ConnAir.MODEL:
//                    newGateway = new ConnAir((long) -1, true, name, "", localAddress, localPort, wanAddress, wanPort);
//                    break;
//                case EZControl_XS1.MODEL:
//                    newGateway = new EZControl_XS1((long) -1, true, name, "", localAddress, localPort, wanAddress, wanPort);
//                    break;
//                case ITGW433.MODEL:
//                    newGateway = new ITGW433((long) -1, true, name, "", localAddress, localPort, wanAddress, wanPort);
//                    break;
//                case RaspyRFM.MODEL:
//                    newGateway = new RaspyRFM((long) -1, true, name, "", localAddress, localPort, wanAddress, wanPort);
//                    break;
//                default:
//                    throw new GatewayUnknownException();
//            }
//
//            try {
//                DatabaseHandler.addGateway(newGateway);
//            } catch (GatewayAlreadyExistsException e) {
//                StatusMessageHandler.showInfoMessage(rootView.getContext(),
//                        R.string.gateway_already_exists, Snackbar.LENGTH_LONG);
//            }
//        } else {
//            DatabaseHandler.updateGateway(gatewayId, name, model, localAddress, localPort, wanAddress, wanPort);
//        }
//
//        GatewaySettingsFragment.sendGatewaysChangedBroadcast(getActivity());
//        StatusMessageHandler.showInfoMessage(((RecyclerViewFragment) getTargetFragment()).getRecyclerView(), R.string.gateway_saved, Snackbar.LENGTH_LONG);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GATEWAY_SSID_ADDED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

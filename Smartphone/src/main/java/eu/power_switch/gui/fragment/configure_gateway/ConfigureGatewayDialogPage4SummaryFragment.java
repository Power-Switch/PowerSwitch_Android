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
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.AddSsidDialog;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureGatewayDialog;
import eu.power_switch.gui.fragment.settings.GatewaySettingsFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.EZControl_XS1;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.gateway.RaspyRFM;
import eu.power_switch.shared.constants.DatabaseConstants;
import eu.power_switch.shared.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.shared.exception.gateway.GatewayUnknownException;

import static eu.power_switch.shared.constants.LocalBroadcastConstants.INTENT_GATEWAY_APARTMENTS_CHANGED;
import static eu.power_switch.shared.constants.LocalBroadcastConstants.INTENT_GATEWAY_SETUP_CHANGED;
import static eu.power_switch.shared.constants.LocalBroadcastConstants.INTENT_GATEWAY_SSIDS_CHANGED;
import static eu.power_switch.shared.constants.LocalBroadcastConstants.INTENT_GATEWAY_SSID_ADDED;

/**
 * "Name" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureGatewayDialogPage4SummaryFragment extends ConfigurationDialogFragment implements ConfigurationDialogTabbedSummaryFragment {

    private View rootView;
    private long gatewayId = -1;

    private String currentName;
    private String currentModel;
    private String currentLocalAddress;
    private int currentLocalPort = -1;
    private String currentWanAddress;
    private int currentWanPort = -1;
    private ArrayList<String> currentSsids = new ArrayList<>();
    private ArrayList<String> currentApartmentNames = new ArrayList<>();

    private TextView name;
    private TextView model;
    private TextView localAddress;
    private TextView wanAddress;
    private TextView ssids;
    private TextView associatedApartments;

    private BroadcastReceiver broadcastReceiver;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_gateway_page_4_summary, container, false);

        name = (TextView) rootView.findViewById(R.id.textView_name);
        model = (TextView) rootView.findViewById(R.id.textView_model);
        localAddress = (TextView) rootView.findViewById(R.id.textView_localAddress);
        wanAddress = (TextView) rootView.findViewById(R.id.textView_wanAddress);
        ssids = (TextView) rootView.findViewById(R.id.textView_ssids);
        associatedApartments = (TextView) rootView.findViewById(R.id.textView_associatedApartments);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (INTENT_GATEWAY_SSIDS_CHANGED.equals(intent.getAction())) {
                    ArrayList<String> changedSsids = intent.getStringArrayListExtra(AddSsidDialog.KEY_SSID);
                    currentSsids.clear();
                    currentSsids.addAll(changedSsids);
                } else if (INTENT_GATEWAY_SETUP_CHANGED.equals(intent.getAction())) {
                    currentName = intent.getStringExtra(ConfigureGatewayDialogPage1Fragment.KEY_NAME);
                    currentModel = intent.getStringExtra(ConfigureGatewayDialogPage1Fragment.KEY_MODEL);
                    currentLocalAddress = intent.getStringExtra(ConfigureGatewayDialogPage1Fragment.KEY_LOCAL_ADDRESS);
                    currentLocalPort = intent.getIntExtra(ConfigureGatewayDialogPage1Fragment.KEY_LOCAL_PORT, -1);
                    currentWanAddress = intent.getStringExtra(ConfigureGatewayDialogPage1Fragment.KEY_WAN_ADDRESS);
                    currentWanPort = intent.getIntExtra(ConfigureGatewayDialogPage1Fragment.KEY_WAN_PORT, -1);
                } else if (INTENT_GATEWAY_APARTMENTS_CHANGED.equals(intent.getAction())) {
                    currentApartmentNames.clear();
                    currentApartmentNames.addAll((ArrayList<String>) intent.getSerializableExtra(ConfigureGatewayDialogPage3Fragment.KEY_APARTMENT_NAMES));
                }

                updateUI();
                notifyConfigurationChanged();
            }
        };

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureGatewayDialog.GATEWAY_ID_KEY)) {
            gatewayId = args.getLong(ConfigureGatewayDialog.GATEWAY_ID_KEY);
            initializeGatewayData(gatewayId);
        }
        updateUI();

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

            List<Apartment> associatedApartments = DatabaseHandler.getAssociatedApartments(gatewayId);
            for (Apartment associatedApartment : associatedApartments) {
                currentApartmentNames.add(associatedApartment.getName());
            }

            currentSsids.clear();
            currentSsids.addAll(gateway.getSsids());
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    private void updateUI() {
        name.setText(currentName);
        model.setText(currentModel);

        if (!TextUtils.isEmpty(currentLocalAddress)) {
            if (currentLocalPort != DatabaseConstants.INVALID_GATEWAY_PORT) {
                localAddress.setText(currentLocalAddress + ":" + currentLocalPort);
            } else {
                localAddress.setText(currentLocalAddress);
            }
        } else {
            localAddress.setText("");
        }

        if (!TextUtils.isEmpty(currentWanAddress)) {
            if (currentWanPort != DatabaseConstants.INVALID_GATEWAY_PORT) {
                wanAddress.setText(currentWanAddress + ":" + currentWanPort);
            } else {
                wanAddress.setText(currentWanAddress);
            }
        } else {
            wanAddress.setText("");
        }

        String ssidText = "";
        for (int i = 0, currentSsidsSize = currentSsids.size(); i < currentSsidsSize; i++) {
            String ssid = currentSsids.get(i);
            ssidText += ssid;

            if (i < currentSsidsSize - 1) {
                ssidText += "\n";
            }
        }
        ssids.setText(ssidText);

        String apartmentsText = "";
        for (int i = 0, currentApartmentNamesSize = currentApartmentNames.size(); i < currentApartmentNamesSize; i++) {
            String apartmentName = currentApartmentNames.get(i);
            apartmentsText += apartmentName;

            if (i < currentApartmentNamesSize - 1) {
                apartmentsText += "\n";
            }
        }
        associatedApartments.setText(apartmentsText);
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
                    newGateway = new BrematicGWY433((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(currentSsids));
                    break;
                case ConnAir.MODEL:
                    newGateway = new ConnAir((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(currentSsids));
                    break;
                case EZControl_XS1.MODEL:
                    newGateway = new EZControl_XS1((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(currentSsids));
                    break;
                case ITGW433.MODEL:
                    newGateway = new ITGW433((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(currentSsids));
                    break;
                case RaspyRFM.MODEL:
                    newGateway = new RaspyRFM((long) -1, true, currentName, "", currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(currentSsids));
                    break;
                default:
                    throw new GatewayUnknownException();
            }

            try {
                long id = DatabaseHandler.addGateway(newGateway);

                newGateway.setId(id);
                for (String apartmentName : currentApartmentNames) {
                    Apartment apartment = DatabaseHandler.getApartment(apartmentName);

                    List<Gateway> associatedGateways = apartment.getAssociatedGateways();
                    if (!apartment.isAssociatedWith(id)) {
                        associatedGateways.add(newGateway);
                    }
                    Apartment updatedApartment = new Apartment(apartment.getId(), apartment.isActive(), apartment.getName(), associatedGateways, apartment.getGeofence());
                    DatabaseHandler.updateApartment(updatedApartment);
                }

            } catch (GatewayAlreadyExistsException e) {
                StatusMessageHandler.showInfoMessage(rootView.getContext(),
                        R.string.gateway_already_exists, Snackbar.LENGTH_LONG);
            }
        } else {
            DatabaseHandler.updateGateway(gatewayId, currentName, currentModel, currentLocalAddress, currentLocalPort, currentWanAddress, currentWanPort, new HashSet<>(currentSsids));
            Gateway updatedGateway = DatabaseHandler.getGateway(gatewayId);

            List<Apartment> apartments = DatabaseHandler.getAllApartments();
            for (Apartment apartment : apartments) {
                if (apartment.isAssociatedWith(updatedGateway.getId())) {
                    if (!currentApartmentNames.contains(apartment.getName())) {
                        for (Gateway gateway : apartment.getAssociatedGateways()) {
                            if (gateway.getId().equals(updatedGateway.getId())) {
                                apartment.getAssociatedGateways().remove(gateway);
                                DatabaseHandler.updateApartment(apartment);
                                break;
                            }
                        }
                    }
                } else {
                    if (currentApartmentNames.contains(apartment.getName())) {
                        apartment.getAssociatedGateways().add(updatedGateway);
                        DatabaseHandler.updateApartment(apartment);
                    }
                }
            }
        }

        GatewaySettingsFragment.sendGatewaysChangedBroadcast(getActivity());
        StatusMessageHandler.showInfoMessage(getTargetFragment(),
                R.string.gateway_saved, Snackbar.LENGTH_LONG);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_GATEWAY_SSID_ADDED);
        intentFilter.addAction(INTENT_GATEWAY_SETUP_CHANGED);
        intentFilter.addAction(INTENT_GATEWAY_APARTMENTS_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

}

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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigureGatewayDialog;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.constants.DatabaseConstants;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * "Name" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureGatewayDialogPage1Fragment extends ConfigurationDialogFragment {

    private View rootView;
    private TextInputLayout floatingName;
    private EditText name;

    private TextInputLayout floatingLocalAddress;
    private EditText localAddress;

    private Spinner model;

    private TextInputLayout floatingLocalPort;
    private EditText localPort;

    private long gatewayId = -1;

    private String originalName;
    private String originalLocalAddress = "";
    private String originalLocalPort = "";
    private String originalWanAddress = "";
    private String originalWanPort = "";
    private TextInputLayout floatingWanAddress;
    private EditText wanAddress;
    private TextInputLayout floatingWanPort;
    private EditText wanPort;

    private void sendAddressChangedBroadcast(Context context) {
        String model = this.model.getSelectedItem().toString();
        String name = getCurrentName();
        String localAddress = getCurrentLocalAddress();
        int localPort = DatabaseConstants.INVALID_GATEWAY_PORT;
        if (getCurrentLocalPortText().length() > 0) {
            localPort = Integer.parseInt(getCurrentLocalPortText());
        }
        String wanAddress = getCurrentWanAddress();
        int wanPort = DatabaseConstants.INVALID_GATEWAY_PORT;
        if (getCurrentWanPortText().length() > 0) {
            wanPort = Integer.parseInt(getCurrentWanPortText());
        }

        Intent intent = new Intent(LocalBroadcastConstants.INTENT_GATEWAY_SETUP_CHANGED);
        intent.putExtra("name", name);
        intent.putExtra("model", model);
        intent.putExtra("localAddress", localAddress);
        intent.putExtra("localPort", localPort);
        intent.putExtra("wanAddress", wanAddress);
        intent.putExtra("wanPort", wanPort);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_gateway_page_1, container, false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkSetupValidity();
                sendAddressChangedBroadcast(getActivity());
            }
        };
        floatingName = (TextInputLayout) rootView.findViewById(R.id.gateway_name_text_input_layout);
        name = (EditText) rootView.findViewById(R.id.txt_edit_gateway_name);
        name.addTextChangedListener(textWatcher);

        model = (Spinner) rootView.findViewById(R.id.spinner_gateway_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gateway_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        model.setAdapter(adapter);
        SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                sendAddressChangedBroadcast(getActivity());
            }
        };
        model.setOnTouchListener(spinnerInteractionListener);
        model.setOnItemSelectedListener(spinnerInteractionListener);

        floatingLocalAddress = (TextInputLayout) rootView.findViewById(R.id.gateway_local_address_text_input_layout);
        localAddress = (EditText) rootView.findViewById(R.id.txt_edit_gateway_local_address);
        localAddress.addTextChangedListener(textWatcher);

        floatingLocalPort = (TextInputLayout) rootView.findViewById(R.id.gateway_local_port_text_input_layout);
        localPort = (EditText) rootView.findViewById(R.id.txt_edit_gateway_local_port);
        localPort.addTextChangedListener(textWatcher);

        floatingWanAddress = (TextInputLayout) rootView.findViewById(R.id.gateway_wan_address_text_input_layout);
        wanAddress = (EditText) rootView.findViewById(R.id.txt_edit_gateway_wan_address);
        wanAddress.addTextChangedListener(textWatcher);

        floatingWanPort = (TextInputLayout) rootView.findViewById(R.id.gateway_wan_port_text_input_layout);
        wanPort = (EditText) rootView.findViewById(R.id.txt_edit_gateway_wan_port);
        wanPort.addTextChangedListener(textWatcher);

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

            originalName = gateway.getName();
            originalLocalAddress = gateway.getLocalHost();
            if (!DatabaseConstants.INVALID_GATEWAY_PORT.equals(gateway.getLocalPort())) {
                originalLocalPort = gateway.getLocalPort().toString();
            }
            originalWanAddress = gateway.getWanHost();
            if (!DatabaseConstants.INVALID_GATEWAY_PORT.equals(gateway.getWanPort())) {
                originalWanPort = gateway.getWanPort().toString();
            }

            name.setText(originalName);
            localAddress.setText(originalLocalAddress);
            localPort.setText(String.valueOf(originalLocalPort));
            wanAddress.setText(originalWanAddress);
            wanPort.setText(String.valueOf(originalWanPort));

            // restore spinner position
            for (int i = 0; i < model.getCount(); i++) {
                if (model.getItemAtPosition(i).equals(gateway.getModel())) {
                    model.setSelection(i, false);
                }
            }

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    /**
     * Checks if current name is valid
     *
     * @param name current name value
     * @return true if valid
     */
    private boolean checkNameValidity(String name) {
        if (name.length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            return false;
        } else {
            floatingName.setError(null);
            return true;
        }
    }

    /**
     * Checks if current local host address is valid
     *
     * @param address current local host address value
     * @return true if valid
     */
    private boolean checkLocalAddressValidity(String address) {
        if (address.length() <= 0) {
            floatingLocalAddress.setError(getString(R.string.please_enter_host));
            return false;
        } else {
//            try {
            // try to create a URL object, catch Exception to know it was not well formatted
//                URL testUrl = new URL(address);

            // if it works, everything is ok
            floatingLocalAddress.setError(null);
            return true;

//            } catch (MalformedURLException e) {
//                floatingLocalAddress.setError(getString(R.string.malformed_url));
//            }
        }
    }

    /**
     * Checks if current local Port is valid
     *
     * @param portText current local port value
     * @return true if valid
     */
    private boolean checkLocalPortValidity(String portText) {
        if (portText.length() <= 0) {
            floatingLocalPort.setError(null);
            return true;
        } else {
            try {
                // try to convert text to int
                int port = Integer.valueOf(portText);
                if (port > 65535 || port <= 0) {
                    floatingLocalPort.setError(getString(R.string.port_invalid));
                    return false;
                } else {
                    floatingLocalPort.setError(null);
                    return true;
                }
            } catch (Exception e) {
                floatingLocalPort.setError(getString(R.string.unknown_error));
                return false;
            }
        }
    }

    /**
     * Checks if current WAN Host address is valid
     *
     * @param address current wan address value
     * @return true if valid
     */
    private boolean checkWanAddressValidity(String address) {
        if (address.length() <= 0) {
            floatingWanAddress.setError(getString(R.string.please_enter_host));
            return false;
        } else {
//            try {
            // try to create a URL object, catch Exception to know it was not well formatted
//                URL testUrl = new URL(address);

            // if it works, everything is ok
            floatingWanAddress.setError(null);
            return true;

//            } catch (MalformedURLException e) {
//                floatingLocalAddress.setError(getString(R.string.malformed_url));
//            }
        }
    }

    /**
     * Checks if current WAN Port is valid
     *
     * @param portText current WAN port value
     * @return true if valid
     */
    private boolean checkWanPortValidity(String portText) {
        if (portText.length() <= 0) {
            floatingWanPort.setError(null);
            return true;
        } else {
            try {
                // try to convert text to int
                int port = Integer.valueOf(portText);
                if (port > 65535 || port <= 0) {
                    floatingWanPort.setError(getString(R.string.port_invalid));
                    return false;
                } else {
                    floatingWanPort.setError(null);
                    return true;
                }
            } catch (Exception e) {
                floatingWanPort.setError(getString(R.string.unknown_error));
                return false;
            }
        }
    }

    /**
     * Gets current name field value
     *
     * @return Name of Gateway
     */
    public String getCurrentName() {
        return name.getText().toString().trim();
    }

    /**
     * Gets current address field value
     *
     * @return Address of Gateway
     */
    public String getCurrentLocalAddress() {
        return localAddress.getText().toString().trim();
    }

    /**
     * Gets current localPort field value
     *
     * @return Port of Gateway (as String)
     */
    private String getCurrentLocalPortText() {
        return localPort.getText().toString().trim();
    }

    private String getCurrentWanAddress() {
        return wanAddress.getText().toString().trim();
    }

    private String getCurrentWanPortText() {
        return wanPort.getText().toString().trim();
    }

    public boolean checkSetupValidity() {
        boolean nameIsValid;
        boolean localAddressIsValid;
        boolean localPortIsValid;
        boolean wanAddressIsValid;
        boolean wanPortIsValid;

        String name = getCurrentName();
        String localAddress = getCurrentLocalAddress();
        String localPortText = getCurrentLocalPortText();
        String wanAddress = getCurrentWanAddress();
        String wanPortText = getCurrentWanPortText();

        nameIsValid = checkNameValidity(name);
        localAddressIsValid = checkLocalAddressValidity(localAddress);
        localPortIsValid = checkLocalPortValidity(localPortText);
        wanAddressIsValid = checkWanAddressValidity(wanAddress);
        wanPortIsValid = checkWanPortValidity(wanPortText);

        boolean oneAddressIsValid = ((localAddressIsValid && localPortIsValid) || (wanAddressIsValid && wanPortIsValid));

        // disable error messages if one address is valid
        if (oneAddressIsValid) {
            floatingLocalAddress.setError(null);
            floatingLocalPort.setError(null);
            floatingWanAddress.setError(null);
            floatingWanPort.setError(null);
        }

        return nameIsValid && oneAddressIsValid;
    }

}

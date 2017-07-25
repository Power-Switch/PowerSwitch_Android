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

package eu.power_switch.gui.fragment.configure_gateway;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.GatewayConfigurationHolder;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.constants.DatabaseConstants;

/**
 * First Fragment used in Configure Gateway Dialog to set basic gateway settings
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureGatewayDialogPage1 extends ConfigurationDialogPage<GatewayConfigurationHolder> {

    @BindView(R.id.gateway_name_text_input_layout)
    TextInputLayout floatingName;
    @BindView(R.id.txt_edit_gateway_name)
    EditText        name;

    @BindView(R.id.gateway_local_address_text_input_layout)
    TextInputLayout floatingLocalAddress;
    @BindView(R.id.txt_edit_gateway_local_address)
    EditText        localAddress;

    @BindView(R.id.spinner_gateway_type)
    Spinner model;

    @BindView(R.id.gateway_local_port_text_input_layout)
    TextInputLayout floatingLocalPort;
    @BindView(R.id.txt_edit_gateway_local_port)
    EditText        localPort;

    @BindView(R.id.gateway_wan_address_text_input_layout)
    TextInputLayout floatingWanAddress;
    @BindView(R.id.txt_edit_gateway_wan_address)
    EditText        wanAddress;
    @BindView(R.id.gateway_wan_port_text_input_layout)
    TextInputLayout floatingWanPort;
    @BindView(R.id.txt_edit_gateway_wan_port)
    EditText        wanPort;

    private String originalName;
    private String originalLocalAddress = "";
    private String originalLocalPort    = "";
    private String originalWanAddress   = "";
    private String originalWanPort      = "";

    @Override
    protected void onRootViewInflated(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.gateway_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        model.setAdapter(adapter);

        initializeGatewayData();

        checkSetupValidity();

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
                updateConfiguration();
            }
        };
        name.addTextChangedListener(textWatcher);

        SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateConfiguration();
            }
        };
        model.setOnTouchListener(spinnerInteractionListener);
        model.setOnItemSelectedListener(spinnerInteractionListener);

        localAddress.addTextChangedListener(textWatcher);
        localPort.addTextChangedListener(textWatcher);
        wanAddress.addTextChangedListener(textWatcher);
        wanPort.addTextChangedListener(textWatcher);
    }

    @Override
    protected void showTutorial() {

    }

    private void updateConfiguration() {
        String model = this.model.getSelectedItem()
                .toString();
        String name         = getCurrentName();
        String localAddress = getCurrentLocalAddress();
        int    localPort    = DatabaseConstants.INVALID_GATEWAY_PORT;
        if (getCurrentLocalPortText().length() > 0) {
            localPort = Integer.parseInt(getCurrentLocalPortText());
        }
        String wanAddress = getCurrentWanAddress();
        int    wanPort    = DatabaseConstants.INVALID_GATEWAY_PORT;
        if (getCurrentWanPortText().length() > 0) {
            wanPort = Integer.parseInt(getCurrentWanPortText());
        }

        getConfiguration().setName(name);
        getConfiguration().setModel(model);
        getConfiguration().setLocalAddress(localAddress);
        getConfiguration().setLocalPort(localPort);
        getConfiguration().setWanAddress(wanAddress);
        getConfiguration().setWanPort(wanPort);

        notifyConfigurationChanged();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_gateway_page_1;
    }

    /**
     * Loads existing gateway data into fields
     */
    private void initializeGatewayData() {
        Gateway gateway = getConfiguration().getGateway();
        if (gateway != null) {
            originalName = getConfiguration().getName();
            originalLocalAddress = getConfiguration().getLocalAddress();
            if (!DatabaseConstants.INVALID_GATEWAY_PORT.equals(getConfiguration().getLocalPort())) {
                originalLocalPort = getConfiguration().getLocalPort()
                        .toString();
            }
            originalWanAddress = getConfiguration().getWanAddress();
            if (!DatabaseConstants.INVALID_GATEWAY_PORT.equals(getConfiguration().getWanPort())) {
                originalWanPort = getConfiguration().getWanPort()
                        .toString();
            }

            name.setText(originalName);
            localAddress.setText(originalLocalAddress);
            localPort.setText(String.valueOf(originalLocalPort));
            wanAddress.setText(originalWanAddress);
            wanPort.setText(String.valueOf(originalWanPort));

            // restore spinner position
            for (int i = 0; i < model.getCount(); i++) {
                if (model.getItemAtPosition(i)
                        .equals(getConfiguration().getModel())) {
                    model.setSelection(i, false);
                    break;
                }
            }
        }
    }

    /**
     * Checks if current name is valid
     *
     * @param name current name value
     *
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
     *
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
     *
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
     *
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
     *
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
        return name.getText()
                .toString()
                .trim();
    }

    /**
     * Gets current address field value
     *
     * @return Address of Gateway
     */
    public String getCurrentLocalAddress() {
        return localAddress.getText()
                .toString()
                .trim();
    }

    /**
     * Gets current localPort field value
     *
     * @return Port of Gateway (as String)
     */
    private String getCurrentLocalPortText() {
        return localPort.getText()
                .toString()
                .trim();
    }

    private String getCurrentWanAddress() {
        return wanAddress.getText()
                .toString()
                .trim();
    }

    private String getCurrentWanPortText() {
        return wanPort.getText()
                .toString()
                .trim();
    }

    public boolean checkSetupValidity() {
        boolean nameIsValid;
        boolean localAddressIsValid;
        boolean localPortIsValid;
        boolean wanAddressIsValid;
        boolean wanPortIsValid;

        String name          = getCurrentName();
        String localAddress  = getCurrentLocalAddress();
        String localPortText = getCurrentLocalPortText();
        String wanAddress    = getCurrentWanAddress();
        String wanPortText   = getCurrentWanPortText();

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

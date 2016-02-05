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

package eu.power_switch.gui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.exception.gateway.GatewayUnknownException;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.settings.GatewaySettingsFragment;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.EZControl_XS1;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.gateway.RaspyRFM;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to edit a Gateway
 */
public class ConfigureGatewayDialog extends ConfigurationDialog {

    /**
     * ID of existing Gateway to Edit
     */
    public static final String GATEWAY_ID_KEY = "GatewayId";

    private View rootView;
    private TextInputLayout floatingName;
    private EditText name;

    private TextInputLayout floatingAddress;
    private EditText address;

    private Spinner model;

    private TextInputLayout floatingPort;
    private EditText port;

    private List<Gateway> existingGateways;

    private long gatewayId = -1;

    private String originalName;
    private String originalAddress;
    private int originalPort;

    public static ConfigureGatewayDialog newInstance(long gatewayId) {
        Bundle args = new Bundle();
        args.putLong(GATEWAY_ID_KEY, gatewayId);

        ConfigureGatewayDialog fragment = new ConfigureGatewayDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening " + getClass().getSimpleName() + "...");
        rootView = inflater.inflate(R.layout.dialog_configure_gateway_content, container);

        try {
            existingGateways = DatabaseHandler.getAllGateways();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                notifyConfigurationChanged();
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
        model.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                notifyConfigurationChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        floatingAddress = (TextInputLayout) rootView.findViewById(R.id.gateway_address_text_input_layout);
        address = (EditText) rootView.findViewById(R.id.txt_edit_gateway_address);
        address.addTextChangedListener(textWatcher);

        floatingPort = (TextInputLayout) rootView.findViewById(R.id.gateway_port_text_input_layout);
        port = (EditText) rootView.findViewById(R.id.txt_edit_gateway_port);
        port.addTextChangedListener(textWatcher);

        return rootView;
    }

    @Override
    protected void initExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(GATEWAY_ID_KEY)) {
            gatewayId = arguments.getLong(GATEWAY_ID_KEY);
            initializeGatewayData(gatewayId);
        } else {
            // hide if new gateway
            imageButtonDelete.setVisibility(View.GONE);

            setSaveButtonState(false);
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_gateway;
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
            originalAddress = gateway.getHost();
            originalPort = gateway.getPort();

            name.setText(originalName);
            address.setText(originalAddress);
            port.setText(String.valueOf(originalPort));

            // restore spinner position
            for (int i = 0; i < model.getCount(); i++) {
                if (model.getItemAtPosition(i).equals(gateway.getModel())) {
                    model.setSelection(i);
                }
            }

            setModified(false);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    protected boolean isValid() {
        boolean nameIsValid;
        boolean addressIsValid;
        boolean portIsValid;
        boolean gatewayAlreadyExists;

        String name = getCurrentName();
        String address = getCurrentAddress();
        String portText = getCurrentPortText();

        nameIsValid = checkNameValidity(name);
        addressIsValid = checkAddressValidity(address);
        portIsValid = checkPortValidity(portText);
        gatewayAlreadyExists = checkGatewayAlreadyExists();

        return nameIsValid && addressIsValid && portIsValid && !gatewayAlreadyExists;
    }

    /**
     * Checks if current configuration already exists in database
     *
     * @return true if a similar gateway already exists
     */
    private boolean checkGatewayAlreadyExists() {
        boolean gatewayAlreadyExists = false;
        for (Gateway gateway : existingGateways) {
            if (gateway.getId() != gatewayId) {
                if (gateway.getHost().equals(getCurrentAddress()) &&
                        getCurrentPortText().equals(String.valueOf(gateway.getPort()))) {
                    gatewayAlreadyExists = true;
                }
            }
        }

        if (gatewayAlreadyExists) {
            floatingName.setErrorEnabled(false);
            floatingAddress.setError(getString(R.string.gateway_already_exists));
            floatingAddress.setErrorEnabled(true);
            floatingPort.setError(getString(R.string.gateway_already_exists));
            floatingPort.setErrorEnabled(true);
        }

        return gatewayAlreadyExists;
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
            floatingName.setErrorEnabled(true);
            return false;
        } else {
            floatingName.setError(null);
            floatingName.setErrorEnabled(false);
            return true;
        }
    }

    /**
     * Checks if current host address is valid
     *
     * @param address current address value
     * @return true if valid
     */
    private boolean checkAddressValidity(String address) {
        if (address.length() <= 0) {
            floatingAddress.setError(getString(R.string.please_enter_host));
            floatingAddress.setErrorEnabled(true);
            return false;
        } else {
//            try {
            // try to create a URL object, catch Exception to know it was not well formatted
//                URL testUrl = new URL(address);

            // if it works, everything is ok
            floatingAddress.setError(null);
            floatingAddress.setErrorEnabled(false);
            return true;

//            } catch (MalformedURLException e) {
//                floatingAddress.setError(getString(R.string.malformed_url));
//                floatingAddress.setErrorEnabled(true);
//            }
        }
    }

    /**
     * Checks if current Port is valid
     *
     * @param portText current port value
     * @return true if valid
     */
    private boolean checkPortValidity(String portText) {
        if (portText.length() <= 0) {
            floatingPort.setError(null);
            floatingPort.setErrorEnabled(false);
            return true;
        } else {
            try {
                // try to convert text to int
                int port = Integer.valueOf(portText);
                if (port > 65535 || port <= 0) {
                    floatingPort.setError(getString(R.string.port_invalid));
                    floatingPort.setErrorEnabled(true);
                    return true;
                } else {
                    floatingPort.setError(null);
                    floatingPort.setErrorEnabled(false);
                    return true;
                }
            } catch (Exception e) {
                floatingPort.setError(getString(R.string.unknown_error));
                floatingPort.setErrorEnabled(true);
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
    public String getCurrentAddress() {
        return address.getText().toString().trim();
    }

    /**
     * Gets current port field value
     *
     * @return Port of Gateway (as String)
     */
    private String getCurrentPortText() {
        if (port.getText().toString().trim().length() == 0) {
            return "49880";
        } else {
            return port.getText().toString().trim();
        }
    }

    /**
     * Saves current configuration to database
     * Either updates an existing Gateway or creates a new one
     */
    @Override
    protected void saveCurrentConfigurationToDatabase() {
        try {
            if (gatewayId == -1) {
                String gatewayModel = model.getSelectedItem().toString();
                String gatewayName = getCurrentName();
                String gatewayAddress = getCurrentAddress();
                int gatewayPort = 49880;
                if (getCurrentPortText().length() != 0) {
                    gatewayPort = Integer.parseInt(getCurrentPortText());
                }

                Gateway newGateway;

                switch (gatewayModel) {
                    case BrematicGWY433.MODEL:
                        newGateway = new BrematicGWY433((long) -1, true, gatewayName, "", gatewayAddress, gatewayPort);
                        break;
                    case ConnAir.MODEL:
                        newGateway = new ConnAir((long) -1, true, gatewayName, "", gatewayAddress, gatewayPort);
                        break;
                    case EZControl_XS1.MODEL:
                        newGateway = new EZControl_XS1((long) -1, true, gatewayName, "", gatewayAddress, gatewayPort);
                        break;
                    case ITGW433.MODEL:
                        newGateway = new ITGW433((long) -1, true, gatewayName, "", gatewayAddress, gatewayPort);
                        break;
                    case RaspyRFM.MODEL:
                        newGateway = new RaspyRFM((long) -1, true, gatewayName, "", gatewayAddress, gatewayPort);
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
                int portInt = 49880;
                if (getCurrentPortText().length() > 0) {
                    portInt = Integer.valueOf(getCurrentPortText());
                }
                DatabaseHandler.updateGateway(gatewayId, getCurrentName(), model.getSelectedItem()
                        .toString(), getCurrentAddress(), portInt);
            }

            GatewaySettingsFragment.sendGatewaysChangedBroadcast(getActivity());
            StatusMessageHandler.showInfoMessage((RecyclerViewFragment) getTargetFragment(), R.string.gateway_saved, Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContext(), e);
        }
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                .gateway_will_be_gone_forever)
                .setPositiveButton
                        (android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DatabaseHandler.deleteGateway(gatewayId);
                                    GatewaySettingsFragment.sendGatewaysChangedBroadcast(getActivity());
                                    StatusMessageHandler.showInfoMessage((RecyclerViewFragment) getTargetFragment(),
                                            R.string.gateway_removed, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                                }

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
    }
}
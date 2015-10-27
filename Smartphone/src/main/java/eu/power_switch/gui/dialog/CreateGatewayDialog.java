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

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.gateway.UnknownGatewayException;
import eu.power_switch.gui.fragment.settings.GatewaySettingsFragment;
import eu.power_switch.log.Log;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;

/**
 * Dialog to create a new Gateway
 */
public class CreateGatewayDialog extends DialogFragment {

    private EditText name;
    private EditText address;
    private View rootView;
    private EditText port;
    private Spinner gatewaySpinner;
    private ImageButton imageButtonDelete;
    private ImageButton imageButtonCancel;
    private ImageButton imageButtonSave;
    private TextInputLayout floatingName;
    private TextInputLayout floatingAddress;
    private TextInputLayout floatingPort;
    private List<Gateway> existingGateways;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.dialog_add_edit_gateway, null);

        existingGateways = DatabaseHandler.getAllGateways();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidity();
            }
        };

        floatingName = (TextInputLayout) rootView.findViewById(R.id.gateway_name_text_input_layout);
        name = (EditText) rootView.findViewById(R.id.txt_edit_gateway_name);
        name.requestFocus();
        name.addTextChangedListener(textWatcher);

        floatingAddress = (TextInputLayout) rootView.findViewById(R.id
                .gateway_address_text_input_layout);
        address = (EditText) rootView.findViewById(R.id.txt_edit_gateway_address);
        address.addTextChangedListener(textWatcher);

        floatingPort = (TextInputLayout) rootView.findViewById(R.id.gateway_port_text_input_layout);
        port = (EditText) rootView.findViewById(R.id.txt_edit_gateway_port);

        gatewaySpinner = (Spinner) rootView.findViewById(R.id.spinner_gateway_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gateway_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        gatewaySpinner.setAdapter(adapter);


        imageButtonDelete = (ImageButton) rootView.findViewById(R.id.imageButton_delete);
        imageButtonDelete.setVisibility(View.GONE);

        imageButtonCancel = (ImageButton) rootView.findViewById(R.id.imageButton_cancel);
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        imageButtonSave = (ImageButton) rootView.findViewById(R.id.imageButton_save);
        imageButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gatewayModel = gatewaySpinner.getSelectedItem().toString();
                String gatewayName = name.getText().toString();
                String gatewayAddress = address.getText().toString();
                int gatewayPort = 49880;
                if (port.getText().toString().length() != 0) {
                    gatewayPort = Integer.parseInt(port.getText().toString());
                }

                Gateway newGateway;

                if (gatewayModel.equals(BrematicGWY433.MODEL)) {
                    newGateway = new BrematicGWY433(0, true, gatewayName, "", gatewayAddress, gatewayPort);
                } else if (gatewayModel.equals(ConnAir.MODEL)) {
                    newGateway = new ConnAir(0, true, gatewayName, "", gatewayAddress, gatewayPort);
                } else if (gatewayModel.equals(ITGW433.MODEL)) {
                    newGateway = new ITGW433(0, true, gatewayName, "", gatewayAddress, gatewayPort);
                } else {
                    throw new UnknownGatewayException();
                }

                try {
                    DatabaseHandler.addGateway(newGateway);
                } catch (Exception e) {
                    Log.e(e);
                    e.printStackTrace();
                    return;
                }

                GatewaySettingsFragment.sendGatewaysChangedBroadcast(getActivity());

                Snackbar.make(getTargetFragment().getView(), R.string.gateway_created, Snackbar.LENGTH_LONG)
                        .show();
                getDialog().dismiss();
            }
        });

        setSaveButtonState(false);

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setTitle(R.string.create_gateway);
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        return dialog;
    }

    private void checkValidity() {
        boolean nameIsValid;
        boolean addressIsValid;

        try {
            String name = this.name.getText().toString().trim();
            String address = this.address.getText().toString().trim();
            String portText = this.port.getText().toString();

            boolean gatewayAlreadyExists = false;
            for (Gateway gateway : existingGateways) {
                if (gateway.getHost().equals(address) && portText.equals(String.valueOf(gateway.getPort()))) {
                    gatewayAlreadyExists = true;
                }
            }

            if (gatewayAlreadyExists) {
                floatingName.setErrorEnabled(false);
                floatingAddress.setError(getString(R.string.gateway_already_exists));
                floatingAddress.setErrorEnabled(true);
                floatingPort.setError(getString(R.string.gateway_already_exists));
                floatingPort.setErrorEnabled(true);
                setSaveButtonState(false);
                return;
            }

            nameIsValid = checkNameValidity(name);
            addressIsValid = checkAddressValidity(address);
//            checkPortValidity(portText);


            setSaveButtonState(nameIsValid && addressIsValid);
        } catch (Exception e) {
            Log.e(e);
            setSaveButtonState(false);
        }
    }

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

    private boolean checkAddressValidity(String address) {
        if (address.length() <= 0) {
            floatingAddress.setError(getString(R.string.please_enter_name));
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

    private boolean checkPortValidity(String portText) {
        if (portText.length() <= 0) {
            floatingPort.setError(getString(R.string.please_enter_name));
            floatingPort.setErrorEnabled(true);
            return false;
        } else {
            try {
                int port = Integer.valueOf(portText);
                floatingPort.setError(null);
                floatingPort.setErrorEnabled(false);
                return true;
            } catch (Exception e) {
                floatingPort.setError(getString(R.string.unknown_error));
                floatingPort.setErrorEnabled(true);
                return false;
            }
        }
    }

    private void setSaveButtonState(boolean enabled) {
        if (enabled) {
            imageButtonSave.setColorFilter(getResources().getColor(eu.power_switch.shared.R.color
                    .active_green));
            imageButtonSave.setClickable(true);
        } else {
            imageButtonSave.setColorFilter(getResources().getColor(eu.power_switch.shared.R.color
                    .inactive_gray));
            imageButtonSave.setClickable(false);
        }
    }
}
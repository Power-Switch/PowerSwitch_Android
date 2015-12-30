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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to configure (create/edit) an Apartment
 * <p/>
 * Created by Markus on 27.12.2015.
 */
public class ConfigureApartmentDialog extends ConfigurationDialog {

    /**
     * ID of existing Apartment to Edit
     */
    public static final String APARTMENT_ID_KEY = "ApartmentId";

    private View rootView;
    private TextInputLayout floatingName;
    private EditText name;

    private long apartmentId = -1;

    private List<Apartment> existingApartments;
    private String originalName;
    private LinearLayout linearLayoutSelectableGateways;

    private Collection<CheckBox> gatewayCheckboxList = new ArrayList<>();

    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_configure_apartment_content, null);

        setDeleteAction(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.are_you_sure)
                        .setMessage(R.string.apartment_will_be_gone_forever)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Apartment apartment = DatabaseHandler.getApartment(apartmentId);
                                    if (SmartphonePreferencesHandler.getCurrentApartmentId()
                                            .equals(apartment.getId())) {
                                        DatabaseHandler.deleteApartment(apartmentId);
                                        // update current Apartment selection
                                        Apartment firstApartment = DatabaseHandler.getAllApartments().get(0);
                                        SmartphonePreferencesHandler.setCurrentApartmentId(firstApartment.getId());
                                    } else {
                                        DatabaseHandler.deleteApartment(apartmentId);
                                    }


                                    ApartmentFragment.sendApartmentChangedBroadcast(getActivity());
                                    StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                                            R.string.apartment_removed, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    Log.e(e);
                                    StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
                                }

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
            }
        });

        try {
            existingApartments = DatabaseHandler.getAllApartments();
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
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
                setModified(true);
                checkValidity();
            }
        };
        floatingName = (TextInputLayout) rootView.findViewById(R.id.apartment_name_text_input_layout);
        name = (EditText) rootView.findViewById(R.id.txt_edit_apartment_name);
        name.addTextChangedListener(textWatcher);

        linearLayoutSelectableGateways = (LinearLayout) rootView.findViewById(R.id.linearLayout_gateways);
        addGatewaysToLayout();

        return rootView;
    }

    /**
     * Generate Gateway items and add them to view
     */
    private void addGatewaysToLayout() {
        String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(inflaterString);

        try {
            List<Gateway> gateways = DatabaseHandler.getAllGateways();
            for (Gateway gateway : gateways) {
                LinearLayout gatewayLayout = (LinearLayout) inflater.inflate(R.layout.gateway_overview, null);
                // every inflated layout has to be added manually, attaching while inflating will only generate every
                // child one, but not for every gateway
                linearLayoutSelectableGateways.addView(gatewayLayout);

                final CheckBox checkBox = (CheckBox) gatewayLayout.findViewById(R.id.checkbox_use_gateway);
                checkBox.setTag(R.string.gateways, gateway);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setModified(true);
                        checkValidity();
                    }
                });
                gatewayCheckboxList.add(checkBox);

                gatewayLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkBox.setChecked(!checkBox.isChecked());
                    }
                });

                TextView gatewayName = (TextView) gatewayLayout.findViewById(R.id.textView_gatewayName);
                gatewayName.setText(gateway.getName());

                TextView gatewayType = (TextView) gatewayLayout.findViewById(R.id.textView_gatewayType);
                gatewayType.setText(gateway.getModel());

                TextView gatewayHost = (TextView) gatewayLayout.findViewById(R.id.textView_gatewayHost);
                gatewayHost.setText(String.format("%s:%d", gateway.getHost(), gateway.getPort()));

                TextView gatewayDisabled = (TextView) gatewayLayout.findViewById(R.id.textView_disabled);
                if (gateway.isActive()) {
                    gatewayDisabled.setVisibility(View.GONE);
                } else {
                    gatewayDisabled.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    /**
     * Get selected Gateways
     *
     * @return List of Gateways
     */
    private ArrayList<Gateway> getCheckedGateways() {
        ArrayList<Gateway> checkedGateways = new ArrayList<>();

        for (CheckBox checkBox : gatewayCheckboxList) {
            if (checkBox.isChecked()) {
                Gateway gateway = (Gateway) checkBox.getTag(R.string.gateways);
                checkedGateways.add(gateway);
            }
        }

        return checkedGateways;
    }

    @Override
    protected void initExistingData(Bundle args) {
        if (args != null && args.containsKey(APARTMENT_ID_KEY)) {
            apartmentId = args.getLong(APARTMENT_ID_KEY);
            initializeApartmentData(apartmentId);
        } else {
            // enable all gateways by default
            for (CheckBox checkBox : gatewayCheckboxList) {
                checkBox.setChecked(true);
            }

            // hide if new apartment
            imageButtonDelete.setVisibility(View.GONE);
            setSaveButtonState(false);
        }

        // Disable delete Button if only one apartment is left
        if (existingApartments.size() <= 1) {
            imageButtonDelete.setColorFilter(ContextCompat.getColor(getActivity(), R.color.inactive_gray));
            imageButtonDelete.setClickable(false);
        }

        setModified(false);
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_apartment;
    }

    /**
     * Loads existing apartment data into fields
     *
     * @param apartmentId ID of existing Apartment
     */
    private void initializeApartmentData(Long apartmentId) {
        try {
            Apartment apartment = DatabaseHandler.getApartment(apartmentId);
            originalName = apartment.getName();
            name.setText(originalName);

            for (Gateway gateway : apartment.getAssociatedGateways()) {
                for (CheckBox checkBox : gatewayCheckboxList) {
                    Gateway checkBoxGateway = (Gateway) checkBox.getTag(R.string.gateways);
                    if (gateway.getId().equals(checkBoxGateway.getId())) {
                        checkBox.setChecked(true);
                    }
                }
            }

        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    /**
     * Checks if current configuration is valid and updates views accordingly
     */
    private void checkValidity() {
        boolean nameIsValid;

        try {
            String name = getCurrentName();

            nameIsValid = checkNameValidity(name);

            setSaveButtonState(nameIsValid);
        } catch (Exception e) {
            Log.e(e);
            setSaveButtonState(false);
        }
    }

    /**
     * Checks if current name is valid
     *
     * @param name
     * @return true if valid
     */
    private boolean checkNameValidity(String name) {
        if (name.length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            floatingName.setErrorEnabled(true);
            return false;
        } else if (checkNameAlreadyExists()) {
            floatingName.setError(getString(R.string.apartment_already_exists));
            floatingName.setErrorEnabled(true);
            return false;
        } else {
            floatingName.setError(null);
            floatingName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean checkNameAlreadyExists() {
        for (Apartment apartment : existingApartments) {
            if (apartment.getName().equals(getCurrentName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets current name field value
     *
     * @return Name of Apartment
     */
    public String getCurrentName() {
        return this.name.getText().toString().trim();
    }

    /**
     * Saves current configuration to database
     * Either updates an existing Gateway or creates a new one
     */
    @Override
    protected void saveCurrentConfigurationToDatabase() {
        try {
            if (apartmentId == -1) {
                String apartmentName = getCurrentName();

                Apartment newApartment = new Apartment((long) -1, apartmentName, getCheckedGateways());

                try {
                    DatabaseHandler.addApartment(newApartment);
                } catch (Exception e) {
                    StatusMessageHandler.showStatusMessage(rootView.getContext(),
                            R.string.unknown_error, Snackbar.LENGTH_LONG);
                }
            } else {
                DatabaseHandler.updateApartment(apartmentId, getCurrentName(), getCheckedGateways());
            }

            ApartmentFragment.sendApartmentChangedBroadcast(getActivity());
            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(), R.string.apartment_saved,
                    Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(rootView.getContext(), R.string.unknown_error, Snackbar.LENGTH_LONG);
        }
    }
}

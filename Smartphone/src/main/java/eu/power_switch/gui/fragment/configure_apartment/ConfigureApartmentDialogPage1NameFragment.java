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

package eu.power_switch.gui.fragment.configure_apartment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
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
import java.util.List;
import java.util.Locale;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureApartmentDialog;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * "Name" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureApartmentDialogPage1NameFragment extends ConfigurationDialogFragment implements ConfigurationDialogTabbedSummaryFragment {

    private View rootView;

    private TextInputLayout floatingName;
    private EditText name;

    private List<Apartment> existingApartments;
    private String originalName;
    private LinearLayout linearLayoutSelectableGateways;

    private List<CheckBox> gatewayCheckboxList = new ArrayList<>();

    private long apartmentId = -1;
    private boolean isInitialized;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_apartment_page_1, container, false);

        try {
            existingApartments = DatabaseHandler.getAllApartments();
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
                if (isInitialized) {
                    checkSetupValidity();
                    notifyConfigurationChanged();
                }
            }
        };
        floatingName = (TextInputLayout) rootView.findViewById(R.id.apartment_name_text_input_layout);
        name = (EditText) rootView.findViewById(R.id.txt_edit_apartment_name);
        name.addTextChangedListener(textWatcher);

        linearLayoutSelectableGateways = (LinearLayout) rootView.findViewById(R.id.linearLayout_gateways);
        addGatewaysToLayout();

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureApartmentDialog.APARTMENT_ID_KEY)) {
            apartmentId = args.getLong(ConfigureApartmentDialog.APARTMENT_ID_KEY);
            initializeApartmentData(apartmentId);
        } else {
            // enable all gateways by default
            for (CheckBox checkBox : gatewayCheckboxList) {
                checkBox.setChecked(true);
            }
        }
        checkSetupValidity();

        isInitialized = true;

        return rootView;
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
            StatusMessageHandler.showErrorMessage(getActivity(), e);
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
            if (!apartment.getId().equals(apartmentId) && apartment.getName().equalsIgnoreCase(getCurrentName())) {
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
        try {
            return this.name.getText().toString().trim();
        } catch (Exception e) {
            return null;
        }
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
                @SuppressLint("InflateParams")
                LinearLayout gatewayLayout = (LinearLayout) inflater.inflate(R.layout.gateway_overview, null);
                // every inflated layout has to be added manually, attaching while inflating will only generate every
                // child one, but not for every gateway
                linearLayoutSelectableGateways.addView(gatewayLayout);

                final CheckBox checkBox = (CheckBox) gatewayLayout.findViewById(R.id.checkbox_use_gateway);
                checkBox.setTag(R.string.gateways, gateway);
                CheckBoxInteractionListener checkBoxInteractionListener = new CheckBoxInteractionListener() {
                    @Override
                    public void onCheckedChangedByUser(CompoundButton buttonView, boolean isChecked) {
                        notifyConfigurationChanged();
                    }
                };
                checkBox.setOnTouchListener(checkBoxInteractionListener);
                checkBox.setOnCheckedChangeListener(checkBoxInteractionListener);
                gatewayCheckboxList.add(checkBox);

                gatewayLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkBox.setChecked(!checkBox.isChecked());
                        notifyConfigurationChanged();
                    }
                });

                TextView gatewayName = (TextView) gatewayLayout.findViewById(R.id.textView_gatewayName);
                gatewayName.setText(gateway.getName());

                TextView gatewayType = (TextView) gatewayLayout.findViewById(R.id.textView_gatewayType);
                gatewayType.setText(gateway.getModel());

                TextView gatewayHost = (TextView) gatewayLayout.findViewById(R.id.textView_gatewayHost);
                gatewayHost.setText(String.format(Locale.getDefault(), "%s:%d", gateway.getLocalHost(), gateway.getLocalPort()));

                TextView gatewayDisabled = (TextView) gatewayLayout.findViewById(R.id.textView_disabled);
                if (gateway.isActive()) {
                    gatewayDisabled.setVisibility(View.GONE);
                } else {
                    gatewayDisabled.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
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
    public void saveCurrentConfigurationToDatabase() throws Exception {
        if (apartmentId == -1) {
            boolean isActive = DatabaseHandler.getAllApartmentNames().size() <= 0;
            Apartment newApartment = new Apartment((long) -1, isActive, getCurrentName(),
                    getCheckedGateways(), null);

            long newId = DatabaseHandler.addApartment(newApartment);
            // set new apartment as active if it is the first and only one
            if (isActive) {
                SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID, newId);
            }
        } else {
            Apartment apartment = DatabaseHandler.getApartment(apartmentId);
            if (apartment.getGeofence() != null) {
                apartment.getGeofence().setName(getCurrentName());
            }

            Apartment updatedApartment = new Apartment(apartmentId, apartment.isActive(), getCurrentName(), getCheckedGateways(), apartment.getGeofence());

            DatabaseHandler.updateApartment(updatedApartment);
        }

        ApartmentFragment.sendApartmentChangedBroadcast(getActivity());
        StatusMessageHandler.showInfoMessage(getTargetFragment()
                , R.string.apartment_saved, Snackbar.LENGTH_LONG);
    }

    @Override
    public boolean checkSetupValidity() {
        boolean nameIsValid;
        nameIsValid = checkNameValidity(getCurrentName());

        return nameIsValid;
    }
}

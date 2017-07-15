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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.GatewayConfigurationHolder;
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.Gateway;

/**
 * "Apartments" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureGatewayDialogPage3 extends ConfigurationDialogPage<GatewayConfigurationHolder> {

    @BindView(R.id.linearLayout_Apartments)
    LinearLayout linearLayoutSelectableApartments;

    private List<CheckBox> apartmentCheckboxList = new ArrayList<>();

    private boolean isInitialized = false;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        addApartmentsToLayout();

        initializeGatewayData();

        isInitialized = true;

        return rootView;
    }

    /**
     * Used to notify the setup page that some info has changed
     */
    public void updateConfiguration(List<Long> apartmentIds) {
        getConfiguration().setApartmentIds(apartmentIds);

        notifyConfigurationChanged();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_gateway_page_3;
    }

    /**
     * Loads existing gateway data into fields
     */
    private void initializeGatewayData() {
        Gateway gateway = getConfiguration().getGateway();
        if (gateway != null) {
            try {
                List<Apartment> associatedApartments = persistanceHandler.getAssociatedApartments(gateway.getId());

                for (CheckBox checkBox : apartmentCheckboxList) {
                    Apartment checkBoxApartment = (Apartment) checkBox.getTag(R.string.apartments);
                    for (Apartment associatedApartment : associatedApartments) {
                        if (checkBoxApartment.getId()
                                .equals(associatedApartment.getId())) {
                            checkBox.setChecked(true);
                        }
                    }
                }

            } catch (Exception e) {
                statusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    /**
     * Generate Apartment items and add them to view
     */
    private void addApartmentsToLayout() {
        String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater       = (LayoutInflater) getActivity().getSystemService(inflaterString);

        try {
            List<Apartment> apartments = persistanceHandler.getAllApartments();
            for (Apartment apartment : apartments) {
                @SuppressLint("InflateParams") LinearLayout apartmentLayout = (LinearLayout) inflater.inflate(R.layout.apartment_overview, null);
                // every inflated layout has to be added manually, attaching while inflating will only generate every
                // child once
                linearLayoutSelectableApartments.addView(apartmentLayout);

                final CheckBox checkBox = apartmentLayout.findViewById(R.id.checkbox_apartment);
                checkBox.setTag(R.string.apartments, apartment);
                CheckBoxInteractionListener checkBoxInteractionListener = new CheckBoxInteractionListener() {
                    @Override
                    public void onCheckedChangedByUser(CompoundButton buttonView, boolean isChecked) {
                        if (isInitialized) {
                            updateConfiguration(getCheckedApartmentIds());
                        }
                    }
                };
                checkBox.setOnTouchListener(checkBoxInteractionListener);
                checkBox.setOnCheckedChangeListener(checkBoxInteractionListener);
                apartmentCheckboxList.add(checkBox);

                apartmentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkBox.setChecked(!checkBox.isChecked());
                        updateConfiguration(getCheckedApartmentIds());
                    }
                });

                TextView apartmentName = apartmentLayout.findViewById(R.id.textView_apartmentName);
                apartmentName.setText(apartment.getName());
            }
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    private ArrayList<Long> getCheckedApartmentIds() {
        ArrayList<Long> checkedApartmentIds = new ArrayList<>();

        for (CheckBox checkBox : apartmentCheckboxList) {
            if (checkBox.isChecked()) {
                Apartment apartment = (Apartment) checkBox.getTag(R.string.apartments);
                checkedApartmentIds.add(apartment.getId());
            }
        }

        return checkedApartmentIds;
    }
}

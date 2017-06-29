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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigureGatewayDialog;
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * "Apartments" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureGatewayDialogPage3Fragment extends ConfigurationDialogFragment {

    public static final String KEY_APARTMENT_IDS = "apartment_ids";

    @BindView(R.id.linearLayout_Apartments)
    LinearLayout linearLayoutSelectableApartments;

    private long gatewayId = -1;

    private List<CheckBox> apartmentCheckboxList = new ArrayList<>();

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context any suitable context
     */
    public static void sendApartmentsChangedBroadcast(Context context, ArrayList<Long> apartmentIds) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_GATEWAY_APARTMENTS_CHANGED);
        intent.putExtra(KEY_APARTMENT_IDS, apartmentIds);

        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        addApartmentsToLayout();

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureGatewayDialog.GATEWAY_ID_KEY)) {
            gatewayId = args.getLong(ConfigureGatewayDialog.GATEWAY_ID_KEY);
            initializeGatewayData(gatewayId);
        }

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_gateway_page_3;
    }

    /**
     * Loads existing gateway data into fields
     *
     * @param gatewayId ID of existing Gateway
     */
    private void initializeGatewayData(long gatewayId) {
        try {
//            Gateway gateway = DatabaseHandler.getGateway(gatewayId);

            List<Apartment> associatedApartments = DatabaseHandler.getAssociatedApartments(gatewayId);

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
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    /**
     * Generate Apartment items and add them to view
     */
    private void addApartmentsToLayout() {
        String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater       = (LayoutInflater) getActivity().getSystemService(inflaterString);

        try {
            List<Apartment> apartments = DatabaseHandler.getAllApartments();
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
                        sendApartmentsChangedBroadcast(getActivity(), getCheckedApartmentIds());
                    }
                };
                checkBox.setOnTouchListener(checkBoxInteractionListener);
                checkBox.setOnCheckedChangeListener(checkBoxInteractionListener);
                apartmentCheckboxList.add(checkBox);

                apartmentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkBox.setChecked(!checkBox.isChecked());
                        sendApartmentsChangedBroadcast(getActivity(), getCheckedApartmentIds());
                    }
                });

                TextView apartmentName = apartmentLayout.findViewById(R.id.textView_apartmentName);
                apartmentName.setText(apartment.getName());
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
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

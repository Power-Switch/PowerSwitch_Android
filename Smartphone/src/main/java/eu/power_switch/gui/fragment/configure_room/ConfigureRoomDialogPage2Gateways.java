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

package eu.power_switch.gui.fragment.configure_room;

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
import java.util.Locale;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.RoomConfigurationHolder;
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import timber.log.Timber;

/**
 * Dialog to edit a Room
 */
public class ConfigureRoomDialogPage2Gateways extends ConfigurationDialogPage<RoomConfigurationHolder> {

    @BindView(R.id.checkbox_use_custom_gateway_selection)
    CheckBox     checkBoxUseCustomGatewaySelection;
    @BindView(R.id.apartmentGateways)
    LinearLayout apartmentGateways;
    @BindView(R.id.otherGateways)
    LinearLayout otherGateways;
    @BindView(R.id.linearLayoutOfApartmentGateways)
    LinearLayout linearLayoutOfApartmentGateways;
    @BindView(R.id.linearLayoutOfOtherGateways)
    LinearLayout linearLayoutOfOtherGateways;
    @BindView(R.id.textView_custom_selection_description)
    TextView     textViewCustomSelectionDescription;

    private Apartment apartment;
    private List<Gateway>  gateways            = new ArrayList<>();
    private List<CheckBox> gatewayCheckboxList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        CheckBoxInteractionListener checkBoxInteractionListener = new CheckBoxInteractionListener() {
            @Override
            public void onCheckedChangedByUser(CompoundButton buttonView, boolean isChecked) {
                updateCustomGatewaySelectionVisibility();

                notifyConfigurationChanged();
            }
        };
        checkBoxUseCustomGatewaySelection.setOnCheckedChangeListener(checkBoxInteractionListener);
        checkBoxUseCustomGatewaySelection.setOnTouchListener(checkBoxInteractionListener);

        try {
            apartment = DatabaseHandler.getApartment(SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID));
            gateways = DatabaseHandler.getAllGateways();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }

        updateGatewayViews();

        initExistingData();

        updateCustomGatewaySelectionVisibility();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_room_page_2;
    }

    private void initExistingData() {
        Room room = getConfiguration().getRoom();

        if (room != null) {
            try {
                if (!getConfiguration().getAssociatedGateways()
                        .isEmpty()) {
                    checkBoxUseCustomGatewaySelection.setChecked(true);
                }

                for (Gateway gateway : getConfiguration().getAssociatedGateways()) {
                    for (CheckBox checkBox : gatewayCheckboxList) {
                        Gateway checkBoxGateway = (Gateway) checkBox.getTag(R.string.gateways);
                        if (gateway.getId()
                                .equals(checkBoxGateway.getId())) {
                            checkBox.setChecked(true);
                        }
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private void updateGatewayViews() {
        try {
            List<Gateway> previouslyCheckedGateways = new ArrayList<>();
            previouslyCheckedGateways.addAll(getCheckedGateways());

            gateways = DatabaseHandler.getAllGateways();

            String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater inflater       = (LayoutInflater) getActivity().getSystemService(inflaterString);

            // clear previous items
            linearLayoutOfApartmentGateways.removeAllViews();
            linearLayoutOfOtherGateways.removeAllViews();
            gatewayCheckboxList.clear();
            for (Gateway gateway : gateways) {
                LinearLayout gatewayLayout;
                if (apartment != null && apartment.isAssociatedWith(gateway.getId())) {
                    gatewayLayout = (LinearLayout) inflater.inflate(R.layout.gateway_overview, linearLayoutOfApartmentGateways, false);
                    linearLayoutOfApartmentGateways.addView(gatewayLayout);
                } else {
                    gatewayLayout = (LinearLayout) inflater.inflate(R.layout.gateway_overview, linearLayoutOfOtherGateways, false);
                    linearLayoutOfOtherGateways.addView(gatewayLayout);
                }

                final CheckBox checkBox = gatewayLayout.findViewById(R.id.checkbox_use_gateway);
                checkBox.setTag(R.string.gateways, gateway);
                CheckBoxInteractionListener checkBoxInteractionListener = new CheckBoxInteractionListener() {
                    @Override
                    public void onCheckedChangedByUser(CompoundButton buttonView, boolean isChecked) {
                        getConfiguration().setAssociatedGateways(getCheckedGateways());

                        notifyConfigurationChanged();
                    }
                };
                checkBox.setOnTouchListener(checkBoxInteractionListener);
                checkBox.setOnCheckedChangeListener(checkBoxInteractionListener);
                if (!previouslyCheckedGateways.isEmpty()) {
                    for (Gateway previousGateway : previouslyCheckedGateways) {
                        if (previousGateway.getId()
                                .equals(gateway.getId())) {
                            checkBox.setChecked(true);
                            break;
                        }
                    }
                }
                gatewayCheckboxList.add(checkBox);

                gatewayLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkBox.setChecked(!checkBox.isChecked());

                        updateCustomGatewaySelectionVisibility();

                        notifyConfigurationChanged();
                    }
                });

                TextView gatewayName = gatewayLayout.findViewById(R.id.textView_gatewayName);
                gatewayName.setText(gateway.getName());

                TextView gatewayType = gatewayLayout.findViewById(R.id.textView_gatewayType);
                gatewayType.setText(gateway.getModel());

                TextView gatewayHost = gatewayLayout.findViewById(R.id.textView_gatewayHost);
                gatewayHost.setText(String.format(Locale.getDefault(), "%s:%d", gateway.getLocalHost(), gateway.getLocalPort()));

                TextView gatewayDisabled = gatewayLayout.findViewById(R.id.textView_disabled);
                if (gateway.isActive()) {
                    gatewayDisabled.setVisibility(View.GONE);
                } else {
                    gatewayDisabled.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    private void updateCustomGatewaySelectionVisibility() {
        if (checkBoxUseCustomGatewaySelection.isChecked()) {
            textViewCustomSelectionDescription.setVisibility(View.GONE);

            // hide sections if empty
            if (linearLayoutOfApartmentGateways.getChildCount() == 0) {
                apartmentGateways.setVisibility(View.GONE);
            } else {
                apartmentGateways.setVisibility(View.VISIBLE);
            }
            if (linearLayoutOfOtherGateways.getChildCount() == 0) {
                otherGateways.setVisibility(View.GONE);
            } else {
                otherGateways.setVisibility(View.VISIBLE);
            }
        } else {
            textViewCustomSelectionDescription.setVisibility(View.VISIBLE);

            if (linearLayoutOfApartmentGateways.getChildCount() == 0) {
                apartmentGateways.setVisibility(View.GONE);
            } else {
                apartmentGateways.setVisibility(View.INVISIBLE);
            }
            if (linearLayoutOfOtherGateways.getChildCount() == 0) {
                otherGateways.setVisibility(View.GONE);
            } else {
                otherGateways.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Get selected Gateways
     *
     * @return List of Gateways
     */
    private ArrayList<Gateway> getCheckedGateways() {
        ArrayList<Gateway> checkedGateways = new ArrayList<>();

        if (!checkBoxUseCustomGatewaySelection.isChecked()) {
            return checkedGateways;
        }

        for (CheckBox checkBox : gatewayCheckboxList) {
            if (checkBox.isChecked()) {
                Gateway gateway = (Gateway) checkBox.getTag(R.string.gateways);
                checkedGateways.add(gateway);
            }
        }

        return checkedGateways;
    }

}
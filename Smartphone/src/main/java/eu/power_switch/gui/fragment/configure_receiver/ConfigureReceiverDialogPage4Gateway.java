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

package eu.power_switch.gui.fragment.configure_receiver;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.ReceiverConfigurationHolder;
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.event.ReceiverParentRoomChangedEvent;

/**
 * "Gateway"/"Network" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 24.04.2016.
 */
public class ConfigureReceiverDialogPage4Gateway extends ConfigurationDialogPage<ReceiverConfigurationHolder> {

    @BindView(R.id.textView_repeatAmount)
    TextView     textView_repetitionAmount;
    @BindView(R.id.button_plus)
    Button       buttonPlus;
    @BindView(R.id.button_minus)
    Button       buttonMinus;
    @BindView(R.id.linearLayoutOfApartmentGateways)
    LinearLayout linearLayoutOfApartmentGateways;
    @BindView(R.id.linearLayoutOfRoomGateways)
    LinearLayout linearLayoutOfRoomGateways;
    @BindView(R.id.linearLayoutOfOtherGateways)
    LinearLayout linearLayoutOfOtherGateways;
    @BindView(R.id.apartmentGateways)
    LinearLayout apartmentGateways;
    @BindView(R.id.roomGateways)
    LinearLayout roomGateways;
    @BindView(R.id.otherGateways)
    LinearLayout otherGateways;
    @BindView(R.id.checkbox_use_custom_gateway_selection)
    CheckBox     checkBoxUseCustomGatewaySelection;
    @BindView(R.id.textView_custom_selection_description)
    TextView     textViewCustomSelectionDescription;

    private int            repetitionAmount    = Receiver.MIN_REPETITIONS;
    private List<CheckBox> gatewayCheckboxList = new ArrayList<>();

    /**
     * Used to notify the summary page that some info has changed
     *
     * @param repeatAmount repeat amount
     * @param gateways     list of gateways
     */
    public void updateConfiguration(Integer repeatAmount, Set<Gateway> gateways) {
        getConfiguration().setRepetitionAmount(repeatAmount);
        getConfiguration().setGateways(new ArrayList<>(gateways));

        notifyConfigurationChanged();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        textView_repetitionAmount.setText(String.valueOf(repetitionAmount));

        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repetitionAmount++;
                buttonMinus.setVisibility(View.VISIBLE);

                if (repetitionAmount >= Receiver.MAX_REPETITIONS) {
                    repetitionAmount = Receiver.MAX_REPETITIONS;
                    buttonPlus.setVisibility(View.INVISIBLE);
                }

                textView_repetitionAmount.setText(String.valueOf(repetitionAmount));

                updateConfiguration(repetitionAmount, getCheckedGateways());
            }
        });

        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repetitionAmount--;
                buttonPlus.setVisibility(View.VISIBLE);

                if (repetitionAmount <= Receiver.MIN_REPETITIONS) {
                    repetitionAmount = Receiver.MIN_REPETITIONS;
                    buttonMinus.setVisibility(View.INVISIBLE);
                }

                textView_repetitionAmount.setText(String.valueOf(repetitionAmount));

                updateConfiguration(repetitionAmount, getCheckedGateways());
            }
        });

        CheckBoxInteractionListener checkBoxInteractionListener = new CheckBoxInteractionListener() {
            @Override
            public void onCheckedChangedByUser(CompoundButton buttonView, boolean isChecked) {
                updateCustomGatewaySelectionVisibility();

                updateConfiguration(repetitionAmount, getCheckedGateways());
            }
        };
        checkBoxUseCustomGatewaySelection.setOnCheckedChangeListener(checkBoxInteractionListener);
        checkBoxUseCustomGatewaySelection.setOnTouchListener(checkBoxInteractionListener);

        updateGatewayViews();

        initializeReceiverData();

        updateCustomGatewaySelectionVisibility();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_receiver_page_4;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onParentRoomChanged(ReceiverParentRoomChangedEvent parentRoomChangedEvent) {
        updateGatewayViews();
    }

    private void initializeReceiverData() {
        Long receiverId = getConfiguration().getId();
        if (receiverId != null) {
            try {
                repetitionAmount = getConfiguration().getRepetitionAmount();

                if (repetitionAmount >= Receiver.MAX_REPETITIONS) {
                    repetitionAmount = Receiver.MAX_REPETITIONS;
                    buttonPlus.setVisibility(View.INVISIBLE);
                    buttonMinus.setVisibility(View.VISIBLE);
                } else if (repetitionAmount <= Receiver.MIN_REPETITIONS) {
                    repetitionAmount = Receiver.MIN_REPETITIONS;
                    buttonPlus.setVisibility(View.VISIBLE);
                    buttonMinus.setVisibility(View.INVISIBLE);
                }
                textView_repetitionAmount.setText(String.valueOf(repetitionAmount));

                Receiver receiver = getConfiguration().getReceiver();
                if (!receiver.getAssociatedGateways()
                        .isEmpty()) {
                    checkBoxUseCustomGatewaySelection.setChecked(true);
                }

                for (Gateway gateway : receiver.getAssociatedGateways()) {
                    for (CheckBox checkBox : gatewayCheckboxList) {
                        Gateway checkBoxGateway = (Gateway) checkBox.getTag(R.string.gateways);
                        if (gateway.getId()
                                .equals(checkBoxGateway.getId())) {
                            checkBox.setChecked(true);
                        }
                    }
                }
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getContentView(), e);
            }
        } else {
            getConfiguration().setRepetitionAmount(repetitionAmount);
            getConfiguration().setGateways(new ArrayList<>(getCheckedGateways()));
        }
    }

    private void updateGatewayViews() {
        try {
            List<Gateway> previouslyCheckedGateways = new ArrayList<>();
            previouslyCheckedGateways.addAll(getCheckedGateways());

            String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater inflater       = (LayoutInflater) getActivity().getSystemService(inflaterString);

            // clear previous items
            linearLayoutOfApartmentGateways.removeAllViews();
            linearLayoutOfRoomGateways.removeAllViews();
            linearLayoutOfOtherGateways.removeAllViews();
            gatewayCheckboxList.clear();
            for (Gateway gateway : DatabaseHandler.getAllGateways()) {
                LinearLayout gatewayLayout;
                // room association is more important than apartment, so it is checked first!
                Apartment parentApartment = getConfiguration().getParentApartment();
                Room      parentRoom      = getConfiguration().getParentRoom();
                if (parentRoom != null && parentRoom.isAssociatedWith(gateway)) {
                    gatewayLayout = (LinearLayout) inflater.inflate(R.layout.gateway_overview, linearLayoutOfRoomGateways, false);
                    linearLayoutOfRoomGateways.addView(gatewayLayout);
                } else if (parentApartment != null && parentApartment.isAssociatedWith(gateway.getId())) {
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
                        updateConfiguration(repetitionAmount, getCheckedGateways());
                    }
                };
                checkBox.setOnTouchListener(checkBoxInteractionListener);
                checkBox.setOnCheckedChangeListener(checkBoxInteractionListener);
                for (Gateway previousGateway : previouslyCheckedGateways) {
                    if (previousGateway.getId()
                            .equals(gateway.getId())) {
                        checkBox.setChecked(true);
                        break;
                    }
                }
                gatewayCheckboxList.add(checkBox);

                gatewayLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkBox.setChecked(!checkBox.isChecked());

                        updateCustomGatewaySelectionVisibility();

                        updateConfiguration(repetitionAmount, getCheckedGateways());
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
            if (linearLayoutOfRoomGateways.getChildCount() == 0) {
                roomGateways.setVisibility(View.GONE);
            } else {
                roomGateways.setVisibility(View.VISIBLE);
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
            if (linearLayoutOfRoomGateways.getChildCount() == 0) {
                roomGateways.setVisibility(View.GONE);
            } else {
                roomGateways.setVisibility(View.INVISIBLE);
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
    private Set<Gateway> getCheckedGateways() {
        Set<Gateway> checkedGateways = new HashSet<>();

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

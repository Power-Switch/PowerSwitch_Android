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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.clipboard.ClipboardHelper;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.database.handler.ReceiverReflectionMagic;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.configuration.holder.ReceiverConfigurationHolder;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.UniversalReceiver;
import eu.power_switch.shared.event.ConfigurationChangedEvent;
import eu.power_switch.wear.service.UtilityService;

/**
 * "Summary" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage5TabbedSummary extends ConfigurationDialogPage<ReceiverConfigurationHolder> implements ConfigurationDialogTabbedSummaryFragment {

    @BindView(R.id.textView_name)
    TextView              name;
    @BindView(R.id.textView_roomName)
    TextView              roomName;
    @BindView(R.id.textView_brand)
    TextView              brand;
    @BindView(R.id.textView_model)
    TextView              model;
    @BindView(R.id.textView_channelMaster)
    TextView              channelMaster;
    @BindView(R.id.textView_channelSlave)
    TextView              channelSlave;
    @BindView(R.id.linearLayout_masterSlaveReceiver)
    LinearLayout          linearLayoutMasterSlaveReceiver;
    @BindView(R.id.linearLayout_dipReceiver)
    LinearLayout          linearLayoutDipReceiver;
    @BindView(R.id.linearLayout_universalReceiver)
    LinearLayout          linearLayoutUniversalReceiver;
    @BindView(R.id.linearLayout_dips)
    LinearLayout          linearLayoutDips;
    @BindView(R.id.linearLayout_universalButtons)
    LinearLayout          linearLayoutUniversalButtons;
    @BindView(R.id.linearLayout_autoPair)
    LinearLayout          linearLayoutAutoPairReceiver;
    @BindView(R.id.textView_seed)
    TextView              seedTextView;
    @BindView(R.id.button_copySeed)
    android.widget.Button buttonCopySeed;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        buttonCopySeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ClipboardHelper.copyToClipboard(getActivity(),
                            getString(R.string.seed),
                            seedTextView.getText()
                                    .toString());
                    StatusMessageHandler.showInfoMessage(getContentView(), R.string.copied_to_clipboard, Snackbar.LENGTH_LONG);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getContentView(), e);
                }
            }
        });

        updateUi();

        initializeReceiverData();

        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onConfigurationChanged(ConfigurationChangedEvent configurationChangedEvent) {
        updateUi();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_receiver_page_5_summary;
    }

    private void initializeReceiverData() {
        if (getConfiguration().getReceiver() == null) {
            return;
        }

        updateUi();
    }

    private void updateUi() {
        updateUiValues();
        updateUiVisibility();
    }

    private void updateUiValues() {
        name.setText(getConfiguration().getName());
        roomName.setText(getConfiguration().getParentRoomName());
        if (getConfiguration().getBrand() == null) {
            brand.setText("");
        } else {
            brand.setText(getConfiguration().getBrand()
                    .getName());
        }
        model.setText(getConfiguration().getModel());
        channelMaster.setText(String.valueOf(getConfiguration().getChannelMaster()));
        channelSlave.setText(String.valueOf(getConfiguration().getChannelSlave()));

        String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater       = (LayoutInflater) getActivity().getSystemService(inflaterString);

        List<DipSwitch> dips = getConfiguration().getDips();
        if (dips != null) {
            linearLayoutDips.removeAllViews();
            for (DipSwitch dipSwitch : dips) {
                @SuppressLint("InflateParams") SwitchCompat switchCompat = (SwitchCompat) inflater.inflate(R.layout.default_switch_compat,
                        null,
                        false);
                switchCompat.setText(dipSwitch.getName());
                switchCompat.setChecked(dipSwitch.isChecked());
                switchCompat.setClickable(false);

                linearLayoutDips.addView(switchCompat,
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        }

        if (getConfiguration().getSeed() == null) {
            seedTextView.setText("");
        } else {
            seedTextView.setText(String.valueOf(getConfiguration().getSeed()));
        }

        List<UniversalButton> universalButtons = getConfiguration().getUniversalButtons();
        if (universalButtons != null) {
            linearLayoutUniversalButtons.removeAllViews();
            for (Button button : universalButtons) {
                UniversalButton universalButton = (UniversalButton) button;

                LinearLayout      linearLayout = new LinearLayout(getActivity());
                AppCompatTextView textView     = new AppCompatTextView(getActivity());
                textView.setText("Name: " + universalButton.getName() + "\n" + "Signal: " + universalButton.getSignal());
                linearLayout.addView(textView);

                linearLayoutUniversalButtons.addView(linearLayout,
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    private void updateUiVisibility() {
        Receiver.Type type = getConfiguration().getType();
        if (type != null) {
            switch (type) {
                case DIPS:
                    linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
                    linearLayoutDipReceiver.setVisibility(View.VISIBLE);
                    linearLayoutAutoPairReceiver.setVisibility(View.GONE);
                    linearLayoutUniversalReceiver.setVisibility(View.GONE);
                    break;
                case MASTER_SLAVE:
                    linearLayoutMasterSlaveReceiver.setVisibility(View.VISIBLE);
                    linearLayoutDipReceiver.setVisibility(View.GONE);
                    linearLayoutAutoPairReceiver.setVisibility(View.GONE);
                    linearLayoutUniversalReceiver.setVisibility(View.GONE);
                    break;
                case UNIVERSAL:
                    linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
                    linearLayoutDipReceiver.setVisibility(View.GONE);
                    linearLayoutAutoPairReceiver.setVisibility(View.GONE);
                    linearLayoutUniversalReceiver.setVisibility(View.VISIBLE);
                    break;
                case AUTOPAIR:
                    linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
                    linearLayoutDipReceiver.setVisibility(View.GONE);
                    linearLayoutAutoPairReceiver.setVisibility(View.VISIBLE);
                    linearLayoutUniversalReceiver.setVisibility(View.GONE);
                    break;
            }
        } else {
            linearLayoutMasterSlaveReceiver.setVisibility(View.GONE);
            linearLayoutDipReceiver.setVisibility(View.GONE);
            linearLayoutAutoPairReceiver.setVisibility(View.GONE);
            linearLayoutUniversalReceiver.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean checkSetupValidity() {
        return true;
    }

    @Override
    public void saveCurrentConfigurationToDatabase() throws Exception {
        Apartment apartment    = getConfiguration().getParentApartment();
        Room      room         = apartment.getRoom(getConfiguration().getParentRoomName());
        String    receiverName = getConfiguration().getName();
        String    modelName    = getConfiguration().getModel();

        String        className = Receiver.receiverMap.get(modelName);
        Receiver.Type type      = ReceiverReflectionMagic.getType(className);

        Constructor<?> constructor = ReceiverReflectionMagic.getConstructor(className, type);

        long receiverId = -1;
        if (getConfiguration().getReceiver() != null) {
            receiverId = getConfiguration().getReceiver()
                    .getId();
        }

        Receiver receiver = null;
        switch (type) {
            case DIPS:
                LinkedList<Boolean> dipValues = new LinkedList<>();
                for (DipSwitch dipSwitch : getConfiguration().getDips()) {
                    dipValues.add(dipSwitch.isChecked());
                }

                receiver = (Receiver) constructor.newInstance(getActivity(), receiverId,
                        receiverName,
                        dipValues, room.getId(), getConfiguration().getGateways());
                break;
            case MASTER_SLAVE:
                receiver = (Receiver) constructor.newInstance(getActivity(),
                        receiverId,
                        receiverName,
                        getConfiguration().getChannelMaster(),
                        getConfiguration().getChannelSlave(),
                        room.getId(),
                        getConfiguration().getGateways());
                break;
            case UNIVERSAL:
                receiver = new UniversalReceiver(getActivity(), receiverId,
                        getConfiguration().getName(),
                        getConfiguration().getUniversalButtons(),
                        room.getId(),
                        getConfiguration().getGateways());
                break;
            case AUTOPAIR:
                receiver = (Receiver) constructor.newInstance(getActivity(),
                        receiverId,
                        receiverName,
                        getConfiguration().getSeed(),
                        room.getId(),
                        getConfiguration().getGateways());
                break;
        }

        receiver.setRepetitionAmount(getConfiguration().getRepetitionAmount());

        if (getConfiguration().getReceiver() == null) {
            DatabaseHandler.addReceiver(receiver);
        } else {
            DatabaseHandler.updateReceiver(receiver);
        }

        RoomsFragment.notifyReceiverChanged();
        // scenes could change too if room was used in a scene
        ScenesFragment.notifySceneChanged();

        // update wear data
        UtilityService.forceWearDataUpdate(getActivity());

        StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.receiver_saved, Snackbar.LENGTH_LONG);
    }

}

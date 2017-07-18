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

import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.clipboard.ClipboardHelper;
import eu.power_switch.event.ConfigurationChangedEvent;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.ReceiverConfigurationHolder;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.obj.receiver.Receiver;

/**
 * "Summary" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage5Summary extends ConfigurationDialogPage<ReceiverConfigurationHolder> {

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
                    statusMessageHandler.showInfoMessage(getContentView(), R.string.copied_to_clipboard, Snackbar.LENGTH_LONG);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getContentView(), e);
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

}

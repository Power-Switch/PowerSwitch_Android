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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import de.markusressel.android.library.tutorialtooltip.builder.IndicatorBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.MessageBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.TutorialTooltipBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.TutorialTooltipChainBuilder;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnIndicatorClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnMessageClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipIndicator;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipMessage;
import de.markusressel.android.library.tutorialtooltip.view.TooltipId;
import de.markusressel.android.library.tutorialtooltip.view.TutorialTooltipView;
import eu.power_switch.R;
import eu.power_switch.clipboard.ClipboardHelper;
import eu.power_switch.event.ReceiverBrandOrModelChangedEvent;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.ReceiverConfigurationHolder;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.UniversalReceiver;
import eu.power_switch.persistence.data.sqlite.handler.ReceiverReflectionMagic;
import eu.power_switch.shared.Brand;
import eu.power_switch.shared.exception.clipboard.EmptyClipboardException;
import timber.log.Timber;

/**
 * "Setup" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage3Setup extends ConfigurationDialogPage<ReceiverConfigurationHolder> {

    @BindView(R.id.listView_channelMaster)
    ListView channelMasterListView;
    @BindView(R.id.listView_channelSlave)
    ListView channelSlaveListView;

    @BindView(R.id.tableLayout_MasterSlave)
    TableLayout      layoutMasterSlave;
    @BindView(R.id.scrollView_dip)
    NestedScrollView layoutDip;
    @BindView(R.id.scrollView_autoPair)
    NestedScrollView layoutAutoPair;
    @BindView(R.id.linearLayout_universalButtons)
    LinearLayout     layoutUniversal;
    @BindView(R.id.universalButtons_List)
    LinearLayout     buttonsList;

    @BindView(R.id.editText_seed)
    TextInputEditText editTextSeed;
    @BindView(R.id.textInputEditText_seed)
    TextInputLayout   textInputEditTextSeed;

    @BindView(R.id.button_paste)
    android.widget.Button buttonPaste;
    @BindView(R.id.button_pair)
    android.widget.Button buttonPair;
    @BindView(R.id.button_unpair)
    android.widget.Button buttonUnpair;
    @BindView(R.id.button_unpairAll)
    android.widget.Button buttonUnpairAll;

    @BindView(R.id.add_universalButton_fab)
    FloatingActionButton addUniversalButtonFAB;

    @Inject
    NetworkHandler networkHandler;

    @Inject
    ReceiverReflectionMagic receiverReflectionMagic;

    private ArrayAdapter<String> channelMasterNamesAdapter;
    private ArrayAdapter<String> channelSlaveNamesAdapter;

    private ArrayList<SwitchCompat> dipViewList;
    private ArrayList<DipSwitch>    dipSwitchArrayList;

    private Receiver currentAutoPairReceiver;

    /**
     * Used to notify the summary page that some info has changed
     *
     * @param channelMaster    Current selected Master Channel
     * @param channelSlave     Current selected Slave Channel
     * @param dips             Current Dip configuration
     * @param universalButtons Current Universal Buttons
     */
    public void updateConfiguration(Character channelMaster, Integer channelSlave, ArrayList<DipSwitch> dips, Long seed,
                                    List<UniversalButton> universalButtons) {
        getConfiguration().setChannelMaster(channelMaster);
        getConfiguration().setChannelSlave(channelSlave);
        getConfiguration().setDips(dips);
        getConfiguration().setSeed(seed);
        getConfiguration().setUniversalButtons(universalButtons);

        notifyConfigurationChanged();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Master/Slave
        channelMasterNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice);
        channelMasterListView.setAdapter(channelMasterNamesAdapter);
        channelMasterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateConfiguration(getSelectedChannelMaster(), getSelectedChannelSlave(), null, getCurrentSeed(), null);
            }
        });

        channelSlaveNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice);
        channelSlaveListView.setAdapter(channelSlaveNamesAdapter);
        channelSlaveListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateConfiguration(getSelectedChannelMaster(), getSelectedChannelSlave(), null, getCurrentSeed(), null);
            }
        });

        // Dips
        dipViewList = new ArrayList<>();
        SwitchCompat dip0 = rootView.findViewById(R.id.switch_dip0);
        SwitchCompat dip1 = rootView.findViewById(R.id.switch_dip1);
        SwitchCompat dip2 = rootView.findViewById(R.id.switch_dip2);
        SwitchCompat dip3 = rootView.findViewById(R.id.switch_dip3);
        SwitchCompat dip4 = rootView.findViewById(R.id.switch_dip4);
        SwitchCompat dip5 = rootView.findViewById(R.id.switch_dip5);
        SwitchCompat dip6 = rootView.findViewById(R.id.switch_dip6);
        SwitchCompat dip7 = rootView.findViewById(R.id.switch_dip7);
        SwitchCompat dip8 = rootView.findViewById(R.id.switch_dip8);
        SwitchCompat dip9 = rootView.findViewById(R.id.switch_dip9);
        dipViewList.add(dip0);
        dipViewList.add(dip1);
        dipViewList.add(dip2);
        dipViewList.add(dip3);
        dipViewList.add(dip4);
        dipViewList.add(dip5);
        dipViewList.add(dip6);
        dipViewList.add(dip7);
        dipViewList.add(dip8);
        dipViewList.add(dip9);

        CompoundButton.OnCheckedChangeListener dipCheckedChangedListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ArrayList<DipSwitch> dips = new ArrayList<>();
                for (SwitchCompat switchCompat : dipViewList) {
                    if (switchCompat.getVisibility() == View.VISIBLE) {
                        dips.add(new DipSwitch(switchCompat.getText()
                                .toString(), switchCompat.isChecked()));
                    }
                }

                updateConfiguration(null, null, dips, getCurrentSeed(), null);
            }
        };

        for (SwitchCompat switchCompat : dipViewList) {
            switchCompat.setOnCheckedChangeListener(dipCheckedChangedListener);
        }

        // AutoPair
        editTextSeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    ((AutoPairReceiver) currentAutoPairReceiver).setSeed(Long.valueOf(editable.toString()));

                    updateConfiguration(getSelectedChannelMaster(),
                            getSelectedChannelSlave(),
                            dipSwitchArrayList,
                            getCurrentSeed(),
                            getCurrentUniversalButtons());

                    textInputEditTextSeed.setError(null);
                } catch (Exception e) {
                    Timber.e(e);

                    updateConfiguration(getSelectedChannelMaster(), getSelectedChannelSlave(), dipSwitchArrayList, null,
                            getCurrentUniversalButtons());

                    textInputEditTextSeed.setError(e.getMessage());
                }
            }
        });

        buttonPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String content   = ClipboardHelper.getClipboardContent(getActivity());
                    Long   longValue = Long.parseLong(content);
                    editTextSeed.setText(String.valueOf(longValue));
                    statusMessageHandler.showInfoMessage(getContentView(), R.string.pasted_from_clipboard, Snackbar.LENGTH_LONG);
                } catch (EmptyClipboardException e) {
                    Timber.w("Tried to paste but clipboard is empty");
                    statusMessageHandler.showInfoMessage(getContentView(), R.string.clipboard_is_empty, Snackbar.LENGTH_LONG);
                    // do nothing
                } catch (NumberFormatException e) {
                    Timber.w("Invalid number format: " + e.getMessage());
                    statusMessageHandler.showInfoMessage(getContentView(), R.string.invalid_format, Snackbar.LENGTH_LONG);
                }
            }
        });

        buttonPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Gateway> activeGateways = persistenceHandler.getAllGateways(true);

                    if (activeGateways.isEmpty()) {
                        statusMessageHandler.showNoActiveGatewayMessage(getActivity());
                        return;
                    }

                    ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
                    for (Gateway gateway : activeGateways) {
                        try {
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(networkHandler, gateway, getString(R.string.pair)));
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(networkHandler, gateway, getString(R.string.pair)));
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }

                    networkHandler.send(networkPackages);

                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getContentView(), e);
                }
            }
        });
        buttonUnpair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Gateway> activeGateways = persistenceHandler.getAllGateways(true);

                    if (activeGateways.isEmpty()) {
                        statusMessageHandler.showNoActiveGatewayMessage(getActivity());
                        return;
                    }

                    ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
                    for (Gateway gateway : activeGateways) {
                        try {
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(networkHandler, gateway, getString(R.string.unpair)));
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(networkHandler, gateway, getString(R.string.unpair)));
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }

                    networkHandler.send(networkPackages);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getContentView(), e);
                }
            }
        });
        buttonUnpairAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Gateway> activeGateways = persistenceHandler.getAllGateways(true);

                    if (activeGateways.isEmpty()) {
                        statusMessageHandler.showNoActiveGatewayMessage(getActivity());
                        return;
                    }

                    ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
                    for (Gateway gateway : activeGateways) {
                        try {
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(networkHandler, gateway, getString(R.string.unpair_all)));
                            networkPackages.add(currentAutoPairReceiver.getNetworkPackage(networkHandler, gateway, getString(R.string.unpair_all)));
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }

                    networkHandler.send(networkPackages);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getContentView(), e);
                }
            }
        });

        // Universal
        addUniversalButtonFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addUniversalButtonFAB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addUniversalButtonLayoutToDialogView();

                updateConfiguration(null, null, null, null, getCurrentUniversalButtons());
            }
        });

        updateUi(null);

        initializeReceiverData();

        createTutorial();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_receiver_page_3;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onBrandOrModelChanged(ReceiverBrandOrModelChangedEvent receiverBrandOrModelChangedEvent) {
        String model = receiverBrandOrModelChangedEvent.getModel();
        Brand  brand = receiverBrandOrModelChangedEvent.getBrand();

        try {
            Receiver receiver = receiverReflectionMagic.getDummy(Receiver.getJavaPath(model));

            getConfiguration().setBrand(receiverBrandOrModelChangedEvent.getBrand());
            getConfiguration().setType(receiver.getType());

            initType(receiver);

            updateConfiguration(getSelectedChannelMaster(),
                    getSelectedChannelSlave(),
                    dipSwitchArrayList,
                    getCurrentSeed(),
                    getCurrentUniversalButtons());
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    private void createTutorial() {
        OnMessageClickedListener onClickListener = new OnMessageClickedListener() {
            @Override
            public void onMessageClicked(TooltipId id, TutorialTooltipView tutorialTooltipView, TutorialTooltipMessage tutorialTooltipMessage,
                                         View view) {
                tutorialTooltipView.remove(true);
            }
        };

        OnIndicatorClickedListener onIndicatorClickedListener = new OnIndicatorClickedListener() {
            @Override
            public void onIndicatorClicked(TooltipId tooltipId, TutorialTooltipView tutorialTooltipView,
                                           TutorialTooltipIndicator tutorialTooltipIndicator, View view) {
                tutorialTooltipView.remove(true);
            }
        };

        TutorialTooltipBuilder message1 = new TutorialTooltipBuilder(getActivity()).attachToDialog(getParentConfigurationDialog().getDialog())
                .anchor(channelMasterListView, TutorialTooltipView.Gravity.CENTER)
                .indicator(new IndicatorBuilder().onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_receiver_master_select__text)
                        .gravity(TutorialTooltipView.Gravity.TOP)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_receiver_master_select__id)
                .build();

        TutorialTooltipBuilder message2 = new TutorialTooltipBuilder(getActivity()).attachToDialog(getParentConfigurationDialog().getDialog())
                .anchor(channelSlaveListView, TutorialTooltipView.Gravity.CENTER)
                .indicator(new IndicatorBuilder().onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_receiver_slave_select__text)
                        .gravity(TutorialTooltipView.Gravity.BOTTOM)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_receiver_slave_select__id)
                .build();

        new TutorialTooltipChainBuilder().addItem(message1)
                .addItem(message2)
                .execute();
    }

    private void initializeReceiverData() {
        Receiver receiver = getConfiguration().getReceiver();

        if (receiver != null) {
            try {
                initType(receiver);
            } catch (Exception e) {
                statusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    private void initData(final MasterSlaveReceiver receiver) {
        channelMasterNamesAdapter.clear();
        channelMasterNamesAdapter.addAll(receiver.getMasterNames());
        channelMasterNamesAdapter.notifyDataSetChanged();


        channelSlaveNamesAdapter.clear();
        channelSlaveNamesAdapter.addAll(receiver.getSlaveNames());
        channelSlaveNamesAdapter.notifyDataSetChanged();

        int channelMasterPosition = channelMasterNamesAdapter.getPosition(receiver.getMaster() + "");
        if (channelMasterPosition == -1) {
            channelMasterPosition = 0;
        }
        channelMasterListView.setItemChecked(channelMasterPosition, true);
        channelMasterListView.smoothScrollToPosition(channelMasterPosition);

        int channelSlavePosition = channelSlaveNamesAdapter.getPosition(receiver.getSlave() + "");
        if (channelSlavePosition == -1) {
            channelSlavePosition = 0;
        }
        channelSlaveListView.setItemChecked(channelSlavePosition, true);
        channelSlaveListView.smoothScrollToPosition(channelSlavePosition);
    }

    private void initData(DipReceiver receiver) {
        LinkedList<DipSwitch> dips = receiver.getDips();

        int i;
        for (i = 0; i < receiver.getDipNames()
                .size(); i++) {
            SwitchCompat currentSwitch = dipViewList.get(i);
            currentSwitch.setText(receiver.getDipNames()
                    .get(i));
            currentSwitch.setVisibility(View.VISIBLE);
            currentSwitch.setChecked(dips.get(i)
                    .isChecked());
        }
        while (i < 10) {
            SwitchCompat currentSwitch = dipViewList.get(i);
            currentSwitch.setVisibility(View.INVISIBLE);
            i++;
        }

        dipSwitchArrayList = new ArrayList<>();
        for (SwitchCompat switchCompat : dipViewList) {
            if (switchCompat.getVisibility() == View.VISIBLE) {
                dipSwitchArrayList.add(new DipSwitch(switchCompat.getText()
                        .toString(), switchCompat.isChecked()));
            }
        }
    }

    private void initData(UniversalReceiver receiver) {
        for (Button button : receiver.getButtons()) {
            UniversalButton universalButton = (UniversalButton) button;
            addUniversalButtonLayoutToDialogView(universalButton.getName(), universalButton.getSignal());
        }
    }

    private void initType(Receiver receiver) {
        switch (receiver.getType()) {
            case DIPS:
                updateUi(receiver);
                initData((DipReceiver) receiver);
                break;
            case MASTER_SLAVE:
                updateUi(receiver);
                initData((MasterSlaveReceiver) receiver);
                break;
            case UNIVERSAL:
                updateUi(receiver);
                initData((UniversalReceiver) receiver);
                break;
            case AUTOPAIR:
                currentAutoPairReceiver = receiver;
                updateUi(receiver);
                break;
        }
    }

    private void updateUi(Receiver receiver) {
        if (receiver == null) {
            layoutMasterSlave.setVisibility(View.GONE);
            layoutDip.setVisibility(View.GONE);
            layoutAutoPair.setVisibility(View.GONE);
            layoutUniversal.setVisibility(View.GONE);
        } else {
            switch (receiver.getType()) {
                case DIPS:
                    layoutMasterSlave.setVisibility(View.GONE);
                    layoutDip.setVisibility(View.VISIBLE);
                    layoutAutoPair.setVisibility(View.GONE);
                    layoutUniversal.setVisibility(View.GONE);
                    break;
                case MASTER_SLAVE:
                    layoutMasterSlave.setVisibility(View.VISIBLE);
                    layoutDip.setVisibility(View.GONE);
                    layoutAutoPair.setVisibility(View.GONE);
                    layoutUniversal.setVisibility(View.GONE);
                    break;
                case UNIVERSAL:
                    layoutMasterSlave.setVisibility(View.GONE);
                    layoutDip.setVisibility(View.GONE);
                    layoutAutoPair.setVisibility(View.GONE);
                    layoutUniversal.setVisibility(View.VISIBLE);
                    break;
                case AUTOPAIR:
                    layoutMasterSlave.setVisibility(View.GONE);
                    layoutDip.setVisibility(View.GONE);
                    layoutAutoPair.setVisibility(View.VISIBLE);
                    layoutUniversal.setVisibility(View.GONE);

                    editTextSeed.setText(String.valueOf(getCurrentSeed()));
                    break;
            }
        }
    }

    private void addUniversalButtonLayoutToDialogView() {
        addUniversalButtonLayoutToDialogView("", "");
    }

    private void addUniversalButtonLayoutToDialogView(String name, String signal) {
        TextWatcher tw = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateConfiguration(null, null, null, null, getCurrentUniversalButtons());
            }
        };
        LinearLayout newUniversalButtonLayout = new LinearLayout(getActivity());
        newUniversalButtonLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout nameLayout = new LinearLayout(getActivity());
        nameLayout.setOrientation(LinearLayout.HORIZONTAL);

        AppCompatEditText universalButtonNameEditText = new AppCompatEditText(getActivity());
        universalButtonNameEditText.setHint(R.string.name);
        universalButtonNameEditText.setText(name);
        universalButtonNameEditText.addTextChangedListener(tw);

        ImageButton deleteUniversalButton = new ImageButton(getActivity());
        deleteUniversalButton.setBackgroundResource(android.R.color.transparent);
        deleteUniversalButton.setImageResource(android.R.drawable.ic_menu_delete);
        deleteUniversalButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                buttonsList.removeView((View) v.getParent()
                        .getParent());
                updateConfiguration(null, null, null, null, getCurrentUniversalButtons());
            }
        });

        nameLayout.addView(universalButtonNameEditText,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
        nameLayout.addView(deleteUniversalButton);

        AppCompatEditText universalButtonSignalEditText = new AppCompatEditText(getActivity());
        universalButtonSignalEditText.setHint(R.string.enter_network_signal);
        universalButtonSignalEditText.setText(signal);
        universalButtonSignalEditText.addTextChangedListener(tw);

        newUniversalButtonLayout.addView(nameLayout);
        newUniversalButtonLayout.addView(universalButtonSignalEditText);

        buttonsList.addView(newUniversalButtonLayout);
    }

    private Character getSelectedChannelMaster() {
        try {
            if (channelMasterListView.getCheckedItemPosition() > channelMasterListView.getCount() || channelMasterListView.getCheckedItemPosition() == -1) {
                channelMasterListView.setItemChecked(0, true);
            }
            return channelMasterNamesAdapter.getItem(channelMasterListView.getCheckedItemPosition())
                    .charAt(0);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getSelectedChannelSlave() {
        try {
            if (channelSlaveListView.getCheckedItemPosition() > channelSlaveListView.getCount() || channelSlaveListView.getCheckedItemPosition() == -1) {
                channelSlaveListView.setItemChecked(0, true);
            }

            return Integer.valueOf(channelSlaveNamesAdapter.getItem(channelSlaveListView.getCheckedItemPosition()));
        } catch (Exception e) {
            return null;
        }
    }

    private Long getCurrentSeed() {
        try {
            return ((AutoPairReceiver) currentAutoPairReceiver).getSeed();
        } catch (Exception e) {
            return null;
        }
    }

    private List<UniversalButton> getCurrentUniversalButtons() {
        ArrayList<UniversalButton> buttons = new ArrayList<>();

        for (int i = 0; i < buttonsList.getChildCount(); i++) {
            LinearLayout universalButtonLayout = (LinearLayout) buttonsList.getChildAt(i);

            LinearLayout      nameLayout     = (LinearLayout) universalButtonLayout.getChildAt(0);
            AppCompatEditText nameEditText   = (AppCompatEditText) nameLayout.getChildAt(0);
            AppCompatEditText signalEditText = (AppCompatEditText) universalButtonLayout.getChildAt(1);

            buttons.add(new UniversalButton(null,
                    nameEditText.getText()
                            .toString(),
                    null,
                    signalEditText.getText()
                            .toString()));
        }

        return buttons;
    }

}

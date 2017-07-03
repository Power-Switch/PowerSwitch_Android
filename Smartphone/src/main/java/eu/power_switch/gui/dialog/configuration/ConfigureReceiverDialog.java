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

package eu.power_switch.gui.dialog.configuration;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ConfigurationDialogTabAdapter;
import eu.power_switch.gui.dialog.configuration.holder.ReceiverConfigurationHolder;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage1Name;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage2Type;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage3Setup;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage4Gateway;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage5TabbedSummary;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import timber.log.Timber;

/**
 * Dialog to create or modify a Receiver
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialog extends ConfigurationDialogTabbed<ReceiverConfigurationHolder> {

    public static ConfigureReceiverDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(-1, targetFragment);
    }

    public static ConfigureReceiverDialog newInstance(long receiverId, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureReceiverDialog     fragment                    = new ConfigureReceiverDialog();
        ReceiverConfigurationHolder receiverConfigurationHolder = new ReceiverConfigurationHolder();
        if (receiverId != -1) {
            receiverConfigurationHolder.setId(receiverId);
        }
        fragment.setConfiguration(receiverConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("Opening " + getClass().getSimpleName() + "...");
    }

    @Override
    protected boolean initializeFromExistingData(Bundle arguments) {
        Long receiverId = getConfiguration().getId();

        try {
            Apartment apartment = DatabaseHandler.getApartment(SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID));
            getConfiguration().setParentApartment(apartment);
        } catch (Exception e) {
            dismiss();
            StatusMessageHandler.showErrorMessage(getContext(), e);
        }

        if (receiverId != null) {
            // init dialog using existing receiver

            try {
                // TODO: optimize database access
                Receiver receiver = DatabaseHandler.getReceiver(receiverId);
                Room     room     = DatabaseHandler.getRoom(receiver.getRoomId());

                getConfiguration().setParentRoom(room);
                getConfiguration().setParentRoomName(room.getName());
                getConfiguration().setReceiver(receiver);
                getConfiguration().setName(receiver.getName());

                getConfiguration().setBrand(receiver.getBrand());
                getConfiguration().setModel(receiver.getModel());

                getConfiguration().setType(receiver.getType());
                switch (receiver.getType()) {
                    case DIPS:
                        getConfiguration().setDips(((DipReceiver) receiver).getDips());
                        break;
                    case MASTER_SLAVE:
                        getConfiguration().setChannelMaster(((MasterSlaveReceiver) receiver).getMaster());
                        getConfiguration().setChannelSlave(((MasterSlaveReceiver) receiver).getSlave());
                        break;
                    case AUTOPAIR:
                        getConfiguration().setSeed(((AutoPairReceiver) receiver).getSeed());
                        break;
                    case UNIVERSAL:
                        List<UniversalButton> universalButtons = new ArrayList<>(receiver.getButtons()
                                .size());
                        for (Button button : receiver.getButtons()) {
                            universalButtons.add((UniversalButton) button);
                        }

                        getConfiguration().setUniversalButtons(universalButtons);
                        break;
                }

                getConfiguration().setRepetitionAmount(receiver.getRepetitionAmount());
                getConfiguration().setGateways(receiver.getAssociatedGateways());

            } catch (Exception e) {
                dismiss();
                StatusMessageHandler.showErrorMessage(getContext(), e);
            }

            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment()));
            return true;
        } else {
            // Create the adapter that will return a fragment
            // for each of the two primary sections of the app.
            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment()));
            return false;
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_receiver;
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                .setMessage(R.string.receiver_will_be_gone_forever)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DatabaseHandler.deleteReceiver(getConfiguration().getId());

                            // notify rooms fragment
                            RoomsFragment.notifyReceiverChanged();
                            // scenes could change too if receiver was used in a scene
                            ScenesFragment.notifySceneChanged();

                            // update receiver widgets
                            ReceiverWidgetProvider.forceWidgetUpdate(getActivity());

                            // update wear data
                            UtilityService.forceWearDataUpdate(getActivity());

                            StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.receiver_deleted, Snackbar.LENGTH_LONG);
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                        }

                        // close dialog
                        getDialog().dismiss();
                    }
                })
                .setNeutralButton(android.R.string.cancel, null)
                .show();
    }

    private static class CustomTabAdapter extends ConfigurationDialogTabAdapter {

        private ConfigurationDialogTabbed<ReceiverConfigurationHolder> parentDialog;
        private ConfigurationDialogTabbedSummaryFragment               summaryFragment;
        private Fragment                                               targetFragment;

        public CustomTabAdapter(ConfigurationDialogTabbed<ReceiverConfigurationHolder> parentDialog, FragmentManager fm, Fragment targetFragment) {
            super(fm);
            this.parentDialog = parentDialog;
            this.targetFragment = targetFragment;
        }

        public ConfigurationDialogTabbedSummaryFragment getSummaryFragment() {
            return summaryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return parentDialog.getString(R.string.name);
                case 1:
                    return parentDialog.getString(R.string.type);
                case 2:
                    return parentDialog.getString(R.string.channel);
                case 3:
                    return parentDialog.getString(R.string.network);
                case 4:
                    return parentDialog.getString(R.string.summary);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureReceiverDialogPage1Name.class, parentDialog);
                    break;
                case 1:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureReceiverDialogPage2Type.class, parentDialog);
                    break;
                case 2:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureReceiverDialogPage3Setup.class, parentDialog);
                    break;
                case 3:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureReceiverDialogPage4Gateway.class, parentDialog);
                    break;
                case 4:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureReceiverDialogPage5TabbedSummary.class, parentDialog);
                    fragment.setTargetFragment(targetFragment, 0);

                    summaryFragment = (ConfigurationDialogTabbedSummaryFragment) fragment;
                    break;
                default:
                    break;
            }

            return fragment;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 5;
        }
    }

}
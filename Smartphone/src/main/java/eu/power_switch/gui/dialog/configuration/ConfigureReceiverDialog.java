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

package eu.power_switch.gui.dialog.configuration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.holder.ReceiverConfigurationHolder;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage1Name;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage2Type;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage3Setup;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage4Gateway;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage5Summary;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.UniversalReceiver;
import eu.power_switch.persistence.data.sqlite.handler.ReceiverReflectionMagic;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import timber.log.Timber;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID;

/**
 * Dialog to create or modify a Receiver
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialog extends ConfigurationDialog<ReceiverConfigurationHolder> {

    @Inject
    ReceiverReflectionMagic receiverReflectionMagic;

    public static ConfigureReceiverDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(null, targetFragment);
    }

    public static ConfigureReceiverDialog newInstance(Receiver receiver, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureReceiverDialog     fragment                    = new ConfigureReceiverDialog();
        ReceiverConfigurationHolder receiverConfigurationHolder = new ReceiverConfigurationHolder();
        if (receiver != null) {
            receiverConfigurationHolder.setReceiver(receiver);
        }
        fragment.setConfiguration(receiverConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) throws Exception {
        Receiver receiver = getConfiguration().getReceiver();

        long      apartmentId = smartphonePreferencesHandler.getValue(KEY_CURRENT_APARTMENT_ID);
        Apartment apartment   = persistenceHandler.getApartment(apartmentId);
        getConfiguration().setParentApartment(apartment);

        if (receiver != null) {
            // init dialog using existing receiver

            Room room = persistenceHandler.getRoom(receiver.getRoomId());

            getConfiguration().setParentRoom(room);
            getConfiguration().setParentRoomName(room.getName());
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
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_receiver;
    }

    @Override
    protected void addPageEntries(List<PageEntry<ReceiverConfigurationHolder>> pageEntries) {
        pageEntries.add(new PageEntry<>(R.string.name, ConfigureReceiverDialogPage1Name.class));
        pageEntries.add(new PageEntry<>(R.string.type, ConfigureReceiverDialogPage2Type.class));
        pageEntries.add(new PageEntry<>(R.string.channel, ConfigureReceiverDialogPage3Setup.class));
        pageEntries.add(new PageEntry<>(R.string.network, ConfigureReceiverDialogPage4Gateway.class));
        pageEntries.add(new PageEntry<>(R.string.summary, ConfigureReceiverDialogPage5Summary.class));
    }

    @Override
    protected void saveConfiguration() throws Exception {
        Timber.d("Saving Receiver...");
        Apartment apartment    = getConfiguration().getParentApartment();
        Room      room         = apartment.getRoom(getConfiguration().getParentRoomName());
        String    receiverName = getConfiguration().getName();
        String    modelName    = getConfiguration().getModel();

        String        className = Receiver.receiverMap.get(modelName);
        Receiver.Type type      = receiverReflectionMagic.getType(className);

        Constructor<?> constructor = receiverReflectionMagic.getConstructor(className, type);


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

                receiver = (Receiver) constructor.newInstance(getActivity(),
                        receiverId,
                        receiverName,
                        dipValues,
                        room.getId(),
                        getConfiguration().getGateways());
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
                receiver = new UniversalReceiver(getActivity(),
                        receiverId,
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

        receiver.setPositionInRoom(getConfiguration().getParentRoom()
                .getReceivers()
                .size() + 1);
        receiver.setRepetitionAmount(getConfiguration().getRepetitionAmount());

        if (receiverId == -1) {
            persistenceHandler.addReceiver(receiver);
        } else {
            persistenceHandler.updateReceiver(receiver);
        }

        RoomsFragment.notifyReceiverChanged();
        // scenes could change too if room was used in a scene
        ScenesFragment.notifySceneChanged();

        // update wear data
        UtilityService.forceWearDataUpdate(getActivity());
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        persistenceHandler.deleteReceiver(getConfiguration().getReceiver()
                .getId());

        // notify rooms fragment
        RoomsFragment.notifyReceiverChanged();
        // scenes could change too if receiver was used in a scene
        ScenesFragment.notifySceneChanged();

        // update receiver widgets
        ReceiverWidgetProvider.forceWidgetUpdate(getActivity());

        // update wear data
        UtilityService.forceWearDataUpdate(getActivity());
    }

}
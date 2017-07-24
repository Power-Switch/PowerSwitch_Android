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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.holder.GatewayConfigurationHolder;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage1;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage2;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage3;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage4Summary;
import eu.power_switch.gui.fragment.settings.GatewaySettingsFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.EZControl_XS1;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.gateway.RaspyRFM;
import eu.power_switch.shared.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.shared.exception.gateway.GatewayUnknownException;

/**
 * Dialog to edit a Gateway
 */
public class ConfigureGatewayDialog extends ConfigurationDialogTabbed<GatewayConfigurationHolder> {

    public static ConfigureGatewayDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(null, targetFragment);
    }

    public static ConfigureGatewayDialog newInstance(Gateway gateway, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureGatewayDialog     fragment                   = new ConfigureGatewayDialog();
        GatewayConfigurationHolder gatewayConfigurationHolder = new GatewayConfigurationHolder();
        if (gateway != null) {
            gatewayConfigurationHolder.setGateway(gateway);
        }
        fragment.setConfiguration(gatewayConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) throws Exception {
        Gateway gateway = getConfiguration().getGateway();

        if (gateway != null) {
            getConfiguration().setName(gateway.getName());
            getConfiguration().setModel(gateway.getModel());

            getConfiguration().setLocalAddress(gateway.getLocalHost());
            getConfiguration().setLocalPort(gateway.getLocalPort());
            getConfiguration().setWanAddress(gateway.getWanHost());
            getConfiguration().setWanPort(gateway.getWanPort());

            getConfiguration().setSsids(gateway.getSsids());

            List<Apartment> associatedApartments = persistenceHandler.getAssociatedApartments(gateway.getId());
            for (Apartment associatedApartment : associatedApartments) {
                getConfiguration().getApartmentIds()
                        .add(associatedApartment.getId());
            }
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_gateway;
    }

    @Override
    protected void addPageEntries(List<PageEntry<GatewayConfigurationHolder>> pageEntries) {
        pageEntries.add(new PageEntry<>(R.string.address, ConfigureGatewayDialogPage1.class));
        pageEntries.add(new PageEntry<>(R.string.ssids, ConfigureGatewayDialogPage2.class));
        pageEntries.add(new PageEntry<>(R.string.apartments, ConfigureGatewayDialogPage3.class));
        pageEntries.add(new PageEntry<>(R.string.summary, ConfigureGatewayDialogPage4Summary.class));
    }

    @Override
    protected void saveConfiguration() throws Exception {
        Gateway gateway = getConfiguration().getGateway();
        if (gateway == null) {
            Gateway newGateway;

            switch (getConfiguration().getModel()) {
                case BrematicGWY433.MODEL:
                    newGateway = new BrematicGWY433((long) -1,
                            true,
                            getConfiguration().getName(),
                            "",
                            getConfiguration().getLocalAddress(),
                            getConfiguration().getLocalPort(),
                            getConfiguration().getWanAddress(),
                            getConfiguration().getWanPort(),
                            getConfiguration().getSsids());
                    break;
                case ConnAir.MODEL:
                    newGateway = new ConnAir((long) -1,
                            true,
                            getConfiguration().getName(),
                            "",
                            getConfiguration().getLocalAddress(),
                            getConfiguration().getLocalPort(),
                            getConfiguration().getWanAddress(),
                            getConfiguration().getWanPort(),
                            getConfiguration().getSsids());
                    break;
                case EZControl_XS1.MODEL:
                    newGateway = new EZControl_XS1((long) -1,
                            true,
                            getConfiguration().getName(),
                            "",
                            getConfiguration().getLocalAddress(),
                            getConfiguration().getLocalPort(),
                            getConfiguration().getWanAddress(),
                            getConfiguration().getWanPort(),
                            getConfiguration().getSsids());
                    break;
                case ITGW433.MODEL:
                    newGateway = new ITGW433((long) -1,
                            true,
                            getConfiguration().getName(),
                            "",
                            getConfiguration().getLocalAddress(),
                            getConfiguration().getLocalPort(),
                            getConfiguration().getWanAddress(),
                            getConfiguration().getWanPort(),
                            getConfiguration().getSsids());
                    break;
                case RaspyRFM.MODEL:
                    newGateway = new RaspyRFM((long) -1,
                            true,
                            getConfiguration().getName(),
                            "",
                            getConfiguration().getLocalAddress(),
                            getConfiguration().getLocalPort(),
                            getConfiguration().getWanAddress(),
                            getConfiguration().getWanPort(),
                            getConfiguration().getSsids());
                    break;
                default:
                    throw new GatewayUnknownException();
            }

            try {
                long id = persistenceHandler.addGateway(newGateway);

                newGateway.setId(id);
                for (Long apartmentId : getConfiguration().getApartmentIds()) {
                    Apartment apartment = persistenceHandler.getApartment(apartmentId);

                    List<Gateway> associatedGateways = apartment.getAssociatedGateways();
                    if (!apartment.isAssociatedWith(id)) {
                        associatedGateways.add(newGateway);
                    }
                    Apartment updatedApartment = new Apartment(apartment.getId(),
                            apartment.isActive(),
                            apartment.getName(),
                            associatedGateways,
                            apartment.getGeofence());
                    persistenceHandler.updateApartment(updatedApartment);
                }

            } catch (GatewayAlreadyExistsException e) {
                statusMessageHandler.showInfoMessage(rootView.getContext(), R.string.gateway_already_exists, Snackbar.LENGTH_LONG);
            }
        } else {
            persistenceHandler.updateGateway(gateway.getId(),
                    getConfiguration().getName(),
                    getConfiguration().getModel(),
                    getConfiguration().getLocalAddress(),
                    getConfiguration().getLocalPort(),
                    getConfiguration().getWanAddress(),
                    getConfiguration().getWanPort(),
                    getConfiguration().getSsids());
            Gateway updatedGateway = persistenceHandler.getGateway(gateway.getId());

            List<Apartment> apartments = persistenceHandler.getAllApartments();
            for (Apartment apartment : apartments) {
                if (apartment.isAssociatedWith(updatedGateway.getId())) {
                    if (!getConfiguration().getApartmentIds()
                            .contains(apartment.getId())) {
                        for (Gateway currentGateway : apartment.getAssociatedGateways()) {
                            if (currentGateway.getId()
                                    .equals(updatedGateway.getId())) {
                                apartment.getAssociatedGateways()
                                        .remove(currentGateway);
                                persistenceHandler.updateApartment(apartment);
                                break;
                            }
                        }
                    }
                } else {
                    if (getConfiguration().getApartmentIds()
                            .contains(apartment.getId())) {
                        apartment.getAssociatedGateways()
                                .add(updatedGateway);
                        persistenceHandler.updateApartment(apartment);
                    }
                }
            }
        }

        GatewaySettingsFragment.notifyGatewaysChanged();
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        persistenceHandler.deleteGateway(getConfiguration().getGateway()
                .getId());
        GatewaySettingsFragment.notifyGatewaysChanged();
    }

}
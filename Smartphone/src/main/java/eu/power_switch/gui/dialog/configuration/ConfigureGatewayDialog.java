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

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ConfigurationDialogTabAdapter;
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
import timber.log.Timber;

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
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("Opening " + getClass().getSimpleName() + "...");
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) {
        Gateway gateway = getConfiguration().getGateway();

        if (gateway != null) {
            try {
                getConfiguration().setName(gateway.getName());
                getConfiguration().setModel(gateway.getModel());

                getConfiguration().setLocalAddress(gateway.getLocalHost());
                getConfiguration().setLocalPort(gateway.getLocalPort());
                getConfiguration().setWanAddress(gateway.getWanHost());
                getConfiguration().setWanPort(gateway.getWanPort());

                getConfiguration().setSsids(gateway.getSsids());

                List<Apartment> associatedApartments = persistanceHandler.getAssociatedApartments(gateway.getId());
                for (Apartment associatedApartment : associatedApartments) {
                    getConfiguration().getApartmentIds()
                            .add(associatedApartment.getId());
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment()));
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_gateway;
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
                long id = persistanceHandler.addGateway(newGateway);

                newGateway.setId(id);
                for (Long apartmentId : getConfiguration().getApartmentIds()) {
                    Apartment apartment = persistanceHandler.getApartment(apartmentId);

                    List<Gateway> associatedGateways = apartment.getAssociatedGateways();
                    if (!apartment.isAssociatedWith(id)) {
                        associatedGateways.add(newGateway);
                    }
                    Apartment updatedApartment = new Apartment(apartment.getId(),
                            apartment.isActive(),
                            apartment.getName(),
                            associatedGateways,
                            apartment.getGeofence());
                    persistanceHandler.updateApartment(updatedApartment);
                }

            } catch (GatewayAlreadyExistsException e) {
                StatusMessageHandler.showInfoMessage(rootView.getContext(), R.string.gateway_already_exists, Snackbar.LENGTH_LONG);
            }
        } else {
            persistanceHandler.updateGateway(gateway.getId(),
                    getConfiguration().getName(),
                    getConfiguration().getModel(),
                    getConfiguration().getLocalAddress(),
                    getConfiguration().getLocalPort(),
                    getConfiguration().getWanAddress(),
                    getConfiguration().getWanPort(),
                    getConfiguration().getSsids());
            Gateway updatedGateway = persistanceHandler.getGateway(gateway.getId());

            List<Apartment> apartments = persistanceHandler.getAllApartments();
            for (Apartment apartment : apartments) {
                if (apartment.isAssociatedWith(updatedGateway.getId())) {
                    if (!getConfiguration().getApartmentIds()
                            .contains(apartment.getId())) {
                        for (Gateway currentGateway : apartment.getAssociatedGateways()) {
                            if (currentGateway.getId()
                                    .equals(updatedGateway.getId())) {
                                apartment.getAssociatedGateways()
                                        .remove(currentGateway);
                                persistanceHandler.updateApartment(apartment);
                                break;
                            }
                        }
                    }
                } else {
                    if (getConfiguration().getApartmentIds()
                            .contains(apartment.getId())) {
                        apartment.getAssociatedGateways()
                                .add(updatedGateway);
                        persistanceHandler.updateApartment(apartment);
                    }
                }
            }
        }

        GatewaySettingsFragment.notifyGatewaysChanged();
        StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.gateway_saved, Snackbar.LENGTH_LONG);
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                .setMessage(R.string.gateway_will_be_gone_forever)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            persistanceHandler.deleteGateway(getConfiguration().getGateway()
                                    .getId());
                            GatewaySettingsFragment.notifyGatewaysChanged();
                            StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.gateway_removed, Snackbar.LENGTH_LONG);
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

        private ConfigurationDialogTabbed<GatewayConfigurationHolder> parentDialog;
        private Fragment                                              targetFragment;

        public CustomTabAdapter(ConfigurationDialogTabbed<GatewayConfigurationHolder> parentDialog, FragmentManager fm, Fragment targetFragment) {
            super(fm);
            this.parentDialog = parentDialog;
            this.targetFragment = targetFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return parentDialog.getString(R.string.address);
                case 1:
                    return parentDialog.getString(R.string.ssids);
                case 2:
                    return parentDialog.getString(R.string.apartments);
                case 3:
                    return parentDialog.getString(R.string.summary);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;

            switch (i) {
                case 0:
                default:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGatewayDialogPage1.class, parentDialog);
                    break;
                case 1:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGatewayDialogPage2.class, parentDialog);
                    break;
                case 2:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGatewayDialogPage3.class, parentDialog);
                    break;
                case 3:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGatewayDialogPage4Summary.class, parentDialog);
                    break;
            }

            fragment.setTargetFragment(targetFragment, 0);

            return fragment;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 4;
        }
    }
}
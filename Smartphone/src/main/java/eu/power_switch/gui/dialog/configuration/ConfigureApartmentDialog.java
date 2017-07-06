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
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ConfigurationDialogTabAdapter;
import eu.power_switch.gui.dialog.configuration.holder.ApartmentConfigurationHolder;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.configure_apartment.ConfigureApartmentDialogPage1Name;
import eu.power_switch.obj.Apartment;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import timber.log.Timber;

/**
 * Dialog to configure (create/edit) an Apartment
 * <p/>
 * Created by Markus on 27.12.2015.
 */
public class ConfigureApartmentDialog extends ConfigurationDialogTabbed<ApartmentConfigurationHolder> {

    /**
     * Open this dialog without predefined data
     *
     * @return An instance of this ConfigurationDialog
     */
    public static ConfigureApartmentDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(null, targetFragment);
    }

    /**
     * Open this dialog with predefined data
     *
     * @return An instance of this ConfigurationDialog
     */
    public static ConfigureApartmentDialog newInstance(Apartment apartment, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureApartmentDialog     fragment                     = new ConfigureApartmentDialog();
        ApartmentConfigurationHolder apartmentConfigurationHolder = new ApartmentConfigurationHolder();
        if (apartment != null) {
            apartmentConfigurationHolder.setApartment(apartment);
        }
        fragment.setConfiguration(apartmentConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    }

    @Override
    protected boolean initializeFromExistingData(Bundle args) {
        try {
            getConfiguration().setExistingApartments(DatabaseHandler.getAllApartments());
        } catch (Exception e) {
            dismiss();
            StatusMessageHandler.showErrorMessage(getContext(), e);
        }

        Apartment apartment = getConfiguration().getApartment();
        if (apartment != null) {
            try {
                getConfiguration().setName(apartment.getName());
                getConfiguration().setAssociatedGateways(apartment.getAssociatedGateways());
            } catch (Exception e) {
                dismiss();
                StatusMessageHandler.showErrorMessage(getContext(), e);
            }
            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment()));
            return false;
        } else {
            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment()));
            return false;
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_apartment;
    }

    @Override
    protected void saveConfiguration() throws Exception {
        Timber.d("Saving apartment");
        Long apartmentId = getConfiguration().getApartment()
                .getId();
        if (apartmentId == null) {
            boolean isActive = DatabaseHandler.getAllApartmentNames()
                    .size() <= 0;
            Apartment newApartment = new Apartment((long) -1,
                    isActive,
                    getConfiguration().getName(),
                    getConfiguration().getAssociatedGateways(),
                    null);

            long newId = DatabaseHandler.addApartment(newApartment);
            // set new apartment as active if it is the first and only one
            if (isActive) {
                SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID, newId);
            }
        } else {
            Apartment apartment = DatabaseHandler.getApartment(apartmentId);
            if (apartment.getGeofence() != null) {
                apartment.getGeofence()
                        .setName(getConfiguration().getName());
            }

            Apartment updatedApartment = new Apartment(apartmentId,
                    apartment.isActive(),
                    getConfiguration().getName(),
                    getConfiguration().getAssociatedGateways(),
                    apartment.getGeofence());

            DatabaseHandler.updateApartment(updatedApartment);
        }

        ApartmentFragment.notifyActiveApartmentChanged(getActivity());
        StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.apartment_saved, Snackbar.LENGTH_LONG);
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                .setMessage(R.string.apartment_will_be_gone_forever)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Long existingApartmentId = getConfiguration().getApartment()
                                    .getId();
                            if (SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID).equals(
                                    existingApartmentId)) {
                                DatabaseHandler.deleteApartment(existingApartmentId);

                                // update current Apartment selection
                                List<Apartment> apartments = DatabaseHandler.getAllApartments();
                                if (apartments.isEmpty()) {
                                    SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID,
                                            SettingsConstants.INVALID_APARTMENT_ID);
                                } else {
                                    SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID,
                                            apartments.get(0)
                                                    .getId());
                                }
                            } else {
                                DatabaseHandler.deleteApartment(existingApartmentId);
                            }

                            ApartmentFragment.notifyActiveApartmentChanged(getActivity());
                            StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.apartment_removed, Snackbar.LENGTH_LONG);
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

        private ConfigurationDialogTabbed<ApartmentConfigurationHolder> parentDialog;
        private Fragment                                                targetFragment;

        public CustomTabAdapter(ConfigurationDialogTabbed<ApartmentConfigurationHolder> parentDialog, FragmentManager fm, Fragment targetFragment) {
            super(fm);
            this.parentDialog = parentDialog;
            this.targetFragment = targetFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return parentDialog.getString(R.string.name);
                case 1:
                    return parentDialog.getString(R.string.location);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureApartmentDialogPage1Name.class, parentDialog);
                    fragment.setTargetFragment(targetFragment, 0);
                    break;
            }

            return fragment;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 1;
        }
    }
}

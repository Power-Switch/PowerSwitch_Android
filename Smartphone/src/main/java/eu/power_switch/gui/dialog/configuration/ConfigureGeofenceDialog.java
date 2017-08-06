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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.dialog.configuration.holder.GeofenceConfigurationHolder;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage1Location;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage2EnterActions;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage3ExitActions;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage4Summary;
import eu.power_switch.gui.fragment.geofences.ApartmentGeofencesFragment;
import eu.power_switch.gui.fragment.geofences.CustomGeofencesFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.shared.permission.PermissionHelper;
import timber.log.Timber;

/**
 * Dialog to create or modify a Geofence
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureGeofenceDialog extends ConfigurationDialog<GeofenceConfigurationHolder> {

    @Inject
    GeofenceApiHandler geofenceApiHandler;

    public static ConfigureGeofenceDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(null, targetFragment);
    }

    public static ConfigureGeofenceDialog newInstance(@Nullable Geofence geofence, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureGeofenceDialog     fragment                    = new ConfigureGeofenceDialog();
        GeofenceConfigurationHolder geofenceConfigurationHolder = new GeofenceConfigurationHolder();
        if (geofence != null) {
            geofenceConfigurationHolder.setGeofence(geofence);
        }
        fragment.setConfiguration(geofenceConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) throws Exception {
        Geofence geofence = getConfiguration().getGeofence();

        if (geofence != null) {
            // init dialog using existing geofence
            getConfiguration().setName(geofence.getName());
            getConfiguration().setLocation(geofence.getCenterLocation());
            getConfiguration().setRadius(geofence.getRadius());
            getConfiguration().setSnapshot(geofence.getSnapshot());
            getConfiguration().setEnterActions(geofence.getActions(Geofence.EventType.ENTER));
            getConfiguration().setExitActions(geofence.getActions(Geofence.EventType.EXIT));
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_geofence;
    }

    @Override
    protected void addPageEntries(List<PageEntry<GeofenceConfigurationHolder>> pageEntries) {
        pageEntries.add(new PageEntry<>(R.string.location, ConfigureGeofenceDialogPage1Location.class));
        pageEntries.add(new PageEntry<>(R.string.enter, ConfigureGeofenceDialogPage2EnterActions.class));
        pageEntries.add(new PageEntry<>(R.string.exit, ConfigureGeofenceDialogPage3ExitActions.class));
        pageEntries.add(new PageEntry<>(R.string.summary, ConfigureGeofenceDialogPage4Summary.class));
    }

    @Override
    protected void saveConfiguration() throws Exception {
        Timber.d("Saving Geofence...");

        HashMap<Geofence.EventType, List<Action>> actionsMap = new HashMap<>();
        actionsMap.put(Geofence.EventType.ENTER, getConfiguration().getEnterActions());
        actionsMap.put(Geofence.EventType.EXIT, getConfiguration().getExitActions());

        boolean isLocationPermissionAvailable = PermissionHelper.isLocationPermissionAvailable(getContext());

        Long apartmentId = getConfiguration().getApartmentId();
        if (apartmentId == null) {
            // custom geofence
            Geofence existingGeofence = getConfiguration().getGeofence();
            if (existingGeofence == null) {
                Geofence newGeofence = new Geofence(-1L,
                        isLocationPermissionAvailable,
                        getConfiguration().getName(),
                        getConfiguration().getLocation(),
                        getConfiguration().getRadius(),
                        getConfiguration().getSnapshot(),
                        actionsMap,
                        Geofence.STATE_NONE);
                long geofenceId = persistenceHandler.addGeofence(newGeofence);
                // update ID of Geofence
                newGeofence.setId(geofenceId);

                geofenceApiHandler.addGeofence(newGeofence);
            } else {
                Geofence updatedGeofence = new Geofence(getConfiguration().getGeofence()
                        .getId(),
                        existingGeofence.isActive(),
                        getConfiguration().getName(),
                        getConfiguration().getLocation(),
                        getConfiguration().getRadius(),
                        getConfiguration().getSnapshot(),
                        actionsMap,
                        existingGeofence.getState());
                persistenceHandler.updateGeofence(updatedGeofence);

                geofenceApiHandler.removeGeofence(existingGeofence.getId());
                if (existingGeofence.isActive()) {
                    geofenceApiHandler.addGeofence(updatedGeofence);
                }
            }
        } else {
            // apartment geofence

            Apartment apartment = persistenceHandler.getApartment(apartmentId);
            Apartment updatedApartment;

            if (apartment.getGeofence() == null) {
                updatedApartment = new Apartment(apartment.getId(),
                        apartment.isActive(),
                        apartment.getName(),
                        apartment.getAssociatedGateways(),
                        new Geofence(-1L,
                                isLocationPermissionAvailable,
                                apartment.getName(),
                                getConfiguration().getLocation(),
                                getConfiguration().getRadius(),
                                getConfiguration().getSnapshot(),
                                getConfiguration().getEnterActions(),
                                getConfiguration().getExitActions(),
                                Geofence.STATE_NONE));
            } else {
                Geofence geofence = apartment.getGeofence();
                Geofence updatedGeofence = new Geofence(geofence.getId(),
                        geofence.isActive(),
                        apartment.getName(),
                        getConfiguration().getLocation(),
                        getConfiguration().getRadius(),
                        getConfiguration().getSnapshot(),
                        getConfiguration().getEnterActions(),
                        getConfiguration().getExitActions(),
                        geofence.getState());

                updatedApartment = new Apartment(apartment.getId(),
                        apartment.isActive(),
                        apartment.getName(),
                        apartment.getAssociatedGateways(),
                        updatedGeofence);

                geofenceApiHandler.removeGeofence(geofence.getId());
            }

            // update apartment in database
            persistenceHandler.updateApartment(updatedApartment);

            // reload from database to get correct geofence ID
            apartment = persistenceHandler.getApartment(apartmentId);
            if (apartment.getGeofence()
                    .isActive()) {
                geofenceApiHandler.addGeofence(apartment.getGeofence());
            }
        }

        ApartmentGeofencesFragment.notifyApartmentGeofencesChanged();
        CustomGeofencesFragment.notifyCustomGeofencesChanged();
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        persistenceHandler.deleteGeofence(getConfiguration().getGeofence()
                .getId());
        geofenceApiHandler.removeGeofence(getConfiguration().getGeofence()
                .getId());

        // same for timers
        CustomGeofencesFragment.notifyCustomGeofencesChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        geofenceApiHandler.onStart();
    }

    @Override
    public void onStop() {
        geofenceApiHandler.onStop();
        super.onStop();
    }

}
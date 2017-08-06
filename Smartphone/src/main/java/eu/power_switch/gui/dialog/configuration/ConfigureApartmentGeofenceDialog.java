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

import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.dialog.configuration.holder.GeofenceConfigurationHolder;
import eu.power_switch.gui.fragment.geofences.ApartmentGeofencesFragment;

/**
 * Dialog to create or modify a Geofence related to an Apartment
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureApartmentGeofenceDialog extends ConfigureGeofenceDialog {

    public static ConfigureApartmentGeofenceDialog newInstance(long apartmentId, @NonNull Fragment targetFragment) {
        return newInstance(apartmentId, null, targetFragment);
    }

    public static ConfigureApartmentGeofenceDialog newInstance(long apartmentId, @Nullable Geofence geofence, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureApartmentGeofenceDialog fragment                    = new ConfigureApartmentGeofenceDialog();
        GeofenceConfigurationHolder      geofenceConfigurationHolder = new GeofenceConfigurationHolder();
        geofenceConfigurationHolder.setApartmentId(apartmentId);
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
        super.initializeFromExistingData(arguments);

        Long apartmentId = getConfiguration().getApartmentId();
        if (apartmentId != null) {
            String apartmentName = persistenceHandler.getApartmentName(apartmentId);
            getConfiguration().setName(apartmentName);
        }
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        persistenceHandler.deleteGeofence(getConfiguration().getGeofence()
                .getId());
        geofenceApiHandler.removeGeofence(getConfiguration().getGeofence()
                .getId());

        // same for timers
        ApartmentGeofencesFragment.notifyApartmentGeofencesChanged();
    }

}
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

package eu.power_switch.gui.dialog;

import android.os.Bundle;
import android.view.View;

import eu.power_switch.gui.fragment.RecyclerViewFragment;

/**
 * Dialog to create or modify a Geofence related to an Apartment
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureApartmentGeofenceDialog extends ConfigureGeofenceDialog {

    public static ConfigureApartmentGeofenceDialog newInstance(long geofenceId) {
        Bundle args = new Bundle();
        args.putLong(GEOFENCE_ID_KEY, geofenceId);

        ConfigureApartmentGeofenceDialog fragment = new ConfigureApartmentGeofenceDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected boolean initializeFromExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(GEOFENCE_ID_KEY)) {
            // init dialog using existing geofence
            geofenceId = arguments.getLong(GEOFENCE_ID_KEY);
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment(), geofenceId));

        } else {
            // Create the adapter that will return a fragment
            // for each of the two primary sections of the app.
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment()));
        }

        imageButtonDelete.setVisibility(View.GONE);
        return false;
    }
}
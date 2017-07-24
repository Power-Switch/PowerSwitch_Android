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

package eu.power_switch.gui.dialog;

import java.util.ArrayList;

import eu.power_switch.gui.dialog.configuration.ConfigureApartmentGeofenceDialog;
import eu.power_switch.obj.Apartment;

/**
 * Created by Markus on 17.02.2016.
 */
public class SelectApartmentForGeofenceDialog extends SelectApartmentDialog {

    @Override
    protected ArrayList<String> getApartmentNames() {
        ArrayList<String> apartmentNames = new ArrayList<>();

        try {
            for (Apartment apartment : persistenceHandler.getAllApartments()) {
                if (apartment.getGeofence() == null) {
                    apartmentNames.add(apartment.getName());
                }
            }
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(getContext(), e);
        }

        return apartmentNames;
    }

    @Override
    protected void onApartmentClicked(Apartment apartment) {
        ConfigureApartmentGeofenceDialog configureApartmentGeofenceDialog = ConfigureApartmentGeofenceDialog.newInstance(apartment.getId(),
                apartment.getGeofence(),
                getTargetFragment());
        configureApartmentGeofenceDialog.setTargetFragment(getTargetFragment(), 0);
        configureApartmentGeofenceDialog.show(getFragmentManager(), null);
        dismiss();
    }
}

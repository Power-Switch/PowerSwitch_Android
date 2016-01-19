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

package eu.power_switch.gui.fragment.configure_apartment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigureApartmentDialog;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.log.Log;

/**
 * "Location" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureApartmentDialogPage2LocationFragment extends Fragment {

    private long apartmentId;
    private View rootView;

    private String currentName;
    private List<Gateway> currentCheckedGateways;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_apartment_page_2, container, false);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureApartmentDialog.APARTMENT_ID_KEY)) {
            apartmentId = args.getLong(ConfigureApartmentDialog.APARTMENT_ID_KEY);
            initializeApartmentData(apartmentId);
        }

        checkValidity();

        return rootView;
    }

    private void initializeApartmentData(long apartmentId) {
        try {
            Apartment apartment = DatabaseHandler.getApartment(apartmentId);
            currentName = apartment.getName();
            currentCheckedGateways = apartment.getAssociatedGateways();

        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    public void saveCurrentConfigurationToDatabase() {
        try {
            if (apartmentId == -1) {
                String apartmentName = currentName;

                Apartment newApartment = new Apartment((long) -1, apartmentName, currentCheckedGateways);

                try {
                    DatabaseHandler.addApartment(newApartment);
                } catch (Exception e) {
                    StatusMessageHandler.showStatusMessage(rootView.getContext(),
                            R.string.unknown_error, Snackbar.LENGTH_LONG);
                }
            } else {
                DatabaseHandler.updateApartment(apartmentId, currentName, currentCheckedGateways);
            }

            ApartmentFragment.sendApartmentChangedBroadcast(getActivity());
            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(), R.string.apartment_saved,
                    Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(rootView.getContext(), R.string.unknown_error, Snackbar.LENGTH_LONG);
        }

    }

    public boolean checkValidity() {
        return false;
    }
}

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

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * Dialog used to quickly select and activate an Apartment
 * <p/>
 * Created by Markus on 08.01.2016.
 */
public class SelectApartmentDialog extends DialogFragment {

    private ArrayList<String> apartmentNames = new ArrayList<>();
    private View rootView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.dialog_apartment_chooser, null);

        ListView listViewApartments = (ListView) rootView.findViewById(R.id.listview_apartments);

        // should be called async but since its still very fast it doesn't make that much of a difference
        apartmentNames.addAll(getApartmentNames());

        ArrayAdapter<String> apartmentNamesAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_list_item_1, apartmentNames);
        listViewApartments.setAdapter(apartmentNamesAdapter);
        listViewApartments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    onApartmentClicked(
                            DatabaseHandler.getApartmentId(apartmentNames.get(position)));
                } catch (Exception e) {
                    dismiss();
                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setTitle(getString(R.string.select_apartment));
        builder.setNeutralButton(R.string.close, null);

        Dialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    /**
     * Used to get Apartment names that can be selected
     *
     * @return list of apartment names
     */
    protected ArrayList<String> getApartmentNames() {
        ArrayList<String> apartmentNames = new ArrayList<>();

        try {
            if (DeveloperPreferencesHandler.getPlayStoreMode()) {
                PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getContext());
                for (Apartment apartment : playStoreModeDataModel.getApartments()) {
                    apartmentNames.add(apartment.getName());
                }
            } else {
                apartmentNames.addAll(DatabaseHandler.getAllApartmentNames());
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }

        return apartmentNames;
    }

    /**
     * This Method is called when an Apartment has been selected from the list
     *
     * @param apartmentId the selected Apartment
     */
    protected void onApartmentClicked(Long apartmentId) {
        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID, apartmentId);
        ApartmentFragment.sendApartmentChangedBroadcast(getContext());
        dismiss();
    }
}

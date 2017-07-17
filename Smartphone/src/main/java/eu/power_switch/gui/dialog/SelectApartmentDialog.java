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
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.dialog.eventbus.EventBusSupportDialogFragment;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;

/**
 * Dialog used to quickly select and activate an Apartment
 * <p/>
 * Created by Markus on 08.01.2016.
 */
public class SelectApartmentDialog extends EventBusSupportDialogFragment {

    @BindView(R.id.listview_apartments)
    ListView listViewApartments;

    @Inject
    PersistenceHandler persistenceHandler;

    private ArrayList<String> apartmentNames = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // should be called async but since its still very fast it doesn't make that much of a difference
        apartmentNames.addAll(getApartmentNames());

        ArrayAdapter<String> apartmentNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, apartmentNames);
        listViewApartments.setAdapter(apartmentNamesAdapter);
        listViewApartments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    onApartmentClicked(persistenceHandler.getApartment(apartmentNames.get(position)));
                } catch (Exception e) {
                    dismiss();
                    statusMessageHandler.showErrorMessage(getActivity(), e);
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

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_apartment_chooser;
    }

    /**
     * Used to get Apartment names that can be selected
     *
     * @return list of apartment names
     */
    protected ArrayList<String> getApartmentNames() {
        ArrayList<String> apartmentNames = new ArrayList<>();

        try {
            apartmentNames.addAll(persistenceHandler.getAllApartmentNames());
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(getActivity(), e);
        }

        return apartmentNames;
    }

    /**
     * This Method is called when an Apartment has been selected from the list
     *
     * @param apartment the selected Apartment
     */
    protected void onApartmentClicked(Apartment apartment) {
        smartphonePreferencesHandler.setValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB, apartment.getId());
        ApartmentFragment.notifyActiveApartmentChanged(getContext());
        dismiss();
    }
}

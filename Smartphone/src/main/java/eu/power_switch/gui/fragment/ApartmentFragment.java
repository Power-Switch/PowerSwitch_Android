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

package eu.power_switch.gui.fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.ApartmentRecyclerViewAdapter;
import eu.power_switch.obj.Apartment;

/**
 * Created by Markus on 25.12.2015.
 */
public class ApartmentFragment extends RecyclerViewFragment {

    private RecyclerView recyclerViewApartments;
    private ApartmentRecyclerViewAdapter apartmentArrayAdapter;
    private ArrayList<Apartment> apartments;
    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_apartment, container, false);
        setHasOptionsMenu(true);

        apartments = new ArrayList<>(DatabaseHandler.getAllApartments());
        recyclerViewApartments = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_apartments);
        apartmentArrayAdapter = new ApartmentRecyclerViewAdapter(getActivity(), apartments);

        recyclerViewApartments.setAdapter(apartmentArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.apartments_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewApartments.setLayoutManager(layoutManager);

        final RecyclerViewFragment recyclerViewFragment = this;
        fab = (FloatingActionButton) rootView.findViewById(R.id.add_apartment_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                CreateApartmentDialog createApartmentDialog = new CreateApartmentDialog();
//                createApartmentDialog.setTargetFragment(recyclerViewFragment, 0);
//                createApartmentDialog.show(getFragmentManager(), null);
            }
        });

        return rootView;
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewApartments;
    }
}

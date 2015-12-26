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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ApartmentRecyclerViewAdapter;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.wear.service.UtilityService;

/**
 * Created by Markus on 25.12.2015.
 */
public class ApartmentFragment extends RecyclerViewFragment {

    private RecyclerView recyclerViewApartments;
    private ApartmentRecyclerViewAdapter apartmentArrayAdapter;
    private ArrayList<Apartment> apartments;
    private FloatingActionButton fab;
    private BroadcastReceiver broadcastReceiver;

    /**
     * Used to notify other Fragments that the selected Apartment has changed
     *
     * @param context any suitable context
     */
    public static void sendApartmentChangedBroadcast(Context context) {
        Log.d("ApartmentFragment", "sendApartmentChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_APARTMENT_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        UtilityService.forceWearDataUpdate(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_apartment, container, false);
        setHasOptionsMenu(true);

        final RecyclerViewFragment recyclerViewFragment = this;
        apartments = new ArrayList<>(DatabaseHandler.getAllApartments());
        recyclerViewApartments = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_apartments);
        apartmentArrayAdapter = new ApartmentRecyclerViewAdapter(getActivity(), apartments);

        recyclerViewApartments.setAdapter(apartmentArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.apartments_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewApartments.setLayoutManager(layoutManager);
        apartmentArrayAdapter.setOnItemClickListener(new ApartmentRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                final Apartment apartment = apartments.get(position);

                SmartphonePreferencesHandler.setCurrentApartmentId(apartment.getId());

                for (Apartment currentApartment : apartments) {
                    if (currentApartment.getId().equals(apartment.getId())) {
                        currentApartment.setActive(true);
                    } else {
                        currentApartment.setActive(false);
                    }
                }

                apartmentArrayAdapter.notifyDataSetChanged();
            }
        });
        apartmentArrayAdapter.setOnItemLongClickListener(new ApartmentRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, final int position) {
                final Apartment apartment = apartments.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DatabaseHandler.deleteApartment(apartment.getId());
                            apartments.remove(position);

                            StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.apartment_removed, Snackbar.LENGTH_LONG);
                            sendApartmentChangedBroadcast(getContext());
                        } catch (Exception e) {
                            Log.e(e);
                            StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.unknown_error, Snackbar.LENGTH_LONG);
                        }
                    }
                }).setNeutralButton(android.R.string.cancel, null).setTitle(getString(R.string
                        .are_you_sure))
                        .setMessage(R.string.remove_apartment_message);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_apartment_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler.addApartment(new Apartment(null, "Dummy Apartment", new LinkedList<Room>(), new LinkedList<Scene>()));
//                CreateApartmentDialog createApartmentDialog = new CreateApartmentDialog();
//                createApartmentDialog.setTargetFragment(recyclerViewFragment, 0);
//                createApartmentDialog.show(getFragmentManager(), null);
                apartmentArrayAdapter.notifyDataSetChanged();
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());
                apartments.clear();
                apartments.addAll(DatabaseHandler.getAllApartments());
                apartmentArrayAdapter.notifyDataSetChanged();
            }
        };

        return rootView;
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewApartments;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_APARTMENT_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

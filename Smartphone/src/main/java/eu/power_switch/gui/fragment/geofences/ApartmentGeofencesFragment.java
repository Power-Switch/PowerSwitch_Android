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

package eu.power_switch.gui.fragment.geofences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.GeofenceRecyclerViewAdapter;
import eu.power_switch.gui.dialog.ConfigureGeofenceDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * Fragment containing a List of all Apartment related Geofences
 */
public class ApartmentGeofencesFragment extends RecyclerViewFragment {

    private ArrayList<Geofence> geofences;
    private GeofenceRecyclerViewAdapter geofenceRecyclerViewAdapter;
    private RecyclerView recyclerViewGeofences;
    private BroadcastReceiver broadcastReceiver;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_apartment_geofences, container, false);

        geofences = new ArrayList<>();

        recyclerViewGeofences = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_geofences);
        geofenceRecyclerViewAdapter = new GeofenceRecyclerViewAdapter(getActivity(), geofences);
        recyclerViewGeofences.setAdapter(geofenceRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager
                .VERTICAL);
        recyclerViewGeofences.setLayoutManager(layoutManager);

        final RecyclerViewFragment recyclerViewFragment = this;
        geofenceRecyclerViewAdapter.setOnItemLongClickListener(new GeofenceRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                final Geofence geofence = geofences.get(position);

                ConfigureGeofenceDialog configureGeofenceDialog = new ConfigureGeofenceDialog();
                Bundle geofenceData = new Bundle();
                geofenceData.putLong(ConfigureGeofenceDialog.GEOFENCE_ID_KEY, geofence.getId());
                configureGeofenceDialog.setArguments(geofenceData);
                configureGeofenceDialog.setTargetFragment(recyclerViewFragment, 0);
                configureGeofenceDialog.show(getFragmentManager(), null);
            }
        });

        refreshGeofences();

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());
                refreshGeofences();
            }
        };

        return rootView;
    }

    @UiThread
    private void refreshGeofences() {
        Log.d(this, "refreshGeofences");
        geofences.clear();

//        if (SmartphonePreferencesHandler.getPlayStoreMode()) {
//            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
//            geofences.addAll(playStoreModeDataModel.getCustomGeofences());
//        } else {
        fillListWithGeofences();
//        }

        geofenceRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void fillListWithGeofences() {
        try {
            List<Apartment> apartments = DatabaseHandler.getAllApartments();
            for (Apartment apartment :
                    apartments) {
                geofences.add(apartment.getGeofence());
            }
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GEOFENCE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewGeofences;
    }
}
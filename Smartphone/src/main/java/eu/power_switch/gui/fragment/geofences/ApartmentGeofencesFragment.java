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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.GeofenceRecyclerViewAdapter;
import eu.power_switch.gui.dialog.ConfigureApartmentGeofenceDialog;
import eu.power_switch.gui.dialog.SelectApartmentForGeofenceDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;

/**
 * Fragment containing a List of all Apartment related Geofences
 */
public class ApartmentGeofencesFragment extends RecyclerViewFragment {

    private HashMap<Long, Apartment> geofenceIdApartmentMap = new HashMap<>();
    private ArrayList<Geofence> geofences;
    private GeofenceRecyclerViewAdapter geofenceRecyclerViewAdapter;
    private RecyclerView recyclerViewGeofences;
    private BroadcastReceiver broadcastReceiver;
    private View rootView;
    private GeofenceApiHandler geofenceApiHandler;
    private FloatingActionButton fab;

    /**
     * Used to notify the apartment geofence page (this) that geofences have changed
     *
     * @param context any suitable context
     */
    public static void sendApartmentGeofencesChangedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_APARTMENT_GEOFENCE_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_apartment_geofences, container, false);
        setHasOptionsMenu(true);

        geofenceApiHandler = new GeofenceApiHandler(getActivity());

        geofences = new ArrayList<>();

        recyclerViewGeofences = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_geofences);
        geofenceRecyclerViewAdapter = new GeofenceRecyclerViewAdapter(getActivity(), geofences, geofenceApiHandler);
        recyclerViewGeofences.setAdapter(geofenceRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager
                .VERTICAL);
        recyclerViewGeofences.setLayoutManager(layoutManager);

        final RecyclerViewFragment recyclerViewFragment = this;
        geofenceRecyclerViewAdapter.setOnItemLongClickListener(new GeofenceRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                final Geofence geofence = geofences.get(position);

                ConfigureApartmentGeofenceDialog configureApartmentGeofenceDialog =
                        ConfigureApartmentGeofenceDialog.newInstance(geofenceIdApartmentMap.get(geofence.getId())
                                .getId());
                configureApartmentGeofenceDialog.setTargetFragment(recyclerViewFragment, 0);
                configureApartmentGeofenceDialog.show(getFragmentManager(), null);
            }
        });

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_geofence_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (DatabaseHandler.getAllApartments().size() == geofences.size()) {
                        StatusMessageHandler.showInfoMessage(getRecyclerView(), "All Apartments already have a geofence", Snackbar.LENGTH_LONG);
                        return;
                    }
                } catch (Exception e) {
                    Log.e(e);
                }

                SelectApartmentForGeofenceDialog selectApartmentForGeofenceDialog = new SelectApartmentForGeofenceDialog();
                selectApartmentForGeofenceDialog.setTargetFragment(recyclerViewFragment, 0);
                selectApartmentForGeofenceDialog.show(getFragmentManager(), null);
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
        geofenceIdApartmentMap.clear();

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
            for (Apartment apartment : apartments) {
                // apartment can have no associated Geofence, so we just ignore it
                if (apartment.getGeofence() != null) {
                    geofences.add(apartment.getGeofence());
                    geofenceIdApartmentMap.put(apartment.getGeofence().getId(), apartment);
                }
            }

            if (apartments.size() == geofences.size()) {
                fab.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_geofence:
                try {
                    if (DatabaseHandler.getAllApartments().size() == geofences.size()) {
                        StatusMessageHandler.showInfoMessage(getRecyclerView(), "All Apartments already have a geofence", Snackbar.LENGTH_LONG);
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(e);
                }

                SelectApartmentForGeofenceDialog selectApartmentForGeofenceDialog = new SelectApartmentForGeofenceDialog();
                selectApartmentForGeofenceDialog.setTargetFragment(this, 0);
                selectApartmentForGeofenceDialog.show(getFragmentManager(), null);
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            inflater.inflate(R.menu.apartment_geofences_fragment_menu, menu);
            if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
                menu.findItem(R.id.create_geofence)
                        .setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
            } else {
                menu.findItem(R.id.create_geofence)
                        .setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.black));
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_APARTMENT_GEOFENCE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
        geofenceApiHandler.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        geofenceApiHandler.onStop();
        super.onStop();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewGeofences;
    }
}
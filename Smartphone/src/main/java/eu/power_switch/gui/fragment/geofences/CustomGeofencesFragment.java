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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.GeofenceRecyclerViewAdapter;
import eu.power_switch.gui.dialog.ConfigureGeofenceDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.permission.PermissionHelper;

/**
 * Fragment containing a List of all custom Geofences created by the user
 */
public class CustomGeofencesFragment extends RecyclerViewFragment {

    private ArrayList<Geofence> geofences = new ArrayList<>();
    private GeofenceRecyclerViewAdapter geofenceRecyclerViewAdapter;
    private RecyclerView recyclerViewGeofences;
    private BroadcastReceiver broadcastReceiver;
    private FloatingActionButton fab;
    private GeofenceApiHandler geofenceApiHandler;

    /**
     * Used to notify the custom geofence page (this) that geofences have changed
     *
     * @param context any suitable context
     */
    public static void sendCustomGeofencesChangedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_CUSTOM_GEOFENCE_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onCreateViewEvent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_custom_geofences, container, false);
        setHasOptionsMenu(true);

        geofenceApiHandler = new GeofenceApiHandler(getActivity());

        recyclerViewGeofences = (RecyclerView) rootView.findViewById(R.id.recyclerview);
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

                ConfigureGeofenceDialog configureGeofenceDialog = ConfigureGeofenceDialog.newInstance(geofence.getId());
                configureGeofenceDialog.setTargetFragment(recyclerViewFragment, 0);
                configureGeofenceDialog.show(getFragmentManager(), null);
            }
        });

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.checkLocationPermission(getContext())) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.missing_permission)
                            .setMessage(R.string.missing_location_permission)
                            .setNeutralButton(R.string.close, null)
                            .show();
                    return;
                }

                ConfigureGeofenceDialog configureGeofenceDialog = new ConfigureGeofenceDialog();
                configureGeofenceDialog.setTargetFragment(recyclerViewFragment, 0);
                configureGeofenceDialog.show(getFragmentManager(), null);
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());
                updateUI();
            }
        };
    }

    @Override
    protected void onInitialized() {
        updateUI();
    }

    @UiThread
    private void updateUI() {
        Log.d(this, "updateUI");
        updateListContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_geofence:
                ConfigureGeofenceDialog configureGeofenceDialog = new ConfigureGeofenceDialog();
                configureGeofenceDialog.setTargetFragment(this, 0);
                configureGeofenceDialog.show(getFragmentManager(), null);
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            inflater.inflate(R.menu.custom_geofences_fragment_menu, menu);
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
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_CUSTOM_GEOFENCE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
        geofenceApiHandler.onStart();
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

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return geofenceRecyclerViewAdapter;
    }

    @Override
    public List refreshListData() throws Exception {
        geofences.clear();

//        if (SmartphonePreferencesHandler.getPlayStoreMode()) {
//            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
//            geofences.addAll(playStoreModeDataModel.getScenes());
//        } else {
        geofences.addAll(DatabaseHandler.getCustomGeofences());
//        }

        return geofences;
    }
}
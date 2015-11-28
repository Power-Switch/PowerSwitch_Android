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

package eu.power_switch.gui.fragment.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.exception.gateway.GatewayHasBeenEnabledException;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.GatewayRecyclerViewAdapter;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.gui.dialog.CreateGatewayDialog;
import eu.power_switch.gui.dialog.EditGatewayDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * Fragment containing all settings related to Gateways
 * <p/>
 * Created by Markus on 30.08.2015.
 */
public class GatewaySettingsFragment extends RecyclerViewFragment {

    private View rootView;

    private BroadcastReceiver broadcastReceiver;

    private GatewayRecyclerViewAdapter gatewayRecyclerViewAdapter;
    private RecyclerView recyclerViewGateways;
    private ArrayList<Gateway> gateways = new ArrayList<>();
    private FloatingActionButton searchGatewayFAB;
    private FloatingActionButton addGatewayFAB;

    public static void sendGatewaysChangedBroadcast(Context context) {
        Log.d("CreateGatewayDialog", "sendGatewaysChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_GATEWAY_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_gateway_settings, container, false);
        setHasOptionsMenu(true);

        final RecyclerViewFragment recyclerViewFragment = this;
        final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (view.getId()) {
                    case R.id.add_gateway_fab:
                        CreateGatewayDialog createGatewayDialog = new CreateGatewayDialog();
                        createGatewayDialog.setTargetFragment(recyclerViewFragment, 0);
                        createGatewayDialog.show(getFragmentManager(), null);
                        break;
                    case R.id.search_gateway_fab:
                        startAutoDiscovery();
                        break;
                    default:
                        break;
                }
            }
        };

        searchGatewayFAB = (FloatingActionButton) rootView.findViewById(R.id.search_gateway_fab);
        searchGatewayFAB.setOnClickListener(onClickListener);

        addGatewayFAB = (FloatingActionButton) rootView.findViewById(R.id.add_gateway_fab);
        addGatewayFAB.setOnClickListener(onClickListener);

        recyclerViewGateways = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_gateways);
        gatewayRecyclerViewAdapter = new GatewayRecyclerViewAdapter(getActivity(), gateways);
        gatewayRecyclerViewAdapter.setOnItemLongClickListener(new GatewayRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                Gateway gateway = gateways.get(position);

                Bundle gatewayData = new Bundle();
                gatewayData.putLong("id", gateway.getId());
                gatewayData.putString("name", gateway.getName());
                gatewayData.putString("model", gateway.getModelAsString());
                gatewayData.putString("address", gateway.getHost());
                gatewayData.putInt("port", gateway.getPort());

                EditGatewayDialog dia = new EditGatewayDialog();
                dia.setTargetFragment(recyclerViewFragment, 0);
                dia.setArguments(gatewayData);
                dia.show(getFragmentManager(), null);
            }
        });
        recyclerViewGateways.setAdapter(gatewayRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.gateway_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewGateways.setLayoutManager(layoutManager);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };

        return rootView;
    }

    private void startAutoDiscovery() {
        if (!NetworkHandler.isWifiAvailable(getActivity())) {
            StatusMessageHandler.showStatusMessage(this, R.string.missing_wifi_connection, Snackbar.LENGTH_LONG);
            return;
        }

        searchGatewayFAB.startAnimation(AnimationHandler.getRotationClockwiseAnimation(getContext()));
        final RecyclerViewFragment recyclerViewFragment = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NetworkHandler nwm = new NetworkHandler(getActivity());
                    final List<Gateway> foundGateways = nwm.searchGateways();

                    // stop animation
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchGatewayFAB.clearAnimation();
                        }
                    });

                    if (foundGateways == null || foundGateways.isEmpty()) {
                        StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.no_gateway_found, Snackbar
                                .LENGTH_LONG);
                        return;
                    }

                    for (Gateway newGateway : foundGateways) {
                        if (newGateway == null) {
                            StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.cant_understand_gateway, Snackbar
                                    .LENGTH_LONG);
                            continue;
                        }
                        // save new Gateway if it doesn't exist already
                        boolean alreadyInDatabase;
                        alreadyInDatabase = isGatewayAlreadyInDatabase(newGateway);

                        if (alreadyInDatabase) {
                            StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.gateway_already_exists_it_has_been_enabled,
                                    Snackbar.LENGTH_LONG);
                        } else {
                            // TODO: Exceptions richtig abfangen und verwenden
                            try {
                                addGateway(newGateway);
                            } catch (Exception e) {
                                Log.e(e);
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(e);
                }
            }
        }).start();
    }

    private boolean isGatewayAlreadyInDatabase(Gateway newGateway) {
        for (Gateway gateway : DatabaseHandler.getAllGateways()) {
            if (gateway.hasSameAddress(newGateway)) {
                // enable existing gateway to avoid user confusion
                if (!gateway.isActive()) {
                    DatabaseHandler.enableGateway(gateway.getId());
                    refreshGateways();
                    for (int i = 0; i < gateways.size(); i++) {
                        Gateway currentGateway = gateways.get(i);
                        if (currentGateway.hasSameAddress(newGateway)) {
                            gatewayRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void addGateway(Gateway newGateway) throws GatewayHasBeenEnabledException, GatewayAlreadyExistsException {
        DatabaseHandler.addGateway(newGateway);
        gateways.add(newGateway);
        gatewayRecyclerViewAdapter.notifyDataSetChanged();
        StatusMessageHandler.showStatusMessage(this, R.string.gateway_found, Snackbar.LENGTH_LONG);
    }

    private void refreshGateways() {
        Log.d("GatewaySettingsFragment", "refreshGateways");

        gateways.clear();

        if (SharedPreferencesHandler.getPlayStoreMode()) {
            gateways.addAll(PlayStoreModeDataModel.getGateways());
        } else {
            gateways.addAll(DatabaseHandler.getAllGateways());
        }

    }

    private void updateUI() {
        if (SharedPreferencesHandler.getHideAddFAB()) {
            searchGatewayFAB.setVisibility(View.GONE);
            addGatewayFAB.setVisibility(View.GONE);
        } else {
            searchGatewayFAB.setVisibility(View.VISIBLE);
            addGatewayFAB.setVisibility(View.VISIBLE);
        }

        refreshGateways();

        gatewayRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_gateway:
                CreateGatewayDialog createGatewayDialog = new CreateGatewayDialog();
                createGatewayDialog.setTargetFragment(this, 0);
                createGatewayDialog.show(getFragmentManager(), null);
            case R.id.search_gateways:
                startAutoDiscovery();
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.gateway_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GATEWAY_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public void onResume() {
        updateUI();
        super.onResume();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewGateways;
    }
}

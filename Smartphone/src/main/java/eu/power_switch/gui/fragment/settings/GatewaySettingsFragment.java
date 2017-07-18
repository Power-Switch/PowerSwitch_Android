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

package eu.power_switch.gui.fragment.settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.event.GatewayChangedEvent;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.GatewayRecyclerViewAdapter;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.gui.dialog.configuration.ConfigureGatewayDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.exception.gateway.GatewayAlreadyExistsException;
import timber.log.Timber;

/**
 * Fragment containing all settings related to Gateways
 * <p/>
 * Created by Markus on 30.08.2015.
 */
public class GatewaySettingsFragment extends RecyclerViewFragment<Gateway> {

    @BindView(R.id.search_gateway_fab)
    FloatingActionButton searchGatewayFAB;

    @Inject
    NetworkHandler networkHandler;

    private GatewayRecyclerViewAdapter gatewayRecyclerViewAdapter;
    private ArrayList<Gateway> gateways = new ArrayList<>();

    public static void notifyGatewaysChanged() {
        Timber.d("notifyGatewaysChanged");
        EventBus.getDefault()
                .post(new GatewayChangedEvent());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            searchGatewayFAB.setVisibility(View.GONE);
        } else {
            searchGatewayFAB.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final RecyclerViewFragment recyclerViewFragment = this;
        final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (view.getId()) {
                    case R.id.add_fab:
                        ConfigureGatewayDialog configureGatewayDialog = ConfigureGatewayDialog.newInstance(recyclerViewFragment);
                        configureGatewayDialog.show(getFragmentManager(), null);
                        break;
                    case R.id.search_gateway_fab:
                        startAutoDiscovery();
                        break;
                    default:
                        break;
                }
            }
        };

        searchGatewayFAB.setImageDrawable(IconicsHelper.getRefreshIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        searchGatewayFAB.setOnClickListener(onClickListener);

        addFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addFAB.setOnClickListener(onClickListener);

        gatewayRecyclerViewAdapter = new GatewayRecyclerViewAdapter(getActivity(), persistenceHandler, statusMessageHandler, gateways);
        gatewayRecyclerViewAdapter.setOnItemLongClickListener(new GatewayRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                Gateway gateway = gateways.get(position);

                ConfigureGatewayDialog configureGatewayDialog = ConfigureGatewayDialog.newInstance(gateway, recyclerViewFragment);
                configureGatewayDialog.show(getFragmentManager(), null);
            }
        });
        getRecyclerView().setAdapter(gatewayRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);

        updateListContent();

        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onGatewayChanged(GatewayChangedEvent gatewayChangedEvent) {
        updateListContent();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_gateway_settings;
    }

    private void startAutoDiscovery() {
        if (!networkHandler.isWifiConnected()) {
            statusMessageHandler.showInfoMessage(getRecyclerView(), R.string.missing_wifi_connection, Snackbar.LENGTH_LONG);
            return;
        }

        searchGatewayFAB.startAnimation(AnimationHandler.getRotationClockwiseAnimation(getContext()));
        final RecyclerViewFragment recyclerViewFragment = this;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<Gateway> foundGateways = networkHandler.searchGateways();

                    // stop animation
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchGatewayFAB.clearAnimation();
                        }
                    });

                    if (foundGateways == null || foundGateways.isEmpty()) {
                        statusMessageHandler.showInfoMessage(recyclerViewFragment.getRecyclerView(), R.string.no_gateway_found, Snackbar.LENGTH_LONG);
                        return null;
                    }

                    int unknownGatewaysCount  = 0;
                    int existingGatewaysCount = 0;
                    int newGatewaysCount      = 0;
                    for (Gateway newGateway : foundGateways) {
                        if (newGateway == null) {
                            unknownGatewaysCount++;
                            continue;
                        }

                        // save new Gateway if it doesn't exist already
                        try {
                            persistenceHandler.addGateway(newGateway);
                            newGatewaysCount++;
                        } catch (GatewayAlreadyExistsException e) {
                            existingGatewaysCount++;
                            persistenceHandler.enableGateway(e.getIdOfExistingGateway());
                        } catch (Exception e) {
                            statusMessageHandler.showErrorMessage(recyclerViewFragment.getRecyclerView(), e);
                        }
                    }

                    statusMessageHandler.showInfoMessage(recyclerViewFragment.getRecyclerView(),
                            getString(R.string.autodiscover_response_message, newGatewaysCount, existingGatewaysCount, unknownGatewaysCount),
                            Snackbar.LENGTH_LONG);

                    notifyGatewaysChanged();
                } catch (Exception e) {
                    Timber.e(e);
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
            }

            @Override
            protected void onPostExecute(Void aVoid) {
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_gateway:
                ConfigureGatewayDialog configureGatewayDialog = ConfigureGatewayDialog.newInstance(this);
                configureGatewayDialog.show(getFragmentManager(), null);
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
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.create_gateway)
                .setIcon(IconicsHelper.getAddIcon(getActivity(), color));
        menu.findItem(R.id.search_gateways)
                .setIcon(IconicsHelper.getRefreshIcon(getActivity(), color));

        boolean useOptionsMenuOnly = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        if (!useOptionsMenuOnly) {
            menu.findItem(R.id.create_gateway)
                    .setVisible(false)
                    .setEnabled(false);
        }
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return gatewayRecyclerViewAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.gateway_grid_span_count);
    }

    @Override
    public List<Gateway> loadListData() throws Exception {
        return persistenceHandler.getAllGateways();
    }

    @Override
    protected void onListDataChanged(List<Gateway> list) {
        gateways.clear();
        gateways.addAll(list);
    }
}

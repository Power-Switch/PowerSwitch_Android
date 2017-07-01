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

package eu.power_switch.wizard.gui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.adapter.WizardGatewayRecyclerViewAdapter;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.wizard.config.ConfigurationHolder;
import timber.log.Timber;

/**
 * Setup page for searching a Gateway
 * <p>
 * Created by Markus on 05.11.2016.
 */
public class SetupGatewayPage extends ConfigurationPage {

    @BindView(R.id.layoutLoading)
    LinearLayout     layoutLoading;
    @BindView(R.id.layoutRefresh)
    LinearLayout     layoutRefresh;
    @BindView(R.id.button_refresh)
    IconicsImageView refreshIcon;

    @BindView(R.id.recyclerViewGateways)
    RecyclerView recyclerViewGateways;
    @BindView(R.id.textViewEmpty)
    TextView     textViewEmpty;


    private ArrayList<Gateway> foundGateways = new ArrayList<>();

    private WizardGatewayRecyclerViewAdapter gatewayRecyclerViewAdapter;


    public static SetupGatewayPage newInstance(ConfigurationHolder configurationHolder) {
        Bundle           args     = new Bundle();
        SetupGatewayPage fragment = new SetupGatewayPage();
        fragment.setArguments(args);
        fragment.setConfigurationHolder(configurationHolder);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        layoutRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findLocalGateways();
            }
        });

        gatewayRecyclerViewAdapter = new WizardGatewayRecyclerViewAdapter(getActivity(), foundGateways);
        recyclerViewGateways.setAdapter(gatewayRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewGateways.setLayoutManager(layoutManager);

        return getMainView();
    }

    private void findLocalGateways() {
        new AsyncTask<Void, Void, ArrayList<Gateway>>() {

            @Override
            protected void onPreExecute() {
                Animation rotationClockwiseAnimation = AnimationHandler.getClockwiseInfiniteAnimation(getActivity());
                refreshIcon.startAnimation(rotationClockwiseAnimation);

                foundGateways.clear();

                showLoading();
            }

            @Override
            protected ArrayList<Gateway> doInBackground(Void... voids) {
                ArrayList<Gateway> gateways = new ArrayList<>(NetworkHandler.searchGateways());

                for (Gateway gateway : gateways) {
                    try {
                        DatabaseHandler.addGateway(gateway);
                    } catch (GatewayAlreadyExistsException e) {
                        Timber.w("Wizard: ignoring found gateway that already exists in database");
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }

                return gateways;
            }

            @Override
            protected void onPostExecute(ArrayList<Gateway> gateways) {
                foundGateways.addAll(gateways);

                gatewayRecyclerViewAdapter.notifyDataSetChanged();

                if (foundGateways.isEmpty()) {
                    showEmpty();
                    setValid(false);
                } else {
                    showResult();
                    setValid(true);
                }

                refreshIcon.clearAnimation();
            }
        }.execute();
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        recyclerViewGateways.setVisibility(View.GONE);
        textViewEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        layoutLoading.setVisibility(View.GONE);
        recyclerViewGateways.setVisibility(View.GONE);
        textViewEmpty.setVisibility(View.VISIBLE);
    }

    private void showResult() {
        layoutLoading.setVisibility(View.GONE);
        recyclerViewGateways.setVisibility(View.VISIBLE);
        textViewEmpty.setVisibility(View.GONE);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.wizard_page_setup_gateway;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.md_green_700);
    }

    @Override
    public void onResume() {
        super.onResume();

        findLocalGateways();
    }

}

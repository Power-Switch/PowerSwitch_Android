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

package eu.power_switch.gui.fragment.configure_gateway;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.event.GatewaySsidAddedEvent;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.SsidRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddSsidDialog;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.GatewayConfigurationHolder;
import eu.power_switch.obj.gateway.Gateway;

/**
 * "SSID" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureGatewayDialogPage2 extends ConfigurationDialogPage<GatewayConfigurationHolder> {

    @BindView(R.id.add_ssid_fab)
    FloatingActionButton addSsidFAB;
    @BindView(R.id.recyclerView_ssids)
    RecyclerView         recyclerViewSsids;

    private ArrayList<String> ssids = new ArrayList<>();

    private SsidRecyclerViewAdapter ssidRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        initializeGatewayData();

        ssidRecyclerViewAdapter = new SsidRecyclerViewAdapter(getActivity(), ssids);
        recyclerViewSsids.setAdapter(ssidRecyclerViewAdapter);
        ssidRecyclerViewAdapter.setOnDeleteClickListener(new SsidRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, final int position) {
                new AlertDialog.Builder(getContext()).setTitle(R.string.delete)
                        .setMessage(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    ssids.remove(position);
                                    ssidRecyclerViewAdapter.notifyDataSetChanged();
                                    updateConfiguration(ssids);
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getContentView(), e);
                                }
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();
            }
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewSsids.setLayoutManager(layoutManager);

        addSsidFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addSsidFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddSsidDialog addSsidDialog = new AddSsidDialog();
                addSsidDialog.setTargetFragment(ConfigureGatewayDialogPage2.this, 0);
                addSsidDialog.show(getFragmentManager(), null);
            }
        });

        return rootView;
    }

    /**
     * Used to notify the setup page that some info has changed
     */
    public void updateConfiguration(List<String> ssids) {
        getConfiguration().setSsids(new HashSet<>(ssids));

        notifyConfigurationChanged();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_gateway_page_2;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onSsidAdded(GatewaySsidAddedEvent e) {
        List<String> newSsids = e.getSsids();

        ssids.addAll(newSsids);
        ssidRecyclerViewAdapter.notifyDataSetChanged();

        updateConfiguration(ssids);
    }

    /**
     * Loads existing gateway data into fields
     */
    private void initializeGatewayData() {
        Gateway gateway = getConfiguration().getGateway();

        if (gateway != null)
            try {
                ssids.clear();
                ssids.addAll(gateway.getSsids());
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getContentView(), e);
            }
    }

}

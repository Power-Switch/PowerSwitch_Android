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

package eu.power_switch.gui.fragment.configure_geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddGeofenceExitActionDialog;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.GeofenceConfigurationHolder;
import eu.power_switch.shared.action.Action;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureGeofenceDialogPage3ExitActions extends ConfigurationDialogPage<GeofenceConfigurationHolder> {

    public static final String KEY_ACTIONS = "actions";
    // TODO: exchange static variables for non-static ones and pass added action through intent.extra instead
    private static ArrayList<Action>         currentExitActions;
    private static ActionRecyclerViewAdapter actionRecyclerViewAdapter;
    @BindView(R.id.add_action)
    FloatingActionButton addActionFAB;
    @BindView(R.id.recyclerview_list_of_actions)
    RecyclerView         recyclerViewTimerActions;
    private BroadcastReceiver broadcastReceiver;

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context any suitable context
     */
    public static void sendActionsChangedBroadcast(Context context, ArrayList<Action> actions) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_GEOFENCE_EXIT_ACTIONS_CHANGED);
        intent.putExtra(KEY_ACTIONS, actions);

        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }

    /**
     * Used to add Actions from AddActionDialog
     *
     * @param action Action
     */
    public static void addAction(Action action) {
        currentExitActions.add(action);
        actionRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sendActionsChangedBroadcast(getContext(), currentExitActions);
            }
        };

        final Fragment fragment = this;
        addActionFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddGeofenceExitActionDialog addGeofenceExitActionDialog = new AddGeofenceExitActionDialog();
                addGeofenceExitActionDialog.setTargetFragment(fragment, 0);
                addGeofenceExitActionDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        currentExitActions = new ArrayList<>();
        actionRecyclerViewAdapter = new ActionRecyclerViewAdapter(getActivity(), currentExitActions);
        actionRecyclerViewAdapter.setOnDeleteClickListener(new ActionRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                currentExitActions.remove(position);
                actionRecyclerViewAdapter.notifyDataSetChanged();
                sendActionsChangedBroadcast(getContext(), currentExitActions);
            }
        });
        recyclerViewTimerActions.setAdapter(actionRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewTimerActions.setLayoutManager(layoutManager);

        initializeData();

        sendActionsChangedBroadcast(getContext(), currentExitActions);

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_geofence_page_3;
    }

    private void initializeData() {
        Geofence geofence = getConfiguration().getGeofence();
        if (geofence != null) {
            try {
                currentExitActions.clear();
                currentExitActions.addAll(geofence.getActions(Geofence.EventType.EXIT));
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GEOFENCE_EXIT_ACTION_ADDED);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

}

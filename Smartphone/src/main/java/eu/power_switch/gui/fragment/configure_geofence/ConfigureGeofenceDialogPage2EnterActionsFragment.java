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
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.AddGeofenceEnterActionDialog;
import eu.power_switch.gui.dialog.ConfigureGeofenceDialog;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureGeofenceDialogPage2EnterActionsFragment extends Fragment {

    private static ArrayList<Action> currentEnterActions;
    private static ActionRecyclerViewAdapter actionRecyclerViewAdapter;

    private BroadcastReceiver broadcastReceiver;
    private View rootView;

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context any suitable context
     */
    public static void sendActionsChangedBroadcast(Context context, ArrayList<Action> actions) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_GEOFENCE_ENTER_ACTIONS_CHANGED);
        intent.putExtra("actions", actions);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Used to add Actions from AddActionDialog
     *
     * @param action Action
     */
    public static void addAction(Action action) {
        currentEnterActions.add(action);
        actionRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_geofence_page_2, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sendActionsChangedBroadcast(getContext(), currentEnterActions);
            }
        };

        final Fragment fragment = this;
        FloatingActionButton addActionFAB = (FloatingActionButton) rootView.findViewById(R.id.add_action);
        addActionFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddGeofenceEnterActionDialog addGeofenceEnterActionDialog = new AddGeofenceEnterActionDialog();
                addGeofenceEnterActionDialog.setTargetFragment(fragment, 0);
                addGeofenceEnterActionDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        currentEnterActions = new ArrayList<>();
        actionRecyclerViewAdapter = new ActionRecyclerViewAdapter
                (getActivity(), currentEnterActions);
        RecyclerView recyclerViewTimerActions = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_actions);
        recyclerViewTimerActions.setAdapter(actionRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewTimerActions.setLayoutManager(layoutManager);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureGeofenceDialog.GEOFENCE_ID_KEY)) {
            long geofenceId = args.getLong(ConfigureGeofenceDialog.GEOFENCE_ID_KEY);
            initializeData(geofenceId);
        }

        sendActionsChangedBroadcast(getContext(), currentEnterActions);

        return rootView;
    }

    private void initializeData(long geofenceId) {
        try {
            currentEnterActions.clear();
            currentEnterActions.addAll(DatabaseHandler.getGeofence(geofenceId).getActions(Geofence.EventType.ENTER));
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GEOFENCE_ENTER_ACTION_ADDED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    public class ActionRecyclerViewAdapter extends RecyclerView.Adapter<ActionRecyclerViewAdapter.ViewHolder> {
        private ArrayList<Action> actions;
        private Context context;

        public ActionRecyclerViewAdapter(Context context, ArrayList<Action> actions) {
            this.actions = actions;
            this.context = context;
        }

        @Override
        public ActionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_action, parent, false);
            return new ActionRecyclerViewAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ActionRecyclerViewAdapter.ViewHolder holder, int position) {
            final Action action = actions.get(position);
            holder.action.setText(action.toString());

            holder.deleteActionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actions.remove(holder.getAdapterPosition());
                    notifyDataSetChanged();
                    sendActionsChangedBroadcast(getContext(), actions);
                }
            });
        }

        @Override
        public int getItemCount() {
            return actions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView action;
            public FloatingActionButton deleteActionFAB;

            public ViewHolder(final View itemView) {
                super(itemView);
                action = (TextView) itemView.findViewById(R.id.txt_action_description);
                deleteActionFAB = (FloatingActionButton) itemView.findViewById(R.id.delete_action_fab);
                deleteActionFAB.setImageDrawable(IconicsHelper.getDeleteIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
            }
        }
    }
}

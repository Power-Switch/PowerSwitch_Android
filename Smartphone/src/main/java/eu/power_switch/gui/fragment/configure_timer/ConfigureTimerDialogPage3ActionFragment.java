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

package eu.power_switch.gui.fragment.configure_timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.dialog.AddTimerActionDialog;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.timer.action.TimerAction;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureTimerDialogPage3ActionFragment extends Fragment {

    private static ArrayList<TimerAction> currentActions;
    private static TimerActionRecyclerViewAdapter timerActionRecyclerViewAdapter;

    private BroadcastReceiver broadcastReceiver;
    private View rootView;

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context
     */
    public static void sendTimerActionChangedBroadcast(Context context, ArrayList<TimerAction> actions) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_TIMER_ACTIONS_CHANGED);
        intent.putExtra("actions", actions);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Used to add TimerActions from "Add TimerAction" Dialog
     *
     * @param timerAction TimerAction
     */
    public static void addTimerAction(TimerAction timerAction) {
        currentActions.add(timerAction);
        timerActionRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_timer_page_3, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sendTimerActionChangedBroadcast(getContext(), currentActions);
            }
        };

        final Fragment fragment = this;
        FloatingActionButton addTimerActionFAB = (FloatingActionButton) rootView.findViewById(R.id.add_timer_action);
        addTimerActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTimerActionDialog addTimerActionDialog = new AddTimerActionDialog();
                addTimerActionDialog.setTargetFragment(fragment, 0);
                addTimerActionDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        currentActions = new ArrayList<>();
        timerActionRecyclerViewAdapter = new TimerActionRecyclerViewAdapter
                (getActivity(), currentActions);
        RecyclerView recyclerViewTimerActions = (RecyclerView) rootView.findViewById(R.id
                .recyclerview_list_of_timerActions);
        recyclerViewTimerActions.setAdapter(timerActionRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewTimerActions.setLayoutManager(layoutManager);

        Bundle args = getArguments();
        if (args != null && args.containsKey("TimerId")) {
            long timerId = args.getLong("TimerId");
            initializeTimerData(timerId);
        }

        sendTimerActionChangedBroadcast(getContext(), getCurrentTimerActions());

        return rootView;
    }

    private void initializeTimerData(long timerId) {
        currentActions.clear();
        currentActions.addAll(DatabaseHandler.getTimer(timerId).getActions());
    }

    private ArrayList<TimerAction> getCurrentTimerActions() {
        ArrayList<TimerAction> actions = new ArrayList<>();


        return actions;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_TIMER_ACTION_ADDED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    public class TimerActionRecyclerViewAdapter extends RecyclerView.Adapter<TimerActionRecyclerViewAdapter.ViewHolder> {
        private ArrayList<TimerAction> timerActions;
        private Context context;

        public TimerActionRecyclerViewAdapter(Context context, ArrayList<TimerAction> timerActions) {
            this.timerActions = timerActions;
            this.context = context;
        }

        @Override
        public TimerActionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_timer_action_dialog, parent, false);
            return new TimerActionRecyclerViewAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(TimerActionRecyclerViewAdapter.ViewHolder holder, final int position) {
            final TimerAction timerAction = timerActions.get(position);
            holder.action.setText(timerAction.toString());

            holder.deleteTimerActionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timerActions.remove(position);
                    notifyDataSetChanged();
                    sendTimerActionChangedBroadcast(getContext(), timerActions);
                }
            });
        }

        @Override
        public int getItemCount() {
            return timerActions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView action;
            public FloatingActionButton deleteTimerActionFAB;


            public ViewHolder(final View itemView) {
                super(itemView);
                action = (TextView) itemView.findViewById(R.id.txt_timer_action);
                deleteTimerActionFAB = (FloatingActionButton) itemView.findViewById(R.id.delete_timer_action_fab);
            }
        }
    }
}

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

package eu.power_switch.gui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddAlarmEventActionDialog;
import eu.power_switch.shared.constants.ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * Fragment containing all settings related to Sleep As Android alarm clock event handling
 * <p/>
 * Created by Markus on 08.10.2015.
 */
public class SleepAsAndroidFragment extends RecyclerViewFragment {

    private View rootView;

    private BroadcastReceiver broadcastReceiver;
    private ArrayList<Action> actions;
    private RecyclerView recyclerViewActions;
    private ActionRecyclerViewAdapter recyclerViewAdapter;
    private AppCompatSpinner spinnerEventType;
    private FloatingActionButton addActionFAB;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_sleep_as_android, container, false);

        final RecyclerViewFragment recyclerViewFragment = this;

        spinnerEventType = (AppCompatSpinner) rootView.findViewById(R.id.spinner_sleep_as_android_event);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sleep_as_android_event_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);
        spinnerEventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        actions = new ArrayList<>(DatabaseHandler.getAlarmActions(
                SLEEP_AS_ANDROID_ALARM_EVENT.ALARM_TRIGGERED));

        recyclerViewActions = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_alarm_event_actions);
        recyclerViewAdapter = new ActionRecyclerViewAdapter(getContext(), actions);
        recyclerViewAdapter.setOnDeleteClickListener(new ActionRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                actions.remove(position);
                DatabaseHandler.setAlarmActions(
                        SLEEP_AS_ANDROID_ALARM_EVENT.getById(spinnerEventType.getSelectedItemPosition())
                        , actions);
                StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.action_removed, Snackbar.LENGTH_LONG);

                recyclerViewAdapter.notifyDataSetChanged();
            }
        });
        recyclerViewActions.setAdapter(recyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.timer_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewActions.setLayoutManager(layoutManager);

        addActionFAB = (FloatingActionButton) rootView.findViewById(R.id.add_action_fab);
        addActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddAlarmEventActionDialog addAlarmEventActionDialog = new AddAlarmEventActionDialog();
                Bundle args = new Bundle();
                args.putInt("eventId", spinnerEventType.getSelectedItemPosition());
                addAlarmEventActionDialog.setArguments(args);
                addAlarmEventActionDialog.setTargetFragment(recyclerViewFragment, 0);
                addAlarmEventActionDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());

                if (LocalBroadcastConstants.INTENT_ALARM_EVENT_ACTION_ADDED.equals(intent.getAction())) {
                    updateUI();
                }
            }
        };

        return rootView;
    }

    private void updateUI() {
        actions.clear();
        actions.addAll(DatabaseHandler.getAlarmActions(
                SLEEP_AS_ANDROID_ALARM_EVENT.getById(spinnerEventType.getSelectedItemPosition())));

        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_ALARM_EVENT_ACTION_ADDED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewActions;
    }
}

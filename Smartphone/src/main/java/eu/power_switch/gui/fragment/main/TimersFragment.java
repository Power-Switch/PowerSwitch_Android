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

package eu.power_switch.gui.fragment.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.adapter.TimerRecyclerViewAdapter;
import eu.power_switch.gui.dialog.ConfigureTimerDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.timer.Timer;

/**
 * Fragment containing a List of all Timers
 * <p/>
 * Created by Markus on 12.09.2015.
 */
public class TimersFragment extends RecyclerViewFragment {

    private ArrayList<Timer> timers;
    private TimerRecyclerViewAdapter timerRecyclerViewAdapter;
    private RecyclerView recyclerViewTimers;
    private BroadcastReceiver broadcastReceiver;
    private View rootView;

    /**
     * Used to notify Timer Fragment (this) that Timers have changed
     *
     * @param context
     */
    public static void sendTimersChangedBroadcast(Context context) {
        Log.d("TimersFragment", "sendTimersChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_TIMER_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_timers, container, false);
        setHasOptionsMenu(true);

        final RecyclerViewFragment recyclerViewFragment = this;

        timers = new ArrayList<>();
        timerRecyclerViewAdapter = new TimerRecyclerViewAdapter(getActivity(), timers);

        recyclerViewTimers = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_timers);
        recyclerViewTimers.setAdapter(timerRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.timer_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewTimers.setLayoutManager(layoutManager);
        timerRecyclerViewAdapter.setOnItemLongClickListener(new TimerRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                final Timer timer = timers.get(position);

                ConfigureTimerDialog configureTimerDialog = new ConfigureTimerDialog();
                Bundle sceneData = new Bundle();
                sceneData.putLong("TimerId", timer.getId());
                configureTimerDialog.setArguments(sceneData);
                configureTimerDialog.setTargetFragment(recyclerViewFragment, 0);
                configureTimerDialog.show(getFragmentManager(), null);
            }
        });

        refreshTimers();

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.add_timer_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigureTimerDialog configureTimerDialog = new ConfigureTimerDialog();
                configureTimerDialog.setTargetFragment(recyclerViewFragment, 0);
                configureTimerDialog.show(getFragmentManager(), null);
            }
        });

        if (SharedPreferencesHandler.getHideAddFAB()) {
            fab.setVisibility(View.GONE);
        }

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TimersFragment", "received intent: " + intent.getAction());
                refreshTimers();
            }
        };

        return rootView;
    }

    private void refreshTimers() {
        Log.d("TimersFragment", "refreshTimers");
        timers.clear();

        if (SharedPreferencesHandler.getPlayStoreMode()) {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
            timers.addAll(playStoreModeDataModel.getTimers());
        } else {
            timers.addAll(DatabaseHandler.getAllTimers());
        }

        timerRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_timer:
                ConfigureTimerDialog configureTimerDialog = new ConfigureTimerDialog();
                configureTimerDialog.setTargetFragment(this, 0);
                configureTimerDialog.show(getFragmentManager(), null);
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timer_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_TIMER_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_SCENE_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_RECEIVER_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewTimers;
    }
}

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
import eu.power_switch.action.Action;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddTimerActionDialog;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.TimerConfigurationHolder;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.timer.Timer;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureTimerDialogPage3Action extends ConfigurationDialogPage<TimerConfigurationHolder> {

    // TODO: exchange static variables for non-static ones and pass added action through intent.extra instead
    private static ArrayList<Action>         currentActions;
    private static ActionRecyclerViewAdapter actionRecyclerViewAdapter;
    @BindView(R.id.add_timer_action)
    FloatingActionButton addTimerActionFAB;
    @BindView(R.id.recyclerview_list_of_actions)
    RecyclerView         recyclerViewTimerActions;
    private BroadcastReceiver broadcastReceiver;


    /**
     * Used to notify the setup page that some info has changed
     */
    public void updateConfiguration(ArrayList<Action> actions) {
        getConfiguration().setActions(actions);

        notifyConfigurationChanged();
    }

    /**
     * Used to notify the setup page that some info has changed
     */
    public void updateConfiguration(ArrayList<Action> actions) {
        getConfiguration().setActions(actions);

        notifyConfigurationChanged();
    }

    /**
     * Used to add TimerActions from "Add TimerAction" Dialog
     *
     * @param action TimerAction
     */
    public static void addTimerAction(Action action) {
        currentActions.add(action);
        actionRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateConfiguration(currentActions);
            }
        };

        final Fragment fragment = this;
        addTimerActionFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addTimerActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTimerActionDialog addTimerActionDialog = new AddTimerActionDialog();
                addTimerActionDialog.setTargetFragment(fragment, 0);
                addTimerActionDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        currentActions = new ArrayList<>();
        actionRecyclerViewAdapter = new ActionRecyclerViewAdapter(getActivity(), currentActions);
        actionRecyclerViewAdapter.setOnDeleteClickListener(new ActionRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                currentActions.remove(position);
                actionRecyclerViewAdapter.notifyDataSetChanged();
                updateConfiguration(currentActions);
            }
        });
        recyclerViewTimerActions.setAdapter(actionRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewTimerActions.setLayoutManager(layoutManager);

        initializeTimerData();

        updateConfiguration(currentActions);

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_timer_page_3;
    }

    private void initializeTimerData() {
        Timer timer = getConfiguration().getTimer();
        if (timer != null) {
            try {
                currentActions.clear();
                currentActions.addAll(timer.getActions());
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_TIMER_ACTION_ADDED);
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

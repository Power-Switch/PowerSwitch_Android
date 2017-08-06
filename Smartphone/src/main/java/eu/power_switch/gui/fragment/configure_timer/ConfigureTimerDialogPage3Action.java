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

package eu.power_switch.gui.fragment.configure_timer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.event.ActionAddedEvent;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddActionDialog;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.TimerConfigurationHolder;
import eu.power_switch.timer.Timer;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureTimerDialogPage3Action extends ConfigurationDialogPage<TimerConfigurationHolder> {

    @BindView(R.id.add_timer_action)
    FloatingActionButton addTimerActionFAB;
    @BindView(R.id.recyclerview_list_of_actions)
    RecyclerView         recyclerViewTimerActions;

    private List<Action> currentActions = new ArrayList<>();
    private ActionRecyclerViewAdapter actionRecyclerViewAdapter;

    @Override
    protected void onRootViewInflated(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initializeTimerData();

        final Fragment fragment = this;
        addTimerActionFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addTimerActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddActionDialog addActionDialog = AddActionDialog.newInstance(fragment);
                addActionDialog.show(getFragmentManager(), null);
            }
        });

        actionRecyclerViewAdapter = new ActionRecyclerViewAdapter(getActivity(), persistenceHandler, currentActions);
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
    }

    @Override
    protected void showTutorial() {
    }

    /**
     * Used to notify the setup page that some info has changed
     */
    public void updateConfiguration(List<Action> actions) {
        getConfiguration().setActions(actions);

        notifyConfigurationChanged();
    }

    /**
     * Used to add TimerActions from "Add TimerAction" Dialog
     *
     * @param action TimerAction
     */
    private void addTimerAction(Action action) {
        currentActions.add(action);
        actionRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_timer_page_3;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onActionAdded(ActionAddedEvent e) {
        addTimerAction(e.getAction());

        updateConfiguration(currentActions);
    }

    private void initializeTimerData() {
        Timer timer = getConfiguration().getTimer();
        if (timer != null) {
            try {
                currentActions.clear();
                currentActions.addAll(timer.getActions());

                getConfiguration().setActions(currentActions);
            } catch (Exception e) {
                statusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

}

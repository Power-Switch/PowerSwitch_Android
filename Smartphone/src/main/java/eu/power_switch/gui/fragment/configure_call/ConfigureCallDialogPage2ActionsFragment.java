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

package eu.power_switch.gui.fragment.configure_call;

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
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddCallActionDialog;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Created by Markus on 05.04.2016.
 */
public class ConfigureCallDialogPage2ActionsFragment extends ConfigurationDialogFragment {

    // TODO: exchange static variables for non-static ones and pass added action through intent.extra instead
    private static List<Action> actions = new ArrayList<>();
    private static ActionRecyclerViewAdapter actionRecyclerViewAdapter;
    private View rootView;
    private BroadcastReceiver broadcastReceiver;

    private static void sendActionsChangedBroadcast(Context context, List<Action> actions) {
        // TODO:
    }

    public static void addAction(Action action) {
        actions.add(action);
        actionRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_call_page_2, container, false);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocalBroadcastConstants.INTENT_CALL_ACTION_ADDED.equals(intent.getAction())) {
//                    actions.add((Action) intent.getSerializableExtra("action"));
                    sendActionsChangedBroadcast(getContext(), actions);
                }
            }
        };

        final Fragment fragment = this;
        FloatingActionButton addActionFAB = (FloatingActionButton) rootView.findViewById(R.id.add_action);
        addActionFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCallActionDialog addCallActionDialog = new AddCallActionDialog();
                addCallActionDialog.setTargetFragment(fragment, 0);
                addCallActionDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        actionRecyclerViewAdapter = new ActionRecyclerViewAdapter(getActivity(), actions);
        actionRecyclerViewAdapter.setOnDeleteClickListener(new ActionRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                actions.remove(position);
                actionRecyclerViewAdapter.notifyDataSetChanged();
                sendActionsChangedBroadcast(getContext(), actions);
            }
        });
        RecyclerView recyclerViewActions = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_actions);
        recyclerViewActions.setAdapter(actionRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewActions.setLayoutManager(layoutManager);

        return rootView;
    }

    private void initializeCallData(long callId) {
        try {
//            Call call = DatabaseHandler.getCall(callId);

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_CALL_ACTION_ADDED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

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

package eu.power_switch.gui.fragment.configure_call_event;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.event.CallEventActionAddedEvent;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddCallEventActionDialog;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.ConfigureCallEventDialog;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PhoneConstants;

/**
 * Created by Markus on 05.04.2016.
 */
public class ConfigureCallEventDialogPage2Actions extends ConfigurationDialogPage {

    // TODO: exchange static variables for non-static ones and pass added action through intent.extra instead
    private static ArrayList<Action> actions = new ArrayList<>();
    private static ActionRecyclerViewAdapter actionRecyclerViewAdapter;

    @BindView(R.id.recyclerview_list_of_actions)
    RecyclerView recyclerViewActions;

    private long callEventId = -1;

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context any suitable context
     */
    public static void sendActionsChangedBroadcast(Context context, ArrayList<Action> actions) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_CALL_EVENT_ACTIONS_CHANGED);
        intent.putExtra("actions", actions);

        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }

    public static void addAction(Action action) {
        actions.add(action);
        actionRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        actions.clear();

        FloatingActionButton addActionFAB = rootView.findViewById(R.id.add_action);
        addActionFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCallEventActionDialog addCallEventActionDialog = new AddCallEventActionDialog();
                addCallEventActionDialog.setTargetFragment(ConfigureCallEventDialogPage2Actions.this, 0);
                addCallEventActionDialog.show(getActivity().getSupportFragmentManager(), null);
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
        recyclerViewActions.setAdapter(actionRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewActions.setLayoutManager(layoutManager);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureCallEventDialog.CALL_EVENT_ID_KEY)) {
            callEventId = args.getLong(ConfigureCallEventDialog.CALL_EVENT_ID_KEY);
            initializeCallData(callEventId);
        }

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_call_event_page_2;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onCallEventActionAdded(CallEventActionAddedEvent callEventActionAddedEvent) {
        sendActionsChangedBroadcast(getContext(), actions);
    }

    private void initializeCallData(long callEventId) {
        try {
            CallEvent callEvent = persistanceHandler.getCallEvent(callEventId);


            actions.addAll(callEvent.getActions(PhoneConstants.CallType.INCOMING));

        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

}

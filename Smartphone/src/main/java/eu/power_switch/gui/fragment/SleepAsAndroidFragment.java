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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddAlarmEventActionDialog;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.constants.TutorialConstants;
import eu.power_switch.shared.log.Log;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

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
    private Spinner spinnerEventType;
    private FloatingActionButton addActionFAB;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_sleep_as_android, container, false);
        setHasOptionsMenu(true);

        final RecyclerViewFragment recyclerViewFragment = this;

        spinnerEventType = (Spinner) rootView.findViewById(R.id.spinner_sleep_as_android_event);
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
            public void onItemClick(View itemView, final int position) {
                new AlertDialog.Builder(getContext()).setMessage(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                actions.remove(position);
                                DatabaseHandler.setAlarmActions(
                                        SLEEP_AS_ANDROID_ALARM_EVENT.getById(spinnerEventType.getSelectedItemPosition())
                                        , actions);
                                StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.action_removed, Snackbar.LENGTH_LONG);

                                recyclerViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null).show();
            }
        });
        recyclerViewActions.setAdapter(recyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.timer_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewActions.setLayoutManager(layoutManager);

        addActionFAB = (FloatingActionButton) rootView.findViewById(R.id.add_action_fab);
        addActionFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        addActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddAlarmEventActionDialog addAlarmEventActionDialog = new AddAlarmEventActionDialog();
                Bundle args = new Bundle();
                args.putInt(AddAlarmEventActionDialog.EVENT_ID_KEY, spinnerEventType.getSelectedItemPosition());
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
                } else {
                    updateUI();
                }
            }
        };

        showTutorial();

        return rootView;
    }

    private void showTutorial() {
        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(spinnerEventType)
                .setUseAutoRadius(false)
                .setRadius(64 * 3)
                .setDismissOnTouch(true)
                .setDismissText(getString(R.string.tutorial__got_it))
                .setContentText(getString(R.string.tutorial__sleep_as_android_explanation))
                .singleUse(TutorialConstants.SLEEP_AS_ANDROID_KEY)
                .show();
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
        intentFilter.addAction(LocalBroadcastConstants.INTENT_RECEIVER_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_SCENE_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_ALARM_EVENT_ACTION_ADDED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.add_action:
                AddAlarmEventActionDialog addAlarmEventActionDialog = new AddAlarmEventActionDialog();
                Bundle args = new Bundle();
                args.putInt(AddAlarmEventActionDialog.EVENT_ID_KEY, spinnerEventType.getSelectedItemPosition());
                addAlarmEventActionDialog.setArguments(args);
                addAlarmEventActionDialog.setTargetFragment(this, 0);
                addAlarmEventActionDialog.show(getActivity().getSupportFragmentManager(), null);
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sleep_as_android_fragment_menu, menu);
        if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
            menu.findItem(R.id.add_action).setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        } else {
            menu.findItem(R.id.add_action).setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.black));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            addActionFAB.setVisibility(View.GONE);
        } else {
            addActionFAB.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewActions;
    }
}

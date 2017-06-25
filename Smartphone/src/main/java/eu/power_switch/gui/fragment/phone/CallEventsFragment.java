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

package eu.power_switch.gui.fragment.phone;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.CallEventRecyclerViewAdapter;
import eu.power_switch.gui.dialog.ConfigureCallEventDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.permission.PermissionHelper;

/**
 * Fragment holding the Call event list
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class CallEventsFragment extends RecyclerViewFragment<CallEvent> {

    private static final String[] NEEDED_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS
    };

    private List<CallEvent> callEvents = new ArrayList<>();
    private CallEventRecyclerViewAdapter callEventRecyclerViewAdapter;
    private RecyclerView recyclerViewCalls;
    private BroadcastReceiver broadcastReceiver;
    private FloatingActionButton fab;

    /**
     * Used to notify the apartment geofence page (this) that geofences have changed
     *
     * @param context any suitable context
     */
    public static void sendCallEventsChangedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_CALL_EVENTS_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onCreateViewEvent(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_call_events, container, false);

        setHasOptionsMenu(true);

        final RecyclerViewFragment recyclerViewFragment = this;

        recyclerViewCalls = rootView.findViewById(R.id.recyclerView);
        callEventRecyclerViewAdapter = new CallEventRecyclerViewAdapter(getActivity(), callEvents);
        recyclerViewCalls.setAdapter(callEventRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewCalls.setLayoutManager(layoutManager);

        callEventRecyclerViewAdapter.setOnItemLongClickListener(new CallEventRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                CallEvent callEvent = callEvents.get(position);

                ConfigureCallEventDialog configureCallEventDialog = ConfigureCallEventDialog.newInstance(callEvent.getId());
                configureCallEventDialog.setTargetFragment(recyclerViewFragment, 0);
                configureCallEventDialog.show(getFragmentManager(), null);
            }
        });

        fab = rootView.findViewById(R.id.add_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.isPhonePermissionAvailable(getContext())) {
                    PermissionHelper.showMissingPermissionDialog(getActivity(), PermissionConstants.REQUEST_CODE_PHONE_PERMISSION, NEEDED_PERMISSIONS);
                    return;
                }

                ConfigureCallEventDialog configureCallEventDialog = new ConfigureCallEventDialog();
                configureCallEventDialog.setTargetFragment(recyclerViewFragment, 0);
                configureCallEventDialog.show(getFragmentManager(), null);
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());

                switch (intent.getAction()) {
                    case LocalBroadcastConstants.INTENT_CALL_EVENTS_CHANGED:
                        refreshCalls();
                        break;
                    case LocalBroadcastConstants.INTENT_PERMISSION_CHANGED:
                        int permissionRequestCode = intent.getIntExtra(PermissionConstants.KEY_REQUEST_CODE, 0);
                        int[] result = intent.getIntArrayExtra(PermissionConstants.KEY_RESULTS);

                        if (permissionRequestCode == PermissionConstants.REQUEST_CODE_PHONE_PERMISSION) {
                            boolean allGranted = true;
                            for (int i = 0; i < result.length; i++) {
                                allGranted &= result[i] == PackageManager.PERMISSION_GRANTED;
                            }

                            if (allGranted) {
                                StatusMessageHandler.showInfoMessage(getRecyclerView(),
                                        R.string.permission_granted, Snackbar.LENGTH_SHORT);

                                sendCallEventsChangedBroadcast(context);
                            } else {
                                StatusMessageHandler.showPermissionMissingMessage(getActivity(),
                                        getRecyclerView(), PermissionConstants.REQUEST_CODE_PHONE_PERMISSION, NEEDED_PERMISSIONS);
                            }
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void onInitialized() {
        if (!PermissionHelper.isPhonePermissionAvailable(getContext()) || !PermissionHelper.isContactPermissionAvailable(getContext())) {
            showEmpty();
            StatusMessageHandler.showPermissionMissingMessage(getActivity(),
                    getRecyclerView(), PermissionConstants.REQUEST_CODE_PHONE_PERMISSION, NEEDED_PERMISSIONS);
        } else {
            refreshCalls();
        }
    }

    private void refreshCalls() {
        Log.d(this, "refreshCallEvents");
        updateListContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_call_event:
                if (!PermissionHelper.isPhonePermissionAvailable(getContext())) {
                    PermissionHelper.showMissingPermissionDialog(getActivity(), PermissionConstants.REQUEST_CODE_PHONE_PERMISSION, NEEDED_PERMISSIONS);
                    break;
                }

                ConfigureCallEventDialog configureCallEventDialog = new ConfigureCallEventDialog();
                configureCallEventDialog.setTargetFragment(this, 0);
                configureCallEventDialog.show(getFragmentManager(), null);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.call_event_fragment_menu, menu);
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.create_call_event).setIcon(IconicsHelper.getAddIcon(getActivity(), color));

        if (!SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            menu.findItem(R.id.create_call_event).setVisible(false).setEnabled(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_CALL_EVENTS_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_PERMISSION_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewCalls;
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return callEventRecyclerViewAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.geofence_grid_span_count);
    }

    @Override
    public List<CallEvent> loadListData() throws Exception {
        if (DeveloperPreferencesHandler.getPlayStoreMode()) {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
            return playStoreModeDataModel.getCallEvents();
        } else {
            return DatabaseHandler.getAllCallEvents();
        }
    }

    @Override
    protected void onListDataChanged(List<CallEvent> list) {
        callEvents.clear();
        callEvents.addAll(list);
    }
}
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
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.SmsEventRecyclerViewAdapter;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.phone.sms.SmsEvent;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.permission.PermissionHelper;

/**
 * Fragment holding the Sms Event list
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class SmsEventsFragment extends RecyclerViewFragment {

    private static final String[] NEEDED_PERMISSIONS = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS
    };

    private List<SmsEvent> smsEvents = new ArrayList<>();
    private SmsEventRecyclerViewAdapter smsEventRecyclerViewAdapter;
    private RecyclerView recyclerViewSmsEvents;
    private BroadcastReceiver broadcastReceiver;
    private FloatingActionButton fab;

    /**
     * Used to notify the apartment geofence page (this) that geofences have changed
     *
     * @param context any suitable context
     */
    public static void sendCallEventsChangedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_SMS_EVENTS_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onCreateViewEvent(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sms_events, container, false);

        setHasOptionsMenu(true);

        recyclerViewSmsEvents = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        smsEventRecyclerViewAdapter = new SmsEventRecyclerViewAdapter(getActivity(), smsEvents);
        recyclerViewSmsEvents.setAdapter(smsEventRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewSmsEvents.setLayoutManager(layoutManager);

        final RecyclerViewFragment recyclerViewFragment = this;

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.isSmsPermissionAvailable(getContext()) || !PermissionHelper.isContactPermissionAvailable(getContext())) {
                    PermissionHelper.showMissingPermissionDialog(getActivity(), PermissionConstants.REQUEST_CODE_SMS_PERMISSION, NEEDED_PERMISSIONS);
                    return;
                }

                // TODO:
//                ConfigureCallEventDialog configureCallEventDialog = new ConfigureCallEventDialog();
//                configureCallEventDialog.setTargetFragment(recyclerViewFragment, 0);
//                configureCallEventDialog.show(getFragmentManager(), null);
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());

                switch (intent.getAction()) {
                    case LocalBroadcastConstants.INTENT_SMS_EVENTS_CHANGED:
                        refreshSmsEvents();
                        break;
                    case LocalBroadcastConstants.INTENT_PERMISSION_CHANGED:
                        int permissionRequestCode = intent.getIntExtra(PermissionConstants.KEY_REQUEST_CODE, 0);
                        int[] result = intent.getIntArrayExtra(PermissionConstants.KEY_RESULTS);

                        if (permissionRequestCode == PermissionConstants.REQUEST_CODE_SMS_PERMISSION) {
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
                                        getRecyclerView(), PermissionConstants.REQUEST_CODE_SMS_PERMISSION, NEEDED_PERMISSIONS);
                            }
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void onInitialized() {
        if (!PermissionHelper.isSmsPermissionAvailable(getContext()) || !PermissionHelper.isContactPermissionAvailable(getContext())) {
            showEmpty();
            StatusMessageHandler.showPermissionMissingMessage(getActivity(),
                    getRecyclerView(),
                    PermissionConstants.REQUEST_CODE_SMS_PERMISSION,
                    Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CONTACTS);
        } else {
            refreshSmsEvents();
        }
    }

    private void refreshSmsEvents() {
        Log.d(this, "refreshSmsEvents");
        updateListContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_sms_event:
                if (!PermissionHelper.isSmsPermissionAvailable(getContext()) || !PermissionHelper.isContactPermissionAvailable(getContext())) {
                    PermissionHelper.showMissingPermissionDialog(getActivity(), PermissionConstants.REQUEST_CODE_SMS_PERMISSION, NEEDED_PERMISSIONS);
                    break;
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sms_event_fragment_menu, menu);
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.create_sms_event).setIcon(IconicsHelper.getAddIcon(getActivity(), color));

        if (!SmartphonePreferencesHandler.getUseOptionsMenuInsteadOfFAB()) {
            menu.findItem(R.id.create_sms_event).setVisible(false).setEnabled(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_SMS_EVENTS_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_PERMISSION_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.getUseOptionsMenuInsteadOfFAB()) {
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
        return recyclerViewSmsEvents;
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return smsEventRecyclerViewAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.geofence_grid_span_count);
    }

    @Override
    public List refreshListData() throws Exception {
        smsEvents.clear();

//        if (SmartphonePreferencesHandler.getPlayStoreMode()) {
//            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
//            geofences.addAll(playStoreModeDataModel.getCustomGeofences());
//        } else {

//        callEvents = DatabaseHandler.getAllCallEvents();


        return smsEvents;
    }
}
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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.event.CallEventChangedEvent;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.CallEventRecyclerViewAdapter;
import eu.power_switch.gui.dialog.configuration.ConfigureCallEventDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.event.PermissionChangedEvent;
import eu.power_switch.shared.permission.PermissionHelper;
import timber.log.Timber;

/**
 * Fragment holding the Call event list
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class CallEventsFragment extends RecyclerViewFragment<CallEvent> {

    private static final String[] NEEDED_PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};

    @BindView(R.id.add_fab)
    FloatingActionButton fab;

    private List<CallEvent> callEvents = new ArrayList<>();
    private CallEventRecyclerViewAdapter callEventRecyclerViewAdapter;


    /**
     * Used to notify the apartment geofence page (this) that geofences have changed
     */
    public static void notifyCallEventsChanged() {
        EventBus.getDefault()
                .post(new CallEventChangedEvent());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final RecyclerViewFragment recyclerViewFragment = this;

        callEventRecyclerViewAdapter = new CallEventRecyclerViewAdapter(getActivity(), callEvents);
        getRecyclerView().setAdapter(callEventRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);

        callEventRecyclerViewAdapter.setOnItemLongClickListener(new CallEventRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                CallEvent callEvent = callEvents.get(position);

                ConfigureCallEventDialog configureCallEventDialog = ConfigureCallEventDialog.newInstance(callEvent.getId());
                configureCallEventDialog.setTargetFragment(recyclerViewFragment, 0);
                configureCallEventDialog.show(getFragmentManager(), null);
            }
        });

        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.isPhonePermissionAvailable(getContext())) {
                    PermissionHelper.showMissingPermissionDialog(getActivity(),
                            PermissionConstants.REQUEST_CODE_PHONE_PERMISSION,
                            NEEDED_PERMISSIONS);
                    return;
                }

                ConfigureCallEventDialog configureCallEventDialog = new ConfigureCallEventDialog();
                configureCallEventDialog.setTargetFragment(recyclerViewFragment, 0);
                configureCallEventDialog.show(getFragmentManager(), null);
            }
        });

        if (!PermissionHelper.isPhonePermissionAvailable(getContext()) || !PermissionHelper.isContactPermissionAvailable(getContext())) {
            showEmpty();
            statusMessageHandler.showPermissionMissingMessage(getActivity(),
                    getRecyclerView(),
                    PermissionConstants.REQUEST_CODE_PHONE_PERMISSION,
                    NEEDED_PERMISSIONS);
        } else {
            refreshCalls();
        }

        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onPermissionChanged(PermissionChangedEvent permissionChangedEvent) {
        int   permissionRequestCode = permissionChangedEvent.getRequestCode();
        int[] result                = permissionChangedEvent.getGrantResults();

        if (permissionRequestCode == PermissionConstants.REQUEST_CODE_PHONE_PERMISSION) {
            boolean allGranted = true;
            for (int i = 0; i < result.length; i++) {
                allGranted &= result[i] == PackageManager.PERMISSION_GRANTED;
            }

            if (allGranted) {
                statusMessageHandler.showInfoMessage(getRecyclerView(), R.string.permission_granted, Snackbar.LENGTH_SHORT);

                notifyCallEventsChanged();
            } else {
                statusMessageHandler.showPermissionMissingMessage(getActivity(),
                        getRecyclerView(),
                        PermissionConstants.REQUEST_CODE_PHONE_PERMISSION,
                        NEEDED_PERMISSIONS);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onCallEventChanged(CallEventChangedEvent callEventChangedEvent) {
        refreshCalls();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_call_events;
    }

    private void refreshCalls() {
        Timber.d("refreshCallEvents");
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
                    PermissionHelper.showMissingPermissionDialog(getActivity(),
                            PermissionConstants.REQUEST_CODE_PHONE_PERMISSION,
                            NEEDED_PERMISSIONS);
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
        menu.findItem(R.id.create_call_event)
                .setIcon(IconicsHelper.getAddIcon(getActivity(), color));

        boolean useOptionsMenuOnly = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        if (!useOptionsMenuOnly) {
            menu.findItem(R.id.create_call_event)
                    .setVisible(false)
                    .setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
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
        return persistenceHandler.getAllCallEvents();
    }

    @Override
    protected void onListDataChanged(List<CallEvent> list) {
        callEvents.clear();
        callEvents.addAll(list);
    }
}
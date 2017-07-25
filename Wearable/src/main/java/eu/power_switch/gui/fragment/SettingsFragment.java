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

package eu.power_switch.gui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.SnapHelper;
import android.support.wearable.view.CurvedChildLayoutManager;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.SettingsListAdapter;
import eu.power_switch.gui.view.SettingsListSnapHelper;
import eu.power_switch.network.service.ListenerService;
import eu.power_switch.network.service.UtilityService;
import eu.power_switch.settings.BooleanSettingsItem;
import eu.power_switch.settings.SettingsItem;
import eu.power_switch.settings.SingleSelectSettingsItem;
import eu.power_switch.shared.constants.WearableSettingsConstants;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import timber.log.Timber;

/**
 * Fragment holding settings related to wearable
 * <p/>
 * Created by Markus on 07.06.2016.
 */
public class SettingsFragment extends FragmentBase {

    private BroadcastReceiver broadcastReceiver;

    private ArrayList<SettingsItem> settings = new ArrayList<>();
    private SettingsListAdapter settingsListAdapter;
    private boolean ownModification = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("received intent: " + intent.getAction());

                if (WearableSettingsConstants.WEARABLE_SETTINGS_CHANGED.equals(intent.getAction())) {
                    if (!ownModification) {
                        refreshUI();
                    } else {
                        ownModification = false;
                    }
                }
            }
        };

        SingleSelectSettingsItem item0 = new SingleSelectSettingsItem(getActivity(),
                IconicsHelper.getTabsIcon(getActivity()),
                R.string.title_startupDefaultTab,
                WearablePreferencesHandler.STARTUP_DEFAULT_TAB,
                R.array.wear_tab_names,
                wearablePreferencesHandler) {

        };

        SettingsItem item1 = new BooleanSettingsItem(getActivity(),
                IconicsHelper.getAutocollapseRoomsIcon(getActivity()),
                R.string.title_autoCollapseRooms,
                WearablePreferencesHandler.AUTO_COLLAPSE_ROOMS,
                wearablePreferencesHandler);
        SettingsItem item2 = new BooleanSettingsItem(getActivity(),
                IconicsHelper.getHistoryIcon(getActivity()),
                R.string.title_highlightLastActivatedButton,
                WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON,
                wearablePreferencesHandler);
        SettingsItem item3 = new BooleanSettingsItem(getActivity(),
                IconicsHelper.getVibrationIcon(getActivity()),
                R.string.title_vibrateOnButtonPress,
                WearablePreferencesHandler.VIBRATE_ON_BUTTON_PRESS,
                wearablePreferencesHandler);

        settings.add(item0);
        settings.add(item1);
        settings.add(item2);
        settings.add(item3);

        final WearableRecyclerView wearableRecyclerView = rootView.findViewById(R.id.wearableRecyclerView);
        wearableRecyclerView.setCenterEdgeItems(true);

        CurvedChildLayoutManager curvedChildLayoutManager = new CurvedChildLayoutManager(getActivity());
        wearableRecyclerView.setLayoutManager(curvedChildLayoutManager);

        SnapHelper snapHelper = new SettingsListSnapHelper();
        snapHelper.attachToRecyclerView(wearableRecyclerView);

        settingsListAdapter = new SettingsListAdapter(getActivity(), settings);
        settingsListAdapter.setOnItemClickListener(new SettingsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SettingsListAdapter.ItemViewHolder viewHolder, int position) {
                SettingsItem settingsItem = settings.get(position);
                if (settingsItem instanceof BooleanSettingsItem) {
                    BooleanSettingsItem booleanSettingsItem = (BooleanSettingsItem) settingsItem;
                    booleanSettingsItem.toggle();

                } else if (settingsItem instanceof SingleSelectSettingsItem) {
                    SingleSelectSettingsItem singleSelectSettingsItem = (SingleSelectSettingsItem) settingsItem;
                    singleSelectSettingsItem.showValueSelector();
                }

                viewHolder.value.setText(settingsItem.getCurrentValueDescription());
                ownModification = true;

                UtilityService.forceWearSettingsUpdate(getActivity());
            }
        });

        wearableRecyclerView.setAdapter(settingsListAdapter);

        refreshUI();

        return rootView;
    }

    private void refreshUI() {
        settingsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (dataApiHandler != null) {
            dataApiHandler.connect();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WearableSettingsConstants.WEARABLE_SETTINGS_CHANGED);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        if (dataApiHandler != null) {
            dataApiHandler.disconnect();
        }

        ListenerService.sendSettingsChangedBroadcast(getActivity());

        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

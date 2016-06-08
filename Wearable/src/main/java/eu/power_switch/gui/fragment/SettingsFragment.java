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

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.SettingsListAdapter;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.network.service.ListenerService;
import eu.power_switch.network.service.UtilityService;
import eu.power_switch.settings.BooleanSettingsItem;
import eu.power_switch.settings.SettingsItem;
import eu.power_switch.shared.constants.WearableSettingsConstants;
import eu.power_switch.shared.log.Log;

/**
 * Fragment holding settings related to wearable
 * <p/>
 * Created by Markus on 07.06.2016.
 */
public class SettingsFragment extends Fragment {

    private BroadcastReceiver broadcastReceiver;
    private DataApiHandler dataApiHandler;

    private ArrayList<SettingsItem> settings = new ArrayList<>();
    private SettingsListAdapter settingsListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        dataApiHandler = new DataApiHandler(getActivity());

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());

                if (WearableSettingsConstants.WEARABLE_SETTINGS_CHANGED.equals(intent.getAction())) {
                    refreshUI();
                }
            }
        };

        SettingsItem item1 = new BooleanSettingsItem(
                getActivity(),
                IconicsHelper.getCloseRoomsIcon(getActivity()),
                R.string.auto_collapse_rooms,
                WearableSettingsConstants.KEY_AUTO_COLLAPSE_ROOMS,
                false);
        SettingsItem item2 = new BooleanSettingsItem(
                getActivity(),
                IconicsHelper.getHistoryIcon(getActivity()),
                R.string.highlight_last_activated_button,
                WearableSettingsConstants.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON,
                false);
        SettingsItem item3 = new BooleanSettingsItem(
                getActivity(),
                IconicsHelper.getVibrationIcon(getActivity()),
                R.string.vibrate_on_button_press,
                WearableSettingsConstants.KEY_VIBRATE_ON_BUTTON_PRESS,
                false);

        settings.add(item1);
        settings.add(item2);
        settings.add(item3);

        final WearableListView wearableListView = (WearableListView) rootView.findViewById(R.id.wearable_List);
        settingsListAdapter = new SettingsListAdapter(getActivity(), settings);
        wearableListView.setAdapter(settingsListAdapter);
        wearableListView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                SettingsItem settingsItem = settings.get(viewHolder.getAdapterPosition());
                if (settingsItem instanceof BooleanSettingsItem) {
                    BooleanSettingsItem booleanSettingsItem = (BooleanSettingsItem) settingsItem;
                    booleanSettingsItem.toggle();
                }

                ListenerService.sendSettingsChangedBroadcast(getActivity());
                wearableListView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());

                UtilityService.forceWearSettingsUpdate(getActivity());
            }

            @Override
            public void onTopEmptyRegionClick() {

            }
        });
        wearableListView.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onScroll(int i) {

            }

            @Override
            public void onAbsoluteScrollChange(int i) {

            }

            @Override
            public void onScrollStateChanged(int i) {

            }

            @Override
            public void onCentralPositionChanged(int i) {

            }
        });


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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        if (dataApiHandler != null) {
            dataApiHandler.disconnect();
        }

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

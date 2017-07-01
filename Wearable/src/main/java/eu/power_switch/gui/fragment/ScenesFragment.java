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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import eu.power_switch.R;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.adapter.SceneRecyclerViewAdapter;
import eu.power_switch.gui.animation.SnappingLinearLayoutManager;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.shared.constants.WearableSettingsConstants;
import timber.log.Timber;

/**
 * Fragment holding all scenes
 * <p/>
 * Created by Markus on 07.06.2016.
 */
public class ScenesFragment extends Fragment {

    private static final String REFRESH_VIEW = "eu.power_switch.scenes.refresh_view";

    private RecyclerView             scenesRecyclerView;
    private SceneRecyclerViewAdapter sceneRecyclerViewAdapter;
    private DataApiHandler           dataApiHandler;

    private BroadcastReceiver broadcastReceiver;
    private LinearLayout      layoutLoading;
    private LinearLayout      layoutEmpty;

    /**
     * Used to notify Scenes Fragment (this) that data has changed
     *
     * @param context any suitable context
     */
    public static void notifyDataChanged(Context context) {
        Intent intent = new Intent(REFRESH_VIEW);

        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scenes, container, false);

        dataApiHandler = new DataApiHandler(getActivity());

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("received intent: " + intent.getAction());

                if (REFRESH_VIEW.equals(intent.getAction())) {
                    refreshUI();
                } else if (WearableSettingsConstants.WEARABLE_SETTINGS_CHANGED.equals(intent.getAction())) {
                    refreshUI();
                }
            }
        };

        layoutLoading = rootView.findViewById(R.id.layoutLoading);

        layoutEmpty = rootView.findViewById(R.id.layoutEmpty);
        layoutEmpty.setVisibility(View.GONE);

        scenesRecyclerView = rootView.findViewById(R.id.scenes_recyclerView);
        sceneRecyclerViewAdapter = new SceneRecyclerViewAdapter(getActivity(), scenesRecyclerView, MainActivity.sceneList, dataApiHandler);
        scenesRecyclerView.setAdapter(sceneRecyclerViewAdapter);

        SnappingLinearLayoutManager layoutManager = new SnappingLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        scenesRecyclerView.setLayoutManager(layoutManager);

        if (MainActivity.isInitialized()) {
            refreshUI();
        }

        return rootView;
    }

    private void refreshUI() {
        sceneRecyclerViewAdapter.notifyDataSetChanged();

        if (MainActivity.sceneList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            scenesRecyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            scenesRecyclerView.setVisibility(View.VISIBLE);
        }
        layoutLoading.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (dataApiHandler != null) {
            dataApiHandler.connect();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REFRESH_VIEW);
        intentFilter.addAction(WearableSettingsConstants.WEARABLE_SETTINGS_CHANGED);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        if (dataApiHandler != null) {
            dataApiHandler.disconnect();
        }

        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

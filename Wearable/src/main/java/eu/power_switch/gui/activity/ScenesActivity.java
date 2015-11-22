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

package eu.power_switch.gui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.gui.adapter.SceneRecyclerViewAdapter;
import eu.power_switch.gui.animation.SnappingLinearLayoutManager;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.network.service.ListenerService;
import eu.power_switch.obj.Scene;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Created by Markus on 26.08.2015.
 */
public class ScenesActivity extends WearableActivity {

    private RecyclerView scenesRecyclerView;
    private SceneRecyclerViewAdapter sceneRecyclerViewAdapter;
    private ArrayList<Scene> sceneList = new ArrayList<>();

    private DataApiHandler dataApiHandler;

    private BroadcastReceiver broadcastReceiver;
    private RelativeLayout relativeLayoutAmbientMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate
        WearablePreferencesHandler wearablePreferencesHandler = new WearablePreferencesHandler(getApplicationContext());
        switch (wearablePreferencesHandler.getTheme()) {
            case SettingsConstants.THEME_DARK_BLUE:
                setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
                setTheme(R.style.PowerSwitchWearTheme_Dark_Red);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                setTheme(R.style.PowerSwitchWearTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_RED:
                setTheme(R.style.PowerSwitchWearTheme_Light_Red);
                break;
            default:
                setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenes);

        // allow always-on screen
        setAmbientEnabled();

        dataApiHandler = new DataApiHandler(getApplicationContext());

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("MainActivity", "received intent: " + intent.getAction());

                ArrayList<Scene> scenes = (ArrayList<Scene>) intent.getSerializableExtra(ListenerService.SCENE_DATA);
                replaceSceneList(scenes);

                refreshUI();
            }
        };

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                relativeLayoutAmbientMode = (RelativeLayout) findViewById(R.id.relativeLayout_ambientMode);

                scenesRecyclerView = (RecyclerView) findViewById(R.id.scenes_recyclerView);
                sceneRecyclerViewAdapter = new SceneRecyclerViewAdapter(getApplicationContext(), scenesRecyclerView,
                        sceneList, dataApiHandler);
                scenesRecyclerView.setAdapter(sceneRecyclerViewAdapter);

                SnappingLinearLayoutManager layoutManager = new SnappingLinearLayoutManager(getApplicationContext(),
                        LinearLayoutManager.VERTICAL, false);
                scenesRecyclerView.setLayoutManager(layoutManager);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (dataApiHandler != null) {
            dataApiHandler.connect();
        }

        try {
            ArrayList<Scene> newScenes = (ArrayList<Scene>) getIntent().getSerializableExtra(ListenerService.SCENE_DATA);
            replaceSceneList(newScenes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(ListenerService.DATA_UPDATED)
        );
    }

    @Override
    protected void onStop() {
        if (dataApiHandler != null) {
            dataApiHandler.disconnect();
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        scenesRecyclerView.setVisibility(View.INVISIBLE);
        relativeLayoutAmbientMode.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExitAmbient() {
        scenesRecyclerView.setVisibility(View.VISIBLE);
        relativeLayoutAmbientMode.setVisibility(View.GONE);
        super.onExitAmbient();
    }

    private void refreshUI() {
        if (sceneList.isEmpty()) {
            finish();
        } else {
            // TODO
        }
        sceneRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void replaceSceneList(ArrayList<Scene> scenes) {
        sceneList.clear();
        sceneList.addAll(scenes);
    }

}

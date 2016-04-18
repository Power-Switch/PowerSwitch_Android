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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.gui.ThemeHelper;
import eu.power_switch.gui.adapter.RoomRecyclerViewAdapter;
import eu.power_switch.gui.animation.SnappingLinearLayoutManager;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.network.service.ListenerService;
import eu.power_switch.obj.Room;
import eu.power_switch.shared.constants.WearableSettingsConstants;
import eu.power_switch.shared.log.Log;

/**
 * Created by Markus on 26.08.2015.
 */
public class RoomsActivity extends WearableActivity {

    private RecyclerView roomsRecyclerView;
    private RoomRecyclerViewAdapter roomsRecyclerViewAdapter;
    private ArrayList<Room> roomList = new ArrayList<>();

    private DataApiHandler dataApiHandler;

    private BroadcastReceiver broadcastReceiver;
    private RelativeLayout relativeLayoutAmbientMode;
    private LinearLayout layoutLoading;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate
        ThemeHelper.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        // allow always-on screen
        setAmbientEnabled();

        dataApiHandler = new DataApiHandler(getApplicationContext());

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());

                if (ListenerService.DATA_UPDATED.equals(intent.getAction())) {
                    // set intent on activity
                    setIntent(intent);

                    refreshUI();
                } else if (WearableSettingsConstants.WEARABLE_SETTINGS_CHANGED.equals(intent.getAction())) {
                    refreshUI();
                } else if (WearableSettingsConstants.WEARABLE_THEME_CHANGED.equals(intent.getAction())) {
                    finish();
                    Intent restartActivityIntent = new Intent(getApplicationContext(), RoomsActivity.class);
                    restartActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    restartActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    // add current extras
                    restartActivityIntent.putExtras(getIntent().getExtras());
                    startActivity(restartActivityIntent);
                }
            }
        };

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {

            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                relativeLayoutAmbientMode = (RelativeLayout) findViewById(R.id.relativeLayout_ambientMode);

                layoutLoading = (LinearLayout) findViewById(R.id.layoutLoading);

                layoutEmpty = (LinearLayout) findViewById(R.id.layoutEmpty);
                layoutEmpty.setVisibility(View.GONE);

                roomsRecyclerView = (RecyclerView) findViewById(R.id.rooms_recyclerView);
                roomsRecyclerViewAdapter = new RoomRecyclerViewAdapter(stub.getContext(), roomsRecyclerView,
                        roomList, dataApiHandler);
                roomsRecyclerView.setAdapter(roomsRecyclerViewAdapter);

                SnappingLinearLayoutManager layoutManager = new SnappingLinearLayoutManager(getApplicationContext(),
                        LinearLayoutManager.VERTICAL, false);
                roomsRecyclerView.setLayoutManager(layoutManager);

                refreshUI();
            }
        });
    }

    private void refreshUI() {
        if (!isAmbient()) {
            layoutEmpty.setVisibility(View.GONE);
            roomsRecyclerView.setVisibility(View.GONE);
            layoutLoading.setVisibility(View.VISIBLE);
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<Room> newRooms = (ArrayList<Room>) getIntent().getSerializableExtra(ListenerService.KEY_ROOM_DATA);

                roomList.clear();
                roomList.addAll(newRooms);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                roomsRecyclerViewAdapter.notifyDataSetChanged();

                if (!isAmbient()) {
                    if (roomList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        roomsRecyclerView.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        roomsRecyclerView.setVisibility(View.VISIBLE);
                    }
                    layoutLoading.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (dataApiHandler != null) {
            dataApiHandler.connect();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ListenerService.DATA_UPDATED);
        intentFilter.addAction(WearableSettingsConstants.WEARABLE_SETTINGS_CHANGED);
        intentFilter.addAction(WearableSettingsConstants.WEARABLE_THEME_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
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

        layoutEmpty.setVisibility(View.GONE);
        roomsRecyclerView.setVisibility(View.GONE);
        relativeLayoutAmbientMode.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExitAmbient() {
        if (roomList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            roomsRecyclerView.setVisibility(View.VISIBLE);
        }
        relativeLayoutAmbientMode.setVisibility(View.GONE);
        super.onExitAmbient();
    }
}

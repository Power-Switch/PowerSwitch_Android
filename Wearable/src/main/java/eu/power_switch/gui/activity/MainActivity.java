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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.network.service.ListenerService;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Main Activity holding all app related views
 */
public class MainActivity extends WearableActivity {

    private ArrayList<Room> roomList = new ArrayList<>();
    private ArrayList<Scene> sceneList = new ArrayList<>();

    private DataApiHandler dataApiHandler;
    private BroadcastReceiver broadcastReceiver;

    private TextView textViewStatus;
    private RelativeLayout relativeLayoutStatus;

//    private DismissOverlayView dismissOverlayView;
//    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate
        WearablePreferencesHandler wearablePreferencesHandler = new WearablePreferencesHandler(getApplicationContext());
        switch (wearablePreferencesHandler.getTheme()) {
            case SettingsConstants.THEME_DARK_BLUE:
                getApplicationContext().setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
                getApplicationContext().setTheme(R.style.PowerSwitchWearTheme_Dark_Red);
                setTheme(R.style.PowerSwitchWearTheme_Dark_Red);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                getApplicationContext().setTheme(R.style.PowerSwitchWearTheme_Light_Blue);
                setTheme(R.style.PowerSwitchWearTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_RED:
                getApplicationContext().setTheme(R.style.PowerSwitchWearTheme_Light_Red);
                setTheme(R.style.PowerSwitchWearTheme_Light_Red);
                break;
            default:
                getApplicationContext().setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // allow always-on screen
        setAmbientEnabled();

        dataApiHandler = new DataApiHandler(getApplicationContext());

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("MainActivity", "received intent: " + intent.getAction());

                if (ListenerService.DATA_UPDATED.equals(intent.getAction())) {
                    ArrayList<Room> rooms = (ArrayList<Room>) intent.getSerializableExtra(ListenerService.ROOM_DATA);
                    replaceRoomList(rooms);

                    ArrayList<Scene> scenes = (ArrayList<Scene>) intent.getSerializableExtra(ListenerService.SCENE_DATA);
                    replaceSceneList(scenes);
                }

                refreshUI();
            }
        };

//        // Obtain the DismissOverlayView element
//        dismissOverlayView = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
//        dismissOverlayView.setIntroText(R.string.long_press_intro);
//        dismissOverlayView.showIntroIfNecessary();
//
//        // Configure a gesture detector
//        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
//            public void onLongPress(MotionEvent ev) {
//                dismissOverlayView.show();
//            }
//        });

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                // Status layout
                relativeLayoutStatus = (RelativeLayout) findViewById(R.id.relativeLayout_status);
                textViewStatus = (TextView) findViewById(R.id.textView_Status);


                Button rooms = (Button) findViewById(R.id.button_rooms);
                rooms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent startIntent = new Intent(getApplicationContext(), RoomsActivity.class);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startIntent.putExtra(ListenerService.ROOM_DATA, roomList);
                        startActivity(startIntent);
                    }
                });

                Button scenes = (Button) findViewById(R.id.button_scenes);
                scenes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent startIntent = new Intent(getApplicationContext(), ScenesActivity.class);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startIntent.putExtra(ListenerService.SCENE_DATA, sceneList);
                        startActivity(startIntent);
                    }
                });

                // Get Room/Receiver/Button configuration from Smartphone App
                new FetchDataAsyncTask().execute();
            }
        });
    }

//    // Capture long presses
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        return gestureDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
//    }

    @Override
    protected void onStart() {
        super.onStart();
        if (dataApiHandler != null) {
            dataApiHandler.connect();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ListenerService.DATA_UPDATED);
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

    private void refreshUI() {
        if (roomList.isEmpty()) {
            textViewStatus.setText(R.string.please_create_receivers_on_your_smartphone_first);

            relativeLayoutStatus.setVisibility(View.VISIBLE);
        } else {
            relativeLayoutStatus.setVisibility(View.GONE);
        }
    }

    private void replaceRoomList(ArrayList<Room> list) {
        roomList.clear();
        roomList.addAll(list);
    }

    private void replaceSceneList(ArrayList<Scene> scenes) {
        sceneList.clear();
        sceneList.addAll(scenes);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);

        textViewStatus.setVisibility(View.GONE);
        relativeLayoutStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExitAmbient() {
        if (roomList.isEmpty()) {
            relativeLayoutStatus.setVisibility(View.VISIBLE);
            textViewStatus.setVisibility(View.VISIBLE);
        } else {
            relativeLayoutStatus.setVisibility(View.GONE);
        }

        super.onExitAmbient();
    }

    /**
     * A background task to load the room, receiver and scene data via the Wear DataApi.
     * <p/>
     * Created by Markus on 07.06.2015.
     */
    private class FetchDataAsyncTask extends
            AsyncTask<Uri, Void, ArrayList<Object>> {

        @Override
        protected ArrayList<Object> doInBackground(Uri... params) {
            // Get Room Data from Smartphone App
            ArrayList<Room> rooms = dataApiHandler.getRoomData();
            WearablePreferencesHandler wearablePreferencesHandler = new WearablePreferencesHandler
                    (getApplicationContext());
            boolean autoCollapseRooms = wearablePreferencesHandler.getAutoCollapseRooms();
            for (Room room : rooms) {
                room.setCollapsed(autoCollapseRooms);
            }

            // Get Scene Data from Smartphone App
            ArrayList<Scene> scenes = dataApiHandler.getSceneData();

            // Get Wearable Settings from Smartphone App
            dataApiHandler.updateSettings(getApplicationContext());

            ArrayList<Object> result = new ArrayList<>();
            result.add(rooms);
            result.add(scenes);

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Object> result) {
            if (result != null) {
                // Update UI based on the result of the background processing
                replaceRoomList((ArrayList<Room>) result.get(0));
                replaceSceneList((ArrayList<Scene>) result.get(1));
            }
            refreshUI();
        }
    }
}
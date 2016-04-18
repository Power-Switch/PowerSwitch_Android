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

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.gui.ThemeHelper;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.network.service.ListenerService;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.shared.constants.WearableSettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Main Activity holding all app related views
 */
public class MainActivity extends WearableActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private ArrayList<Room> roomList = new ArrayList<>();
    private ArrayList<Scene> sceneList = new ArrayList<>();

    private DataApiHandler dataApiHandler;
    private BroadcastReceiver broadcastReceiver;

    private TextView textViewStatus;
    private RelativeLayout relativeLayoutStatus;
    private TextView textViewApartmet;
    private LinearLayout contentLayout;

//    private DismissOverlayView dismissOverlayView;
//    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate
        ThemeHelper.applyTheme(this);

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
                    String apartmentName = intent.getStringExtra(ListenerService.KEY_APARTMENT_DATA);
                    textViewApartmet.setText(apartmentName);

                    ArrayList<Room> rooms = (ArrayList<Room>) intent.getSerializableExtra(ListenerService.KEY_ROOM_DATA);
                    replaceRoomList(rooms);

                    ArrayList<Scene> scenes = (ArrayList<Scene>) intent.getSerializableExtra(ListenerService.KEY_SCENE_DATA);
                    replaceSceneList(scenes);

                    refreshUI();
                } else if (WearableSettingsConstants.WEARABLE_THEME_CHANGED.equals(intent.getAction())) {
                    finish();
                    Intent restartActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                    restartActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    restartActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    restartActivityIntent.putExtra(ListenerService.KEY_APARTMENT_DATA, textViewApartmet.getText().toString());
                    restartActivityIntent.putExtra(ListenerService.KEY_ROOM_DATA, roomList);
                    restartActivityIntent.putExtra(ListenerService.KEY_SCENE_DATA, sceneList);
                    startActivity(restartActivityIntent);
                }
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

                contentLayout = (LinearLayout) findViewById(R.id.contentLayout);

                textViewApartmet = (TextView) findViewById(R.id.textView_apartment);

                Button buttonRooms = (Button) findViewById(R.id.button_rooms);
                buttonRooms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent startIntent = new Intent(getApplicationContext(), RoomsActivity.class);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startIntent.putExtra(ListenerService.KEY_ROOM_DATA, roomList);
                        startActivity(startIntent);
                    }
                });

                Button buttonScenes = (Button) findViewById(R.id.button_scenes);
                buttonScenes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent startIntent = new Intent(getApplicationContext(), ScenesActivity.class);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startIntent.putExtra(ListenerService.KEY_SCENE_DATA, sceneList);
                        startActivity(startIntent);
                    }
                });

                if (getIntent().hasExtra(ListenerService.KEY_APARTMENT_DATA) &&
                        getIntent().hasExtra(ListenerService.KEY_ROOM_DATA) &&
                        getIntent().hasExtra(ListenerService.KEY_SCENE_DATA)) {
                    String apartmentName = getIntent().getStringExtra(ListenerService.KEY_APARTMENT_DATA);
                    textViewApartmet.setText(apartmentName);

                    ArrayList<Room> roomArrayList = (ArrayList<Room>) getIntent().getSerializableExtra(ListenerService.KEY_ROOM_DATA);
                    replaceRoomList(roomArrayList);

                    ArrayList<Scene> sceneArrayList = (ArrayList<Scene>) getIntent().getSerializableExtra(ListenerService.KEY_SCENE_DATA);
                    replaceSceneList(sceneArrayList);

                    refreshUI();
                } else {
                    // Get Room/Receiver/Button/Scene configuration from Smartphone App
                    new FetchDataAsyncTask().execute();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//                // No explanation needed, we can request the permission.

            Log.d("Write external storage permission is missing, asking for it...");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
//            }
        } else {
            Log.d("Write external storage permission already granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Write external storage permission GRANTED");
                } else {
                    Log.d("Write external storage permission DENIED");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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

    private void refreshUI() {
        if (!isAmbient()) {
            if (roomList.isEmpty()) {
                contentLayout.setVisibility(View.GONE);
                textViewStatus.setText(R.string.please_create_receivers_on_your_smartphone_first);
                textViewStatus.setVisibility(View.VISIBLE);
                relativeLayoutStatus.setVisibility(View.VISIBLE);
            } else {
                contentLayout.setVisibility(View.VISIBLE);
                relativeLayoutStatus.setVisibility(View.GONE);
            }
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
        contentLayout.setVisibility(View.GONE);
    }

    @Override
    public void onExitAmbient() {
        if (roomList.isEmpty()) {
            relativeLayoutStatus.setVisibility(View.VISIBLE);
            textViewStatus.setText(R.string.please_create_receivers_on_your_smartphone_first);
            textViewStatus.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            contentLayout.setVisibility(View.VISIBLE);
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
            // Get Apartment Data from Smartphone App
            String apartmentName = dataApiHandler.getApartmentName();
            ArrayList<String> apartments = new ArrayList<>();
            apartments.add(apartmentName);

            // Get Room Data from Smartphone App
            ArrayList<Room> rooms = dataApiHandler.getRoomData();
            boolean autoCollapseRooms = WearablePreferencesHandler.getAutoCollapseRooms();
            for (Room room : rooms) {
                room.setCollapsed(autoCollapseRooms);
            }

            // Get Scene Data from Smartphone App
            ArrayList<Scene> scenes = dataApiHandler.getSceneData();

            // Get Wearable Settings from Smartphone App
            dataApiHandler.updateSettings();

            ArrayList<Object> result = new ArrayList<>();
            result.add(apartments);
            result.add(rooms);
            result.add(scenes);

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Object> result) {
            if (result != null) {
                // Update UI based on the result of the background processing
                textViewApartmet.setText(((ArrayList<String>) result.get(0)).get(0));
                replaceRoomList((ArrayList<Room>) result.get(1));
                replaceSceneList((ArrayList<Scene>) result.get(2));
            }
            refreshUI();
        }
    }
}
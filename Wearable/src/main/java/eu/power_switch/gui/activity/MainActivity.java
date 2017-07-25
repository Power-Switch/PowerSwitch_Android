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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.dagger.android.DaggerWearableActivity;
import eu.power_switch.gui.adapter.NavigationDrawerAdapter;
import eu.power_switch.gui.fragment.RoomsFragment;
import eu.power_switch.gui.fragment.ScenesFragment;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.network.service.ListenerService;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.shared.constants.WearableSettingsConstants;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import timber.log.Timber;

/**
 * Main Activity holding all app related views
 */
public class MainActivity extends DaggerWearableActivity implements WearableActionDrawer.OnMenuItemClickListener {

    private static final int         MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    public static        String      apartmentName                                 = "";
    public static        List<Room>  roomList                                      = new ArrayList<>();
    public static        List<Scene> sceneList                                     = new ArrayList<>();
    private static       boolean     isInitialized                                 = false;
    private DataApiHandler    dataApiHandler;
    private BroadcastReceiver broadcastReceiver;

    private WearableDrawerLayout     mWearableDrawerLayout;
    private WearableNavigationDrawer mWearableNavigationDrawer;
    private WearableActionDrawer     mWearableActionDrawer;

    private TextView       textViewStatus;
    private RelativeLayout relativeLayoutStatus;
    private FrameLayout    contentFrameLayout;


//    private DismissOverlayView dismissOverlayView;
//    private GestureDetector gestureDetector;

    public static boolean isInitialized() {
        return isInitialized;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // allow always-on screen
        setAmbientEnabled();

        dataApiHandler = new DataApiHandler(getApplicationContext());

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("MainActivity", "received intent: " + intent.getAction());

                if (ListenerService.DATA_UPDATED.equals(intent.getAction())) {
                    apartmentName = intent.getStringExtra(ListenerService.KEY_APARTMENT_DATA);

                    ArrayList<Room> rooms = (ArrayList<Room>) intent.getSerializableExtra(ListenerService.KEY_ROOM_DATA);
                    roomList.clear();
                    roomList.addAll(rooms);

                    RoomsFragment.notifyDataChanged(getApplicationContext());

                    ArrayList<Scene> scenes = (ArrayList<Scene>) intent.getSerializableExtra(ListenerService.KEY_SCENE_DATA);
                    sceneList.clear();
                    sceneList.addAll(scenes);

                    ScenesFragment.notifyDataChanged(getApplicationContext());
                } else if (WearableSettingsConstants.WEARABLE_THEME_CHANGED.equals(intent.getAction())) {
                    finish();
                    Intent restartActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                    restartActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    restartActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

        // Main Wearable Drawer Layout that wraps all content
        mWearableDrawerLayout = findViewById(R.id.drawer_layout);
        contentFrameLayout = findViewById(R.id.content_frame);

        // Top Navigation Drawer
        mWearableNavigationDrawer = findViewById(R.id.top_navigation_drawer);
        NavigationDrawerAdapter navigationDrawerAdapter = new NavigationDrawerAdapter(this);
        mWearableNavigationDrawer.setAdapter(navigationDrawerAdapter);

        // load first fragment
        int index = wearablePreferencesHandler.getValue(WearablePreferencesHandler.STARTUP_DEFAULT_TAB);
        navigationDrawerAdapter.onItemSelected(index);
        // TODO: Refresh Navigation drawer

        // Peeks Navigation drawer on the top.
        mWearableDrawerLayout.peekDrawer(Gravity.TOP);

//        // Bottom Action Drawer
//        mWearableActionDrawer = (WearableActionDrawer) findViewById(R.id.bottom_action_drawer);

//        // Populate Action Drawer Menu
//        Menu menu = mWearableActionDrawer.getMenu();
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.action_drawer_menu, menu);
//        mWearableActionDrawer.setOnMenuItemClickListener(this);

//        // Peeks action drawer on the bottom.
//        mWearableDrawerLayout.peekDrawer(Gravity.BOTTOM);

        // Status layout
        relativeLayoutStatus = findViewById(R.id.relativeLayout_status);
        textViewStatus = findViewById(R.id.textView_Status);

        // Get Room/Receiver/Button/Scene configuration from Smartphone App
        new FetchDataAsyncTask().execute();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//                // No explanation needed, we can request the permission.

            Timber.d("Write external storage permission is missing, asking for it...");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
//            }
        } else {
            Timber.d("Write external storage permission already granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Timber.d("Write external storage permission GRANTED");
                } else {
                    Timber.d("Write external storage permission DENIED");
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
        intentFilter.addAction(WearableSettingsConstants.WEARABLE_THEME_CHANGED);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        if (dataApiHandler != null) {
            dataApiHandler.disconnect();
        }

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);

        contentFrameLayout.setVisibility(View.GONE);
        relativeLayoutStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExitAmbient() {
        contentFrameLayout.setVisibility(View.VISIBLE);
        relativeLayoutStatus.setVisibility(View.GONE);

        super.onExitAmbient();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        final int itemId = menuItem.getItemId();

        switch (itemId) {
//            case R.id.menu_planet_name:
//                toastMessage = mSolarSystem.get(mSelectedPlanet).getName();
//                break;
//            case R.id.menu_surface_area:
//                toastMessage = mSolarSystem.get(mSelectedPlanet).getSurfaceArea();
//                break;
        }

        mWearableDrawerLayout.closeDrawer(mWearableActionDrawer);

        return false;
    }

    /**
     * A background task to load the room, receiver and scene data via the Wear DataApi.
     * <p/>
     * Created by Markus on 07.06.2015.
     */
    private class FetchDataAsyncTask extends AsyncTask<Uri, Void, List<Object>> {

        @Override
        protected List<Object> doInBackground(Uri... params) {
            List<Object> result = new ArrayList<>();

            // Get Apartment Data from Smartphone App
            String       apartmentName  = dataApiHandler.getApartmentName();
            List<String> apartmentNames = new ArrayList<>();
            apartmentNames.add(apartmentName);

            result.add(apartmentNames);

            // Get Room Data from Smartphone App
            List<Room> rooms = dataApiHandler.getRoomData();
            if (rooms != null) {
                boolean autoCollapseRooms = wearablePreferencesHandler.getValue(WearablePreferencesHandler.AUTO_COLLAPSE_ROOMS);
                for (Room room : rooms) {
                    room.setCollapsed(autoCollapseRooms);
                }

                result.add(rooms);
            } else {
                result.add(new ArrayList<>());
            }

            // Get Scene Data from Smartphone App
            List<Scene> scenes = dataApiHandler.getSceneData();
            if (scenes != null) {
                result.add(scenes);
            } else {
                result.add(new ArrayList<>());
            }

            // Get Wearable Settings from Smartphone App
            dataApiHandler.updateSettings();

            return result;
        }

        @Override
        protected void onPostExecute(List<Object> result) {
            if (result != null) {
                // Update UI based on the result of the background processing
//                textViewApartmet.setText(((ArrayList<String>) result.get(0)).get(0));

                apartmentName = ((List<String>) result.get(0)).get(0);

                roomList.clear();
                List<Room> rooms = (List<Room>) result.get(1);
                roomList.addAll(rooms);

                RoomsFragment.notifyDataChanged(getApplicationContext());

                sceneList.clear();
                List<Scene> scenes = (List<Scene>) result.get(2);
                sceneList.addAll(scenes);

                ScenesFragment.notifyDataChanged(getApplicationContext());

                textViewStatus.setVisibility(View.GONE);
                if (!isAmbient()) {
                    contentFrameLayout.setVisibility(View.VISIBLE);
                    relativeLayoutStatus.setVisibility(View.GONE);
                }

                isInitialized = true;
            } else {
                Toast.makeText(getApplicationContext(), R.string.unknown_error, Toast.LENGTH_LONG)
                        .show();
                isInitialized = true;
            }
        }
    }
}
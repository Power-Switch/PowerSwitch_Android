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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
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

import eu.power_switch.R;
import eu.power_switch.gui.WearableThemeHelper;
import eu.power_switch.gui.adapter.NavigationDrawerAdapter;
import eu.power_switch.gui.fragment.RoomsFragment;
import eu.power_switch.gui.fragment.ScenesFragment;
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
public class MainActivity extends WearableActivity implements WearableActionDrawer.OnMenuItemClickListener {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    public static String apartmentName = "";
    public static ArrayList<Room> roomList = new ArrayList<>();
    public static ArrayList<Scene> sceneList = new ArrayList<>();
    private static boolean isInitialized = false;
    private DataApiHandler dataApiHandler;
    private BroadcastReceiver broadcastReceiver;

    private WearableDrawerLayout mWearableDrawerLayout;
    private WearableNavigationDrawer mWearableNavigationDrawer;
    private WearableActionDrawer mWearableActionDrawer;

    private TextView textViewStatus;
    private RelativeLayout relativeLayoutStatus;
    private FrameLayout contentFrameLayout;

//    private DismissOverlayView dismissOverlayView;
//    private GestureDetector gestureDetector;

    public static boolean isInitialized() {
        return isInitialized;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // set Theme before anything else in onCreate
        WearableThemeHelper.applyTheme(this);

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
        mWearableDrawerLayout = (WearableDrawerLayout) findViewById(R.id.drawer_layout);
        contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);

        // Top Navigation Drawer
        mWearableNavigationDrawer = (WearableNavigationDrawer) findViewById(R.id.top_navigation_drawer);
        NavigationDrawerAdapter navigationDrawerAdapter = new NavigationDrawerAdapter(this);
        mWearableNavigationDrawer.setAdapter(navigationDrawerAdapter);

        // load first fragment
        navigationDrawerAdapter.onItemSelected(WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB));
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
        relativeLayoutStatus = (RelativeLayout) findViewById(R.id.relativeLayout_status);
        textViewStatus = (TextView) findViewById(R.id.textView_Status);

        // Get Room/Receiver/Button/Scene configuration from Smartphone App
        new FetchDataAsyncTask().execute();

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
            boolean autoCollapseRooms = WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS);
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
//                textViewApartmet.setText(((ArrayList<String>) result.get(0)).get(0));

                apartmentName = ((ArrayList<String>) result.get(0)).get(0);

                roomList.clear();
                roomList.addAll((ArrayList<Room>) result.get(1));

                RoomsFragment.notifyDataChanged(getApplicationContext());

                sceneList.clear();
                sceneList.addAll((ArrayList<Scene>) result.get(2));

                ScenesFragment.notifyDataChanged(getApplicationContext());

                textViewStatus.setVisibility(View.GONE);
                if (!isAmbient()) {
                    contentFrameLayout.setVisibility(View.VISIBLE);
                    relativeLayoutStatus.setVisibility(View.GONE);
                }

                isInitialized = true;
            } else {
                Toast.makeText(getApplicationContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
                isInitialized = true;
            }
        }
    }
}
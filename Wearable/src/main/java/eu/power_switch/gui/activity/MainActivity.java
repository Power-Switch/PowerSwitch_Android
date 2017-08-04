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
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wear.widget.drawer.WearableDrawerLayout;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.butterknife.ButterKnifeWearableActivity;
import eu.power_switch.event.DataChangedEvent;
import eu.power_switch.event.PreferenceChangedEvent;
import eu.power_switch.event.RoomDataChangedEvent;
import eu.power_switch.event.SceneDataChangedEvent;
import eu.power_switch.gui.adapter.NavigationDrawerAdapter;
import eu.power_switch.gui.fragment.RoomsFragment;
import eu.power_switch.gui.fragment.ScenesFragment;
import eu.power_switch.gui.fragment.SettingsFragment;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import timber.log.Timber;

import static eu.power_switch.gui.adapter.NavigationDrawerAdapter.INDEX_ROOMS;
import static eu.power_switch.gui.adapter.NavigationDrawerAdapter.INDEX_SCENES;
import static eu.power_switch.gui.adapter.NavigationDrawerAdapter.INDEX_SETTINGS;

/**
 * Main Activity holding all app related views
 */
public class MainActivity extends ButterKnifeWearableActivity {

    private static final int         MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    public static        String      apartmentName                                 = "";
    public static        List<Room>  roomList                                      = new ArrayList<>();
    public static        List<Scene> sceneList                                     = new ArrayList<>();
    private static       boolean     isInitialized                                 = false;
    private DataApiHandler dataApiHandler;

    @BindView(R.id.drawer_layout)
    WearableDrawerLayout mWearableDrawerLayout;

    // Main Wearable Drawer Layout that wraps all content
    @BindView(R.id.content_frame)
    FrameLayout                  contentFrameLayout;
    @BindView(R.id.top_navigation_drawer)
    WearableNavigationDrawerView mWearableNavigationDrawer;

    @BindView(R.id.textView_Status)
    TextView       textViewStatus;
    @BindView(R.id.relativeLayout_status)
    RelativeLayout relativeLayoutStatus;

//    private DismissOverlayView dismissOverlayView;
//    private GestureDetector gestureDetector;

    public static boolean isInitialized() {
        return isInitialized;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // allow always-on screen
        setAmbientEnabled();

        dataApiHandler = new DataApiHandler(getApplicationContext());

        // Top Navigation Drawer
        NavigationDrawerAdapter navigationDrawerAdapter = new NavigationDrawerAdapter(this);
        mWearableNavigationDrawer.setAdapter(navigationDrawerAdapter);
        mWearableNavigationDrawer.addOnItemSelectedListener(new WearableNavigationDrawerView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment;

                switch (i) {
                    case INDEX_ROOMS:
                        fragment = new RoomsFragment();
                        break;
                    case INDEX_SCENES:
                        fragment = new ScenesFragment();
                        break;
                    case INDEX_SETTINGS:
                        fragment = new SettingsFragment();
                        break;
                    default:
                        fragment = new RoomsFragment();
                        break;
                }

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();
            }
        });

        // load first fragment
        int index = wearablePreferencesHandler.getValue(WearablePreferencesHandler.STARTUP_DEFAULT_TAB);
        mWearableNavigationDrawer.setCurrentItem(index, false);

        // Get Room/Receiver/Button/Scene configuration from Smartphone App
        new FetchDataAsyncTask().execute();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("Write external storage permission is missing, asking for it...");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            Timber.d("Write external storage permission already granted");
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onDataChanged(DataChangedEvent e) {
        apartmentName = e.getApartmentName();

        List<Room> rooms = e.getRooms();
        roomList.clear();
        roomList.addAll(rooms);

        EventBus.getDefault()
                .post(new RoomDataChangedEvent(roomList));

        List<Scene> scenes = e.getScenes();
        sceneList.clear();
        sceneList.addAll(scenes);

        EventBus.getDefault()
                .post(new SceneDataChangedEvent(sceneList));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onPreferenceChanged(PreferenceChangedEvent e) {
        if (e.getPreferenceItem() == WearablePreferencesHandler.THEME) {
            recreate();

            // TODO: check if recreate() is sufficient and remove this code if so
//            finish();
//            Intent restartActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
//            restartActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            restartActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(restartActivityIntent);
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
    }

    @Override
    protected void onStop() {
        if (dataApiHandler != null) {
            dataApiHandler.disconnect();
        }

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

                EventBus.getDefault()
                        .post(new RoomDataChangedEvent(rooms));

                sceneList.clear();
                List<Scene> scenes = (List<Scene>) result.get(2);
                sceneList.addAll(scenes);

                EventBus.getDefault()
                        .post(new SceneDataChangedEvent(scenes));

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
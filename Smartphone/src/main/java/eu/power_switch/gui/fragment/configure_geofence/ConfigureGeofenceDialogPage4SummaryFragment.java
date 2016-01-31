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

package eu.power_switch.gui.fragment.configure_geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureGeofenceDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.geofences.CustomGeofencesFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * Created by Markus on 29.01.2016.
 */
public class ConfigureGeofenceDialogPage4SummaryFragment extends Fragment implements ConfigurationDialogTabbedSummaryFragment {

    private BroadcastReceiver broadcastReceiver;
    private View rootView;

    private long currentId = -1;
    private String currentName = "";
    private List<Action> currentEnterActions = new ArrayList<>();
    private List<Action> currentExitActions = new ArrayList<>();
    private LatLng currentLocation;
    private double currentGeofenceRadius;
    private Bitmap currentSnapshot;
    private TextView textViewName;
    private TextView textViewLocation;
    private ImageView imageViewLocationSnapshot;
    private TextView textViewEnterActions;
    private TextView textViewExitActions;
    private TextView textViewGeofenceRadius;


    private static void sendGeofenceSetupChangedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_SETUP_GEOFENCE_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_geofence_page_4, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocalBroadcastConstants.INTENT_GEOFENCE_LOCATION_CHANGED.equals(intent.getAction())) {
                    currentName = intent.getStringExtra("name");

                    double latitude = intent.getDoubleExtra("latitude", Geofence.INVALID_LAT);
                    double longitude = intent.getDoubleExtra("longitude", Geofence.INVALID_LON);
                    currentLocation = new LatLng(latitude, longitude);

                    currentGeofenceRadius = intent.getDoubleExtra("geofenceRadius", -1);

                    currentSnapshot = intent.getParcelableExtra("snapshot");

                } else if (LocalBroadcastConstants.INTENT_GEOFENCE_ENTER_ACTIONS_CHANGED.equals(intent.getAction())) {
                    currentEnterActions = (ArrayList<Action>) intent.getSerializableExtra("actions");

                } else if (LocalBroadcastConstants.INTENT_GEOFENCE_EXIT_ACTIONS_CHANGED.equals(intent.getAction())) {
                    currentExitActions = (ArrayList<Action>) intent.getSerializableExtra("actions");

                }

                updateUi();

                sendGeofenceSetupChangedBroadcast(getContext());
            }
        };

        textViewName = (TextView) rootView.findViewById(R.id.textView_name);
        textViewLocation = (TextView) rootView.findViewById(R.id.textView_location);
        imageViewLocationSnapshot = (ImageView) rootView.findViewById(R.id.imageView_locationSnapshot);
        textViewGeofenceRadius = (TextView) rootView.findViewById(R.id.textView_geofence_radius);
        textViewEnterActions = (TextView) rootView.findViewById(R.id.textView_enter_actions);
        textViewExitActions = (TextView) rootView.findViewById(R.id.textView_exit_actions);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureGeofenceDialog.GEOFENCE_ID_KEY)) {
            currentId = args.getLong(ConfigureGeofenceDialog.GEOFENCE_ID_KEY);
            initializeGeofenceData(currentId);
        }

        updateUi();

        return rootView;
    }

    private void initializeGeofenceData(long geofenceId) {
        try {
            Geofence geofence = DatabaseHandler.getGeofence(geofenceId);

            currentName = geofence.getName();
            currentGeofenceRadius = geofence.getRadius();
            currentLocation = geofence.getCenterLocation();
            currentSnapshot = geofence.getSnapshot();
            currentEnterActions = geofence.getActions(Geofence.EventType.ENTER);
            currentExitActions = geofence.getActions(Geofence.EventType.EXIT);

            // necessary, because geofences created by default are not yet initialized correctly, so even saved
            // geofences can be invalid
            sendGeofenceSetupChangedBroadcast(getContext());
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    private void updateUi() {
        if (currentName != null) {
            textViewName.setText(currentName);
        }

        if (currentLocation != null) {
            textViewLocation.setText(currentLocation.toString());
        }

        if (currentSnapshot != null) {
            imageViewLocationSnapshot.setImageBitmap(currentSnapshot);
        }

        if (currentGeofenceRadius != -1) {
            textViewGeofenceRadius.setText(String.valueOf((int) currentGeofenceRadius) + " m");
        }

        String enterActionText = "";
        if (currentEnterActions != null) {
            for (Action action : currentEnterActions) {
                enterActionText += action.toString() + "\n";
            }
        }
        textViewEnterActions.setText(enterActionText);

        String exitActionText = "";
        if (currentExitActions != null) {
            for (Action action : currentExitActions) {
                exitActionText += action.toString() + "\n";
            }
        }
        textViewExitActions.setText(exitActionText);
    }

    @Override
    public boolean checkSetupValidity() {
        if (currentName == null || currentName.length() <= 0) {
            return false;
        }

        if (currentGeofenceRadius == -1) {
            return false;
        }

        if (currentLocation == null) {
            return false;
        }

        if (currentSnapshot == null) {
            return false;
        }

        if (currentEnterActions == null || currentExitActions == null) {
            return false;
        }

        return !(currentEnterActions.size() == 0 && currentExitActions.size() == 0);

    }

    @Override
    public void saveCurrentConfigurationToDatabase() throws Exception {
        try {
            HashMap<Geofence.EventType, List<Action>> actionsMap = new HashMap<>();
            actionsMap.put(Geofence.EventType.ENTER, currentEnterActions);
            actionsMap.put(Geofence.EventType.EXIT, currentExitActions);

            if (currentId == -1) {
                DatabaseHandler.addGeofence(new Geofence(currentId, true, currentName, currentLocation,
                        currentGeofenceRadius, currentSnapshot, actionsMap));
            } else {
                Geofence geofence = DatabaseHandler.getGeofence(currentId);

                DatabaseHandler.updateGeofence(new Geofence(currentId, geofence.isActive(), currentName, currentLocation,
                        currentGeofenceRadius, currentSnapshot, actionsMap));
            }

            CustomGeofencesFragment.sendCustomGeofencesChangedBroadcast(getContext());

            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                    R.string.geofence_saved, Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GEOFENCE_LOCATION_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GEOFENCE_ENTER_ACTIONS_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_GEOFENCE_EXIT_ACTIONS_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

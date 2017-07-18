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

package eu.power_switch.google_play_services.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.shared.constants.GeofenceConstants;
import timber.log.Timber;

/**
 * Google Geofence API Handler
 * Used to manage Google Location/Geofence API Geofences
 * <p/>
 * Created by Markus on 21.12.2015.
 */
@Singleton
public class GeofenceApiHandler {

    @Inject
    StatusMessageHandler statusMessageHandler;

    private Context         context;
    private GoogleApiClient googleApiClient;

    @Inject
    public GeofenceApiHandler(Context context) {
        this.context = context;

        // Create an instance of GoogleAPIClient.
        googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Timber.d("GoogleApiClient connected");
            }

            @Override
            public void onConnectionSuspended(int i) {
                Timber.d("GoogleApiClient connection suspended");
            }
        })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Timber.e("GoogleApiClient connection failed");
                    }
                })
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Creates a Geofence Object with given parameters
     *
     * @param id                 ID of Geofence
     * @param latitude           Latitude of Geofence location
     * @param longitude          Longitude of Geofence location
     * @param radius             Radius in meter of geofence, for best results with WiFi networks this value should be >= 100
     * @param expirationDuration ???
     *
     * @return Geofence
     */
    public static Geofence createGeofence(String id, double latitude, double longitude, int radius, long expirationDuration) {
        Geofence geofence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(id)

                // Set the circular region of this geofence.
                .setCircularRegion(latitude, longitude, radius)

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(expirationDuration)

                // Delay to wait before DWELL transition happens
                .setLoiteringDelay(GeofenceConstants.DEFAULT_LOITERING_DELAY)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();

        return geofence;
    }

    private static GeofencingRequest getGeofencingRequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    public void blockingConnect() {
        googleApiClient.blockingConnect();
    }

    /**
     * Add Geofence to GeofenceAPI
     *
     * @param geofence Geofence
     */
    public void addGeofence(eu.power_switch.google_play_services.geofence.Geofence geofence) {
        addGeofence(getGeofencingRequest(createGeofence(String.valueOf(geofence.getId()),
                geofence.getCenterLocation().latitude,
                geofence.getCenterLocation().longitude,
                (int) geofence.getRadius(),
                -1)), getGeofencePendingIntent());
    }

    private void addGeofence(GeofencingRequest geofencingRequest, PendingIntent geofencePendingIntent) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, geofencePendingIntent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        switch (status.getStatusCode()) {
                            case CommonStatusCodes.SUCCESS:
                                statusMessageHandler.showInfoMessage(context, R.string.geofence_enabled, Snackbar.LENGTH_SHORT);
                        }

                        Timber.d(status.toString());
                    }
                });
    }

    /**
     * Remove Geofence from GeofenceAPI
     *
     * @param geofenceId ID of Geofence
     */
    public void removeGeofence(long geofenceId) {
        removeGeofence(googleApiClient, String.valueOf(geofenceId));
    }

    private void removeGeofence(GoogleApiClient googleApiClient, final String geofenceId) {
        ArrayList<String> geofenceIds = new ArrayList<>();
        geofenceIds.add(geofenceId);

        LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceIds)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        switch (status.getStatusCode()) {
                            case CommonStatusCodes.SUCCESS:
                                statusMessageHandler.showInfoMessage(context, R.string.geofence_disabled, Snackbar.LENGTH_SHORT);
                        }

                        Timber.d(status.toString());
                    }
                }); // Result processed in onResult().
    }

    /**
     * Remove all Geofences from Google Location Api
     */
    public void removeAllGeofences() {
        LocationServices.GeofencingApi.removeGeofences(googleApiClient,
                // This is the same pending intent that was used in addGeofence().
                getGeofencePendingIntent())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        switch (status.getStatusCode()) {
                            case CommonStatusCodes.SUCCESS:
                                statusMessageHandler.showInfoMessage(context, R.string.geofences_disabled, Snackbar.LENGTH_LONG);
                        }

                        Timber.d(status.toString());
                    }
                }); // Result processed in onResult().
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(context, GeofenceIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofence() and removeAllGeofences().
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Connect (async) GoogleApiClient
     */
    public void onStart() {
        googleApiClient.connect();
    }

    /**
     * Disconnect (async) GoogleApiClient
     */
    public void onStop() {
        googleApiClient.disconnect();
    }
}

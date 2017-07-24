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

package eu.power_switch.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.shared.permission.PermissionHelper;
import timber.log.Timber;

/**
 * Created by Markus on 22.07.2017.
 */
@Singleton
public class LocationHandlerImpl implements LocationHandler {

    private final Context context;

    private final Set<LocationListener> listeners = Collections.synchronizedSet(new HashSet<LocationListener>());

    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final LocationCallback            locationCallback;

    private Location lastLocation;

    @Inject
    public LocationHandlerImpl(Context context) {
        this.context = context;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                callListeners(locationAvailability.isLocationAvailable());
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    callListeners(location);
                }
            }
        };

        getLastLocation();
    }

    private synchronized void callListeners(boolean locationAvailable) {
        for (LocationListener listener : listeners) {
            listener.onAvailabilityChanged(locationAvailable);
        }
    }

    private synchronized void callListeners(Location location) {
        for (LocationListener listener : listeners) {
            lastLocation = location;
            listener.onLocationUpdated(location);
        }
    }

    @Override
    public Location getLastLocation() {
        if (PermissionHelper.isLocationPermissionAvailable(context)) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lastLocation = location;
                                Timber.d("LastLocation: " + location.getLatitude() + " " + location.getLongitude());
                            } else {
                                Timber.w("getLastLocation result was null");
                            }
                        }
                    });
        }

        return lastLocation;
    }

    public boolean addLocationListener(LocationListener listener) {
        if (listener == null) {
            return false;
        }

        boolean added = listeners.add(listener);

        if (added && listeners.size() == 1) {
            startLocationUpdates();
        }

        return added;
    }

    public boolean removeLocationListener(LocationListener listener) {
        if (listener == null) {
            return false;
        }

        boolean removed = listeners.remove(listener);

        if (removed && listeners.size() == 0) {
            stopLocationUpdates();
        }

        return removed;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        locationRequest.setInterval(60 * 1000 * 1000);
        locationRequest.setFastestInterval(60 * 1000);

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Missing location permission, not starting location updates");
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}

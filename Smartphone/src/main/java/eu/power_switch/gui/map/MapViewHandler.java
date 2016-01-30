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

package eu.power_switch.gui.map;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import eu.power_switch.R;
import eu.power_switch.exception.location.AddressNotFoundException;
import eu.power_switch.shared.log.Log;

/**
 * This class is responsible for initializing and managing access to a MapView Object
 * <p/>
 * Created by Markus on 20.01.2016.
 */
public class MapViewHandler implements OnMapReadyCallback {

    private Context context;

    /**
     * MapView this Handler is responsible for
     */
    private MapView mapView;

    /**
     * GoogleMap returned by MapView initialization
     */
    private GoogleMap googleMap;

    /**
     * List of all Markers
     * <p/>
     * Map<MarkerId, Marker>
     */
    private Map<String, Marker> markers = new HashMap<>();

    /**
     * List of all Circles
     * <p/>
     * Map<CircleId, Circle>
     */
    private Map<String, Circle> circles = new HashMap<>();

    /**
     * List of all Geofences
     * <p/>
     * Map<GeofenceId, Geofence>
     */
    private Map<String, Geofence> geofences = new HashMap<>();

    /**
     * Set of onMapReadyCallback listeners
     */
    private Set<OnMapReadyCallback> onMapReadyListeners = new HashSet<>();

    /**
     * Constructor
     *
     * @param context            any suitable context
     * @param mapView
     * @param savedInstanceState
     */
    public MapViewHandler(Context context, MapView mapView, Bundle
            savedInstanceState) {
        this.context = context;
        this.mapView = mapView;

        mapView.onCreate(savedInstanceState);
    }

    /**
     * Get MapView this handler is responsible for
     *
     * @return MapView
     */
    public MapView getMapView() {
        return mapView;
    }

    /**
     * Add an OnMapReadyCallbackListener to get notified when the GoogleMap has initialized
     *
     * @param onMapReadyCallbackListener Listener
     */
    public void addOnMapReadyListener(OnMapReadyCallback onMapReadyCallbackListener) {
        onMapReadyListeners.add(onMapReadyCallbackListener);
    }

    /**
     * Add an onMapLoadedCallbackListener to get notified when map has fully loaded and rendered
     * <p/>
     * Note: This method can only be called after successful googleMap initialization
     *
     * @param onMapLoadedCallbackListener Listener
     */
    public void setOnMapLoadedListener(GoogleMap.OnMapLoadedCallback onMapLoadedCallbackListener) {
        googleMap.setOnMapLoadedCallback(onMapLoadedCallbackListener);
    }

    /**
     * Start async GoogleMap initialization process
     */
    public void initMapAsync() {
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // trigger listeners
        for (OnMapReadyCallback onMapReadyListener : onMapReadyListeners) {
            onMapReadyListener.onMapReady(this.googleMap);
        }
    }

    /**
     * Add Geofence to Map
     *
     * @param latLng position
     * @param radius radius
     * @return Geofence
     */
    public Geofence addGeofence(LatLng latLng, double radius) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .draggable(true);
        Marker marker = googleMap.addMarker(markerOptions);

        CircleOptions circleOptions = new CircleOptions().center(latLng)
                .radius(radius)
                .fillColor(ContextCompat.getColor(context, R.color.geofenceFillColor))
                .strokeColor(Color.BLUE)
                .strokeWidth(2);
        Circle circle = googleMap.addCircle(circleOptions);

        circles.put(circle.getId(), circle);
        Geofence geofence = new Geofence(marker, circle);
        geofences.put(marker.getId(), geofence);

        return geofence;
    }

    public void updateGeofence(String id, CircleOptions circleOptions) {
        Geofence geofence = geofences.get(id);
        geofence.setRadius(circleOptions.getRadius());
        geofence.setCenter(circleOptions.getCenter());
        geofence.setVisible(circleOptions.isVisible());
        geofence.setFillColor(circleOptions.getFillColor());
        geofence.setStrokeColor(circleOptions.getStrokeColor());
        geofence.setStrokeWidth(circleOptions.getStrokeWidth());
        geofence.setZIndex(circleOptions.getZIndex());
    }

    /**
     * Remove Geofence
     *
     * @param id ID of Geofence
     */
    public void removeGeofence(String id) {
        Geofence geofence = geofences.get(id);
        geofence.remove();
        geofences.remove(id);
    }

    /**
     * Add Marker to Map
     *
     * @param markerOptions options
     * @return Marker
     */
    public Marker addMarker(MarkerOptions markerOptions) {
        Marker marker = googleMap.addMarker(markerOptions);
        markers.put(marker.getId(), marker);
        return marker;
    }

    public void updateMarker(String id, MarkerOptions markerOptions) {
        Marker marker = markers.get(id);
        marker.setPosition(markerOptions.getPosition());
        marker.setAlpha(markerOptions.getAlpha());
        marker.setDraggable(markerOptions.isDraggable());
        marker.setFlat(markerOptions.isFlat());
        marker.setIcon(markerOptions.getIcon());
        marker.setVisible(markerOptions.isVisible());
        marker.setTitle(markerOptions.getTitle());
        marker.setSnippet(markerOptions.getSnippet());
        marker.setRotation(markerOptions.getRotation());
        marker.setAnchor(markerOptions.getAnchorU(), markerOptions.getAnchorV());
        marker.setInfoWindowAnchor(markerOptions.getInfoWindowAnchorU(), markerOptions.getInfoWindowAnchorV());
    }

    /**
     * Remove Marker from Map
     *
     * @param id ID of Marker
     */
    public void removeMarker(String id) {
        Marker marker = markers.get(id);
        marker.remove();
        markers.remove(id);
    }

    /**
     * Find a address description for a given coordinate
     *
     * @param latLng coordinate
     * @return address description
     * @throws AddressNotFoundException
     */
    public String findAddress(LatLng latLng) throws AddressNotFoundException {
        /* get latitude and longitude from the address */
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);


                String addressAsString = address.getAddressLine(0);

                Log.d("Address; ", addressAsString);
                return addressAsString;
            } else {
                throw new AddressNotFoundException("latitude: " + latLng.latitude + ", longitude: " + latLng.longitude);
            }
        } catch (IOException e) {
            Log.e(e);
        }

        return null;
    }

    /**
     * Find Coordinates for a given address
     *
     * @param address address as text
     * @return coordinate near the given address
     * @throws AddressNotFoundException
     */
    public LatLng findCoordinates(String address) throws AddressNotFoundException {
        /* get latitude and longitude from the address */
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 5);
            if (addresses.size() > 0) {
                Double lat = (addresses.get(0).getLatitude());
                Double lon = (addresses.get(0).getLongitude());

                Log.d("lat-lon", lat + "......." + lon);
                final LatLng location = new LatLng(lat, lon);
                return location;
            } else {
                throw new AddressNotFoundException(address);
            }
        } catch (IOException e) {
            Log.e(e);
        }

        return null;
    }

    /**
     * Move the map camera to a specific location
     *
     * @param location new location of camera
     * @param animated true if movement should be animated, false otherwise
     */
    public void moveCamera(LatLng location, boolean animated) {
        if (location == null) {
            Log.w("location is null!");
            return;
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(location);

        if (animated) {
            googleMap.animateCamera(cameraUpdate);
        } else {
            googleMap.moveCamera(cameraUpdate);
        }
    }

    /**
     * Move the map camera to a specific location
     *
     * @param location new location of camera
     * @param zoom     zoom level
     * @param animated true if movement should be animated, false otherwise
     */
    public void moveCamera(LatLng location, float zoom, boolean animated) {
        if (location == null) {
            Log.w("location is null!");
            return;
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, zoom);

        if (animated) {
            googleMap.animateCamera(cameraUpdate);
        } else {
            googleMap.moveCamera(cameraUpdate);
        }
    }

    public void takeSnapshot(GoogleMap.SnapshotReadyCallback callback) {
        mapView.invalidate();
        googleMap.snapshot(callback);
    }


}

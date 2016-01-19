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

package eu.power_switch.gui.fragment.configure_apartment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigureApartmentDialog;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.location.LocationHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.log.Log;

/**
 * "Location" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureApartmentDialogPage2LocationFragment extends Fragment implements OnMapReadyCallback {

    private long apartmentId = -1;
    private View rootView;
    private String currentName;
    private List<Gateway> currentCheckedGateways;
    private LatLng currentLocation;
    private double currentGeofenceRadius = 100;
    private MapView mapView;
    private GoogleMap googleMap;
    private LocationHandler locationHandler;
    private Marker clickMarker;
    private Circle geofenceCircle;
    private SeekBar geofenceRadiusSeekbar;
    private EditText geofenceRadiusEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_apartment_page_2, container, false);

        locationHandler = new LocationHandler(getActivity());

        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        geofenceRadiusEditText = (EditText) rootView.findViewById(R.id.geofenceRadiusEditText);
        geofenceRadiusEditText.setText(String.valueOf((int) currentGeofenceRadius));

        geofenceRadiusSeekbar = (SeekBar) rootView.findViewById(R.id.geofenceRadiusSeekbar);
        geofenceRadiusSeekbar.setMax(2000);
        geofenceRadiusSeekbar.setProgress((int) currentGeofenceRadius);
        geofenceRadiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateGeofenceRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                updateGeofenceRadius(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateGeofenceRadius(seekBar.getProgress());
            }
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureApartmentDialog.APARTMENT_ID_KEY)) {
            apartmentId = args.getLong(ConfigureApartmentDialog.APARTMENT_ID_KEY);
            initializeApartmentData(apartmentId);
        }

        checkValidity();

        return rootView;
    }

    private void updateGeofenceRadius(double radius) {
        currentGeofenceRadius = radius;

        if (geofenceCircle != null) {
            geofenceCircle.setRadius(radius);
        }

        geofenceRadiusEditText.setText(String.valueOf((int) radius));
    }

    private void initializeApartmentData(long apartmentId) {
        try {
            Apartment apartment = DatabaseHandler.getApartment(apartmentId);
            currentName = apartment.getName();
            currentCheckedGateways = apartment.getAssociatedGateways();
            currentLocation = apartment.getLocation();
            currentGeofenceRadius = apartment.getGeofenceRadius();
            updateGeofenceRadius(currentGeofenceRadius);

        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    public void saveCurrentConfigurationToDatabase() {
        try {
            if (apartmentId == -1) {
                String apartmentName = currentName;

                Apartment newApartment = new Apartment((long) -1, apartmentName, currentCheckedGateways,
                        currentLocation, currentGeofenceRadius);

                try {
                    DatabaseHandler.addApartment(newApartment);
                } catch (Exception e) {
                    StatusMessageHandler.showStatusMessage(rootView.getContext(),
                            R.string.unknown_error, Snackbar.LENGTH_LONG);
                }
            } else {
                DatabaseHandler.updateApartment(apartmentId, currentName, currentCheckedGateways, currentLocation, currentGeofenceRadius);
            }

            ApartmentFragment.sendApartmentChangedBroadcast(getActivity());
            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(), R.string.apartment_saved,
                    Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(rootView.getContext(), R.string.unknown_error, Snackbar.LENGTH_LONG);
        }

    }

    public boolean checkValidity() {
        return false;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(latLng.toString());
                currentLocation = latLng;

                if (clickMarker == null) {
                    clickMarker = googleMap.addMarker(new MarkerOptions().position(latLng)
                            .title(getString(R.string.location))
                            .draggable(true));
                } else {
                    clickMarker.setPosition(latLng);
                }

                if (geofenceCircle == null) {
                    geofenceCircle = googleMap.addCircle(new CircleOptions().center(latLng)
                            .radius(currentGeofenceRadius)
                            .fillColor(ContextCompat.getColor(getContext(), R.color.geofenceFillColor))
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2));
                } else {
                    geofenceCircle.setCenter(latLng);
                    geofenceCircle.setRadius(currentGeofenceRadius);
                }

            }
        });
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (clickMarker.getId().equals(marker.getId())) {
                    geofenceCircle.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (clickMarker.getId().equals(marker.getId())) {
                    geofenceCircle.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (clickMarker.getId().equals(marker.getId())) {
                    geofenceCircle.setCenter(marker.getPosition());
                }
            }
        });

        if (apartmentId != -1) {
            try {
                Apartment apartment = DatabaseHandler.getApartment(apartmentId);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(apartment.getLocation(), 14));
            } catch (Exception e) {
                Log.e(e);
            }
        } else {
            LatLng latLng = new LatLng(52.4369683282584, 13.373247385025024);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        }
//        Location location = locationHandler.getLastLocation();
    }

    @Override
    public void onStart() {
        super.onStart();
        locationHandler.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        locationHandler.disconnect();
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

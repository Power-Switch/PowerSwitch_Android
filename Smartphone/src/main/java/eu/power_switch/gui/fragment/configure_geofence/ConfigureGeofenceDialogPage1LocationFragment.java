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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.location.AddressNotFoundException;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.dialog.ConfigureGeofenceDialog;
import eu.power_switch.gui.map.MapViewHandler;
import eu.power_switch.shared.constants.GeofenceConstants;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * "Location" Fragment used in Configure Geofence Dialog
 * <p/>
 * Created by Markus on 27.01.2016.
 */
public class ConfigureGeofenceDialogPage1LocationFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private long geofenceId = -1;

    private String name = "NAME";
    private View rootView;
    private MapViewHandler mapViewHandler;
    private eu.power_switch.gui.map.Geofence geofenceView;
    private SeekBar geofenceRadiusSeekbar;
    private EditText geofenceRadiusEditText;
    private ImageButton searchAddressButton;
    private TextInputLayout searchAddressTextInputLayout;
    private EditText searchAddressEditText;

    private double currentGeofenceRadius = GeofenceConstants.DEFAULT_GEOFENCE_RADIUS;
    private Bitmap currentSnapshot;

    private boolean cameraChangedBySystem = true;

    /**
     * Used to notify parent Dialog that configuration has changed
     *
     * @param context any suitable context
     */
    public static void sendSetupGeofenceChangedBroadcast(Context context, String name, LatLng location, double
            geofenceRadius, Bitmap snapShot) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_GEOFENCE_LOCATION_CHANGED);
        intent.putExtra("name", name);
        intent.putExtra("latitude", location.latitude);
        intent.putExtra("longitude", location.longitude);
        intent.putExtra("geofenceRadius", geofenceRadius);
        intent.putExtra("snapshot", snapShot);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_geofence_page_1, container, false);

        MapView mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapViewHandler = new MapViewHandler(getContext(), mapView, savedInstanceState);
        mapViewHandler.addOnMapReadyListener(this);
        mapViewHandler.initMapAsync();

        searchAddressTextInputLayout = (TextInputLayout) rootView.findViewById(R.id
                .searchAddressTextInputLayout);
        searchAddressEditText = (EditText) rootView.findViewById(R.id.searchAddressEditText);

        searchAddressButton = (ImageButton) rootView.findViewById(R.id.searchAddressImageButton);
        searchAddressButton.setImageDrawable(IconicsHelper.getSearchIcon(getActivity(), android.R.color.white));
        searchAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LatLng location = mapViewHandler.findCoordinates(searchAddressEditText.getText().toString());
                    hideSoftwareKeyboard(getView());

                    if (geofenceView == null) {
                        geofenceView = mapViewHandler.addGeofence(location, currentGeofenceRadius);
                    } else {
                        geofenceView.setCenter(location);
                        geofenceView.setRadius(currentGeofenceRadius);
                    }

                    cameraChangedBySystem = true;
                    mapViewHandler.moveCamera(location, 14, true);

                    // name = searchAddressEditText.getText().toString();

                    searchAddressTextInputLayout.setError(null);
                    searchAddressTextInputLayout.setErrorEnabled(false);
                } catch (AddressNotFoundException e) {
                    searchAddressTextInputLayout.setErrorEnabled(true);
                    searchAddressTextInputLayout.setError(getString(R.string.address_not_found));
                } catch (Exception e) {
                    searchAddressTextInputLayout.setErrorEnabled(true);
                    searchAddressTextInputLayout.setError(getString(R.string.unknown_error));
                }
            }
        });

        geofenceRadiusEditText = (EditText) rootView.findViewById(R.id.geofenceRadiusEditText);
        geofenceRadiusEditText.setText(String.valueOf((int) currentGeofenceRadius));
        geofenceRadiusEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int radius = Integer.valueOf(s.toString());

                    updateGeofenceRadius(radius);
                    if (radius != geofenceRadiusSeekbar.getProgress()) {
                        if (radius <= geofenceRadiusSeekbar.getMax()) {
                            geofenceRadiusSeekbar.setProgress(radius);
                        }
                    }

                    cameraChangedBySystem = true;
                    mapViewHandler.moveCamera(getCurrentLocation(), false);
                }
            }
        });

        geofenceRadiusSeekbar = (SeekBar) rootView.findViewById(R.id.geofenceRadiusSeekbar);
        geofenceRadiusSeekbar.setMax(2000);
        geofenceRadiusSeekbar.setProgress((int) currentGeofenceRadius);
        geofenceRadiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateGeofenceRadius(progress);
                if (geofenceRadiusEditText.getText().length() > 0 &&
                        Integer.valueOf(geofenceRadiusEditText.getText().toString()) != progress) {
                    geofenceRadiusEditText.setText(String.valueOf(seekBar.getProgress()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                updateGeofenceRadius(seekBar.getProgress());
                if (geofenceRadiusEditText.getText().length() > 0 &&
                        Integer.valueOf(geofenceRadiusEditText.getText().toString()) != seekBar.getProgress()) {
                    geofenceRadiusEditText.setText(String.valueOf(seekBar.getProgress()));
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateGeofenceRadius(seekBar.getProgress());
                if (geofenceRadiusEditText.getText().length() > 0 &&
                        Integer.valueOf(geofenceRadiusEditText.getText().toString()) != seekBar.getProgress()) {
                    geofenceRadiusEditText.setText(String.valueOf(seekBar.getProgress()));
                }

                cameraChangedBySystem = true;
                mapViewHandler.moveCamera(getCurrentLocation(), false);
            }
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureGeofenceDialog.GEOFENCE_ID_KEY)) {
            geofenceId = args.getLong(ConfigureGeofenceDialog.GEOFENCE_ID_KEY);
        }

        initializeData();

        return rootView;
    }

    private void initializeData() {
        try {
            if (geofenceId != -1) {
                Geofence geofence = DatabaseHandler.getGeofence(geofenceId);

                name = geofence.getName();

                if (geofence.getCenterLocation() != null) {
                    double radius = geofence.getRadius();
                    geofenceRadiusSeekbar.setProgress((int) radius);
                    geofenceRadiusEditText.setText(String.valueOf((int) radius));

                    updateGeofenceRadius(radius);
                } else {
                    updateGeofenceRadius(GeofenceConstants.DEFAULT_GEOFENCE_RADIUS);
                }
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private void hideSoftwareKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private LatLng getCurrentLocation() {
        try {
            return geofenceView.getMarker().getPosition();
        } catch (Exception e) {
            return null;
        }
    }

    private void updateGeofenceRadius(double radius) {
        currentGeofenceRadius = radius;

        if (geofenceView != null) {
            geofenceView.setRadius(radius);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            googleMap.setMyLocationEnabled(true);
        }

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMapToolbarEnabled(false);

        mapViewHandler.setOnMapLoadedListener(this);
        final GoogleMap.OnMapLoadedCallback listener = this;
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraChangedBySystem) {
                    mapViewHandler.setOnMapLoadedListener(listener);
                    cameraChangedBySystem = false;
                }
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                Log.d(latLng.toString());

                if (geofenceView == null) {
                    geofenceView = mapViewHandler.addGeofence(latLng, currentGeofenceRadius);
                } else {
                    geofenceView.setCenter(latLng);
                    geofenceView.setRadius(currentGeofenceRadius);
                }

                try {
                    String address = mapViewHandler.findAddress(latLng);
                    if (address != null && address.length() > 0) {
                        searchAddressEditText.setText(address);
                    }
                } catch (AddressNotFoundException e) {
                    Log.e(e);
                }

                cameraChangedBySystem = true;
                if (mapViewHandler.getCurrentZoomLevel() < 13) {
                    mapViewHandler.moveCamera(latLng, 14, true);
                } else {
                    mapViewHandler.moveCamera(latLng, true);
                }
            }
        });
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (geofenceView.getMarker().getId().equals(marker.getId())) {
                    geofenceView.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (geofenceView.getMarker().getId().equals(marker.getId())) {
                    geofenceView.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (geofenceView.getMarker().getId().equals(marker.getId())) {
                    geofenceView.setCenter(marker.getPosition());

                    try {
                        String address = mapViewHandler.findAddress(marker.getPosition());
                        if (address != null && address.length() > 0) {
                            searchAddressEditText.setText(address);
                        }
                    } catch (AddressNotFoundException e) {
                        Log.e(e);
                    }

                    cameraChangedBySystem = true;
                    mapViewHandler.moveCamera(marker.getPosition(), true);
                }
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                cameraChangedBySystem = true;
                mapViewHandler.moveCamera(geofenceView.getMarker().getPosition(), true);
                return true;
            }
        });

        if (geofenceId != -1) {
            try {
                Geofence geofence = DatabaseHandler.getGeofence(geofenceId);
                if (geofenceView != null) {
                    geofenceView.remove();
                }
                if (geofence.getCenterLocation() != null) {
                    geofenceView = mapViewHandler.addGeofence(geofence.getCenterLocation(), geofence.getRadius());

                    cameraChangedBySystem = true;
                    mapViewHandler.moveCamera(getCurrentLocation(), 14, false);
                }
            } catch (Exception e) {
                Log.e(e);
            }
        }
    }

    @Override
    public void onMapLoaded() {
        Log.d("Map fully loaded");
        mapViewHandler.takeSnapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                Log.d("Snapshot Ready");
                currentSnapshot = bitmap;

                try {
                    sendSetupGeofenceChangedBroadcast(getContext(), name, getCurrentLocation(), currentGeofenceRadius, currentSnapshot);
                } catch (Exception e) {
                    Log.e(e);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapViewHandler.getMapView().onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapViewHandler.getMapView().onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapViewHandler.getMapView().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapViewHandler.getMapView().onLowMemory();
    }
}
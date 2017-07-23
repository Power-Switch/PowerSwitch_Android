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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.GeofenceConfigurationHolder;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.gui.map.MapViewHandler;
import eu.power_switch.location.GeoCodingHandler;
import eu.power_switch.shared.constants.GeofenceConstants;
import timber.log.Timber;

/**
 * "Location" Fragment used in Configure Geofence Dialog
 * <p/>
 * Created by Markus on 27.01.2016.
 */
public class ConfigureGeofenceDialogPage1Location extends ConfigurationDialogPage<GeofenceConfigurationHolder> implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    @BindView(R.id.geofenceRadiusSeekbar)
    SeekBar         geofenceRadiusSeekbar;
    @BindView(R.id.geofenceRadiusEditText)
    EditText        geofenceRadiusEditText;
    @BindView(R.id.searchAddressImageButton)
    ImageButton     searchAddressButton;
    @BindView(R.id.searchAddressTextInputLayout)
    TextInputLayout searchAddressTextInputLayout;
    @BindView(R.id.searchAddressEditText)
    EditText        searchAddressEditText;

    @BindView(R.id.searchAddressProgress)
    ProgressBar searchAddressProgress;
    @BindView(R.id.mapView)
    MapView     mapView;

    @Inject
    GeoCodingHandler geoCodingHandler;

    private MapViewHandler                   mapViewHandler;
    private eu.power_switch.gui.map.Geofence geofenceView;

    private boolean cameraChangedBySystem = true;
    private boolean isFirstTimeMapInit    = true;

    /**
     * Used to notify parent Dialog that configuration has changed
     */
    public void updateConfiguration(String name, LatLng location, double geofenceRadius, Bitmap snapShot) {
        getConfiguration().setName(name);
        getConfiguration().setLocation(location);
        getConfiguration().setRadius(geofenceRadius);
        getConfiguration().setSnapshot(snapShot);

        notifyConfigurationChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mapViewHandler = new MapViewHandler(getContext(), mapView, savedInstanceState);
        mapViewHandler.addOnMapReadyListener(this);
        mapViewHandler.initMapAsync();

        searchAddressButton.setImageDrawable(IconicsHelper.getSearchIcon(getActivity(),
                ContextCompat.getColor(getActivity(), android.R.color.white)));
        searchAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftwareKeyboard(getView());

                searchAddressEditText.setEnabled(false);
                searchAddressProgress.setVisibility(View.VISIBLE);
                new AsyncTask<String, Void, AsyncTaskResult<LatLng>>() {
                    @Override
                    protected AsyncTaskResult<LatLng> doInBackground(String... params) {
                        try {
                            Address address  = geoCodingHandler.findAddress(params[0]);
                            LatLng  location = null;
                            if (address != null) {
                                location = geoCodingHandler.getLocation(address);
                            }

                            return new AsyncTaskResult<>(location);
                        } catch (Exception e) {
                            return new AsyncTaskResult<>(e);
                        }
                    }

                    @Override
                    protected void onPostExecute(AsyncTaskResult<LatLng> result) {
                        if (result.isSuccess()) {
                            LatLng location = result.getResult()
                                    .get(0);

                            if (location == null) {
                                Timber.w("Location is null, ignoring");
                                return;
                            }

                            searchAddressTextInputLayout.setError(null);

                            if (geofenceView == null) {
                                geofenceView = mapViewHandler.addGeofence(location, getConfiguration().getRadius());
                            } else {
                                geofenceView.setCenter(location);
                                geofenceView.setRadius(getConfiguration().getRadius());
                            }

                            cameraChangedBySystem = true;
                            mapViewHandler.moveCamera(location, 14, true);

                            findAddress(location);
                        } else {
                            if (result.getException() == null) {
                                searchAddressTextInputLayout.setError(getString(R.string.address_not_found));
                            } else {
                                searchAddressTextInputLayout.setError(getString(R.string.unknown_error));
                            }
                        }

                        searchAddressEditText.setEnabled(true);
                        searchAddressProgress.setVisibility(View.GONE);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        searchAddressEditText.getText()
                                .toString());
            }
        });

        geofenceRadiusEditText.setText(String.valueOf((int) getConfiguration().getRadius()));
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

        geofenceRadiusSeekbar.setMax(2000);
        geofenceRadiusSeekbar.setProgress((int) getConfiguration().getRadius());
        geofenceRadiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateGeofenceRadius(progress);
                if (geofenceRadiusEditText.getText()
                        .length() > 0 && Integer.valueOf(geofenceRadiusEditText.getText()
                        .toString()) != progress) {
                    geofenceRadiusEditText.setText(String.valueOf(seekBar.getProgress()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                updateGeofenceRadius(seekBar.getProgress());
                if (geofenceRadiusEditText.getText()
                        .length() > 0 && Integer.valueOf(geofenceRadiusEditText.getText()
                        .toString()) != seekBar.getProgress()) {
                    geofenceRadiusEditText.setText(String.valueOf(seekBar.getProgress()));
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateGeofenceRadius(seekBar.getProgress());
                if (geofenceRadiusEditText.getText()
                        .length() > 0 && Integer.valueOf(geofenceRadiusEditText.getText()
                        .toString()) != seekBar.getProgress()) {
                    geofenceRadiusEditText.setText(String.valueOf(seekBar.getProgress()));
                }

                cameraChangedBySystem = true;
                mapViewHandler.moveCamera(getCurrentLocation(), false);
            }
        });

        initializeData();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_geofence_page_1;
    }

    private void initializeData() {
        Geofence geofence = getConfiguration().getGeofence();

        if (geofence != null) {
            try {
                if (geofence.getCenterLocation() != null) {
                    double radius = geofence.getRadius();
                    geofenceRadiusSeekbar.setProgress((int) radius);
                    geofenceRadiusEditText.setText(String.valueOf((int) radius));

                    updateGeofenceRadius(radius);
                } else {
                    updateGeofenceRadius(GeofenceConstants.DEFAULT_GEOFENCE_RADIUS);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
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
            return geofenceView.getMarker()
                    .getPosition();
        } catch (Exception e) {
            return null;
        }
    }

    private void updateGeofenceRadius(double radius) {
        getConfiguration().setRadius(radius);

        notifyConfigurationChanged();

        if (geofenceView != null) {
            geofenceView.setRadius(radius);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
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
                Timber.d(latLng.toString());

                if (geofenceView == null) {
                    geofenceView = mapViewHandler.addGeofence(latLng, getConfiguration().getRadius());
                } else {
                    geofenceView.setCenter(latLng);
                    geofenceView.setRadius(getConfiguration().getRadius());
                }

                findAddress(latLng);

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
                if (geofenceView.getMarker()
                        .getId()
                        .equals(marker.getId())) {
                    geofenceView.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (geofenceView.getMarker()
                        .getId()
                        .equals(marker.getId())) {
                    geofenceView.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (geofenceView.getMarker()
                        .getId()
                        .equals(marker.getId())) {
                    geofenceView.setCenter(marker.getPosition());

                    findAddress(marker.getPosition());

                    cameraChangedBySystem = true;
                    mapViewHandler.moveCamera(marker.getPosition(), true);
                }
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                cameraChangedBySystem = true;
                mapViewHandler.moveCamera(geofenceView.getMarker()
                        .getPosition(), true);
                return true;
            }
        });

        Geofence geofence = getConfiguration().getGeofence();
        if (geofence != null) {
            try {
                if (geofenceView != null) {
                    geofenceView.remove();
                }
                if (geofence.getCenterLocation() != null) {
                    geofenceView = mapViewHandler.addGeofence(geofence.getCenterLocation(), geofence.getRadius());

                    cameraChangedBySystem = true;
                    mapViewHandler.moveCamera(getCurrentLocation(), 14, false);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private void findAddress(LatLng latLng) {
        searchAddressEditText.setEnabled(false);
        searchAddressProgress.setVisibility(View.VISIBLE);

        new AsyncTask<LatLng, Void, AsyncTaskResult<String>>() {

            @Override
            protected AsyncTaskResult<String> doInBackground(LatLng... addresses) {
                try {
                    Address address            = geoCodingHandler.findAddress(addresses[0]);
                    String  addressDescription = null;

                    if (address != null) {
                        addressDescription = address.getAddressLine(0);
                    }

                    return new AsyncTaskResult<>(addressDescription);
                } catch (Exception e) {
                    Timber.e(e);
                    return new AsyncTaskResult<>(e);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<String> result) {
                searchAddressProgress.setVisibility(View.GONE);

                if (result.isSuccess()) {
                    searchAddressTextInputLayout.setError(null);

                    String firstMatch = result.getResult()
                            .get(0);
                    searchAddressEditText.setText(firstMatch);
                    getConfiguration().setName(firstMatch);

                    notifyConfigurationChanged();
                } else {
                    searchAddressEditText.setText("");
                }

                searchAddressEditText.setEnabled(true);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, latLng);
    }

    @Override
    public void onMapLoaded() {
        Timber.d("Map fully loaded");
        mapViewHandler.takeSnapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                Timber.d("Snapshot Ready");
                getConfiguration().setSnapshot(bitmap);

                try {
                    if (isFirstTimeMapInit) {
                        isFirstTimeMapInit = false;
                    } else {
                        updateConfiguration(getConfiguration().getName(), getCurrentLocation(), getConfiguration().getRadius(), bitmap);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapViewHandler.getMapView()
                .onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapViewHandler.getMapView()
                .onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapViewHandler.getMapView()
                .onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapViewHandler.getMapView()
                .onLowMemory();
    }
}

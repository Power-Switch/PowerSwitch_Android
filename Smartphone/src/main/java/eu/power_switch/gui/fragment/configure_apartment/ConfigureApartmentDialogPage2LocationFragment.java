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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.location.AddressNotFoundException;
import eu.power_switch.google_play_services.location.LocationApiHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigureApartmentDialog;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.map.Geofence;
import eu.power_switch.gui.map.MapViewHandler;
import eu.power_switch.gui.map.OnMapReadyListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * "Location" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureApartmentDialogPage2LocationFragment extends Fragment implements OnMapReadyListener {

    private long apartmentId = -1;
    private View rootView;
    private String currentName;
    private List<Gateway> currentCheckedGateways;
    private double currentGeofenceRadius = 100;
    private LocationApiHandler locationApiHandler;
    private MapViewHandler mapViewHandler;
    private Geofence geofence;
    private SeekBar geofenceRadiusSeekbar;
    private EditText geofenceRadiusEditText;
    private BroadcastReceiver broadcastReceiver;
    private EditText searchAddressEditText;
    private TextInputLayout searchAddressTextInputLayout;

    /**
     * Used to notify parent Dialog that configuration has changed
     *
     * @param context any suitable context
     */
    public static void sendSetupApartmentChangedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_SETUP_APARTMENT_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_apartment_page_2, container, false);

        locationApiHandler = new LocationApiHandler(getActivity());

        final MapView mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapViewHandler = new MapViewHandler(getContext(), this, mapView, savedInstanceState);
        mapViewHandler.addOnMapReadyListener(this);
        mapViewHandler.initMapAsync();

        searchAddressTextInputLayout = (TextInputLayout) rootView.findViewById(R.id
                .searchAddressTextInputLayout);
        searchAddressEditText = (EditText) rootView.findViewById(R.id.searchAddressEditText);

        ImageButton searchAddressButton = (ImageButton) rootView.findViewById(R.id.searchAddressImageButton);
        searchAddressButton.setImageDrawable(IconicsHelper.getSearchIcon(getActivity(), android.R.color.white));
        searchAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LatLng location = mapViewHandler.findCoordinates(searchAddressEditText.getText().toString());

                    if (geofence == null) {
                        geofence = mapViewHandler.addGeofence(location, currentGeofenceRadius);
                    } else {
                        geofence.setCenter(location);
                        geofence.setRadius(currentGeofenceRadius);
                    }
                    mapViewHandler.moveCamera(location, 14, true);

                    searchAddressTextInputLayout.setError(null);
                    searchAddressTextInputLayout.setErrorEnabled(false);

                    sendSetupApartmentChangedBroadcast(getContext());
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
                    sendSetupApartmentChangedBroadcast(getContext());
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

                sendSetupApartmentChangedBroadcast(getContext());
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                currentName = intent.getStringExtra("name");
                currentCheckedGateways = (ArrayList<Gateway>) intent.getSerializableExtra("checkedGateways");

                sendSetupApartmentChangedBroadcast(getContext());
            }
        };

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

        if (geofence != null) {
            geofence.setRadius(radius);
        }
    }

    private void initializeApartmentData(long apartmentId) {
        try {
            Apartment apartment = DatabaseHandler.getApartment(apartmentId);
            currentName = apartment.getName();
            currentCheckedGateways = apartment.getAssociatedGateways();

            if (apartment.getGeofence().getCenterLocation() != null) {
                double radius = apartment.getGeofence().getRadius();
                geofenceRadiusSeekbar.setProgress((int) radius);
                geofenceRadiusEditText.setText((int) radius);

                updateGeofenceRadius(radius);
            } else {
                updateGeofenceRadius(100);
            }


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
                        new eu.power_switch.google_play_services.geofence.Geofence(
                                (long) -1, true, currentName, geofence.getMarker().getPosition(), currentGeofenceRadius)
                );

                try {
                    DatabaseHandler.addApartment(newApartment);
                } catch (Exception e) {
                    StatusMessageHandler.showStatusMessage(rootView.getContext(),
                            R.string.unknown_error, Snackbar.LENGTH_LONG);
                }
            } else {
                DatabaseHandler.updateApartment(apartmentId, currentName, currentCheckedGateways, geofence.getMarker()
                        .getPosition(), currentGeofenceRadius);
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
        return true;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            googleMap.setMyLocationEnabled(true);
        }

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMapToolbarEnabled(false);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(latLng.toString());

                if (geofence == null) {
                    geofence = mapViewHandler.addGeofence(latLng, currentGeofenceRadius);
                } else {
                    geofence.setCenter(latLng);
                    geofence.setRadius(currentGeofenceRadius);
                }

                try {
                    String address = mapViewHandler.findAddress(latLng);
                    if (address != null && address.length() > 0) {
                        searchAddressEditText.setText(address);
                    }
                } catch (AddressNotFoundException e) {
                    Log.e(e);
                }

                sendSetupApartmentChangedBroadcast(getContext());
            }
        });
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (geofence.getMarker().getId().equals(marker.getId())) {
                    geofence.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (geofence.getMarker().getId().equals(marker.getId())) {
                    geofence.setCenter(marker.getPosition());
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (geofence.getMarker().getId().equals(marker.getId())) {
                    geofence.setCenter(marker.getPosition());

                    try {
                        String address = mapViewHandler.findAddress(marker.getPosition());
                        if (address != null && address.length() > 0) {
                            searchAddressEditText.setText(address);
                        }
                    } catch (AddressNotFoundException e) {
                        Log.e(e);
                    }

                    sendSetupApartmentChangedBroadcast(getContext());
                }
            }
        });

        if (apartmentId != -1) {
            try {
                Apartment apartment = DatabaseHandler.getApartment(apartmentId);
                if (geofence != null) {
                    geofence.remove();
                }
                if (apartment.getGeofence().getCenterLocation() != null) {
                    geofence = mapViewHandler.addGeofence(apartment.getGeofence().getCenterLocation(), apartment
                            .getGeofence().getRadius());
                    mapViewHandler.moveCamera(apartment.getGeofence().getCenterLocation(), 14, false);
                }
            } catch (Exception e) {
                Log.e(e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        locationApiHandler.connect();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_NAME_APARTMENT_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
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
        locationApiHandler.disconnect();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapViewHandler.getMapView().onLowMemory();
    }
}

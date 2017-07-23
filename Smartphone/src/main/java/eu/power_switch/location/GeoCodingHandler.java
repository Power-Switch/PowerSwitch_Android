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

package eu.power_switch.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by Markus on 23.07.2017.
 */
@Singleton
public class GeoCodingHandler {

    private Context  context;
    private Geocoder geoCoder;

    @Inject
    public GeoCodingHandler(Context context) {
        this.context = context;

        geoCoder = new Geocoder(context, Locale.getDefault());
    }

    /**
     * Find a address description for a given coordinate
     *
     * @param latLng coordinate
     *
     * @return list of addresses around the given location
     */
    @NonNull
    @CheckResult
    @WorkerThread
    public List<Address> findAddress(@NonNull LatLng latLng, int maxResultsCount) {
        /* get latitude and longitude from the address */
        try {
            return geoCoder.getFromLocation(latLng.latitude, latLng.longitude, maxResultsCount);
        } catch (IOException e) {
            Timber.e(e);
        }

        return Collections.emptyList();
    }

    /**
     * Find a address description for a given coordinate
     *
     * @param latLng coordinate
     *
     * @return address closest to the given location or {@code null}
     */
    @Nullable
    @CheckResult
    @WorkerThread
    public Address findAddress(@NonNull LatLng latLng) {
        List<Address> addresses = findAddress(latLng, 1);
        if (addresses.size() != 0) {
            return addresses.get(0);
        } else {
            return null;
        }
    }

    /**
     * @param latLng coordinate
     *
     * @return street name + house number, or null
     */
    @Nullable
    @CheckResult
    @WorkerThread
    public String getStreetDescription(@NonNull LatLng latLng) {
        List<Address> addresses = findAddress(latLng, 1);

        if (addresses.size() > 0) {
            Address address = addresses.get(0);

            String addressAsString = address.getAddressLine(0);

            Timber.d("Address: ", addressAsString);
            return addressAsString;
        }

        return null;
    }

    /**
     * Find the complete address for a short address description
     *
     * @param address address as text
     *
     * @return list of addresses matching the given description
     */
    @NonNull
    @CheckResult
    @WorkerThread
    public List<Address> findAddress(@NonNull String address, int maxResultsCount) {
        /* get latitude and longitude from the address */
        try {
            return geoCoder.getFromLocationName(address, maxResultsCount);
        } catch (IOException e) {
            Timber.e(e);
        }

        return Collections.emptyList();
    }

    /**
     * Find the complete address for a short address description
     *
     * @param address address as text
     *
     * @return address matching the given description or {@code null}
     */
    @Nullable
    @CheckResult
    @WorkerThread
    public Address findAddress(@NonNull String address) {
        List<Address> addresses = findAddress(address, 1);
        if (addresses.size() != 0) {
            return addresses.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get the location of an address
     *
     * @param address address
     *
     * @return location
     */
    @NonNull
    @CheckResult
    public LatLng getLocation(@NonNull Address address) {
        return new LatLng(address.getLatitude(), address.getLongitude());
    }

}

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

package eu.power_switch.gui.dialog.configuration.holder;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.action.Action;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.dialog.configuration.ConfigurationHolder;
import eu.power_switch.shared.constants.GeofenceConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Markus on 04.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GeofenceConfigurationHolder extends ConfigurationHolder {

    private Geofence geofence;

    private Long apartmentId;

    private String name;

    private LatLng location;

    private double radius = GeofenceConstants.DEFAULT_GEOFENCE_RADIUS;

    private Bitmap snapshot;

    private List<Action> enterActions = new ArrayList<>();

    private List<Action> exitActions = new ArrayList<>();

    @Override
    public boolean isValid() {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        if (radius == -1) {
            return false;
        }

        if (location == null) {
            return false;
        }

        if (snapshot == null) {
            return false;
        }

        if (enterActions == null || exitActions == null) {
            return false;
        }
        if (enterActions.size() == 0 && exitActions.size() == 0) {
            return false;
        }

        return true;
    }
}

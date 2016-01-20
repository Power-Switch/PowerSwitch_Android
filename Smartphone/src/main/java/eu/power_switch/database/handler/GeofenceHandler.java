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

package eu.power_switch.database.handler;

import com.google.android.gms.location.Geofence;

/**
 * Provides database methods for managing Geofences
 * <p/>
 * Created by Markus on 20.01.2016.
 */
abstract class GeofenceHandler {

    protected static void add() {

    }

    protected static Geofence get(Long id) {

        return new Geofence() {
            @Override
            public String getRequestId() {
                return null;
            }
        };
    }

}

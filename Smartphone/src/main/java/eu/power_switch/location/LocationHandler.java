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

import android.location.Location;

/**
 * Interface for a location handler
 * <p>
 * Created by Markus on 22.07.2017.
 */
public interface LocationHandler {

    /**
     * Get the last known location without requesting a position update by the system
     *
     * @return the last known location
     */
    Location getLastLocation();

    /**
     * Add a location listener
     *
     * @param listener the listener to add
     *
     * @return true if the listener was added, false otherwise
     */
    boolean addLocationListener(LocationListener listener);

    /**
     * Remove a location listener
     *
     * @param listener the listener to remove
     *
     * @return true if the listener was removed, false otherwise
     */
    boolean removeLocationListener(LocationListener listener);

}
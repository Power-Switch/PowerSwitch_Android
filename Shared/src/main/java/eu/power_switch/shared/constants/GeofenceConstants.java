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

package eu.power_switch.shared.constants;

/**
 * Class holding constants related to Settings for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 11.11.2015.
 */
public class GeofenceConstants {

    // Tabs
    public static final int APARTMENTS_TAB_INDEX = 0;
    public static final int CUSTOM_TAB_INDEX     = 1;

    public static final int DEFAULT_LOITERING_DELAY = 30;

    /**
     * Default Geofence radius
     */
    public static final int DEFAULT_GEOFENCE_RADIUS = 500;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private GeofenceConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}

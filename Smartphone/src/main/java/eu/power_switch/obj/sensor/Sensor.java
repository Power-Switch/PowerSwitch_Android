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

package eu.power_switch.obj.sensor;

/**
 * This class represents a network device that is only able to broadcast data to the smartphone application, like a
 * temperature sensor, light intensity sensor etc.
 * <p/>
 * Created by Markus on 15.01.2016.
 */
public abstract class Sensor {

    /**
     * ID of this Sensor
     */
    Long id;

    /**
     * Constructor
     *
     * @param id ID of this Sensor
     */
    public Sensor(Long id) {
        this.id = id;
    }

}

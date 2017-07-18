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

package eu.power_switch.obj.communicator;

import org.json.JSONObject;

import java.util.Map;

import eu.power_switch.network.NetworkResponseCallback;
import eu.power_switch.obj.gateway.Gateway;
import lombok.Data;
import lombok.ToString;

/**
 * This class represents a network device, that is able to send data to the smartphone application as well as receive
 * data from it for executing actions or setting values
 * <p/>
 * Created by Markus on 15.01.2016.
 */
@Data
@ToString
public abstract class Communicator implements NetworkResponseCallback {

    /**
     * ID of this communicator
     */
    protected Long id;

    /**
     * Name of this Communicator
     */
    protected String name;

    /**
     * Brand of this Communicator
     */
    protected String brand;

    /**
     * Model of this Communicator
     */
    protected String model;

    /**
     *
     */
    protected Map<Object, Object> values;

    /**
     * Constructor
     *
     * @param id ID of this Communicator
     */
    public Communicator(Long id) {
        this.id = id;
    }

    public Object getValue(Gateway gateway, Object key) {
        return values.get(key);
    }

    public abstract JSONObject setValue(Gateway gateway, Object key, Object value);

}

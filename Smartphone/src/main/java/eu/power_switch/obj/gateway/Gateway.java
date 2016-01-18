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

package eu.power_switch.obj.gateway;

import java.util.HashSet;
import java.util.Set;

import eu.power_switch.network.NetworkPackage;

/**
 * Represents a Gateway that can receive network packages from the app and convert them for the actual wireless devices
 */
public abstract class Gateway {

    /**
     * ID of this Gateway
     */
    protected Long id;
    /**
     * Boolean whether this Gateway is active
     */
    protected boolean active;
    /**
     * Name of this Gateway
     */
    protected String name;
    /**
     * Model name of this Gateway
     */
    protected String model;
    /**
     * Firmware version of this Gateway
     */
    protected String firmware;
    /**
     * Host address where this Gateway is accessible
     */
    protected String host;
    /**
     * Port on which data can be exchanged with this Gateway
     */
    protected int port;

    protected Set<Capability> capabilities = new HashSet<>();

    /**
     * Constructor
     *
     * @param id
     * @param active   true if this gateway is used to send network signals
     * @param name     name of this gateway
     * @param model    model of this gateway
     * @param firmware firmware version of this gateway
     * @param address  host address of this gateway
     * @param port     port of this gateway
     */
    public Gateway(long id, boolean active, String name, String model, String firmware, String address, int port) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.model = model;
        this.firmware = firmware;
        this.host = address;
        this.port = port;
    }

    /**
     * Returns the time to wait between sending NetworkPackages to not overwhelm the Gateway
     *
     * @return time in milliseconds
     */
    public abstract int getTimeout();

    /**
     * Get ID of this Gateway
     *
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns whether this Gateway is active or not
     * (if it will be used by the App to send packets)
     *
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets active status of this Gateway
     *
     * @param active true if active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Get Host address of this Gateway
     *
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * Get name of this Gateway
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get model name of this Gateway
     *
     * @return
     */
    public String getModel() {
        return model;
    }

    /**
     * Get firmware version of this Gateway
     *
     * @return
     */
    public String getFirmware() {
        return firmware;
    }

    /**
     * Get port over which data can be exchanged with this Gateway
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns a set of {@link Capability} this gateway has
     *
     * @return
     */
    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    /**
     * @return String representation of this Gateway
     */
    @Override
    public String toString() {
        return name + " (" + model + ", firmware: " + firmware + "): " + host + ":" + port;
    }

    /**
     * Compare host address and port to another Gateway
     *
     * @param gateway
     * @return
     */
    public boolean hasSameAddress(Gateway gateway) {
        return (gateway.getHost().equals(this.host) && gateway.getPort() == this.port);
    }

    public abstract String getModelAsString();

    public abstract NetworkPackage.CommunicationType getCommunicationType();

    public enum Capability {
        SEND,
        RECEIVE
    }
}

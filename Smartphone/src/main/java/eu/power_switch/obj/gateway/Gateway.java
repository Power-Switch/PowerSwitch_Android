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

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

import eu.power_switch.shared.constants.DatabaseConstants;
import eu.power_switch.shared.log.LogHandler;

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
     * Host address where this Gateway is accessible via local network (WiFi/Lan etc.)
     */
    protected String localHost;
    /**
     * Port on which data can be exchanged with this Gateway via local network (WiFi/Lan etc.)
     */
    protected Integer localPort;
    /**
     * Host address where this Gateway is accessible via WAN
     */
    protected String wanHost;
    /**
     * Port on which data can be exchanged with this Gateway via WAN
     */
    protected Integer wanPort;

    protected Set<String> ssids;

    protected Set<Capability> capabilities = new HashSet<>();

    /**
     * Constructor
     *
     * @param id        ID of this Gateway
     * @param active    true if this gateway is used to send network signals
     * @param name      name of this gateway
     * @param model     model of this gateway
     * @param firmware  firmware version of this gateway
     * @param localHost localHost address of this gateway
     * @param localPort localPort of this gateway
     */
    public Gateway(long id, boolean active, @NonNull String name, @NonNull String model, @NonNull String firmware, @NonNull String localHost, @NonNull Integer localPort, @NonNull String wanHost, @NonNull Integer wanPort, @NonNull Set<String> ssids) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.model = model;
        this.firmware = firmware;
        this.localHost = localHost;
        this.localPort = localPort;
        this.wanHost = wanHost;
        this.wanPort = wanPort;
        this.ssids = ssids;
    }

    /**
     * Constructor
     * <p>
     * Variant allowing an Integer as an ID (used for testing)
     *
     * @param id        ID of this Gateway
     * @param active    true if this gateway is used to send network signals
     * @param name      name of this gateway
     * @param model     model of this gateway
     * @param firmware  firmware version of this gateway
     * @param localHost localHost address of this gateway
     * @param localPort localPort of this gateway
     */
    public Gateway(int id, boolean active, @NonNull String name, @NonNull String model, @NonNull String firmware, @NonNull String localHost, @NonNull Integer localPort, @NonNull String wanHost, @NonNull Integer wanPort, @NonNull Set<String> ssids) {
        this((long) id, active, name, model, firmware, localHost, localPort, wanHost, wanPort, ssids);
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
     * @return ID of this Gateway
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns whether this Gateway is active or not
     * (if it will be used by the App to send packets)
     *
     * @return true if active, false otherwise
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
     * Get local Host address of this Gateway
     *
     * @return Host address of this Gateway
     */
    @NonNull
    public String getLocalHost() {
        return localHost;
    }

    /**
     * Get local Port over which data can be exchanged with this Gateway
     *
     * @return local Port of this Gateway
     */
    @NonNull
    public Integer getLocalPort() {
        if (DatabaseConstants.INVALID_GATEWAY_PORT.equals(localPort) && getLocalHost().length() > 0) {
            return getDefaultLocalPort();
        } else {
            return localPort;
        }
    }

    public boolean hasValidLocalAddress() {
        return getLocalHost().length() > 0 && getLocalPort() != 0;
    }

    protected abstract Integer getDefaultLocalPort();

    /**
     * Get WAN Host address of this Gateway
     *
     * @return WAN Host address of this Gateway
     */
    @NonNull
    public String getWanHost() {
        return wanHost;
    }

    /**
     * Get WAN Port over which data can be exchanged with this Gateway
     *
     * @return WAN Port of this Gateway
     */
    @NonNull
    public Integer getWanPort() {
        if (DatabaseConstants.INVALID_GATEWAY_PORT.equals(wanPort) && getWanHost().length() > 0) {
            return getDefaultLocalPort();
        } else {
            return wanPort;
        }
    }

    public boolean hasValidWanAddress() {
        return getWanHost().length() > 0 && getWanPort() > 0;
    }

    /**
     * Get name of this Gateway
     *
     * @return Name of this Gateway
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Get model name of this Gateway
     *
     * @return Model of this Gateway
     */
    @NonNull
    public String getModel() {
        return model;
    }

    /**
     * Get firmware version of this Gateway
     *
     * @return Firmware of this Gateway
     */
    @NonNull
    public String getFirmware() {
        return firmware;
    }

    /**
     * Get set of SSIDs that should always communicate via local address
     *
     * @return Set of SSIDs
     */
    @NonNull
    public Set<String> getSsids() {
        return ssids;
    }

    /**
     * Returns a set of {@link Capability} this gateway has
     *
     * @return Set of Capabilities
     */
    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    /**
     * @return String representation of this Gateway
     */
    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Gateway: ").append(name)
                .append(" (").append(model).append(", firmware: ").append(firmware).append("): \n");

        if (hasValidLocalAddress()) {
            stringBuilder.append(LogHandler.addIndentation("Local: " + localHost + ":" + localPort)).append("\n");
        }
        if (hasValidWanAddress()) {
            stringBuilder.append(LogHandler.addIndentation("WAN: " + wanHost + ":" + wanPort)).append("\n");
        }

        return stringBuilder.toString();
    }

    /**
     * Compare localHost address and localPort to another Gateway
     *
     * @param gateway Gateway to compare with this Gateway
     * @return true if local address & local port match
     */
    public boolean hasSameLocalAddress(@NonNull Gateway gateway) {
        return String.valueOf(gateway.getLocalHost()).equals(String.valueOf(getLocalHost())) &&
                (String.valueOf(gateway.getLocalPort()).equals(String.valueOf(getLocalPort())));
    }

    public abstract CommunicationProtocol getCommunicationProtocol();

    public enum CommunicationProtocol {
        UDP,
        TCP,
        HTTP
    }

    public enum Capability {
        SEND,
        RECEIVE
    }
}

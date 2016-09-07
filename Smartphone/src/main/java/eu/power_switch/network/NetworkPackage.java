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

package eu.power_switch.network;

import java.io.Serializable;

/**
 * Internal representation of a network package that is used in NetworkHandler
 */
public class NetworkPackage implements Serializable {

    /**
     * Destination localHost of this network package
     */
    private String host;
    /**
     * Destination localPort of this network package
     */
    private int port;
    /**
     * Message of this network package
     */
    private String message;
    /**
     * Time to wait after sending this package before sending the next one
     */
    private int timeout;
    /**
     * Type of this Network Package
     */
    private CommunicationType communicationType;

    /**
     * Default Constructor
     *
     * @param host    Host address
     * @param port    Port
     * @param message Message
     * @param timeout Timeout
     */
    public NetworkPackage(CommunicationType communicationType, String host, int port, String message, int timeout) {
        this.communicationType = communicationType;
        this.host = host;
        this.port = port;
        this.message = message;
        this.timeout = timeout;
    }

    /**
     * Get Type of this Network Package
     *
     * @return type {@see CommunicationType}
     */
    public CommunicationType getCommunicationType() {
        return communicationType;
    }

    /**
     * Get Host address that this NetworkPackage should be sent to
     *
     * @return localHost address
     */
    public String getHost() {
        return host;
    }

    /**
     * Get Port that this NetworkPackage should be sent to
     *
     * @return localPort
     */
    public int getPort() {
        return port;
    }

    /**
     * Get message that should be sent
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get time to wait after sending the NetworkPackage
     * <p/>
     * This is used to avoid conflicts between signals if multiple signals are to be sent in a short amount of time.
     * Different values are used for different Gateways.
     *
     * @return timeout of this NetworkPackage
     * @see eu.power_switch.obj.gateway.Gateway#getTimeout()
     */
    public int getTimeout() {
        return timeout;
    }

    public enum CommunicationType {
        UDP,
        HTTP
    }
}

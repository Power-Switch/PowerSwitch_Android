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

import java.util.Set;

import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.communicator.Communicator;
import eu.power_switch.obj.sensor.Sensor;

/**
 * Created by Markus on 15.01.2016.
 */
public class EZControl_XS1 extends Gateway {

    /**
     * Model constant
     */
    public static final String MODEL = "EZcontrol XS1";

    private Set<Communicator> communicators;
    private Set<Sensor> sensors;

    public EZControl_XS1(Long id, boolean active, String name, String firmware, String localAddress, int localPort, String wanAddress, int wanPort, Set<String> ssids) {
        super(id, active, name, MODEL, firmware, localAddress, localPort, wanAddress, wanPort, ssids);
        capabilities.add(Capability.SEND);
        capabilities.add(Capability.RECEIVE);

        requestActors();
        requestSensors();
    }

    private void requestActors() {
        communicators = NetworkHandler.getActors(this);
    }

    private void requestSensors() {
        sensors = NetworkHandler.getSensors(this);
    }

    public Set<Communicator> getCommunicators() {
        return communicators;
    }

    public Set<Sensor> getSensors() {
        return sensors;
    }

    @Override
    protected Integer getDefaultLocalPort() {
        return 0;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public NetworkPackage.CommunicationType getCommunicationType() {
        return NetworkPackage.CommunicationType.HTTP;
    }
}

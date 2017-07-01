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

package eu.power_switch.obj.communicator.device.elv;

import org.json.JSONObject;

import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.network.UdpNetworkPackage;
import eu.power_switch.obj.HeatingControl;
import eu.power_switch.obj.communicator.Communicator;
import eu.power_switch.obj.gateway.Gateway;
import timber.log.Timber;

/**
 * ELV FHT80B-2/3 Heating Control
 * <p/>
 * Created by Markus on 15.01.2016.
 */
public class FHT80B extends Communicator implements HeatingControl {

    /**
     * Currently set targetTemperature
     */
    private double targetTemperature;

    public FHT80B(Long id) {
        super(id);
    }

    public void requestValue(Gateway gateway, Object key) {
        String signal = getSignal(gateway, key);

        NetworkPackage networkPackage;
        switch (gateway.getCommunicationProtocol()) {
            case UDP:
                // todo: local/wan logic
                networkPackage = new UdpNetworkPackage(gateway.getLocalHost(), gateway
                        .getLocalPort(), signal, gateway.getTimeout());
                break;
            case TCP:
            case HTTP:
            default:
                throw new RuntimeException("Unknown Communication Protocol");
        }

        NetworkHandler.send(this, networkPackage);
    }


    @Override
    public JSONObject setValue(Gateway gateway, Object key, Object value) {
        return null;
    }

    @Override
    public double getTargetTemperature() {
        return targetTemperature;
    }

    @Override
    public void setTargetTemperature(double targetTemperature) throws Exception {
        // TODO: sende Befehl an Gateway

    }

    private String getSignal(Gateway gateway, Object key) {
        return null;
    }

    @Override
    public void receiveResponse(String key, String message) {
        Timber.d("received response: ", message);
    }
}

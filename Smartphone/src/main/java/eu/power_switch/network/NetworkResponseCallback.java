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
 * Interface used to send network response messages (received after request or from a sensor) to the requesting party
 * <p/>
 * Created by Markus on 18.01.2016.
 */
public interface NetworkResponseCallback extends Serializable {

    /**
     * This Method is called when a response is received after a request has been sent to a gateway
     *
     * @param key     request key
     * @param message response message
     */
    void receiveResponse(String key, String message);

}

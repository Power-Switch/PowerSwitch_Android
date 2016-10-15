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

package eu.power_switch.shared.exception.gateway;

/**
 * Created by Markus on 16.07.2015.
 */
public class GatewayAlreadyExistsException extends Exception {

    private final long idOfExistingGateway;

    public GatewayAlreadyExistsException(long idOfExistingGateway) {
        super("Gateway already exists!");
        this.idOfExistingGateway = idOfExistingGateway;
    }

    public long getIdOfExistingGateway() {
        return idOfExistingGateway;
    }
}

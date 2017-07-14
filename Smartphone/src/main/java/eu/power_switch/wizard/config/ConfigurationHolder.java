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

package eu.power_switch.wizard.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.power_switch.database.handler.DatabaseHandlerStatic;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.gateway.Gateway;
import lombok.Data;
import timber.log.Timber;

/**
 * This class is a container for all values that are configured during the wizard configuration process.
 * <p>
 * Created by Markus on 27.06.2017.
 */
@Data
public class ConfigurationHolder {

    private String        apartmentName;
    private String        roomName;
    private List<Gateway> gateways;

    /**
     * Writes the configuration to the Database
     */
    public void writeToDatabase() {
        try {
            // Add apartment
            long apartmentId = DatabaseHandlerStatic.addApartment(new Apartment(0L, true, apartmentName, Collections.EMPTY_LIST, null));
            // add room to apartment
            DatabaseHandlerStatic.addRoom(new Room(0L, apartmentId, roomName, 0, false, new ArrayList<Gateway>()));
            // add found gateways to DB and associate them with the apartment above
//            DatabaseHandler.addGateway();

        } catch (Exception e) {
            Timber.e(e);
        }
    }
}

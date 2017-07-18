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

package eu.power_switch.gui.dialog.configuration.holder;

import java.util.List;

import eu.power_switch.gui.dialog.configuration.ConfigurationHolder;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.Brand;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Markus on 03.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReceiverConfigurationHolder extends ConfigurationHolder {

    private Apartment parentApartment;

    private Room parentRoom;

    private String parentRoomName;

    private Receiver receiver;

    // Name

    private String name;

    // Brand/Model

    private Brand brand;

    private String model;


    ////// Configuration
    private Receiver.Type type;

    // Master/Slave
    private Character channelMaster;

    private Integer channelSlave;

    // Dip
    private List<DipSwitch> dips;

    // Universal
    private Long seed;

    private List<UniversalButton> universalButtons;

    // Gateway
    private int repetitionAmount = Receiver.MIN_REPETITIONS;

    private List<Gateway> gateways;

    /**
     * Checks if the current receiver name already exists in a room
     *
     * @param selectedRoom the room to check
     *
     * @return true if a receiver with the same name already exists, false otherwise
     */
    public boolean receiverNameAlreadyExists(Room selectedRoom) {
        for (Receiver receiver : selectedRoom.getReceivers()) {
            if (!(this.receiver != null && this.receiver.getId()
                    .equals(receiver.getId())) && receiver.getName()
                    .equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValid() {
        if (name == null || name.trim()
                .equals("")) {
            return false;
        }
        if (parentRoomName == null) {
            return false;
        }
        if (brand == null) {
            return false;
        }
        if (model == null) {
            return false;
        }

        if (type == null) {
            return false;
        }

        switch (type) {
            case DIPS:
                if (dips == null) {
                    return false;
                }
                break;
            case MASTER_SLAVE:
                if (channelMaster == null || channelMaster == '\u0000') {
                    return false;
                }
                if (channelSlave == null || channelSlave <= 0) {
                    return false;
                }
                break;
            case UNIVERSAL:
                if (universalButtons == null || universalButtons.size() == 0) {
                    return false;
                } else {
                    for (UniversalButton universalButton : universalButtons) {
                        if (universalButton.getName()
                                .length() == 0 || universalButton.getSignal()
                                .length() == 0) {
                            return false;
                        }
                    }
                }
                break;
            case AUTOPAIR:
                if (seed == null) {
                    return false;
                }
                break;
        }

        if (gateways == null) {
            return false;
        }

        return true;
    }
}

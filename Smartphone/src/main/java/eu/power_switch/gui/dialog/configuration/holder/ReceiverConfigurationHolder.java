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

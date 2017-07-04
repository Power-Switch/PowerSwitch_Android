package eu.power_switch.gui.dialog.configuration.holder;

import android.text.TextUtils;

import java.util.List;

import eu.power_switch.gui.dialog.configuration.ConfigurationHolder;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Markus on 03.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomConfigurationHolder extends ConfigurationHolder {

    private Room room;

    private List<Room> existingRooms;

    private Long id;

    private String name;

    private List<Receiver> receivers;

    private List<Gateway> associatedGateways;

    public boolean checkNameAlreadyExists() {
        for (Room room : existingRooms) {
            if (!room.getId()
                    .equals(id) && room.getName()
                    .equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValid() {
        if (TextUtils.isEmpty(name) || checkNameAlreadyExists()) {
            return false;
        }

        if (receivers == null) {
            return false;
        }

        if (associatedGateways == null) {
            return false;
        }

        return true;
    }

}

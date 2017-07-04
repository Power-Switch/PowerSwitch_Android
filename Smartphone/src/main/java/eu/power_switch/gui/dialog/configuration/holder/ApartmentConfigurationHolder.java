package eu.power_switch.gui.dialog.configuration.holder;

import java.util.List;

import eu.power_switch.gui.dialog.configuration.ConfigurationHolder;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.Gateway;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Markus on 03.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApartmentConfigurationHolder extends ConfigurationHolder {

    private Apartment apartment;

    private List<Apartment> existingApartments;

    private String name;

    private List<Gateway> associatedGateways;

    public boolean checkNameAlreadyExists() {
        for (Apartment apartment : existingApartments) {
            if (!(this.apartment != null && this.apartment.getId()
                    .equals(apartment.getId())) && apartment.getName()
                    .equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValid() {
        return name != null && name.trim()
                .length() > 0 && !checkNameAlreadyExists() && associatedGateways != null;
    }
}

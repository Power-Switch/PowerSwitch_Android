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

import android.text.TextUtils;

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
        if (name == null || TextUtils.isEmpty(name.trim())) {
            return false;
        }

        if (checkNameAlreadyExists()) {
            return false;
        }

        if (associatedGateways == null) {
            return false;
        }

        return true;
    }
}

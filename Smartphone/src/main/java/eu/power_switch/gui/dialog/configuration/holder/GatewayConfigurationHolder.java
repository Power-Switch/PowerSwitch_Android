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

package eu.power_switch.gui.dialog.configuration.holder;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.power_switch.gui.dialog.configuration.ConfigurationHolder;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.constants.DatabaseConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Markus on 03.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GatewayConfigurationHolder extends ConfigurationHolder {

    private Gateway gateway;

    private String name;

    private String model;

    private String  localAddress = "";
    private Integer localPort    = DatabaseConstants.INVALID_GATEWAY_PORT;
    private String  wanAddress   = "";
    private Integer wanPort      = DatabaseConstants.INVALID_GATEWAY_PORT;

    private Set<String> ssids = new HashSet<>(0);

    private List<Long> apartmentIds = new ArrayList<>(0);

    @Override
    public boolean isValid() {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        if (TextUtils.isEmpty(model)) {
            return false;
        }

        // as long as one of the address fields is filled in its ok
        if (TextUtils.isEmpty(localAddress) && TextUtils.isEmpty(wanAddress)) {
            return false;
        }

        return true;
    }

}

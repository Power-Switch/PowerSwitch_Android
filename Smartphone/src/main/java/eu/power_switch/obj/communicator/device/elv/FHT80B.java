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

import eu.power_switch.obj.HeatingControl;
import eu.power_switch.obj.communicator.Communicator;

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

    @Override
    public Object getValue(Object key) {
        return null;
    }

    @Override
    public double getTargetTemperature() {
        return targetTemperature;
    }

    @Override
    public void setTargetTemperature(double targetTemperature) throws Exception {
        // TODO: sende Befehl an Gateway

        this.targetTemperature = targetTemperature;
    }
}

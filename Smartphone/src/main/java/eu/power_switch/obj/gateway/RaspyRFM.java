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

package eu.power_switch.obj.gateway;

import android.support.annotation.NonNull;

import java.util.Set;

/**
 * RaspyRFM represents a RasperyPi ConnAir emulator Gateway
 */
public class RaspyRFM extends ConnAir {

    /**
     * Model constant
     */
    public static final String MODEL = "RaspyRFM";

    public RaspyRFM(Long id, boolean active, String name, String firmware, String localAddress, int localPort, String wanAddress, int wanPort, Set<String> ssids) {
        super(id, active, name, firmware, localAddress, localPort, wanAddress, wanPort, ssids);
        model = MODEL;
    }

    public RaspyRFM(Integer id, boolean active, String name, String firmware, String localAddress, int localPort, String wanAddress, int wanPort, @NonNull Set<String> ssids) {
        this(id.longValue(), active, name, firmware, localAddress, localPort, wanAddress, wanPort, ssids);
    }

}
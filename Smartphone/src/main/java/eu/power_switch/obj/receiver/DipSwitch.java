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

package eu.power_switch.obj.receiver;

/**
 * Represents a dip switch used in DipReceivers
 */
public class DipSwitch {

    /**
     * Name of Dip
     */
    private String name;

    /**
     * State of Dip
     */
    private boolean state;

    /**
     * Default Constructor
     *
     * @param name  Name of Dip
     * @param state State of Dip
     */
    public DipSwitch(String name, boolean state) {
        this.name = name;
        this.state = state;
    }

    /**
     * Get Name of Dip
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get state of Dip
     *
     * @return state (true if enabled)
     */
    public boolean isChecked() {
        return state;
    }

}

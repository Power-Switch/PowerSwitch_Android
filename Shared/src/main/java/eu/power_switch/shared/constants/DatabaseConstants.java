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

package eu.power_switch.shared.constants;

/**
 * Created by Markus on 29.02.2016.
 */
public class DatabaseConstants {

    /**
     * ID Constants used to identify static Buttons (not used for Universal Buttons)
     */
    public static final long BUTTON_ON_ID = -10; // negative values to prevent database conflicts
    public static final long BUTTON_OFF_ID = BUTTON_ON_ID - 1;
    public static final long BUTTON_UP_ID = BUTTON_ON_ID - 2;
    public static final long BUTTON_STOP_ID = BUTTON_ON_ID - 3;
    public static final long BUTTON_DOWN_ID = BUTTON_ON_ID - 4;

}

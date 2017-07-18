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

package eu.power_switch.shared.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class holding constants related to Api for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 11.11.2015.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiConstants {

    // DEPRECATED: Old intents from Versions older than 1.0
    /**
     * Intent used to switch on a Receiver
     */
    @Deprecated
    public static final String intent_switch_on  = "de.ressel.powerswitch.action.Switch.ON";
    /**
     * Intent used to switch off a Receiver
     */
    @Deprecated
    public static final String intent_switch_off = "de.ressel.powerswitch.action.Switch.OFF";
    /**
     * Intent used to switch on all Receivers in a Room
     */
    @Deprecated
    public static final String intent_room_on    = "de.ressel.powerswitch.action.Room.ON";
    /**
     * Intent used to switch off all Receivers in a Room
     */
    @Deprecated
    public static final String intent_room_off   = "de.ressel.powerswitch.action.Room.OFF";


    // NEW:

    public static final String UNIVERSAL_ACTION_INTENT = "eu.power_switch.action";

    public static final String KEY_BUTTON    = "Button";
    public static final String KEY_RECEIVER  = "Receiver";
    public static final String KEY_ROOM      = "Room";
    public static final String KEY_SCENE     = "Scene";
    public static final String KEY_APARTMENT = "Apartment";


    // Tasker Plugin
    public static final String KEY_REPLACE_VARIABLES_APARTMENT = "KEY_REPLACE_VARIABLES_APARTMENT";
    public static final String KEY_REPLACE_VARIABLES_ROOM      = "KEY_REPLACE_VARIABLES_ROOM";
    public static final String KEY_REPLACE_VARIABLES_RECEIVER  = "KEY_REPLACE_VARIABLES_RECEIVER";
    public static final String KEY_REPLACE_VARIABLES_BUTTON    = "KEY_REPLACE_VARIABLES_BUTTON";
    public static final String KEY_REPLACE_VARIABLES_SCENE     = "KEY_REPLACE_VARIABLES_SCENE";

}

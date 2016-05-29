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

package eu.power_switch.phone;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Helper class for Phone related stuff
 * <p/>
 * Created by Markus on 29.05.2016.
 */
public class PhoneHelper {

    /**
     * Check if the device is able to receive calls
     *
     * @param context any suitable context
     * @return true if calls are supported, false otherwise
     */
    public static boolean isCallingSupported(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

}

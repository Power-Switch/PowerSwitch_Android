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

package eu.power_switch.nfc;

import android.content.Context;
import android.nfc.NfcAdapter;

/**
 * This class is responsible for writing actions to NFC Tags
 * <p/>
 * Created by Markus on 24.03.2016.
 */
public class NfcHandler {

    /**
     * Checks if NFC is supported by the device
     *
     * @param context any suitable context
     * @return true if NFC is supported, false otherwise
     */
    public static boolean isNfcSupported(Context context) {
        return NfcAdapter.getDefaultAdapter(context) != null;
    }

}

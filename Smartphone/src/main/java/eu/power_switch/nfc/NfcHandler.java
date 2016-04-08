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

import android.app.Activity;
import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

import java.io.IOException;

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

    /**
     * Checks if NFC is supported by the device
     *
     * @param context any suitable context
     * @return true if NFC is supported, false otherwise
     */
    public static boolean isNfcEnabled(Context context) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        return nfcAdapter.isEnabled();
    }

    /**
     * Converts a Long into a NdefMessage in application/vnd.facebook.places MIMEtype.
     * <p/>
     * for writing Places
     */
    public static NdefMessage getAsNdef(String content) {
        byte[] textBytes = content.getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "application/vnd.facebook.places".getBytes(), new byte[]{}, textBytes);
        return new NdefMessage(new NdefRecord[]{textRecord});
    }

    /**
     * Writes an NdefMessage to a NFC tag
     */
    public static void writeTag(NdefMessage message, Tag tag) throws Exception {
        int size = message.toByteArray().length;
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            ndef.connect();
            if (!ndef.isWritable()) {
                return false;
            }
            if (ndef.getMaxSize() < size) {
                return false;
            }
            ndef.writeNdefMessage(message);
            return true;
        } else {
            NdefFormatable format = NdefFormatable.get(tag);
            if (format != null) {
                try {
                    format.connect();
                    format.format(message);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public static void soundNotify(Context context) {

    }
}

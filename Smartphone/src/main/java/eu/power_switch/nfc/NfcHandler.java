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
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

import eu.power_switch.shared.exception.nfc.NfcTagInsufficientMemoryException;
import eu.power_switch.shared.exception.nfc.NfcTagNotWritableException;

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
        NdefRecord textRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "application/eu.power_switch".getBytes(),
                new byte[]{},
                textBytes);
        return new NdefMessage(new NdefRecord[]{
                textRecord,
                NdefRecord.createApplicationRecord("eu.power_switch")});
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
                throw new NfcTagNotWritableException();
            }
            if (ndef.getMaxSize() < size) {
                throw new NfcTagInsufficientMemoryException(ndef.getMaxSize(), size);
            }
            ndef.writeNdefMessage(message);
        } else {
            NdefFormatable format = NdefFormatable.get(tag);
            if (format != null) {
                format.connect();
                format.format(message);
            } else {
                throw new IllegalArgumentException("Ndef format is NULL");
            }
        }
    }

    /**
     * Play NFC Tag found notification sound
     *
     * @param context any suitable context
     */
    public static void soundNotify(Context context) {
//        MediaPlayer mp = MediaPlayer.create(context, );
//        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                mp.release();
//            }
//        });
//        mp.start();
    }
}

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
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import eu.power_switch.shared.log.Log;

/**
 * Hidden Activity to receiver NFC Tag messages without displaying any user interface
 * <p/>
 * Created by Markus on 09.04.2016.
 */
public class HiddenReceiverActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();

        // Get NFC Tag intent
        Intent intent = getIntent();

        Log.d(this, intent);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

//            String type = intent.getType();
//            Uri uri = intent.getData();

//            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Log.d("NFC Data: " + readNfcTag(intent));
        }

        // close hidden activity afterwards
        finish();
    }

    private String readNfcTag(Intent intent) {
        String value = "";

        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }

            for (NdefMessage message : msgs) {
                for (NdefRecord record : message.getRecords()) {
                    value += new String(record.getPayload());
                }
            }

            return value;
        }

        return "none";
    }
}

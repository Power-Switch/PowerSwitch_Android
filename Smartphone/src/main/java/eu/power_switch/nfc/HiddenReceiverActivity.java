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
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.log.Log;

/**
 * Hidden Activity to receiver NFC Tag messages without displaying any user interface
 * <p/>
 * Created by Markus on 09.04.2016.
 */
public class HiddenReceiverActivity extends Activity {

    public static final String KEY_APARTMENT = "Apartment:";
    public static final String KEY_ROOM = "Room:";
    public static final String KEY_RECEIVER = "Receiver:";
    public static final String KEY_BUTTON = "Button:";
    public static final String KEY_SCENE = "Scene:";

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

            Log.d("NFC Data: " + readNfcTagPayload(intent));
            executeAction(readNfcTagPayload(intent));
        }

        // close hidden activity afterwards
        finish();
    }

    private String readNfcTagPayload(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }

            return new String(msgs[0].getRecords()[0].getPayload());
        }

        return null;
    }

    private void executeAction(String content) {
        if (content == null) {
            return;
        }

        try {
            if (content.contains(KEY_APARTMENT)) {
                int start;
                int stop;

                start = KEY_APARTMENT.length();
                stop = content.indexOf(KEY_ROOM);
                String apartmentName = content.substring(start, stop);

                if (content.contains(KEY_ROOM) && content.contains(KEY_RECEIVER) && content.contains(KEY_BUTTON)) {
                    start = stop + KEY_ROOM.length();
                    stop = content.indexOf(KEY_RECEIVER);
                    String roomName = content.substring(start, stop);

                    start = stop + KEY_RECEIVER.length();
                    stop = content.indexOf(KEY_BUTTON);
                    String receiverName = content.substring(start, stop);

                    start = stop + KEY_BUTTON.length();
                    stop = content.length();
                    String buttonName = content.substring(start, stop);

                    Apartment apartment = DatabaseHandler.getApartment(apartmentName);
                    Room room = apartment.getRoom(roomName);
                    Receiver receiver = room.getReceiver(receiverName);
                    Button button = receiver.getButton(buttonName);

                    ActionHandler.execute(this, receiver, button);
                } else if (content.contains(KEY_ROOM) && content.contains(KEY_BUTTON)) {
                    start = stop + KEY_ROOM.length();
                    stop = content.indexOf(KEY_BUTTON);
                    String roomName = content.substring(start, stop);

                    start = stop + KEY_BUTTON.length();
                    stop = content.length();
                    String buttonName = content.substring(start, stop);

                    Apartment apartment = DatabaseHandler.getApartment(apartmentName);
                    Room room = apartment.getRoom(roomName);

                    ActionHandler.execute(this, room, buttonName);
                } else if (content.contains(KEY_SCENE)) {
                    start = stop + KEY_SCENE.length();
                    stop = content.length();
                    String sceneName = content.substring(start, stop);

                    Apartment apartment = DatabaseHandler.getApartment(apartmentName);
                    Scene scene = apartment.getScene(sceneName);

                    ActionHandler.execute(this, scene);
                }
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(this, e);
        }
    }
}

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

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import javax.inject.Inject;

import dagger.android.DaggerActivity;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.persistence.PersistanceHandler;
import timber.log.Timber;

/**
 * Hidden Activity to receive NFC Tag messages without displaying any user interface
 * <p/>
 * Created by Markus on 09.04.2016.
 */
public class HiddenReceiverActivity extends DaggerActivity {

    public static final String KEY_APARTMENT = "Apartment:";
    public static final String KEY_ROOM = "Room:";
    public static final String KEY_RECEIVER = "Receiver:";
    public static final String KEY_BUTTON = "Button:";
    public static final String KEY_SCENE = "Scene:";

    @Inject
    ActionHandler actionHandler;

    @Inject
    PersistanceHandler persistanceHandler;

    @Inject
    StatusMessageHandler statusMessageHandler;

    @Override
    protected void onResume() {
        super.onResume();
        // Get NFC Tag intent
        Intent intent = getIntent();

        Timber.d("Intent: ", intent);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
//            String type = intent.getType();
//            Uri uri = intent.getData();
//            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String payload = readNfcTagPayload(intent);

            Timber.d("NFC Data: " + payload);
            executeAction(payload);
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

                if (content.contains(KEY_ROOM) && content.contains(KEY_RECEIVER) && content.contains(KEY_BUTTON)) {
                    start = KEY_APARTMENT.length();
                    stop = content.indexOf(KEY_ROOM);
                    Long apartmentId = Long.valueOf(content.substring(start, stop));

                    start = stop + KEY_ROOM.length();
                    stop = content.indexOf(KEY_RECEIVER);
                    Long roomId = Long.valueOf(content.substring(start, stop));

                    start = stop + KEY_RECEIVER.length();
                    stop = content.indexOf(KEY_BUTTON);
                    Long receiverId = Long.valueOf(content.substring(start, stop));

                    start = stop + KEY_BUTTON.length();
                    stop = content.length();
                    Long buttonId = Long.valueOf(content.substring(start, stop));

                    Apartment apartment = persistanceHandler.getApartment(apartmentId);
                    Room      room      = apartment.getRoom(roomId);
                    Receiver  receiver  = room.getReceiver(receiverId);
                    Button    button    = receiver.getButton(buttonId);

                    actionHandler.execute(receiver, button);
                } else if (content.contains(KEY_ROOM) && content.contains(KEY_BUTTON)) {
                    start = KEY_APARTMENT.length();
                    stop = content.indexOf(KEY_ROOM);
                    Long apartmentId = Long.valueOf(content.substring(start, stop));

                    start = stop + KEY_ROOM.length();
                    stop = content.indexOf(KEY_BUTTON);
                    Long roomId = Long.valueOf(content.substring(start, stop));

                    start = stop + KEY_BUTTON.length();
                    stop = content.length();
                    String buttonName = content.substring(start, stop);

                    Apartment apartment = persistanceHandler.getApartment(apartmentId);
                    Room      room      = apartment.getRoom(roomId);

                    actionHandler.execute(room, buttonName);
                } else if (content.contains(KEY_SCENE)) {
                    start = KEY_APARTMENT.length();
                    stop = content.indexOf(KEY_SCENE);
                    Long apartmentId = Long.valueOf(content.substring(start, stop));

                    start = stop + KEY_SCENE.length();
                    stop = content.length();
                    Long sceneId = Long.valueOf(content.substring(start, stop));

                    Apartment apartment = persistanceHandler.getApartment(apartmentId);
                    Scene     scene     = apartment.getScene(sceneId);

                    actionHandler.execute(scene);
                }
            }
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(this, e);
        }
    }
}

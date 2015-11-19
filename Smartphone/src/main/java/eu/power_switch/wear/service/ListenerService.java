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

package eu.power_switch.wear.service;

import android.app.PendingIntent;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import eu.power_switch.api.IntentReceiver;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.log.Log;

/**
 * A Wear listener service, used to receive inbound messages from
 * the Wear device.
 * <p/>
 * Created by Markus on 04.06.2015.
 */
public class ListenerService extends WearableListenerService {

    /**
     * This method is called when a message from a wearable device is received
     *
     * @param messageEvent
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(WearableConstants.RECEIVER_ACTION_TRIGGER_PATH)) {

            String messageData = new String(messageEvent.getData());
            Log.d("Wear_ListenerService", "Message received: " + messageData);

            // trigger api intent
            try {
                PendingIntent pendingIntent = createApiPendingIntent(messageData);
                if (pendingIntent != null) {
                    pendingIntent.send();
                    Log.d("Pending intent send");
                } else {
                    Log.e("Error parsing wearable message\n\n" + messageData);
                    Toast.makeText(getApplicationContext(), "Error parsing wearable message\n\n" + messageData, Toast.LENGTH_LONG)
                            .show();
                }
            } catch (PendingIntent.CanceledException e) {
                Log.e("Pending intent canceled", e);
            }
        } else if (messageEvent.getPath().equals(WearableConstants.REQUEST_DATA_UPDATE_PATH)) {
            UtilityService.forceWearDataUpdate(this);
        } else if (messageEvent.getPath().equals(WearableConstants.REQUEST_SETTINGS_UPDATE_PATH)) {
            UtilityService.forceWearSettingsUpdate(this);
        }
    }

    /**
     * Create PendingIntent to trigger Api IntentReceiver
     *
     * @param messageData
     * @return
     */
    private PendingIntent createApiPendingIntent(String messageData) {
        try {
            String roomName;
            String receiverName;
            String buttonName;

            if (messageData.contains("RoomName") && messageData.contains("ReceiverName") && messageData.contains("ButtonName")) {
                int start = messageData.indexOf("RoomName:") + 9;
                int stop = messageData.indexOf("ReceiverName:");
                roomName = messageData.substring(start, stop);
                start = stop + 13;
                stop = messageData.indexOf("ButtonName:");
                receiverName = messageData.substring(start, stop);
                start = stop + 11;
                stop = messageData.indexOf(";;");
                buttonName = messageData.substring(start, stop);

                PendingIntent pendingIntent = IntentReceiver.buildReceiverButtonPendingIntent(getApplicationContext(), roomName, receiverName, buttonName, 0);
                return pendingIntent;
            } else if (messageData.contains("RoomName") && messageData.contains("ButtonName")) {
                int start = messageData.indexOf("RoomName:") + 9;
                int stop = messageData.indexOf("ButtonName:");
                roomName = messageData.substring(start, stop);
                start = stop + 11;
                stop = messageData.indexOf(";;");
                buttonName = messageData.substring(start, stop);

                PendingIntent pendingIntent = IntentReceiver.buildRoomButtonPendingIntent(getApplicationContext(), roomName, buttonName, 0);
                return pendingIntent;
            } else if (messageData.contains("SceneName")) {
                int start = messageData.indexOf("SceneName:") + 10;
                int stop = messageData.indexOf(";;");
                String sceneName = messageData.substring(start, stop);

                PendingIntent pendingIntent = IntentReceiver.buildSceneButtonPendingIntent(getApplicationContext(), sceneName, 0);
                return pendingIntent;
            }
        } catch (Exception e) {
            Log.e("createApiPendingIntent", e);
        }

        return null;
    }
}

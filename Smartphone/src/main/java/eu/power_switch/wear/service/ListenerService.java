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

import android.support.design.widget.Snackbar;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.settings.WearableSettingsFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * A Wear listener service, used to receive inbound messages from
 * the Wear device.
 * <p/>
 * Created by Markus on 04.06.2015.
 */
public class ListenerService extends WearableListenerService {

    /**
     * This method extracts settings data contained in a DataMap Array and saves it into the local PreferenceHandler.
     *
     * @param settings received settings data
     */
    public static void extractSettings(ArrayList<DataMap> settings) {
        // save map values to local preferenceHandler
        for (DataMap dataMapItem : settings) {
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB)) {
                int value = dataMapItem.getInt(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB, value);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS)) {
                boolean bool = dataMapItem.getBoolean(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS, bool);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
                boolean bool = dataMapItem.getBoolean(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, bool);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF)) {
                boolean bool = dataMapItem.getBoolean(WearablePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF, bool);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_THEME)) {
                int value = dataMapItem.getInt(WearablePreferencesHandler.KEY_THEME);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_THEME, value);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS)) {
                boolean bool = dataMapItem.getBoolean(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS, bool);
            }
            if (dataMapItem.containsKey(WearablePreferencesHandler.KEY_VIBRATION_DURATION)) {
                int value = dataMapItem.getInt(WearablePreferencesHandler.KEY_VIBRATION_DURATION);
                WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_VIBRATION_DURATION, value);
            }
        }
    }

    /**
     * This method is called when a message from a wearable device is received
     *
     * @param messageEvent
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LogHandler.configureLogger(getApplicationContext());

        if (messageEvent.getPath().equals(WearableConstants.RECEIVER_ACTION_TRIGGER_PATH)) {

            String messageData = new String(messageEvent.getData());
            Log.d("Wear_ListenerService", "Message received: " + messageData);

            // trigger api intent
            parseMessage(messageData);
        } else if (messageEvent.getPath().equals(WearableConstants.REQUEST_DATA_UPDATE_PATH)) {
            UtilityService.forceWearDataUpdate(this);
        } else if (messageEvent.getPath().equals(WearableConstants.REQUEST_SETTINGS_UPDATE_PATH)) {
            UtilityService.forceWearSettingsUpdate(this);
        }
    }

    /**
     * Parse message string
     *
     * @param messageData
     */
    private void parseMessage(String messageData) {
        try {
            Long roomId;
            Long receiverId;
            Long buttonId;

            if (messageData.contains(WearableConstants.KEY_ROOM_ID) &&
                    messageData.contains(WearableConstants.KEY_RECEIVER_ID) &&
                    messageData.contains(WearableConstants.KEY_BUTTON_ID)) {
                int start = messageData.indexOf(WearableConstants.KEY_ROOM_ID) + WearableConstants.KEY_ROOM_ID.length();
                int stop = messageData.indexOf(WearableConstants.KEY_RECEIVER_ID);
                roomId = Long.valueOf(messageData.substring(start, stop));
                start = stop + WearableConstants.KEY_RECEIVER_ID.length();
                stop = messageData.indexOf(WearableConstants.KEY_BUTTON_ID);
                receiverId = Long.valueOf(messageData.substring(start, stop));
                start = stop + WearableConstants.KEY_BUTTON_ID.length();
                stop = messageData.indexOf(";;");
                buttonId = Long.valueOf(messageData.substring(start, stop));

                Room room = DatabaseHandler.getRoom(roomId);
                Receiver receiver = room.getReceiver(receiverId);
                Button button = receiver.getButton(buttonId);

                ActionHandler.execute(getApplicationContext(), receiver, button);
            } else if (messageData.contains(WearableConstants.KEY_ROOM_ID) &&
                    messageData.contains(WearableConstants.KEY_BUTTON_ID)) {
                int start = messageData.indexOf(WearableConstants.KEY_ROOM_ID) + WearableConstants.KEY_ROOM_ID.length();
                int stop = messageData.indexOf(WearableConstants.KEY_BUTTON_ID);
                roomId = Long.valueOf(messageData.substring(start, stop));
                start = stop + WearableConstants.KEY_BUTTON_ID.length();
                stop = messageData.indexOf(";;");
                buttonId = Long.valueOf(messageData.substring(start, stop));

                Room room = DatabaseHandler.getRoom(roomId);

                ActionHandler.execute(getApplicationContext(), room, buttonId);
            } else if (messageData.contains(WearableConstants.KEY_SCENE_ID)) {
                int start = messageData.indexOf(WearableConstants.KEY_SCENE_ID) + WearableConstants.KEY_SCENE_ID.length();
                int stop = messageData.indexOf(";;");
                Long sceneId = Long.valueOf(messageData.substring(start, stop));

                Scene scene = DatabaseHandler.getScene(sceneId);

                ActionHandler.execute(getApplicationContext(), scene);
            }
        } catch (Exception e) {
            Log.e("parseMessage", e);
            StatusMessageHandler.showInfoMessage(getApplicationContext(),
                    R.string.error_executing_wear_action, Snackbar.LENGTH_LONG);
        }
    }

    /**
     * Reacts to DataChanged Events from DataApi
     *
     * @param dataEvents
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            if (event.getDataItem() != null) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    if (WearableConstants.SETTINGS_PATH.equals(event.getDataItem().getUri().getPath())) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        ArrayList<DataMap> settings = dataMapItem.getDataMap()
                                .getDataMapArrayList(WearableConstants.EXTRA_SETTINGS);
                        extractSettings(settings);
                        WearableSettingsFragment.notifySettingsChanged(this);
                    }
                }
            }
        }
    }
}

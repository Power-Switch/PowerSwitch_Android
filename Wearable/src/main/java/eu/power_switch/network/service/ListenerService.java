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

package eu.power_switch.network.service;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.power_switch.obj.Button;
import eu.power_switch.obj.Receiver;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.constants.WearableSettingsConstants;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Created by Markus on 05.06.2015.
 * <p/>
 * A Wear listener service, used to receive inbound messages from
 * other devices.
 */
public class ListenerService extends WearableListenerService {

    public static final String DATA_UPDATED = "eu.power_switch.data_updated";
    public static final String KEY_APARTMENT_DATA = "apartment_data";
    public static final String KEY_ROOM_DATA = "room_data";
    public static final String KEY_SCENE_DATA = "scene_data";

    /**
     * Extract Apartment info from DataMap Array
     *
     * @param dataMapArrayList received data
     * @return Apartment name
     */
    public static String extractApartmentDataMapItems(ArrayList<DataMap> dataMapArrayList) {
        long apartmentId = -1;

        for (DataMap dataMapItem : dataMapArrayList) {
            if (dataMapItem.containsKey(WearableConstants.DATAMAP_KEY_ROOM_APARTMENT_ID)) {
                apartmentId = dataMapItem.getLong(WearableConstants.DATAMAP_KEY_ROOM_APARTMENT_ID);
                break;
            }
        }

        for (DataMap dataMapItem : dataMapArrayList) {
            if (dataMapItem.containsKey(WearableConstants.DATAMAP_KEY_APARTMENT_NAME)) {
                if (apartmentId == dataMapItem.getLong(WearableConstants.DATAMAP_KEY_APARTMENT_ID)) {
                    return dataMapItem.getString(WearableConstants.DATAMAP_KEY_APARTMENT_NAME);
                }
            }
        }

        return null;
    }

    /**
     * This method converts received data contained in a DataMap Array back to Rooms, Receivers and Buttons.
     *
     * @param dataMapArrayList received data
     * @return List of Rooms containing the appropriate Receivers and Buttons
     */
    public static ArrayList<Room> extractRoomDataMapItems(ArrayList<DataMap> dataMapArrayList) {
        ArrayList<Room> rooms = new ArrayList<>();

        for (DataMap dataMapItem : dataMapArrayList) {
            if (dataMapItem.containsKey(WearableConstants.DATAMAP_KEY_ROOM_NAME)) {
                long roomId = dataMapItem.getLong(WearableConstants.DATAMAP_KEY_ROOM_ID);
                String roomName = dataMapItem.getString(WearableConstants.DATAMAP_KEY_ROOM_NAME);

                rooms.add(new Room(roomId, roomName));

            } else if (dataMapItem.containsKey(WearableConstants.DATAMAP_KEY_RECEIVER_NAME)) {
                long receiverId = dataMapItem.getLong(WearableConstants.DATAMAP_KEY_RECEIVER_ID);
                String receiverName = dataMapItem.getString(WearableConstants.DATAMAP_KEY_RECEIVER_NAME);
                long receiverRoomId = dataMapItem.getLong(WearableConstants.DATAMAP_KEY_RECEIVER_ROOM_ID);
                int positionInRoom = dataMapItem.getInt(WearableConstants.DATAMAP_KEY_RECEIVER_POSITION_IN_ROOM);
                long lastActivatedButtonId = dataMapItem.getLong(WearableConstants.DATAMAP_KEY_RECEIVER_LAST_ACTIVATED_BUTTON_ID);

                Receiver receiver = new Receiver(receiverId, receiverName, receiverRoomId,
                        lastActivatedButtonId, positionInRoom);

                for (Room room : rooms) {
                    if (room.getId() == receiver.getRoomId()) {
                        room.addReceiver(receiver);
                        break;
                    }
                }
            } else if (dataMapItem.containsKey(WearableConstants.DATAMAP_KEY_BUTTON_NAME)) {
                long buttonId = dataMapItem.getLong(WearableConstants.DATAMAP_KEY_BUTTON_ID);
                String buttonName = dataMapItem.getString(WearableConstants.DATAMAP_KEY_BUTTON_NAME);
                long buttonReceiverId = dataMapItem.getLong(WearableConstants.DATAMAP_KEY_BUTTON_RECEIVER_ID);

                Button button = new Button(buttonId, buttonName, buttonReceiverId);

                for (Room room : rooms) {
                    for (Receiver receiver : room.getReceivers()) {
                        if (receiver.getId() == button.getReceiverId()) {
                            receiver.addButton(button);
                            break;
                        }
                    }
                }
            }
        }

        // sort receivers
        for (Room room : rooms) {
            Collections.sort(room.getReceivers(), new Comparator<Receiver>() {
                @Override
                public int compare(Receiver t0, Receiver t1) {
                    return t0.getPositionInRoom() - t1.getPositionInRoom();
                }
            });
        }

        return rooms;
    }

    /**
     * This method converts received data contained in a DataMap Array back to Scenes.
     *
     * @param dataMapArrayList received data
     * @return List of Rooms containing the appropriate Receivers and Buttons
     */
    public static ArrayList<Scene> extractSceneDataMapItems(ArrayList<DataMap> dataMapArrayList) {
        ArrayList<Scene> scenes = new ArrayList<>();

        for (DataMap dataMapItem : dataMapArrayList) {
            if (dataMapItem.containsKey(WearableConstants.DATAMAP_KEY_SCENE_NAME)) {
                long sceneId = dataMapItem.getLong(WearableConstants.DATAMAP_KEY_SCENE_ID);
                String sceneName = dataMapItem.getString(WearableConstants.DATAMAP_KEY_SCENE_NAME);
                Scene scene = new Scene(sceneId, sceneName);
                scenes.add(scene);
            }
        }

        return scenes;
    }

    /**
     * This method extracts settings data contained in a DataMap Array and saves it into the local PreferenceHandler.
     *
     * @param settings received settings data
     */
    public static void extractSettings(ArrayList<DataMap> settings) {
        // save map values to local preferenceHandler
        for (DataMap dataMapItem : settings) {
            if (dataMapItem.containsKey(WearableSettingsConstants.KEY_AUTO_COLLAPSE_ROOMS)) {
                boolean bool = dataMapItem.getBoolean(WearableSettingsConstants.KEY_AUTO_COLLAPSE_ROOMS);
                WearablePreferencesHandler.setAutoCollapseRooms(bool);
            }
            if (dataMapItem.containsKey(WearableSettingsConstants.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
                boolean bool = dataMapItem.getBoolean(WearableSettingsConstants.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
                WearablePreferencesHandler.setHighlightLastActivatedButton(bool);
            }
            if (dataMapItem.containsKey(WearableSettingsConstants.KEY_SHOW_ROOM_ALL_ON_OFF)) {
                boolean bool = dataMapItem.getBoolean(WearableSettingsConstants.KEY_SHOW_ROOM_ALL_ON_OFF);
                WearablePreferencesHandler.setShowRoomAllOnOff(bool);
            }
            if (dataMapItem.containsKey(WearableSettingsConstants.KEY_THEME)) {
                int value = dataMapItem.getInt(WearableSettingsConstants.KEY_THEME);
                WearablePreferencesHandler.setTheme(value);
            }
            if (dataMapItem.containsKey(WearableSettingsConstants.KEY_VIBRATE_ON_BUTTON_PRESS)) {
                boolean bool = dataMapItem.getBoolean(WearableSettingsConstants.KEY_VIBRATE_ON_BUTTON_PRESS);
                WearablePreferencesHandler.setVibrateOnButtonPress(bool);
            }
            if (dataMapItem.containsKey(WearableSettingsConstants.KEY_VIBRATION_DURATION)) {
                int value = dataMapItem.getInt(WearableSettingsConstants.KEY_VIBRATION_DURATION);
                WearablePreferencesHandler.setVibrationDuration(value);
            }
        }
    }

    /**
     * Reacts to DataChanged Events from DataApi
     *
     * @param dataEvents
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            if (event.getDataItem() != null) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    if (WearableConstants.DATA_PATH.equals(event.getDataItem().getUri().getPath())) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        ArrayList<DataMap> data = dataMapItem.getDataMap()
                                .getDataMapArrayList(WearableConstants.EXTRA_DATA);

                        boolean autoCollapseRooms = WearablePreferencesHandler.getAutoCollapseRooms();

                        String apartmentName = extractApartmentDataMapItems(data);
                        // convert received data to room/receiver/button objects
                        ArrayList<Room> rooms = extractRoomDataMapItems(data);
                        for (Room room : rooms) {
                            room.setCollapsed(autoCollapseRooms);
                        }
                        ArrayList<Scene> scenes = extractSceneDataMapItems(data);

                        // send data to Activity
                        sendDataUpdatedBroadcast(apartmentName, rooms, scenes);
                    } else if (WearableConstants.SETTINGS_PATH.equals(event.getDataItem().getUri().getPath())) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        ArrayList<DataMap> settings = dataMapItem.getDataMap()
                                .getDataMapArrayList(WearableConstants.EXTRA_SETTINGS);

                        int oldThemeValue = WearablePreferencesHandler.getTheme();
                        extractSettings(settings);
                        int newThemeValue = WearablePreferencesHandler.getTheme();

                        // notify about changes
                        if (newThemeValue != oldThemeValue) {
                            sendThemeChangedBroadcast();
                        } else {
                            sendSettingsChangedBroadcast();
                        }

                    }
                } else if (event.getType() == DataEvent.TYPE_DELETED) {
                    if (WearableConstants.DATA_PATH.equals(event.getDataItem().getUri().getPath())) {
                        // send data to Activity
                        // update with empty lists
                        sendDataUpdatedBroadcast("", new ArrayList<Room>(), new ArrayList<Scene>());
                    }
                }
            }
        }
    }

    /**
     * Reacts on Messages from MessageApi
     *
     * @param messageEvent
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Toast.makeText(getApplicationContext(),
                "Message received: " + convertEventDataToString(messageEvent.getData()), Toast.LENGTH_LONG)
                .show();

        if (messageEvent.getPath().equals(WearableConstants.START_ACTIVITY_PATH)) {
            // TODO: Launch Wearable App
            // is this even possible?
        }
    }

    /**
     * Sends local Broadcast that underlying data has changed and UI has to be updated
     *
     * @param rooms
     * @param scenes
     */

    private void sendDataUpdatedBroadcast(String apartmentName, ArrayList<Room> rooms, ArrayList<Scene> scenes) {
        Intent intent = new Intent(DATA_UPDATED);
        intent.putExtra(KEY_APARTMENT_DATA, apartmentName);
        intent.putExtra(KEY_ROOM_DATA, rooms);
        intent.putExtra(KEY_SCENE_DATA, scenes);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Sends local Broadcast that underlying settings have changed and UI has to be updated
     */
    private void sendSettingsChangedBroadcast() {
        Intent intent = new Intent(WearableSettingsConstants.WEARABLE_SETTINGS_CHANGED);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Sends local Broadcast that underlying settings have changed and UI has to be updated
     */
    private void sendThemeChangedBroadcast() {
        Intent intent = new Intent(WearableSettingsConstants.WEARABLE_THEME_CHANGED);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String convertEventDataToString(byte[] data) {
        return new String(data);
    }
}
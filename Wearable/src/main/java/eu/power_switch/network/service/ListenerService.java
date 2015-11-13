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

/**
 * Created by Markus on 05.06.2015.
 * <p/>
 * A Wear listener service, used to receive inbound messages from
 * other devices.
 */
public class ListenerService extends WearableListenerService {

    public static final String DATA_UPDATED = "eu.power_switch.data_updated";
    public static final String ROOM_DATA = "room_data";
    public static final String SCENE_DATA = "scene_data";

    /**
     * This method converts received data contained in a DataMap Array back to Rooms, Receivers and Buttons.
     *
     * @param dataMapArrayList received data
     * @return List of Rooms containing the appropriate Receivers and Buttons
     */
    public static ArrayList<Room> extractRoomDataMapItems(ArrayList<DataMap> dataMapArrayList) {
        ArrayList<Room> rooms = new ArrayList<>();

        for (DataMap dataMapItem : dataMapArrayList) {
            if (dataMapItem.containsKey(WearableConstants.ROOM_NAME_DATAMAP_KEY)) {
                long roomId = dataMapItem.getLong(WearableConstants.ROOM_ID_DATAMAP_KEY);
                String roomName = dataMapItem.getString(WearableConstants.ROOM_NAME_DATAMAP_KEY);

                rooms.add(new Room(roomId, roomName));

            } else if (dataMapItem.containsKey(WearableConstants.RECEIVER_NAME_DATAMAP_KEY)) {
                long receiverId = dataMapItem.getLong(WearableConstants.RECEIVER_ID_DATAMAP_KEY);
                String receiverName = dataMapItem.getString(WearableConstants.RECEIVER_NAME_DATAMAP_KEY);
                long receiverRoomId = dataMapItem.getLong(WearableConstants.RECEIVER_ROOM_ID_DATAMAP_KEY);
                int positionInRoom = dataMapItem.getInt(WearableConstants.RECEIVER_POSITION_IN_ROOM_DATAMAP_KEY);
                long lastActivatedButtonId = dataMapItem.getLong(WearableConstants.RECEIVER_LAST_ACTIVATED_BUTTON_ID_DATAMAP_KEY);

                Receiver receiver = new Receiver(receiverId, receiverName, receiverRoomId,
                        lastActivatedButtonId, positionInRoom);

                for (Room room : rooms) {
                    if (room.getId() == receiver.getRoomId()) {
                        room.addReceiver(receiver);
                        break;
                    }
                }
            } else if (dataMapItem.containsKey(WearableConstants.BUTTON_NAME_DATAMAP_KEY)) {
                long buttonId = dataMapItem.getLong(WearableConstants.BUTTON_ID_DATAMAP_KEY);
                String buttonName = dataMapItem.getString(WearableConstants.BUTTON_NAME_DATAMAP_KEY);
                long buttonReceiverId = dataMapItem.getLong(WearableConstants.BUTTON_RECEIVER_ID_DATAMAP_KEY);

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
            if (dataMapItem.containsKey(WearableConstants.SCENE_NAME_DATAMAP_KEY)) {
                long sceneId = dataMapItem.getLong(WearableConstants.SCENE_ID_DATAMAP_KEY);
                String sceneName = dataMapItem.getString(WearableConstants.SCENE_NAME_DATAMAP_KEY);
                Scene scene = new Scene(sceneId, sceneName);
                scenes.add(scene);
            }
        }

        return scenes;
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
                if (event.getType() == DataEvent.TYPE_CHANGED
                        && WearableConstants.DATA_PATH.equals(event.getDataItem().getUri().getPath())) {

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    ArrayList<DataMap> data = dataMapItem.getDataMap().getDataMapArrayList(WearableConstants.EXTRA_DATA);

                    // convert received data to room/receiver/button objects
                    ArrayList<Room> rooms = extractRoomDataMapItems(data);
                    ArrayList<Scene> scenes = extractSceneDataMapItems(data);

                    // send data to Activity
                    sendDataUpdatedBroadcast(rooms, scenes);
                } else if (event.getType() == DataEvent.TYPE_CHANGED
                        && WearableConstants.SETTINGS_PATH.equals(event.getDataItem().getUri().getPath())) {

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    ArrayList<DataMap> settings = dataMapItem.getDataMap().getDataMapArrayList(WearableConstants.EXTRA_SETTINGS);

                    extractSettings(settings);

                    // TODO: notify app about changes
                }
            }
        }
    }

    private void extractSettings(ArrayList<DataMap> settings) {
        // TODO: get changed values and save to local preferenceHandler
    }

    /**
     * Reacts on Messegaes from MessageApi
     *
     * @param messageEvent
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Toast.makeText(getApplicationContext(), "Message received: " + convertEventDataToString(messageEvent.getData()), Toast.LENGTH_LONG)
                .show();

        if (messageEvent.getPath().equals(WearableConstants.START_ACTIVITY_PATH)) {
            // TODO: Launch Wearable App
            // is this even possible?
        }
    }

    /**
     * Sends Broadcast that underlying data has changed and UI has to be updated
     *
     * @param rooms
     * @param scenes
     */
    private void sendDataUpdatedBroadcast(ArrayList<Room> rooms, ArrayList<Scene> scenes) {
        Intent intent = new Intent(DATA_UPDATED);
        intent.putExtra(ROOM_DATA, rooms);
        intent.putExtra(SCENE_DATA, scenes);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String convertEventDataToString(byte[] data) {
        return new String(data);
    }

}
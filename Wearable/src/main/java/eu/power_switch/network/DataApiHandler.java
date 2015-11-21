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

package eu.power_switch.network;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import eu.power_switch.gui.animation.ActionResponse;
import eu.power_switch.network.service.ListenerService;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.constants.WearableConstants;

/**
 * Created by Markus on 03.06.2015.
 */
public class DataApiHandler {

    private static GoogleApiClient googleApiClient;
    protected boolean googleApiClientIsConnected;
    private MessageApiHandler messageApiHandler;
    private Context context;

    public DataApiHandler(Context context) {
        this.context = context;
        initPlayServices();
    }

    public static String buildReceiverActionString(String roomName, String receiverName, String buttonName) {
        return "RoomName:" + roomName + "ReceiverName:" + receiverName + "ButtonName:" + buttonName + ";;";
    }

    public static String buildRoomActionString(String roomName, String buttonName) {
        return "RoomName:" + roomName + "ButtonName:" + buttonName + ";;";
    }

    public static String buildSceneActionString(String sceneName) {
        return "SceneName:" + sceneName + ";;";
    }

    private void initPlayServices() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d("", "googleApi connected");
                        googleApiClientIsConnected = true;
                        // now usable
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d("", "googleApi connection suspended");
                        googleApiClientIsConnected = false;
                        // not usable anymore
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d("", "googleApi connection FAILED!" + " Result: " + connectionResult);
                        googleApiClientIsConnected = false;

                        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
                            // The Wearable API is unavailable
                            Log.d("", "Wear API is unavailable!");
                        }
                    }
                })
                .addApiIfAvailable(Wearable.API)
                .build();

        messageApiHandler = new MessageApiHandler(context, googleApiClient);
    }

    public boolean blockingConnect() {
        ConnectionResult connectionResult = googleApiClient.blockingConnect(SettingsConstants.GOOGLE_API_CLIENT_TIMEOUT, TimeUnit
                .SECONDS);

        if (!connectionResult.isSuccess() || !googleApiClient.isConnected()) {
            Log.e("FetchDataAsyncTask", String.format("Failed to connect to GoogleApiClient (error code = %d)",
                    connectionResult.getErrorCode()));
            return false;
        }
        Log.e("FetchDataAsyncTask", "GoogleApiClient connected using blocking connect method");
        return true;
    }

    /**
     * Send Receiver Action Trigger to Smartphone App
     *
     * @param actionString Receiver actionString, see {buildReceiverActionString] for more info
     */
    public void sendReceiverActionTrigger(String actionString) {
        if (!googleApiClientIsConnected) {
            ActionResponse.showFailureAnimation(context, "GooglePlayServices not connected");
            return;
        }
        Log.d("", "sending receiver trigger: " + actionString);
        messageApiHandler.sendAction(actionString);
    }

    /**
     * Send Room Action Trigger to Smartphone App
     *
     * @param actionString Room actionString, see {buildRoomActionString] for more info
     */
    public void sendRoomActionTrigger(String actionString) {
        if (!googleApiClientIsConnected) {
            ActionResponse.showFailureAnimation(context, "GooglePlayServices not connected");
            return;
        }
        Log.d("", "sending receiver trigger: " + actionString);
        messageApiHandler.sendAction(actionString);
    }

    /**
     * Send Scene Action Trigger to Smartphone App
     *
     * @param actionString Scene actionString, see {buildSceneActionString] for more info
     */
    public void sendSceneActionTrigger(String actionString) {
        if (!googleApiClientIsConnected) {
            ActionResponse.showFailureAnimation(context, "GooglePlayServices not connected");
            return;
        }
        Log.d("", "sending scene trigger: " + actionString);
        messageApiHandler.sendAction(actionString);
    }

    /**
     * Send data update request to smartphone app
     */
    public void sendDataUpdateRequest() {
        if (!googleApiClientIsConnected) {
            ActionResponse.showFailureAnimation(context, "GooglePlayServices not connected");
            return;
        }
        Log.d("", "requesting data update");
        messageApiHandler.sendUpdateRequest();
    }

    public void connect() {
        googleApiClient.connect();
    }

    public void disconnect() {
        googleApiClient.disconnect();
    }

    /**
     * Retrieve room data from Wear cloud storage
     *
     * @return List of Rooms
     */
    public ArrayList<Room> getRoomData() {
        ArrayList<Room> rooms = new ArrayList<>();

        if (!googleApiClient.isConnected()) {
            if (!blockingConnect()) {
                return null;
            }
        }

        ArrayList<DataMap> data;
        DataItemBuffer dataItemBuffer = Wearable.DataApi.getDataItems(googleApiClient).await();

        if (dataItemBuffer.getStatus().isSuccess()) {
            for (DataItem dataItem : dataItemBuffer) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                data = dataMapItem.getDataMap().getDataMapArrayList(WearableConstants.EXTRA_DATA);
                if (data != null) {
                    rooms = ListenerService.extractRoomDataMapItems(data);
                    break;
                }
            }
        }
        dataItemBuffer.release();

        return rooms;
    }

    /**
     * Retrieve scene data from Wear cloud storage
     *
     * @return List of Scenes
     */
    public ArrayList<Scene> getSceneData() {
        ArrayList<Scene> scenes = new ArrayList<>();

        if (!googleApiClient.isConnected()) {
            if (!blockingConnect()) {
                return null;
            }
        }

        ArrayList<DataMap> data;
        DataItemBuffer dataItemBuffer = Wearable.DataApi.getDataItems(googleApiClient).await();

        if (dataItemBuffer.getStatus().isSuccess()) {
            for (DataItem dataItem : dataItemBuffer) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                data = dataMapItem.getDataMap().getDataMapArrayList(WearableConstants.EXTRA_DATA);
                if (data != null) {
                    scenes = ListenerService.extractSceneDataMapItems(data);
                    break;
                }
            }
        }
        dataItemBuffer.release();

        return scenes;
    }

    /**
     * Retrieve wear settings from Wear cloud storage
     */
    public void updateSettings(Context context) {
        if (!googleApiClient.isConnected()) {
            if (!blockingConnect()) {
                return;
            }
        }

        ArrayList<DataMap> data;
        DataItemBuffer dataItemBuffer = Wearable.DataApi.getDataItems(googleApiClient).await();

        if (dataItemBuffer.getStatus().isSuccess()) {
            for (DataItem dataItem : dataItemBuffer) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                data = dataMapItem.getDataMap().getDataMapArrayList(WearableConstants.EXTRA_SETTINGS);
                if (data != null) {
                    ListenerService.extractSettings(context, data);
                    break;
                }
            }
        }
        dataItemBuffer.release();
    }
}

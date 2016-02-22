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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.constants.WearableSettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Created by Markus on 06.06.2015.
 * <p/>
 * Service to handle background Network communication with the Wearable App
 */
public class UtilityService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UtilityService() {
        super("UtilityService");
    }

    /**
     * Create Intent to update Wear Data via background service
     *
     * @param context any suitable context
     */
    public static void forceWearDataUpdate(Context context) {
        Log.d("Updating Data for Wearable");
        Intent intent = new Intent(context, UtilityService.class);
        intent.setAction(WearableConstants.REQUEST_DATA_UPDATE_PATH);
        context.startService(intent);
    }

    /**
     * Create Intent to update Wear Settings via background service
     *
     * @param context any suitable context
     */
    public static void forceWearSettingsUpdate(Context context) {
        Log.d("Updating Settings for Wearable");
        Intent intent = new Intent(context, UtilityService.class);
        intent.setAction(WearableConstants.REQUEST_SETTINGS_UPDATE_PATH);
        context.startService(intent);
    }

    /**
     * Transfer the required data over to the wearable
     *
     * @param rooms     List containing Rooms from Database
     * @param receivers List containing Receivers from Database
     */
    private void sendDataToWearable(List<Room> rooms, List<Receiver> receivers, List<Button> buttons, List<Scene>
            scenes) {
        Log.d("Sending new Data to Wearable...");
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API).build();

        // It's OK to use blockingConnect() here as we are running in an
        // IntentService that executes work on a separate (background) thread.
        ConnectionResult connectionResult = googleApiClient.blockingConnect(
                SettingsConstants.GOOGLE_API_CLIENT_TIMEOUT, TimeUnit.SECONDS);

        ArrayList<DataMap> data = new ArrayList<>();

        ArrayList<DataMap> roomData = new ArrayList<>();
        ArrayList<DataMap> receiverData = new ArrayList<>();
        ArrayList<DataMap> buttonData = new ArrayList<>();
        ArrayList<DataMap> sceneData = new ArrayList<>();

        for (Room room : rooms) {
            roomData.add(convertToDataMap(room));
            data.add(convertToDataMap(room));
        }

        for (Receiver receiver : receivers) {
            receiverData.add(convertToDataMap(receiver));
            data.add(convertToDataMap(receiver));
        }

        for (Button button : buttons) {
            buttonData.add(convertToDataMap(button));
            data.add(convertToDataMap(button));
        }

        for (Scene scene : scenes) {
            sceneData.add(convertToDataMap(scene));
            data.add(convertToDataMap(scene));
        }

        if (connectionResult.isSuccess() && googleApiClient.isConnected() && data.size() > 0) {

            PutDataMapRequest dataMap = PutDataMapRequest.create(WearableConstants.DATA_PATH);
            dataMap.getDataMap().putDataMapArrayList(WearableConstants.EXTRA_DATA, data);
            PutDataRequest request = dataMap.asPutDataRequest();

            // Send the data over
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, request).await();

            if (!result.getStatus().isSuccess()) {
                Log.e("", String.format("Error sending data using DataApi (error code = %d)",
                        result.getStatus().getStatusCode()));
            } else {
                Log.d("Update data sent");
            }

        } else {
            // GoogleApiClient connection error
            Log.e("Error connecting GoogleApiClient");
        }

    }

    /**
     * Puts a Room into a DataMap
     *
     * @param room Room to convert
     * @return DataMap
     */
    private DataMap convertToDataMap(Room room) {
        DataMap roomDataMap = new DataMap();

        roomDataMap.putLong(WearableConstants.ROOM_ID_DATAMAP_KEY, room.getId());
        roomDataMap.putString(WearableConstants.ROOM_NAME_DATAMAP_KEY, room.getName());

        return roomDataMap;
    }

    /**
     * Puts a Receiver into a DataMap
     *
     * @param receiver Receiver to convert
     * @return DataMap
     */
    private DataMap convertToDataMap(Receiver receiver) {
        DataMap receiverDataMap = new DataMap();

        receiverDataMap.putLong(WearableConstants.RECEIVER_ID_DATAMAP_KEY, receiver.getId());
        receiverDataMap.putString(WearableConstants.RECEIVER_NAME_DATAMAP_KEY, receiver.getName());
        receiverDataMap.putLong(WearableConstants.RECEIVER_ROOM_ID_DATAMAP_KEY, receiver.getRoomId());
        receiverDataMap.putInt(WearableConstants.RECEIVER_POSITION_IN_ROOM_DATAMAP_KEY, receiver.getPositionInRoom());
        receiverDataMap.putLong(WearableConstants.RECEIVER_LAST_ACTIVATED_BUTTON_ID_DATAMAP_KEY, receiver.getLastActivatedButtonId());

        return receiverDataMap;
    }

    /**
     * Puts a Button into a DataMap
     *
     * @param button Button to convert
     * @return DataMap
     */
    private DataMap convertToDataMap(Button button) {
        DataMap buttonDataMap = new DataMap();

        buttonDataMap.putLong(WearableConstants.BUTTON_ID_DATAMAP_KEY, button.getId());
        buttonDataMap.putString(WearableConstants.BUTTON_NAME_DATAMAP_KEY, button.getName());
        buttonDataMap.putLong(WearableConstants.BUTTON_RECEIVER_ID_DATAMAP_KEY, button.getReceiverId());

        return buttonDataMap;
    }

    /**
     * Puts a Scene into a DataMap
     *
     * @param scene Scene to convert
     * @return DataMap
     */
    private DataMap convertToDataMap(Scene scene) {
        DataMap roomDataMap = new DataMap();

        roomDataMap.putLong(WearableConstants.SCENE_ID_DATAMAP_KEY, scene.getId());
        roomDataMap.putString(WearableConstants.SCENE_NAME_DATAMAP_KEY, scene.getName());

        return roomDataMap;
    }

    /**
     * Receive internal intents
     *
     * @param intent
     */
    @Override
    synchronized protected void onHandleIntent(Intent intent) {
        Log.d(this, intent);

        // Get Room/Receiver/Scene Data from Database and send to wearable
        if (WearableConstants.REQUEST_DATA_UPDATE_PATH.equals(intent.getAction())) {
            Log.d("Getting Data from Database to send to Wearable...");

            if (DeveloperPreferencesHandler.getPlayStoreMode()) {
                PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getApplicationContext());

                List<Room> rooms = playStoreModeDataModel.getRooms();
                List<Receiver> receivers = playStoreModeDataModel.getReceivers();

                List<Button> buttons = new ArrayList<>();
                for (Receiver receiver : receivers) {
                    buttons.addAll(receiver.getButtons());
                }

                List<Scene> scenes = playStoreModeDataModel.getScenes();

                sendDataToWearable(rooms, receivers, buttons, scenes);
                return;
            }

            try {
                List<Room> rooms = DatabaseHandler.getRooms(SmartphonePreferencesHandler.getCurrentApartmentId());

                List<Receiver> receivers = new ArrayList<>();
                for (Room room : rooms) {
                    receivers.addAll(room.getReceivers());
                }

                List<Button> buttons = new ArrayList<>();
                for (Receiver receiver : receivers) {
                    buttons.addAll(receiver.getButtons());
                }
                List<Scene> scenes = DatabaseHandler.getScenes(SmartphonePreferencesHandler.getCurrentApartmentId());

                sendDataToWearable(rooms, receivers, buttons, scenes);
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getApplicationContext(), e);
            }
        } else if (WearableConstants.REQUEST_SETTINGS_UPDATE_PATH.equals(intent.getAction())) {
            try {
                sendSettingsToWearable();
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getApplicationContext(), e);
            }
        }
    }

    /**
     * Sends current Wearable Settings made in Smartphone app over to the Wearable companion app
     */
    private void sendSettingsToWearable() {
        Log.d("Sending Settings to Wearable...");
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API).build();

        // It's OK to use blockingConnect() here as we are running in an
        // IntentService that executes work on a separate (background) thread.
        ConnectionResult connectionResult = googleApiClient.blockingConnect(
                SettingsConstants.GOOGLE_API_CLIENT_TIMEOUT, TimeUnit.SECONDS);

        ArrayList<DataMap> settings = new ArrayList<>();
        DataMap settingsDataMap = getSettingsDataMap();
        settings.add(settingsDataMap);

        if (connectionResult.isSuccess() && googleApiClient.isConnected() && settings.size() > 0) {

            PutDataMapRequest dataMap = PutDataMapRequest.create(WearableConstants.SETTINGS_PATH);
            dataMap.getDataMap().putDataMapArrayList(WearableConstants.EXTRA_SETTINGS, settings);
            PutDataRequest request = dataMap.asPutDataRequest();

            // Send the data over
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, request).await();

            if (!result.getStatus().isSuccess()) {
                Log.e("", String.format("Error sending settings using DataApi (error code = %d)",
                        result.getStatus().getStatusCode()));
            } else {
                Log.d("Updated settings sent");
            }

        } else {
            // GoogleApiClient connection error
            Log.e("Error connecting GoogleApiClient");
        }
    }

    /**
     * Get Wearable settings and put them into a DataMap
     *
     * @return DataMap containing all Wearable settings
     */
    private DataMap getSettingsDataMap() {
        DataMap settingsDataMap = new DataMap();
        settingsDataMap.putBoolean(WearableSettingsConstants.AUTO_COLLAPSE_ROOMS_KEY, WearablePreferencesHandler
                .getAutoCollapseRooms());
        settingsDataMap.putBoolean(WearableSettingsConstants.HIGHLIGHT_LAST_ACTIVATED_BUTTON_KEY, WearablePreferencesHandler
                .getHighlightLastActivatedButton());
        settingsDataMap.putBoolean(WearableSettingsConstants.SHOW_ROOM_ALL_ON_OFF_KEY, WearablePreferencesHandler.getShowRoomAllOnOff());
        settingsDataMap.putInt(WearableSettingsConstants.THEME_KEY, WearablePreferencesHandler.getTheme());
        settingsDataMap.putBoolean(WearableSettingsConstants.VIBRATE_ON_BUTTON_PRESS_KEY, WearablePreferencesHandler
                .getVibrateOnButtonPress());
        settingsDataMap.putInt(WearableSettingsConstants.VIBRATION_DURATION_KEY, WearablePreferencesHandler.getVibrationDuration());

        return settingsDataMap;
    }
}

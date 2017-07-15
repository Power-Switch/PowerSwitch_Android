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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.DaggerIntentService;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.persistence.PersistanceHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.wearable.CommunicationHelper;
import timber.log.Timber;

/**
 * Created by Markus on 06.06.2015.
 * <p/>
 * Service to handle background Network communication with the Wearable App
 */
public class UtilityService extends DaggerIntentService {

    @Inject
    PersistanceHandler persistanceHandler;

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
        Timber.d("Updating Data for Wearable");
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
        Timber.d("Updating Settings for Wearable");
        Intent intent = new Intent(context, UtilityService.class);
        intent.setAction(WearableConstants.REQUEST_SETTINGS_UPDATE_PATH);
        context.startService(intent);
    }

    /**
     * Puts a Apartment into a DataMap
     *
     * @param apartment Apartment to convert
     *
     * @return DataMap
     */
    private DataMap convertToDataMap(Apartment apartment) {
        DataMap roomDataMap = new DataMap();

        roomDataMap.putLong(WearableConstants.DATAMAP_KEY_APARTMENT_ID, apartment.getId());
        roomDataMap.putString(WearableConstants.DATAMAP_KEY_APARTMENT_NAME, apartment.getName());

        return roomDataMap;
    }

    /**
     * Puts a Room into a DataMap
     *
     * @param room Room to convert
     *
     * @return DataMap
     */
    private DataMap convertToDataMap(Room room) {
        DataMap roomDataMap = new DataMap();

        roomDataMap.putLong(WearableConstants.DATAMAP_KEY_ROOM_ID, room.getId());
        roomDataMap.putString(WearableConstants.DATAMAP_KEY_ROOM_NAME, room.getName());
        roomDataMap.putLong(WearableConstants.DATAMAP_KEY_ROOM_APARTMENT_ID, room.getApartmentId());

        return roomDataMap;
    }

    /**
     * Puts a Receiver into a DataMap
     *
     * @param receiver Receiver to convert
     *
     * @return DataMap
     */
    private DataMap convertToDataMap(Receiver receiver) {
        DataMap receiverDataMap = new DataMap();

        receiverDataMap.putLong(WearableConstants.DATAMAP_KEY_RECEIVER_ID, receiver.getId());
        receiverDataMap.putString(WearableConstants.DATAMAP_KEY_RECEIVER_NAME, receiver.getName());
        receiverDataMap.putLong(WearableConstants.DATAMAP_KEY_RECEIVER_ROOM_ID, receiver.getRoomId());
        receiverDataMap.putInt(WearableConstants.DATAMAP_KEY_RECEIVER_POSITION_IN_ROOM, receiver.getPositionInRoom());
        receiverDataMap.putLong(WearableConstants.DATAMAP_KEY_RECEIVER_LAST_ACTIVATED_BUTTON_ID, receiver.getLastActivatedButtonId());

        return receiverDataMap;
    }

    /**
     * Puts a Button into a DataMap
     *
     * @param button Button to convert
     *
     * @return DataMap
     */
    private DataMap convertToDataMap(Button button) {
        DataMap buttonDataMap = new DataMap();

        buttonDataMap.putLong(WearableConstants.DATAMAP_KEY_BUTTON_ID, button.getId());
        buttonDataMap.putString(WearableConstants.DATAMAP_KEY_BUTTON_NAME, button.getName());
        buttonDataMap.putLong(WearableConstants.DATAMAP_KEY_BUTTON_RECEIVER_ID, button.getReceiverId());

        return buttonDataMap;
    }

    /**
     * Puts a Scene into a DataMap
     *
     * @param scene Scene to convert
     *
     * @return DataMap
     */
    private DataMap convertToDataMap(Scene scene) {
        DataMap roomDataMap = new DataMap();

        roomDataMap.putLong(WearableConstants.DATAMAP_KEY_SCENE_ID, scene.getId());
        roomDataMap.putString(WearableConstants.DATAMAP_KEY_SCENE_NAME, scene.getName());

        return roomDataMap;
    }

    /**
     * Receive internal intents
     *
     * @param intent
     */
    @Override
    synchronized protected void onHandleIntent(Intent intent) {
        Timber.d("Received intent: ", intent);

        try {
            // Get Room/Receiver/Scene Data from Database and send to wearable
            if (WearableConstants.REQUEST_DATA_UPDATE_PATH.equals(intent.getAction())) {
                Timber.d("Getting Data from Database to send to Wearable...");

                if (SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID) != SettingsConstants.INVALID_APARTMENT_ID) {
                    List<Apartment> apartments = persistanceHandler.getAllApartments();

                    Apartment activeApartment = persistanceHandler.getApartment(SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID));

                    List<Room> rooms = activeApartment.getRooms();

                    List<Receiver> receivers = new ArrayList<>();
                    for (Room room : rooms) {
                        receivers.addAll(room.getReceivers());
                    }

                    List<Button> buttons = new ArrayList<>();
                    for (Receiver receiver : receivers) {
                        buttons.addAll(receiver.getButtons());
                    }
                    List<Scene> scenes = activeApartment.getScenes();

                    sendDataToWearable(apartments, rooms, receivers, buttons, scenes);
                }
            } else if (WearableConstants.REQUEST_SETTINGS_UPDATE_PATH.equals(intent.getAction())) {
                sendSettingsToWearable();
            }

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getApplicationContext(), e);
        }
    }

    /**
     * Transfer the required data over to the wearable
     *
     * @param rooms     List containing Rooms from Database
     * @param receivers List containing Receivers from Database
     */
    private void sendDataToWearable(List<Apartment> apartments, List<Room> rooms, List<Receiver> receivers, List<Button> buttons,
                                    List<Scene> scenes) {
        Timber.d("Sending new Data to Wearable...");
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .build();

        // It's OK to use blockingConnect() here as we are running in an
        // IntentService that executes work on a separate (background) thread.
        ConnectionResult connectionResult = googleApiClient.blockingConnect(SettingsConstants.GOOGLE_API_CLIENT_TIMEOUT, TimeUnit.SECONDS);

        ArrayList<DataMap> data = new ArrayList<>();


        for (Apartment apartment : apartments) {
            data.add(convertToDataMap(apartment));
        }

        for (Room room : rooms) {
            data.add(convertToDataMap(room));
        }

        for (Receiver receiver : receivers) {
            data.add(convertToDataMap(receiver));
        }

        for (Button button : buttons) {
            data.add(convertToDataMap(button));
        }

        for (Scene scene : scenes) {
            data.add(convertToDataMap(scene));
        }

        if (connectionResult.isSuccess() && googleApiClient.isConnected()) {

            PutDataMapRequest dataMap = PutDataMapRequest.create(WearableConstants.DATA_PATH);
            dataMap.getDataMap()
                    .putDataMapArrayList(WearableConstants.EXTRA_DATA, data);
            PutDataRequest request = dataMap.asPutDataRequest();

            // Send the data over
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, request)
                    .await();

            if (!result.getStatus()
                    .isSuccess()) {
                Timber.e("",
                        String.format(Locale.getDefault(),
                                "Error sending data using DataApi (error code = %d)",
                                result.getStatus()
                                        .getStatusCode()));
            } else {
                Timber.d("Update data sent");
            }

        } else {
            // GoogleApiClient connection error
            Timber.e("Error connecting GoogleApiClient");
        }
    }

    /**
     * Sends current Wearable Settings made in Smartphone app over to the Wearable companion app
     */
    private void sendSettingsToWearable() {
        Timber.d("Sending Settings to Wearable...");
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .build();

        // It's OK to use blockingConnect() here as we are running in an
        // IntentService that executes work on a separate (background) thread.
        ConnectionResult connectionResult = googleApiClient.blockingConnect(SettingsConstants.GOOGLE_API_CLIENT_TIMEOUT, TimeUnit.SECONDS);

        ArrayList<DataMap> settings        = new ArrayList<>();
        DataMap            settingsDataMap = CommunicationHelper.getSettingsDataMap();
        settings.add(settingsDataMap);

        if (connectionResult.isSuccess() && googleApiClient.isConnected()) {

            PutDataMapRequest dataMap = PutDataMapRequest.create(WearableConstants.SETTINGS_PATH);
            dataMap.getDataMap()
                    .putDataMapArrayList(WearableConstants.EXTRA_SETTINGS, settings);
            PutDataRequest request = dataMap.asPutDataRequest();

            // Send the data over
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, request)
                    .await();

            if (!result.getStatus()
                    .isSuccess()) {
                Timber.e("",
                        String.format(Locale.getDefault(),
                                "Error sending settings using DataApi (error code = %d)",
                                result.getStatus()
                                        .getStatusCode()));
            } else {
                Timber.d("Updated settings sent");
            }

        } else {
            // GoogleApiClient connection error
            Timber.e("Error connecting GoogleApiClient");
        }
    }
}

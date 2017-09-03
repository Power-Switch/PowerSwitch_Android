/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import eu.power_switch.shared.wearable.CommunicationHelper;
import eu.power_switch.shared.wearable.dataevents.ApartmentDataEvent;
import eu.power_switch.shared.wearable.dataevents.ApplicationDataEvent;
import eu.power_switch.shared.wearable.dataevents.ButtonDataEvent;
import eu.power_switch.shared.wearable.dataevents.GatewayDataEvent;
import eu.power_switch.shared.wearable.dataevents.ReceiverDataEvent;
import eu.power_switch.shared.wearable.dataevents.RoomDataEvent;
import eu.power_switch.shared.wearable.dataevents.SceneDataEvent;
import me.denley.courier.Courier;
import timber.log.Timber;

/**
 * Created by Markus on 06.06.2015.
 * <p/>
 * Service to handle background Network communication with the Wearable App
 */
public class UtilityService extends DaggerIntentService {

    @Inject
    PersistenceHandler persistenceHandler;

    @Inject
    SmartphonePreferencesHandler smartphonePreferencesHandler;

    @Inject
    WearablePreferencesHandler wearablePreferencesHandler;

    @Inject
    StatusMessageHandler statusMessageHandler;

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

                List<Apartment> apartments = persistenceHandler.getAllApartments();
                sendDataToWearable(apartments);
            } else if (WearableConstants.REQUEST_SETTINGS_UPDATE_PATH.equals(intent.getAction())) {
                sendSettingsToWearable();
            }
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(getApplicationContext(), e);
        }
    }

    /**
     * Transfer the required data over to the wearable
     */
    private void sendDataToWearable(List<Apartment> apartments) {
        Timber.d("Sending new Data to Wearable...");

        ApplicationDataEvent applicationDataEvent = convertToDataEvent(apartments);

        Courier.deliverData(this, WearableConstants.DATA_PATH, applicationDataEvent);
    }

    private ApplicationDataEvent convertToDataEvent(List<Apartment> apartments) {
        ApplicationDataEvent applicationDataEvent = new ApplicationDataEvent();

        for (Apartment apartment : apartments) {
            ApartmentDataEvent apartmentDataEvent = new ApartmentDataEvent();
            apartmentDataEvent.setId(apartment.getId());
            apartmentDataEvent.setName(apartment.getName());

            apartmentDataEvent.setRoomDataEvents(new ArrayList<RoomDataEvent>());
            ArrayList<RoomDataEvent> roomDataEvents = apartmentDataEvent.getRoomDataEvents();
            for (Room room : apartment.getRooms()) {
                RoomDataEvent roomDataEvent = convertToDataEvent(room);
                roomDataEvents.add(roomDataEvent);
            }

            apartmentDataEvent.setSceneDataEvents(new ArrayList<SceneDataEvent>());
            ArrayList<SceneDataEvent> sceneDataEvents = apartmentDataEvent.getSceneDataEvents();
            for (Scene scene : apartment.getScenes()) {
                SceneDataEvent sceneDataEvent = convertToDataEvent(scene);
                sceneDataEvents.add(sceneDataEvent);
            }

            apartmentDataEvent.setGatewayDataEvents(new ArrayList<GatewayDataEvent>());
            ArrayList<GatewayDataEvent> gatewayDataEvents = apartmentDataEvent.getGatewayDataEvents();
            for (Gateway gateway : apartment.getAssociatedGateways()) {
                GatewayDataEvent gatewayDataEvent = convertToDataEvent(gateway);
                gatewayDataEvents.add(gatewayDataEvent);
            }
        }

        return applicationDataEvent;
    }

    private RoomDataEvent convertToDataEvent(Room room) {
        RoomDataEvent roomDataEvent = new RoomDataEvent();
        roomDataEvent.setId(room.getId());
        roomDataEvent.setName(room.getName());
        roomDataEvent.setCollapsed(room.isCollapsed());

        roomDataEvent.setGatewayDataEvents(new ArrayList<GatewayDataEvent>());
        ArrayList<GatewayDataEvent> gatewayDataEvents = roomDataEvent.getGatewayDataEvents();
        for (Gateway gateway : room.getAssociatedGateways()) {
            GatewayDataEvent gatewayDataEvent = convertToDataEvent(gateway);
            gatewayDataEvents.add(gatewayDataEvent);
        }

        roomDataEvent.setReceiverDataEvents(new ArrayList<ReceiverDataEvent>());
        ArrayList<ReceiverDataEvent> receiverDataEvents = roomDataEvent.getReceiverDataEvents();
        for (Receiver receiver : room.getReceivers()) {
            ReceiverDataEvent receiverDataEvent = convertToDataEvent(receiver);
            receiverDataEvents.add(receiverDataEvent);
        }

        return roomDataEvent;
    }

    private SceneDataEvent convertToDataEvent(Scene scene) {
        SceneDataEvent sceneDataEvent = new SceneDataEvent();
        sceneDataEvent.setId(scene.getId());
        sceneDataEvent.setName(scene.getName());
        return sceneDataEvent;
    }

    private ReceiverDataEvent convertToDataEvent(Receiver receiver) {
        ReceiverDataEvent receiverDataEvent = new ReceiverDataEvent();
        receiverDataEvent.setId(receiver.getId());
        receiverDataEvent.setName(receiver.getName());
        receiverDataEvent.setLastActivatedButtonId(receiver.getLastActivatedButtonId());

        receiverDataEvent.setGatewayDataEvents(new ArrayList<GatewayDataEvent>());
        ArrayList<GatewayDataEvent> gatewayDataEvents = receiverDataEvent.getGatewayDataEvents();
        for (Gateway gateway : receiver.getAssociatedGateways()) {
            GatewayDataEvent gatewayDataEvent = convertToDataEvent(gateway);
            gatewayDataEvents.add(gatewayDataEvent);
        }

        receiverDataEvent.setButtonDataEvents(new ArrayList<ButtonDataEvent>());
        ArrayList<ButtonDataEvent> buttonDataEvents = receiverDataEvent.getButtonDataEvents();
        for (Button button : receiver.getButtons()) {
            ButtonDataEvent buttonDataEvent = convertToDataEvent(button);
            buttonDataEvents.add(buttonDataEvent);
        }

        return receiverDataEvent;
    }

    private ButtonDataEvent convertToDataEvent(Button button) {
        ButtonDataEvent buttonDataEvent = new ButtonDataEvent();
        buttonDataEvent.setId(button.getId());
        buttonDataEvent.setName(button.getName());
        return buttonDataEvent;
    }

    private GatewayDataEvent convertToDataEvent(Gateway gateway) {
        GatewayDataEvent gatewayDataEvent = new GatewayDataEvent();
        gatewayDataEvent.setId(gateway.getId());
        gatewayDataEvent.setName(gateway.getName());
        return gatewayDataEvent;
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
        DataMap            settingsDataMap = CommunicationHelper.getSettingsDataMap(this, wearablePreferencesHandler);
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
                Timber.e(String.format(Locale.getDefault(),
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

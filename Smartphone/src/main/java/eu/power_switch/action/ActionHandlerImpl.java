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

package eu.power_switch.action;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.history.HistoryHelper;
import eu.power_switch.history.HistoryItem;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.network.UdpNetworkPackage;
import eu.power_switch.notification.NotificationHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.AlarmClockConstants;
import eu.power_switch.shared.constants.PhoneConstants;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import eu.power_switch.timer.Timer;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import timber.log.Timber;

import static eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler.KEY_SHOW_GEOFENCE_NOTIFICATIONS;
import static eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler.KEY_SHOW_TIMER_NOTIFICATIONS;

/**
 * Created by Markus on 05.12.2015.
 */
@Singleton
public class ActionHandlerImpl implements ActionHandler {

    private Context                      context;
    private NetworkHandler               networkHandler;
    private NotificationHandler          notificationHandler;
    private PersistenceHandler           persistenceHandler;
    private SmartphonePreferencesHandler smartphonePreferencesHandler;
    private WearablePreferencesHandler   wearablePreferencesHandler;
    private StatusMessageHandler         statusMessageHandler;

    @Inject
    public ActionHandlerImpl(Context context, NetworkHandler networkHandler, NotificationHandler notificationHandler,
                             PersistenceHandler persistenceHandler, SmartphonePreferencesHandler smartphonePreferencesHandler,
                             WearablePreferencesHandler wearablePreferencesHandler, StatusMessageHandler statusMessageHandler) {
        this.context = context;
        this.networkHandler = networkHandler;
        this.notificationHandler = notificationHandler;
        this.persistenceHandler = persistenceHandler;
        this.smartphonePreferencesHandler = smartphonePreferencesHandler;
        this.wearablePreferencesHandler = wearablePreferencesHandler;
        this.statusMessageHandler = statusMessageHandler;
    }

    /**
     * Execute Receiver Action
     *
     * @param receiver receiver to execute on
     * @param button   button to activate
     */
    @Override
    public void execute(@NonNull Receiver receiver, @NonNull Button button) {
        try {
            executeReceiverAction(receiver, button);

            HistoryHelper.add(persistenceHandler,
                    new HistoryItem((long) -1,
                            Calendar.getInstance(),
                            context.getString(R.string.receiver_action_history_text, receiver.getName(), button.getName())));
        } catch (ActionNotSupportedException e) {
            Timber.e("Action not supported by Receiver!", e);
            statusMessageHandler.showInfoMessage(context, context.getString(R.string.action_not_supported_by_receiver), 5000);
        } catch (GatewayNotSupportedException e) {
            Timber.e("Gateway not supported by Receiver!", e);
            statusMessageHandler.showInfoMessage(context, context.getString(R.string.gateway_not_supported_by_receiver), 5000);
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, persistenceHandler, e);
            } catch (Exception e1) {
                Timber.e(e1);
            }
        }
    }

    private void executeReceiverAction(@NonNull Receiver receiver, @NonNull Button button) throws Exception {
        Apartment apartment = persistenceHandler.getContainingApartment(receiver);
        Room      room      = apartment.getRoom(receiver.getRoomId());

        ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
        List<Gateway>             gateways;
        if (!receiver.getAssociatedGateways()
                .isEmpty()) {
            gateways = receiver.getAssociatedGateways();
        } else {
            if (room.getAssociatedGateways()
                    .isEmpty()) {
                gateways = apartment.getAssociatedGateways();
            } else {
                gateways = room.getAssociatedGateways();
            }
        }

        if (gateways.isEmpty() && apartment.getAssociatedGateways()
                .isEmpty()) {
            statusMessageHandler.showInfoMessage(context, R.string.apartment_has_no_associated_gateways, Snackbar.LENGTH_LONG);
            return;
        }

        boolean hasActiveGateway = false;
        for (Gateway gateway : gateways) {
            if (gateway.isActive()) {
                hasActiveGateway = true;
                break;
            }
        }

        if (!hasActiveGateway) {
            statusMessageHandler.showInfoMessage(context, R.string.no_active_gateway, Snackbar.LENGTH_LONG);
            return;
        }

        for (Gateway gateway : gateways) {
            if (gateway.isActive()) {
                NetworkPackage networkPackage = getNetworkPackage(apartment, gateway, receiver, button);

                for (int i = 0; i < receiver.getRepetitionAmount(); i++) {
                    networkPackages.add(networkPackage);
                }
            }
        }

        networkHandler.send(networkPackages);

        // set on object, as well as in database
        receiver.setLastActivatedButtonId(button.getId());
        persistenceHandler.setLastActivatedButtonId(receiver.getId(), button.getId());

        if (smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
            ReceiverWidgetProvider.forceWidgetUpdate(context);
        }
        if (wearablePreferencesHandler.getValue(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
            UtilityService.forceWearDataUpdate(context);
        }
    }

    /**
     * Execute Room Action
     *
     * @param room       room to execute on
     * @param buttonName button name to execute on each receiver
     */
    @Override
    public void execute(@NonNull Room room, @NonNull String buttonName) {
        try {
            executeRoomAction(room, buttonName);

            HistoryHelper.add(persistenceHandler,
                    new HistoryItem((long) -1,
                            Calendar.getInstance(),
                            context.getString(R.string.room_action_history_text, room.getName(), buttonName)));
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, persistenceHandler, e);
            } catch (Exception e1) {
                Timber.e(e1);
            }
        }
    }

    /**
     * Execute Room Action
     *
     * @param room     room to execute on
     * @param buttonId button ID to execute on each receiver
     */
    @Override
    public void execute(@NonNull Room room, long buttonId) {
        try {
            executeRoomAction(room, buttonId);

            HistoryHelper.add(persistenceHandler,
                    new HistoryItem((long) -1,
                            Calendar.getInstance(),
                            context.getString(R.string.room_action_history_text,
                                    room.getName(),
                                    Button.getName(context, persistenceHandler, buttonId))));
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, persistenceHandler, e);
            } catch (Exception e1) {
                Timber.e(e1);
            }
        }
    }

    private void executeRoomAction(@NonNull Room room, @NonNull String buttonName) throws Exception {
        Apartment apartment = persistenceHandler.getContainingApartment(room);

        List<Gateway> gateways;
        if (!room.getAssociatedGateways()
                .isEmpty()) {
            gateways = room.getAssociatedGateways();
        } else {
            gateways = apartment.getAssociatedGateways();
        }

        if (gateways.isEmpty() && apartment.getAssociatedGateways()
                .isEmpty()) {
            statusMessageHandler.showInfoMessage(context, R.string.apartment_has_no_associated_gateways, Snackbar.LENGTH_LONG);
            return;
        }

        boolean hasActiveGateway = false;
        for (Gateway gateway : apartment.getAssociatedGateways()) {
            if (gateway.isActive()) {
                hasActiveGateway = true;
                break;
            }
        }

        if (!hasActiveGateway) {
            statusMessageHandler.showInfoMessage(context, R.string.no_active_gateway, Snackbar.LENGTH_LONG);
            return;
        }

        ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
        for (Receiver receiver : room.getReceivers()) {
            try {
                Button button = receiver.getButtonCaseInsensitive(buttonName);

                List<Gateway> associatedGateways;
                if (!receiver.getAssociatedGateways()
                        .isEmpty()) {
                    associatedGateways = receiver.getAssociatedGateways();
                } else {
                    associatedGateways = gateways;
                }

                for (Gateway gateway : associatedGateways) {
                    if (gateway.isActive()) {
                        try {
                            NetworkPackage networkPackage = getNetworkPackage(apartment, gateway, receiver, button);

                            for (int i = 0; i < receiver.getRepetitionAmount(); i++) {
                                networkPackages.add(networkPackage);
                            }

                            // set on object, as well as in database
                            receiver.setLastActivatedButtonId(button.getId());
                            persistenceHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
                        } catch (ActionNotSupportedException e) {
                            Timber.e("Action not supported by Receiver!", e);
                            statusMessageHandler.showInfoMessage(context, context.getString(R.string.action_not_supported_by_receiver), 5000);
                        } catch (GatewayNotSupportedException e) {
                            Timber.e("Gateway not supported by Receiver!", e);
                            statusMessageHandler.showInfoMessage(context, context.getString(R.string.gateway_not_supported_by_receiver), 5000);
                        }
                    }
                }
            } catch (NoSuchElementException e) {
                // ignore if Receiver doesnt support this action
            }
        }

        if (networkPackages.size() <= 0) {
            Timber.d(context.getString(R.string.no_receiver_supports_this_action));
            statusMessageHandler.showInfoMessage(context, context.getString(R.string.no_receiver_supports_this_action), Snackbar.LENGTH_LONG);
        } else {
            networkHandler.send(networkPackages);
        }

        if (smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
            ReceiverWidgetProvider.forceWidgetUpdate(context);
        }
        if (wearablePreferencesHandler.getValue(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
            UtilityService.forceWearDataUpdate(context);
        }
    }

    private void executeRoomAction(@NonNull Room room, long buttonId) throws Exception {
        Apartment apartment = persistenceHandler.getContainingApartment(room);

        List<Gateway> gateways;
        if (!room.getAssociatedGateways()
                .isEmpty()) {
            gateways = room.getAssociatedGateways();
        } else {
            gateways = apartment.getAssociatedGateways();
        }

        if (gateways.isEmpty() && apartment.getAssociatedGateways()
                .isEmpty()) {
            statusMessageHandler.showInfoMessage(context, R.string.apartment_has_no_associated_gateways, Snackbar.LENGTH_LONG);
            return;
        }

        boolean hasActiveGateway = false;
        for (Gateway gateway : apartment.getAssociatedGateways()) {
            if (gateway.isActive()) {
                hasActiveGateway = true;
                break;
            }
        }

        if (!hasActiveGateway) {
            statusMessageHandler.showInfoMessage(context, R.string.no_active_gateway, Snackbar.LENGTH_LONG);
            return;
        }

        ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
        for (Receiver receiver : room.getReceivers()) {
            try {
                Button button = receiver.getButton(buttonId);

                List<Gateway> associatedGateways;
                if (!receiver.getAssociatedGateways()
                        .isEmpty()) {
                    associatedGateways = receiver.getAssociatedGateways();
                } else {
                    associatedGateways = gateways;
                }

                for (Gateway gateway : associatedGateways) {
                    if (gateway.isActive()) {
                        try {
                            NetworkPackage networkPackage = getNetworkPackage(apartment, gateway, receiver, button);
                            for (int i = 0; i < receiver.getRepetitionAmount(); i++) {
                                networkPackages.add(networkPackage);
                            }
                        } catch (ActionNotSupportedException e) {
                            Timber.e("Action not supported by Receiver!", e);
                            statusMessageHandler.showInfoMessage(context, context.getString(R.string.action_not_supported_by_receiver), 5000);
                        } catch (GatewayNotSupportedException e) {
                            Timber.e("Gateway not supported by Receiver!", e);
                            statusMessageHandler.showInfoMessage(context, context.getString(R.string.gateway_not_supported_by_receiver), 5000);
                        }
                    }
                }

                // set on object, as well as in database
                receiver.setLastActivatedButtonId(button.getId());
                persistenceHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
            } catch (NoSuchElementException e) {
                // ignore if Receiver doesnt support this action
            }
        }

        if (networkPackages.size() <= 0) {
            Timber.d(context.getString(R.string.no_receiver_supports_this_action));
            statusMessageHandler.showInfoMessage(context, context.getString(R.string.no_receiver_supports_this_action), Snackbar.LENGTH_LONG);
        } else {
            networkHandler.send(networkPackages);
        }

        if (smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
            ReceiverWidgetProvider.forceWidgetUpdate(context);
        }
        if (wearablePreferencesHandler.getValue(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
            UtilityService.forceWearDataUpdate(context);
        }
    }

    /**
     * Execute Scene Action
     *
     * @param scene scene to execute
     */
    @Override
    public void execute(@NonNull Scene scene) {
        try {
            executeScene(scene);

            HistoryHelper.add(persistenceHandler,
                    new HistoryItem((long) -1, Calendar.getInstance(), context.getString(R.string.scene_action_history_text, scene.getName())));
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, persistenceHandler, e);
            } catch (Exception e1) {
                Timber.e(e1);
            }
        }
    }

    private void executeScene(@NonNull Scene scene) throws Exception {
        Apartment apartment = persistenceHandler.getContainingApartment(scene);

        if (apartment.getAssociatedGateways()
                .isEmpty()) {
            statusMessageHandler.showInfoMessage(context, R.string.apartment_has_no_associated_gateways, Snackbar.LENGTH_LONG);
            return;
        }

        boolean hasActiveGateway = false;
        for (Gateway gateway : apartment.getAssociatedGateways()) {
            if (gateway.isActive()) {
                hasActiveGateway = true;
                break;
            }
        }

        if (!hasActiveGateway) {
            statusMessageHandler.showInfoMessage(context, R.string.no_active_gateway, Snackbar.LENGTH_LONG);
            return;
        }

        // TODO: check for missing associated gateway

        ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
        for (SceneItem sceneItem : scene.getSceneItems()) {
            Receiver receiver = persistenceHandler.getReceiver(sceneItem.getReceiverId());
            Room     room     = persistenceHandler.getRoom(receiver.getRoomId());
            Button   button   = receiver.getButton(sceneItem.getButtonId());

            List<Gateway> gateways;
            if (!receiver.getAssociatedGateways()
                    .isEmpty()) {
                gateways = receiver.getAssociatedGateways();
            } else {
                if (!room.getAssociatedGateways()
                        .isEmpty()) {
                    gateways = room.getAssociatedGateways();
                } else {
                    gateways = apartment.getAssociatedGateways();
                }
            }

            for (Gateway gateway : gateways) {
                if (gateway.isActive()) {
                    NetworkPackage networkPackage = getNetworkPackage(apartment, gateway, receiver, button);

                    for (int i = 0; i < receiver.getRepetitionAmount(); i++) {
                        networkPackages.add(networkPackage);
                    }

                    // set on object, as well as in database
                    // TODO: why set in on the object? o.O
                    receiver.setLastActivatedButtonId(sceneItem.getButtonId());
                    persistenceHandler.setLastActivatedButtonId(sceneItem.getReceiverId(), sceneItem.getButtonId());
                }
            }
        }

        networkHandler.send(networkPackages);

        if (smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
            ReceiverWidgetProvider.forceWidgetUpdate(context);
        }
        if (wearablePreferencesHandler.getValue(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
            UtilityService.forceWearDataUpdate(context);
        }
    }

    /**
     * Execute Timer actions
     *
     * @param timer timer to execute
     */
    @Override
    public void execute(@NonNull Timer timer) {
        try {
            executeActions(timer.getActions());

            if (smartphonePreferencesHandler.getValue(KEY_SHOW_TIMER_NOTIFICATIONS)) {
                notificationHandler.createNotification("Timer", "Timer \"" + timer.getName() + "\" executed");
            }

            HistoryHelper.add(persistenceHandler,
                    new HistoryItem((long) -1, Calendar.getInstance(), context.getString(R.string.timer_action_history_text, timer.getName())));
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, persistenceHandler, e);
            } catch (Exception e1) {
                Timber.e(e1);
            }
        }
    }

    /**
     * Execute Sleep As Android actions
     *
     * @param event event type
     */
    @Override
    public void execute(@NonNull SleepAsAndroidConstants.Event event) {
        try {
            List<Action> actions = persistenceHandler.getAlarmActions(event);
            executeActions(actions);

            HistoryHelper.add(persistenceHandler,
                    new HistoryItem((long) -1,
                            Calendar.getInstance(),
                            context.getString(R.string.sleep_as_android_action_history_text, event.toString())));
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, persistenceHandler, e);
            } catch (Exception e1) {
                Timber.e(e1);
            }
        }
    }

    /**
     * Execute Alarm Clock actions
     *
     * @param event alarm event type
     */
    @Override
    public void execute(@NonNull AlarmClockConstants.Event event) {
        try {
            List<Action> actions = persistenceHandler.getAlarmActions(event);
            executeActions(actions);

            HistoryHelper.add(persistenceHandler,
                    new HistoryItem((long) -1,
                            Calendar.getInstance(),
                            context.getString(R.string.alarm_clock_action_history_text, event.toString())));
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, persistenceHandler, e);
            } catch (Exception e1) {
                Timber.e(e1);
            }
        }
    }

    /**
     * Execute Geofence actions
     *
     * @param geofence  geofence
     * @param eventType event type
     */
    @Override
    public void execute(@NonNull Geofence geofence, @NonNull Geofence.EventType eventType) {
        try {
            executeActions(geofence.getActions(eventType));

            HistoryItem historyItem;
            String      notificationMessage;
            if (Geofence.EventType.ENTER.equals(eventType)) {
                notificationMessage = "Geofence \"" + geofence.getName() + "\" entered";
                historyItem = new HistoryItem((long) -1,
                        Calendar.getInstance(),
                        context.getString(R.string.geofence_enter_action_history_text, geofence.getName()));
            } else if (Geofence.EventType.EXIT.equals(eventType)) {
                notificationMessage = "Geofence \"" + geofence.getName() + "\" exited";
                historyItem = new HistoryItem((long) -1,
                        Calendar.getInstance(),
                        context.getString(R.string.geofence_exit_action_history_text, geofence.getName()));
            } else {
                notificationMessage = "Geofence \"" + geofence.getName() + "\" Event: " + eventType.toString() + " activated";
                historyItem = new HistoryItem((long) -1,
                        Calendar.getInstance(),
                        context.getString(R.string.geofence_event_type_action_history_text, geofence.getName(), eventType.toString()));
            }

            if (smartphonePreferencesHandler.getValue(KEY_SHOW_GEOFENCE_NOTIFICATIONS)) {
                notificationHandler.createNotification(context.getString(R.string.geofence), notificationMessage);
            }
            HistoryHelper.add(persistenceHandler, historyItem);
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, persistenceHandler, e);
            } catch (Exception e1) {
                Timber.e(e1);
            }
        }
    }

    /**
     * Execute CallEvent actions
     *
     * @param callEvent
     * @param callType
     */
    @Override
    public void execute(CallEvent callEvent, @NonNull PhoneConstants.CallType callType) {
        try {
            executeActions(callEvent.getActions(callType));

            HistoryItem historyItem = new HistoryItem((long) -1,
                    Calendar.getInstance(),
                    context.getString(R.string.geofence_enter_action_history_text, callEvent.getName()));
            HistoryHelper.add(persistenceHandler, historyItem);
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, persistenceHandler, e);
            } catch (Exception e1) {
                Timber.e(e1);
            }
        }
    }

    private void executeActions(@NonNull List<Action> actions) throws Exception {
        Apartment apartment;
        Room      room;

        for (Action action : actions) {
            switch (action.getActionType()) {
                case Action.ACTION_TYPE_RECEIVER:
                    ReceiverAction receiverAction = (ReceiverAction) action;

                    apartment = persistenceHandler.getApartment(receiverAction.getApartmentId());
                    room = apartment.getRoom(receiverAction.getRoomId());
                    Receiver receiver = room.getReceiver(receiverAction.getReceiverId());
                    Button button = receiver.getButton(receiverAction.getButtonId());

                    executeReceiverAction(receiver, button);
                    break;
                case Action.ACTION_TYPE_ROOM:
                    RoomAction roomAction = (RoomAction) action;

                    apartment = persistenceHandler.getApartment(roomAction.getApartmentId());
                    room = apartment.getRoom(roomAction.getRoomId());

                    executeRoomAction(room, roomAction.getButtonName());
                    break;
                case Action.ACTION_TYPE_SCENE:
                    SceneAction sceneAction = (SceneAction) action;

                    apartment = persistenceHandler.getApartment(sceneAction.getApartmentId());
                    Scene scene = apartment.getScene(sceneAction.getSceneId());

                    executeScene(scene);
                    break;
            }
        }
    }

    private NetworkPackage getNetworkPackage(Apartment apartment, Gateway gateway, Receiver receiver, Button button) throws Exception {
        String signal = receiver.getSignal(gateway, button.getName());

        if (gateway.hasValidLocalAddress() && !gateway.hasValidWanAddress()) {
            // only valid local address
            Timber.d("Using local address");
            return getLocalNetworkPackage(gateway, signal);
        } else if (!gateway.hasValidLocalAddress() && gateway.hasValidWanAddress()) {
            // only valid WAN address
            Timber.d("Using WAN address");
            return getWanNetworkPackage(gateway, signal);
        } else if (gateway.hasValidLocalAddress() && gateway.hasValidWanAddress()) {
            // decide if local or WAN address should be used
            if (networkHandler.isWifiConnected() || networkHandler.isEthernetConnected()) {
                if (networkHandler.isInternetConnected()) {
                    if (!gateway.getSsids()
                            .isEmpty()) {
                        if (gateway.getSsids()
                                .contains(networkHandler.getConnectedWifiSSID())) {
                            Timber.d("Using local address, connected to SSID specified in Gateway");
                            return getLocalNetworkPackage(gateway, signal);
                        } else {
                            Timber.d("Using WAN address, connected to unspecified SSID");
                            return getWanNetworkPackage(gateway, signal);
                        }
                    } else {
                        if (apartment.getGeofence() != null && apartment.getGeofence()
                                .isActive() && Geofence.STATE_INSIDE.equals(apartment.getGeofence()
                                .getState())) {
                            Timber.d("Using local address, inside geofence");
                            return getLocalNetworkPackage(gateway, signal);
                        } else {
                            Timber.d("Using WAN address, outside or missing geofence data");
                            return getWanNetworkPackage(gateway, signal);
                        }
                    }
                } else {
                    Timber.d("Using local address, no WAN (Internet connection) available");
                    return getLocalNetworkPackage(gateway, signal);
                }
            } else {
                Timber.d("Using WAN address, no WiFi or LAN available");
                return getWanNetworkPackage(gateway, signal);
            }
        } else {
            throw new Exception("Invalid Gateway configuration!");
        }
    }

    private NetworkPackage getLocalNetworkPackage(Gateway gateway, String signal) {
        return new UdpNetworkPackage(gateway.getLocalHost(), gateway.getLocalPort(), signal, gateway.getTimeout());
    }

    private NetworkPackage getWanNetworkPackage(Gateway gateway, String signal) {
        return new UdpNetworkPackage(gateway.getWanHost(), gateway.getWanPort(), signal, gateway.getTimeout());
    }
}
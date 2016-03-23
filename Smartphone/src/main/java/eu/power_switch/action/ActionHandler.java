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

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.history.HistoryHelper;
import eu.power_switch.history.HistoryItem;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.settings.WearablePreferencesHandler;
import eu.power_switch.timer.Timer;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;

/**
 * Created by Markus on 05.12.2015.
 */
public class ActionHandler {

    /**
     * Execute Receiver Action
     *
     * @param context  any suitable context
     * @param receiver receiver to execute on
     * @param button   button to activate
     */
    public static void execute(@NonNull Context context, @NonNull Receiver receiver, @NonNull Button button) {
        try {
            executeReceiverAction(context, receiver, button);

            HistoryHelper.add(context, new HistoryItem((long) -1, Calendar.getInstance(),
                    context.getString(R.string.receiver_action_history_text, receiver.getName(), button.getName())));
        } catch (ActionNotSupportedException e) {
            Log.e("Action not supported by Receiver!", e);
            StatusMessageHandler.showInfoMessage(context,
                    context.getString(R.string.action_not_supported_by_receiver), 5000);
        } catch (GatewayNotSupportedException e) {
            Log.e("Gateway not supported by Receiver!", e);
            StatusMessageHandler.showInfoMessage(context,
                    context.getString(R.string.gateway_not_supported_by_receiver), 5000);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, e);
            } catch (Exception e1) {
                Log.e(e1);
            }
        }
    }

    private static void executeReceiverAction(@NonNull Context context, @NonNull Receiver receiver, @NonNull Button button) throws Exception {
        NetworkHandler.init(context);

        List<NetworkPackage> networkPackages = new ArrayList<>();
        Apartment apartment = DatabaseHandler.getContainingApartment(receiver);

        if (apartment.getAssociatedGateways().isEmpty()) {
            StatusMessageHandler.showInfoMessage(context, R.string.apartment_has_no_associated_gateways,
                    Snackbar.LENGTH_LONG);
            return;
        }

        for (Gateway gateway : apartment.getAssociatedGateways()) {
            if (gateway.isActive()) {
                NetworkPackage networkPackage = receiver.getNetworkPackage(gateway, button.getName());
                networkPackages.add(networkPackage);
            }
        }

        DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());

        NetworkHandler.send(networkPackages);

        if (SmartphonePreferencesHandler.getHighlightLastActivatedButton()) {
            ReceiverWidgetProvider.forceWidgetUpdate(context);
        }
        if (WearablePreferencesHandler.getHighlightLastActivatedButton()) {
            UtilityService.forceWearDataUpdate(context);
        }
    }

    /**
     * Execute Room Action
     *
     * @param context    any suitable context
     * @param room       room to execute on
     * @param buttonName button name to execute on each receiver
     */
    public static void execute(@NonNull Context context, @NonNull Room room, @NonNull String buttonName) {
        try {
            executeRoomAction(context, room, buttonName);

            HistoryHelper.add(context, new HistoryItem((long) -1, Calendar.getInstance(),
                    context.getString(R.string.room_action_history_text, room.getName(), buttonName)));
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, e);
            } catch (Exception e1) {
                Log.e(e1);
            }
        }
    }

    /**
     * Execute Room Action
     *
     * @param context  any suitable context
     * @param room     room to execute on
     * @param buttonId button ID to execute on each receiver
     */
    public static void execute(@NonNull Context context, @NonNull Room room, @NonNull long buttonId) {
        try {
            executeRoomAction(context, room, buttonId);

            HistoryHelper.add(context, new HistoryItem((long) -1, Calendar.getInstance(),
                    context.getString(R.string.room_action_history_text, room.getName(), Button.getName(context, buttonId))));
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, e);
            } catch (Exception e1) {
                Log.e(e1);
            }
        }
    }

    private static void executeRoomAction(@NonNull Context context, @NonNull Room room, @NonNull String buttonName) throws Exception {
        NetworkHandler.init(context);

        Apartment apartment = DatabaseHandler.getContainingApartment(room);
        if (apartment.getAssociatedGateways().isEmpty()) {
            StatusMessageHandler.showInfoMessage(context, R.string.apartment_has_no_associated_gateways,
                    Snackbar.LENGTH_LONG);
            return;
        }

        List<NetworkPackage> networkPackages = new ArrayList<>();
        for (Receiver receiver : room.getReceivers()) {
            try {
                Button button = receiver.getButtonCaseInsensitive(buttonName);
                for (Gateway gateway : apartment.getAssociatedGateways()) {
                    if (gateway.isActive()) {
                        try {
                            NetworkPackage networkPackage = receiver.getNetworkPackage(gateway, button.getName());
                            networkPackages.add(networkPackage);
                        } catch (ActionNotSupportedException e) {
                            Log.e("Action not supported by Receiver!", e);
                            StatusMessageHandler.showInfoMessage(context,
                                    context.getString(R.string.action_not_supported_by_receiver), 5000);
                        } catch (GatewayNotSupportedException e) {
                            Log.e("Gateway not supported by Receiver!", e);
                            StatusMessageHandler.showInfoMessage(context,
                                    context.getString(R.string.gateway_not_supported_by_receiver), 5000);
                        }
                    }
                }

                DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
            } catch (NoSuchElementException e) {
                // ignore if Receiver doesnt support this action
            }
        }

        if (networkPackages.size() <= 0) {
            Log.d(context.getString(R.string.no_receiver_supports_this_action));
            StatusMessageHandler.showInfoMessage(context,
                    context.getString(R.string.no_receiver_supports_this_action), Snackbar.LENGTH_LONG);
        } else {
            NetworkHandler.send(networkPackages);
        }

        if (SmartphonePreferencesHandler.getHighlightLastActivatedButton()) {
            ReceiverWidgetProvider.forceWidgetUpdate(context);
        }
        if (WearablePreferencesHandler.getHighlightLastActivatedButton()) {
            UtilityService.forceWearDataUpdate(context);
        }
    }

    private static void executeRoomAction(@NonNull Context context, @NonNull Room room, @NonNull long buttonId) throws Exception {
        NetworkHandler.init(context);

        Apartment apartment = DatabaseHandler.getContainingApartment(room);
        if (apartment.getAssociatedGateways().isEmpty()) {
            StatusMessageHandler.showInfoMessage(context, R.string.apartment_has_no_associated_gateways,
                    Snackbar.LENGTH_LONG);
            return;
        }

        List<NetworkPackage> networkPackages = new ArrayList<>();
        for (Receiver receiver : room.getReceivers()) {
            try {
                Button button = receiver.getButton(buttonId);
                for (Gateway gateway : apartment.getAssociatedGateways()) {
                    if (gateway.isActive()) {
                        try {
                            NetworkPackage networkPackage = receiver.getNetworkPackage(gateway, button.getName());
                            networkPackages.add(networkPackage);
                        } catch (ActionNotSupportedException e) {
                            Log.e("Action not supported by Receiver!", e);
                            StatusMessageHandler.showInfoMessage(context,
                                    context.getString(R.string.action_not_supported_by_receiver), 5000);
                        } catch (GatewayNotSupportedException e) {
                            Log.e("Gateway not supported by Receiver!", e);
                            StatusMessageHandler.showInfoMessage(context,
                                    context.getString(R.string.gateway_not_supported_by_receiver), 5000);
                        }
                    }
                }

                DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
            } catch (NoSuchElementException e) {
                // ignore if Receiver doesnt support this action
            }
        }

        if (networkPackages.size() <= 0) {
            Log.d(context.getString(R.string.no_receiver_supports_this_action));
            StatusMessageHandler.showInfoMessage(context,
                    context.getString(R.string.no_receiver_supports_this_action), Snackbar.LENGTH_LONG);
        } else {
            NetworkHandler.send(networkPackages);
        }

        if (SmartphonePreferencesHandler.getHighlightLastActivatedButton()) {
            ReceiverWidgetProvider.forceWidgetUpdate(context);
        }
        if (WearablePreferencesHandler.getHighlightLastActivatedButton()) {
            UtilityService.forceWearDataUpdate(context);
        }
    }

    /**
     * Execute Scene Action
     *
     * @param context any suitable context
     * @param scene   scene to execute
     */
    public static void execute(@NonNull Context context, @NonNull Scene scene) {
        try {
            executeScene(context, scene);

            HistoryHelper.add(context, new HistoryItem((long) -1, Calendar.getInstance(),
                    context.getString(R.string.scene_action_history_text, scene.getName())));
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, e);
            } catch (Exception e1) {
                Log.e(e1);
            }
        }
    }

    private static void executeScene(@NonNull Context context, @NonNull Scene scene) throws Exception {
        NetworkHandler.init(context);

        List<NetworkPackage> packages = new ArrayList<>();

        Apartment apartment = DatabaseHandler.getContainingApartment(scene);
        if (apartment.getAssociatedGateways().isEmpty()) {
            StatusMessageHandler.showInfoMessage(context,
                    R.string.apartment_has_no_associated_gateways, Snackbar.LENGTH_LONG);
            return;
        }

        for (Gateway gateway : apartment.getAssociatedGateways()) {
            if (gateway.isActive()) {
                for (SceneItem sceneItem : scene.getSceneItems()) {

                    packages.add(sceneItem.getReceiver().getNetworkPackage(gateway,
                            sceneItem.getActiveButton().getName()));

                    DatabaseHandler.setLastActivatedButtonId(sceneItem.getReceiver()
                            .getId(), sceneItem.getActiveButton().getId());
                }
            }
        }

        NetworkHandler.send(packages);

        if (SmartphonePreferencesHandler.getHighlightLastActivatedButton()) {
            ReceiverWidgetProvider.forceWidgetUpdate(context);
        }
        if (WearablePreferencesHandler.getHighlightLastActivatedButton()) {
            UtilityService.forceWearDataUpdate(context);
        }
    }

    /**
     * Execute Timer actions
     *
     * @param context any suitable context
     * @param timer   timer to execute
     */
    public static void execute(@NonNull Context context, @NonNull Timer timer) {
        try {
            executeActions(context, timer.getActions());

            HistoryHelper.add(context, new HistoryItem((long) -1, Calendar.getInstance(),
                    context.getString(R.string.timer_action_history_text, timer.getName())));
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, e);
            } catch (Exception e1) {
                Log.e(e1);
            }
        }
    }

    /**
     * Execute Sleep As Android actions
     *
     * @param context any suitable context
     * @param event   event type
     */
    public static void execute(@NonNull Context context, @NonNull SleepAsAndroidConstants.SLEEP_AS_ANDROID_ALARM_EVENT event) {
        try {
            List<Action> actions = DatabaseHandler.getAlarmActions(event);
            executeActions(context, actions);

            HistoryHelper.add(context, new HistoryItem((long) -1, Calendar.getInstance(),
                    context.getString(R.string.sleep_as_android_action_history_text, event.toString())));
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, e);
            } catch (Exception e1) {
                Log.e(e1);
            }
        }
    }

    /**
     * Execute Geofence actions
     *
     * @param context   any suitable context
     * @param geofence  geofence
     * @param eventType event type
     */
    public static void execute(@NonNull Context context, @NonNull Geofence geofence, @NonNull Geofence.EventType eventType) {
        try {
            executeActions(context, geofence.getActions(eventType));

            HistoryItem historyItem;
            if (Geofence.EventType.ENTER.equals(eventType)) {
                historyItem = new HistoryItem((long) -1, Calendar.getInstance(),
                        context.getString(R.string.geofence_enter_action_history_text, geofence.getName()));
            } else if (Geofence.EventType.EXIT.equals(eventType)) {
                historyItem = new HistoryItem((long) -1, Calendar.getInstance(),
                        context.getString(R.string.geofence_exit_action_history_text, geofence.getName()));
            } else {
                historyItem = new HistoryItem((long) -1, Calendar.getInstance(),
                        context.getString(R.string.geofence_event_type_action_history_text, geofence.getName(), eventType.toString()));
            }

            HistoryHelper.add(context, historyItem);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(context, e);
            try {
                HistoryHelper.add(context, e);
            } catch (Exception e1) {
                Log.e(e1);
            }
        }
    }

    private static void executeActions(@NonNull Context context, @NonNull List<Action> actions) throws Exception {
        for (Action action : actions) {
            switch (action.getActionType()) {
                case Action.ACTION_TYPE_RECEIVER:
                    ReceiverAction receiverAction = (ReceiverAction) action;
                    executeReceiverAction(context, receiverAction.getReceiver(), receiverAction.getButton());
                    break;
                case Action.ACTION_TYPE_ROOM:
                    RoomAction roomAction = (RoomAction) action;
                    executeRoomAction(context, roomAction.getRoom(), roomAction.getButtonName());
                    break;
                case Action.ACTION_TYPE_SCENE:
                    SceneAction sceneAction = (SceneAction) action;
                    executeScene(context, sceneAction.getScene());
                    break;
            }
        }
    }
}

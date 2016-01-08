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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.exception.receiver.ActionNotSupportedException;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.history.HistoryItem;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.ExternalAppConstants;
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
     * @param receiver
     * @param button
     */
    public static void execute(Context context, Receiver receiver, Button button) {
        try {
            executeReceiverAction(context, receiver, button);

            HistoryItem historyItem = new HistoryItem((long) -1, Calendar.getInstance(), context.getString(R.string
                    .receiver_action_history_text, receiver.getName(), button.getName()));
            DatabaseHandler.addHistoryItem(historyItem);
            MainActivity.sendHistoryChangedBroadcast(context);
        } catch (ActionNotSupportedException e) {
            Log.e("Action not supported by Receiver!", e);
            StatusMessageHandler.showStatusMessage(context,
                    context.getString(R.string.action_not_supported_by_receiver), 5000);
        } catch (GatewayNotSupportedException e) {
            Log.e("Gateway not supported by Receiver!", e);
            StatusMessageHandler.showStatusMessage(context,
                    context.getString(R.string.gateway_not_supported_by_receiver), 5000);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, 5000);
        }
    }

    private static void executeReceiverAction(Context context, Receiver receiver, Button button) throws Exception {
        NetworkHandler.init(context);

        List<NetworkPackage> networkPackages = new ArrayList<>();
        Apartment apartment = DatabaseHandler.getContainingApartment(receiver);

        if (apartment.getAssociatedGateways().isEmpty()) {

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
     * @param room
     * @param buttonName
     */
    public static void execute(Context context, Room room, String buttonName) {
        try {
            executeRoomAction(context, room, buttonName);

            HistoryItem historyItem = new HistoryItem((long) -1, Calendar.getInstance(), context.getString(R.string
                    .room_action_history_text, room.getName(), buttonName));
            DatabaseHandler.addHistoryItem(historyItem);
            MainActivity.sendHistoryChangedBroadcast(context);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, 5000);
        }
    }

    private static void executeRoomAction(Context context, Room room, String buttonName) throws Exception {
        NetworkHandler.init(context);

        Apartment apartment = DatabaseHandler.getContainingApartment(room);

        List<NetworkPackage> networkPackages = new ArrayList<>();
        for (Receiver receiver : room.getReceivers()) {
            Button button = receiver.getButton(buttonName);
            if (button != null) {
                for (Gateway gateway : apartment.getAssociatedGateways()) {
                    if (gateway.isActive()) {
                        try {
                            NetworkPackage networkPackage = receiver.getNetworkPackage(gateway, button.getName());
                            networkPackages.add(networkPackage);
                        } catch (ActionNotSupportedException e) {
                            Log.e("Action not supported by Receiver!", e);
                            StatusMessageHandler.showStatusMessage(context,
                                    context.getString(R.string.action_not_supported_by_receiver), 5000);
                        } catch (GatewayNotSupportedException e) {
                            Log.e("Gateway not supported by Receiver!", e);
                            StatusMessageHandler.showStatusMessage(context,
                                    context.getString(R.string.gateway_not_supported_by_receiver), 5000);
                        }
                    }
                }

                DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
            }
        }

        if (networkPackages.size() <= 0) {
            Log.d(context.getString(R.string.no_receiver_supports_this_action));
            StatusMessageHandler.showStatusMessage(context, context.getString(R.string.no_receiver_supports_this_action), Toast
                    .LENGTH_LONG);
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
     * @param scene
     */
    public static void execute(Context context, Scene scene) {
        try {
            executeScene(context, scene);

            HistoryItem historyItem = new HistoryItem((long) -1, Calendar.getInstance(), context.getString(R.string
                    .scene_action_history_text, scene.getName()));
            DatabaseHandler.addHistoryItem(historyItem);
            MainActivity.sendHistoryChangedBroadcast(context);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, 5000);
        }
    }

    private static void executeScene(Context context, Scene scene) throws Exception {
        NetworkHandler.init(context);

        List<NetworkPackage> packages = new ArrayList<>();

        Apartment apartment = DatabaseHandler.getContainingApartment(scene);
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
     * @param timer
     */
    public static void execute(Context context, Timer timer) {
        try {
            executeActions(context, timer.getActions());

            HistoryItem historyItem = new HistoryItem((long) -1, Calendar.getInstance(), context.getString(R.string
                    .timer_action_history_text, timer.getName()));
            DatabaseHandler.addHistoryItem(historyItem);
            MainActivity.sendHistoryChangedBroadcast(context);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, 5000);
        }
    }

    /**
     * Execute Sleep As Android actions
     *
     * @param context any suitable context
     * @param event
     */
    public static void execute(Context context, ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT event) {
        try {
            List<Action> actions = DatabaseHandler.getAlarmActions(event);
            executeActions(context, actions);

            HistoryItem historyItem = new HistoryItem((long) -1, Calendar.getInstance(), context.getString(R.string
                    .sleep_as_android_action_history_text, event.toString()));
            DatabaseHandler.addHistoryItem(historyItem);
            MainActivity.sendHistoryChangedBroadcast(context);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, 5000);
        }
    }

    private static void executeActions(Context context, List<Action> actions) throws Exception {
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

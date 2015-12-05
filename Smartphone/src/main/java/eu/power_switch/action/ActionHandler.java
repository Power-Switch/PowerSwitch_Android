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
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.exception.receiver.ActionNotSupportedException;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Button;
import eu.power_switch.obj.receiver.Room;
import eu.power_switch.obj.receiver.Scene;
import eu.power_switch.obj.receiver.SceneItem;
import eu.power_switch.obj.receiver.device.Receiver;
import eu.power_switch.shared.log.Log;
import eu.power_switch.wear.service.UtilityService;

/**
 * Created by Markus on 05.12.2015.
 */
public class ActionHandler {

    /**
     * Execute Receiver Action
     *
     * @param context
     * @param receiver
     * @param button
     */
    public static void executeAction(Context context, Receiver receiver, Button button) {
        try {
            NetworkHandler.init(context);

            List<NetworkPackage> networkPackages = new ArrayList<>();
            for (Gateway gateway : DatabaseHandler.getAllGateways(true)) {
                NetworkPackage networkPackage = receiver.getNetworkPackage(gateway, button.getName());
                networkPackages.add(networkPackage);
            }

            DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());

            NetworkHandler.send(networkPackages);

            UtilityService.forceWearDataUpdate(context);
        } catch (ActionNotSupportedException e) {
            Log.e("Action not supported by Receiver!", e);
            StatusMessageHandler.showStatusMessage(context,
                    context.getString(R.string.action_not_supported_by_receiver), Toast.LENGTH_LONG);
        } catch (GatewayNotSupportedException e) {
            Log.e("Gateway not supported by Receiver!", e);
            StatusMessageHandler.showStatusMessage(context,
                    context.getString(R.string.gateway_not_supported_by_receiver), Toast.LENGTH_LONG);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, 1000);
        }
    }

    /**
     * Execute Room Action
     *
     * @param context
     * @param room
     * @param buttonName
     */
    public static void executeAction(Context context, Room room, String buttonName) {
        try {
            NetworkHandler.init(context);

            List<NetworkPackage> networkPackages = new ArrayList<>();
            for (Receiver receiver : room.getReceivers()) {
                Button button = receiver.getButton(buttonName);
                for (Gateway gateway : DatabaseHandler.getAllGateways(true)) {
                    try {
                        NetworkPackage networkPackage = receiver.getNetworkPackage(gateway, button.getName());
                        networkPackages.add(networkPackage);
                    } catch (ActionNotSupportedException e) {
                        Log.e("Action not supported by Receiver!", e);
                        StatusMessageHandler.showStatusMessage(context,
                                context.getString(R.string.action_not_supported_by_receiver), Toast.LENGTH_LONG);
                    } catch (GatewayNotSupportedException e) {
                        Log.e("Gateway not supported by Receiver!", e);
                        StatusMessageHandler.showStatusMessage(context,
                                context.getString(R.string.gateway_not_supported_by_receiver), Toast.LENGTH_LONG);
                    }
                }

                if (button != null) {
                    DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
                }
            }

            if (networkPackages.size() <= 0) {
                Log.d(context.getString(R.string.no_receiver_supports_this_action));
                Toast.makeText(context, context.getString(R.string.no_receiver_supports_this_action), Toast
                        .LENGTH_LONG).show();
            } else {
                NetworkHandler.send(networkPackages);
            }

            UtilityService.forceWearDataUpdate(context);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, 1000);
        }
    }

    /**
     * Execute Scene Action
     *
     * @param context
     * @param scene
     */
    public static void executeAction(Context context, Scene scene) {
        try {
            NetworkHandler.init(context);

            List<NetworkPackage> packages = new ArrayList<>();
            for (Gateway gateway : DatabaseHandler.getAllGateways(true)) {
                for (SceneItem sceneItem : scene.getSceneItems()) {
                    packages.add(sceneItem.getReceiver().getNetworkPackage(gateway,
                            sceneItem.getActiveButton().getName()));

                    DatabaseHandler.setLastActivatedButtonId(sceneItem.getReceiver()
                            .getId(), sceneItem.getActiveButton().getId());
                }
            }
            NetworkHandler.send(packages);

            UtilityService.forceWearDataUpdate(context);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, 1000);
        }
    }
}

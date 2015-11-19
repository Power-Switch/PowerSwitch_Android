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

package eu.power_switch.api;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.receiver.ActionNotSupportedException;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.constants.ApiConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.activity.ConfigureReceiverWidgetActivity;

public class IntentReceiver extends BroadcastReceiver {

    // DEPRECATED: Old intents from Versions older than 1.0
    /**
     * Intent used to switch on a Receiver
     */
    @Deprecated
    public static String intent_switch_on = "de.ressel.powerswitch.action.Switch.ON";
    /**
     * Intent used to switch off a Receiver
     */
    @Deprecated
    public static String intent_switch_off = "de.ressel.powerswitch.action.Switch.OFF";
    /**
     * Intent used to switch on all Receivers in a Room
     */
    @Deprecated
    public static String intent_room_on = "de.ressel.powerswitch.action.Room.ON";
    /**
     * Intent used to switch off all Receivers in a Room
     */
    @Deprecated
    public static String intent_room_off = "de.ressel.powerswitch.action.Room.OFF";

    // NEW:
    private static String KEY_BUTTON = "Button";
    private static String KEY_RECEIVER = "Receiver";
    private static String KEY_ROOM = "Room";
    private static String KEY_SCENE = "Scene";

    /**
     * Generates a unique PendingIntent for actions on receivers
     *
     * @param context           any suitable context
     * @param roomName          name of Room
     * @param receiverName      name of Receiver
     * @param buttonName        name of Button
     * @param uniqueRequestCode unique identifier for different combinations of rooms, receivers and buttons
     * @return PendingIntent
     */
    public static PendingIntent buildReceiverButtonPendingIntent(Context context, String roomName,
                                                                 String receiverName, String buttonName, int uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createReceiverButtonIntent(roomName, receiverName, buttonName),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Intent createReceiverButtonIntent(String roomName, String receiverName, String buttonName) {
        Intent intent = new Intent();
        intent.setAction(ApiConstants.UNIVERSAL_ACTION_INTENT);
        intent.putExtra(IntentReceiver.KEY_ROOM, roomName);
        intent.putExtra(IntentReceiver.KEY_RECEIVER, receiverName);
        intent.putExtra(IntentReceiver.KEY_BUTTON, buttonName);

        return intent;
    }

    /**
     * Generates a unique PendingIntent for actions on rooms
     *
     * @param context           any suitable context
     * @param roomName          name of Room
     * @param buttonName        name of Button
     * @param uniqueRequestCode unique identifier for different combinations of rooms and buttons
     * @return PendingIntent
     */
    public static PendingIntent buildRoomButtonPendingIntent(Context context, String roomName, String buttonName, int uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createRoomButtonIntent(roomName, buttonName),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Intent createRoomButtonIntent(String roomName, String buttonName) {
        Intent intent = new Intent();
        intent.setAction(ApiConstants.UNIVERSAL_ACTION_INTENT);
        intent.putExtra(IntentReceiver.KEY_ROOM, roomName);
        intent.putExtra(IntentReceiver.KEY_BUTTON, buttonName);

        return intent;
    }

    /**
     * Generates a unique PendingIntent for actions on scenes
     *
     * @param context           any suitable context
     * @param sceneName         name of Scene
     * @param uniqueRequestCode unique identifier for different combinations of scenes and buttons
     * @return PendingIntent
     */
    public static PendingIntent buildSceneButtonPendingIntent(Context context, String sceneName, int uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createSceneIntent(sceneName), PendingIntent
                .FLAG_UPDATE_CURRENT);
    }

    public static Intent createSceneIntent(String sceneName) {
        Intent intent = new Intent();
        intent.setAction(ApiConstants.UNIVERSAL_ACTION_INTENT);
        intent.putExtra(IntentReceiver.KEY_SCENE, sceneName);

        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String log = "onReceive: Action: ";
            log += intent.getAction();
            Bundle extras = intent.getExtras();
            log += "{ ";
            if (extras != null) {
                for (String extra : extras.keySet()) {
                    log += extra + "[" + extras.get(extra) + "], ";
                }
            }
            log += " }";
            Log.d(this, log);
        } catch (Exception e) {
            Log.e(e);
        }

        try {
            DatabaseHandler.init(context);

            if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
                Log.d("IntentReceiver", "appwidget update");
            } else if (intent.getAction().equals(ApiConstants.UNIVERSAL_ACTION_INTENT)) {
                parseActionIntent(context, intent);
            } else if (intent.getAction().equals(intent_switch_on)
                    || intent.getAction().equals(intent_switch_off)
                    || intent.getAction().equals(intent_room_on)
                    || intent.getAction().equals(intent_room_off)) {
                parseActionIntentOld(context, intent);
            } else {
                Log.d("Received unknown intent: " + intent.getAction());
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    /**
     * @param context
     * @param intent
     */
    private void parseActionIntent(Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();
            if (extras != null) {

//                if (DatabaseHandler.getAllGateways(true).isEmpty()) {
//                    Snackbar.make(v, R.string.no_active_gateway, Snackbar.LENGTH_LONG).setAction
//                            (R.string.open_settings, new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    MainActivity.addToBackstack(SettingsTabFragment.class, "Settings");
//                                    fragmentActivity.getSupportFragmentManager()
//                                            .beginTransaction()
//                                            .setCustomAnimations(R.anim
//                                                    .slide_in_right, R.anim.slide_out_left, android.R.anim
//                                                    .slide_in_left, android.R.anim.slide_out_right)
//                                            .replace(R.id.mainContentFrameLayout, new SettingsTabFragment())
//                                            .addToBackStack(null).commit();
//                                }
//                            }).show();
//                    return;
//                }

                NetworkHandler nwm = new NetworkHandler(context);

                if (extras.containsKey(KEY_ROOM) && extras.containsKey(KEY_RECEIVER) && extras.containsKey(KEY_BUTTON)) {
                    // Expects the following Extras:
                    // Room:<RoomName>
                    // Receiver:<ReceiverName>
                    // Button:<ButtonName>

                    try {
                        Room room = DatabaseHandler.getRoom(extras.getString(KEY_ROOM));
                        Receiver receiver = room.getReceiver(extras.getString(KEY_RECEIVER));
                        Button button = receiver.getButton(extras.getString(KEY_BUTTON));

                        List<NetworkPackage> networkPackages = new ArrayList<>();
                        for (Gateway gateway : DatabaseHandler.getAllGateways(true)) {
                            NetworkPackage networkPackage = receiver.getNetworkPackage(gateway, button.getName());
                            networkPackages.add(networkPackage);
                        }

                        DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());

                        nwm.send(networkPackages);

                        UtilityService.forceWearDataUpdate(context);

                    } catch (ActionNotSupportedException e) {
                        Log.e("Action not supported by Receiver!", e);
                        Toast.makeText(context, context.getString(R.string.action_not_supported_by_receiver), Toast.LENGTH_LONG)
                                .show();
                    }

                } else if (extras.containsKey(KEY_ROOM) && extras.containsKey(KEY_BUTTON)) {
                    // Expects the following Extras:
                    // Room:<RoomName>
                    // Button:<ButtonName>
                    //
                    // Where ButtonName is the name of the Button that is
                    // pressed for each Receiver in the specified Room

                    Room room = DatabaseHandler.getRoom(extras.getString(KEY_ROOM));

                    List<NetworkPackage> networkPackages = new ArrayList<>();
                    for (Receiver receiver : room.getReceivers()) {
                        Button button = receiver.getButton(extras.getString(KEY_BUTTON));
                        for (Gateway gateway : DatabaseHandler.getAllGateways(true)) {
                            try {
                                NetworkPackage networkPackage = receiver.getNetworkPackage(gateway, button.getName());
                                networkPackages.add(networkPackage);
                            } catch (Exception e) {
                                Log.e("Action not supported by Receiver!", e);
                            }
                        }

                        if (button != null) {
                            DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
                        }
                    }

                    if (networkPackages.size() <= 0) {
                        Log.d("No Receiver in this Room supports this action!");
                        Toast.makeText(context, context.getString(R.string.no_receiver_supports_this_action), Toast
                                .LENGTH_LONG).show();
                    } else {
                        nwm.send(networkPackages);
                    }

                    UtilityService.forceWearDataUpdate(context);

                } else if (extras.containsKey(KEY_SCENE)) {
                    // Expects the following Extras:
                    // Scene:<SceneName>
                    Scene scene = DatabaseHandler.getScene(extras.getString(KEY_SCENE));
                    List<NetworkPackage> packages = new ArrayList<>();
                    for (Gateway gateway : DatabaseHandler.getAllGateways(true)) {
                        for (SceneItem sceneItem : scene.getSceneItems()) {
                            packages.add(sceneItem.getReceiver().getNetworkPackage(gateway,
                                    sceneItem.getActiveButton().getName()));

                            DatabaseHandler.setLastActivatedButtonId(sceneItem.getReceiver()
                                    .getId(), sceneItem.getActiveButton().getId());
                        }
                    }
                    nwm.send(packages);

                    UtilityService.forceWearDataUpdate(context);
                }
            } else {
                throw new NullPointerException();
            }

            SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(context);
            // force receiver widget update to highlight last button
            if (sharedPreferencesHandler.getHighlightLastActivatedButton()) {
                ConfigureReceiverWidgetActivity.forceWidgetUpdate(context);
            }
        } catch (Exception e) {
            Log.e("Error parsing intent!", e);
            Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Old method of parsing Intent Extras
     * Please use the new method parseActionIntent()
     *
     * @param context
     * @param intent
     */
    @Deprecated
    private void parseActionIntentOld(Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                NetworkHandler nwm = new NetworkHandler(context);

                int start;
                int end;

                if (intent.getAction().equals(intent_switch_on) || intent.getAction().equals(intent_switch_off)) {
                    if (extras.containsKey("Switch")) {
                        String switchProperties = extras.getString("Switch");
                        try {
                            Log.d("IntentReceiver", "Switch: " + switchProperties);

                            start = switchProperties.indexOf("room:") + 5;
                            end = switchProperties.indexOf(";switch");
                            String roomName = switchProperties.substring(start, end);

                            start = switchProperties.indexOf("switch:") + 7;
                            end = switchProperties.indexOf(";;");
                            String switchName = switchProperties.substring(start, end);

                            String buttonName;
                            if (intent.getAction().equals(intent_switch_on)) {
                                buttonName = context.getString(R.string.on);
                            } else {
                                buttonName = context.getString(R.string.off);
                            }

                            Room room = DatabaseHandler.getRoom(roomName);

                            List<NetworkPackage> networkPackages = new ArrayList<>();

                            Receiver receiver = room.getReceiver(switchName);
                            Button button = receiver.getButton(buttonName);
                            for (Gateway gateway : DatabaseHandler.getAllGateways(true)) {
                                networkPackages.add(receiver.getNetworkPackage(gateway, buttonName));
                            }
                            if (button != null) {
                                DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
                            }

                            nwm.send(networkPackages);

                        } catch (Exception e) {
                            Log.e("invalid intent string: " + switchProperties + "\n", e);
                            Toast.makeText(context,
                                    "PowerSwitch - Error: invalid intent string: " + switchProperties,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else if (intent.getAction().equals(intent_room_on) || intent.getAction().equals(intent_room_off)) {
                    if (extras.containsKey("Room")) {
                        String roomProperties = extras.getString("Room");
                        try {
                            Log.d("IntentReceiver", "Room: " + roomProperties);

                            start = 0;
                            end = roomProperties.indexOf(";;");
                            String roomName = roomProperties.substring(start, end);

                            String buttonName;
                            if (intent.getAction().equals(intent_room_on)) {
                                buttonName = context.getString(R.string.on);
                            } else {
                                buttonName = context.getString(R.string.off);
                            }

                            Room room = DatabaseHandler.getRoom(roomName);


                            List<NetworkPackage> networkPackages = new ArrayList<>();
                            for (Receiver receiver : room.getReceivers()) {
                                Button button = receiver.getButton(buttonName);
                                for (Gateway gateway : DatabaseHandler.getAllGateways(true)) {
                                    networkPackages.add(receiver.getNetworkPackage(gateway, buttonName));
                                }

                                if (button != null) {
                                    DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
                                }
                            }
                            nwm.send(networkPackages);

                        } catch (Exception e) {
                            Log.e("invalid intent string" + "\n", e);
                            Toast.makeText(context,
                                    "PowerSwitch - Error: invalid intent string: " + roomProperties,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    throw new NullPointerException();
                }
            }
        } catch (Exception e) {
            Log.e(e);
            Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG).show();
        }
    }
}

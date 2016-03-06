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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.ApiConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;

/**
 * BroadcastReceiver for action API via Intents
 */
public class IntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHandler.configureLogger();

        Log.d(this, intent);

        try {

            if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
                Log.d("IntentReceiver", "appwidget update");
            } else if (ApiConstants.UNIVERSAL_ACTION_INTENT.equals(intent.getAction())) {
                parseActionIntent(context, intent);
            } else if (ApiConstants.intent_switch_on.equals(intent.getAction())
                    || ApiConstants.intent_switch_off.equals(intent.getAction())
                    || ApiConstants.intent_room_on.equals(intent.getAction())
                    || ApiConstants.intent_room_off.equals(intent.getAction())) {
                parseActionIntentOld(context, intent);
            } else {
                Log.d("Received unknown intent: " + intent.getAction());
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    /**
     * Method for extracting information from action intents received externally (via API)
     *
     * @param context any suitable Context
     * @param intent  action intent
     */
    private void parseActionIntent(@NonNull Context context, @NonNull Intent intent) {
        try {
            Bundle extras = intent.getExtras();
//            if (extras != null && extras.containsKey(ApiConstants.KEY_APARTMENT)) {
//                // NOTE: Every action needs the Apartment:<ApartmentName> Extra (and some other)

            if (extras.containsKey(ApiConstants.KEY_ROOM) &&
                    extras.containsKey(ApiConstants.KEY_RECEIVER) &&
                    extras.containsKey(ApiConstants.KEY_BUTTON)) {
                // Expects the following Extras:
                // Room:<RoomName>
                // Receiver:<ReceiverName>
                // Button:<ButtonName>

                try {
                    Room room = DatabaseHandler.getRoom(extras.getString(ApiConstants.KEY_ROOM).trim());
                    Receiver receiver = room.getReceiver(extras.getString(ApiConstants.KEY_RECEIVER).trim());
                    Button button = receiver.getButton(extras.getString(ApiConstants.KEY_BUTTON).trim());

                    ActionHandler.execute(context, receiver, button);
                } catch (Exception e) {
                    Log.e("Error!", e);
                    Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG).show();
                }

            } else if (extras.containsKey(ApiConstants.KEY_APARTMENT) &&
                    extras.containsKey(ApiConstants.KEY_ROOM) &&
                    extras.containsKey(ApiConstants.KEY_BUTTON)) {
                // Expects the following Extras:
                // Room:<RoomName>
                // Button:<ButtonName>
                //
                // Where ButtonName is the name of the Button that is
                // pressed for each Receiver in the specified Room

                try {
                    Apartment apartment = DatabaseHandler.getApartment(extras.getString(ApiConstants.KEY_APARTMENT).trim());
                    Room room = apartment.getRoom(extras.getString(ApiConstants.KEY_ROOM).trim());
                    String buttonName = extras.getString(ApiConstants.KEY_BUTTON).trim();

                    ActionHandler.execute(context, room, buttonName);
                } catch (Exception e) {
                    Log.e("Error!", e);
                    Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG).show();
                }
            } else if (extras.containsKey(ApiConstants.KEY_APARTMENT) &&
                    extras.containsKey(ApiConstants.KEY_SCENE)) {
                // Expects the following Extras:
                // Scene:<SceneName>

                try {
                    Apartment apartment = DatabaseHandler.getApartment(extras.getString(ApiConstants.KEY_APARTMENT).trim());
                    Scene scene = apartment.getScene(extras.getString(ApiConstants.KEY_SCENE).trim());

                    ActionHandler.execute(context, scene);
                } catch (Exception e) {
                    Log.e("Error!", e);
                    Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG).show();
                }
//                }
            } else {
                throw new NullPointerException("extras are null!");
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
     * @param context any suitable Context
     * @param intent  action intent
     */
    @Deprecated
    private void parseActionIntentOld(Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                NetworkHandler.init(context);

                int start;
                int end;

                if (intent.getAction().equals(ApiConstants.intent_switch_on) || intent.getAction()
                        .equals(ApiConstants.intent_switch_off)) {
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
                            if (intent.getAction().equals(ApiConstants.intent_switch_on)) {
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

                            NetworkHandler.send(networkPackages);

                        } catch (Exception e) {
                            Log.e("invalid intent string: " + switchProperties + "\n", e);
                            Toast.makeText(context,
                                    "PowerSwitch - Error: invalid intent string: " + switchProperties,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else if (intent.getAction().equals(ApiConstants.intent_room_on) || intent.getAction()
                        .equals(ApiConstants.intent_room_off)) {
                    if (extras.containsKey("Room")) {
                        String roomProperties = extras.getString("Room");
                        try {
                            Log.d("IntentReceiver", "Room: " + roomProperties);

                            start = 0;
                            end = roomProperties.indexOf(";;");
                            String roomName = roomProperties.substring(start, end);

                            String buttonName;
                            if (intent.getAction().equals(ApiConstants.intent_room_on)) {
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
                            NetworkHandler.send(networkPackages);

                        } catch (Exception e) {
                            Log.e("invalid intent string" + "\n", e);
                            Toast.makeText(context,
                                    "PowerSwitch - Error: invalid intent string: " + roomProperties,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    throw new NullPointerException("extras are null!");
                }
            }
        } catch (Exception e) {
            Log.e(e);
            Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG).show();
        }
    }
}

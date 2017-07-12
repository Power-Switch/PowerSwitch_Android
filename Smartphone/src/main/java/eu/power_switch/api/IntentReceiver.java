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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import dagger.android.DaggerBroadcastReceiver;
import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.network.NetworkHandlerImpl;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.ApiConstants;
import timber.log.Timber;

/**
 * BroadcastReceiver for action API via Intents
 */
public class IntentReceiver extends DaggerBroadcastReceiver {

    @Inject
    ActionHandler actionHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Timber.d("Received intent: ", intent);

        try {
            if (intent.getAction()
                    .equals("android.appwidget.action.APPWIDGET_UPDATE")) {
                Timber.d("IntentReceiver", "appwidget update");
            } else if (ApiConstants.UNIVERSAL_ACTION_INTENT.equals(intent.getAction())) {
                parseActionIntent(context, intent);
            } else if (ApiConstants.intent_switch_on.equals(intent.getAction()) || ApiConstants.intent_switch_off.equals(intent.getAction()) || ApiConstants.intent_room_on.equals(
                    intent.getAction()) || ApiConstants.intent_room_off.equals(intent.getAction())) {
                parseActionIntentOld(context, intent);
            } else {
                Timber.d("Received unknown intent: " + intent.getAction());
            }
        } catch (Exception e) {
            Timber.e(e);
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

            if (extras.containsKey(ApiConstants.KEY_ROOM) && extras.containsKey(ApiConstants.KEY_RECEIVER) && extras.containsKey(ApiConstants.KEY_BUTTON)) {
                // Expects the following Extras:
                // Room:<RoomName>
                // Receiver:<ReceiverName>
                // Button:<ButtonName>

                Room room = DatabaseHandler.getRoomCaseInsensitive(extras.getString(ApiConstants.KEY_ROOM)
                        .trim());
                Receiver receiver = room.getReceiverCaseInsensitive(extras.getString(ApiConstants.KEY_RECEIVER)
                        .trim());
                Button button = receiver.getButtonCaseInsensitive(extras.getString(ApiConstants.KEY_BUTTON)
                        .trim());

                actionHandler.execute(receiver, button);
            } else if (extras.containsKey(ApiConstants.KEY_APARTMENT) && extras.containsKey(ApiConstants.KEY_ROOM) && extras.containsKey(ApiConstants.KEY_BUTTON)) {
                // Expects the following Extras:
                // Room:<RoomName>
                // Button:<ButtonName>
                //
                // Where ButtonName is the name of the Button that is
                // pressed for each Receiver in the specified Room

                Apartment apartment = DatabaseHandler.getApartmentCaseInsensitive(extras.getString(ApiConstants.KEY_APARTMENT)
                        .trim());
                Room room = apartment.getRoomCaseInsensitive(extras.getString(ApiConstants.KEY_ROOM)
                        .trim());
                String buttonName = extras.getString(ApiConstants.KEY_BUTTON)
                        .trim();

                actionHandler.execute(room, buttonName);
            } else if (extras.containsKey(ApiConstants.KEY_APARTMENT) && extras.containsKey(ApiConstants.KEY_SCENE)) {
                // Expects the following Extras:
                // Scene:<SceneName>

                Apartment apartment = DatabaseHandler.getApartmentCaseInsensitive(extras.getString(ApiConstants.KEY_APARTMENT)
                        .trim());
                Scene scene = apartment.getSceneCaseInsensitive(extras.getString(ApiConstants.KEY_SCENE)
                        .trim());

                actionHandler.execute(scene);
            } else {
                Toast.makeText(context, context.getString(R.string.invalid_arguments), Toast.LENGTH_LONG)
                        .show();
            }
        } catch (NoSuchElementException e) {
            Timber.e(e);
            Toast.makeText(context, context.getString(R.string.error_executing_action_template, e.getMessage()), Toast.LENGTH_LONG)
                    .show();
        } catch (Exception e) {
            Timber.e("Error parsing intent!", e);
            Toast.makeText(context, context.getString(R.string.error_parsing_intent, e.getMessage()), Toast.LENGTH_LONG)
                    .show();
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
                NetworkHandlerImpl.init(context);

                int start;
                int end;

                if (intent.getAction()
                        .equals(ApiConstants.intent_switch_on) || intent.getAction()
                        .equals(ApiConstants.intent_switch_off)) {
                    if (extras.containsKey("Switch")) {
                        String switchProperties = extras.getString("Switch");
                        try {
                            Timber.d("IntentReceiver", "Switch: " + switchProperties);

                            start = switchProperties.indexOf("room:") + 5;
                            end = switchProperties.indexOf(";switch");
                            String roomName = switchProperties.substring(start, end);

                            start = switchProperties.indexOf("switch:") + 7;
                            end = switchProperties.indexOf(";;");
                            String switchName = switchProperties.substring(start, end);

                            String buttonName;
                            if (intent.getAction()
                                    .equals(ApiConstants.intent_switch_on)) {
                                buttonName = context.getString(R.string.on);
                            } else {
                                buttonName = context.getString(R.string.off);
                            }

                            Room     room     = DatabaseHandler.getRoomCaseInsensitive(roomName);
                            Receiver receiver = room.getReceiverCaseInsensitive(switchName);
                            Button   button   = receiver.getButtonCaseInsensitive(buttonName);

                            actionHandler.execute(receiver, button);
                        } catch (Exception e) {
                            Timber.e("invalid intent string: " + switchProperties + "\n", e);
                            Toast.makeText(context, "PowerSwitch - Error: invalid intent string: " + switchProperties, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                } else if (intent.getAction()
                        .equals(ApiConstants.intent_room_on) || intent.getAction()
                        .equals(ApiConstants.intent_room_off)) {
                    if (extras.containsKey("Room")) {
                        String roomProperties = extras.getString("Room");
                        try {
                            Timber.d("IntentReceiver", "Room: " + roomProperties);

                            start = 0;
                            end = roomProperties.indexOf(";;");
                            String roomName = roomProperties.substring(start, end);

                            String buttonName;
                            if (intent.getAction()
                                    .equals(ApiConstants.intent_room_on)) {
                                buttonName = context.getString(R.string.on);
                            } else {
                                buttonName = context.getString(R.string.off);
                            }

                            Room room = DatabaseHandler.getRoomCaseInsensitive(roomName);

                            actionHandler.execute(room, buttonName);
                        } catch (Exception e) {
                            Timber.e("invalid intent string" + "\n", e);
                            Toast.makeText(context, "PowerSwitch - Error: invalid intent string: " + roomProperties, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                } else {
                    throw new NullPointerException("extras are null!");
                }
            }
        } catch (Exception e) {
            Timber.e(e);
            Toast.makeText(context, context.getString(R.string.error_parsing_intent, e.getMessage()), Toast.LENGTH_LONG)
                    .show();
        }
    }
}

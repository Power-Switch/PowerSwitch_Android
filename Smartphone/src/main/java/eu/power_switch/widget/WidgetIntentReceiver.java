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

package eu.power_switch.widget;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.WidgetConstants;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;
import eu.power_switch.shared.log.Log;

/**
 * Intent Receiver for Widgets
 * This Class handles all actions done on any widget type
 * <p/>
 * Created by Markus on 07.11.2015.
 */
public class WidgetIntentReceiver extends BroadcastReceiver {

    /**
     * Generates a unique PendingIntent for actions on receiver widgets
     *
     * @param context           any suitable context
     * @param room              Room
     * @param receiver          Receiver
     * @param button            Button
     * @param uniqueRequestCode unique identifier for different combinations of rooms, receivers and buttons
     * @return PendingIntent
     */
    public static PendingIntent buildReceiverWidgetActionPendingIntent(Context context, Apartment apartment, Room room, Receiver receiver,
                                                                       Button button, int uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createReceiverButtonIntent(apartment.getName(), room.getName(),
                receiver.getName(), button.getName()), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent createReceiverButtonIntent(String apartmentName, String roomName, String receiverName, String buttonName) {
        Intent intent = new Intent();
        intent.setAction(WidgetConstants.WIDGET_ACTION_INTENT);
        intent.putExtra(WidgetConstants.KEY_APARTMENT, apartmentName);
        intent.putExtra(WidgetConstants.KEY_ROOM, roomName);
        intent.putExtra(WidgetConstants.KEY_RECEIVER, receiverName);
        intent.putExtra(WidgetConstants.KEY_BUTTON, buttonName);

        return intent;
    }

    /**
     * Generates a unique PendingIntent for actions on room widgets
     *
     * @param context           any suitable context
     * @param room              Room
     * @param buttonName        name of Button
     * @param uniqueRequestCode unique identifier for different combinations of rooms, receivers and buttons
     * @return PendingIntent
     */
    public static PendingIntent buildRoomWidgetButtonPendingIntent(Context context, Apartment apartment, Room room, String buttonName,
                                                                   int uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createRoomButtonIntent(apartment.getName(), room.getName(), buttonName),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent createRoomButtonIntent(String apartmentName, String roomName, String buttonName) {
        Intent intent = new Intent();
        intent.setAction(WidgetConstants.WIDGET_ACTION_INTENT);
        intent.putExtra(WidgetConstants.KEY_APARTMENT, apartmentName);
        intent.putExtra(WidgetConstants.KEY_ROOM, roomName);
        intent.putExtra(WidgetConstants.KEY_BUTTON, buttonName);

        return intent;
    }

    /**
     * Generates a unique PendingIntent for actions on scene widgets
     *
     * @param context           any suitable context
     * @param scene             Scene
     * @param uniqueRequestCode unique identifier for different combinations of rooms, receivers and buttons
     * @return PendingIntent
     */
    public static PendingIntent buildSceneWidgetPendingIntent(Context context, Apartment apartment, Scene scene, int
            uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createSceneIntent(apartment.getName(), scene.getName()), PendingIntent
                .FLAG_UPDATE_CURRENT);
    }

    private static Intent createSceneIntent(String apartmentName, String sceneName) {
        Intent intent = new Intent();
        intent.setAction(WidgetConstants.WIDGET_ACTION_INTENT);
        intent.putExtra(WidgetConstants.KEY_APARTMENT, apartmentName);
        intent.putExtra(WidgetConstants.KEY_SCENE, sceneName);

        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this, intent);

        try {
            if (intent.getAction().equals(WidgetConstants.WIDGET_ACTION_INTENT)) {
                // vibrate
                if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS)) {
                    VibrationHandler.vibrate(context, SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION));
                }

                parseWidgetActionIntent(context, intent);
            } else {
                Log.d("Received unknown intent: " + intent.getAction());
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private void parseWidgetActionIntent(Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();
            if (extras.containsKey(WidgetConstants.KEY_APARTMENT)
                    && extras.containsKey(WidgetConstants.KEY_ROOM)
                    && extras.containsKey(WidgetConstants.KEY_RECEIVER)
                    && extras.containsKey(WidgetConstants.KEY_BUTTON)) {
                String apartmentName = extras.getString(WidgetConstants.KEY_APARTMENT);
                String roomName = extras.getString(WidgetConstants.KEY_ROOM);
                String receiverName = extras.getString(WidgetConstants.KEY_RECEIVER);
                String buttonName = extras.getString(WidgetConstants.KEY_BUTTON);

                Apartment apartment = DatabaseHandler.getApartment(apartmentName);
                Room room = apartment.getRoom(roomName);
                Receiver receiver = room.getReceiver(receiverName);
                Button button = receiver.getButton(buttonName);

                ActionHandler.execute(context, receiver, button);
            } else if (extras.containsKey(WidgetConstants.KEY_APARTMENT)
                    && extras.containsKey(WidgetConstants.KEY_ROOM)
                    && extras.containsKey(WidgetConstants.KEY_BUTTON)) {
                String apartmentName = extras.getString(WidgetConstants.KEY_APARTMENT);
                String roomName = extras.getString(WidgetConstants.KEY_ROOM);
                String buttonName = extras.getString(WidgetConstants.KEY_BUTTON);

                Apartment apartment = DatabaseHandler.getApartment(apartmentName);
                Room room = apartment.getRoom(roomName);

                ActionHandler.execute(context, room, buttonName);
            } else if (extras.containsKey(WidgetConstants.KEY_APARTMENT)
                    && extras.containsKey(WidgetConstants.KEY_SCENE)) {
                String apartmentName = extras.getString(WidgetConstants.KEY_APARTMENT);
                String sceneName = extras.getString(WidgetConstants.KEY_SCENE);

                Apartment apartment = DatabaseHandler.getApartment(apartmentName);
                Scene scene = apartment.getScene(sceneName);

                ActionHandler.execute(context, scene);
            } else {
                Toast.makeText(context, R.string.invalid_arguments, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("Error parsing intent!", e);
            Toast.makeText(context, context.getString(R.string.error_parsing_intent, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}

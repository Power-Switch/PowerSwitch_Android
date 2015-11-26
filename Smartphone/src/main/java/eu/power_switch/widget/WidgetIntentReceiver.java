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
import eu.power_switch.api.IntentReceiver;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.constants.WidgetConstants;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;

/**
 * Intent Receiver for Widgets
 * This Class handles all actions done on any widget type
 * <p/>
 * Created by Markus on 07.11.2015.
 */
public class WidgetIntentReceiver extends BroadcastReceiver {

    private static String KEY_BUTTON = "Button";
    private static String KEY_RECEIVER = "Receiver";
    private static String KEY_ROOM = "Room";
    private static String KEY_SCENE = "Scene";

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
    public static PendingIntent buildReceiverWidgetActionPendingIntent(Context context, Room room, Receiver receiver,
                                                                       Button button, int uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createReceiverButtonIntent(room.getName(),
                receiver.getName(), button.getName()), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent createReceiverButtonIntent(String roomName, String receiverName, String buttonName) {
        Intent intent = new Intent();
        intent.setAction(WidgetConstants.WIDGET_ACTION_INTENT);
        intent.putExtra(KEY_ROOM, roomName);
        intent.putExtra(KEY_RECEIVER, receiverName);
        intent.putExtra(KEY_BUTTON, buttonName);

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
    public static PendingIntent buildRoomWidgetButtonPendingIntent(Context context, Room room, String buttonName,
                                                                   int uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createRoomButtonIntent(room.getName(), buttonName),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent createRoomButtonIntent(String roomName, String buttonName) {
        Intent intent = new Intent();
        intent.setAction(WidgetConstants.WIDGET_ACTION_INTENT);
        intent.putExtra(KEY_ROOM, roomName);
        intent.putExtra(KEY_BUTTON, buttonName);

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
    public static PendingIntent buildSceneWidgetPendingIntent(Context context, Scene scene, int
            uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createSceneIntent(scene.getName()), PendingIntent
                .FLAG_UPDATE_CURRENT);
    }

    private static Intent createSceneIntent(String sceneName) {
        Intent intent = new Intent();
        intent.setAction(WidgetConstants.WIDGET_ACTION_INTENT);
        intent.putExtra(KEY_SCENE, sceneName);

        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHandler.configureLogger();

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
            if (intent.getAction().equals(WidgetConstants.WIDGET_ACTION_INTENT)) {
                SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(context);
                // vibrate
                if (sharedPreferencesHandler.getVibrateOnButtonPress()) {
                    VibrationHandler.vibrate(context, sharedPreferencesHandler.getVibrationDuration());
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
            if (extras.containsKey(KEY_ROOM) && extras.containsKey(KEY_RECEIVER) && extras.containsKey(KEY_BUTTON)) {
                String roomName = extras.getString(KEY_ROOM);
                String receiverName = extras.getString(KEY_RECEIVER);
                String buttonName = extras.getString(KEY_BUTTON);

                IntentReceiver.parseActionIntent(context,
                        IntentReceiver.createReceiverButtonIntent(roomName, receiverName, buttonName));
            } else if (extras.containsKey(KEY_ROOM) && extras.containsKey(KEY_BUTTON)) {
                String roomName = extras.getString(KEY_ROOM);
                String buttonName = extras.getString(KEY_BUTTON);

                IntentReceiver.parseActionIntent(context,
                        IntentReceiver.createRoomButtonIntent(roomName, buttonName));
            } else if (extras.containsKey(KEY_SCENE)) {
                String sceneName = extras.getString(KEY_SCENE);

                IntentReceiver.parseActionIntent(context,
                        IntentReceiver.createSceneIntent(sceneName));
            }
        } catch (Exception e) {
            Log.e("Error parsing intent!", e);
            Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG).show();
        }
    }
}

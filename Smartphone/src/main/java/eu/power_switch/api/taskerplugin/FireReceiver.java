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

package eu.power_switch.api.taskerplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.ApiConstants;
import eu.power_switch.shared.log.Log;

/**
 * Created by Markus on 22.02.2016.
 */
public class FireReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(intent);

        if (com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
            parseIntent(context, intent);
        }
    }

    private void parseIntent(Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();
//            if (extras != null && extras.containsKey(ApiConstants.KEY_APARTMENT)) {
//                // NOTE: Every action needs the Apartment:<ApartmentName> Extra (and some other)

            if (extras.containsKey(ApiConstants.KEY_APARTMENT) && extras.containsKey(ApiConstants.KEY_ROOM) && extras
                    .containsKey(ApiConstants.KEY_RECEIVER) && extras
                    .containsKey(ApiConstants.KEY_BUTTON)) {

                try {
                    Room room = DatabaseHandler.getRoom(extras.getLong(ApiConstants.KEY_ROOM));
                    Receiver receiver = room.getReceiver(extras.getLong(ApiConstants.KEY_RECEIVER));
                    Button button = receiver.getButton(extras.getLong(ApiConstants.KEY_BUTTON));

                    ActionHandler.execute(context, receiver, button);
                } catch (Exception e) {
                    Log.e("Error!", e);
                    Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG)
                            .show();
                }

            } else if (extras.containsKey(ApiConstants.KEY_APARTMENT) && extras.containsKey(ApiConstants.KEY_ROOM) &&
                    extras.containsKey(ApiConstants.KEY_BUTTON)) {

                try {
                    Apartment apartment = DatabaseHandler.getApartment(extras.getLong(ApiConstants.KEY_APARTMENT));
                    Room room = apartment.getRoom(extras.getLong(ApiConstants.KEY_ROOM));
                    String buttonName = extras.getString(ApiConstants.KEY_BUTTON).trim();

                    ActionHandler.execute(context, room, buttonName);
                } catch (Exception e) {
                    Log.e("Error!", e);
                    Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG)
                            .show();
                }
            } else if (extras.containsKey(ApiConstants.KEY_APARTMENT) && extras.containsKey(ApiConstants.KEY_SCENE)) {

                try {
                    Apartment apartment = DatabaseHandler.getApartment(extras.getLong(ApiConstants.KEY_APARTMENT));
                    Scene scene = apartment.getScene(extras.getLong(ApiConstants.KEY_SCENE));

                    ActionHandler.execute(context, scene);
                } catch (Exception e) {
                    Log.e("Error!", e);
                    Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG)
                            .show();
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

}

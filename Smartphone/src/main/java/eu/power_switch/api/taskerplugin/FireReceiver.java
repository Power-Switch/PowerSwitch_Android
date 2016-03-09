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

import java.util.NoSuchElementException;

import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.ApiConstants;
import eu.power_switch.shared.log.Log;

/**
 * BroadcastReceiver responsible for executing actions fired by Tasker
 * <p/>
 * Created by Markus on 22.02.2016.
 */
public class FireReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(intent);

        if (com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
            parseActionIntent(context, intent);
        }
    }

    private void parseActionIntent(Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();

            if (extras.containsKey(ApiConstants.KEY_APARTMENT) &&
                    extras.containsKey(ApiConstants.KEY_ROOM) &&
                    extras.containsKey(ApiConstants.KEY_RECEIVER) &&
                    extras.containsKey(ApiConstants.KEY_BUTTON)) {

                Apartment apartment = DatabaseHandler.getApartmentCaseInsensitive(extras.getString(ApiConstants.KEY_APARTMENT).trim());
                Room room = apartment.getRoomCaseInsensitive(extras.getString(ApiConstants.KEY_ROOM).trim());
                Receiver receiver = room.getReceiverCaseInsensitive(extras.getString(ApiConstants.KEY_RECEIVER).trim());
                Button button = receiver.getButtonCaseInsensitive(extras.getString(ApiConstants.KEY_BUTTON).trim());

                ActionHandler.execute(context, receiver, button);
            } else if (extras.containsKey(ApiConstants.KEY_APARTMENT) &&
                    extras.containsKey(ApiConstants.KEY_ROOM) &&
                    extras.containsKey(ApiConstants.KEY_BUTTON)) {

                Apartment apartment = DatabaseHandler.getApartmentCaseInsensitive(extras.getString(ApiConstants.KEY_APARTMENT).trim());
                Room room = apartment.getRoomCaseInsensitive(extras.getString(ApiConstants.KEY_ROOM).trim());
                String buttonName = extras.getString(ApiConstants.KEY_BUTTON).trim();

                ActionHandler.execute(context, room, buttonName);
            } else if (extras.containsKey(ApiConstants.KEY_APARTMENT) &&
                    extras.containsKey(ApiConstants.KEY_SCENE)) {

                Apartment apartment = DatabaseHandler.getApartmentCaseInsensitive(extras.getString(ApiConstants.KEY_APARTMENT).trim());
                Scene scene = apartment.getSceneCaseInsensitive(extras.getString(ApiConstants.KEY_SCENE).trim());

                ActionHandler.execute(context, scene);
            } else {
                Toast.makeText(context, context.getString(R.string.invalid_arguments), Toast.LENGTH_LONG).show();
            }
        } catch (NoSuchElementException e) {
            Log.e(this, e);
            Toast.makeText(context, context.getString(R.string.error_executing_action_template, e.getMessage()), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("Error parsing intent!", e);
            Toast.makeText(context, context.getString(R.string.error_parsing_intent, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}

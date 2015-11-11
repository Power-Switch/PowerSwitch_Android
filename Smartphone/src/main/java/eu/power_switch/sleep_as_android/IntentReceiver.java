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

package eu.power_switch.sleep_as_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.log.Log;
import eu.power_switch.shared.constants.ExternalAppConstants;

/**
 * IntentReceiver to handle any Sleep As Android related Intents
 * <p/>
 * Created by Markus on 08.10.2015.
 */
public class IntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String log = "onReceive: Action: ";
            log += intent.getAction();
            log += "( ";
            if (intent.getData() != null) {
                log += intent.getData().getScheme();
                log += "://";
                log += intent.getData().getHost();
            }
            log += " ) ";
            Bundle extras = intent.getExtras();
            log += "{ ";
            if (extras != null) {
                for (String extra : extras.keySet()) {
                    log += extra + "[" + extras.get(extra) + "], ";
                }
            }
            log += " }";
            Log.d("IntentReceiver", log);
        } catch (Exception e) {
            Log.e(e);
        }

        try {
            DatabaseHandler.init(context);

            if (intent.getAction().equals(ExternalAppConstants.ALARM_TRIGGERED_INTENT)) {
                Log.d("IntentReceiver", "Alarm triggered!");
            } else if (intent.getAction().equals(ExternalAppConstants.ALARM_SNOOZED_INTENT)) {
                Log.d("IntentReceiver", "Alarm snoozed...");
            } else if (intent.getAction().equals(ExternalAppConstants.ALARM_DISMISSED_INTENT)) {
                Log.d("IntentReceiver", "Alarm dismissed...");
            } else {
                Log.d("IntentReceiver", "Received unknown intent: " + intent.getAction());
            }

        } catch (Exception e) {
            Log.e(e);
        }
    }
}

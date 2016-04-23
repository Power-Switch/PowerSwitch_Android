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

package eu.power_switch.alarm_clock.sleep_as_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.power_switch.action.ActionHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;

/**
 * IntentReceiver to handle any Sleep As Android related Intents
 * <p/>
 * Created by Markus on 08.10.2015.
 */
public class IntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHandler.configureLogger(context);

        Log.d(this, intent);

        try {
            if (!SmartphonePreferencesHandler.getSleepAsAndroidEnabled()) {
                return;
            }

            if (SleepAsAndroidConstants.Event.ALARM_TRIGGERED.getIntentAction()
                    .equals(intent.getAction())) {
                Log.d("IntentReceiver", "Alarm triggered!");
                ActionHandler.execute(context, SleepAsAndroidConstants.Event.ALARM_TRIGGERED);

            } else if (SleepAsAndroidConstants.Event.ALARM_SNOOZED.getIntentAction()
                    .equals(intent.getAction())) {
                Log.d("IntentReceiver", "Alarm snoozed...");
                ActionHandler.execute(context, SleepAsAndroidConstants.Event.ALARM_SNOOZED);

            } else if (SleepAsAndroidConstants.Event.ALARM_DISMISSED.getIntentAction()
                    .equals(intent.getAction())) {
                Log.d("IntentReceiver", "Alarm dismissed...");
                ActionHandler.execute(context, SleepAsAndroidConstants.Event.ALARM_DISMISSED);

            } else {
                Log.d("IntentReceiver", "Received unknown intent: " + intent.getAction());
            }

        } catch (Exception e) {
            Log.e(e);
        }
    }
}

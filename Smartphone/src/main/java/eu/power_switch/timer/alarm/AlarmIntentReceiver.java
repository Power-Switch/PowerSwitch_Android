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

package eu.power_switch.timer.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import java.util.Calendar;

import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.shared.constants.TimerConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;

/**
 * IntentReceiver to handle any Alarm/Timer related Intents
 * This Class is responsible for restarting all Alarms on device boot
 * and also contains the logic whether a timer should be triggered or not.
 * <p/>
 * Created by Markus on 21.09.2015.
 */
public class AlarmIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHandler.configureLogger();

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
            Log.d(this, log);
        } catch (Exception e) {
            Log.e(e);
        }

        try {
            if (intent.getAction().equals(TimerConstants.TIMER_ACTIVATION_INTENT)) {
                Log.d(this, "parsing timer activation intent...");
                parseActionIntent(context, intent);
            } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                // restart all active alarms because device rebooted
                Log.d(this, "restarting all active alarms because device rebooted...");
                reinitializeAlarms(context);
            } else {
                Log.d(this, "Received unknown intent: " + intent.getAction());
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private void parseActionIntent(Context context, Intent intent) {
        try {
            long id = Long.valueOf(intent.getData().getHost());
            Timer timer = DatabaseHandler.getTimer(id);

            Calendar currentTime = Calendar.getInstance();
            if (Build.VERSION.SDK_INT < 19) {
                int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                int executionHour = timer.getExecutionTime().get(Calendar.HOUR_OF_DAY);
                if (currentHour != executionHour) {
                    Log.d(this, "Timer hour doesnt match: " + currentHour + " != " + timer
                            .getExecutionTime().get(Calendar.HOUR_OF_DAY));
                    return;
                }

                int currentMinute = currentTime.get(Calendar.MINUTE);
                int executionMinute = timer.getExecutionTime().get(Calendar.MINUTE);
                if (!(currentMinute >= executionMinute && currentMinute <= executionMinute + 3)) {
                    Log.d(this, "Timer minute not in valid range: currentMinute: " + currentMinute + " ; executionMinute: " +
                            executionMinute);
                    return;
                }
            }

            Log.d(this, "executing timer...");
            switch (timer.getExecutionType()) {
                case Timer.EXECUTION_TYPE_WEEKDAY:
                    WeekdayTimer weekdayTimer = (WeekdayTimer) timer;

                    if (weekdayTimer.containsExecutionDay(currentTime.get(Calendar.DAY_OF_WEEK))) {
                        ActionHandler.execute(context, timer);
                    } else {
                        Log.d(this, "timer executionDays doesn't contain current day, not executing timer");
                    }
                    break;
                case Timer.EXECUTION_TYPE_INTERVAL:
                    ActionHandler.execute(context, timer);
                    break;
                default:
                    Log.e(this, "Unknown Timer executionType: " + timer.getExecutionType());
                    break;
            }

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(context, e);
        }

        reinitializeAlarms(context);
    }

    private void reinitializeAlarms(Context context) {
        try {
            for (Timer timer : DatabaseHandler.getAllTimers(true)) {
                AlarmHandler.cancelAlarm(context, timer);
                AlarmHandler.createAlarm(context, timer);
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(context, e);
        }
    }
}

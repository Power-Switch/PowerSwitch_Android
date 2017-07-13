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

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

import javax.inject.Inject;

import dagger.android.DaggerBroadcastReceiver;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.shared.constants.TimerConstants;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;
import timber.log.Timber;

/**
 * IntentReceiver to handle any Alarm/Timer related Intents
 * This Class is responsible for restarting all Alarms on device boot
 * and also contains the logic whether a timer should be triggered or not.
 * <p/>
 * Created by Markus on 21.09.2015.
 */
public class TimerAlarmIntentReceiver extends DaggerBroadcastReceiver {

    @Inject
    ActionHandler actionHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Timber.d("Received intent: ", intent);

        try {
            if (intent.getAction()
                    .equals(TimerConstants.TIMER_ACTIVATION_INTENT)) {
                Timber.d("parsing timer activation intent...");
                parseActionIntent(context, intent);
            } else if (intent.getAction()
                    .equals("android.intent.action.BOOT_COMPLETED")) {
                // restart all active alarms because device rebooted
                Timber.d("restarting all active alarms because device rebooted...");
                reinitializeAlarms(context);
            } else {
                Timber.d("Received unknown intent: " + intent.getAction());
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void parseActionIntent(Context context, Intent intent) {
        try {
            long id = Long.valueOf(intent.getData()
                    .getHost());
            Timer timer = DatabaseHandler.getTimer(id);

            Calendar currentTime = Calendar.getInstance();
            if (Build.VERSION.SDK_INT < 19) {
                int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                int executionHour = timer.getExecutionTime()
                        .get(Calendar.HOUR_OF_DAY);
                if (currentHour != executionHour) {
                    Timber.d("Timer hour doesnt match: " + currentHour + " != " + timer.getExecutionTime()
                            .get(Calendar.HOUR_OF_DAY));
                    return;
                }

                int currentMinute = currentTime.get(Calendar.MINUTE);
                int executionMinute = timer.getExecutionTime()
                        .get(Calendar.MINUTE);
                if (!(currentMinute >= executionMinute && currentMinute <= executionMinute + 3)) {
                    Timber.d("Timer minute not in valid range: currentMinute: " + currentMinute + " ; executionMinute: " + executionMinute);
                    return;
                }
            }

            Timber.d("executing timer...");
            switch (timer.getExecutionType()) {
                case Timer.EXECUTION_TYPE_WEEKDAY:
                    WeekdayTimer weekdayTimer = (WeekdayTimer) timer;

                    if (weekdayTimer.containsExecutionDay(currentTime.get(Calendar.DAY_OF_WEEK))) {
                        actionHandler.execute(timer);
                    } else {
                        Timber.d("timer executionDays doesn't contain current day, not executing timer");
                    }
                    break;
                case Timer.EXECUTION_TYPE_INTERVAL:
                    actionHandler.execute(timer);
                    break;
                default:
                    Timber.e("Unknown Timer executionType: " + timer.getExecutionType());
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

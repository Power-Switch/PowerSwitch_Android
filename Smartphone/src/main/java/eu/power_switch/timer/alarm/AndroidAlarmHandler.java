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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.shared.constants.TimerConstants;
import eu.power_switch.timer.IntervalTimer;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;
import timber.log.Timber;

/**
 * Class to handle Android Alarms
 * <p/>
 * Created by Markus on 12.09.2015.
 */
@Singleton
public class AndroidAlarmHandler {

    private final Context context;

    @Inject
    public AndroidAlarmHandler(Context context) {
        this.context = context;
    }

    /**
     * Creates an Intent that will be sent when a Timer alarm goes off
     *
     * @param timer Timer this intent will activate
     *
     * @return Intent
     */
    private Intent createAlarmIntent(Timer timer) {
        Intent intent = new Intent(context, TimerAlarmIntentReceiver.class);
        intent.setAction(TimerConstants.TIMER_ACTIVATION_INTENT);
        intent.setData(Uri.parse(TimerConstants.TIMER_URI_SCHEME + "://" + timer.getId()));

        return intent;
    }

    /**
     * Creates a one time/repeating alarm
     *
     * @param timer Timer that this alarm will activate
     */
    public void createAlarm(Timer timer) {
        Timber.d("AlarmHandler", "activating alarm of timer: " + timer.getId());
        Intent        alarmIntent   = createAlarmIntent(timer);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Timber.d("AlarmHandler", "intent: " + alarmIntent);
        Timber.d("AlarmHandler",
                "exactTime: " + timer.getExecutionTime()
                        .getTime()
                        .toLocaleString());

        if (Timer.EXECUTION_TYPE_WEEKDAY.equals(timer.getExecutionType())) {
            createAlarm((WeekdayTimer) timer, pendingIntent);
        } else {
            createAlarm((IntervalTimer) timer, pendingIntent);
        }
    }

    private void createAlarm(WeekdayTimer timer, PendingIntent pendingIntent) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (timer.getExecutionInterval() == -1) {
            // one time alarm
            alarmMgr.set(AlarmManager.RTC_WAKEUP,
                    timer.getRandomizedExecutionTime()
                            .getTimeInMillis(),
                    pendingIntent);
        } else {
            // repeating alarm
            Calendar currentTime = Calendar.getInstance();

            Calendar randomizedExecutionTime = timer.getRandomizedExecutionTime();

            Calendar nextExecutionTime = Calendar.getInstance();
            nextExecutionTime.set(Calendar.HOUR_OF_DAY, randomizedExecutionTime.get(Calendar.HOUR_OF_DAY));
            nextExecutionTime.set(Calendar.MINUTE, randomizedExecutionTime.get(Calendar.MINUTE));
            nextExecutionTime.set(Calendar.SECOND, 0);
            nextExecutionTime.set(Calendar.MILLISECOND, 0);

            int i = 0;
            while (true) {
                if (!timer.containsExecutionDay(nextExecutionTime.get(Calendar.DAY_OF_WEEK)) || nextExecutionTime.before(currentTime)) {
                    nextExecutionTime.add(Calendar.DAY_OF_WEEK, 1);
                    i++;

                    if (i > 100) {
                        Timber.e("AlarmHandler", "Endlosschleife beim finden der nächsten Alarmzeit!");
                        return;
                    }
                } else {
                    break;
                }
            }

            Timber.d("AlarmHandler",
                    "next exactExecutionTime(incl. randomizer): " + nextExecutionTime.getTime()
                            .toLocaleString());

            if (Build.VERSION.SDK_INT >= 23) {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextExecutionTime.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, nextExecutionTime.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT < 19) {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, nextExecutionTime.getTimeInMillis(), pendingIntent);
            } else {
                Timber.e("AlarmHandler", "Unknown SDK Version!");
            }
        }
    }

    private void createAlarm(IntervalTimer timer, PendingIntent pendingIntent) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (timer.getExecutionInterval() == -1) {
            // one time alarm
            alarmMgr.set(AlarmManager.RTC_WAKEUP,
                    timer.getRandomizedExecutionTime()
                            .getTimeInMillis(),
                    pendingIntent);
        } else {
            // repeating alarm
            Calendar currentTime = Calendar.getInstance();

            Calendar randomizedExecutionTime = timer.getRandomizedExecutionTime();

            Calendar nextExecutionTime = Calendar.getInstance();
            nextExecutionTime.set(Calendar.HOUR_OF_DAY, randomizedExecutionTime.get(Calendar.HOUR_OF_DAY));
            nextExecutionTime.set(Calendar.MINUTE, randomizedExecutionTime.get(Calendar.MINUTE));
            nextExecutionTime.set(Calendar.SECOND, 0);
            nextExecutionTime.set(Calendar.MILLISECOND, 0);

            while (nextExecutionTime.before(currentTime)) {
                nextExecutionTime.add(Calendar.MILLISECOND, (int) timer.getExecutionInterval());
            }

            Timber.d("AlarmHandler",
                    "next exactExecutionTime(incl. randomizer): " + nextExecutionTime.getTime()
                            .toLocaleString());

            if (Build.VERSION.SDK_INT >= 23) {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextExecutionTime.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, nextExecutionTime.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT < 19) {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, nextExecutionTime.getTimeInMillis(), pendingIntent);
            } else {
                Timber.e("AlarmHandler", "Unknown SDK Version!");
            }
        }
    }

    /**
     * Cancels an existing Alarm
     *
     * @param timer Timer that this alarm would activate
     */
    public void cancelAlarm(Timer timer) {
        Timber.d("AlarmHandler", "cancelling alarm of timer: " + timer.getId());
        AlarmManager  alarmMgr      = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, createAlarmIntent(timer), PendingIntent.FLAG_UPDATE_CURRENT);

        Timber.d("AlarmHandler", "cancelling intent: " + createAlarmIntent(timer));
        Timber.d("AlarmHandler",
                "cancelling time: " + timer.getExecutionTime()
                        .getTime()
                        .toLocaleString());
        alarmMgr.cancel(pendingIntent);
    }
}
/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog.configuration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.List;

import javax.inject.Inject;

import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.holder.TimerConfigurationHolder;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage1Time;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage2Days;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage3Action;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage4TabbedSummary;
import eu.power_switch.timer.IntervalTimer;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;
import eu.power_switch.timer.alarm.AndroidAlarmHandler;
import timber.log.Timber;

/**
 * Dialog to create or modify a Timer
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureTimerDialog extends ConfigurationDialogTabbed<TimerConfigurationHolder> {

    @Inject
    AndroidAlarmHandler androidAlarmHandler;

    public static ConfigureTimerDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(null, targetFragment);
    }

    public static ConfigureTimerDialog newInstance(Timer timer, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureTimerDialog     fragment                 = new ConfigureTimerDialog();
        TimerConfigurationHolder timerConfigurationHolder = new TimerConfigurationHolder();
        if (timer != null) {
            timerConfigurationHolder.setTimer(timer);
        }
        fragment.setConfiguration(timerConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) throws Exception {
        Timer timer = getConfiguration().getTimer();

        if (timer != null) {
            getConfiguration().setTimer(timer);

            getConfiguration().setActive(timer.isActive());
            getConfiguration().setName(timer.getName());

            getConfiguration().setExecutionTime(timer.getExecutionTime());
            getConfiguration().setRandomizerValue(timer.getRandomizerValue());

            getConfiguration().setExecutionInterval(timer.getExecutionInterval());
            getConfiguration().setExecutionType(timer.getExecutionType());

            if (Timer.EXECUTION_TYPE_WEEKDAY.equals(timer.getExecutionType())) {
                WeekdayTimer weekdayTimer = (WeekdayTimer) timer;
                getConfiguration().setExecutionDays(weekdayTimer.getExecutionDays());
            } else {

            }

            getConfiguration().setActions(timer.getActions());
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_timer;
    }

    @Override
    protected void addPageEntries(List<PageEntry<TimerConfigurationHolder>> pageEntries) {
        pageEntries.add(new PageEntry<>(R.string.time, ConfigureTimerDialogPage1Time.class));
        pageEntries.add(new PageEntry<>(R.string.days, ConfigureTimerDialogPage2Days.class));
        pageEntries.add(new PageEntry<>(R.string.actions, ConfigureTimerDialogPage3Action.class));
        pageEntries.add(new PageEntry<>(R.string.summary, ConfigureTimerDialogPage4TabbedSummary.class));
    }

    @Override
    protected void saveConfiguration() throws Exception {
        Timber.d("Saving Timer...");

        Timer timer   = null;
        long  timerId = -1;
        if (getConfiguration().getTimer() != null) {
            timerId = getConfiguration().getTimer()
                    .getId();
        }
        switch (getConfiguration().getExecutionType()) {
            case Timer.EXECUTION_TYPE_INTERVAL:
                timer = new IntervalTimer(timerId,
                        getConfiguration().isActive(),
                        getConfiguration().getName(),
                        getConfiguration().getExecutionTime(),
                        getConfiguration().getRandomizerValue(),
                        getConfiguration().getExecutionInterval(),
                        getConfiguration().getActions());
                break;

            case Timer.EXECUTION_TYPE_WEEKDAY:
                timer = new WeekdayTimer(timerId,
                        getConfiguration().isActive(),
                        getConfiguration().getName(),
                        getConfiguration().getExecutionTime(),
                        getConfiguration().getRandomizerValue(),
                        getConfiguration().getExecutionDays(),
                        getConfiguration().getActions());
                break;
        }

        if (timer != null) {
            if (getConfiguration().getTimer() == null) {
                long newId = persistenceHandler.addTimer(timer);
                // update id (because the alarm is based on it's id)
                timer.setId(newId);
            } else {
                // cancel any existing alarm
                androidAlarmHandler.cancelAlarm(timer);

                // update in db
                persistenceHandler.updateTimer(timer);
            }

            if (timer.isActive()) {
                // activate alarm if necessary
                androidAlarmHandler.createAlarm(timer);
            }
        }

        TimersFragment.notifyTimersChanged();
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        androidAlarmHandler.cancelAlarm(getConfiguration().getTimer());

        persistenceHandler.deleteTimer(getConfiguration().getTimer()
                .getId());

        // notify scenes fragment
        TimersFragment.notifyTimersChanged();
    }

}

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

package eu.power_switch.gui.fragment.configure_timer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.configuration.holder.TimerConfigurationHolder;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.shared.action.Action;
import eu.power_switch.shared.event.ConfigurationChangedEvent;
import eu.power_switch.timer.IntervalTimer;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureTimerDialogPage4TabbedSummary extends ConfigurationDialogPage<TimerConfigurationHolder> implements ConfigurationDialogTabbedSummaryFragment {

    @BindView(R.id.textView_name)
    TextView textViewName;
    @BindView(R.id.textView_time)
    TextView textViewTime;
    @BindView(R.id.textView_execution_days)
    TextView textViewDays;
    @BindView(R.id.textView_action)
    TextView textViewAction;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        checkSetupValidity();
        updateUi();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_timer_page_4_summary;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onConfigurationChanged(ConfigurationChangedEvent e) {
        updateUi();
    }

    private void updateUi() {
        updateUiValues();
//        updateUiVisibility();
    }

    private void updateUiValues() {
        if (getConfiguration().getName() != null) {
            textViewName.setText(getConfiguration().getName());
        }

        if (getConfiguration().getExecutionTime() != null) {
            long hourOfDay = getConfiguration().getExecutionTime()
                    .get(Calendar.HOUR_OF_DAY);
            long minute = getConfiguration().getExecutionTime()
                    .get(Calendar.MINUTE);

            String executionTime = "";
            if (hourOfDay < 10) {
                executionTime += "0";
            }
            executionTime += hourOfDay + ":";
            if (minute < 10) {
                executionTime += "0";
            }
            executionTime += minute;

            executionTime += " " + getString(R.string.plus_minus_minutes, getConfiguration().getRandomizerValue());

            textViewTime.setText(executionTime);
        } else {
            textViewTime.setText("");
        }

        String executionDaysText = "";
        if (getConfiguration().getExecutionDays() != null) {

            boolean first = true;
            for (WeekdayTimer.Day day : getConfiguration().getExecutionDays()) {
                if (first) {
                    first = false;
                } else {
                    executionDaysText += ", ";
                }
                executionDaysText += WeekdayTimer.Day.getWeekdayName(getContext(), day);
            }
        }
        textViewDays.setText(executionDaysText);

        String actionText = "";
        if (getConfiguration().getActions() != null) {
            for (Action action : getConfiguration().getActions()) {
                actionText += action.toString() + "\n";
            }
        }
        textViewAction.setText(actionText);
    }

    @Override
    public void saveCurrentConfigurationToDatabase() {
        try {
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
                    DatabaseHandler.addTimer(timer);
                } else {
                    DatabaseHandler.updateTimer(timer);
                }
            }

            TimersFragment.notifyTimersChanged();
            StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.timer_saved, Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    @Override
    public boolean checkSetupValidity() {
        return true;
    }

}

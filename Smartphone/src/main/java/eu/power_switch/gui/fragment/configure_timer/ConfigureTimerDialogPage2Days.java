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

package eu.power_switch.gui.fragment.configure_timer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.TimerConfigurationHolder;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureTimerDialogPage2Days extends ConfigurationDialogPage<TimerConfigurationHolder> {

    @BindView(R.id.toggleButton_monday)
    ToggleButton toggleButtonMonday;
    @BindView(R.id.toggleButton_tuesday)
    ToggleButton toggleButtonTuesday;
    @BindView(R.id.toggleButton_wednesday)
    ToggleButton toggleButtonWednesday;
    @BindView(R.id.toggleButton_thursday)
    ToggleButton toggleButtonThursday;
    @BindView(R.id.toggleButton_friday)
    ToggleButton toggleButtonFriday;
    @BindView(R.id.toggleButton_saturday)
    ToggleButton toggleButtonSaturday;
    @BindView(R.id.toggleButton_sunday)
    ToggleButton toggleButtonSunday;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        initializeTimerData();

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateConfiguration(1000, getSelectedDays(), Timer.EXECUTION_TYPE_WEEKDAY);
            }
        };

        toggleButtonMonday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonTuesday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonWednesday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonThursday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonFriday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonSaturday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonSunday.setOnCheckedChangeListener(onCheckedChangeListener);

        return rootView;
    }

    /**
     * Used to notify the summary page that some info has changed
     *
     * @param executionInterval time in milliseconds
     * @param executionDays     list of days
     * @param executionType     Timer Type
     */
    public void updateConfiguration(long executionInterval, List<WeekdayTimer.Day> executionDays, String executionType) {
        getConfiguration().setExecutionInterval(executionInterval);
        getConfiguration().setExecutionDays(executionDays);
        getConfiguration().setExecutionType(executionType);

        notifyConfigurationChanged();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_timer_page_2;
    }

    private void initializeTimerData() {
        Timer timer = getConfiguration().getTimer();

        if (timer != null) {
            try {

//        currentExecutionInterval = timer.getExecutionInterval();

                if (Timer.EXECUTION_TYPE_WEEKDAY.equals(timer.getExecutionType())) {
                    WeekdayTimer weekdayTimer = (WeekdayTimer) timer;

                    for (WeekdayTimer.Day day : weekdayTimer.getExecutionDays()) {
                        switch (day) {
                            case MONDAY:
                                toggleButtonMonday.setChecked(true);
                                break;
                            case TUESDAY:
                                toggleButtonTuesday.setChecked(true);
                                break;
                            case WEDNESDAY:
                                toggleButtonWednesday.setChecked(true);
                                break;
                            case THURSDAY:
                                toggleButtonThursday.setChecked(true);
                                break;
                            case FRIDAY:
                                toggleButtonFriday.setChecked(true);
                                break;
                            case SATURDAY:
                                toggleButtonSaturday.setChecked(true);
                                break;
                            case SUNDAY:
                                toggleButtonSunday.setChecked(true);
                                break;
                        }
                    }
                } else if (Timer.EXECUTION_TYPE_INTERVAL.equals(timer.getExecutionType())) {

                }
            } catch (Exception e) {
                statusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    private List<WeekdayTimer.Day> getSelectedDays() {
        List<WeekdayTimer.Day> selectedDays = new ArrayList<>();

        if (toggleButtonMonday.isChecked()) {
            selectedDays.add(WeekdayTimer.Day.MONDAY);
        }
        if (toggleButtonTuesday.isChecked()) {
            selectedDays.add(WeekdayTimer.Day.TUESDAY);
        }
        if (toggleButtonWednesday.isChecked()) {
            selectedDays.add(WeekdayTimer.Day.WEDNESDAY);
        }
        if (toggleButtonThursday.isChecked()) {
            selectedDays.add(WeekdayTimer.Day.THURSDAY);
        }
        if (toggleButtonFriday.isChecked()) {
            selectedDays.add(WeekdayTimer.Day.FRIDAY);
        }
        if (toggleButtonSaturday.isChecked()) {
            selectedDays.add(WeekdayTimer.Day.SATURDAY);
        }
        if (toggleButtonSunday.isChecked()) {
            selectedDays.add(WeekdayTimer.Day.SUNDAY);
        }

        return selectedDays;
    }

}

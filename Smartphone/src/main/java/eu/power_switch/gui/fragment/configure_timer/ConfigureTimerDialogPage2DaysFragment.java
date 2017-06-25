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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigureTimerDialog;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureTimerDialogPage2DaysFragment extends ConfigurationDialogFragment {

    public static final String KEY_EXECUTION_INTERVAL = "executionInterval";
    public static final String KEY_EXECUTION_DAYS = "executionDays";
    public static final String KEY_EXECUTION_TYPE = "executionType";

    private View rootView;
    private ToggleButton toggleButtonMonday;
    private ToggleButton toggleButtonTuesday;
    private ToggleButton toggleButtonWednesday;
    private ToggleButton toggleButtonThursday;
    private ToggleButton toggleButtonFriday;
    private ToggleButton toggleButtonSaturday;
    private ToggleButton toggleButtonSunday;

    /**
     * Used to notify the summary page that some info has changed
     *
     * @param context           any suitable context
     * @param executionInterval time in milliseconds
     * @param executionDays     list of days
     * @param executionType     Timer Type
     */
    public static void sendTimerExecutionIntervalChangedBroadcast(Context context, long executionInterval,
                                                                  ArrayList<WeekdayTimer.Day> executionDays, String
                                                                           executionType) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_TIMER_EXECUTION_INTERVAL_CHANGED);
        intent.putExtra(KEY_EXECUTION_INTERVAL, executionInterval);
        intent.putExtra(KEY_EXECUTION_DAYS, executionDays);
        intent.putExtra(KEY_EXECUTION_TYPE, executionType);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_timer_page_2, container, false);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendTimerExecutionIntervalChangedBroadcast(getContext(), 1000, getSelectedDays(), Timer
                        .EXECUTION_TYPE_WEEKDAY);
            }
        };

        toggleButtonMonday = rootView.findViewById(R.id.toggleButton_monday);
        toggleButtonMonday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonTuesday = rootView.findViewById(R.id.toggleButton_tuesday);
        toggleButtonTuesday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonWednesday = rootView.findViewById(R.id.toggleButton_wednesday);
        toggleButtonWednesday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonThursday = rootView.findViewById(R.id.toggleButton_thursday);
        toggleButtonThursday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonFriday = rootView.findViewById(R.id.toggleButton_friday);
        toggleButtonFriday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonSaturday = rootView.findViewById(R.id.toggleButton_saturday);
        toggleButtonSaturday.setOnCheckedChangeListener(onCheckedChangeListener);
        toggleButtonSunday = rootView.findViewById(R.id.toggleButton_sunday);
        toggleButtonSunday.setOnCheckedChangeListener(onCheckedChangeListener);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureTimerDialog.TIMER_ID_KEY)) {
            long timerId = args.getLong(ConfigureTimerDialog.TIMER_ID_KEY);
            initializeTimerData(timerId);
        }

        return rootView;
    }

    private void initializeTimerData(long timerId) {
        try {
            Timer timer = DatabaseHandler.getTimer(timerId);

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
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    private ArrayList<WeekdayTimer.Day> getSelectedDays() {
        ArrayList<WeekdayTimer.Day> selectedDays = new ArrayList<>();

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

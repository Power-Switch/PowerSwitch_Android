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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureTimerDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.timer.IntervalTimer;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureTimerDialogPage4TabbedSummaryFragment extends Fragment implements ConfigurationDialogTabbedSummaryFragment {

    private long currentId = -1;
    private boolean currentIsActive;
    private String currentName = "";
    private Calendar currentExecutionTime = Calendar.getInstance();
    private long currentExecutionInterval = -1;
    private ArrayList<WeekdayTimer.Day> currentExecutionDays;
    private String currentExecutionType;
    private ArrayList<Action> currentActions;

    private BroadcastReceiver broadcastReceiver;
    private View rootView;
    private TextView textViewName;
    private TextView textViewTime;
    private TextView textViewDays;
    private TextView textViewAction;

    /**
     * Used to notify parent Dialog that configuration has changed
     *
     * @param context any suitable context
     */
    public static void sendSummaryChangedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_TIMER_SUMMARY_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_timer_page_4_summary, container, false);

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocalBroadcastConstants.INTENT_TIMER_NAME_EXECUTION_TIME_CHANGED.equals(intent.getAction())) {
                    currentExecutionTime = (Calendar) intent.getSerializableExtra("executionTime");
                    currentName = intent.getStringExtra("name");

                } else if (LocalBroadcastConstants.INTENT_TIMER_EXECUTION_INTERVAL_CHANGED.equals(intent.getAction())) {
                    currentExecutionInterval = intent.getLongExtra("executionInterval", -1);
                    currentExecutionDays = (ArrayList<WeekdayTimer.Day>) intent.getSerializableExtra("executionDays");
                    currentExecutionType = intent.getStringExtra("executionType");

                } else if (LocalBroadcastConstants.INTENT_TIMER_ACTIONS_CHANGED.equals(intent.getAction())) {
                    currentActions = (ArrayList<Action>) intent.getSerializableExtra("actions");
                }

                updateUi();

                sendSummaryChangedBroadcast(getContext());
            }
        };

        textViewName = (TextView) rootView.findViewById(R.id.textView_name);
        textViewTime = (TextView) rootView.findViewById(R.id.textView_time);
        textViewDays = (TextView) rootView.findViewById(R.id.textView_execution_days);
        textViewAction = (TextView) rootView.findViewById(R.id.textView_action);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureTimerDialog.TIMER_ID_KEY)) {
            currentId = args.getLong(ConfigureTimerDialog.TIMER_ID_KEY);
            initializeTimerData(currentId);
        }

        checkSetupValidity();
        updateUi();

        return rootView;
    }

    private void initializeTimerData(long timerId) {
        try {
            final Timer timer = DatabaseHandler.getTimer(timerId);

            currentId = timerId;
            currentIsActive = timer.isActive();
            currentName = timer.getName();
            currentActions = timer.getActions();
            currentExecutionType = timer.getExecutionType();
            currentExecutionTime = timer.getExecutionTime();
            currentExecutionInterval = timer.getExecutionInterval();

            if (Timer.EXECUTION_TYPE_WEEKDAY.equals(timer.getExecutionType())) {
                WeekdayTimer weekdayTimer = (WeekdayTimer) timer;
                currentExecutionDays = weekdayTimer.getExecutionDays();
            }

        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }

        updateUi();
    }

    private void updateUi() {
        updateUiValues();
//        updateUiVisibility();
    }

    private void updateUiValues() {
        if (currentName != null) {
            textViewName.setText(currentName);
        }

        if (currentExecutionTime != null) {
            long hourOfDay = currentExecutionTime.get(Calendar.HOUR_OF_DAY);
            long minute = currentExecutionTime.get(Calendar.MINUTE);

            String executionTime = "";
            if (hourOfDay < 10) {
                executionTime += "0";
            }
            executionTime += hourOfDay + ":";
            if (minute < 10) {
                executionTime += "0";
            }
            executionTime += minute;

            textViewTime.setText(executionTime);
        } else {
            textViewTime.setText("");
        }

        String executionDaysText = "";
        if (currentExecutionDays != null) {

            boolean first = true;
            for (WeekdayTimer.Day day : currentExecutionDays) {
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
        if (currentActions != null) {
            for (Action action : currentActions) {
                actionText += action.toString() + "\n";
            }
        }
        textViewAction.setText(actionText);
    }

    @Override
    public void saveCurrentConfigurationToDatabase() {
        try {
            if (currentId == -1) {
                if (Timer.EXECUTION_TYPE_INTERVAL.equals(currentExecutionType)) {
                    Timer timer = new IntervalTimer(0, true, currentName, currentExecutionTime, currentExecutionInterval,
                            currentActions);
                    DatabaseHandler.addTimer(timer);
                } else if (Timer.EXECUTION_TYPE_WEEKDAY.equals(currentExecutionType)) {
                    Timer timer = new WeekdayTimer(0, true, currentName, currentExecutionTime, currentExecutionDays,
                            currentActions);
                    DatabaseHandler.addTimer(timer);
                }
            } else {
                if (Timer.EXECUTION_TYPE_INTERVAL.equals(currentExecutionType)) {
                    Timer timer = new IntervalTimer(currentId, currentIsActive, currentName, currentExecutionTime, currentExecutionInterval,
                            currentActions);
                    DatabaseHandler.updateTimer(timer);
                } else if (Timer.EXECUTION_TYPE_WEEKDAY.equals(currentExecutionType)) {
                    Timer timer = new WeekdayTimer(currentId, currentIsActive, currentName, currentExecutionTime, currentExecutionDays,
                            currentActions);
                    DatabaseHandler.updateTimer(timer);
                }
            }

            TimersFragment.sendTimersChangedBroadcast(getContext());
            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                    R.string.timer_saved, Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    @Override
    public boolean checkSetupValidity() {
        if (currentName == null || currentName.length() <= 0) {
            return false;
        }

        if (currentExecutionTime == null) {
            return false;
        }

        if (currentExecutionType == null) {
            return false;
        } else if (Timer.EXECUTION_TYPE_INTERVAL.equals(currentExecutionType)) {
            if (currentExecutionInterval == -1) {
                return false;
            }
        } else if (Timer.EXECUTION_TYPE_WEEKDAY.equals(currentExecutionType)) {
            if (currentExecutionDays == null || currentExecutionDays.isEmpty()) {
                return false;
            }
        }

        return !(currentActions == null || currentActions.isEmpty());
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_TIMER_NAME_EXECUTION_TIME_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_TIMER_EXECUTION_INTERVAL_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_TIMER_ACTIONS_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

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
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigureTimerDialog;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.timer.Timer;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureTimerDialogPage1TimeFragment extends ConfigurationDialogFragment {

    public static final String KEY_NAME = "name";
    public static final String KEY_EXECUTION_TIME = "executionTime";
    public static final String KEY_RANDOMIZER_VALUE = "randomizerValue";

    private TextInputLayout floatingName;
    private EditText name;
    private TimePicker timePicker;
    private TextView textViewRandomizer;
    private SeekBar seekBarRandomizer;

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context  any suitable context
     * @param calendar The calendar when this timer activates
     */
    public static void sendTimerNameExecutionTimeChangedBroadcast(Context context, String name, Calendar calendar, int randomizerValue) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_TIMER_NAME_EXECUTION_TIME_CHANGED);
        intent.putExtra(KEY_NAME, name);
        intent.putExtra(KEY_EXECUTION_TIME, calendar);
        intent.putExtra(KEY_RANDOMIZER_VALUE, randomizerValue);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_fragment_configure_timer_page_1, container, false);

        floatingName = (TextInputLayout) rootView.findViewById(R.id.timer_name_text_input_layout);
        name = (EditText) rootView.findViewById(R.id.editText_timer_name);
        name.requestFocus();
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidity();
                sendTimerNameExecutionTimeChangedBroadcast(getContext(), getCurrentName(), getCurrentTime(), getCurrentRandomizerValue());
            }
        });

        timePicker = (TimePicker) rootView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                Log.d(ConfigureTimerDialogPage1TimeFragment.class, "Time set to: " + hourOfDay + ":" + minute);
                sendTimerNameExecutionTimeChangedBroadcast(getContext(), getCurrentName(), c, getCurrentRandomizerValue());
            }
        });

        textViewRandomizer = (TextView) rootView.findViewById(R.id.textViewRandomizer);

        seekBarRandomizer = (SeekBar) rootView.findViewById(R.id.seekbarRandomizer);
        seekBarRandomizer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateRandomizerValue(seekBar.getProgress());
                sendTimerNameExecutionTimeChangedBroadcast(getContext(), getCurrentName(), getCurrentTime(), getCurrentRandomizerValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        updateRandomizerValue(0);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureTimerDialog.TIMER_ID_KEY)) {
            long timerId = args.getLong(ConfigureTimerDialog.TIMER_ID_KEY);
            initializeTimerData(timerId);
        }

        checkValidity();

        return rootView;
    }

    private void initializeTimerData(long timerId) {
        try {
            Timer timer = DatabaseHandler.getTimer(timerId);

            name.setText(timer.getName());

            Calendar c = timer.getExecutionTime();
            timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(c.get(Calendar.MINUTE));

            updateRandomizerValue(timer.getRandomizerValue());
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    private String getCurrentName() {
        return name.getText().toString().trim();
    }

    private Calendar getCurrentTime() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        c.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private int getCurrentRandomizerValue() {
        return seekBarRandomizer.getProgress();
    }

    private boolean checkValidity() {
        String currentReceiverName = getCurrentName();

        if (currentReceiverName.length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            floatingName.setErrorEnabled(true);
            return false;
        }

        floatingName.setError(null);
        floatingName.setErrorEnabled(false);
        return true;
    }

    private void updateRandomizerValue(int progress) {
        textViewRandomizer.setText(getString(R.string.plus_minus_minutes, progress));
        seekBarRandomizer.setProgress(progress);
    }

}

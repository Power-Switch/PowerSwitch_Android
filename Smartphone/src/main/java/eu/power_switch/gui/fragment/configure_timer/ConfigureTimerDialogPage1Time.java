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
import android.support.design.widget.TextInputLayout;
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

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.TimerConfigurationHolder;
import eu.power_switch.timer.Timer;
import timber.log.Timber;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureTimerDialogPage1Time extends ConfigurationDialogPage<TimerConfigurationHolder> {

    @BindView(R.id.timer_name_text_input_layout)
    TextInputLayout floatingName;
    @BindView(R.id.editText_timer_name)
    EditText        name;
    @BindView(R.id.timePicker)
    TimePicker      timePicker;
    @BindView(R.id.textViewRandomizer)
    TextView        textViewRandomizer;
    @BindView(R.id.seekbarRandomizer)
    SeekBar         seekBarRandomizer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        initializeTimerData();

        updateUi();

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
                updateConfiguration(getCurrentName(), getCurrentTime(), getCurrentRandomizerValue());
            }
        });

        timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                Timber.d("Time set to: " + hourOfDay + ":" + minute);
                updateConfiguration(getCurrentName(), c, getCurrentRandomizerValue());
            }
        });

        seekBarRandomizer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateRandomizerValue(seekBar.getProgress());
                updateConfiguration(getCurrentName(), getCurrentTime(), getCurrentRandomizerValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        checkValidity();

        return rootView;
    }

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param calendar The calendar when this timer activates
     */
    public void updateConfiguration(String name, Calendar calendar, int randomizerValue) {
        getConfiguration().setName(name);
        getConfiguration().setExecutionTime(calendar);
        getConfiguration().setRandomizerValue(randomizerValue);

        notifyConfigurationChanged();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_timer_page_1;
    }


    private void initializeTimerData() {
        Timer timer = getConfiguration().getTimer();
        if (timer != null) {
            try {
                name.setText(getConfiguration().getName());

                Calendar c = getConfiguration().getExecutionTime();
                timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(c.get(Calendar.MINUTE));

            } catch (Exception e) {
                statusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    private void updateUi() {
        updateRandomizerValue(getConfiguration().getRandomizerValue());
    }

    private String getCurrentName() {
        return name.getText()
                .toString()
                .trim();
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
            return false;
        }

        floatingName.setError(null);
        return true;
    }

    private void updateRandomizerValue(int progress) {
        textViewRandomizer.setText(getString(R.string.plus_minus_minutes, progress));
        seekBarRandomizer.setProgress(progress);
    }

}

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

package eu.power_switch.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

import eu.power_switch.R;

/**
 * Created by Markus on 31.07.2016.
 */
public class SliderPreferenceFragmentCompat extends PreferenceDialogFragmentCompat {

    TextView textView;
    SeekBar seekBar;

    public SliderPreferenceFragmentCompat() {
    }

    public static SliderPreferenceFragmentCompat newInstance(String key) {
        SliderPreferenceFragmentCompat fragment = new SliderPreferenceFragmentCompat();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected View onCreateDialogView(Context context) {
        LayoutInflater li = LayoutInflater.from(context);
        View contentView = li.inflate(R.layout.dialog_slider_preference, null);

        return contentView;
        // super.onCreateDialogView(context);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SliderPreference sliderPreference = getSliderPreference();

        textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(String.format(Locale.getDefault(), "%d ms", sliderPreference.getProgress()));

        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        seekBar.setMax(1000);
        seekBar.setProgress(getSliderPreference().getProgress());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView.setText(String.format(Locale.getDefault(), "%d ms", seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        SliderPreference sliderPreference = getSliderPreference();

        if (positiveResult) {
            if (sliderPreference.isPersistent()) {
                sliderPreference.persistInt(seekBar.getProgress());
            }
            sliderPreference.setSummary(String.format(Locale.getDefault(), "%d ms", seekBar.getProgress()));
        }
    }

    private SliderPreference getSliderPreference() {
        return (SliderPreference) getPreference();
    }

}

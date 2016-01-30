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

package eu.power_switch.gui.fragment.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import eu.power_switch.R;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.settings.WearablePreferencesHandler;
import eu.power_switch.wear.service.UtilityService;

/**
 * Fragment containing all settings related to Wearable companion app
 * <p/>
 * Created by Markus on 30.08.2015.
 */
public class WearableSettingsFragment extends Fragment {

    private View rootView;

    private CheckBox autoCollapseRooms;
    private LinearLayout vibrationDurationLayout;
    private CheckBox vibrateOnButtonPress;
    private EditText vibrationDuration;
    private CheckBox highlightLastActivatedButton;

    private RadioButton radioButtonDarkBlue;
    private RadioButton radioButtonLightBlue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_wear_settings, container, false);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (buttonView.getId()) {
                    case R.id.checkBox_autoCollapseRooms:
                        WearablePreferencesHandler.setAutoCollapseRooms(isChecked);
                        break;
                    case R.id.checkBox_vibrateOnButtonPress:
                        WearablePreferencesHandler.setVibrateOnButtonPress(isChecked);
                        if (isChecked) {
                            vibrationDurationLayout.setVisibility(View.VISIBLE);
                        } else {
                            vibrationDurationLayout.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.checkBox_highlightLastActivatedButton:
                        WearablePreferencesHandler.setHighlightLastActivatedButton(isChecked);
                        break;
                    default:
                        break;
                }

                // sync settings with wearable app
                UtilityService.forceWearSettingsUpdate(getContext());
            }
        };

        autoCollapseRooms = (CheckBox) rootView.findViewById(R.id.checkBox_autoCollapseRooms);
        autoCollapseRooms.setOnCheckedChangeListener(onCheckedChangeListener);

        highlightLastActivatedButton = (CheckBox) rootView.findViewById(R.id.checkBox_highlightLastActivatedButton);
        highlightLastActivatedButton.setOnCheckedChangeListener(onCheckedChangeListener);

        vibrateOnButtonPress = (CheckBox) rootView.findViewById(R.id.checkBox_vibrateOnButtonPress);
        vibrateOnButtonPress.setOnCheckedChangeListener(onCheckedChangeListener);

        vibrationDurationLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout_vibrationDuration);
        vibrationDuration = (EditText) rootView.findViewById(R.id.editText_vibrationDuration);
        vibrationDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    WearablePreferencesHandler.setVibrationDuration(Integer.valueOf(s.toString()));
                    UtilityService.forceWearSettingsUpdate(getContext());
                }
            }
        });


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.radioButton_darkBlue:
                        WearablePreferencesHandler.setTheme(SettingsConstants.THEME_DARK_BLUE);
                        radioButtonLightBlue.setChecked(false);
                        break;
                    case R.id.radioButton_lightBlue:
                        WearablePreferencesHandler.setTheme(SettingsConstants.THEME_LIGHT_BLUE);
                        radioButtonDarkBlue.setChecked(false);
                        break;
                    default:
                        break;
                }

                UtilityService.forceWearSettingsUpdate(getContext());

                // TODO: restart wear app
            }
        };

        radioButtonDarkBlue = (RadioButton) rootView.findViewById(R.id.radioButton_darkBlue);
        radioButtonDarkBlue.setOnClickListener(onClickListener);

        radioButtonLightBlue = (RadioButton) rootView.findViewById(R.id.radioButton_lightBlue);
        radioButtonLightBlue.setOnClickListener(onClickListener);

        return rootView;
    }

    private void updateUI() {
        autoCollapseRooms.setChecked(WearablePreferencesHandler.getAutoCollapseRooms());
        highlightLastActivatedButton.setChecked(WearablePreferencesHandler.getHighlightLastActivatedButton());
        vibrateOnButtonPress.setChecked(WearablePreferencesHandler.getVibrateOnButtonPress());
        vibrationDuration.setText(String.valueOf(WearablePreferencesHandler.getVibrationDuration()));
        if (!WearablePreferencesHandler.getVibrateOnButtonPress()) {
            vibrationDurationLayout.setVisibility(View.GONE);
        } else {
            vibrationDurationLayout.setVisibility(View.VISIBLE);
        }

        switch (WearablePreferencesHandler.getTheme()) {
            case SettingsConstants.THEME_DARK_BLUE:
                radioButtonDarkBlue.setChecked(true);
                break;
            case SettingsConstants.THEME_DARK_RED:
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                radioButtonLightBlue.setChecked(true);
                break;
            case SettingsConstants.THEME_LIGHT_RED:
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}

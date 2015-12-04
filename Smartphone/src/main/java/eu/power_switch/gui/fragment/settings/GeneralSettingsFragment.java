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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.DeveloperOptionsDialog;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.widget.activity.ConfigureReceiverWidgetActivity;

/**
 * Fragment containing all settings related to Smartphone app
 * <p/>
 * Created by Markus on 30.08.2015.
 */
public class GeneralSettingsFragment extends Fragment {

    private View rootView;

    private AppCompatCheckBox autoDiscover;
    private AppCompatCheckBox autoCollapseRooms;
    private AppCompatCheckBox autoCollapseTimers;
    private AppCompatCheckBox showRoomAllOnOffButtons;
    private AppCompatCheckBox hideAddFAB;
    private AppCompatCheckBox highlightLastActivatedButton;

    private LinearLayout vibrationDurationLayout;
    private AppCompatCheckBox vibrateOnButtonPress;
    private AppCompatEditText vibrationDuration;

    private RadioButton radioButtonDarkBlue;
    private RadioButton radioButtonLightBlue;

    private int devMenuClickCounter = 0;
    private Calendar devMenuFirstClickTime;
    private AppCompatSpinner startupDefaultTab;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                switch (buttonView.getId()) {
                    case R.id.checkBox_autoDiscover:
                        SmartphonePreferencesHandler.setAutoDiscover(isChecked);
                        break;
                    case R.id.checkBox_autoCollapseRooms:
                        SmartphonePreferencesHandler.setAutoCollapseRooms(isChecked);
                        break;
                    case R.id.checkBox_autoCollapseTimers:
                        SmartphonePreferencesHandler.setAutoCollapseTimers(isChecked);
                        break;
                    case R.id.checkBox_showRoomAllOnOffButtons:
                        SmartphonePreferencesHandler.setShowRoomAllOnOff(isChecked);
                        break;
                    case R.id.checkBox_hideAddFAB:
                        SmartphonePreferencesHandler.setHideAddFAB(isChecked);
                        break;
                    case R.id.checkBox_vibrateOnButtonPress:
                        SmartphonePreferencesHandler.setVibrateOnButtonPress(isChecked);
                        if (isChecked) {
                            vibrationDurationLayout.setVisibility(View.VISIBLE);
                        } else {
                            vibrationDurationLayout.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.checkBox_highlightLastActivatedButton:
                        SmartphonePreferencesHandler.setHighlightLastActivatedButton(isChecked);
                        // force receiver widget update
                        ConfigureReceiverWidgetActivity.forceWidgetUpdate(getContext());
                        break;
                    default:
                        break;
                }
            }
        };

        // setup hidden developer menu
        AppCompatTextView generalSettingsTextView = (AppCompatTextView) rootView.findViewById(R.id.TextView_generalSettings);
        generalSettingsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                if (devMenuFirstClickTime != null) {
                    Calendar latestTime = Calendar.getInstance();
                    latestTime.setTime(devMenuFirstClickTime.getTime());
                    latestTime.add(Calendar.SECOND, 5);
                    if (currentTime.after(latestTime)) {
                        devMenuClickCounter = 0;
                    }
                }

                devMenuClickCounter++;
                if (devMenuClickCounter == 1) {
                    devMenuFirstClickTime = currentTime;
                }
                if (devMenuClickCounter >= 5) {
                    devMenuClickCounter = 0;

                    DeveloperOptionsDialog developerOptionsDialog = new DeveloperOptionsDialog();
                    developerOptionsDialog.show(getActivity().getSupportFragmentManager(), null);
                }
            }
        });


        startupDefaultTab = (AppCompatSpinner) rootView.findViewById(R.id.spinner_startupDefaultTab);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.main_tab_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startupDefaultTab.setAdapter(adapter);
        startupDefaultTab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SmartphonePreferencesHandler.setStartupDefaultTab(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        autoDiscover = (AppCompatCheckBox) rootView.findViewById(R.id.checkBox_autoDiscover);
        autoDiscover.setOnCheckedChangeListener(onCheckedChangeListener);

        autoCollapseRooms = (AppCompatCheckBox) rootView.findViewById(R.id.checkBox_autoCollapseRooms);
        autoCollapseRooms.setOnCheckedChangeListener(onCheckedChangeListener);

        autoCollapseTimers = (AppCompatCheckBox) rootView.findViewById(R.id.checkBox_autoCollapseTimers);
        autoCollapseTimers.setOnCheckedChangeListener(onCheckedChangeListener);

        showRoomAllOnOffButtons = (AppCompatCheckBox) rootView.findViewById(R.id.checkBox_showRoomAllOnOffButtons);
        showRoomAllOnOffButtons.setOnCheckedChangeListener(onCheckedChangeListener);

        hideAddFAB = (AppCompatCheckBox) rootView.findViewById(R.id.checkBox_hideAddFAB);
        hideAddFAB.setOnCheckedChangeListener(onCheckedChangeListener);

        highlightLastActivatedButton = (AppCompatCheckBox) rootView.findViewById(R.id.checkBox_highlightLastActivatedButton);
        highlightLastActivatedButton.setOnCheckedChangeListener(onCheckedChangeListener);

        vibrateOnButtonPress = (AppCompatCheckBox) rootView.findViewById(R.id.checkBox_vibrateOnButtonPress);
        vibrateOnButtonPress.setOnCheckedChangeListener(onCheckedChangeListener);

        vibrationDurationLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout_vibrationDuration);
        vibrationDuration = (AppCompatEditText) rootView.findViewById(R.id.editText_vibrationDuration);
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
                    SmartphonePreferencesHandler.setVibrationDuration(Integer.valueOf(s.toString()));
                }
            }
        });


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.radioButton_darkBlue:
                        SmartphonePreferencesHandler.setTheme(SettingsConstants.THEME_DARK_BLUE);
                        break;
                    case R.id.radioButton_lightBlue:
                        SmartphonePreferencesHandler.setTheme(SettingsConstants.THEME_LIGHT_BLUE);
                        break;
                    default:
                        break;
                }

                getActivity().finish();
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };

        radioButtonDarkBlue = (RadioButton) rootView.findViewById(R.id.radioButton_darkBlue);
        radioButtonDarkBlue.setOnClickListener(onClickListener);

        radioButtonLightBlue = (RadioButton) rootView.findViewById(R.id.radioButton_lightBlue);
        radioButtonLightBlue.setOnClickListener(onClickListener);

        return rootView;
    }

    private void updateUI() {
        startupDefaultTab.setSelection(SmartphonePreferencesHandler.getStartupDefaultTab());
        autoDiscover.setChecked(SmartphonePreferencesHandler.getAutoDiscover());
        autoCollapseRooms.setChecked(SmartphonePreferencesHandler.getAutoCollapseRooms());
        autoCollapseTimers.setChecked(SmartphonePreferencesHandler.getAutoCollapseTimers());
        showRoomAllOnOffButtons.setChecked(SmartphonePreferencesHandler.getShowRoomAllOnOff());
        hideAddFAB.setChecked(SmartphonePreferencesHandler.getHideAddFAB());
        highlightLastActivatedButton.setChecked(SmartphonePreferencesHandler.getHighlightLastActivatedButton());
        vibrateOnButtonPress.setChecked(SmartphonePreferencesHandler.getVibrateOnButtonPress());
        vibrationDuration.setText(String.format("%d", SmartphonePreferencesHandler.getVibrationDuration()));
        if (!SmartphonePreferencesHandler.getVibrateOnButtonPress()) {
            vibrationDurationLayout.setVisibility(View.GONE);
        } else {
            vibrationDurationLayout.setVisibility(View.VISIBLE);
        }

        switch (SmartphonePreferencesHandler.getTheme()) {
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

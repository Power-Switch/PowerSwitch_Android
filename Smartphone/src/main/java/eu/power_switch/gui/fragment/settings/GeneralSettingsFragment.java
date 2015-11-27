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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.DeveloperOptionsDialog;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.widget.activity.ConfigureReceiverWidgetActivity;

/**
 * Fragment containing all settings related to Smartphone app
 * <p/>
 * Created by Markus on 30.08.2015.
 */
public class GeneralSettingsFragment extends Fragment {

    private View rootView;

    private SharedPreferencesHandler sharedPreferencesHandler;

    private CheckBox autoDiscover;
    private CheckBox autoCollapseRooms;
    private CheckBox autoCollapseTimers;
    private CheckBox showRoomAllOnOffButtons;
    private CheckBox hideAddFAB;
    private CheckBox highlightLastActivatedButton;

    private LinearLayout vibrationDurationLayout;
    private CheckBox vibrateOnButtonPress;
    private EditText vibrationDuration;

    private RadioButton radioButtonDarkBlue;
    private RadioButton radioButtonLightBlue;

    private int devMenuClickCounter = 0;
    private Calendar devMenuFirstClickTime;
    private Spinner startupDefaultTab;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                switch (buttonView.getId()) {
                    case R.id.checkBox_autoDiscover:
                        sharedPreferencesHandler.setAutoDiscover(isChecked);
                        break;
                    case R.id.checkBox_autoCollapseRooms:
                        sharedPreferencesHandler.setAutoCollapseRooms(isChecked);
                        break;
                    case R.id.checkBox_autoCollapseTimers:
                        sharedPreferencesHandler.setAutoCollapseTimers(isChecked);
                        break;
                    case R.id.checkBox_showRoomAllOnOffButtons:
                        sharedPreferencesHandler.setShowRoomAllOnOff(isChecked);
                        break;
                    case R.id.checkBox_hideAddFAB:
                        sharedPreferencesHandler.setHideAddFAB(isChecked);
                        break;
                    case R.id.checkBox_vibrateOnButtonPress:
                        sharedPreferencesHandler.setVibrateOnButtonPress(isChecked);
                        if (isChecked) {
                            vibrationDurationLayout.setVisibility(View.VISIBLE);
                        } else {
                            vibrationDurationLayout.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.checkBox_highlightLastActivatedButton:
                        sharedPreferencesHandler.setHighlightLastActivatedButton(isChecked);
                        // force receiver widget update
                        ConfigureReceiverWidgetActivity.forceWidgetUpdate(getContext());
                        break;
                    default:
                        break;
                }
            }
        };

        // setup hidden developer menu
        TextView generalSettingsTextView = (TextView) rootView.findViewById(R.id.TextView_generalSettings);
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


        startupDefaultTab = (Spinner) rootView.findViewById(R.id.spinner_startupDefaultTab);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.main_tab_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startupDefaultTab.setAdapter(adapter);
        startupDefaultTab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(getContext());
                sharedPreferencesHandler.setStartupDefaultTab(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        autoDiscover = (CheckBox) rootView.findViewById(R.id.checkBox_autoDiscover);
        autoDiscover.setOnCheckedChangeListener(onCheckedChangeListener);

        autoCollapseRooms = (CheckBox) rootView.findViewById(R.id.checkBox_autoCollapseRooms);
        autoCollapseRooms.setOnCheckedChangeListener(onCheckedChangeListener);

        autoCollapseTimers = (CheckBox) rootView.findViewById(R.id.checkBox_autoCollapseTimers);
        autoCollapseTimers.setOnCheckedChangeListener(onCheckedChangeListener);

        showRoomAllOnOffButtons = (CheckBox) rootView.findViewById(R.id.checkBox_showRoomAllOnOffButtons);
        showRoomAllOnOffButtons.setOnCheckedChangeListener(onCheckedChangeListener);

        hideAddFAB = (CheckBox) rootView.findViewById(R.id.checkBox_hideAddFAB);
        hideAddFAB.setOnCheckedChangeListener(onCheckedChangeListener);

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
                    sharedPreferencesHandler.setVibrationDuration(Integer.valueOf(s.toString()));
                }
            }
        });


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.radioButton_darkBlue:
                        sharedPreferencesHandler.setTheme(SettingsConstants.THEME_DARK_BLUE);
                        break;
                    case R.id.radioButton_lightBlue:
                        sharedPreferencesHandler.setTheme(SettingsConstants.THEME_LIGHT_BLUE);
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

        sharedPreferencesHandler = new SharedPreferencesHandler(getActivity());

        return rootView;
    }

    private void updateUI() {
        autoDiscover.setChecked(sharedPreferencesHandler.getAutoDiscover());
        autoCollapseRooms.setChecked(sharedPreferencesHandler.getAutoCollapseRooms());
        autoCollapseTimers.setChecked(sharedPreferencesHandler.getAutoCollapseTimers());
        showRoomAllOnOffButtons.setChecked(sharedPreferencesHandler.getShowRoomAllOnOff());
        hideAddFAB.setChecked(sharedPreferencesHandler.getHideAddFAB());
        highlightLastActivatedButton.setChecked(sharedPreferencesHandler.getHighlightLastActivatedButton());
        vibrateOnButtonPress.setChecked(sharedPreferencesHandler.getVibrateOnButtonPress());
        vibrationDuration.setText(String.format("%d", sharedPreferencesHandler.getVibrationDuration()));
        if (!sharedPreferencesHandler.getVibrateOnButtonPress()) {
            vibrationDurationLayout.setVisibility(View.GONE);
        } else {
            vibrationDurationLayout.setVisibility(View.VISIBLE);
        }

        switch (sharedPreferencesHandler.getTheme()) {
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

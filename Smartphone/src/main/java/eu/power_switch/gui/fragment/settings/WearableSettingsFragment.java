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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
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
    private BroadcastReceiver broadcastReceiver;

    /**
     * Used to notify this Fragment that Wearable settings have been changed (from/on the wearable device itself)
     *
     * @param context any suitable context
     */
    public static void notifySettingsChanged(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_WEARABLE_SETTINGS_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_wear_settings, container, false);

        CheckBoxInteractionListener checkBoxInteractionListener = new CheckBoxInteractionListener() {
            @Override
            public void onCheckedChangedByUser(CompoundButton buttonView, boolean isChecked) {
                switch (buttonView.getId()) {
                    case R.id.checkBox_autoCollapseRooms:
                        WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS, isChecked);
                        break;
                    case R.id.checkBox_vibrateOnButtonPress:
                        WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS, isChecked);
                        if (isChecked) {
                            vibrationDurationLayout.setVisibility(View.VISIBLE);
                        } else {
                            vibrationDurationLayout.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.checkBox_highlightLastActivatedButton:
                        WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, isChecked);
                        break;
                    default:
                        break;
                }

                // sync settings with wearable app
                UtilityService.forceWearSettingsUpdate(getContext());
            }
        };

        autoCollapseRooms = (CheckBox) rootView.findViewById(R.id.checkBox_autoCollapseRooms);
        autoCollapseRooms.setOnCheckedChangeListener(checkBoxInteractionListener);
        autoCollapseRooms.setOnTouchListener(checkBoxInteractionListener);

        highlightLastActivatedButton = (CheckBox) rootView.findViewById(R.id.checkBox_highlightLastActivatedButton);
        highlightLastActivatedButton.setOnCheckedChangeListener(checkBoxInteractionListener);
        highlightLastActivatedButton.setOnTouchListener(checkBoxInteractionListener);

        vibrateOnButtonPress = (CheckBox) rootView.findViewById(R.id.checkBox_vibrateOnButtonPress);
        vibrateOnButtonPress.setOnCheckedChangeListener(checkBoxInteractionListener);
        vibrateOnButtonPress.setOnTouchListener(checkBoxInteractionListener);

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
                    WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_VIBRATION_DURATION, Integer.valueOf(s.toString()));
                    UtilityService.forceWearSettingsUpdate(getContext());
                }
            }
        });


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.radioButton_darkBlue:
                        WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_THEME, SettingsConstants.THEME_DARK_BLUE);
                        radioButtonLightBlue.setChecked(false);
                        break;
                    case R.id.radioButton_lightBlue:
                        WearablePreferencesHandler.set(WearablePreferencesHandler.KEY_THEME, SettingsConstants.THEME_LIGHT_BLUE);
                        radioButtonDarkBlue.setChecked(false);
                        break;
                    default:
                        break;
                }

                UtilityService.forceWearSettingsUpdate(getContext());
            }
        };

        radioButtonDarkBlue = (RadioButton) rootView.findViewById(R.id.radioButton_darkBlue);
        radioButtonDarkBlue.setOnClickListener(onClickListener);

        radioButtonLightBlue = (RadioButton) rootView.findViewById(R.id.radioButton_lightBlue);
        radioButtonLightBlue.setOnClickListener(onClickListener);

        // BroadcastReceiver to get notifications from background service if data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };

        return rootView;
    }

    private void updateUI() {
        autoCollapseRooms.setChecked(WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_AUTO_COLLAPSE_ROOMS));
        highlightLastActivatedButton.setChecked(WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON));
        vibrateOnButtonPress.setChecked(WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS));
        vibrationDuration.setText(String.valueOf(WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_VIBRATION_DURATION)));
        if (!WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS)) {
            vibrationDurationLayout.setVisibility(View.GONE);
        } else {
            vibrationDurationLayout.setVisibility(View.VISIBLE);
        }

        switch (WearablePreferencesHandler.<Integer>get(WearablePreferencesHandler.KEY_THEME)) {
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

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_WEARABLE_SETTINGS_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}



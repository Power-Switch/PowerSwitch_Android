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

package eu.power_switch.gui.activity;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;

import eu.power_switch.R;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Created by Markus on 26.08.2015.
 */
public class ReceiversActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate
        switch (WearablePreferencesHandler.getTheme()) {
            case SettingsConstants.THEME_DARK_BLUE:
                setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
                setTheme(R.style.PowerSwitchWearTheme_Dark_Red);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                setTheme(R.style.PowerSwitchWearTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_RED:
                setTheme(R.style.PowerSwitchWearTheme_Light_Red);
                break;
            default:
                setTheme(R.style.PowerSwitchWearTheme_Dark_Blue);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receivers);

        // allow always-on screen
        setAmbientEnabled();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

            }
        });
    }

}

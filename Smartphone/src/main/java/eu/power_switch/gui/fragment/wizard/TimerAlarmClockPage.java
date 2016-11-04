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

package eu.power_switch.gui.fragment.wizard;

import android.os.Bundle;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;

/**
 * Basic wizard page consisting of an icon, a title text and a description text
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class TimerAlarmClockPage extends BasicPage {

    public static TimerAlarmClockPage newInstance() {
        Bundle args = new Bundle();
        TimerAlarmClockPage fragment = new TimerAlarmClockPage();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onSetUiValues() {
        super.onSetUiValues();
        setIcon(IconicsHelper.getAlarmClockIcon(getActivity()));
        setTitle(R.string.timers);
        setDescription(R.string.tutorial__timer_explanation);
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.red);
    }

    @Override
    protected int getLayout() {
        return R.layout.wizard_page_basic;
    }

}

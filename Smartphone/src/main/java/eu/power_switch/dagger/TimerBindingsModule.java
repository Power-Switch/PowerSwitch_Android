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

package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.configuration.ConfigureTimerDialog;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage1Time;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage2Days;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage3Action;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage4TabbedSummary;
import eu.power_switch.timer.alarm.TimerAlarmIntentReceiver;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class TimerBindingsModule {

    @ContributesAndroidInjector
    abstract TimersFragment timersFragment();

    @ContributesAndroidInjector
    abstract ConfigureTimerDialog configureTimerDialog();

    @ContributesAndroidInjector
    abstract ConfigureTimerDialogPage1Time configureTimerDialogPage1Time();

    @ContributesAndroidInjector
    abstract ConfigureTimerDialogPage2Days configureTimerDialogPage2Days();

    @ContributesAndroidInjector
    abstract ConfigureTimerDialogPage3Action configureTimerDialogPage3Action();

    @ContributesAndroidInjector
    abstract ConfigureTimerDialogPage4TabbedSummary configureTimerDialogPage4TabbedSummary();

    @ContributesAndroidInjector
    abstract TimerAlarmIntentReceiver alarmIntentReceiver();

}

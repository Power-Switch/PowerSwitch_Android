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

package eu.power_switch.dagger.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.fragment.RoomsFragment;
import eu.power_switch.gui.fragment.ScenesFragment;
import eu.power_switch.gui.fragment.SettingsFragment;
import eu.power_switch.network.service.ListenerService;
import eu.power_switch.network.service.UtilityService;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;

/**
 * Created by Markus on 25.07.2017.
 */
@Module
public abstract class AndroidBindingsModule {

    // Main

    @ContributesAndroidInjector
    abstract MainActivity mainActivity();

    @ContributesAndroidInjector
    abstract RoomsFragment roomsFragment();

    @ContributesAndroidInjector
    abstract ScenesFragment scenesFragment();

    @ContributesAndroidInjector
    abstract SettingsFragment settingsFragment();

    @Provides
    @Singleton
    public static WearablePreferencesHandler provideWearablePreferencesHandler(Context context) {
        return new WearablePreferencesHandler(context);
    }

    @ContributesAndroidInjector
    abstract ListenerService listenerService();

    @ContributesAndroidInjector
    abstract UtilityService utilityService();

}

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

package eu.power_switch.dagger.android;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;
import eu.power_switch.gui.WearableThemeHelper;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;

/**
 * Created by Markus on 25.07.2017.
 */
public abstract class DaggerWearableActivity extends WearableActivity implements HasFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    @Inject
    protected WearablePreferencesHandler wearablePreferencesHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        // set Theme before anything else in onCreate
        applyTheme();

        super.onCreate(savedInstanceState);
    }

    @Override
    public AndroidInjector<Fragment> fragmentInjector() {
        return fragmentInjector;
    }

    protected void applyTheme() {
        WearableThemeHelper.applyTheme(this, wearablePreferencesHandler);
    }

}

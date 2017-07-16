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

package eu.power_switch.wizard.gui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import com.github.paolorotolo.appintro.AppIntro;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.support.HasSupportFragmentInjector;
import eu.power_switch.persistence.shared_preferences.DeveloperPreferencesHandler;
import eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler;

/**
 * Base class for a Wizard Activity backed by ButterKnife
 * <p>
 * Created by Markus on 30.06.2017.
 */
public abstract class WizardActivityBase extends AppIntro implements HasFragmentInjector, HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment>             supportFragmentInjector;
    @Inject
    DispatchingAndroidInjector<android.app.Fragment> frameworkFragmentInjector;

    @Inject
    SmartphonePreferencesHandler smartphonePreferencesHandler;

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // apply forced locale (if set in developer options)
        applyLocale();

        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);
    }

    private void applyLocale() {
        if (DeveloperPreferencesHandler.getForceLanguage()) {
            Resources res = getResources();
            // Change locale settings in the app.
            DisplayMetrics                    dm   = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = DeveloperPreferencesHandler.getLocale();
            res.updateConfiguration(conf, dm);
        }
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }

    @Override
    public AndroidInjector<android.app.Fragment> fragmentInjector() {
        return frameworkFragmentInjector;
    }

}

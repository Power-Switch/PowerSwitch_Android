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

package eu.power_switch.gui.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.support.HasSupportFragmentInjector;
import eu.power_switch.application.PowerSwitch;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.persistence.preferences.DeveloperPreferencesHandler;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;

/**
 * Created by Markus on 30.06.2017.
 */
public abstract class SupportActivityBase extends AppCompatActivity implements HasFragmentInjector, HasSupportFragmentInjector {

    /**
     * Normal activity style
     */
    public static final int DEFAULT = 0;

    /**
     * Normal activity style
     */
    public static final int DIALOG = 1;

    /**
     * Indicates if this activity is in night mode
     */
    private boolean nightModeActive;
    /**
     * Indicates if the current theme is one that changes based on the time of day
     */
    private boolean dayNightModeActive;


    @Inject
    DispatchingAndroidInjector<Fragment>             supportFragmentInjector;
    @Inject
    DispatchingAndroidInjector<android.app.Fragment> frameworkFragmentInjector;

    @Inject
    protected StatusMessageHandler statusMessageHandler;

    @Inject
    protected SmartphonePreferencesHandler smartphonePreferencesHandler;

    @Inject
    protected DeveloperPreferencesHandler developerPreferencesHandler;

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }

    @Override
    public AndroidInjector<android.app.Fragment> fragmentInjector() {
        return frameworkFragmentInjector;
    }

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        // apply forced locale (if set in developer options)
        applyLocale();

        // set initial state
        nightModeActive = PowerSwitch.isNightModeActive();
        dayNightModeActive = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.KEY_THEME) == SettingsConstants.THEME_DAY_NIGHT_BLUE;
        // set Theme before anything else in onCreate();
        switch (getStyle()) {
            case DEFAULT:
                SmartphoneThemeHelper.applyTheme(this, smartphonePreferencesHandler);
                break;
            case DIALOG:
                SmartphoneThemeHelper.applyDialogTheme(this, smartphonePreferencesHandler);
                break;
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (PowerSwitch.isNightModeActive() != nightModeActive) {
            nightModeActive = !nightModeActive;
            if (dayNightModeActive) {
                recreate();
            }
        }
    }

    public void applyLocale() {
        boolean forceLanguage = developerPreferencesHandler.getValue(DeveloperPreferencesHandler.FORCE_ENABLE_FABRIC);
        if (forceLanguage) {
            String localeString = developerPreferencesHandler.getValue(DeveloperPreferencesHandler.LOCALE);
            Locale locale       = new Locale(localeString);

            Resources res = getResources();
            // Change locale settings in the app.
            DisplayMetrics                    dm   = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = locale;
            res.updateConfiguration(conf, dm);
        }
    }

    /**
     * @return true if this activity should use a dialog theme instead of a normal activity theme
     */
    @Style
    protected abstract int getStyle();

    @IntDef({DEFAULT, DIALOG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Style {
    }

}

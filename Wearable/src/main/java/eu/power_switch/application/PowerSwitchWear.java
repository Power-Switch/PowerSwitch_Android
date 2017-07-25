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

package eu.power_switch.application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.core.CrashlyticsCore;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import eu.power_switch.BuildConfig;
import eu.power_switch.dagger.DaggerAppComponent;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Entry point for the Wearable application
 * <p/>
 * Created by Markus on 12.08.2015.
 */
public class PowerSwitchWear extends DaggerApplication {

    // Default System Handler for uncaught Exceptions
    private Thread.UncaughtExceptionHandler originalUncaughtExceptionHandler;

    public PowerSwitchWear() {
        // save original uncaught exception handler
        originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // Set up our own UncaughtExceptionHandler to log errors we couldn't even think of
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable throwable) {
                Timber.e("FATAL EXCEPTION", throwable);

                if (originalUncaughtExceptionHandler != null) {
                    //Delegates to Android's error handling
                    originalUncaughtExceptionHandler.uncaughtException(thread, throwable);
                }

//                System.exit(2); //Prevents the service/app from freezing
            }
        });
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder()
                .create(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // Configure Fabric
        Fabric.with(this,
                new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG) // disable Crashlytics on debug builds
                        .build())
                        .build(),
                new Answers());
    }
}

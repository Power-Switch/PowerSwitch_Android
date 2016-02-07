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

package eu.power_switch.application;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Entry Point for the Application
 * <p/>
 * Created by Markus on 11.08.2015.
 */
public class PowerSwitch extends MultiDexApplication {

    // Default System Handler for uncaught Exceptions
    private Thread.UncaughtExceptionHandler originalUncaughtExceptionHandler;

    public PowerSwitch() {
        // save original uncaught exception handler
        originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // Set up our own UncaughtExceptionHandler to log errors we couldn't even think of
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Log.e("Fatal Exception", throwable);

                // pass on exception to android system
                originalUncaughtExceptionHandler.uncaughtException(thread, throwable);
            }
        });
    }

    /**
     * Get a text representation of application version name and build number
     *
     * @param context any suitable context
     * @return app version as text
     */
    public static String getAppVersionDescription(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (Exception e) {
            Log.e(e);
            return "unknown (error while retrieving)";
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Configure Log4J Logger
        LogHandler.configureLogger();

        Log.d("Application init...");
        Log.d("App version: " + getAppVersionDescription(this));
        Log.d("Device API Level: " + android.os.Build.VERSION.SDK_INT);
        Log.d("Device OS Version name: " + Build.VERSION.RELEASE);
        Log.d("Device brand/model: " + LogHandler.getDeviceName());

        // One time initialization of handlers for static access
        DatabaseHandler.init(this);
        NetworkHandler.init(this);
        SmartphonePreferencesHandler.init(this);
        WearablePreferencesHandler.init(this);

        DeveloperPreferencesHandler.init(this);
    }

}
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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.core.CrashlyticsCore;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import dagger.android.HasActivityInjector;
import eu.power_switch.BuildConfig;
import eu.power_switch.R;
import eu.power_switch.dagger.DaggerAppComponent;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.persistence.shared_preferences.DeveloperPreferencesHandler;
import eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.application.ApplicationHelper;
import eu.power_switch.shared.log.LogHelper;
import eu.power_switch.shared.log.TimberHelper;
import eu.power_switch.shared.permission.PermissionHelper;
import eu.power_switch.shared.persistence.preferences.PreferenceItem;
import eu.power_switch.shared.settings.WearablePreferencesHandler;
import eu.power_switch.timer.Timer;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import eu.power_switch.widget.provider.RoomWidgetProvider;
import eu.power_switch.widget.provider.SceneWidgetProvider;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Entry Point for the Application
 * <p/>
 * Created by Markus on 11.08.2015.
 */
public class PowerSwitch extends DaggerApplication implements HasActivityInjector {

    // Default System Handler for uncaught Exceptions
    private Thread.UncaughtExceptionHandler originalUncaughtExceptionHandler;
    private Handler                         mHandler;

    @Inject
    PersistenceHandler persistenceHandler;

    @Inject
    SmartphonePreferencesHandler smartphonePreferencesHandler;

    @Inject
    WearablePreferencesHandler wearablePreferencesHandler;

    @Inject
    DeveloperPreferencesHandler developerPreferencesHandler;

    @Inject
    StatusMessageHandler statusMessageHandler;

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder()
                .create(this);
    }

    public PowerSwitch() {
        // save original uncaught exception handler
        originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // Set up our own UncaughtExceptionHandler to log errors we couldn't even think of
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable throwable) {
                Timber.e("FATAL EXCEPTION", throwable);

                try {
                    if (isUIThread()) {
                        statusMessageHandler.showErrorDialog(getApplicationContext(), throwable);
                    } else {  //handle non UI thread throw uncaught exception
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                statusMessageHandler.showErrorDialog(getApplicationContext(), throwable);
                            }
                        });
                    }
                } catch (Exception e) {
                    Timber.e("Error showing \"Unknown Error\" AlertDialog", e);
                }

                // not possible without killing all app processes, including the UnkownErrorDialog!?
//                if (originalUncaughtExceptionHandler != null) {
                // Delegates to Android's error handling
//                    originalUncaughtExceptionHandler.uncaughtException(thread, throwable);
//                }

                System.exit(2); //Prevents the service/app from freezing
            }
        });
    }

    /**
     * Get the build time of the current version of this application
     *
     * @return build time as string (or "unknown" if error while retrieving), never null
     */
    @NonNull
    public static String getAppBuildTime() {
        try {
            Date date = new Date(BuildConfig.BUILD_TIMESTAMP);
            return SimpleDateFormat.getInstance()
                    .format(date);
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Check if the current is the UI thread
     *
     * @return true if the current thread is the same as the UI thread
     */
    public boolean isUIThread() {
        return Looper.getMainLooper()
                .getThread() == Thread.currentThread();
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
        boolean internalFileLoggingOnly;
        Integer logDestinationType = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.KEY_LOG_DESTINATION);
        if (logDestinationType.equals(Integer.valueOf(getString(R.string.value_internal)))) {
            internalFileLoggingOnly = true;
        } else {
            internalFileLoggingOnly = false;
        }

        TimberHelper.plantTrees(this, BuildConfig.DEBUG, internalFileLoggingOnly);

        Timber.d("Application init...");
        Timber.d("App version: " + ApplicationHelper.getAppVersionDescription(this));
        Timber.d("App build time: " + getAppBuildTime());
        Timber.d("Device API Level: " + android.os.Build.VERSION.SDK_INT);
        Timber.d("Device OS Version name: " + Build.VERSION.RELEASE);
        Timber.d("Device brand/model: " + LogHelper.getDeviceName());

        // log preferences
        for (PreferenceItem preferenceItem : smartphonePreferencesHandler.getAllPreferenceItems()) {
            Timber.d(preferenceItem.getKey(this) + ": " + smartphonePreferencesHandler.getValue(preferenceItem));
        }

        for (PreferenceItem preferenceItem : wearablePreferencesHandler.getAllPreferenceItems()) {
            Timber.d(preferenceItem.getKey(this) + ": " + wearablePreferencesHandler.getValue(preferenceItem));
        }

        for (PreferenceItem preferenceItem : developerPreferencesHandler.getAllPreferenceItems()) {
            Timber.d(preferenceItem.getKey(this) + ": " + developerPreferencesHandler.getValue(preferenceItem));
        }

        // Configure Fabric
        boolean crashReportingEnabled = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA);
        boolean forceFabric           = developerPreferencesHandler.getValue(DeveloperPreferencesHandler.FORCE_ENABLE_FABRIC);
        boolean enableFabric          = crashReportingEnabled || forceFabric;

        if (enableFabric) {
            Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(
                    // disable Crashlytics on debug builds
                    BuildConfig.DEBUG && !forceFabric)
                    .build())
                    .build(), new Answers());
        }

        // Initialize Firebase
//        Firebase.setAndroidContext(this);


        // This is where you do your work in the UI thread.
// Your worker tells you in the message what to do.
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Toast.makeText(getApplicationContext(), message.obj.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        };


        // Log configuration and update widgets, wear, etc.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
//                    mHandler.obtainMessage(0, "Wait...").sendToTarget();

                    // wait some time for application to finish loading
                    Thread.sleep(5000);
//                    mHandler.obtainMessage(0, "Working...").sendToTarget();


                    if (!PermissionHelper.isLocationPermissionAvailable(getApplicationContext())) {
                        try {
                            persistenceHandler.disableGeofences();
                            Timber.d("Disabled all Geofences because of missing location permission");
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }

                    // update wear data
                    UtilityService.forceWearDataUpdate(getApplicationContext());
                    UtilityService.forceWearSettingsUpdate(getApplicationContext());

                    // update receiver widgets
                    ReceiverWidgetProvider.forceWidgetUpdate(getApplicationContext());
                    // update room widgets
                    RoomWidgetProvider.forceWidgetUpdate(getApplicationContext());
                    // update scene widgets
                    SceneWidgetProvider.forceWidgetUpdate(getApplicationContext());
                } catch (Exception e) {
                    Timber.e(e);
                }

                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_LESS_FAVORABLE);

//                    mHandler.obtainMessage(0, "Wait...").sendToTarget();
                    Thread.sleep(5000);
//                    mHandler.obtainMessage(0, "Logging database...").sendToTarget();

                    for (Apartment apartment : persistenceHandler.getAllApartments()) {
                        Timber.d(apartment.toString());
                    }

                    for (Timer timer : persistenceHandler.getAllTimers()) {
                        Timber.d(timer.toString());
                    }

                    for (Geofence geofence : persistenceHandler.getAllGeofences()) {
                        Timber.d(geofence.toString());
                    }

                    for (Gateway gateway : persistenceHandler.getAllGateways()) {
                        Timber.d(gateway.toString() + "\n");
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * Trigger restart of the application
     *
     * @param context application context
     */
    public static void restart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long          executionTime = System.currentTimeMillis() + 1000;

        if (Build.VERSION.SDK_INT >= 23) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, executionTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, executionTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT < 19) {
            alarmMgr.set(AlarmManager.RTC, executionTime, pendingIntent);
        }

        System.exit(0);
    }
}
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

package eu.power_switch.google_play_services.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;

/**
 * Geofence IntentReceiver used to reinitialize Geofences after device reboot
 * <p/>
 * Created by Markus on 31.01.2016.
 */
public class GeofenceIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHandler.init(context);

        try {
            String log = "onReceive: Action: ";
            log += intent.getAction();
            log += "( ";
            if (intent.getData() != null) {
                log += intent.getData().getScheme();
                log += "://";
                log += intent.getData().getHost();
            }
            log += " ) ";
            Bundle extras = intent.getExtras();
            log += "{ ";
            if (extras != null) {
                for (String extra : extras.keySet()) {
                    log += extra + "[" + extras.get(extra) + "], ";
                }
            }
            log += " }";
            Log.d("AlarmIntentReceiver", log);
        } catch (Exception e) {
            Log.e(e);
        }

        try {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                // restart all active geofences because device rebooted
                Log.d(this, "restarting all active geofences because device rebooted...");
                reinitializeGeofences(context);
            } else {
                Log.d(this, "Received unknown intent: " + intent.getAction());
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private void reinitializeGeofences(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GeofenceApiHandler geofenceApiHandler = new GeofenceApiHandler(context);
                geofenceApiHandler.blockingConnect();

//                geofenceApiHandler.removeAllGeofences();
                try {
                    List<Geofence> geofences = DatabaseHandler.getAllGeofences(true);
                    for (Geofence geofence : geofences) {
                        geofenceApiHandler.addGeofence(geofence);
                    }
                } catch (Exception e) {
                    Log.e(GeofenceIntentReceiver.class, e);
                }
            }
        }).start();
    }
}

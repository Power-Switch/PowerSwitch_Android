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

package eu.power_switch.network.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.constants.WearableSettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Created by Markus on 09.06.2016.
 */
public class UtilityService extends IntentService {

    public UtilityService() {
        super("UtilityService (Wear)");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UtilityService(String name) {
        super(name);
    }

    /**
     * Create Intent to update Wear Settings via background service
     *
     * @param context any suitable context
     */
    public static void forceWearSettingsUpdate(Context context) {
        Log.d("Updating Settings for Wearable");
        Intent intent = new Intent(context, UtilityService.class);
        intent.setAction(WearableConstants.REQUEST_SETTINGS_UPDATE_PATH);
        context.startService(intent);
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (WearableConstants.REQUEST_SETTINGS_UPDATE_PATH.equals(intent.getAction())) {
            Log.d("Pushing Wearable Settings to Cloud...");
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API).build();

            // It's OK to use blockingConnect() here as we are running in an
            // IntentService that executes work on a separate (background) thread.
            ConnectionResult connectionResult = googleApiClient.blockingConnect(
                    SettingsConstants.GOOGLE_API_CLIENT_TIMEOUT, TimeUnit.SECONDS);

            ArrayList<DataMap> settings = new ArrayList<>();
            DataMap settingsDataMap = getSettingsDataMap();
            settings.add(settingsDataMap);

            if (connectionResult.isSuccess() && googleApiClient.isConnected()) {

                PutDataMapRequest dataMap = PutDataMapRequest.create(WearableConstants.SETTINGS_PATH);
                dataMap.getDataMap().putDataMapArrayList(WearableConstants.EXTRA_SETTINGS, settings);
                PutDataRequest request = dataMap.asPutDataRequest();

                // Send the data over
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, request).await();

                if (!result.getStatus().isSuccess()) {
                    Log.e("", String.format("Error sending settings using DataApi (error code = %d)",
                            result.getStatus().getStatusCode()));
                } else {
                    Log.d("Updated settings sent");
                }

            } else {
                // GoogleApiClient connection error
                Log.e("Error connecting GoogleApiClient");
            }
        }
    }

    /**
     * Get Wearable settings and put them into a DataMap
     *
     * @return DataMap containing all Wearable settings
     */
    private DataMap getSettingsDataMap() {
        DataMap settingsDataMap = new DataMap();
        settingsDataMap.putBoolean(WearableSettingsConstants.KEY_AUTO_COLLAPSE_ROOMS, WearablePreferencesHandler
                .getAutoCollapseRooms());
        settingsDataMap.putBoolean(WearableSettingsConstants.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, WearablePreferencesHandler
                .getHighlightLastActivatedButton());
        settingsDataMap.putBoolean(WearableSettingsConstants.KEY_SHOW_ROOM_ALL_ON_OFF, WearablePreferencesHandler.getShowRoomAllOnOff());
        settingsDataMap.putInt(WearableSettingsConstants.KEY_THEME, WearablePreferencesHandler.getTheme());
        settingsDataMap.putBoolean(WearableSettingsConstants.KEY_VIBRATE_ON_BUTTON_PRESS, WearablePreferencesHandler
                .getVibrateOnButtonPress());
        settingsDataMap.putInt(WearableSettingsConstants.KEY_VIBRATION_DURATION, WearablePreferencesHandler.getVibrationDuration());

        return settingsDataMap;
    }
}

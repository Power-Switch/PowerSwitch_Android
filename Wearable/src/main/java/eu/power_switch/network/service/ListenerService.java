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

package eu.power_switch.network.service;

import android.widget.Toast;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.power_switch.dagger.android.DaggerWearableListenerService;
import eu.power_switch.event.DataChangedEvent;
import eu.power_switch.event.PreferenceChangedEvent;
import eu.power_switch.persistence.database.DatabaseHandler;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import eu.power_switch.shared.wearable.CommunicationHelper;
import eu.power_switch.shared.wearable.dataevents.ApplicationDataEvent;
import io.paperdb.Paper;
import me.denley.courier.Packager;

/**
 * Created by Markus on 05.06.2015.
 * <p/>
 * A Wear listener service, used to receive inbound messages from
 * other devices.
 */
public class ListenerService extends DaggerWearableListenerService {

    @Inject
    DatabaseHandler databaseHandler;

    @Inject
    WearablePreferencesHandler wearablePreferencesHandler;

    /**
     * Reacts to DataChanged Events from DataApi
     *
     * @param dataEvents
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            if (event.getDataItem() != null) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    if (WearableConstants.DATA_PATH.equals(event.getDataItem()
                            .getUri()
                            .getPath())) {

                        final DataItem dataItem = event.getDataItem();

                        final DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);

                        ApplicationDataEvent dataEvent = Packager.unpack(this, dataItem, ApplicationDataEvent.class);

                        // save received data to persistence
                        Paper.book()
                                .write("apartments", dataEvent);

                        // send data to Activity
                        EventBus.getDefault()
                                .post(new DataChangedEvent(dataEvent));
                    } else if (WearableConstants.SETTINGS_PATH.equals(event.getDataItem()
                            .getUri()
                            .getPath())) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        ArrayList<DataMap> settings = dataMapItem.getDataMap()
                                .getDataMapArrayList(WearableConstants.EXTRA_SETTINGS);

                        int oldThemeValue = wearablePreferencesHandler.getValue(WearablePreferencesHandler.THEME);
                        CommunicationHelper.extractSettings(this, wearablePreferencesHandler, settings);
                        int newThemeValue = wearablePreferencesHandler.getValue(WearablePreferencesHandler.THEME);

                        // notify about changes
                        if (newThemeValue != oldThemeValue) {
                            EventBus.getDefault()
                                    .post(new PreferenceChangedEvent<>(WearablePreferencesHandler.THEME));
                        } else {
                            EventBus.getDefault()
                                    .post(new PreferenceChangedEvent<>(null));
                        }
                    }
                } else if (event.getType() == DataEvent.TYPE_DELETED) {
                    if (WearableConstants.DATA_PATH.equals(event.getDataItem()
                            .getUri()
                            .getPath())) {
                        // send data to Activity
                        // update with empty lists

                        EventBus.getDefault()
                                .post(new DataChangedEvent(null));
                    }
                }
            }
        }
    }

    /**
     * Reacts on Messages from MessageApi
     *
     * @param messageEvent
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Toast.makeText(getApplicationContext(), "Message received: " + convertEventDataToString(messageEvent.getData()), Toast.LENGTH_LONG)
                .show();

        if (messageEvent.getPath()
                .equals(WearableConstants.START_ACTIVITY_PATH)) {
            // TODO: Launch Wearable App
            // is this even possible?
        }
    }

    private String convertEventDataToString(byte[] data) {
        return new String(data);
    }
}
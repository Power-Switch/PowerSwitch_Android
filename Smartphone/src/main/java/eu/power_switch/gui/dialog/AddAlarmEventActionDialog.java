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

package eu.power_switch.gui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.shared.constants.ExternalAppConstants;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Dialog to select a timer action configuration
 * <p/>
 * Created by Markus on 28.09.2015.
 */
public class AddAlarmEventActionDialog extends AddActionDialog {

    ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT currentEventType =
            ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT.ALARM_TRIGGERED;

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context
     */
    public static void sendAlarmEventActionAddedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_ALARM_EVENT_ACTION_ADDED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            int eventId = args.getInt("eventId");
            currentEventType = ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT.getById(eventId);
        }

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    protected void addCurrentSelection() {
        DatabaseHandler.addAlarmAction(currentEventType, getCurrentSelection());
    }

    @Override
    protected void sendDataChangedBroadcast(Context context) {
        sendAlarmEventActionAddedBroadcast(context);
    }
}
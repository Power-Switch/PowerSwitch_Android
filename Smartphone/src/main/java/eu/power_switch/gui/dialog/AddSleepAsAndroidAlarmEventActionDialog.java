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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.event.AlarmEventActionAddedEvent;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;

/**
 * Dialog to select a timer action configuration
 * <p/>
 * Created by Markus on 28.09.2015.
 */
public class AddSleepAsAndroidAlarmEventActionDialog extends AddActionDialog {

    public static final String EVENT_ID_KEY = "eventId";

    SleepAsAndroidConstants.Event currentEventType =
            SleepAsAndroidConstants.Event.ALARM_TRIGGERED;

    public static AddSleepAsAndroidAlarmEventActionDialog newInstance(int eventId) {
        Bundle args = new Bundle();
        args.putInt(EVENT_ID_KEY, eventId);

        AddSleepAsAndroidAlarmEventActionDialog fragment = new AddSleepAsAndroidAlarmEventActionDialog();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Used to notify the setup page that some info has changed
     */
    public static void notifyAlarmEventActionAdded() {
        EventBus.getDefault()
                .post(new AlarmEventActionAddedEvent());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null && args.containsKey(EVENT_ID_KEY)) {
            int eventId = args.getInt(EVENT_ID_KEY);
            currentEventType = SleepAsAndroidConstants.Event.getById(eventId);
        }

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    protected void addCurrentSelection() {
        try {
            ArrayList<Action> actions = new ArrayList<>(persistanceHandler.getAlarmActions(currentEventType));
            actions.add(getCurrentSelection());
            persistanceHandler.setAlarmActions(currentEventType, actions);
            StatusMessageHandler.showInfoMessage(getTargetFragment(),
                    R.string.action_saved, Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    protected void sendDataChangedBroadcast(Context context) {
        notifyAlarmEventActionAdded();
    }
}
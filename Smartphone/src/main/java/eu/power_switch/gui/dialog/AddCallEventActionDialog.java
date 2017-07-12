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

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import eu.power_switch.event.CallEventActionAddedEvent;
import eu.power_switch.gui.fragment.configure_call_event.ConfigureCallEventDialogPage2Actions;

/**
 * Dialog to select a action configuration
 * <p/>
 * Created by Markus on 28.09.2015.
 */
public class AddCallEventActionDialog extends AddActionDialog {

    /**
     * Used to notify the setup page that some info has changed
     */
    public static void sendCallEventActionAddedBroadcast() {
        EventBus.getDefault()
                .post(new CallEventActionAddedEvent());
    }

    @Override
    protected void addCurrentSelection() {
        ConfigureCallEventDialogPage2Actions.addAction(getCurrentSelection());
    }

    @Override
    protected void sendDataChangedBroadcast(Context context) {
        sendCallEventActionAddedBroadcast();
    }
}
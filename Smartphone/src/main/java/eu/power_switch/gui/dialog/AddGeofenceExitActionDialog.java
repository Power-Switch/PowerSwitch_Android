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
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage3ExitActionsFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Dialog to select a action configuration
 * <p/>
 * Created by Markus on 28.09.2015.
 */
public class AddGeofenceExitActionDialog extends AddActionDialog {

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context any suitable context
     */
    public static void sendGeofenceExitActionAddedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_GEOFENCE_EXIT_ACTION_ADDED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected void addCurrentSelection() {
        ConfigureGeofenceDialogPage3ExitActionsFragment.addAction(getCurrentSelection());
    }

    @Override
    protected void sendDataChangedBroadcast(Context context) {
        sendGeofenceExitActionAddedBroadcast(context);
    }
}
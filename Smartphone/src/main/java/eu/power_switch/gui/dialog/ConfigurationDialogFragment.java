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

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Created by Markus on 25.03.2016.
 */
public abstract class ConfigurationDialogFragment extends Fragment {

    /**
     * Used to notify parent Dialog that configuration has changed
     */
    public void notifyConfigurationChanged() {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_CONFIGURATION_DIALOG_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

}

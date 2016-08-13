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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import eu.power_switch.R;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

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

    /**
     * Get content view of this ConfigurationDialogFragment
     * <p/>
     * This view should be declared with the id "contentView" in the layout definition of this content fragment.
     * If no such view can be found it will default to the "getView()" method of the Fragment
     *
     * @return
     */
    @Nullable
    public View getContentView() {
        View contentView = getView().findViewById(R.id.contentView);

        if (contentView == null) {
            Log.w("ContentView is null! Did you define a view with id \"contentView\" in your layout? Using getView() as fallback.");
            return getView();
        } else {
            return contentView;
        }
    }

}

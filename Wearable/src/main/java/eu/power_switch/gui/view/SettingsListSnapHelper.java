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

package eu.power_switch.gui.view;

import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import timber.log.Timber;

/**
 * Created by Markus on 18.07.2017.
 */
public class SettingsListSnapHelper extends LinearSnapHelper {

    private SettingsListItemLayout lastCenteredItem;

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        View snapView = super.findSnapView(layoutManager);

        if (snapView instanceof SettingsListItemLayout) {
            SettingsListItemLayout settingsListItemLayout = (SettingsListItemLayout) snapView;
            if (settingsListItemLayout == lastCenteredItem) {
                Timber.v("ignoring snappedView, same as last");
                return snapView;
            }

            if (lastCenteredItem != null) {
                lastCenteredItem.onNonCenterPosition(true);
            }
            lastCenteredItem = settingsListItemLayout;
            settingsListItemLayout.onCenterPosition(true);
        }

        return snapView;
    }
}

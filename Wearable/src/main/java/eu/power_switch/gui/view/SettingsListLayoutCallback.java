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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.CurvingLayoutCallback;
import android.view.View;

/**
 * Created by Markus on 04.08.2017.
 */
public class SettingsListLayoutCallback extends CurvingLayoutCallback {

    private static final float MAX_ICON_SCALE  = 1f;
    private static final float MIN_ICON_SCALE  = 0.5f;
    private static final float MAX_ALPHA_SCALE = 1f;
    private static final float MIN_ALPHA_SCALE = 0.25f;

    public SettingsListLayoutCallback(Context context) {
        super(context);
    }

    @Override
    public void onLayoutFinished(View child, RecyclerView parent) {
        super.onLayoutFinished(child, parent);

        if (child instanceof SettingsListItemLayout) {
            // Figure out % progress from top to bottom
            float centerOffset                   = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
            float verticalRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

            // Normalize for center
            float progressToCenter = Math.abs(0.5f - verticalRelativeToCenterOffset);

            // adjust to get a value between 0 and 1
            float progressScale = Math.max(1 - progressToCenter * 2, 0);
            progressScale = Math.min(progressScale, 1);

            float alphaScale = ((MAX_ALPHA_SCALE - MIN_ALPHA_SCALE) * progressScale + MIN_ALPHA_SCALE) * 255;
            float iconScale  = (MAX_ICON_SCALE - MIN_ICON_SCALE) * progressScale + MIN_ICON_SCALE;

            SettingsListItemLayout listItem = (SettingsListItemLayout) child;
            listItem.setIconScale(iconScale);
            listItem.setTextAlpha((int) alphaScale);
        }
    }
}

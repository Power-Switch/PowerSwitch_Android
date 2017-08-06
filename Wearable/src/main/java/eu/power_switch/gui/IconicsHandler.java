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

package eu.power_switch.gui;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Markus on 08.06.2016.
 */
@Singleton
public class IconicsHandler {

    @Inject
    Context context;

    @Inject
    public IconicsHandler() {
    }

    /**
     * Get an icon that shall be used in a Navigation Drawer
     *
     * @param icon icon id
     *
     * @return IconicsDrawable
     */
    public IconicsDrawable getNavigationDrawerIcon(IIcon icon) {
        return getIcon(icon, Color.WHITE, 64);
    }

    /**
     * Get an icon that shall be used in a settingslist
     *
     * @param icon icon id
     *
     * @return IconicsDrawable
     */
    public IconicsDrawable getWearableSettingsListIcon(IIcon icon) {
        return getIcon(icon, Color.WHITE, 24);
    }

    private IconicsDrawable getIcon(IIcon icon, @ColorInt int color, int sizeDp) {
        return new IconicsDrawable(context, icon).sizeDp(sizeDp)
                .color(color);
    }

}

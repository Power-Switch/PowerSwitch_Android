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

package eu.power_switch.gui;

import android.content.Context;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

/**
 * Created by Markus on 08.06.2016.
 */
public class IconicsHelper {

    /**
     * Get "Rooms" icon
     *
     * @param context any suitable context
     * @return "Rooms" icon
     */
    public static IconicsDrawable getRoomsIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_view_dashboard)
                .sizeDp(64)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Scenes" icon
     *
     * @param context any suitable context
     * @return "Scenes" icon
     */
    public static IconicsDrawable getScenesIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_brightness_setting)
                .sizeDp(64)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Settings" icon
     *
     * @param context any suitable context
     * @return "Settings" icon
     */
    public static IconicsDrawable getSettingsIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_settings)
                .sizeDp(64)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Close rooms" icon
     *
     * @param context any suitable context
     * @return "Close rooms" icon
     */
    public static IconicsDrawable getAutocollapseRoomsIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_photo_size_select_small)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "History" icon
     *
     * @param context any suitable context
     * @return "History" icon
     */
    public static IconicsDrawable getHistoryIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_time_restore)
                .sizeDp(24)
                .iconOffsetXDp(-2)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Vibration" icon
     *
     * @param context any suitable context
     * @return "Vibration" icon
     */
    public static IconicsDrawable getVibrationIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_vibration)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }


}

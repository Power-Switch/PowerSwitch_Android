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
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import eu.power_switch.R;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;

/**
 * Helper class for access to often used icons
 * <p/>
 * Created by Markus on 13.12.2015.
 */
public class IconicsHelper {

    private IconicsHelper() {
    }

    public static IconicsDrawable getMenuIcon(Context context) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_menu)
                .sizeDp(24);

        if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
            iconicsDrawable.color(ContextCompat.getColor(context, android.R.color.white));
        } else {
            iconicsDrawable.color(ContextCompat.getColor(context, android.R.color.black));
        }

        return iconicsDrawable;
    }

    public static IconicsDrawable getAddIcon(Context context, @ColorRes int colorResourceId) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_plus)
                .color(ContextCompat.getColor(context, colorResourceId))
                .sizeDp(24)
                .paddingDp(5);

        return iconicsDrawable;
    }

    public static IconicsDrawable getRefreshIcon(Context context, @ColorRes int colorResourceId) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_refresh)
                .color(ContextCompat.getColor(context, colorResourceId))
                .sizeDp(24)
                .paddingDp(2);

        return iconicsDrawable;
    }

    public static IconicsDrawable getCancelIcon(Context context) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_close_circle)
                .color(ContextCompat.getColor(context, R.color.inactive_gray))
                .sizeDp(36);

        return iconicsDrawable;
    }

    public static IconicsDrawable getDeleteIcon(Context context, @ColorRes int colorResourceId) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_delete)
                .color(ContextCompat.getColor(context, colorResourceId))
                .sizeDp(36);

        return iconicsDrawable;
    }

    public static IconicsDrawable getNextIcon(Context context) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_arrow_forward)
                .sizeDp(36)
                .paddingDp(2);

        if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
            iconicsDrawable.color(ContextCompat.getColor(context, android.R.color.white));
        } else {
            iconicsDrawable.color(ContextCompat.getColor(context, android.R.color.black));
        }

        return iconicsDrawable;
    }

    public static IconicsDrawable getSaveIcon(Context context) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_check_circle)
                .sizeDp(36);

        if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
            iconicsDrawable.color(ContextCompat.getColor(context, android.R.color.white));
        } else {
            iconicsDrawable.color(ContextCompat.getColor(context, android.R.color.black));
        }

        return iconicsDrawable;
    }

    public static IconicsDrawable getReorderIcon(Context context) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_reorder)
                .sizeDp(24);

        if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
            iconicsDrawable.color(ContextCompat.getColor(context, android.R.color.white));
        } else {
            iconicsDrawable.color(ContextCompat.getColor(context, android.R.color.black));
        }

        return iconicsDrawable;
    }
}

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
import android.support.v4.content.ContextCompat;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import eu.power_switch.R;
import eu.power_switch.shared.ThemeHelper;

/**
 * Helper class for access to often used icons
 * <p/>
 * Created by Markus on 13.12.2015.
 */
public class IconicsHelper {

    private IconicsHelper() {
    }

    /**
     * Get "Menu" icon
     *
     * @param context any suitable context
     * @return "Menu" Icon
     */
    public static IconicsDrawable getMenuIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_menu)
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
                .iconOffsetXDp(-1)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Timer" icon
     *
     * @param context any suitable context
     * @return "Timer" icon
     */
    public static IconicsDrawable getTimerIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_time)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Phone" icon
     *
     * @param context any suitable context
     * @return "Phone" icon
     */
    public static IconicsDrawable getPhoneIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_phone)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "AlarmClock" icon
     *
     * @param context any suitable context
     * @return "AlarmClock" icon
     */
    public static IconicsDrawable getAlarmClockIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_alarm)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "NFC" icon
     *
     * @param context any suitable context
     * @return "NFC" icon
     */
    public static IconicsDrawable getNfcIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_nfc)
                .sizeDp(24)
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
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Help" icon
     *
     * @param context any suitable context
     * @return "Help" icon
     */
    public static IconicsDrawable getHelpIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_help)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Donate" icon
     *
     * @param context any suitable context
     * @return "Donate" icon
     */
    public static IconicsDrawable getDonateIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_money)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "About" icon
     *
     * @param context any suitable context
     * @return "About" icon
     */
    public static IconicsDrawable getAboutIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_info)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Backup/Restore" icon
     *
     * @param context any suitable context
     * @return "Backup/Restore" icon
     */
    public static IconicsDrawable getBackupRestoreIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_time_restore)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Geofences" icon
     *
     * @param context any suitable context
     * @return "Geofences" icon
     */
    public static IconicsDrawable getGeofencesIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_gps_dot)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Apartments" icon
     *
     * @param context any suitable context
     * @return "Apartments" icon
     */
    public static IconicsDrawable getApartmentsIcon(Context context) {
        return getApartmentsIcon(context, 24);
    }

    /**
     * Get "Apartments" icon
     *
     * @param context any suitable context
     *
     * @return "Apartments" icon
     */
    public static IconicsDrawable getApartmentsIcon(Context context, int sizeDp) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_home).sizeDp(sizeDp)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Rooms/Scenes" icon
     *
     * @param context any suitable context
     * @return "Rooms/Scenes" icon
     */
    public static IconicsDrawable getRoomsScenesIcon(Context context) {
        final int color = eu.power_switch.shared.ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_lamp)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Add" icon
     *
     * @param context any suitable context
     * @param color   icon color
     * @return "Add" icon
     */
    public static IconicsDrawable getAddIcon(Context context, int color) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_plus)
                .color(color)
                .sizeDp(24)
                .paddingDp(5);

        return iconicsDrawable;
    }

    /**
     * Get "Refresh" icon
     *
     * @param context any suitable context
     * @return "Refresh" icon
     */
    public static IconicsDrawable getRefreshIcon(Context context, int color) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_refresh)
                .color(color)
                .sizeDp(24)
                .paddingDp(2);

        return iconicsDrawable;
    }

    /**
     * Get "Cancel" icon
     *
     * @param context any suitable context
     * @return "Cancel" icon
     */
    public static IconicsDrawable getCancelIcon(Context context) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_close_circle)
                .color(ContextCompat.getColor(context, R.color.inactive_gray))
                .sizeDp(36);

        return iconicsDrawable;
    }

    /**
     * Get "Delete" icon
     *
     * @param context any suitable context
     * @return "Delete" icon
     */
    public static IconicsDrawable getDeleteIcon(Context context, int color) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_delete)
                .color(color)
                .sizeDp(36);

        return iconicsDrawable;
    }

    /**
     * Get "Next" icon
     *
     * @param context any suitable context
     * @return "Next" icon
     */
    public static IconicsDrawable getNextIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_arrow_forward)
                .sizeDp(36)
                .paddingDp(2)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Save" icon
     *
     * @param context any suitable context
     * @return "Save" icon
     */
    public static IconicsDrawable getSaveIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_check_circle)
                .sizeDp(36)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Reorder" icon
     *
     * @param context any suitable context
     * @return "Reorder" icon
     */
    public static IconicsDrawable getReorderHandleIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_reorder)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    public static IconicsDrawable getReorderIcon(Context context, int color) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_reorder)
                .color(color)
                .sizeDp(24)
                .paddingDp(2);

        return iconicsDrawable;
    }

    /**
     * Get "Search" icon
     *
     * @param context any suitable context
     * @return "Search" icon
     */
    public static IconicsDrawable getSearchIcon(Context context, int color) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_search)
                .color(color)
                .sizeDp(24);

        return iconicsDrawable;
    }

    /**
     * Get "Attention" icon
     *
     * @param context any suitable context
     * @return "Attention" icon
     */
    public static IconicsDrawable getAttentionIcon(Context context) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_report_problem)
                .color(ContextCompat.getColor(context, R.color.color_red_a700))
                .sizeDp(24);

        return iconicsDrawable;
    }

    /**
     * Get "Up" icon
     *
     * @param context any suitable context
     * @return "Up" icon
     */
    public static IconicsDrawable getUpIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_arrow_upward)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Folder" icon
     *
     * @param context any suitable context
     * @return "Folder" icon
     */
    public static IconicsDrawable getFolderIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_folder)
                .sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Keyboard Arrow Right" icon
     *
     * @param context any suitable context
     * @return "Keyboard Arrow Right" icon
     */
    public static IconicsDrawable getKbArrowRightIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_keyboard_arrow_right)
                .sizeDp(16)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Keyboard Arrow Down" icon
     *
     * @param context any suitable context
     * @return "Keyboard Arrow Down" icon
     */
    public static IconicsDrawable getKbArrowDownIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_keyboard_arrow_down)
                .sizeDp(16)
                .color(color);

        return iconicsDrawable;
    }
}

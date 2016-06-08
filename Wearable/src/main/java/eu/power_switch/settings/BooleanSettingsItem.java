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

package eu.power_switch.settings;

import android.content.Context;
import android.support.annotation.StringRes;

import com.mikepenz.iconics.IconicsDrawable;

import eu.power_switch.R;

/**
 * Created by Markus on 08.06.2016.
 */
public class BooleanSettingsItem extends SettingsItem<Boolean> {

    public BooleanSettingsItem(Context context, IconicsDrawable iconDrawable, @StringRes int description, String settingsKey, Boolean defaultValue) {
        super(context, iconDrawable, description, settingsKey, defaultValue);
    }

    @Override
    public String getValueDescription() {
        if (getValue()) {
            return context.getString(R.string.on);
        } else {
            return context.getString(R.string.off);
        }
    }

    /**
     * Toggle state of this setting
     */
    public void toggle() {
        setValue(!getValue());
    }
}

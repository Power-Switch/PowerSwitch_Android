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

package eu.power_switch.shared;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

/**
 * Created by Markus on 07.03.2016.
 */
public class ThemeHelper {

    /**
     * Get Color from Theme attribute
     *
     * @param context Activity context
     * @param attr    Attribute ressource ID
     *
     * @return Color as Int
     */
    @ColorInt
    public static int getThemeAttrColor(@NonNull Context context, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme()
                .resolveAttribute(attr, typedValue, true)) {
            if (typedValue.type >= TypedValue.TYPE_FIRST_INT && typedValue.type <= TypedValue.TYPE_LAST_INT) {
                return typedValue.data;
            } else if (typedValue.type == TypedValue.TYPE_STRING) {
                return ContextCompat.getColor(context, typedValue.resourceId);
            }
        }

        return 0;
    }
}

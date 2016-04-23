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

package eu.power_switch.obj.button;

import android.content.Context;

import eu.power_switch.R;
import eu.power_switch.shared.constants.DatabaseConstants;

/**
 * Created by Markus on 29.02.2016.
 */
public class UpButton extends Button {

    /**
     * ID of this Button
     */
    public static final long ID = DatabaseConstants.BUTTON_UP_ID;

    /**
     * Constructor
     *
     * @param context    any suitable context
     * @param receiverId ID of Receiver that this Button is associated with
     */
    public UpButton(Context context, Long receiverId) {
        super(ID, context.getString(R.string.up), receiverId);
    }
}

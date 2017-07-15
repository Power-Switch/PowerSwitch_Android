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
import eu.power_switch.persistence.PersistanceHandler;
import eu.power_switch.shared.constants.DatabaseConstants;
import lombok.Getter;
import lombok.ToString;
import timber.log.Timber;

/**
 * Represents a button that can be pressed on a receiver remote.
 * It is always associated with a receiverID.
 */
@ToString
@Getter
public class Button {

    /**
     * ID of this Button
     */
    private Long id;

    /**
     * Name of this Button
     */
    private String name;

    /**
     * ID of the receiver this Button is associated with
     */
    private Long receiverId;

    /**
     * Constructor
     *
     * @param id         ID of Button
     * @param name       name of Button
     * @param receiverId ID of Receiver that this Button is associated with
     */
    public Button(Long id, String name, Long receiverId) {
        this.id = id;
        this.name = name;
        this.receiverId = receiverId;
    }

    /**
     * Get the Name of a Button by ID
     *
     * @param buttonId ID of Button
     * @return Name of Button
     */
    public static String getName(Context context, PersistanceHandler persistanceHandler, Long buttonId) {
        try {
            if (buttonId == DatabaseConstants.BUTTON_ON_ID) {
                return context.getString(R.string.on);
            } else if (buttonId == DatabaseConstants.BUTTON_OFF_ID) {
                return context.getString(R.string.off);
            } else if (buttonId == DatabaseConstants.BUTTON_UP_ID) {
                return context.getString(R.string.up);
            } else if (buttonId == DatabaseConstants.BUTTON_STOP_ID) {
                return context.getString(R.string.stop);
            } else if (buttonId == DatabaseConstants.BUTTON_DOWN_ID) {
                return context.getString(R.string.down);
            } else {
                return persistanceHandler.getButton(buttonId)
                        .getName();
            }
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

}

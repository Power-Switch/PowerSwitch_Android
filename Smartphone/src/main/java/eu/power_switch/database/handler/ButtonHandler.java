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

package eu.power_switch.database.handler;

import eu.power_switch.R;
import eu.power_switch.obj.Button;

/**
 * Provides database methods for managing Buttons
 */
abstract class ButtonHandler {

    /**
     * Gets Button from Database
     *
     * @param id         ID of Button
     * @param receiverId ID of Receiver to find buttonId
     * @return Button
     */
    protected static Button getButton(Long id, Long receiverId) throws Exception {
        if (id == Button.BUTTON_ON_ID) {
            return new Button(Button.BUTTON_ON_ID, DatabaseHandler.context.getString(R.string.on), receiverId);
        } else if (id == Button.BUTTON_OFF_ID) {
            return new Button(Button.BUTTON_OFF_ID, DatabaseHandler.context.getString(R.string.off), receiverId);
        } else if (id == Button.BUTTON_UP_ID) {
            return new Button(Button.BUTTON_UP_ID, DatabaseHandler.context.getString(R.string.up), receiverId);
        } else if (id == Button.BUTTON_STOP_ID) {
            return new Button(Button.BUTTON_STOP_ID, DatabaseHandler.context.getString(R.string.stop), receiverId);
        } else if (id == Button.BUTTON_DOWN_ID) {
            return new Button(Button.BUTTON_DOWN_ID, DatabaseHandler.context.getString(R.string.down), receiverId);
        } else {
            return UniversalButtonHandler.getUniversalButton(id);
        }
    }

}
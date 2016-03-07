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

package eu.power_switch.obj;

import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;

/**
 * Represents a SceneItem
 * A SceneItem is associated with a Receiver and one of its buttons.
 */
public class SceneItem {

    /**
     * Associated Receiver
     */
    private Receiver receiver;

    /**
     * Associated Button
     */
    private Button activeButton;

    /**
     * Constructor
     *
     * @param receiver
     * @param activeButton
     */
    public SceneItem(Receiver receiver, Button activeButton) {
        this.receiver = receiver;
        this.activeButton = activeButton;
    }

    /**
     * Get associated Receiver
     *
     * @return Receiver
     */
    public Receiver getReceiver() {
        return receiver;
    }

    /**
     * Get associated Button
     *
     * @return Button
     */
    public Button getActiveButton() {
        return activeButton;
    }

    /**
     * Set associated Button
     *
     * @param button
     */
    public void setActiveButton(Button button) {
        this.activeButton = button;
    }

    @Override
    public String toString() {
        return "SceneItem(" +
                receiver.getName() +
                ":" +
                activeButton.getName() +
                ")";
    }
}
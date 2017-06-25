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
import lombok.Data;
import lombok.ToString;

/**
 * Created by Markus on 28.08.2015.
 */
@Data
@ToString
public class UniversalButton extends Button {

    /**
     * Network signal
     */
    private String signal;

    /**
     * Default Constructor
     *
     * @param id         ID of this UniversalButton
     * @param name       Name
     * @param receiverId ID of Receiver this UniversalButton is associated with
     * @param signal     Network signal that will be sent on button press
     */
    public UniversalButton(Long id, String name, Long receiverId, String signal) {
        super(id, name, receiverId);
        this.signal = signal;
    }

}
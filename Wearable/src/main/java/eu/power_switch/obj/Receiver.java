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

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a Receiver
 * <p/>
 * Created by Markus on 06.06.2015.
 */
@AllArgsConstructor
@Data
public class Receiver implements Serializable {

    /**
     * ID of this Receiver
     */
    private long id;

    /**
     * Name of this Receiver
     */
    private String name;

    /**
     * ID of the Room this Receiver is contained in
     */
    private long roomId;

    /**
     * List of all Buttons attached to this Receiver
     */
    private List<Button> buttons;

    /**
     * Position in Room
     */
    private int positionInRoom;

    /**
     * ID of last activated Button
     */
    private long lastActivatedButtonId;

    public void addButton(Button button) {
        buttons.add(button);
    }
}

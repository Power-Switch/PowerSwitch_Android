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

/**
 * Created by Markus on 28.08.2015.
 */
public class UniversalButton {

    /**
     * ID of this UniversalButton
     */
    private int id;
    /**
     * ID of Receiver this UniversalButton is associated with
     */
    private int receiverId;
    /**
     * Name
     */
    private String name;
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
    public UniversalButton(int id, String name, int receiverId, String signal) {
        this.id = id;
        this.name = name;
        this.receiverId = receiverId;
        this.signal = signal;
    }

    /**
     * Gets ID of this UniversalButton
     *
     * @return ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets Name of this UniversalButton
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets Receiver ID that this UniversalButton is associated with
     *
     * @return
     */
    public int getReceiverId() {
        return receiverId;
    }

    /**
     * Gets network signal of this UniversalButton
     *
     * @return network signal
     */
    public String getSignal() {
        return signal;
    }
}
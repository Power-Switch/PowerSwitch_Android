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
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a Receiver
 * <p/>
 * Created by Markus on 06.06.2015.
 */
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

    /**
     * Default constructor
     *
     * @param id
     * @param name
     * @param roomId ID of the Room this Receiver is contained in
     */
    public Receiver(long id, String name, long roomId, long lastActivatedButtonId, int positionInRoom) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
        this.buttons = new LinkedList<>();
        this.lastActivatedButtonId = lastActivatedButtonId;
        this.positionInRoom = positionInRoom;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public void addButton(Button button) {
        buttons.add(button);
    }

    public long getRoomId() {
        return roomId;
    }

    public long getLastActivatedButtonId() {
        return lastActivatedButtonId;
    }

    public void setLastActivatedButtonId(long lastActivatedButtonId) {
        this.lastActivatedButtonId = lastActivatedButtonId;
    }

    public int getPositionInRoom() {
        return positionInRoom;
    }
}

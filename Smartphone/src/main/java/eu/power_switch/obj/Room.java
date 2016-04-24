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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.log.LogHandler;

/**
 * Represents a room that can contain receivers
 */
public class Room {

    /**
     * ID of this Room
     */
    private Long id;

    /**
     * ID of Apartment this Room is contained in
     */
    private Long apartmentId;

    /**
     * Name of this Room
     */
    private String name;

    /**
     * List of all receivers that this room contains
     */
    private LinkedList<Receiver> receivers;

    /**
     * Position in apartment (list) of this Room
     */
    private Integer positionInApartment = -1;

    /**
     * Specifies if this room should be rendered collapsed
     */
    private boolean collapsed;

    /**
     * List of Gateways this Room is associated with
     */
    private List<Gateway> associatedGateways;

    /**
     * Constructor
     *
     * @param id          ID of this Room
     * @param apartmentId ID of Apartment
     * @param name        Name of this Room
     */
    public Room(Long id, Long apartmentId, String name, int positionInApartment, boolean isCollapsed, List<Gateway> associatedGateways) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.name = name;
        this.receivers = new LinkedList<>();
        this.positionInApartment = positionInApartment;
        this.collapsed = isCollapsed;
        this.associatedGateways = associatedGateways;
    }

    /**
     * Get all contained receivers
     *
     * @return Receiver list
     */
    public LinkedList<Receiver> getReceivers() {
        return receivers;
    }

    /**
     * Add Receiver to the contained receivers of this Room
     *
     * @param receiver Receiver
     */
    public void addReceiver(Receiver receiver) {
        receivers.add(receiver);
    }

    /**
     * Add a List of Receivers to the contained receivers of this Room
     *
     * @param receivers list of Receivers
     */
    public void addReceivers(List<Receiver> receivers) {
        this.receivers.addAll(receivers);
    }

    /**
     * Get ID of this Room
     *
     * @return ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Get ID of the Apartment this Room is located in
     *
     * @return ID of Apartment
     */
    public Long getApartmentId() {
        return apartmentId;
    }

    /**
     * Get name of this Room
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get Position of this Room in List
     *
     * @return position
     */
    public Integer getPositionInApartment() {
        return positionInApartment;
    }

    /**
     * Set Position of this Room in List
     *
     * @param positionInApartment position
     */
    public void setPositionInApartment(Integer positionInApartment) {
        this.positionInApartment = positionInApartment;
    }

    /**
     * Get collapsed state of this Room
     *
     * @return true if collapsed
     */
    public boolean isCollapsed() {
        return collapsed;
    }

    /**
     * Set collapsed state of this Room
     *
     * @param collapsed true if collapsed, false otherwise
     */
    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    /**
     * Get a list of Gateways this Room is associated with
     *
     * @return List of Gateways
     */
    @NonNull
    public List<Gateway> getAssociatedGateways() {
        return associatedGateways;
    }

    /**
     * Gets a specific Receiver in this Room
     *
     * @param name Name of Receiver
     * @return Receiver
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Receiver getReceiver(@Nullable String name) {
        for (Receiver receiver : receivers) {
            if (receiver.getName().equals(name)) {
                return receiver;
            }
        }
        throw new NoSuchElementException("Receiver \"" + name + "\" not found");
    }

    /**
     * Gets a specific Receiver in this Room, ignoring case
     *
     * @param name Name of Receiver
     * @return Receiver
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Receiver getReceiverCaseInsensitive(@Nullable String name) {
        for (Receiver receiver : receivers) {
            if (receiver.getName().equalsIgnoreCase(name)) {
                return receiver;
            }
        }
        throw new NoSuchElementException("Receiver \"" + name + "\" not found");
    }

    /**
     * Gets a specific Receiver in this Room
     *
     * @param id ID of Receiver
     * @return Receiver
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Receiver getReceiver(@Nullable Long id) {
        for (Receiver receiver : receivers) {
            if (receiver.getId().equals(id)) {
                return receiver;
            }
        }
        throw new NoSuchElementException("Receiver with ID \"" + id + "\" not found");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Room: ").
                append(getName())
                .append("(").append(getId()).append(")")
                .append(" {\n");

        for (Receiver receiver : getReceivers()) {
            stringBuilder.append(LogHandler.addIndentation(receiver.toString())).append("\n");
        }

        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}

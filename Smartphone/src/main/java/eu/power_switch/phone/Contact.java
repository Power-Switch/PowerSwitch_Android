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

package eu.power_switch.phone;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Class representing a Phone Contact
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class Contact {

    /**
     * Name of this Contact
     */
    private String name;

    /**
     * Phone number(s) of this Contact
     */
    private List<String> phoneNumbers;

    /**
     * Constructor
     *
     * @param name         Name
     * @param phoneNumbers list of phone numbers
     */
    public Contact(@NonNull String name, @NonNull List<String> phoneNumbers) {
        this.name = name;
        this.phoneNumbers = phoneNumbers;
    }

    /**
     * Get Name of this Contact
     *
     * @return name of this contact
     */
    public String getName() {
        return name;
    }

    /**
     * Get a list of phone numbers of this contact
     *
     * @return list of phone number (as text)
     */
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(name);
        stringBuilder.append(": ");
        for (String number : phoneNumbers) {
            stringBuilder.append(number).append(", ");
        }

        return stringBuilder.toString();
    }
}

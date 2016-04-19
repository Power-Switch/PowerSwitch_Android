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

package eu.power_switch.shared.constants;

import java.util.NoSuchElementException;

/**
 * Class holding constants related to Settings Call and SMS stuff
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class PhoneConstants {

    // Tabs
    public static final int CALL_TAB_INDEX = 0;
    public static final int SMS_TAB_INDEX = 1;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PhoneConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    public enum CallType {
        INCOMING(0),
        OUTGOING(1);

        private final int id;

        CallType(int id) {
            this.id = id;
        }

        public static CallType getById(int id) {
            for (CallType e : values()) {
                if (e.getId() == id) {
                    return e;
                }
            }
            throw new NoSuchElementException("No CallType with id: " + id);
        }

        public int getId() {
            return id;
        }
    }

    public enum SmsType {
        INCOMING(0),
        OUTGOING(1);

        private final int id;

        SmsType(int id) {
            this.id = id;
        }

        public static SmsType getById(int id) {
            for (SmsType e : values()) {
                if (e.getId() == id) {
                    return e;
                }
            }
            throw new NoSuchElementException("No SmsType with id: " + id);
        }

        public int getId() {
            return id;
        }
    }
}

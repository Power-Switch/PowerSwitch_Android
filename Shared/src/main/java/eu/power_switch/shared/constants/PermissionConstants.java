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

/**
 * Created by Markus on 22.12.2015.
 */
public class PermissionConstants {

    public static final String KEY_REQUEST_CODE = "requestCode";
    public static final String KEY_RESULTS = "results";

    public static final int REQUEST_CODE_STORAGE_PERMISSION = 123;
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 124;
    public static final int REQUEST_CODE_PHONE_PERMISSION = 125;
    public static final int REQUEST_CODE_SMS_PERMISSION = 126;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PermissionConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

}

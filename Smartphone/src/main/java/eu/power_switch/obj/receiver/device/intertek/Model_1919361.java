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

package eu.power_switch.obj.receiver.device.intertek;

import android.content.Context;

import java.util.LinkedList;

import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.device.elro.AB440SC;

public class Model_1919361 extends AB440SC implements DipReceiver {

    private static final String BRAND = Receiver.BRAND_INTERTEK;
    private static final String MODEL = Receiver.getModelName(Model_1919361.class.getCanonicalName());

    public Model_1919361(Context context, Long id, String name, LinkedList<Boolean> dips, Long roomId) {
        super(context, id, name, dips, roomId);
        brand = BRAND;
        model = MODEL;
    }
}

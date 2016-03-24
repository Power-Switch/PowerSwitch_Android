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

package eu.power_switch.obj.receiver.device.elro;

import android.content.Context;

import java.util.LinkedList;

import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;

public class AB440SC extends AB440ID implements DipReceiver {
    // dips are 12345 ABCDE
    // identical to "AB440ID" (but more dips)

    private static final String MODEL = Receiver.getModelName(AB440SC.class.getCanonicalName());

    public AB440SC(Context context, Long id, String name, LinkedList<Boolean> dips, Long roomId) {
        super(context, id, name, dips, roomId);
        dipList = new LinkedList<>();

        if (dips != null && dips.size() == 10) {
            dipList.add(new DipSwitch("1", dips.get(0)));
            dipList.add(new DipSwitch("2", dips.get(1)));
            dipList.add(new DipSwitch("3", dips.get(2)));
            dipList.add(new DipSwitch("4", dips.get(3)));
            dipList.add(new DipSwitch("5", dips.get(4)));
            dipList.add(new DipSwitch("A", dips.get(5)));
            dipList.add(new DipSwitch("B", dips.get(6)));
            dipList.add(new DipSwitch("C", dips.get(7)));
            dipList.add(new DipSwitch("D", dips.get(8)));
            dipList.add(new DipSwitch("E", dips.get(9)));
        } else {
            dipList.add(new DipSwitch("1", false));
            dipList.add(new DipSwitch("2", false));
            dipList.add(new DipSwitch("3", false));
            dipList.add(new DipSwitch("4", false));
            dipList.add(new DipSwitch("5", false));
            dipList.add(new DipSwitch("A", false));
            dipList.add(new DipSwitch("B", false));
            dipList.add(new DipSwitch("C", false));
            dipList.add(new DipSwitch("D", false));
            dipList.add(new DipSwitch("E", false));
        }

        model = MODEL;
    }

    @Override
    public String getSignal(Gateway gateway, String action) throws GatewayNotSupportedException, ActionNotSupportedException {
        return super.getSignal(gateway, action);
    }
}

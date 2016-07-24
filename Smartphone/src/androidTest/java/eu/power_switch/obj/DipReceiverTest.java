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

import java.util.LinkedList;
import java.util.List;

import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.DipSwitch;

/**
 * Created by Markus on 24.07.2016.
 */
public class DipReceiverTest extends ReceiverTest {

    protected void setDips(List<Boolean> dips) throws Exception {
        DipReceiver receiver = (DipReceiver) this.receiver;
        LinkedList<DipSwitch> receiverDips = receiver.getDips();

        if (dips == null) {
            throw new IllegalArgumentException("dips is null!");
        }

        if (receiverDips.size() != dips.size()) {
            throw new IllegalArgumentException("list sizes do not match! argument: " + dips.size() + " receiver: " + receiverDips.size());
        }

        for (int i = 0; i < receiverDips.size(); i++) {
            DipSwitch dip = receiverDips.get(i);
            dip.setChecked(dips.get(i));
        }
    }

}

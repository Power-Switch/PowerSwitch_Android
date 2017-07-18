/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.obj.receiver.device.unitec;

import android.content.Context;

import java.util.List;
import java.util.Random;

import eu.power_switch.R;
import eu.power_switch.obj.button.OffButton;
import eu.power_switch.obj.button.OnButton;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.Brand;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;

/**
 * Created by Markus on 14.04.2016.
 */
public class Unitec_EIM_209_48110 extends Receiver implements AutoPairReceiver {

    private static final Brand  BRAND = Brand.UNITEC;
    private static final String MODEL = Receiver.getModelName(Unitec_EIM_209_48110.class.getCanonicalName());

    private long seed = -1;

    public Unitec_EIM_209_48110(Context context, Long id, String name, long seed, Long roomId, List<Gateway> associatedGateways) {
        super(context, id, name, BRAND, MODEL, Type.AUTOPAIR, roomId, associatedGateways);

        buttons.add(new OnButton(context, id));
        buttons.add(new OffButton(context, id));

        if (seed == -1) {
            // init seed for this receiver instance, to always generate the same codes from now on
            Random ran = new Random();
            this.seed = ran.nextLong();
        } else {
            this.seed = seed;
        }
    }

    @Override
    public String getSignal(Gateway gateway, String action) throws GatewayNotSupportedException, ActionNotSupportedException {
        String lo = "1,";
        String hi = "2,";

        // unknown diff between on/off

        Random ran = new Random(seed);

        String signal = "";
        if (gateway instanceof ConnAir || gateway instanceof BrematicGWY433) {
//            signal += headAutoPairConnAir;
        } else if (gateway instanceof ITGW433) {
//            signal += headAutoPairITGW;
        } else {
            throw new GatewayNotSupportedException();
        }

        // action
        // not supported?
        if (action.equals(context.getString(R.string.unpair_all))) {

        } else if (action.equals(context.getString(R.string.on)) || action.equals(context.getString(R.string.pair))) {

        } else if (action.equals(context.getString(R.string.off)) || action.equals(context.getString(R.string.unpair))) {

        } else {
            throw new ActionNotSupportedException(action);
        }

        if (gateway instanceof ConnAir || gateway instanceof BrematicGWY433) {
//            signal += tailAutoPairConnAir;
        } else if (gateway instanceof ITGW433) {
//            signal += tailAutoPairITGW;
        }

        return signal;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }
}

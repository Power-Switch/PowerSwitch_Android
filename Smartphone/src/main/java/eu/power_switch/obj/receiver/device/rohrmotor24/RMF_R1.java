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

package eu.power_switch.obj.receiver.device.rohrmotor24;

import android.content.Context;

import java.util.Random;

import eu.power_switch.R;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;

/**
 * Created by Markus on 11.09.2015.
 */
public class RMF_R1 extends Receiver implements AutoPairReceiver {

    private static final Brand BRAND = Brand.ROHRMOTOR24;
    static String MODEL = Receiver.getModelName(RMF_R1.class.getCanonicalName());

    private String headAutoPairConnAir = "TXP:0,0,10,10920,91,41,57,18,";
    private String headAutoPairITGW = "TXP:0,0,10,10920,91,42,0,57,18,";

    private String tailAutoPairConnAir = "8,61;";
    private String tailAutoPairITGW = "8,120,0";

    private long seed = -1;

    public RMF_R1(Context context, Long id, String name, long seed, Long roomId) {
        super(context, id, name, BRAND, MODEL, Type.AUTOPAIR, roomId);
        buttons.add(new Button(Button.BUTTON_UP_ID, context.getString(R.string.up), id));
        buttons.add(new Button(Button.BUTTON_STOP_ID, context.getString(R.string.stop), id));
        buttons.add(new Button(Button.BUTTON_DOWN_ID, context.getString(R.string.down), id));
        if (seed == -1) {
            // init seed for this receiver instance, to always generate the same codes from now on
            Random ran = new Random();
            this.seed = ran.nextLong();
        } else {
            this.seed = seed;
        }
    }

    @Override
    protected String getSignal(Gateway gateway, String action) throws GatewayNotSupportedException, ActionNotSupportedException {
        String lo = "4,";
        String hi = "8,";
        String seqLo = lo + hi;
        String seqFl = hi + lo;
        String h = seqFl;
        String l = seqLo;
        String up = l + l + l + h + l + l + l;
        String stop = l + h + l + h + l + h + l;
        String down = l + l + h + h + l + l + h;

        Random ran = new Random(seed);

        String signal = "";
        if (gateway instanceof ConnAir || gateway instanceof BrematicGWY433) {
            signal += headAutoPairConnAir;
        } else if (gateway instanceof ITGW433) {
            signal += headAutoPairITGW;
        } else {
            throw new GatewayNotSupportedException();
        }

        // action
        if (action.equals(context.getString(R.string.pair))) {
            for (int i = 0; i < 32; i++) {
                if (ran.nextBoolean()) {
                    signal += h;
                } else {
                    signal += l;
                }
            }

            signal += h + h + l + l + h + h + l;
            if (gateway instanceof ConnAir || gateway instanceof BrematicGWY433) {
                signal += "4,61;";
                return signal;
            } else if (gateway instanceof ITGW433) {
                signal += "4,120,0";
                return signal;
            } else {
                throw new GatewayNotSupportedException();
            }

        } else if (action.equals(context.getString(R.string.up))) {
            for (int i = 0; i < 32; i++) {
                if (ran.nextBoolean()) {
                    signal += h;
                } else {
                    signal += l;
                }
            }

            signal += up;
        } else if (action.equals(context.getString(R.string.stop))) {
            for (int i = 0; i < 32; i++) {
                if (ran.nextBoolean()) {
                    signal += h;
                } else {
                    signal += l;
                }
            }

            signal += stop;
        } else if (action.equals(context.getString(R.string.down))) {
            for (int i = 0; i < 32; i++) {
                if (ran.nextBoolean()) {
                    signal += h;
                } else {
                    signal += l;
                }
            }

            signal += down;
        } else {
            throw new ActionNotSupportedException();
        }

        if (gateway instanceof ConnAir || gateway instanceof BrematicGWY433) {
            signal += tailAutoPairConnAir;
        } else if (gateway instanceof ITGW433) {
            signal += tailAutoPairITGW;
        }

        return signal;
    }

    @Override
    public long getSeed() {
        return seed;
    }
}

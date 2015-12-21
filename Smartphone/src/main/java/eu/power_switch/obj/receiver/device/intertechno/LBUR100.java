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

package eu.power_switch.obj.receiver.device.intertechno;

import android.content.Context;

import java.util.Random;

import eu.power_switch.R;
import eu.power_switch.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.exception.receiver.ActionNotSupportedException;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.Receiver;

public class LBUR100 extends Receiver implements AutoPairReceiver {

    static String BRAND = Receiver.BRAND_INTERTECHNO;
    static String MODEL = Receiver.getModelName(LBUR100.class.getCanonicalName());

    private String headAutoPairConnAir = "TXP:0,0,5,10976,98,66,3,29,";
    private String headAutoPairITGW = "0,0,5,10976,98,67,0,3,29,";

    private String tailAutoPairConnAir = "3,126";
    private String tailAutoPairITGW = "3,112,0";

    private long seed = -1;

    public LBUR100(Context context, Long id, String name, long seed, Long roomId) {
        super(context, id, name, BRAND, MODEL, TYPE_AUTOPAIR, roomId);
        buttons.add(new Button(Button.BUTTON_ON_ID, context.getString(R.string.on), id));
        buttons.add(new Button(Button.BUTTON_OFF_ID, context.getString(R.string.off), id));
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
        String lo = "3,";
        String hi = "15,";
        String seqLo = lo + lo + lo + hi;
        String seqFl = lo + hi + lo + lo;
        String h = seqFl;
        String l = seqLo;
        String on = seqLo + seqLo + seqFl;
        String off = seqLo + seqLo + seqLo;
        String additional = seqLo + seqLo;

        Random ran = new Random(seed);

        String signal = "";
        if (gateway.getClass() == ConnAir.class || gateway.getClass() == BrematicGWY433.class) {
            signal += headAutoPairConnAir;
        } else if (gateway.getClass() == ITGW433.class) {
            signal += headAutoPairITGW;
        } else {
            throw new GatewayNotSupportedException();
        }

        // action
        // not supported?
        if (action.equals(context.getString(R.string.unpair_all))) {

            for (int i = 0; i < 24; i++) {
                signal += l;
            }

            signal += l + l + h + l;
            signal += l + l + l + l;

        } else if (action.equals(context.getString(R.string.on)) || action.equals(context.getString(R.string.pair))) {

            signal += h;

            for (int i = 0; i < 24; i++) {
                if (ran.nextBoolean()) {
                    signal += h;
                } else {
                    signal += l;
                }
            }

            signal += on;

            for (int i = 0; i < 2; i++) {
                if (ran.nextBoolean()) {
                    signal += h;
                } else {
                    signal += l;
                }
            }

            signal += additional;

        } else if (action.equals(context.getString(R.string.off)) || action.equals(context.getString(R.string.unpair))) {

            signal += h;

            for (int i = 0; i < 24; i++) {
                if (ran.nextBoolean()) {
                    signal += h;
                } else {
                    signal += l;
                }
            }

            signal += off;

            for (int i = 0; i < 2; i++) {
                if (ran.nextBoolean()) {
                    signal += h;
                } else {
                    signal += l;
                }
            }

            signal += additional;
        } else {
            throw new ActionNotSupportedException();
        }

        if (gateway.getClass() == ConnAir.class || gateway.getClass() == BrematicGWY433.class) {
            signal += tailAutoPairConnAir;
        } else if (gateway.getClass() == ITGW433.class) {
            signal += tailAutoPairITGW;
        }

        return signal;
    }

    @Override
    public long getSeed() {
        return seed;
    }
}
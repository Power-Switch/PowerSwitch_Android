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

package eu.power_switch.obj.receiver.device.bat;

import android.content.Context;

import java.util.Random;

import eu.power_switch.R;
import eu.power_switch.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.exception.receiver.ActionNotSupportedException;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.Receiver;

/**
 * Created by Markus on 11.09.2015.
 */
public class RC_AAA3680_A_IP20 extends Receiver implements AutoPairReceiver {

    private static final String BRAND = Receiver.BRAND_BAT;
    private static final String MODEL = Receiver.getModelName(RC_AAA3680_A_IP20.class.getCanonicalName());

    private String headAutoPairConnAir = "TXP:0,0,6,0,505,25,6,14,";
    private String headAutoPairITGW = "TXP:0,0,6,0,505,26,0,6,14,";

    private String tailAutoPairConnAir = "1,2;";
    private String tailAutoPairITGW = "1,2,0";

    private long seed = -1;

    public RC_AAA3680_A_IP20(Context context, Long id, String name, long seed, Long roomId) {
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
        boolean actionSupported = false;
        for (Button button : buttons) {
            if (button.getName().equals(action)) {
                actionSupported = true;
                break;
            }
        }
        if (!actionSupported) {
            throw new ActionNotSupportedException();
        }

        String lo = "1,";
        String hi = "2,";
        String seqLo = lo + hi;
        String seqFl = hi + lo;
        String h = seqFl;
        String l = seqLo;

        // On/Off nicht einfach berechenbar
        String on = "";
        String off = "";


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
        if (action.equals(context.getString(R.string.pair))) {
        } else if (action.equals(context.getString(R.string.on))) {
            // TODO
        } else if (action.equals(context.getString(R.string.off))) {
            // TODO
        } else {
            throw new ActionNotSupportedException();
        }

        if (gateway.getClass() == ConnAir.class || gateway.getClass() == BrematicGWY433.class) {
            signal += tailAutoPairConnAir;
        } else if (gateway.getClass() == ITGW433.class) {
            signal += tailAutoPairITGW;
        } else {
            throw new GatewayNotSupportedException();
        }

        return signal;
    }

    @Override
    public long getSeed() {
        return seed;
    }
}

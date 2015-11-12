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

package eu.power_switch.obj.device.brennenstuhl;

import android.content.Context;

import eu.power_switch.R;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.device.AutoPairReceiver;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.obj.gateway.Gateway;

public class RC3600 extends Receiver implements AutoPairReceiver {

    static String BRAND = Receiver.BRAND_BRENNENSTUHL;
    static String MODEL = Receiver.getModelName(RC3600.class.getCanonicalName());

    private long seed = -1;

    public RC3600(Context context, Long id, String name, long seed, Long roomId) {
        super(context, id, name, BRAND, MODEL, TYPE_AUTOPAIR, roomId);
        buttons.add(new Button(Button.BUTTON_ON_ID, context.getString(R.string.on), id));
        buttons.add(new Button(Button.BUTTON_OFF_ID, context.getString(R.string.off), id));
        this.seed = seed;
    }

    @Override
    protected String getSignal(Gateway gateway, String string) {
        return null;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    // // TODO: uses other Sequences for Low and High
    // // can be paired automatically
    //
    // /*
    // * Pair = On On: TXP:0,0,10,3060,510,25,
    // * 2,1,1,2,1,2,2,1,1,2,2,1,1,2,1,2,1,
    // * 2,1,2,2,1,1,2,1,2,2,1,1,2,2,
    // * 1,2,1,1,2,2,1,1,2,1,2,1,2,2,1,2,1, 6,7,;
    // *
    // * Off: TXP:0,0,10,3060,510,25,
    // * 2,1,1,2,1,2,2,1,2,1,2,1,1,2,1,2,1
    // * ,2,1,2,2,1,2,1,2,1,2,1,2,1,2,
    // * 1,1,2,2,1,1,2,1,2,1,2,1,2,2,1,2,1, 6,7,;
    // *
    // * angenommen: 0 = lo = 1,2, 1 = hi = 2,1,
    // *
    // * On: TXP:0,0,10,3060,510,25, 1001 0100 0010 0101 1010 0011
    // * 6,7,;
    // *
    // * Off: TXP:0,0,10,3060,510,25, 1001 1100 0011 1111 0100 0011
    // * 6,7,;
    // *
    // * sieht schon ziemlich gut aus, ich teste noch ein anderes
    // * pairing paar um m�glicherweise ein Muster erkennen zu
    // * k�nnen
    // *
    // *
    // * On: TXP:0,0,10,3060,510,25,
    // * 2,1,1,2,1,2,2,1,2,1,2,1,2,1,2,1,2,
    // * 1,1,2,1,2,2,1,2,1,2,1,1,2,1,
    // * 2,1,2,1,2,2,1,1,2,1,2,2,1,2,1,2,1, 6,7,;
    // *
    // * Off: TXP:0,0,10,3060,510,25,
    // * 2,1,1,2,1,2,2,1,2,1,2,1,2,1,1,2,1
    // * ,2,2,1,1,2,2,1,1,2,2,1,2,1,2,
    // * 1,1,2,2,1,2,1,2,1,1,2,2,1,2,1,2,1, 6,7,;
    // *
    // * angenommen: 0 = lo = 1,2, 1 = hi = 2,1,
    // *
    // * On: 1001 1111 1001 1100 0010 0111
    // *
    // * Off: 1001 1110 0101 0111 0111 0111
    // *
    // * Zum vergleichen: 1001 0100 0010 0101 1010 0011 On 1001 1100
    // * 0011 1111 0100 0011 Off
    // *
    // * 1001 1111 1001 1100 0010 0111 On 1001 1110 0101 0111 0111
    // * 0111 Off
    // *
    // * Ich komm nicht dahinter, wie die Codes aufgebaut sind. Man
    // * sollte irgendwie zwischen dem On und dem Off Signal
    // * unterscheiden k�nnen.
    // */
    // canBePaired = true;
    // hasDips = true;
    //
    // lo = "1,";
    // hi = "2,";
    //
    // sA = "0,";
    // sG = "0,";
    // sRepeat = "10,";
    // sPause = "3060,";
    // sTune = "510,";
    // sBaud = "25,";
    //
    // tx433version = "6,";
    // sSpeed = "7,";
    //
    // seqLo = lo + hi + lo + hi;
    // seqFl = lo + hi + hi + lo;
    //
    // h = seqFl;
    // l = seqLo;
    //
    // on = seqFl + seqFl;
    // off = seqFl + seqLo;
    // additional = seqLo + seqFl;
    //
    // buttonStyle = twoButtons;

}

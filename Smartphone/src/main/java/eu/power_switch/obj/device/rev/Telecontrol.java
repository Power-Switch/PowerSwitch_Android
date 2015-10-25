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

package eu.power_switch.obj.device.rev;

import android.content.Context;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.exception.receiver.ActionNotSupportedException;
import eu.power_switch.log.Log;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.device.MasterSlaveReceiver;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;

public class Telecontrol extends Receiver implements MasterSlaveReceiver {

    static String BRAND = Receiver.BRAND_REV;
    static String MODEL = Receiver.getModelName(Telecontrol.class.getCanonicalName());

    private String tx433version = "1,";

    private String sSpeedConnAir = "16";
    private String headConnAir = "TXP:0,0,10,5600,350,25,";
    private String tailConnAir = tx433version + sSpeedConnAir + ";";

    private String sSpeedITGW = "32,";
    private String headITGW = "0,0,10,11200,350,26,0,";
    private String tailITGW = tx433version + sSpeedITGW + "0";

    private Character channelMaster;
    private int channelSlave;

    public Telecontrol(Context context, long id, String name, char channelMaster, int channelSlave, long roomId) {
        super(context, id, name, BRAND, MODEL, TYPE_MASTER_SLAVE, roomId);
        buttons.add(new Button(Button.BUTTON_ON_ID, context.getString(R.string.on), id));
        buttons.add(new Button(Button.BUTTON_OFF_ID, context.getString(R.string.off), id));
        this.channelMaster = channelMaster;
        this.channelSlave = channelSlave;
    }

    public ArrayList<String> getMasterNames() {
        ArrayList<String> m = new ArrayList<>();
        m.add("A");
        m.add("B");
        m.add("C");
        m.add("D");
        return m;
    }

    public ArrayList<String> getSlaveNames() {
        ArrayList<String> s = new ArrayList<>();
        s.add("1");
        s.add("2");
        s.add("3");
        return s;
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
        String hi = "3,";
        String seqHi = hi + lo + hi + lo;
        String seqLo = lo + hi + lo + hi;
        String seqFl = lo + hi + hi + lo;
        String h = seqFl;
        String l = seqHi;
        String on = seqFl + seqFl;
        String off = seqLo + seqLo;
        String additional = seqLo + seqFl + seqFl;

        // switch channelMaster (character)
        String master = "";
        switch (channelMaster) {
            case 'A':
                master = l + h + h + h;
                break;
            case 'B':
                master = h + l + h + h;
                break;
            case 'C':
                master = h + h + l + h;
                break;
            case 'D':
                master = h + h + h + l;
                break;
            default:
                Log.e("Switch", "No Matching Master");
                break;
        }

        // switch channelSlave (number)
        String slave = "";
        switch (channelSlave) {
            case 1:
                slave = l + h + h;
                break;
            case 2:
                slave = h + l + h;
                break;
            case 3:
                slave = h + h + l;
                break;
            default:
                Log.e("Switch", "No Matching Slave");
                break;
        }

        if (gateway.getClass() == ConnAir.class) {
            if (action.equals(context.getString(R.string.on))) {
                String ON = headConnAir + master + slave + additional + on + tailConnAir;
                return ON;
            } else {
                String OFF = headConnAir + master + slave + additional + off + tailConnAir;
                return OFF;
            }
        } else if (gateway.getClass() == BrematicGWY433.class) {
            if (action.equals(context.getString(R.string.on))) {
                String ON = headConnAir + master + slave + additional + on + tailConnAir;
                return ON;
            } else {
                String OFF = headConnAir + master + slave + additional + off + tailConnAir;
                return OFF;
            }
        } else if (gateway.getClass() == ITGW433.class) {
            if (action.equals(context.getString(R.string.on))) {
                String ON = headITGW + master + slave + additional + on + tailITGW;
                return ON;
            } else {
                String OFF = headITGW + master + slave + additional + off + tailITGW;
                return OFF;
            }
        } else {
            throw new GatewayNotSupportedException();
        }
    }

    @Override
    public char getMaster() {
        return channelMaster;
    }

    @Override
    public int getSlave() {
        return channelSlave;
    }
}

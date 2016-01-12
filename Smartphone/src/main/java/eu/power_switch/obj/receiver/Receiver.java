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

package eu.power_switch.obj.receiver;

import android.content.Context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import eu.power_switch.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.exception.receiver.ActionNotSupportedException;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.gateway.Gateway;

/**
 * Represents any kind of device that can receive network signals
 */
public abstract class Receiver {

    /**
     * Map <Receiver Model> -> <ClassName>
     * Used to reconstruct Receiver from Database
     */
    public static final Map<String, String> receiverMap = new HashMap<String, String>() {
        private static final long serialVersionUID = -5100907062901850003L;

        {
            // BAT
            put("RC 3500-A IP44 DE", "eu.power_switch.obj.receiver.device.bat.RC3500_A_IP44_DE");
            put("RC AAA1000-A IP44 Outdoor", "eu.power_switch.obj.receiver.device.bat.RC_AAA1000_A_IP44_Outdoor");
            put("RC AAA3680-A IP20", "eu.power_switch.obj.receiver.device.bat.RC_AAA3680_A_IP20");
            // Brennenstuhl
            put("RC 3600", "eu.power_switch.obj.receiver.device.brennenstuhl.RC3600");
            put("RCS 1000 N Comfort", "eu.power_switch.obj.receiver.device.brennenstuhl.RCS1000NComfort");
            put("RCS 1044 N Comfort", "eu.power_switch.obj.receiver.device.brennenstuhl.RCS1044NComfort");
            // Elro
            put("AB440D 200W", "eu.power_switch.obj.receiver.device.elro.AB440D_200W");
            put("AB440D 300W", "eu.power_switch.obj.receiver.device.elro.AB440D_300W");
            put("AB440ID", "eu.power_switch.obj.receiver.device.elro.AB440ID");
            put("AB440IS", "eu.power_switch.obj.receiver.device.elro.AB440IS");
            put("AB440L", "eu.power_switch.obj.receiver.device.elro.AB440L");
            put("AB440S", "eu.power_switch.obj.receiver.device.elro.AB440S");
            put("AB440SC", "eu.power_switch.obj.receiver.device.elro.AB440SC");
            put("AB440WD", "eu.power_switch.obj.receiver.device.elro.AB440WD");
            // Hama
            put("Hama", "eu.power_switch.obj.receiver.device.hama.Hama");
            // Intertechno
            put("CMR 300", "eu.power_switch.obj.receiver.device.intertechno.CMR300");
            put("CMR 500", "eu.power_switch.obj.receiver.device.intertechno.CMR500");
            put("CMR 1000", "eu.power_switch.obj.receiver.device.intertechno.CMR1000");
            put("CMR 1224", "eu.power_switch.obj.receiver.device.intertechno.CMR1224");
            put("GRR 300", "eu.power_switch.obj.receiver.device.intertechno.GRR300");
            put("GRR 3500", "eu.power_switch.obj.receiver.device.intertechno.GRR3500");
            put("IT 1500", "eu.power_switch.obj.receiver.device.intertechno.IT1500");
            put("IT 2300", "eu.power_switch.obj.receiver.device.intertechno.IT2300");
            put("ITDL 1000", "eu.power_switch.obj.receiver.device.intertechno.ITDL1000");
            put("ITDM 250", "eu.power_switch.obj.receiver.device.intertechno.ITDM250");
            put("ITL 150", "eu.power_switch.obj.receiver.device.intertechno.ITL150");
            put("ITL 210", "eu.power_switch.obj.receiver.device.intertechno.ITL210");
            put("ITL 230", "eu.power_switch.obj.receiver.device.intertechno.ITL230");
            put("ITL 250", "eu.power_switch.obj.receiver.device.intertechno.ITL250");
            put("ITL 300", "eu.power_switch.obj.receiver.device.intertechno.ITL300");
            put("ITL 500", "eu.power_switch.obj.receiver.device.intertechno.ITL500");
            put("ITL 1000", "eu.power_switch.obj.receiver.device.intertechno.ITL1000");
            put("ITL 3500", "eu.power_switch.obj.receiver.device.intertechno.ITL3500");
            put("ITLR 300", "eu.power_switch.obj.receiver.device.intertechno.ITLR300");
            put("ITLR 3500", "eu.power_switch.obj.receiver.device.intertechno.ITLR3500");
            put("ITLR 3500T", "eu.power_switch.obj.receiver.device.intertechno.ITLR3500T");
            put("ITR 300", "eu.power_switch.obj.receiver.device.intertechno.ITR300");
            put("ITR 1500", "eu.power_switch.obj.receiver.device.intertechno.ITR1500");
            put("ITR 3500", "eu.power_switch.obj.receiver.device.intertechno.ITR3500");
            put("ITR 7000", "eu.power_switch.obj.receiver.device.intertechno.ITR7000");
            put("ITWR 3500", "eu.power_switch.obj.receiver.device.intertechno.ITWR3500");
            put("LBUR 100", "eu.power_switch.obj.receiver.device.intertechno.LBUR100");
            put("PA3 1000", "eu.power_switch.obj.receiver.device.intertechno.PA3_1000");
            put("PAR 1500", "eu.power_switch.obj.receiver.device.intertechno.PAR_1500");
            put("YCR 1000", "eu.power_switch.obj.receiver.device.intertechno.YCR1000");
            // Mumbi
            put("m-FS300", "eu.power_switch.obj.receiver.device.mumbi.m_FS300");
            // Pollin
            put("Set 2605", "eu.power_switch.obj.receiver.device.pollin_electronic.Set2605");
            // Rev
            put("Ritter", "eu.power_switch.obj.receiver.device.rev.Ritter");
            put("Telecontrol", "eu.power_switch.obj.receiver.device.rev.Telecontrol");
            // Rohrmotor 24
            put("RMF Motor", "eu.power_switch.obj.receiver.device.rohrmotor24.RMF_Motor");
            put("RMF-R1", "eu.power_switch.obj.receiver.device.rohrmotor24.RMF_R1");
            put("RMF-R1 UP", "eu.power_switch.obj.receiver.device.rohrmotor24.RMF_R1_UP");
            // Vivanco
            put("FSS 31000W", "eu.power_switch.obj.receiver.device.vivanco.FSS31000W");
            put("FSS 33600W", "eu.power_switch.obj.receiver.device.vivanco.FSS33600W");
            // Universal
            put("Universal", "eu.power_switch.obj.receiver.UniversalReceiver");
        }
    };

    /**
     * Brand constants
     */
    public static final String BRAND_BAT = "BAT";
    public static final String BRAND_BRENNENSTUHL = "Brennenstuhl";
    public static final String BRAND_ELRO = "Elro";
    public static final String BRAND_HAMA = "Hama";
    public static final String BRAND_INTERTECHNO = "Intertechno";
    public static final String BRAND_MUMBI = "Mumbi";
    public static final String BRAND_POLLIN_ELECTRONIC = "Pollin Electronic";
    public static final String BRAND_REV = "REV";
    public static final String BRAND_ROHRMOTOR24 = "Rohrmotor 24";
    public static final String BRAND_UNIVERSAL = "Universal";
    public static final String BRAND_VIVANCO = "Vivanco";

    /**
     * Type constants
     */
    public static final String TYPE_DIPS = "Dips";
    public static final String TYPE_MASTER_SLAVE = "MasterSlave";
    public static final String TYPE_UNIVERSAL = "Universal";
    public static final String TYPE_AUTOPAIR = "AutoPair";

    protected Context context;

    protected Long id;
    protected String name;
    protected String brand;
    protected String model;
    protected LinkedList<Button> buttons;
    protected Long roomId;
    protected String type;
    protected Integer positionInRoom = -1;
    protected Long lastActivatedButtonId = (long) -1;

    public Receiver(Context context, Long id, String name, String brand, String model, String type, Long roomId) {
        this.context = context;
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.buttons = new LinkedList<>();
        this.type = type;
        this.roomId = roomId;
    }

    public static String getJavaPath(String model) {
        return receiverMap.get(model);
    }

    public static String getModelName(String javaPath) {
        Set<Map.Entry<String, String>> entrySet = receiverMap.entrySet();

        Iterator<Map.Entry<String, String>> itr = entrySet.iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String value = entry.getValue();
            if (value.equals(javaPath)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public LinkedList<Button> getButtons() {
        return buttons;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getType() {
        return type;
    }

    public Integer getPositionInRoom() {
        return positionInRoom;
    }

    public void setPositionInRoom(int positionInRoom) {
        this.positionInRoom = positionInRoom;
    }

    public Long getLastActivatedButtonId() {
        return lastActivatedButtonId;
    }

    public void setLastActivatedButtonId(Long lastActivatedButtonId) {
        this.lastActivatedButtonId = lastActivatedButtonId;
    }

    public NetworkPackage getNetworkPackage(Gateway gateway, String action) throws GatewayNotSupportedException, ActionNotSupportedException {
        String signal = getSignal(gateway, action);
        return new NetworkPackage(gateway.getHost(), gateway.getPort(), signal, gateway.getTimeout());
    }

    protected abstract String getSignal(Gateway gateway, String action) throws GatewayNotSupportedException, ActionNotSupportedException;

    public Button getButton(String name) {
        for (Button button : buttons) {
            if (button.getName().equals(name)) {
                return button;
            }
        }
        return null;
    }
}

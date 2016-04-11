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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;
import eu.power_switch.shared.log.Log;

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
            //Intertek
            put("Model 1919361", "eu.power_switch.obj.receiver.device.intertek.Model_1919361");
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
            put("Custom Buttons", "eu.power_switch.obj.receiver.UniversalReceiver");
            put("HX2262 Compat.", "eu.power_switch.obj.receiver.device.universal.HX2262_Comp");
        }
    };

    protected Context context;
    /**
     * ID of this Receiver
     */
    protected Long id;
    /**
     * Name of this Receiver
     */
    protected String name;
    /**
     * Brand of this Receiver
     */
    protected Brand brand;
    /**
     * Model of this Receiver
     */
    protected String model;
    /**
     * Buttons of this Receiver
     */
    protected LinkedList<Button> buttons;
    /**
     * Room ID of this Receiver
     */
    protected Long roomId;
    /**
     * Type of this Receiver {@see TYPE}
     */
    protected Type type;

    /**
     * Position in room (list) of this Receiver
     */
    protected Integer positionInRoom = -1;

    /**
     * ID of last activated Button of this Receiver
     */
    protected Long lastActivatedButtonId = (long) -1;

    /**
     * Constructor
     *
     * @param context any suitable context
     * @param id      ID of this Receiver
     * @param name    Name of this Receiver
     * @param brand   Brand of this Receiver
     * @param model   Model of this Receiver
     * @param type    Type of this Receiver {@see TYPE}
     * @param roomId  Room ID of this Receiver
     */
    public Receiver(Context context, Long id, String name, Brand brand, String model, Type type, Long roomId) {
        this.context = context;
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.buttons = new LinkedList<>();
        this.type = type;
        this.roomId = roomId;
    }

    /**
     * Get java classpath of corresponding Class by Model Name
     *
     * @param model Receiver Model
     * @return Java Classpath
     */
    public static String getJavaPath(String model) {
        return receiverMap.get(model);
    }

    /**
     * Get Model name by Java Classpath
     *
     * @param javaPath Classpath
     * @return Model name
     */
    @Nullable
    public static String getModelName(@NonNull String javaPath) {
        Set<Map.Entry<String, String>> entrySet = receiverMap.entrySet();

        for (Map.Entry<String, String> entry : entrySet) {
            String value = entry.getValue();
            if (value.equals(javaPath)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get ID of this Receiver
     *
     * @return ID of this Receiver
     */
    public Long getId() {
        return id;
    }

    /**
     * Get Name of this Receiver
     *
     * @return Name of this Receiver
     */
    public String getName() {
        return name;
    }

    /**
     * Get Brand of this Receiver
     *
     * @return Brand of this receiver
     */
    public Brand getBrand() {
        return brand;
    }

    /**
     * Get Model of this Receiver
     *
     * @return Model of this Receiver
     */
    public String getModel() {
        return model;
    }

    /**
     * Get all Buttons of this Receiver
     *
     * @return List of Buttons
     */
    public LinkedList<Button> getButtons() {
        return buttons;
    }

    /**
     * Get ID of Room this Receiver is associated with
     *
     * @return ID of Room
     */
    public Long getRoomId() {
        return roomId;
    }

    /**
     * Get Type of this Receiver
     *
     * @return Type of this Receiver
     */
    public Type getType() {
        return type;
    }

    /**
     * Get Position in Room of this Receiver
     *
     * @return position in room
     */
    public Integer getPositionInRoom() {
        return positionInRoom;
    }

    /**
     * Set Position in Room of this Receiver
     *
     * @param positionInRoom position in room
     */
    public void setPositionInRoom(int positionInRoom) {
        this.positionInRoom = positionInRoom;
    }

    /**
     * Get ID of last activated Button
     *
     * @return ID of last activated Button
     */
    public Long getLastActivatedButtonId() {
        return lastActivatedButtonId;
    }

    /**
     * Set ID of last activated Button
     *
     * @param lastActivatedButtonId ID of last activated Button
     */
    public void setLastActivatedButtonId(Long lastActivatedButtonId) {
        this.lastActivatedButtonId = lastActivatedButtonId;
    }

    /**
     * Get NetworkPackage for a Gateway/Action combination for this Receiver
     *
     * @param gateway Gateway
     * @param action  Action name (Button name)
     * @return NetworkPackage
     * @throws GatewayNotSupportedException thrown if this Receiver doesn't support the given Gateway
     * @throws ActionNotSupportedException  thrown if this Receiver doesn't support the given Action
     */
    public NetworkPackage getNetworkPackage(Gateway gateway, String action) throws GatewayNotSupportedException, ActionNotSupportedException {
        String signal = getSignal(gateway, action);
        if (gateway.hasValidLocalAddress()) {

            if (NetworkHandler.isWifiConnected()) {
                Log.d("Using local address");
                return new NetworkPackage(gateway.getCommunicationType(), gateway.getLocalHost(), gateway.getLocalPort(), signal,
                        gateway.getTimeout());
            } else {
                Log.d("Using WAN address");
                return new NetworkPackage(gateway.getCommunicationType(), gateway.getWanHost(), gateway.getWanPort(), signal,
                        gateway.getTimeout());
            }
        } else {
            Log.d("Using WAN address");
            return new NetworkPackage(gateway.getCommunicationType(), gateway.getWanHost(), gateway.getWanPort(), signal,
                    gateway.getTimeout());
        }
    }

    /**
     * Get network signal for a given Gateway/Action combination
     *
     * @param gateway
     * @param action
     * @return Network signal
     * @throws GatewayNotSupportedException thrown if this Receiver doesn't support the given Gateway
     * @throws ActionNotSupportedException  thrown if this Receiver doesn't support the given Action
     */

    public abstract String getSignal(Gateway gateway, String action) throws GatewayNotSupportedException, ActionNotSupportedException;

    /**
     * Get a Button of this Receiver by its name
     *
     * @param name Name of Button
     * @return Button or null
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Button getButton(@Nullable String name) {
        for (Button button : buttons) {
            if (button.getName().equals(name)) {
                return button;
            }
        }
        throw new NoSuchElementException("Button \"" + name + "\" not found");
    }

    /**
     * Get a Button of this Receiver by its name, ignoring case
     *
     * @param name Name of Button
     * @return Button or null
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Button getButtonCaseInsensitive(@Nullable String name) {
        for (Button button : buttons) {
            if (button.getName().equalsIgnoreCase(name)) {
                return button;
            }
        }
        throw new NoSuchElementException("Button \"" + name + "\" not found");
    }

    /**
     * Get a Button of this Receiver by its ID
     *
     * @param id ID of Button
     * @return Button
     * @throws NoSuchElementException if no element was not found
     */
    @NonNull
    public Button getButton(@Nullable Long id) {
        for (Button button : buttons) {
            if (button.getId().equals(id)) {
                return button;
            }
        }
        throw new NoSuchElementException("Button with ID \"" + id + "\" not found");
    }

    @Override
    public String toString() {
        return getBrand().toString() + " " + getModel() + ": " + getName() + "(" + getId() + ")";
    }

    /**
     * Type constants
     */
    public enum Type {
        DIPS("Dips"),
        MASTER_SLAVE("MasterSlave"),
        UNIVERSAL("Universal"),
        AUTOPAIR("AutoPair");

        private String name;

        Type(String name) {
            this.name = name;
        }

        /**
         * Get enum from string representation
         *
         * @param name name of enum
         * @return enum
         */
        public static Type getEnum(String name) {
            for (Type v : values()) {
                if (v.toString().equalsIgnoreCase(name)) {
                    return v;
                }
            }

            return valueOf(name);
        }

        public String getName() {
            return name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            if (name != null) {
                return getName();
            } else {
                return super.toString();
            }
        }
    }

    /**
     * Brand constants
     */
    public enum Brand {
        BAT("BAT"),
        BRENNENSTUHL("Brennenstuhl"),
        ELRO("Elro"),
        HAMA("Hama"),
        INTERTECHNO("Intertechno"),
        INTERTEK("Intertek"),
        MUMBI("Mumbi"),
        POLLIN_ELECTRONIC("Pollin Electronic"),
        REV("REV"),
        ROHRMOTOR24("Rohrmotor 24"),
        UNIVERSAL("Universal"),
        VIVANCO("Vivanco");

        private String name;

        Brand(String name) {
            this.name = name;
        }

        /**
         * Get enum from string representation
         *
         * @param name name of enum
         * @return enum
         */
        public static Brand getEnum(String name) {
            for (Brand v : values()) {
                if (v.toString().equalsIgnoreCase(name)) {
                    return v;
                }
            }

            return valueOf(name);
        }

        /**
         * Get Name of this Model
         *
         * @return
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            if (name != null) {
                return getName();
            } else {
                return super.toString();
            }
        }
    }

}

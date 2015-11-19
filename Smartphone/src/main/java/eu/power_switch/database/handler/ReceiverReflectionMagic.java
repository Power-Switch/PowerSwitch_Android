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

package eu.power_switch.database.handler;

import android.content.Context;
import android.database.Cursor;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.device.AutoPairReceiver;
import eu.power_switch.obj.device.DipReceiver;
import eu.power_switch.obj.device.MasterSlaveReceiver;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.obj.device.UniversalReceiver;
import eu.power_switch.shared.log.Log;

/**
 *
 */
public abstract class ReceiverReflectionMagic {

    /**
     * Creates a Receiver based on a database cursor.
     *
     * @param cursor  A database cursor with one element.
     * @param context The application context for all database operations.
     * @return The complete Receiver object.
     */
    public static Receiver fromDatabase(Context context, Cursor cursor) {
        Long id = cursor.getLong(0);
        String name = cursor.getString(1);
        String model = cursor.getString(2);
        String type = cursor.getString(3);
        String className = cursor.getString(4);
        Long roomId = cursor.getLong(5);

        int positionInRoom = -1;
        if (!cursor.isNull(6)) {
            positionInRoom = cursor.getInt(6);
        }

        Long lastActivatedButtonId = cursor.getLong(7);

        Receiver receiver = null;

        try {
            Constructor<?> constructor = getConstructor(className, type);

            if (type.equals(Receiver.TYPE_DIPS)) {
                LinkedList<Boolean> dips = DipHandler.getDips(id);
                receiver = (Receiver) constructor.newInstance(context, id, name, dips, roomId);
            } else if (type.equals(Receiver.TYPE_MASTER_SLAVE)) {
                Character channelMaster = MasterSlaveReceiverHandler.getMaster(id);
                int channelSlave = MasterSlaveReceiverHandler.getSlave(id);
                receiver = (Receiver) constructor.newInstance(context, id, name, channelMaster, channelSlave, roomId);
            } else if (type.equals(Receiver.TYPE_AUTOPAIR)) {
                long seed = AutoPairHandler.getSeed(id);
                receiver = (Receiver) constructor.newInstance(context, id, name, seed, roomId);
            } else if (type.equals(Receiver.TYPE_UNIVERSAL)) {
                List<UniversalButton> buttons = UniversalButtonHandler.getUniversalButtons(id);
                receiver = (Receiver) constructor.newInstance(context, id, name, buttons, roomId);
            }

        } catch (Exception e) {
            Log.e(e);
            e.printStackTrace();
        }

        if (receiver != null) {
            receiver.setPositionInRoom(positionInRoom);
            receiver.setLastActivatedButtonId(lastActivatedButtonId);
        }

        return receiver;
    }

    /**
     * Find an appropriate constructor for the given class and receiver type
     *
     * @param className Class name
     * @param type      type of Receiver
     * @return Constructor
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    public static Constructor<?> getConstructor(String className, String type) throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException {
        Constructor<?> constructor = null;
        Class<?> myClass = Class.forName(className);

        if (type.equals(Receiver.TYPE_DIPS)) {
            constructor = myClass.getConstructor(Context.class, Long.class, String.class, LinkedList.class, Long.class);
        } else if (type.equals(Receiver.TYPE_MASTER_SLAVE)) {
            constructor = myClass.getConstructor(Context.class, Long.class, String.class, char.class, int.class,
                    Long.class);
        } else if (type.equals(Receiver.TYPE_AUTOPAIR)) {
            constructor = myClass.getConstructor(Context.class, Long.class, String.class, long.class, Long.class);
        } else if (type.equals(Receiver.TYPE_UNIVERSAL)) {
            constructor = myClass.getConstructor(Context.class, Long.class, String.class, List.class, Long.class);
        }
        return constructor;
    }

    /**
     * Gives you the type of a Receiver based on its java path.
     *
     * @param javaPath The java path of the Receiver.
     * @return The type of the Receiver or null if unknown.
     */
    public static String getType(String javaPath) {

        try {
            Class<?> myClass = Class.forName(javaPath);
            Class<?>[] implementedInterfaces = myClass.getInterfaces();

            for (Class<?> someClass : implementedInterfaces) {
                if (someClass.equals(MasterSlaveReceiver.class)) {
                    return Receiver.TYPE_MASTER_SLAVE;
                } else if (someClass.equals(DipReceiver.class)) {
                    return Receiver.TYPE_DIPS;
                } else if (someClass.equals(AutoPairReceiver.class)) {
                    return Receiver.TYPE_AUTOPAIR;
                }
            }
            if (myClass.equals(UniversalReceiver.class)) {
                return Receiver.TYPE_UNIVERSAL;
            }

        } catch (ClassNotFoundException e) {
            Log.e(e);
            e.printStackTrace();
        }

        // throw new Exception("Unknown Receiver Type");
        return null;
    }

    /**
     * Get an empty dummy Receiver just by providing the class path
     *
     * @param context  any suitable context
     * @param javaPath path to class
     * @return Receiver object
     */
    public static Receiver getDummy(Context context, String javaPath) {
        long dummyReceiverId = 0;
        String dummyReceiverName = "dummy";

        try {
            Class<?> myClass = Class.forName(javaPath);
            Class<?>[] implementedInterfaces = myClass.getInterfaces();

            for (Class<?> someClass : implementedInterfaces) {
                if (someClass.equals(MasterSlaveReceiver.class)) {
                    Constructor<?> constructor = myClass.getConstructor(Context.class, Long.class, String.class, char
                            .class, int.class, Long.class);
                    return (Receiver) constructor.newInstance(context, dummyReceiverId, dummyReceiverName, 'A', 0,
                            null);
                } else if (someClass.equals(DipReceiver.class)) {
                    Constructor<?> constructor;
                    constructor = myClass.getConstructor(Context.class, Long.class, String.class, LinkedList.class, Long.class);
                    return (Receiver) constructor.newInstance(context, dummyReceiverId, dummyReceiverName, new
                            LinkedList<Boolean>(), null);
                } else if (someClass.equals(AutoPairReceiver.class)) {
                    Constructor<?> constructor = myClass.getConstructor(Context.class, Long.class, String.class, long
                            .class, Long.class);
                    return (Receiver) constructor.newInstance(context, dummyReceiverId, dummyReceiverName, -1, null);
                }
            }

            if (myClass.equals(UniversalReceiver.class)) {
                return new UniversalReceiver(context, dummyReceiverId, dummyReceiverName, new
                        LinkedList<UniversalButton>(), null);
            }
        } catch (Exception e) {
            Log.e(e);
            e.printStackTrace();
        }

        return null;
    }
}

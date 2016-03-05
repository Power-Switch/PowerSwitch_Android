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
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.UniversalReceiver;

/**
 *
 */
public abstract class ReceiverReflectionMagic {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private ReceiverReflectionMagic() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Creates a Receiver based on a database cursor.
     *
     * @param cursor  A database cursor with one element.
     * @param context The application context for all database operations.
     * @return The complete Receiver object.
     */
    public static Receiver fromDatabase(Context context, Cursor cursor) throws Exception {
        Long id = cursor.getLong(0);
        String name = cursor.getString(1);
        String model = cursor.getString(2);
        Receiver.Type type = Receiver.Type.getEnum(cursor.getString(3));
        String className = cursor.getString(4);
        Long roomId = cursor.getLong(5);

        int positionInRoom = -1;
        if (!cursor.isNull(6)) {
            positionInRoom = cursor.getInt(6);
        }

        Long lastActivatedButtonId = cursor.getLong(7);

        Receiver receiver = null;

        Constructor<?> constructor = getConstructor(className, type);

        switch (type) {
            case MASTER_SLAVE:
                Character channelMaster = MasterSlaveReceiverHandler.getMaster(id);
                int channelSlave = MasterSlaveReceiverHandler.getSlave(id);
                receiver = (Receiver) constructor.newInstance(context, id, name, channelMaster, channelSlave, roomId);
                break;
            case DIPS:
                LinkedList<Boolean> dips = DipHandler.getDips(id);
                receiver = (Receiver) constructor.newInstance(context, id, name, dips, roomId);
                break;
            case UNIVERSAL:
                List<UniversalButton> buttons = UniversalButtonHandler.getUniversalButtons(id);
                receiver = (Receiver) constructor.newInstance(context, id, name, buttons, roomId);
                break;
            case AUTOPAIR:
                long seed = AutoPairHandler.getSeed(id);
                receiver = (Receiver) constructor.newInstance(context, id, name, seed, roomId);
                break;
        }

        receiver.setPositionInRoom(positionInRoom);
        receiver.setLastActivatedButtonId(lastActivatedButtonId);

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
    public static Constructor<?> getConstructor(String className, Receiver.Type type) throws ClassNotFoundException,
            NoSuchMethodException, IllegalArgumentException {
        Class<?> myClass = Class.forName(className);

        switch (type) {
            case DIPS:
                return myClass.getConstructor(Context.class, Long.class, String.class, LinkedList.class, Long.class);
            case MASTER_SLAVE:
                return myClass.getConstructor(Context.class, Long.class, String.class, char.class, int.class,
                        Long.class);
            case UNIVERSAL:
                return myClass.getConstructor(Context.class, Long.class, String.class, List.class, Long.class);
            case AUTOPAIR:
                return myClass.getConstructor(Context.class, Long.class, String.class, long.class, Long.class);
            default:
                throw new ClassNotFoundException("Unknown type " + type.toString());
        }
    }

    /**
     * Gives you the type of a Receiver based on its java path.
     *
     * @param javaPath The java path of the Receiver.
     * @return The type of the Receiver or null if unknown.
     */
    public static Receiver.Type getType(String javaPath) throws ClassNotFoundException {

        Class<?> myClass = Class.forName(javaPath);
        Class<?>[] implementedInterfaces = myClass.getInterfaces();

        for (Class<?> someClass : implementedInterfaces) {
            if (someClass.equals(MasterSlaveReceiver.class)) {
                return Receiver.Type.MASTER_SLAVE;
            } else if (someClass.equals(DipReceiver.class)) {
                return Receiver.Type.DIPS;
            } else if (someClass.equals(AutoPairReceiver.class)) {
                return Receiver.Type.AUTOPAIR;
            }
        }
        if (myClass.equals(UniversalReceiver.class)) {
            return Receiver.Type.UNIVERSAL;
        }

        throw new ClassNotFoundException("Unknown Receiver Type/Classpath");
    }

    /**
     * Get an empty dummy Receiver just by providing the class path
     *
     * @param context  any suitable context
     * @param javaPath path to class
     * @return Receiver object
     */
    public static Receiver getDummy(Context context, String javaPath) throws Exception {
        long dummyReceiverId = 0;
        String dummyReceiverName = "dummy";

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

        throw new RuntimeException("Unknown Receiver");
    }
}

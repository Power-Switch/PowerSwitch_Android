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
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import eu.power_switch.database.Database;
import eu.power_switch.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.exception.gateway.GatewayHasBeenEnabledException;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.log.Log;
import eu.power_switch.timer.Timer;
import eu.power_switch.widget.ReceiverWidget;
import eu.power_switch.widget.RoomWidget;
import eu.power_switch.widget.SceneWidget;

/**
 * This class handles all database related operations used anywhere in the app.
 * It has to be initialized before it can be used but this has to be done only once per App launch. Multiple
 * initializations are possible (wont cause an error) but wont do anything either.
 * <p/>
 * Access the Database only using the static methods of this class. All other Handlers should be protected and only
 * accessed via this class.
 * <p/>
 * Example:
 * Do:
 * Room room = DatabaseHandler.getRoom(2);
 * <p/>
 * DONT:
 * Room room = RoomHandler.get(2);
 */
public final class DatabaseHandler {

    /**
     * Context
     */
    protected static Context context;
    /**
     * Database helper for opening/closing Database Files
     */
    protected static Database dbHelper;
    /**
     * Database Object
     */
    protected static SQLiteDatabase database;
    /**
     * Lock Object to monitor and prevent parallel database access
     */
    private static Lock lock;

    private DatabaseHandler() {
    }

    /**
     * Initialize DatabaseHandler
     * <p/>
     * This is only needed once per App launch
     * You can call this method multiple times but it will only initialize handlers once.
     *
     * @param context Any suitable context
     */
    synchronized public static void init(Context context) {
        if (DatabaseHandler.context != null) {
            // dont init again
            return;
        }
        Log.d("Init Database Handler");

        lock = new ReentrantLock();

        DatabaseHandler.context = context;
        dbHelper = new Database(context);
    }

    /**
     * Open Database for read-only access
     */
    synchronized private static void openReadable() {
        lock.lock();
        try {
            database = dbHelper.getReadableDatabase();
        } catch (Exception e) {
            Log.e(e);
            lock.unlock();
        }
    }

    /**
     * Open Database for read-write access
     */
    synchronized private static void openWritable() {
        lock.lock();
        try {
            database = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e(e);
            lock.unlock();
        }
    }

    /**
     * Close Database
     */
    private static void close() {
        try {
            dbHelper.close();
        } catch (Exception e) {
            Log.e("Error closing Database", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * ////////////////////
     * // Room functions //
     * ////////////////////
     *
     */

    /**
     * Save a room to the database.
     *
     * @param room the new room
     */
    public static void addRoom(Room room) {
        openWritable();
        try {
            RoomHandler.add(room);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Update an existing room.
     *
     * @param id      the ID of the Room
     * @param newName the new Name
     */
    public static void updateRoom(Long id, String newName) {
        openWritable();
        try {
            RoomHandler.update(id, newName);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Delte a room.
     *
     * @param id the ID of the room
     */
    public static void deleteRoom(Long id) {
        openWritable();
        try {
            RoomHandler.delete(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Get a room object by its name.
     *
     * @param name the name of the room
     * @return a room object
     */
    public static Room getRoom(String name) {
        openReadable();
        Room room = null;
        try {
            room = RoomHandler.get(name);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return room;
    }

    /**
     * Get a room object by its ID.
     *
     * @param id the ID of the room
     * @return a room object
     */
    public static Room getRoom(Long id) {
        openReadable();
        Room room = null;
        try {
            room = RoomHandler.get(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return room;
    }

    /**
     * Get all rooms.
     *
     * @return a list of all rooms
     */
    public static List<Room> getAllRooms() {
        openReadable();
        List<Room> rooms = null;
        try {
            rooms = RoomHandler.getAll();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return rooms;
    }

    /**
     *
     * ////////////////////////
     * // Receiver functions //
     * ////////////////////////
     *
     */

    /**
     * Add Receiver to database
     *
     * @param receiver the new Receiver
     */
    public static void addReceiver(Receiver receiver) {
        openWritable();
        try {
            ReceiverHandler.add(receiver);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Updates a Receiver in database
     *
     * @param receiver the edited Receiver
     */
    public static void updateReceiver(Receiver receiver) {
        openWritable();
        try {
            ReceiverHandler.update(receiver);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Get Receiver by id
     *
     * @param id ID of the Receiver
     * @return Receiver, can be null
     */
    public static Receiver getReceiver(Long id) {
        openReadable();
        Receiver receiver = null;
        try {
            receiver = ReceiverHandler.get(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return receiver;
    }

    /**
     * Get all Receivers associated with a Room
     *
     * @param id ID of room
     * @return List of Receivers
     */
    public static ArrayList<Receiver> getReceiverByRoomId(Long id) {
        openReadable();
        ArrayList<Receiver> receivers = null;
        try {
            receivers = ReceiverHandler.getByRoom(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return receivers;
    }

    /**
     * Get Receiver associated with a Room
     *
     * @param roomId       ID of room
     * @param receiverName Name of Receiver
     * @return List of Receivers
     */
    public static Receiver getReceiverByRoomId(Long roomId, String receiverName) {
        openReadable();
        Receiver receiver = null;
        try {
            receiver = ReceiverHandler.getByRoom(roomId, receiverName);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return receiver;
    }

    /**
     * Get all Receivers in database
     *
     * @return List of Receivers
     */
    public static List<Receiver> getAllReceivers() {
        openReadable();
        List<Receiver> receivers = null;
        try {
            receivers = ReceiverHandler.getAll();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return receivers;
    }

    /**
     * Delete Receiver from Database
     *
     * @param id ID of Receiver
     */
    public static void deleteReceiver(Long id) {
        openWritable();
        try {
            ReceiverHandler.delete(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Get Button from Database
     *
     * @param id ID of Button
     * @return Button
     */
    public static UniversalButton getButton(Long id) {
        openReadable();
        UniversalButton button = null;
        try {
            button = UniversalButtonHandler.getUniversalButton(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return button;
    }

    /**
     * Get Buttons associated with a Receiver
     *
     * @param receiverId ID of Receiver
     * @return List of Buttons
     */
    public static List<UniversalButton> getButtons(Long receiverId) {
        openReadable();
        List<UniversalButton> buttons = null;
        try {
            buttons = UniversalButtonHandler.getUniversalButtons(receiverId);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return buttons;
    }

//    /**
//     * Gets ID of last activated Button of a Receiver
//     *
//     * @param id ID of Receiver
//     * @return ID of last activated Button, -1 if not set
//     */
//    public static long getLastActivatedButtonId(Long id) {
//        openReadable();
//        long buttonId = -1;
//        try {
//            buttonId = ReceiverHandler.getLastActivatedButtonId(id);
//        } catch (Exception e) {
//            Log.e(e);
//        } finally {
//            close();
//        }
//        return buttonId;
//    }

    /**
     * Sets ID of last activated Button of a Receiver
     *
     * @param receiverId ID of Receiver
     * @param buttonId   ID of Button
     * @return ID of last activated Button, -1 if not set
     */
    public static void setLastActivatedButtonId(Long receiverId, Long buttonId) {
        openWritable();
        try {
            ReceiverHandler.setLastActivatedButtonId(receiverId, buttonId);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     *
     * /////////////////////
     * // Scene functions //
     * /////////////////////
     *
     */

    /**
     * Add a scene to Database
     *
     * @param scene the new Scene
     */
    public static void addScene(Scene scene) {
        openWritable();
        try {
            SceneHandler.add(scene);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Update existing Scene
     *
     * @param scene the edited Scene
     */
    public static void updateScene(Scene scene) {
        openWritable();
        try {
            SceneHandler.update(scene);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Delete Scene from Database
     *
     * @param id ID of Scene
     */
    public static void deleteScene(Long id) {
        openWritable();
        try {
            SceneHandler.delete(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Get Scene from Database
     *
     * @param name Name of Scene
     * @return Scene
     */
    public static Scene getScene(String name) {
        openReadable();
        Scene scene = null;
        try {
            scene = SceneHandler.get(name);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return scene;
    }

    /**
     * Get Scene from Database
     *
     * @param id ID of Scene
     * @return Scene
     */
    public static Scene getScene(Long id) {
        openReadable();
        Scene scene = null;
        try {
            scene = SceneHandler.get(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return scene;
    }

    /**
     * Get all scenes from Database
     *
     * @return List of Scenes
     */
    public static List<Scene> getAllScenes() {
        openReadable();
        List<Scene> scenes = null;
        try {
            scenes = SceneHandler.getAll();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return scenes;
    }

    /**
     *
     * ///////////////////////
     * // Gateway functions //
     * ///////////////////////
     *
     */

    /**
     * Add Gateway to Database
     *
     * @param gateway new Gateway
     * @return ID of saved Database entry
     * @throws GatewayHasBeenEnabledException
     * @throws GatewayAlreadyExistsException
     */
    public static long addGateway(Gateway gateway) throws GatewayHasBeenEnabledException, GatewayAlreadyExistsException {
        openWritable();
        long id;
        try {
            id = GatewayHandler.add(gateway);
            close();
        } catch (Exception e) {
            close();
            throw e;
        }
        return id;
    }

    /**
     * Enable existing Gateway
     *
     * @param id ID of Gateway
     */
    public static void enableGateway(Long id) {
        openWritable();
        try {
            GatewayHandler.enable(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Disable existing Gateway
     *
     * @param id ID of Gateway
     */
    public static void disableGateway(Long id) {
        openWritable();
        try {
            GatewayHandler.disable(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Update existing Gateway
     *
     * @param id      ID of Gateway
     * @param name    new Name of Gateway
     * @param model   new Model of Gateway
     * @param address new Address (Host) of Gateway
     * @param port    new Port of Gateway
     */
    public static void updateGateway(Long id, String name, String model, String address, Integer port) {
        openWritable();
        try {
            GatewayHandler.update(id, name, model, address, port);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Delete Gateway from Database
     *
     * @param id ID of Gateway
     */
    public static void deleteGateway(Long id) {
        openWritable();
        try {
            GatewayHandler.delete(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Get Gateway from Database
     *
     * @param id ID of Gateway
     * @return Gateway
     */
    public static Gateway getGateway(Long id) {
        openReadable();
        Gateway gateway = null;
        try {
            gateway = GatewayHandler.get(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return gateway;
    }

    /**
     * Get all Gateways
     *
     * @return List of Gateways
     */
    public static List<Gateway> getAllGateways() {
        openReadable();
        List<Gateway> gateways = null;
        try {
            gateways = GatewayHandler.getAll();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return gateways;
    }

    /**
     * Get all enabled/disabled Gateways
     *
     * @param isActive true if Gateway is enabled
     * @return List of Gateways
     */
    public static List<Gateway> getAllGateways(boolean isActive) {
        openReadable();
        List<Gateway> gateways = null;
        try {
            gateways = GatewayHandler.getAll(isActive);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return gateways;
    }

    /**
     *
     * ///////////////////////
     * // Widget functions //
     * ///////////////////////
     *
     */

    /**
     * Add ReceiverWidget to Database
     *
     * @param receiverWidget WidgetInfo Object
     */
    public static void addReceiverWidget(ReceiverWidget receiverWidget) {
        openWritable();
        try {
            WidgetHandler.addReceiverWidget(receiverWidget);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Delete ReceiverWidget from Database
     *
     * @param id WidgetId
     */
    public static void deleteReceiverWidget(int id) {
        openWritable();
        try {
            WidgetHandler.deleteReceiverWidget(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Get ReceiverWidget from Database
     *
     * @param id WidgetId
     */
    public static ReceiverWidget getReceiverWidget(int id) {
        openReadable();
        ReceiverWidget receiverWidget = null;
        try {
            receiverWidget = WidgetHandler.getReceiverWidget(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return receiverWidget;
    }

    /**
     * Add RoomWidget to Database
     *
     * @param roomWidget WidgetInfo Object
     */
    public static void addRoomWidget(RoomWidget roomWidget) {
        openWritable();
        try {
            WidgetHandler.addRoomWidget(roomWidget);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Delete RoomWidget from Database
     *
     * @param id WidgetId
     */
    public static void deleteRoomWidget(int id) {
        openWritable();
        try {
            WidgetHandler.deleteRoomWidget(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Get RoomWidget from Database
     *
     * @param id WidgetId
     */
    public static RoomWidget getRoomWidget(int id) {
        openReadable();
        RoomWidget roomWidget = null;
        try {
            roomWidget = WidgetHandler.getRoomWidget(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return roomWidget;
    }

    /**
     * Add SceneWidget to Database
     *
     * @param sceneWidget WidgetInfo Object
     */
    public static void addSceneWidget(SceneWidget sceneWidget) {
        openWritable();
        try {
            WidgetHandler.addSceneWidget(sceneWidget);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Delete SceneWidget from Database
     *
     * @param id WidgetId
     */
    public static void deleteSceneWidget(int id) {
        openWritable();
        try {
            WidgetHandler.deleteSceneWidget(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Get SceneWidget from Database
     *
     * @param id WidgetId
     */
    public static SceneWidget getSceneWidget(int id) {
        openWritable();
        SceneWidget sceneWidget = null;
        try {
            sceneWidget = WidgetHandler.getSceneWidget(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return sceneWidget;
    }


    /**
     * Get Timer from Database
     *
     * @param id ID of Timer
     * @return Timer
     */
    public static Timer getTimer(Long id) {
        openReadable();
        Timer timer = null;
        try {
            timer = TimerHandler.get(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return timer;
    }

    /**
     * Get all Timers.
     *
     * @return a list of all Timers
     */
    public static List<Timer> getAllTimers() {
        openReadable();
        List<Timer> timers = null;
        try {
            timers = TimerHandler.getAll();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return timers;
    }

    /**
     * Get all active/inactive Timers.
     *
     * @param isActive true if Timer is active
     * @return a list of all active/inactive Timers
     */
    public static List<Timer> getAllTimers(boolean isActive) {
        openReadable();
        List<Timer> timers = null;
        try {
            timers = TimerHandler.getAll(isActive);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return timers;
    }

    /**
     * Add Timer to Database
     *
     * @param timer Timer Object
     */
    public static long addTimer(Timer timer) {
        openWritable();
        long id = -1;
        try {
            id = TimerHandler.add(timer);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }

        return id;
    }

    /**
     * Enable Timer
     *
     * @param id ID of Timer
     */
    public static void enableTimer(Long id) {
        openWritable();
        try {
            TimerHandler.enable(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Disable Timer
     *
     * @param id ID of Timer
     */
    public static void disableTimer(Long id) {
        openWritable();
        try {
            TimerHandler.disable(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Deletes Timer from Database
     *
     * @param id ID of Timer
     */
    public static void deleteTimer(Long id) {
        openWritable();
        try {
            TimerHandler.delete(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Updates an existing Timer
     *
     * @param timer new Timer with same ID as old one
     */
    public static void updateTimer(Timer timer) {
        openWritable();
        try {
            TimerHandler.update(timer);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Sets the position of a Receiver
     *
     * @param receiverId ID of Receiver
     * @param position   position in room
     */
    public static void setPositionInRoom(Long receiverId, Long position) {
        openWritable();
        try {
            ReceiverHandler.setPositionInRoom(receiverId, position);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }
}

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
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import eu.power_switch.action.Action;
import eu.power_switch.database.Database;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.history.HistoryItem;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.ExternalAppConstants;
import eu.power_switch.shared.exception.gateway.GatewayAlreadyExistsException;
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
    synchronized public static void init(@NonNull Context context) {
        if (DatabaseHandler.context != null) {
            // dont init again
            return;
        }
        Log.d(DatabaseHandler.class, "Init Database Handler...");

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
            database.beginTransaction();
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
            if (database.inTransaction()) {
                database.endTransaction();
            }
            dbHelper.close();
        } catch (Exception e) {
            Log.e("Error closing Database", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * /////////////////////////
     * // Apartment functions //
     * /////////////////////////
     */

    public static long addApartment(Apartment apartment) throws Exception {
        openWritable();
        long id = -1;
        try {
            id = ApartmentHandler.add(apartment);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
            close();
            throw e;
        } finally {
            close();
        }

        return id;
    }

    /**
     * Deletes an Apartment from Database
     *
     * @param id ID of Apartment
     */
    public static void deleteApartment(Long id) throws Exception {
        openWritable();
        try {
            ApartmentHandler.delete(id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
            close();
        } finally {
            close();
        }
    }

    /**
     * Updates an Apartment in Database
     *
     * @param apartment updated Apartment
     */
    public static void updateApartment(Apartment apartment) throws Exception {
        openWritable();
        try {
            ApartmentHandler.update(apartment);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
            close();
        } finally {
            close();
        }
    }

    /**
     * Get an Apartment by Name
     *
     * @param name Name of Apartment
     * @return Apartment
     */
    public static Apartment getApartment(String name) throws Exception {
        openReadable();
        Apartment apartment = null;
        try {
            apartment = ApartmentHandler.get(name);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return apartment;
    }

    /**
     * Get an Apartment by Name
     *
     * @param id ID of Apartment
     * @return Apartment
     */
    public static Apartment getApartment(Long id) throws Exception {
        openReadable();
        Apartment apartment = null;
        try {
            apartment = ApartmentHandler.get(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return apartment;
    }

    public static List<Apartment> getAllApartments() throws Exception {
        openReadable();
        List<Apartment> apartments = null;
        try {
            apartments = ApartmentHandler.getAll();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return apartments;
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
    public static void addRoom(Room room) throws Exception {
        openWritable();
        try {
            RoomHandler.add(room);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
            close();
            throw e;
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
    public static void updateRoom(Long id, String newName) throws Exception {
        openWritable();
        try {
            RoomHandler.update(id, newName);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
            close();
            throw e;
        } finally {
            close();
        }
    }

    /**
     * Delte a room.
     *
     * @param id the ID of the room
     */
    public static void deleteRoom(Long id) throws Exception {
        openWritable();
        try {
            RoomHandler.delete(id);
            database.setTransactionSuccessful();
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
    public static Room getRoom(String name) throws Exception {
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
    public static Room getRoom(Long id) throws Exception {
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
    public static List<Room> getAllRooms() throws Exception {
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
     * Get all rooms of a specific Apartment.
     *
     * @return a list of rooms
     */
    public static List<Room> getRooms(Long apartmentId) throws Exception {
        openReadable();
        List<Room> rooms = null;
        try {
            rooms = RoomHandler.getByApartment(apartmentId);
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
    public static void addReceiver(Receiver receiver) throws Exception {
        openWritable();
        try {
            ReceiverHandler.add(receiver);
            database.setTransactionSuccessful();
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
    public static void updateReceiver(Receiver receiver) throws Exception {
        openWritable();
        try {
            ReceiverHandler.update(receiver);
            database.setTransactionSuccessful();
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
    public static Receiver getReceiver(Long id) throws Exception {
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
    public static ArrayList<Receiver> getReceiverByRoomId(Long id) throws Exception {
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
    public static Receiver getReceiverByRoomId(Long roomId, String receiverName) throws Exception {
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
    public static List<Receiver> getAllReceivers() throws Exception {
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
    public static void deleteReceiver(Long id) throws Exception {
        openWritable();
        try {
            ReceiverHandler.delete(id);
            database.setTransactionSuccessful();
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
    public static UniversalButton getButton(Long id) throws Exception {
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
    public static List<UniversalButton> getButtons(Long receiverId) throws Exception {
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

    /**
     * Sets ID of last activated Button of a Receiver
     *
     * @param receiverId ID of Receiver
     * @param buttonId   ID of Button
     */
    public static void setLastActivatedButtonId(Long receiverId, Long buttonId) throws Exception {
        openWritable();
        try {
            ReceiverHandler.setLastActivatedButtonId(receiverId, buttonId);
            database.setTransactionSuccessful();
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
    public static void addScene(Scene scene) throws Exception {
        openWritable();
        try {
            SceneHandler.add(scene);
            database.setTransactionSuccessful();
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
    public static void updateScene(Scene scene) throws Exception {
        openWritable();
        try {
            SceneHandler.update(scene);
            database.setTransactionSuccessful();
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
    public static void deleteScene(Long id) throws Exception {
        openWritable();
        try {
            SceneHandler.delete(id);
            database.setTransactionSuccessful();
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
    public static Scene getScene(String name) throws Exception {
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
    public static Scene getScene(Long id) throws Exception {
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
     * Get all scenes of a specific Apartment.
     *
     * @return a list of scenes
     */
    public static List<Scene> getScenes(Long apartmentId) throws Exception {
        openReadable();
        List<Scene> scenes = null;
        try {
            scenes = SceneHandler.getByApartment(apartmentId);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return scenes;
    }

    /**
     * Get all scenes from Database
     *
     * @return List of Scenes
     */
    public static List<Scene> getAllScenes() throws Exception {
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
     * @throws GatewayAlreadyExistsException
     */
    public static long addGateway(Gateway gateway) throws Exception {
        openWritable();
        long id;
        try {
            id = GatewayHandler.add(gateway);
            database.setTransactionSuccessful();
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
    public static void enableGateway(Long id) throws Exception {
        openWritable();
        try {
            GatewayHandler.enable(id);
            database.setTransactionSuccessful();
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
    public static void disableGateway(Long id) throws Exception {
        openWritable();
        try {
            GatewayHandler.disable(id);
            database.setTransactionSuccessful();
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
    public static void updateGateway(Long id, String name, String model, String address, Integer port) throws Exception {
        openWritable();
        try {
            GatewayHandler.update(id, name, model, address, port);
            database.setTransactionSuccessful();
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
    public static void deleteGateway(Long id) throws Exception {
        openWritable();
        try {
            GatewayHandler.delete(id);
            database.setTransactionSuccessful();
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
    public static Gateway getGateway(Long id) throws Exception {
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
    public static List<Gateway> getAllGateways() throws Exception {
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
    public static List<Gateway> getAllGateways(boolean isActive) throws Exception {
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
     * //////////////////////
     * // Widget functions //
     * //////////////////////
     *
     */

    /**
     * Add ReceiverWidget to Database
     *
     * @param receiverWidget WidgetInfo Object
     */
    public static void addReceiverWidget(ReceiverWidget receiverWidget) throws Exception {
        openWritable();
        try {
            WidgetHandler.addReceiverWidget(receiverWidget);
            database.setTransactionSuccessful();
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
    public static void deleteReceiverWidget(int id) throws Exception {
        openWritable();
        try {
            WidgetHandler.deleteReceiverWidget(id);
            database.setTransactionSuccessful();
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
    public static ReceiverWidget getReceiverWidget(int id) throws Exception {
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
    public static void addRoomWidget(RoomWidget roomWidget) throws Exception {
        openWritable();
        try {
            WidgetHandler.addRoomWidget(roomWidget);
            database.setTransactionSuccessful();
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
    public static void deleteRoomWidget(int id) throws Exception {
        openWritable();
        try {
            WidgetHandler.deleteRoomWidget(id);
            database.setTransactionSuccessful();
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
    public static RoomWidget getRoomWidget(int id) throws Exception {
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
    public static void addSceneWidget(SceneWidget sceneWidget) throws Exception {
        openWritable();
        try {
            WidgetHandler.addSceneWidget(sceneWidget);
            database.setTransactionSuccessful();
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
    public static void deleteSceneWidget(int id) throws Exception {
        openWritable();
        try {
            WidgetHandler.deleteSceneWidget(id);
            database.setTransactionSuccessful();
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
    public static SceneWidget getSceneWidget(int id) throws Exception {
        openWritable();
        SceneWidget sceneWidget = null;
        try {
            sceneWidget = WidgetHandler.getSceneWidget(id);
            database.setTransactionSuccessful();
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
    public static Timer getTimer(Long id) throws Exception {
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
    public static List<Timer> getAllTimers() throws Exception {
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
    public static List<Timer> getAllTimers(boolean isActive) throws Exception {
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
    public static long addTimer(Timer timer) throws Exception {
        openWritable();
        long id = -1;
        try {
            id = TimerHandler.add(timer);
            database.setTransactionSuccessful();
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
    public static void enableTimer(Long id) throws Exception {
        openWritable();
        try {
            TimerHandler.enable(id);
            database.setTransactionSuccessful();
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
    public static void disableTimer(Long id) throws Exception {
        openWritable();
        try {
            TimerHandler.disable(id);
            database.setTransactionSuccessful();
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
    public static void deleteTimer(Long id) throws Exception {
        openWritable();
        try {
            TimerHandler.delete(id);
            database.setTransactionSuccessful();
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
    public static void updateTimer(Timer timer) throws Exception {
        openWritable();
        try {
            TimerHandler.update(timer);
            database.setTransactionSuccessful();
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
    public static void setPositionInRoom(Long receiverId, Long position) throws Exception {
        openWritable();
        try {
            ReceiverHandler.setPositionInRoom(receiverId, position);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     *
     * ////////////////////////////////
     * // Sleep As Android functions //
     * ////////////////////////////////
     *
     */

    /**
     * Get Actions for a specific alarm event
     *
     * @param event alarm event
     * @return List of Actions
     */
    public static List<Action> getAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT event) throws Exception {
        openReadable();
        List<Action> actions = new ArrayList<>();
        try {
            actions = SleepAsAndroidHandler.getAlarmActions(event);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return actions;
    }

    /**
     * Set Actions for a specific alarm event
     *
     * @param event   alarm event
     * @param actions List of Actions
     */
    public static void setAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT event, ArrayList<Action> actions) throws Exception {
        openWritable();
        try {
            SleepAsAndroidHandler.setAlarmActions(event, actions);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * ///////////////////////
     * // History functions //
     * ///////////////////////
     */

    /**
     * Gets all HistoryItems in Database
     *
     * @return List of HistoryItems
     */
    public static LinkedList<HistoryItem> getHistory() throws Exception {
        openReadable();
        LinkedList<HistoryItem> historyItems = new LinkedList<>();
        try {
            historyItems = HistoryHandler.getHistory();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return historyItems;
    }

    /**
     * Adds a HistoryItem to database
     *
     * @param historyItem HistoryItem
     */
    public static void addHistoryItem(HistoryItem historyItem) throws Exception {
        openWritable();
        try {
            HistoryHandler.add(historyItem);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    public static Apartment getContainingApartment(Receiver receiver) {
        openReadable();
        Apartment apartment = null;
        try {
            apartment = ApartmentHandler.get(receiver);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return apartment;
    }

    public static Apartment getContainingApartment(Room room) {
        openReadable();
        Apartment apartment = null;
        try {
            apartment = ApartmentHandler.get(room);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return apartment;
    }

    public static Apartment getContainingApartment(Scene scene) {
        openReadable();
        Apartment apartment = null;
        try {
            apartment = ApartmentHandler.get(scene);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return apartment;
    }

    /**
     * ////////////////////////
     * // Geofence functions //
     * ////////////////////////
     */

    /**
     * Get Gateway from Database
     *
     * @param id ID of Gateway
     * @return Gateway
     */
    public static Geofence getGeofence(Long id) throws Exception {
        openReadable();
        Geofence geofence = null;
        try {
            geofence = GeofenceHandler.get(id);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return geofence;
    }

    /**
     * Get a list of all active/inactive Geofences
     *
     * @param isActive true if active, false otherwise
     * @return list of Geofences
     */
    public static List<Geofence> getAllGeofences(boolean isActive) throws Exception {
        openReadable();
        List<Geofence> geofences = null;
        try {
            geofences = GeofenceHandler.getAll(isActive);
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return geofences;
    }

    /**
     * Get a list of all custom Geofences
     *
     * @return list of custom Geofences
     */
    public static List<Geofence> getCustomGeofences() throws Exception {
        openReadable();
        List<Geofence> geofences = null;
        try {
            geofences = GeofenceHandler.getCustom();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
        return geofences;
    }

    /**
     * Add Geofence to Database
     *
     * @param geofence new Geofence
     * @return ID of saved Database entry
     */
    public static long addGeofence(Geofence geofence) throws Exception {
        openWritable();
        long id;
        try {
            id = GeofenceHandler.add(geofence);
            database.setTransactionSuccessful();
            close();
        } catch (Exception e) {
            close();
            throw e;
        }
        return id;
    }

    /**
     * Update existing Geofence in Database
     *
     * @param geofence updated Geofence
     */
    public static void updateGeofence(Geofence geofence) {
        openWritable();
        try {
            GeofenceHandler.update(geofence);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Enable existing Geofence
     *
     * @param id ID of Geofence
     */
    public static void enableGeofence(Long id) throws Exception {
        openWritable();
        try {
            GeofenceHandler.enable(id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Disable existing Geofence
     *
     * @param id ID of Geofence
     */
    public static void disableGeofence(Long id) throws Exception {
        openWritable();
        try {
            GeofenceHandler.disable(id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }

    /**
     * Delete Geofence from Database
     *
     * @param id ID of Geofence
     */
    public static void deleteGeofence(Long id) throws Exception {
        openWritable();
        try {
            GeofenceHandler.delete(id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            close();
        }
    }
}
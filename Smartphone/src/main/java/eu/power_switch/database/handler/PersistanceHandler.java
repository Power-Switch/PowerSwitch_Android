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

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.action.Action;
import eu.power_switch.database.Database;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.history.HistoryItem;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.AlarmClockConstants;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import eu.power_switch.shared.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.timer.Timer;
import eu.power_switch.widget.ReceiverWidget;
import eu.power_switch.widget.RoomWidget;
import eu.power_switch.widget.SceneWidget;
import timber.log.Timber;

/**
 * This class handles all database related operations used anywhere in the app.
 * It has to be initialized before it can be used but this has to be done only once per App launch. Multiple
 * initializations are possible (wont cause an error) but wont do anything either.
 * <p/>
 * Access the Database only using the static methods of this class. All other Handlers should be protected and only
 * accessed via this class.
 */
@Singleton
public final class PersistanceHandler {

    @Inject
    ApartmentHandler            apartmentHandler;
    @Inject
    AlarmClockHandler           alarmClockHandler;
    @Inject
    CallEventActionHandler      callEventActionHandler;
    @Inject
    CallEventHandler            callEventHandler;
    @Inject
    CallEventPhoneNumberHandler callEventPhoneNumberHandler;
    @Inject
    DipHandler                  dipHandler;
    @Inject
    GatewayHandler              gatewayHandler;
    @Inject
    GeofenceActionHandler       geofenceActionHandler;
    @Inject
    GeofenceHandler             geofenceHandler;
    @Inject
    HistoryHandler              historyHandler;
    @Inject
    MasterSlaveReceiverHandler  masterSlaveReceiverHandler;
    @Inject
    PhoneNumberHandler          phoneNumberHandler;
    @Inject
    ReceiverHandler             receiverHandler;
    @Inject
    RoomHandler                 roomHandler;
    @Inject
    SceneHandler                sceneHandler;
    @Inject
    SceneItemHandler            sceneItemHandler;
    @Inject
    SleepAsAndroidHandler       sleepAsAndroidHandler;
    @Inject
    TimerActionHandler          timerActionHandler;
    @Inject
    TimerHandler                timerHandler;
    @Inject
    UniversalButtonHandler      universalButtonHandler;
    @Inject
    WidgetHandler               widgetHandler;

    /**
     * Database helper for opening/closing Database Files
     */
    private Database dbHelper;

    /**
     * Lock Object to monitor and prevent parallel database access
     */
    private Lock lock;

    /**
     * Open Database for read-only access
     */
    private synchronized SQLiteDatabase openReadable() throws Exception {
        lock.lock();
        try {
            return dbHelper.getReadableDatabase();
        } catch (Exception e) {
            Timber.e("Error getting read-only Database", e);
            lock.unlock();
            throw e;
        }
    }

    /**
     * Open Database for read-write access
     */
    private synchronized SQLiteDatabase openWritable() throws Exception {
        lock.lock();
        try {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.beginTransaction();
            return database;
        } catch (Exception e) {
            Timber.e("Error getting writable Database", e);
            lock.unlock();
            throw e;
        }
    }

    /**
     * Close Database
     */
    private void close(SQLiteDatabase database) {
        try {
            if (database.inTransaction()) {
                database.endTransaction();
            }
            if (database.isOpen()) {
                dbHelper.close();
            }
        } catch (Exception e) {
            Timber.e("Error closing Database", e);
        } finally {
            lock.unlock();
        }
    }

    @Inject
    public PersistanceHandler(Database database) {
        this.dbHelper = database;
        lock = new ReentrantLock();
    }

    /**
     * /////////////////////////
     * // Apartment functions //
     * /////////////////////////
     */

    /**
     * Add Apartment to Database
     *
     * @param apartment Apartment
     *
     * @return ID of added Apartment
     */
    @WorkerThread
    public long addApartment(Apartment apartment) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            long id = apartmentHandler.add(database, apartment);
            database.setTransactionSuccessful();
            return id;
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Deletes an Apartment from Database
     *
     * @param id ID of Apartment
     */
    @WorkerThread
    public void deleteApartment(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            apartmentHandler.delete(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Updates an Apartment in Database
     *
     * @param apartment updated Apartment
     */
    @WorkerThread
    public void updateApartment(Apartment apartment) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            apartmentHandler.update(database, apartment);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get an Apartment by Name
     *
     * @param name Name of Apartment
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    public Apartment getApartment(String name) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.get(database, name);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get an Apartment by Name, ignoring case
     *
     * @param name Name of Apartment
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    public Apartment getApartmentCaseInsensitive(String name) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.getCaseInsensitive(database, name);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get an Apartment by ID
     *
     * @param id ID of Apartment
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    public Apartment getApartment(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.get(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get the ID of an Apartment by its name, ignoring case
     *
     * @param name Name of Apartment
     *
     * @return ID of Apartment
     */
    @NonNull
    @WorkerThread
    public Long getApartmentId(String name) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.getId(database, name);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get the Name of an Apartment by ID
     *
     * @param id ID of Apartment
     *
     * @return Name of Apartment
     */
    @NonNull
    @WorkerThread
    public String getApartmentName(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.getName(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all Apartments from Database
     *
     * @return List of Apartment Names
     *
     * @throws Exception
     */
    @NonNull
    @WorkerThread
    public List<String> getAllApartmentNames() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.getAllNames(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all Apartments from Database
     *
     * @return List of Apartments
     *
     * @throws Exception
     */
    @NonNull
    @WorkerThread
    public List<Apartment> getAllApartments() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.getAll(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all Apartments associated with the given gateway id from Database
     *
     * @return List of Apartments
     *
     * @throws Exception
     */
    @NonNull
    @WorkerThread
    public List<Apartment> getAssociatedApartments(long gatewayId) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.getAssociated(database, gatewayId);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Apartment that contains a specific Receiver
     *
     * @param receiver Receiver
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    public Apartment getContainingApartment(Receiver receiver) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.get(database, receiver);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Apartment that contains a specific Room
     *
     * @param room Room
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    public Apartment getContainingApartment(Room room) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.get(database, room);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Apartment that contains a specific Scene
     *
     * @param scene Scene
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    public Apartment getContainingApartment(Scene scene) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return apartmentHandler.get(database, scene);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
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
     *
     * @return ID of added Room
     */
    @WorkerThread
    public long addRoom(Room room) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            long id = roomHandler.add(database, room);
            database.setTransactionSuccessful();
            return id;
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Update an existing room.
     *
     * @param id      the ID of the Room
     * @param newName the new Name
     */
    @WorkerThread
    public void updateRoom(Long id, String newName, List<Gateway> associatedGateways) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            roomHandler.update(database, id, newName, associatedGateways);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Update collapsed state of an existing room.
     *
     * @param id          the ID of the Room
     * @param isCollapsed the new Name
     */
    @WorkerThread
    public void updateRoomCollapsed(Long id, boolean isCollapsed) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            roomHandler.updateCollapsed(database, id, isCollapsed);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Sets the position of a Room
     *
     * @param roomId   ID of Room
     * @param position position in apartment
     */
    @WorkerThread
    public void setPositionOfRoom(Long roomId, Long position) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            roomHandler.setPosition(database, roomId, position);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delte a room.
     *
     * @param id the ID of the room
     */
    @WorkerThread
    public void deleteRoom(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            roomHandler.delete(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get a room object by its name.
     *
     * @param name the name of the room
     *
     * @return a room object
     */
    @NonNull
    @WorkerThread
    public Room getRoom(String name) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return roomHandler.get(database, name);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get a room object by its name, ignoring case
     *
     * @param name the name of the room
     *
     * @return a room object
     */
    @NonNull
    @WorkerThread
    public Room getRoomCaseInsensitive(String name) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return roomHandler.getCaseInsensitive(database, name);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get a room object by its ID.
     *
     * @param id the ID of the room
     *
     * @return a room object
     */
    @NonNull
    @WorkerThread
    public Room getRoom(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return roomHandler.get(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all rooms.
     *
     * @return a list of all rooms
     */
    @NonNull
    @WorkerThread
    public List<Room> getAllRooms() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return roomHandler.getAll(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all rooms of a specific Apartment.
     *
     * @return a list of rooms
     */
    @NonNull
    @WorkerThread
    public List<Room> getRooms(Long apartmentId) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return roomHandler.getByApartment(database, apartmentId);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all room IDs of a specific Apartment.
     *
     * @return a list of room IDs
     */
    @NonNull
    @WorkerThread
    public List<Long> getRoomIds(Long apartmentId) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return roomHandler.getIdsByApartment(database, apartmentId);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
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
    @WorkerThread
    public void addReceiver(Receiver receiver) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            receiverHandler.add(database, receiver);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Updates a Receiver in database
     *
     * @param receiver the edited Receiver
     */
    @WorkerThread
    public void updateReceiver(Receiver receiver) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            receiverHandler.update(database, receiver);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Receiver by id
     *
     * @param id ID of the Receiver
     *
     * @return Receiver, can be null
     */
    @NonNull
    @WorkerThread
    public Receiver getReceiver(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return receiverHandler.get(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all Receivers associated with a Room
     *
     * @param id ID of room
     *
     * @return List of Receivers
     */
    @NonNull
    @WorkerThread
    public List<Receiver> getReceiverByRoomId(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return receiverHandler.getByRoom(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Receiver associated with a Room
     *
     * @param roomId       ID of room
     * @param receiverName Name of Receiver
     *
     * @return List of Receivers
     */
    @NonNull
    @WorkerThread
    public Receiver getReceiverByRoomId(Long roomId, String receiverName) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return receiverHandler.getByRoom(database, roomId, receiverName);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Sets the position of a Receiver
     *
     * @param receiverId ID of Receiver
     * @param position   position in room
     */
    @WorkerThread
    public void setPositionOfReceiver(Long receiverId, Long position) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            receiverHandler.setPositionInRoom(database, receiverId, position);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all Receivers in database
     *
     * @return List of Receivers
     */
    @NonNull
    @WorkerThread
    public List<Receiver> getAllReceivers() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return receiverHandler.getAll(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delete Receiver from Database
     *
     * @param id ID of Receiver
     */
    @WorkerThread
    public void deleteReceiver(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            receiverHandler.delete(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Button from Database
     *
     * @param id ID of Button
     *
     * @return Button
     */
    @NonNull
    @WorkerThread
    public Button getButton(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return universalButtonHandler.getUniversalButton(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Buttons associated with a Receiver
     *
     * @param receiverId ID of Receiver
     *
     * @return List of Buttons
     */
    @NonNull
    @WorkerThread
    public List<UniversalButton> getButtons(Long receiverId) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return universalButtonHandler.getUniversalButtons(database, receiverId);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Sets ID of last activated Button of a Receiver
     *
     * @param receiverId ID of Receiver
     * @param buttonId   ID of Button
     */
    @WorkerThread
    public void setLastActivatedButtonId(Long receiverId, Long buttonId) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            receiverHandler.setLastActivatedButtonId(database, receiverId, buttonId);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
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
    @WorkerThread
    public void addScene(Scene scene) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            sceneHandler.add(database, scene);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Update existing Scene
     *
     * @param scene the edited Scene
     */
    @WorkerThread
    public void updateScene(Scene scene) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            sceneHandler.update(database, scene);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delete Scene from Database
     *
     * @param id ID of Scene
     */
    @WorkerThread
    public void deleteScene(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            sceneHandler.delete(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Scene from Database
     *
     * @param name Name of Scene
     *
     * @return Scene
     */
    @NonNull
    @WorkerThread
    public Scene getScene(String name) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return sceneHandler.get(database, name);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Scene from Database
     *
     * @param id ID of Scene
     *
     * @return Scene
     */
    @NonNull
    @WorkerThread
    public Scene getScene(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return sceneHandler.get(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all scenes of a specific Apartment.
     *
     * @return a list of scenes
     */
    @NonNull
    @WorkerThread
    public List<Scene> getScenes(Long apartmentId) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return sceneHandler.getByApartment(database, apartmentId);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all scenes from Database
     *
     * @return List of Scenes
     */
    @NonNull
    @WorkerThread
    public List<Scene> getAllScenes() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return sceneHandler.getAll(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
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
     *
     * @return ID of saved Database entry
     *
     * @throws GatewayAlreadyExistsException
     */
    @WorkerThread
    public long addGateway(Gateway gateway) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            long id = gatewayHandler.add(database, gateway);
            database.setTransactionSuccessful();
            return id;
        } catch (GatewayAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Enable existing Gateway
     *
     * @param id ID of Gateway
     */
    @WorkerThread
    public void enableGateway(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            gatewayHandler.enable(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Disable existing Gateway
     *
     * @param id ID of Gateway
     */
    @WorkerThread
    public void disableGateway(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            gatewayHandler.disable(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Update existing Gateway
     *
     * @param id           ID of Gateway
     * @param name         new Name of Gateway
     * @param model        new Model of Gateway
     * @param localAddress new local Address (Host) of Gateway
     * @param localPort    new local Port of Gateway
     * @param wanAddress   new WAN Address (Host) of Gateway
     * @param wanPort      new WAN Port of Gateway
     */
    @WorkerThread
    public void updateGateway(Long id, String name, String model, String localAddress, Integer localPort, String wanAddress, Integer wanPort,
                              Set<String> ssids) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            gatewayHandler.update(database, id, name, model, localAddress, localPort, wanAddress, wanPort, ssids);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delete Gateway from Database
     *
     * @param id ID of Gateway
     */
    @WorkerThread
    public void deleteGateway(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            gatewayHandler.delete(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get Gateway from Database
     *
     * @param id ID of Gateway
     *
     * @return Gateway
     */
    @NonNull
    @WorkerThread
    public Gateway getGateway(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return gatewayHandler.get(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all Gateways
     *
     * @return List of Gateways
     */
    @NonNull
    @WorkerThread
    public List<Gateway> getAllGateways() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return gatewayHandler.getAll(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all enabled/disabled Gateways
     *
     * @param isActive true if Gateway is enabled
     *
     * @return List of Gateways
     */
    @NonNull
    @WorkerThread
    public List<Gateway> getAllGateways(boolean isActive) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return gatewayHandler.getAll(database, isActive);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Checks if the gateway is associated with any apartment
     *
     * @param gateway gateway to check for associations
     *
     * @return true if associated with at least one apartment, false otherwise
     */
    @WorkerThread
    public boolean isAssociatedWithAnyApartment(Gateway gateway) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return gatewayHandler.isAssociatedWithAnyApartment(database, gateway.getId());
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
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
    @WorkerThread
    public void addReceiverWidget(ReceiverWidget receiverWidget) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            widgetHandler.addReceiverWidget(database, receiverWidget);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delete ReceiverWidget from Database
     *
     * @param id WidgetId
     */
    @WorkerThread
    public void deleteReceiverWidget(int id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            widgetHandler.deleteReceiverWidget(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get ReceiverWidget from Database
     *
     * @param id WidgetId
     */
    @NonNull
    @WorkerThread
    public ReceiverWidget getReceiverWidget(int id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return widgetHandler.getReceiverWidget(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Add RoomWidget to Database
     *
     * @param roomWidget WidgetInfo Object
     */
    @WorkerThread
    public void addRoomWidget(RoomWidget roomWidget) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            widgetHandler.addRoomWidget(database, roomWidget);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delete RoomWidget from Database
     *
     * @param id WidgetId
     */
    @WorkerThread
    public void deleteRoomWidget(int id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            widgetHandler.deleteRoomWidget(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get RoomWidget from Database
     *
     * @param id WidgetId
     */
    @NonNull
    @WorkerThread
    public RoomWidget getRoomWidget(int id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return widgetHandler.getRoomWidget(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Add SceneWidget to Database
     *
     * @param sceneWidget WidgetInfo Object
     */
    @WorkerThread
    public void addSceneWidget(SceneWidget sceneWidget) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            widgetHandler.addSceneWidget(database, sceneWidget);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delete SceneWidget from Database
     *
     * @param id WidgetId
     */
    @WorkerThread
    public void deleteSceneWidget(int id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            widgetHandler.deleteSceneWidget(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get SceneWidget from Database
     *
     * @param id WidgetId
     */
    @NonNull
    @WorkerThread
    public SceneWidget getSceneWidget(int id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            SceneWidget sceneWidget = widgetHandler.getSceneWidget(database, id);
            database.setTransactionSuccessful();
            return sceneWidget;
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }


    /**
     * Get Timer from Database
     *
     * @param id ID of Timer
     *
     * @return Timer
     */
    @NonNull
    @WorkerThread
    public Timer getTimer(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return timerHandler.get(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all Timers.
     *
     * @return a list of all Timers
     */
    @NonNull
    @WorkerThread
    public List<Timer> getAllTimers() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return timerHandler.getAll(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all active/inactive Timers.
     *
     * @param isActive true if Timer is active
     *
     * @return a list of all active/inactive Timers
     */
    @NonNull
    @WorkerThread
    public List<Timer> getAllTimers(boolean isActive) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return timerHandler.getAll(database, isActive);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Add Timer to Database
     *
     * @param timer Timer Object
     */
    @WorkerThread
    public long addTimer(Timer timer) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            long id = timerHandler.add(database, timer);
            database.setTransactionSuccessful();
            return id;
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Enable Timer
     *
     * @param id ID of Timer
     */
    @WorkerThread
    public void enableTimer(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            timerHandler.enable(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Disable Timer
     *
     * @param id ID of Timer
     */
    @WorkerThread
    public void disableTimer(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            timerHandler.disable(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Deletes Timer from Database
     *
     * @param id ID of Timer
     */
    @WorkerThread
    public void deleteTimer(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            timerHandler.delete(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Updates an existing Timer
     *
     * @param timer new Timer with same ID as old one
     */
    @WorkerThread
    public void updateTimer(Timer timer) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            timerHandler.update(database, timer);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     *
     * ////////////////////////////////
     * // Alarm Clock functions //
     * ////////////////////////////////
     *
     */

    /**
     * Get Actions for a specific alarm event
     *
     * @param event alarm event
     *
     * @return List of Actions
     */
    @NonNull
    @WorkerThread
    public List<Action> getAlarmActions(AlarmClockConstants.Event event) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return alarmClockHandler.getAlarmActions(database, event);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Set Actions for a specific alarm event
     *
     * @param event   alarm event
     * @param actions List of Actions
     */
    @WorkerThread
    public void setAlarmActions(AlarmClockConstants.Event event, List<Action> actions) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            alarmClockHandler.setAlarmActions(database, event, actions);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
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
     *
     * @return List of Actions
     */
    @NonNull
    @WorkerThread
    public List<Action> getAlarmActions(SleepAsAndroidConstants.Event event) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return sleepAsAndroidHandler.getAlarmActions(database, event);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Set Actions for a specific alarm event
     *
     * @param event   alarm event
     * @param actions List of Actions
     */
    @WorkerThread
    public void setAlarmActions(SleepAsAndroidConstants.Event event, List<Action> actions) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            sleepAsAndroidHandler.setAlarmActions(database, event, actions);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * ///////////////////////
     * // History functions //
     * ///////////////////////
     */

    /**
     * Gets all HistoryItems in Database, sorted by date/time
     *
     * @return List of HistoryItems
     */
    @NonNull
    @WorkerThread
    public LinkedList<HistoryItem> getHistory() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return historyHandler.getHistory(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delete entire History from Database
     */
    @WorkerThread
    public void clearHistory() throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            historyHandler.clear(database);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Adds a HistoryItem to database
     *
     * @param historyItem HistoryItem
     */
    @WorkerThread
    public void addHistoryItem(HistoryItem historyItem) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            historyHandler.add(database, historyItem);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
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
     *
     * @return Gateway
     */
    @Nullable
    @WorkerThread
    public Geofence getGeofence(Long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return geofenceHandler.get(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get a list of all Geofences
     *
     * @return list of Geofences
     */
    @NonNull
    @WorkerThread
    public List<Geofence> getAllGeofences() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return geofenceHandler.getAll(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get a list of all active/inactive Geofences
     *
     * @param isActive true if active, false otherwise
     *
     * @return list of Geofences
     */
    @NonNull
    @WorkerThread
    public List<Geofence> getAllGeofences(boolean isActive) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return geofenceHandler.getAll(database, isActive);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get a list of all custom Geofences
     *
     * @return list of custom Geofences
     */
    @NonNull
    @WorkerThread
    public List<Geofence> getCustomGeofences() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return geofenceHandler.getCustom(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Add Geofence to Database
     *
     * @param geofence new Geofence
     *
     * @return ID of saved Database entry
     */
    @WorkerThread
    public long addGeofence(Geofence geofence) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            long id = geofenceHandler.add(database, geofence);
            database.setTransactionSuccessful();
            return id;
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Update existing Geofence in Database
     *
     * @param geofence updated Geofence
     */
    @WorkerThread
    public void updateGeofence(Geofence geofence) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            geofenceHandler.update(database, geofence);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Enable existing Geofence
     *
     * @param id ID of Geofence
     */
    @WorkerThread
    public void enableGeofence(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            geofenceHandler.enable(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Disable existing Geofence
     *
     * @param id ID of Geofence
     */
    @WorkerThread
    public void disableGeofence(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            geofenceHandler.disable(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Disable all existing Geofences
     */
    @WorkerThread
    public void disableGeofences() throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            geofenceHandler.disableAll(database);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Update Geofence State
     */
    @WorkerThread
    public void updateState(Long id, @Geofence.State String state) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            geofenceHandler.updateState(database, id, state);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delete Geofence from Database
     *
     * @param id ID of Geofence
     */
    @WorkerThread
    public void deleteGeofence(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            geofenceHandler.delete(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * /////////////////////////
     * //// Phone functions ////
     * /////////////////////////
     */

    /**
     * Get a CallEvent from Database
     *
     * @param id ID of Call Event
     *
     * @return CallEvent
     */
    @NonNull
    @WorkerThread
    public CallEvent getCallEvent(long id) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return callEventHandler.get(database, id);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all CallEvents that react to the specified phone number
     *
     * @param phoneNumber phone number used in the Call Event
     *
     * @return List of CallEvents
     */
    public List<CallEvent> getCallEvents(String phoneNumber) throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return callEventHandler.get(database, phoneNumber);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Get all Call Events from Database
     *
     * @return List of CallEvents
     */
    @WorkerThread
    public List<CallEvent> getAllCallEvents() throws Exception {
        SQLiteDatabase database = openReadable();
        try {
            return callEventHandler.getAll(database);
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Add CallEvent to Database
     *
     * @param callEvent new Call Event
     *
     * @return ID of saved Database entry
     */
    @WorkerThread
    public long addCallEvent(CallEvent callEvent) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            long id = callEventHandler.add(database, callEvent);
            database.setTransactionSuccessful();
            return id;
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Delete Call Event from Database
     *
     * @param id ID of Call Event
     */
    @WorkerThread
    public void deleteCallEvent(Long id) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            callEventHandler.delete(database, id);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }

    /**
     * Update existing CallEvent in Database
     *
     * @param callEvent updated CallEvent
     */
    @WorkerThread
    public void updateCallEvent(CallEvent callEvent) throws Exception {
        SQLiteDatabase database = openWritable();
        try {
            callEventHandler.update(database, callEvent);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        } finally {
            close(database);
        }
    }
}
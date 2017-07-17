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

package eu.power_switch.persistence;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;
import java.util.Set;

import eu.power_switch.action.Action;
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

/**
 * Created by Markus on 15.07.2017.
 */
public interface PersistenceHandler {

    /**
     * Add Apartment to Database
     *
     * @param apartment Apartment
     *
     * @return ID of added Apartment
     */
    @WorkerThread
    long addApartment(Apartment apartment) throws Exception;

    /**
     * Deletes an Apartment from Database
     *
     * @param id ID of Apartment
     */
    @WorkerThread
    void deleteApartment(Long id) throws Exception;

    /**
     * Updates an Apartment in Database
     *
     * @param apartment updated Apartment
     */
    @WorkerThread
    void updateApartment(Apartment apartment) throws Exception;

    /**
     * Get an Apartment by Name
     *
     * @param name Name of Apartment
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    Apartment getApartment(String name) throws Exception;

    /**
     * Get an Apartment by Name, ignoring case
     *
     * @param name Name of Apartment
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    Apartment getApartmentCaseInsensitive(String name) throws Exception;

    /**
     * Get an Apartment by ID
     *
     * @param id ID of Apartment
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    Apartment getApartment(Long id) throws Exception;

    /**
     * Get the ID of an Apartment by its name, ignoring case
     *
     * @param name Name of Apartment
     *
     * @return ID of Apartment
     */
    @NonNull
    @WorkerThread
    Long getApartmentId(String name) throws Exception;

    /**
     * Get the Name of an Apartment by ID
     *
     * @param id ID of Apartment
     *
     * @return Name of Apartment
     */
    @NonNull
    @WorkerThread
    String getApartmentName(Long id) throws Exception;

    /**
     * Get all Apartments from Database
     *
     * @return List of Apartment Names
     *
     * @throws Exception
     */
    @NonNull
    @WorkerThread
    List<String> getAllApartmentNames() throws Exception;

    /**
     * Get all Apartments from Database
     *
     * @return List of Apartments
     *
     * @throws Exception
     */
    @NonNull
    @WorkerThread
    List<Apartment> getAllApartments() throws Exception;

    /**
     * Get all Apartments associated with the given gateway id from Database
     *
     * @return List of Apartments
     *
     * @throws Exception
     */
    @NonNull
    @WorkerThread
    List<Apartment> getAssociatedApartments(long gatewayId) throws Exception;

    /**
     * Get Apartment that contains a specific Receiver
     *
     * @param receiver Receiver
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    Apartment getContainingApartment(Receiver receiver) throws Exception;

    /**
     * Get Apartment that contains a specific Room
     *
     * @param room Room
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    Apartment getContainingApartment(Room room) throws Exception;

    /**
     * Get Apartment that contains a specific Scene
     *
     * @param scene Scene
     *
     * @return Apartment
     */
    @NonNull
    @WorkerThread
    Apartment getContainingApartment(Scene scene) throws Exception;

    /**
     * Save a room to the database.
     *
     * @param room the new room
     *
     * @return ID of added Room
     */
    @WorkerThread
    long addRoom(Room room) throws Exception;

    /**
     * Update an existing room.
     *
     * @param id      the ID of the Room
     * @param newName the new Name
     */
    @WorkerThread
    void updateRoom(Long id, String newName, List<Gateway> associatedGateways) throws Exception;

    /**
     * Update collapsed state of an existing room.
     *
     * @param id          the ID of the Room
     * @param isCollapsed the new Name
     */
    @WorkerThread
    void updateRoomCollapsed(Long id, boolean isCollapsed) throws Exception;

    /**
     * Sets the position of a Room
     *
     * @param roomId   ID of Room
     * @param position position in apartment
     */
    @WorkerThread
    void setPositionOfRoom(Long roomId, Long position) throws Exception;

    /**
     * Delte a room.
     *
     * @param id the ID of the room
     */
    @WorkerThread
    void deleteRoom(Long id) throws Exception;

    /**
     * Get a room object by its name.
     *
     * @param name the name of the room
     *
     * @return a room object
     */
    @NonNull
    @WorkerThread
    Room getRoom(String name) throws Exception;

    /**
     * Get a room object by its name, ignoring case
     *
     * @param name the name of the room
     *
     * @return a room object
     */
    @NonNull
    @WorkerThread
    Room getRoomCaseInsensitive(String name) throws Exception;

    /**
     * Get a room object by its ID.
     *
     * @param id the ID of the room
     *
     * @return a room object
     */
    @NonNull
    @WorkerThread
    Room getRoom(Long id) throws Exception;

    /**
     * Get the name of a room by its ID.
     *
     * @param id the ID of the room
     *
     * @return the name of a room object
     */
    String getRoomName(Long id) throws Exception;

    /**
     * Get all rooms.
     *
     * @return a list of all rooms
     */
    @NonNull
    @WorkerThread
    List<Room> getAllRooms() throws Exception;

    /**
     * Get all rooms of a specific Apartment.
     *
     * @return a list of rooms
     */
    @NonNull
    @WorkerThread
    List<Room> getRooms(Long apartmentId) throws Exception;

    /**
     * Get all room IDs of a specific Apartment.
     *
     * @return a list of room IDs
     */
    @NonNull
    @WorkerThread
    List<Long> getRoomIds(Long apartmentId) throws Exception;

    /**
     * Add Receiver to database
     *
     * @param receiver the new Receiver
     */
    @WorkerThread
    void addReceiver(Receiver receiver) throws Exception;

    /**
     * Updates a Receiver in database
     *
     * @param receiver the edited Receiver
     */
    @WorkerThread
    void updateReceiver(Receiver receiver) throws Exception;

    /**
     * Get Receiver by id
     *
     * @param id ID of the Receiver
     *
     * @return Receiver, can be null
     */
    @NonNull
    @WorkerThread
    Receiver getReceiver(Long id) throws Exception;

    /**
     * Get the name of a receiver by its ID.
     *
     * @param id the ID of the receiver
     *
     * @return the name of a receiver object
     */
    String getReceiverName(Long id) throws Exception;

    /**
     * Get all Receivers associated with a Room
     *
     * @param id ID of room
     *
     * @return List of Receivers
     */
    @NonNull
    @WorkerThread
    List<Receiver> getReceiverByRoomId(Long id) throws Exception;

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
    Receiver getReceiverByRoomId(Long roomId, String receiverName) throws Exception;

    /**
     * Sets the position of a Receiver
     *
     * @param receiverId ID of Receiver
     * @param position   position in room
     */
    @WorkerThread
    void setPositionOfReceiver(Long receiverId, Long position) throws Exception;

    /**
     * Get all Receivers in database
     *
     * @return List of Receivers
     */
    @NonNull
    @WorkerThread
    List<Receiver> getAllReceivers() throws Exception;

    /**
     * Delete Receiver from Database
     *
     * @param id ID of Receiver
     */
    @WorkerThread
    void deleteReceiver(Long id) throws Exception;

    /**
     * Get Button from Database
     *
     * @param id ID of Button
     *
     * @return Button
     */
    @NonNull
    @WorkerThread
    Button getButton(Long id) throws Exception;

    /**
     * Get Buttons associated with a Receiver
     *
     * @param receiverId ID of Receiver
     *
     * @return List of Buttons
     */
    @NonNull
    @WorkerThread
    List<UniversalButton> getButtons(Long receiverId) throws Exception;

    /**
     * Sets ID of last activated Button of a Receiver
     *
     * @param receiverId ID of Receiver
     * @param buttonId   ID of Button
     */
    @WorkerThread
    void setLastActivatedButtonId(Long receiverId, Long buttonId) throws Exception;

    /**
     * Add a scene to Database
     *
     * @param scene the new Scene
     */
    @WorkerThread
    void addScene(Scene scene) throws Exception;

    /**
     * Update existing Scene
     *
     * @param scene the edited Scene
     */
    @WorkerThread
    void updateScene(Scene scene) throws Exception;

    /**
     * Delete Scene from Database
     *
     * @param id ID of Scene
     */
    @WorkerThread
    void deleteScene(Long id) throws Exception;

    /**
     * Get Scene from Database
     *
     * @param name Name of Scene
     *
     * @return Scene
     */
    @NonNull
    @WorkerThread
    Scene getScene(String name) throws Exception;

    /**
     * Get Scene from Database
     *
     * @param id ID of Scene
     *
     * @return Scene
     */
    @NonNull
    @WorkerThread
    Scene getScene(Long id) throws Exception;

    /**
     * Get the name of a scene by its ID.
     *
     * @param id the ID of the scene
     *
     * @return the name of a scene object
     */
    String getSceneName(Long id) throws Exception;

    /**
     * Get all scenes of a specific Apartment.
     *
     * @return a list of scenes
     */
    @NonNull
    @WorkerThread
    List<Scene> getScenes(Long apartmentId) throws Exception;

    /**
     * Get all scenes from Database
     *
     * @return List of Scenes
     */
    @NonNull
    @WorkerThread
    List<Scene> getAllScenes() throws Exception;

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
    long addGateway(Gateway gateway) throws Exception;

    /**
     * Enable existing Gateway
     *
     * @param id ID of Gateway
     */
    @WorkerThread
    void enableGateway(Long id) throws Exception;

    /**
     * Disable existing Gateway
     *
     * @param id ID of Gateway
     */
    @WorkerThread
    void disableGateway(Long id) throws Exception;

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
    void updateGateway(Long id, String name, String model, String localAddress, Integer localPort, String wanAddress, Integer wanPort,
                       Set<String> ssids) throws Exception;

    /**
     * Delete Gateway from Database
     *
     * @param id ID of Gateway
     */
    @WorkerThread
    void deleteGateway(Long id) throws Exception;

    /**
     * Get Gateway from Database
     *
     * @param id ID of Gateway
     *
     * @return Gateway
     */
    @NonNull
    @WorkerThread
    Gateway getGateway(Long id) throws Exception;

    /**
     * Get all Gateways
     *
     * @return List of Gateways
     */
    @NonNull
    @WorkerThread
    List<Gateway> getAllGateways() throws Exception;

    /**
     * Get all enabled/disabled Gateways
     *
     * @param isActive true if Gateway is enabled
     *
     * @return List of Gateways
     */
    @NonNull
    @WorkerThread
    List<Gateway> getAllGateways(boolean isActive) throws Exception;

    /**
     * Checks if the gateway is associated with any apartment
     *
     * @param gateway gateway to check for associations
     *
     * @return true if associated with at least one apartment, false otherwise
     */
    @WorkerThread
    boolean isAssociatedWithAnyApartment(Gateway gateway) throws Exception;

    /**
     * Add ReceiverWidget to Database
     *
     * @param receiverWidget WidgetInfo Object
     */
    @WorkerThread
    void addReceiverWidget(ReceiverWidget receiverWidget) throws Exception;

    /**
     * Delete ReceiverWidget from Database
     *
     * @param id WidgetId
     */
    @WorkerThread
    void deleteReceiverWidget(int id) throws Exception;

    /**
     * Get ReceiverWidget from Database
     *
     * @param id WidgetId
     */
    @NonNull
    @WorkerThread
    ReceiverWidget getReceiverWidget(int id) throws Exception;

    /**
     * Add RoomWidget to Database
     *
     * @param roomWidget WidgetInfo Object
     */
    @WorkerThread
    void addRoomWidget(RoomWidget roomWidget) throws Exception;

    /**
     * Delete RoomWidget from Database
     *
     * @param id WidgetId
     */
    @WorkerThread
    void deleteRoomWidget(int id) throws Exception;

    /**
     * Get RoomWidget from Database
     *
     * @param id WidgetId
     */
    @NonNull
    @WorkerThread
    RoomWidget getRoomWidget(int id) throws Exception;

    /**
     * Add SceneWidget to Database
     *
     * @param sceneWidget WidgetInfo Object
     */
    @WorkerThread
    void addSceneWidget(SceneWidget sceneWidget) throws Exception;

    /**
     * Delete SceneWidget from Database
     *
     * @param id WidgetId
     */
    @WorkerThread
    void deleteSceneWidget(int id) throws Exception;

    /**
     * Get SceneWidget from Database
     *
     * @param id WidgetId
     */
    @NonNull
    @WorkerThread
    SceneWidget getSceneWidget(int id) throws Exception;

    /**
     * Get Timer from Database
     *
     * @param id ID of Timer
     *
     * @return Timer
     */
    @NonNull
    @WorkerThread
    Timer getTimer(Long id) throws Exception;

    /**
     * Get all Timers.
     *
     * @return a list of all Timers
     */
    @NonNull
    @WorkerThread
    List<Timer> getAllTimers() throws Exception;

    /**
     * Get all active/inactive Timers.
     *
     * @param isActive true if Timer is active
     *
     * @return a list of all active/inactive Timers
     */
    @NonNull
    @WorkerThread
    List<Timer> getAllTimers(boolean isActive) throws Exception;

    /**
     * Add Timer to Database
     *
     * @param timer Timer Object
     */
    @WorkerThread
    long addTimer(Timer timer) throws Exception;

    /**
     * Enable Timer
     *
     * @param id ID of Timer
     */
    @WorkerThread
    void enableTimer(Long id) throws Exception;

    /**
     * Disable Timer
     *
     * @param id ID of Timer
     */
    @WorkerThread
    void disableTimer(Long id) throws Exception;

    /**
     * Deletes Timer from Database
     *
     * @param id ID of Timer
     */
    @WorkerThread
    void deleteTimer(Long id) throws Exception;

    /**
     * Updates an existing Timer
     *
     * @param timer new Timer with same ID as old one
     */
    @WorkerThread
    void updateTimer(Timer timer) throws Exception;

    /**
     * Get Actions for a specific alarm event
     *
     * @param event alarm event
     *
     * @return List of Actions
     */
    @NonNull
    @WorkerThread
    List<Action> getAlarmActions(AlarmClockConstants.Event event) throws Exception;

    /**
     * Set Actions for a specific alarm event
     *
     * @param event   alarm event
     * @param actions List of Actions
     */
    @WorkerThread
    void setAlarmActions(AlarmClockConstants.Event event, List<Action> actions) throws Exception;

    /**
     * Get Actions for a specific alarm event
     *
     * @param event alarm event
     *
     * @return List of Actions
     */
    @NonNull
    @WorkerThread
    List<Action> getAlarmActions(SleepAsAndroidConstants.Event event) throws Exception;

    /**
     * Set Actions for a specific alarm event
     *
     * @param event   alarm event
     * @param actions List of Actions
     */
    @WorkerThread
    void setAlarmActions(SleepAsAndroidConstants.Event event, List<Action> actions) throws Exception;

    /**
     * Gets all HistoryItems in Database, sorted by date/time
     *
     * @return List of HistoryItems
     */
    @NonNull
    @WorkerThread
    List<HistoryItem> getHistory() throws Exception;

    /**
     * Delete entire History from Database
     */
    @WorkerThread
    void clearHistory() throws Exception;

    /**
     * Adds a HistoryItem to database
     *
     * @param historyItem HistoryItem
     */
    @WorkerThread
    void addHistoryItem(HistoryItem historyItem) throws Exception;

    /**
     * Get Gateway from Database
     *
     * @param id ID of Gateway
     *
     * @return Gateway
     */
    @Nullable
    @WorkerThread
    Geofence getGeofence(Long id) throws Exception;

    /**
     * Get a list of all Geofences
     *
     * @return list of Geofences
     */
    @NonNull
    @WorkerThread
    List<Geofence> getAllGeofences() throws Exception;

    /**
     * Get a list of all active/inactive Geofences
     *
     * @param isActive true if active, false otherwise
     *
     * @return list of Geofences
     */
    @NonNull
    @WorkerThread
    List<Geofence> getAllGeofences(boolean isActive) throws Exception;

    /**
     * Get a list of all custom Geofences
     *
     * @return list of custom Geofences
     */
    @NonNull
    @WorkerThread
    List<Geofence> getCustomGeofences() throws Exception;

    /**
     * Add Geofence to Database
     *
     * @param geofence new Geofence
     *
     * @return ID of saved Database entry
     */
    @WorkerThread
    long addGeofence(Geofence geofence) throws Exception;

    /**
     * Update existing Geofence in Database
     *
     * @param geofence updated Geofence
     */
    @WorkerThread
    void updateGeofence(Geofence geofence) throws Exception;

    /**
     * Enable existing Geofence
     *
     * @param id ID of Geofence
     */
    @WorkerThread
    void enableGeofence(Long id) throws Exception;

    /**
     * Disable existing Geofence
     *
     * @param id ID of Geofence
     */
    @WorkerThread
    void disableGeofence(Long id) throws Exception;

    /**
     * Disable all existing Geofences
     */
    @WorkerThread
    void disableGeofences() throws Exception;

    /**
     * Update Geofence State
     */
    @WorkerThread
    void updateState(Long id, @Geofence.State String state) throws Exception;

    /**
     * Delete Geofence from Database
     *
     * @param id ID of Geofence
     */
    @WorkerThread
    void deleteGeofence(Long id) throws Exception;

    /**
     * Get a CallEvent from Database
     *
     * @param id ID of Call Event
     *
     * @return CallEvent
     */
    @NonNull
    @WorkerThread
    CallEvent getCallEvent(long id) throws Exception;

    /**
     * Get all CallEvents that react to the specified phone number
     *
     * @param phoneNumber phone number used in the Call Event
     *
     * @return List of CallEvents
     */
    List<CallEvent> getCallEvents(String phoneNumber) throws Exception;

    /**
     * Get all Call Events from Database
     *
     * @return List of CallEvents
     */
    @WorkerThread
    List<CallEvent> getAllCallEvents() throws Exception;

    /**
     * Add CallEvent to Database
     *
     * @param callEvent new Call Event
     *
     * @return ID of saved Database entry
     */
    @WorkerThread
    long addCallEvent(CallEvent callEvent) throws Exception;

    /**
     * Delete Call Event from Database
     *
     * @param id ID of Call Event
     */
    @WorkerThread
    void deleteCallEvent(Long id) throws Exception;

    /**
     * Update existing CallEvent in Database
     *
     * @param callEvent updated CallEvent
     */
    @WorkerThread
    void updateCallEvent(CallEvent callEvent) throws Exception;
}

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

package eu.power_switch.persistence.data.demo_mode;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.action.Action;
import eu.power_switch.action.ReceiverAction;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.history.HistoryItem;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.UniversalButton;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.button.OffButton;
import eu.power_switch.obj.button.OnButton;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.device.intertechno.CMR1000;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.AlarmClockConstants;
import eu.power_switch.shared.constants.PhoneConstants;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;
import eu.power_switch.widget.ReceiverWidget;
import eu.power_switch.widget.RoomWidget;
import eu.power_switch.widget.SceneWidget;

/**
 * This Class represents a demo Room/Receiver/Scene setup for use in Play Store images
 * <p/>
 * Created by Markus on 30.07.2015.
 */
@Singleton
public class DemoModePersistenceHandler implements PersistenceHandler {

    private Context context;

    private List<Apartment> apartments = new ArrayList<>();
    private List<Gateway>   gateways   = new ArrayList<>();

    private List<Room> rooms_heimat = new ArrayList<>();

    private List<Scene> scenes_heimat = new ArrayList<>();

    // Apartments
    private Apartment apartment_heimat;
    private Apartment apartment_eltern;
    // Scenes
    private Scene scene_kinoabend   = new Scene((long) 0, (long) 0, "Kinoabend");
    private Scene scene_abendessen  = new Scene((long) 1, (long) 0, "Abendessen");
    private Scene scene_feier       = new Scene((long) 2, (long) 0, "Feier");
    // Rooms
    private Room  room_wohnzimmer   = new Room((long) 0, (long) 0, "Wohnzimmer", 0, false, new ArrayList<Gateway>());
    private Room  room_schlafzimmer = new Room((long) 1, (long) 0, "Schlafzimmer", 0, false, new ArrayList<Gateway>());
    private Room  room_kueche       = new Room((long) 2, (long) 0, "Küche", 0, false, new ArrayList<Gateway>());
    private Room  room_kinderzimmer = new Room((long) 3, (long) 0, "Kinderzimmer", 0, false, new ArrayList<Gateway>());
    private Room  room_garten       = new Room((long) 4, (long) 0, "Garten", 0, false, new ArrayList<Gateway>());
    // Receiver
    private Receiver receiver_sofa_wohnzimmer;
    private Receiver receiver_ecklampe_wohnzimmer;
    private Receiver receiver_verstaerker_wohnzimmer;
    private Receiver RECEIVER_DECKE_SCHLAFZIMMER;
    private Receiver RECEIVER_FENSTER_SCHLAFZIMMER;
    private Receiver RECEIVER_NACHTTISCHE_SCHLAFZIMMER;
    private Receiver RECEIVER_ABZUGSHAUBE_KUECHE;
    private Receiver RECEIVER_ESSTISCH_KUECHE;
    private Receiver RECEIVER_ARBEITSFLAECHE_KUECHE;
    private Receiver RECEIVER_KAFFEEMASCHINE_KUECHE;
    private Receiver RECEIVER_DECKE_KINDERZIMMER;
    private Receiver RECEIVER_NACHTLICHT_KINDERZIMMER;
    private Receiver RECEIVER_TERRASSE_GARTEN;
    private Receiver RECEIVER_WEGBELEUCHTUNG_GARTEN;
    private Receiver RECEIVER_HINTERHAUS_GARTEN;
    private Receiver RECEIVER_WEIHNACHTSDEKO_GARTEN;

    /**
     * Default constructor
     */
    @Inject
    public DemoModePersistenceHandler(Context context) {
        this.context = context;

        initApartments();
        initReceivers();
        initRooms();
        initScenes();
        initGateways();
    }

    private void initApartments() {
        apartments.clear();

        HashMap<Geofence.EventType, List<Action>> actionsMap = new HashMap<>();
        actionsMap.put(Geofence.EventType.ENTER, new ArrayList<Action>());

        apartment_heimat = new Apartment((long) 0, true, "Heimat", rooms_heimat, scenes_heimat, gateways,
                new Geofence((long) 0, true, "Heimat", new LatLng(52.437418, 13.373122), 100, null, actionsMap, Geofence.STATE_NONE));
        apartment_eltern = new Apartment((long) 0, false, "Eltern", rooms_heimat, scenes_heimat, gateways,
                new Geofence((long) 0, true, "Eltern", new LatLng(52.437418, 13.573122), 500, null, actionsMap, Geofence.STATE_NONE));
        apartments.add(apartment_heimat);
        apartments.add(apartment_eltern);
    }

    private void initGateways() {
        gateways.clear();


        gateways.add(new ConnAir((long) 0,
                true,
                "AutoDiscovered",
                "1.0",
                "192.168.2.125",
                49880,
                "example.myfritz.dyndns.org",
                49880,
                new HashSet<>(Arrays.asList("FritzBox 7272"))));
        gateways.add(new ITGW433((long) 1,
                true,
                "AutoDiscovered",
                "1.0",
                "192.168.2.148",
                49880,
                "example.myfritz.dyndns.org",
                49881,
                Collections.<String>emptySet()));
        gateways.add(new BrematicGWY433((long) 2,
                true,
                "AutoDiscovered",
                "1.0",
                "192.168.2.189",
                49880,
                "example.myfritz.dyndns.org",
                49882,
                Collections.<String>emptySet()));
    }

    private void initReceivers() {
        receiver_sofa_wohnzimmer = new CMR1000(context, (long) 0, "Sofa", 'E', 1, room_wohnzimmer.getId(), new ArrayList<Gateway>());
        receiver_ecklampe_wohnzimmer = new CMR1000(context, (long) 1, "Ecklampe", 'E', 1, room_wohnzimmer.getId(), new ArrayList<Gateway>());
        receiver_verstaerker_wohnzimmer = new CMR1000(context, (long) 2, "Verstärker", 'E', 1, room_wohnzimmer.getId(), new ArrayList<Gateway>());
        RECEIVER_DECKE_SCHLAFZIMMER = new CMR1000(context, (long) 3, "Decke", 'E', 1, room_schlafzimmer.getId(), new ArrayList<Gateway>());
        RECEIVER_FENSTER_SCHLAFZIMMER = new CMR1000(context, (long) 4, "Fenster", 'E', 1, room_schlafzimmer.getId(), new ArrayList<Gateway>());
        RECEIVER_NACHTTISCHE_SCHLAFZIMMER = new CMR1000(context,
                (long) 5,
                "Nachttische", 'E', 1, room_schlafzimmer.getId(),
                new ArrayList<Gateway>());
        RECEIVER_ABZUGSHAUBE_KUECHE = new CMR1000(context, (long) 6, "Abzugshaube", 'E', 1, room_kueche.getId(), new ArrayList<Gateway>());
        RECEIVER_ESSTISCH_KUECHE = new CMR1000(context, (long) 7, "Esstisch", 'E', 1, room_kueche.getId(), new ArrayList<Gateway>());
        RECEIVER_ARBEITSFLAECHE_KUECHE = new CMR1000(context, (long) 8, "Arbeitsfläche", 'E', 1, room_kueche.getId(), new ArrayList<Gateway>());
        RECEIVER_KAFFEEMASCHINE_KUECHE = new CMR1000(context, (long) 9, "Kaffeemaschine", 'E', 1, room_kueche.getId(), new ArrayList<Gateway>());
        RECEIVER_DECKE_KINDERZIMMER = new CMR1000(context, (long) 10, "Decke", 'E', 1, room_kinderzimmer.getId(), new ArrayList<Gateway>());
        RECEIVER_NACHTLICHT_KINDERZIMMER = new CMR1000(context, (long) 11, "Nachtlicht", 'E', 1, room_kinderzimmer.getId(), new ArrayList<Gateway>());
        RECEIVER_TERRASSE_GARTEN = new CMR1000(context, (long) 12, "Terrasse", 'E', 1, room_garten.getId(), new ArrayList<Gateway>());
        RECEIVER_WEGBELEUCHTUNG_GARTEN = new CMR1000(context, (long) 13, "Wegbeleuchtung", 'E', 1, room_garten.getId(), new ArrayList<Gateway>());
        RECEIVER_HINTERHAUS_GARTEN = new CMR1000(context, (long) 14, "Hinterhaus", 'E', 1, room_garten.getId(), new ArrayList<Gateway>());
        RECEIVER_WEIHNACHTSDEKO_GARTEN = new CMR1000(context, (long) 15, "Weihnachtsdeko", 'E', 1, room_garten.getId(), new ArrayList<Gateway>());
    }

    private void initScenes() {
        scene_kinoabend.getSceneItems()
                .clear();
        scene_kinoabend.addSceneItem(receiver_sofa_wohnzimmer, receiver_sofa_wohnzimmer.getButton(OnButton.ID));
        scene_kinoabend.addSceneItem(receiver_ecklampe_wohnzimmer, receiver_ecklampe_wohnzimmer.getButton(OffButton.ID));
        scene_kinoabend.addSceneItem(receiver_verstaerker_wohnzimmer, receiver_verstaerker_wohnzimmer.getButton(OnButton.ID));

        scene_abendessen.getSceneItems()
                .clear();
        scene_abendessen.addSceneItem(RECEIVER_ESSTISCH_KUECHE, RECEIVER_ESSTISCH_KUECHE.getButton(OnButton.ID));
        scene_abendessen.addSceneItem(RECEIVER_ABZUGSHAUBE_KUECHE, RECEIVER_ABZUGSHAUBE_KUECHE.getButton(OffButton.ID));
        scene_abendessen.addSceneItem(RECEIVER_ARBEITSFLAECHE_KUECHE, RECEIVER_ARBEITSFLAECHE_KUECHE.getButton(OffButton.ID));

        scene_feier.getSceneItems()
                .clear();
        scene_feier.addSceneItem(RECEIVER_WEGBELEUCHTUNG_GARTEN, RECEIVER_WEGBELEUCHTUNG_GARTEN.getButton(OnButton.ID));
        scene_feier.addSceneItem(RECEIVER_ESSTISCH_KUECHE, RECEIVER_ESSTISCH_KUECHE.getButton(OnButton.ID));
        scene_feier.addSceneItem(receiver_sofa_wohnzimmer, receiver_sofa_wohnzimmer.getButton(OnButton.ID));
        scene_feier.addSceneItem(RECEIVER_TERRASSE_GARTEN, RECEIVER_TERRASSE_GARTEN.getButton(OnButton.ID));

        scenes_heimat.clear();
        scenes_heimat.add(scene_kinoabend);
        scenes_heimat.add(scene_abendessen);
        scenes_heimat.add(scene_feier);
    }

    private void initRooms() {
        room_wohnzimmer.getReceivers()
                .clear();
        room_wohnzimmer.addReceiver(receiver_sofa_wohnzimmer);
        room_wohnzimmer.addReceiver(receiver_ecklampe_wohnzimmer);
        room_wohnzimmer.addReceiver(receiver_verstaerker_wohnzimmer);

        room_schlafzimmer.getReceivers()
                .clear();
        room_schlafzimmer.addReceiver(RECEIVER_DECKE_SCHLAFZIMMER);
        room_schlafzimmer.addReceiver(RECEIVER_FENSTER_SCHLAFZIMMER);
        room_schlafzimmer.addReceiver(RECEIVER_NACHTTISCHE_SCHLAFZIMMER);

        room_kueche.getReceivers()
                .clear();
        room_kueche.addReceiver(RECEIVER_ABZUGSHAUBE_KUECHE);
        room_kueche.addReceiver(RECEIVER_ESSTISCH_KUECHE);
        room_kueche.addReceiver(RECEIVER_ARBEITSFLAECHE_KUECHE);
        room_kueche.addReceiver(RECEIVER_KAFFEEMASCHINE_KUECHE);

        room_kinderzimmer.getReceivers()
                .clear();
        room_kinderzimmer.addReceiver(RECEIVER_DECKE_KINDERZIMMER);
        room_kinderzimmer.addReceiver(RECEIVER_NACHTLICHT_KINDERZIMMER);

        room_garten.getReceivers()
                .clear();
        room_garten.addReceiver(RECEIVER_TERRASSE_GARTEN);
        room_garten.addReceiver(RECEIVER_WEGBELEUCHTUNG_GARTEN);
        room_garten.addReceiver(RECEIVER_HINTERHAUS_GARTEN);
        room_garten.addReceiver(RECEIVER_WEIHNACHTSDEKO_GARTEN);

        rooms_heimat.clear();
        rooms_heimat.add(room_wohnzimmer);
        rooms_heimat.add(room_schlafzimmer);
        rooms_heimat.add(room_kueche);
        rooms_heimat.add(room_kinderzimmer);
        rooms_heimat.add(room_garten);
    }

    @Override
    public long addApartment(Apartment apartment) throws Exception {
        return 0;
    }

    @Override
    public void deleteApartment(Long id) throws Exception {

    }

    @Override
    public void updateApartment(Apartment apartment) throws Exception {

    }

    @NonNull
    @Override
    public Apartment getApartment(String name) throws Exception {
        for (Apartment apartment : getAllApartments()) {
            if (apartment.getName()
                    .equals(name)) {
                return apartment;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public Apartment getApartmentCaseInsensitive(String name) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public Apartment getApartment(Long id) throws Exception {
        for (Apartment apartment : getAllApartments()) {
            if (apartment.getId()
                    .equals(id)) {
                return apartment;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public Long getApartmentId(String name) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public String getApartmentName(Long id) throws Exception {
        return getApartment(id).getName();
    }

    @NonNull
    @Override
    public List<String> getAllApartmentNames() throws Exception {
        List<String> names = new ArrayList<>();
        for (Apartment apartment : getAllApartments()) {
            names.add(apartment.getName());
        }

        return names;
    }

    @NonNull
    @Override
    public List<Apartment> getAllApartments() throws Exception {
        List<Apartment> apartments = new ArrayList<>();

        apartments.add(apartment_heimat);
        apartments.add(apartment_eltern);

        return apartments;
    }

    @NonNull
    @Override
    public List<Apartment> getAssociatedApartments(long gatewayId) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public Apartment getContainingApartment(Receiver receiver) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public Apartment getContainingApartment(Room room) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public Apartment getContainingApartment(Scene scene) throws Exception {
        return null;
    }

    @Override
    public long addRoom(Room room) throws Exception {
        return 0;
    }

    @Override
    public void updateRoom(Long id, String newName, List<Gateway> associatedGateways) throws Exception {

    }

    @Override
    public void updateRoomCollapsed(Long id, boolean isCollapsed) throws Exception {

    }

    @Override
    public void setPositionOfRoom(Long roomId, Long position) throws Exception {

    }

    @Override
    public void deleteRoom(Long id) throws Exception {

    }

    @NonNull
    @Override
    public Room getRoom(String name) throws Exception {
        for (Room room : getAllRooms()) {
            if (room.getName()
                    .equals(name)) {
                return room;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public Room getRoomCaseInsensitive(String name) throws Exception {
        for (Room room : getAllRooms()) {
            if (room.getName()
                    .equalsIgnoreCase(name)) {
                return room;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public Room getRoom(Long id) throws Exception {
        for (Apartment apartment : getAllApartments()) {
            for (Room room : apartment.getRooms()) {
                if (room.getId()
                        .equals(id)) {
                    return room;
                }
            }
        }

        return null;
    }

    @Override
    public String getRoomName(Long id) throws Exception {
        return getRoom(id).getName();
    }

    @NonNull
    @Override
    public List<Room> getAllRooms() throws Exception {
        List<Room> rooms = new ArrayList<>();
        for (Apartment apartment : getAllApartments()) {
            rooms.addAll(apartment.getRooms());
        }

        return rooms;
    }

    @NonNull
    @Override
    public List<Room> getRooms(Long apartmentId) throws Exception {
        List<Room> rooms = new ArrayList<>();

        for (Room room : getAllRooms()) {
            if (room.getApartmentId()
                    .equals(apartmentId)) {
                rooms.add(room);
            }
        }

        return rooms;
    }

    @NonNull
    @Override
    public List<Long> getRoomIds(Long apartmentId) throws Exception {
        return null;
    }

    @Override
    public void addReceiver(Receiver receiver) throws Exception {

    }

    @Override
    public void updateReceiver(Receiver receiver) throws Exception {

    }

    @NonNull
    @Override
    public Receiver getReceiver(Long id) throws Exception {
        for (Receiver receiver : getAllReceivers()) {
            if (receiver.getId()
                    .equals(id)) {
                return receiver;
            }
        }

        return null;
    }

    @Override
    public String getReceiverName(Long id) throws Exception {
        return getReceiver(id).getName();
    }

    @NonNull
    @Override
    public List<Receiver> getReceiverByRoomId(Long id) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public Receiver getReceiverByRoomId(Long roomId, String receiverName) throws Exception {
        return null;
    }

    @Override
    public void setPositionOfReceiver(Long receiverId, Long position) throws Exception {

    }

    @NonNull
    @Override
    public List<Receiver> getAllReceivers() throws Exception {
        List<Receiver> receivers = new ArrayList<>();
        for (Room room : getAllRooms()) {
            receivers.addAll(room.getReceivers());
        }

        return receivers;
    }

    @Override
    public void deleteReceiver(Long id) throws Exception {

    }

    @NonNull
    @Override
    public Button getButton(Long id) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public List<UniversalButton> getButtons(Long receiverId) throws Exception {
        return null;
    }

    @Override
    public void setLastActivatedButtonId(Long receiverId, Long buttonId) throws Exception {

    }

    @Override
    public void addScene(Scene scene) throws Exception {

    }

    @Override
    public void updateScene(Scene scene) throws Exception {

    }

    @Override
    public void deleteScene(Long id) throws Exception {

    }

    @NonNull
    @Override
    public Scene getScene(String name) throws Exception {
        for (Scene scene : getAllScenes()) {
            if (scene.getName()
                    .equals(name)) {
                return scene;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public Scene getScene(Long id) throws Exception {
        for (Scene scene : getAllScenes()) {
            if (scene.getId()
                    .equals(id)) {
                return scene;
            }
        }

        return null;
    }

    @Override
    public String getSceneName(Long id) throws Exception {
        return getScene(id).getName();
    }

    @NonNull
    @Override
    public List<Scene> getScenes(Long apartmentId) throws Exception {
        return getApartment(apartmentId).getScenes();
    }

    @NonNull
    @Override
    public List<Scene> getAllScenes() throws Exception {
        List<Scene> scenes = new ArrayList<>();
        for (Apartment apartment : getAllApartments()) {
            scenes.addAll(apartment.getScenes());
        }

        return scenes;
    }

    @Override
    public long addGateway(Gateway gateway) throws Exception {
        return 0;
    }

    @Override
    public void enableGateway(Long id) throws Exception {

    }

    @Override
    public void disableGateway(Long id) throws Exception {

    }

    @Override
    public void updateGateway(Long id, String name, String model, String localAddress, Integer localPort, String wanAddress, Integer wanPort,
                              Set<String> ssids) throws Exception {

    }

    @Override
    public void deleteGateway(Long id) throws Exception {

    }

    @NonNull
    @Override
    public Gateway getGateway(Long id) throws Exception {
        for (Gateway gateway : getAllGateways()) {
            if (gateway.getId()
                    .equals(id)) {
                return gateway;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public List<Gateway> getAllGateways() throws Exception {
        return gateways;
    }

    @NonNull
    @Override
    public List<Gateway> getAllGateways(boolean isActive) throws Exception {
        List<Gateway> activeGateways = new ArrayList<>();
        for (Gateway gateway : getAllGateways()) {
            if (gateway.isActive()) {
                activeGateways.add(gateway);
            }
        }

        return activeGateways;
    }

    @Override
    public boolean isAssociatedWithAnyApartment(Gateway gateway) throws Exception {
        return false;
    }

    @Override
    public void addReceiverWidget(ReceiverWidget receiverWidget) throws Exception {

    }

    @Override
    public void deleteReceiverWidget(int id) throws Exception {

    }

    @NonNull
    @Override
    public ReceiverWidget getReceiverWidget(int id) throws Exception {
        return null;
    }

    @Override
    public void addRoomWidget(RoomWidget roomWidget) throws Exception {

    }

    @Override
    public void deleteRoomWidget(int id) throws Exception {

    }

    @NonNull
    @Override
    public RoomWidget getRoomWidget(int id) throws Exception {
        return null;
    }

    @Override
    public void addSceneWidget(SceneWidget sceneWidget) throws Exception {

    }

    @Override
    public void deleteSceneWidget(int id) throws Exception {

    }

    @NonNull
    @Override
    public SceneWidget getSceneWidget(int id) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public Timer getTimer(Long id) throws Exception {
        for (Timer timer : getAllTimers()) {
            if (timer.getId()
                    .equals(id)) {
                return timer;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public List<Timer> getAllTimers() throws Exception {
        List<Timer> timers = new ArrayList<>();

        ReceiverAction timerReceiverAction = new ReceiverAction(0,
                apartment_heimat.getId(), null,
                room_wohnzimmer.getId(), null,
                receiver_ecklampe_wohnzimmer.getId(), null,
                receiver_ecklampe_wohnzimmer.getButton(OnButton.ID)
                        .getId());
        List<WeekdayTimer.Day> days = new ArrayList<>();
        days.add(WeekdayTimer.Day.MONDAY);
        days.add(WeekdayTimer.Day.TUESDAY);
        days.add(WeekdayTimer.Day.WEDNESDAY);
        days.add(WeekdayTimer.Day.THURSDAY);
        days.add(WeekdayTimer.Day.FRIDAY);
        days.add(WeekdayTimer.Day.SATURDAY);
        days.add(WeekdayTimer.Day.SUNDAY);

        List<Action> actions = new ArrayList<>();
        actions.add(timerReceiverAction);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 0);

        timers.add(new WeekdayTimer(0, true, "Abendlicht", c, 0, days, actions));


        ReceiverAction timerReceiverAction2 = new ReceiverAction(0,
                apartment_heimat.getId(), null,
                room_kueche.getId(), null,
                RECEIVER_KAFFEEMASCHINE_KUECHE.getId(), null,
                receiver_ecklampe_wohnzimmer.getButton(OnButton.ID)
                        .getId());

        List<Action> actions2 = new ArrayList<>();
        actions2.add(timerReceiverAction2);

        List<WeekdayTimer.Day> days2 = new ArrayList<>();
        days2.add(WeekdayTimer.Day.MONDAY);
        days2.add(WeekdayTimer.Day.TUESDAY);
        days2.add(WeekdayTimer.Day.WEDNESDAY);
        days2.add(WeekdayTimer.Day.THURSDAY);
        days2.add(WeekdayTimer.Day.FRIDAY);

        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.HOUR_OF_DAY, 6);
        c2.set(Calendar.MINUTE, 30);

        timers.add(new WeekdayTimer(1, true, "Morgenkaffee", c2, 0, days2, actions2));

        return timers;
    }

    @NonNull
    @Override
    public List<Timer> getAllTimers(boolean isActive) throws Exception {
        List<Timer> activeTimers = new ArrayList<>();
        for (Timer timer : getAllTimers()) {
            if (timer.isActive()) {
                activeTimers.add(timer);
            }
        }

        return activeTimers;
    }

    @Override
    public long addTimer(Timer timer) throws Exception {
        return 0;
    }

    @Override
    public void enableTimer(Long id) throws Exception {

    }

    @Override
    public void disableTimer(Long id) throws Exception {

    }

    @Override
    public void deleteTimer(Long id) throws Exception {

    }

    @Override
    public void updateTimer(Timer timer) throws Exception {

    }

    /**
     * Get a list of Stock Alarm Actions
     *
     * @param eventType Alarm Event Type
     *
     * @return List of Actions for specified Event Type
     */
    public List<Action> getAlarmActions(AlarmClockConstants.Event eventType) {
        List<Action> actions = new ArrayList<>();

        switch (eventType) {
            case ALARM_TRIGGERED:
                actions.add(new ReceiverAction(-1, apartment_heimat.getId(), null, room_schlafzimmer.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OnButton.ID)
                                .getId()));
                break;
            case ALARM_SNOOZED:
                actions.add(new ReceiverAction(-1, apartment_heimat.getId(), null, room_schlafzimmer.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OffButton.ID)
                                .getId()));
                break;
            case ALARM_DISMISSED:
                actions.add(new ReceiverAction(-1, apartment_heimat.getId(), null, room_schlafzimmer.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OnButton.ID)
                                .getId()));
                actions.add(new ReceiverAction(-1, apartment_heimat.getId(), null, room_schlafzimmer.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getId(), null,
                        RECEIVER_FENSTER_SCHLAFZIMMER.getButton(OnButton.ID)
                                .getId()));
                break;
        }

        return actions;
    }

    @Override
    public void setAlarmActions(AlarmClockConstants.Event event, List<Action> actions) throws Exception {

    }

    @NonNull
    @Override
    public List<Action> getAlarmActions(SleepAsAndroidConstants.Event event) throws Exception {
        List<Action> actions = new ArrayList<>();

        switch (event) {
            case ALARM_TRIGGERED:
                actions.add(new ReceiverAction(-1, apartment_heimat.getId(), null, room_schlafzimmer.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OnButton.ID)
                                .getId()));
                break;
            case ALARM_SNOOZED:
                actions.add(new ReceiverAction(-1, apartment_heimat.getId(), null, room_schlafzimmer.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OffButton.ID)
                                .getId()));
                break;
            case ALARM_DISMISSED:
                actions.add(new ReceiverAction(-1, apartment_heimat.getId(), null, room_schlafzimmer.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OnButton.ID)
                                .getId()));
                actions.add(new ReceiverAction(-1, apartment_heimat.getId(), null, room_schlafzimmer.getId(), null,
                        RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getId(), null,
                        RECEIVER_FENSTER_SCHLAFZIMMER.getButton(OnButton.ID)
                                .getId()));
                break;
        }

        return actions;
    }

    @Override
    public void setAlarmActions(SleepAsAndroidConstants.Event event, List<Action> actions) throws Exception {

    }

    @NonNull
    @Override
    public List<HistoryItem> getHistory() throws Exception {
        return new ArrayList<>();
    }

    @Override
    public void clearHistory() throws Exception {

    }

    @Override
    public void addHistoryItem(HistoryItem historyItem) throws Exception {

    }

    @Nullable
    @Override
    public Geofence getGeofence(Long id) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public List<Geofence> getAllGeofences() throws Exception {
        return null;
    }

    @NonNull
    @Override
    public List<Geofence> getAllGeofences(boolean isActive) throws Exception {
        return null;
    }

    @NonNull
    @Override
    public List<Geofence> getCustomGeofences() throws Exception {
        return null;
    }

    @Override
    public long addGeofence(Geofence geofence) throws Exception {
        return 0;
    }

    @Override
    public void updateGeofence(Geofence geofence) throws Exception {

    }

    @Override
    public void enableGeofence(Long id) throws Exception {

    }

    @Override
    public void disableGeofence(Long id) throws Exception {

    }

    @Override
    public void disableGeofences() throws Exception {

    }

    @Override
    public void updateState(Long id, @Geofence.State String state) throws Exception {

    }

    @Override
    public void deleteGeofence(Long id) throws Exception {

    }

    @NonNull
    @Override
    public CallEvent getCallEvent(long id) throws Exception {
        return null;
    }

    @Override
    public List<CallEvent> getCallEvents(String phoneNumber) throws Exception {
        return null;
    }

    @Override
    public List<CallEvent> getAllCallEvents() throws Exception {
        List<CallEvent> callEvents = new ArrayList<>();

        Map<PhoneConstants.CallType, Set<String>> phoneNumbersMap = new HashMap<>();
        Set<String>                               phoneNumbers    = new HashSet<>();
        phoneNumbers.add("0174 37 97 508");
        phoneNumbersMap.put(PhoneConstants.CallType.INCOMING, phoneNumbers);

        List<Action> actions = new ArrayList<>();
        actions.add(new ReceiverAction(-1, apartment_heimat.getId(), null, room_schlafzimmer.getId(), null,
                RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getId(), null,
                RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OnButton.ID)
                        .getId()));

        Map<PhoneConstants.CallType, List<Action>> actionsMap = new HashMap<>();
        actionsMap.put(PhoneConstants.CallType.INCOMING, actions);

        CallEvent callEvent = new CallEvent(0, true, "Call Event 1", phoneNumbersMap, actionsMap);


        callEvents.add(callEvent);

        return callEvents;
    }

    @Override
    public long addCallEvent(CallEvent callEvent) throws Exception {
        return 0;
    }

    @Override
    public void deleteCallEvent(Long id) throws Exception {

    }

    @Override
    public void updateCallEvent(CallEvent callEvent) throws Exception {

    }

}

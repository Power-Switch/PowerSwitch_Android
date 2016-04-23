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

package eu.power_switch.developer;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eu.power_switch.action.Action;
import eu.power_switch.action.ReceiverAction;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.OffButton;
import eu.power_switch.obj.button.OnButton;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.device.intertechno.CMR1000;
import eu.power_switch.shared.constants.AlarmClockConstants;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;

/**
 * This Class represents a demo Room/Receiver/Scene setup for use in Play Store images
 * <p/>
 * Created by Markus on 30.07.2015.
 */
public class PlayStoreModeDataModel {

    private static Context context;

    private static ArrayList<Apartment> APARTMENTS = new ArrayList<>();
    private static ArrayList<Gateway> GATEWAYS = new ArrayList<>();

    private static ArrayList<Room> ROOMS_HEIMAT = new ArrayList<>();

    private static ArrayList<Scene> SCENES_HEIMAT = new ArrayList<>();

    // Apartments
    private static Apartment APARTMENT_HEIMAT;
    private static Apartment APARTMENT_ELTERN;
    // Scenes
    private static Scene SCENE_KINOABEND = new Scene((long) 0, (long) 0, "Kinoabend");
    private static Scene SCENE_ABENDESSEN = new Scene((long) 1, (long) 0, "Abendessen");
    private static Scene SCENE_FEIER = new Scene((long) 2, (long) 0, "Feier");
    // Rooms
    private static Room ROOM_WOHNZIMMER = new Room((long) 0, (long) 0, "Wohnzimmer", 0, false);
    private static Room ROOM_SCHLAFZIMMER = new Room((long) 1, (long) 0, "Schlafzimmer", 0, false);
    private static Room ROOM_KUECHE = new Room((long) 2, (long) 0, "Küche", 0, false);
    private static Room ROOM_KINDERZIMMER = new Room((long) 3, (long) 0, "Kinderzimmer", 0, false);
    private static Room ROOM_GARTEN = new Room((long) 4, (long) 0, "Garten", 0, false);
    // Receiver
    private static Receiver RECEIVER_SOFA_WOHNZIMMER;
    private static Receiver RECEIVER_ECKLAMPE_WOHNZIMMER;
    private static Receiver RECEIVER_VERSTAERKER_WOHNZIMMER;
    private static Receiver RECEIVER_DECKE_SCHLAFZIMMER;
    private static Receiver RECEIVER_FENSTER_SCHLAFZIMMER;
    private static Receiver RECEIVER_NACHTTISCHE_SCHLAFZIMMER;
    private static Receiver RECEIVER_ABZUGSHAUBE_KUECHE;
    private static Receiver RECEIVER_ESSTISCH_KUECHE;
    private static Receiver RECEIVER_ARBEITSFLAECHE_KUECHE;
    private static Receiver RECEIVER_KAFFEEMASCHINE_KUECHE;
    private static Receiver RECEIVER_DECKE_KINDERZIMMER;
    private static Receiver RECEIVER_NACHTLICHT_KINDERZIMMER;
    private static Receiver RECEIVER_TERRASSE_GARTEN;
    private static Receiver RECEIVER_WEGBELEUCHTUNG_GARTEN;
    private static Receiver RECEIVER_HINTERHAUS_GARTEN;
    private static Receiver RECEIVER_WEIHNACHTSDEKO_GARTEN;

    /**
     * Default constructor
     */
    public PlayStoreModeDataModel(Context context) {
        PlayStoreModeDataModel.context = context;

        initApartments();
        initReceivers(context);
        initRooms();
        initScenes();
        initGateways();
    }

    private static void initApartments() {
        APARTMENTS.clear();
        APARTMENT_HEIMAT = new Apartment((long) 0, true, "Heimat", ROOMS_HEIMAT, SCENES_HEIMAT, GATEWAYS,
                new Geofence((long) 0, true, "Heimat", new LatLng(52.437418, 13.373122), 100,
                        null, new HashMap<Geofence.EventType, List<Action>>(), Geofence.STATE_NONE));
        APARTMENT_ELTERN = new Apartment((long) 0, false, "Eltern", ROOMS_HEIMAT, SCENES_HEIMAT, GATEWAYS,
                new Geofence((long) 0, true, "Eltern", new LatLng(52.437418, 13.573122), 500,
                        null, new HashMap<Geofence.EventType, List<Action>>(), Geofence.STATE_NONE));
        APARTMENTS.add(APARTMENT_HEIMAT);
        APARTMENTS.add(APARTMENT_ELTERN);
    }

    private static void initGateways() {
        GATEWAYS.clear();


        GATEWAYS.add(new ConnAir((long) 0, true, "AutoDiscovered", "1.0", "192.168.2.125", 49880, "example.myfritz.dyndns.org", 49880, new HashSet<String>(Arrays.asList("FritzBox 7272"))));
        GATEWAYS.add(new ITGW433((long) 1, true, "AutoDiscovered", "1.0", "192.168.2.148", 49880, "example.myfritz.dyndns.org", 49881, Collections.<String>emptySet()));
        GATEWAYS.add(new BrematicGWY433((long) 2, true, "AutoDiscovered", "1.0", "192.168.2.189", 49880, "example.myfritz.dyndns.org", 49882, Collections.<String>emptySet()));
    }

    private static void initReceivers(Context context) {
        RECEIVER_SOFA_WOHNZIMMER = new CMR1000(context, (long) 0, "Sofa", 'E', 1, ROOM_WOHNZIMMER.getId());
        RECEIVER_ECKLAMPE_WOHNZIMMER = new CMR1000(context, (long) 1, "Ecklampe", 'E', 1, ROOM_WOHNZIMMER.getId());
        RECEIVER_VERSTAERKER_WOHNZIMMER = new CMR1000(context, (long) 2, "Verstärker", 'E', 1, ROOM_WOHNZIMMER.getId());
        RECEIVER_DECKE_SCHLAFZIMMER = new CMR1000(context, (long) 3, "Decke", 'E', 1, ROOM_SCHLAFZIMMER.getId());
        RECEIVER_FENSTER_SCHLAFZIMMER = new CMR1000(context, (long) 4, "Fenster", 'E', 1, ROOM_SCHLAFZIMMER.getId());
        RECEIVER_NACHTTISCHE_SCHLAFZIMMER = new CMR1000(context, (long) 5, "Nachttische", 'E', 1, ROOM_SCHLAFZIMMER.getId());
        RECEIVER_ABZUGSHAUBE_KUECHE = new CMR1000(context, (long) 6, "Abzugshaube", 'E', 1, ROOM_KUECHE.getId());
        RECEIVER_ESSTISCH_KUECHE = new CMR1000(context, (long) 7, "Esstisch", 'E', 1, ROOM_KUECHE.getId());
        RECEIVER_ARBEITSFLAECHE_KUECHE = new CMR1000(context, (long) 8, "Arbeitsfläche", 'E', 1, ROOM_KUECHE.getId());
        RECEIVER_KAFFEEMASCHINE_KUECHE = new CMR1000(context, (long) 9, "Kaffeemaschine", 'E', 1, ROOM_KUECHE.getId());
        RECEIVER_DECKE_KINDERZIMMER = new CMR1000(context, (long) 10, "Decke", 'E', 1, ROOM_KINDERZIMMER.getId());
        RECEIVER_NACHTLICHT_KINDERZIMMER = new CMR1000(context, (long) 11, "Nachtlicht", 'E', 1, ROOM_KINDERZIMMER.getId());
        RECEIVER_TERRASSE_GARTEN = new CMR1000(context, (long) 12, "Terrasse", 'E', 1, ROOM_GARTEN.getId());
        RECEIVER_WEGBELEUCHTUNG_GARTEN = new CMR1000(context, (long) 13, "Wegbeleuchtung", 'E', 1, ROOM_GARTEN.getId());
        RECEIVER_HINTERHAUS_GARTEN = new CMR1000(context, (long) 14, "Hinterhaus", 'E', 1, ROOM_GARTEN.getId());
        RECEIVER_WEIHNACHTSDEKO_GARTEN = new CMR1000(context, (long) 15, "Weihnachtsdeko", 'E', 1, ROOM_GARTEN.getId());
    }

    private static void initScenes() {
        SCENE_KINOABEND.getSceneItems().clear();
        SCENE_KINOABEND.addSceneItem(RECEIVER_SOFA_WOHNZIMMER, RECEIVER_SOFA_WOHNZIMMER.getButton(OnButton.ID));
        SCENE_KINOABEND.addSceneItem(RECEIVER_ECKLAMPE_WOHNZIMMER, RECEIVER_ECKLAMPE_WOHNZIMMER.getButton(OffButton.ID));
        SCENE_KINOABEND.addSceneItem(RECEIVER_VERSTAERKER_WOHNZIMMER, RECEIVER_VERSTAERKER_WOHNZIMMER.getButton(OnButton.ID));

        SCENE_ABENDESSEN.getSceneItems().clear();
        SCENE_ABENDESSEN.addSceneItem(RECEIVER_ESSTISCH_KUECHE, RECEIVER_ESSTISCH_KUECHE.getButton(OnButton.ID));
        SCENE_ABENDESSEN.addSceneItem(RECEIVER_ABZUGSHAUBE_KUECHE, RECEIVER_ABZUGSHAUBE_KUECHE.getButton(OffButton.ID));
        SCENE_ABENDESSEN.addSceneItem(RECEIVER_ARBEITSFLAECHE_KUECHE, RECEIVER_ARBEITSFLAECHE_KUECHE.getButton(OffButton.ID));

        SCENE_FEIER.getSceneItems().clear();
        SCENE_FEIER.addSceneItem(RECEIVER_WEGBELEUCHTUNG_GARTEN, RECEIVER_WEGBELEUCHTUNG_GARTEN.getButton(OnButton.ID));
        SCENE_FEIER.addSceneItem(RECEIVER_ESSTISCH_KUECHE, RECEIVER_ESSTISCH_KUECHE.getButton(OnButton.ID));
        SCENE_FEIER.addSceneItem(RECEIVER_SOFA_WOHNZIMMER, RECEIVER_SOFA_WOHNZIMMER.getButton(OnButton.ID));
        SCENE_FEIER.addSceneItem(RECEIVER_TERRASSE_GARTEN, RECEIVER_TERRASSE_GARTEN.getButton(OnButton.ID));

        SCENES_HEIMAT.clear();
        SCENES_HEIMAT.add(SCENE_KINOABEND);
        SCENES_HEIMAT.add(SCENE_ABENDESSEN);
        SCENES_HEIMAT.add(SCENE_FEIER);
    }

    private static void initRooms() {
        ROOM_WOHNZIMMER.getReceivers().clear();
        ROOM_WOHNZIMMER.addReceiver(RECEIVER_SOFA_WOHNZIMMER);
        ROOM_WOHNZIMMER.addReceiver(RECEIVER_ECKLAMPE_WOHNZIMMER);
        ROOM_WOHNZIMMER.addReceiver(RECEIVER_VERSTAERKER_WOHNZIMMER);

        ROOM_SCHLAFZIMMER.getReceivers().clear();
        ROOM_SCHLAFZIMMER.addReceiver(RECEIVER_DECKE_SCHLAFZIMMER);
        ROOM_SCHLAFZIMMER.addReceiver(RECEIVER_FENSTER_SCHLAFZIMMER);
        ROOM_SCHLAFZIMMER.addReceiver(RECEIVER_NACHTTISCHE_SCHLAFZIMMER);

        ROOM_KUECHE.getReceivers().clear();
        ROOM_KUECHE.addReceiver(RECEIVER_ABZUGSHAUBE_KUECHE);
        ROOM_KUECHE.addReceiver(RECEIVER_ESSTISCH_KUECHE);
        ROOM_KUECHE.addReceiver(RECEIVER_ARBEITSFLAECHE_KUECHE);
        ROOM_KUECHE.addReceiver(RECEIVER_KAFFEEMASCHINE_KUECHE);

        ROOM_KINDERZIMMER.getReceivers().clear();
        ROOM_KINDERZIMMER.addReceiver(RECEIVER_DECKE_KINDERZIMMER);
        ROOM_KINDERZIMMER.addReceiver(RECEIVER_NACHTLICHT_KINDERZIMMER);

        ROOM_GARTEN.getReceivers().clear();
        ROOM_GARTEN.addReceiver(RECEIVER_TERRASSE_GARTEN);
        ROOM_GARTEN.addReceiver(RECEIVER_WEGBELEUCHTUNG_GARTEN);
        ROOM_GARTEN.addReceiver(RECEIVER_HINTERHAUS_GARTEN);
        ROOM_GARTEN.addReceiver(RECEIVER_WEIHNACHTSDEKO_GARTEN);

        ROOMS_HEIMAT.clear();
        ROOMS_HEIMAT.add(ROOM_WOHNZIMMER);
        ROOMS_HEIMAT.add(ROOM_SCHLAFZIMMER);
        ROOMS_HEIMAT.add(ROOM_KUECHE);
        ROOMS_HEIMAT.add(ROOM_KINDERZIMMER);
        ROOMS_HEIMAT.add(ROOM_GARTEN);
    }

    public ArrayList<Apartment> getApartments() {
        return APARTMENTS;
    }

    public Apartment getActiveApartment() {
        return APARTMENTS.get(0);
    }

    public List<Gateway> getGateways() {
        return GATEWAYS;
    }

    /**
     * Get all Timers
     *
     * @return List of Timers
     */
    public ArrayList<Timer> getTimers() {
        ArrayList<Timer> timers = new ArrayList<>();

        ReceiverAction timerReceiverAction = new ReceiverAction(0, APARTMENT_HEIMAT.getName(),
                ROOM_WOHNZIMMER, RECEIVER_ECKLAMPE_WOHNZIMMER,
                RECEIVER_ECKLAMPE_WOHNZIMMER.getButton(OnButton.ID));
        ArrayList<WeekdayTimer.Day> days = new ArrayList<>();
        days.add(WeekdayTimer.Day.MONDAY);
        days.add(WeekdayTimer.Day.TUESDAY);
        days.add(WeekdayTimer.Day.WEDNESDAY);
        days.add(WeekdayTimer.Day.THURSDAY);
        days.add(WeekdayTimer.Day.FRIDAY);
        days.add(WeekdayTimer.Day.SATURDAY);
        days.add(WeekdayTimer.Day.SUNDAY);

        ArrayList<Action> actions = new ArrayList<>();
        actions.add(timerReceiverAction);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 0);

        timers.add(new WeekdayTimer(0, true, "Abendlicht", c, days, actions));


        ReceiverAction timerReceiverAction2 = new ReceiverAction(0, APARTMENT_HEIMAT.getName(),
                ROOM_KUECHE, RECEIVER_KAFFEEMASCHINE_KUECHE,
                RECEIVER_ECKLAMPE_WOHNZIMMER.getButton(OnButton.ID));

        ArrayList<Action> actions2 = new ArrayList<>();
        actions2.add(timerReceiverAction2);

        ArrayList<WeekdayTimer.Day> days2 = new ArrayList<>();
        days2.add(WeekdayTimer.Day.MONDAY);
        days2.add(WeekdayTimer.Day.TUESDAY);
        days2.add(WeekdayTimer.Day.WEDNESDAY);
        days2.add(WeekdayTimer.Day.THURSDAY);
        days2.add(WeekdayTimer.Day.FRIDAY);

        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.HOUR_OF_DAY, 6);
        c2.set(Calendar.MINUTE, 30);

        timers.add(new WeekdayTimer(1, true, "Morgenkaffee", c2, days2, actions2));

        return timers;
    }

    /**
     * Get a list of Stock Alarm Actions
     *
     * @param eventType Alarm Event Type
     * @return List of Actions for specified Event Type
     */
    public List<Action> getAlarmActions(AlarmClockConstants.Event eventType) {
        List<Action> actions = new ArrayList<>();

        switch (eventType) {
            case ALARM_TRIGGERED:
                actions.add(new ReceiverAction(-1, APARTMENT_HEIMAT.getName(), ROOM_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OnButton.ID)));
                break;
            case ALARM_SNOOZED:
                actions.add(new ReceiverAction(-1, APARTMENT_HEIMAT.getName(), ROOM_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OffButton.ID)));
                break;
            case ALARM_DISMISSED:
                actions.add(new ReceiverAction(-1, APARTMENT_HEIMAT.getName(), ROOM_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OnButton.ID)));
                actions.add(new ReceiverAction(-1, APARTMENT_HEIMAT.getName(), ROOM_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER, RECEIVER_FENSTER_SCHLAFZIMMER.getButton(OnButton.ID)));
                break;
        }

        return actions;
    }

    /**
     * Get a list of Stock Alarm Actions
     *
     * @param eventType Alarm Event Type
     * @return List of Actions for specified Event Type
     */
    public List<Action> getAlarmActions(SleepAsAndroidConstants.Event eventType) {
        List<Action> actions = new ArrayList<>();

        switch (eventType) {
            case ALARM_TRIGGERED:
                actions.add(new ReceiverAction(-1, APARTMENT_HEIMAT.getName(), ROOM_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OnButton.ID)));
                break;
            case ALARM_SNOOZED:
                actions.add(new ReceiverAction(-1, APARTMENT_HEIMAT.getName(), ROOM_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OffButton.ID)));
                break;
            case ALARM_DISMISSED:
                actions.add(new ReceiverAction(-1, APARTMENT_HEIMAT.getName(), ROOM_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER.getButton(OnButton.ID)));
                actions.add(new ReceiverAction(-1, APARTMENT_HEIMAT.getName(), ROOM_SCHLAFZIMMER, RECEIVER_NACHTTISCHE_SCHLAFZIMMER, RECEIVER_FENSTER_SCHLAFZIMMER.getButton(OnButton.ID)));
                break;
        }

        return actions;
    }
}

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import eu.power_switch.action.Action;
import eu.power_switch.action.ReceiverAction;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.device.intertechno.CMR1000;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;

/**
 * This Class represents a demo Room/Receiver/Scene setup for use in Play Store images
 * <p/>
 * Created by Markus on 30.07.2015.
 */
public class PlayStoreModeDataModel {

    private static Context context;

    private static ArrayList<Apartment> apartments = new ArrayList<>();
    private static ArrayList<Room> rooms = new ArrayList<>();
    private static ArrayList<Receiver> receivers = new ArrayList<>();
    private static ArrayList<Scene> scenes = new ArrayList<>();

    private static ArrayList<Gateway> gateways = new ArrayList<>();

    // apartments
    private static Apartment heimat;
    // Scenes
    private static Scene scene_kinoabend = new Scene((long) 0, (long) 0, "Kinoabend");
    private static Scene scene_abendessen = new Scene((long) 1, (long) 0, "Abendessen");
    private static Scene scene_feier = new Scene((long) 2, (long) 0, "Feier");
    // Rooms
    private static Room wohnzimmer = new Room((long) 0, (long) 0, "Wohnzimmer", 0, false);
    private static Room schlafzimmer = new Room((long) 1, (long) 0, "Schlafzimmer", 0, false);
    private static Room kueche = new Room((long) 2, (long) 0, "Küche", 0, false);
    private static Room kinderzimmer = new Room((long) 3, (long) 0, "Kinderzimmer", 0, false);
    private static Room garten = new Room((long) 4, (long) 0, "Garten", 0, false);
    // Receiver
    private static Receiver sofa_wohnzimmer;
    private static Receiver ecklampe_wohnzimmer;
    private static Receiver verstaerker_wohnzimmer;
    private static Receiver decke_schlafzimmer;
    private static Receiver fenster_schlafzimmer;
    private static Receiver nachttische_schlafzimmer;
    private static Receiver abzugshaube_kueche;
    private static Receiver esstisch_kueche;
    private static Receiver arbeitsflaeche_kueche;
    private static Receiver kaffeemaschine_kueche;
    private static Receiver decke_kinderzimmer;
    private static Receiver nachtlicht_kinderzimmer;
    private static Receiver terrasse_garten;
    private static Receiver wegbeleuchtung_garten;
    private static Receiver hinterhaus_garten;
    private static Receiver weihnachtsdeko_garten;

    /**
     * Default constructor
     */
    public PlayStoreModeDataModel(Context context) {
        PlayStoreModeDataModel.context = context;

        initReceivers(context);
        initRooms();
        initScenes();
        initGateways();

        heimat = new Apartment((long) 0, true, "Heimat", rooms, scenes, getGateways(),
                new Geofence((long) 0, true, "Heimat", new LatLng(52.437418, 13.373122), 100,
                        null, new HashMap<Geofence.EventType, List<Action>>()));

        apartments.clear();
        apartments.add(heimat);
    }

    private static void initGateways() {
        gateways.clear();
        gateways.add(new ConnAir((long) 0, true, "AutoDiscovered", "1.0", "192.168.2.125", 49880));
        gateways.add(new ITGW433((long) 1, true, "AutoDiscovered", "1.0", "192.168.2.148", 49880));
        gateways.add(new BrematicGWY433((long) 2, true, "AutoDiscovered", "1.0", "192.168.2.189", 49880));
    }

    /**
     * Get all Gateways
     *
     * @return List of Gateways
     */
    public static ArrayList<Gateway> getGateways() {
        return gateways;
    }

    private static void initReceivers(Context context) {
        sofa_wohnzimmer = new CMR1000(context, (long) 0, "Sofa", 'E', 1, wohnzimmer.getId());
        ecklampe_wohnzimmer = new CMR1000(context, (long) 1, "Ecklampe", 'E', 1, wohnzimmer.getId());
        verstaerker_wohnzimmer = new CMR1000(context, (long) 2, "Verstärker", 'E', 1, wohnzimmer.getId());
        decke_schlafzimmer = new CMR1000(context, (long) 3, "Decke", 'E', 1, schlafzimmer.getId());
        fenster_schlafzimmer = new CMR1000(context, (long) 4, "Fenster", 'E', 1, schlafzimmer.getId());
        nachttische_schlafzimmer = new CMR1000(context, (long) 5, "Nachttische", 'E', 1, schlafzimmer.getId());
        abzugshaube_kueche = new CMR1000(context, (long) 6, "Abzugshaube", 'E', 1, kueche.getId());
        esstisch_kueche = new CMR1000(context, (long) 7, "Esstisch", 'E', 1, kueche.getId());
        arbeitsflaeche_kueche = new CMR1000(context, (long) 8, "Arbeitsfläche", 'E', 1, kueche.getId());
        kaffeemaschine_kueche = new CMR1000(context, (long) 9, "Kaffeemaschine", 'E', 1, kueche.getId());
        decke_kinderzimmer = new CMR1000(context, (long) 10, "Decke", 'E', 1, kinderzimmer.getId());
        nachtlicht_kinderzimmer = new CMR1000(context, (long) 11, "Nachtlicht", 'E', 1, kinderzimmer.getId());
        terrasse_garten = new CMR1000(context, (long) 12, "Terrasse", 'E', 1, garten.getId());
        wegbeleuchtung_garten = new CMR1000(context, (long) 13, "Wegbeleuchtung", 'E', 1, garten.getId());
        hinterhaus_garten = new CMR1000(context, (long) 14, "Hinterhaus", 'E', 1, garten.getId());
        weihnachtsdeko_garten = new CMR1000(context, (long) 15, "Weihnachtsdeko", 'E', 1, garten.getId());
    }

    private static void initScenes() {
        scene_kinoabend.getSceneItems().clear();
        scene_kinoabend.addSceneItem(sofa_wohnzimmer, sofa_wohnzimmer.getButtons().getFirst());
        scene_kinoabend.addSceneItem(ecklampe_wohnzimmer, ecklampe_wohnzimmer.getButtons().getLast());
        scene_kinoabend.addSceneItem(verstaerker_wohnzimmer, verstaerker_wohnzimmer.getButtons().getFirst());

        scene_abendessen.getSceneItems().clear();
        scene_abendessen.addSceneItem(esstisch_kueche, esstisch_kueche.getButtons().getFirst());
        scene_abendessen.addSceneItem(abzugshaube_kueche, abzugshaube_kueche.getButtons().getLast());
        scene_abendessen.addSceneItem(arbeitsflaeche_kueche, arbeitsflaeche_kueche.getButtons().getLast());

        scene_feier.getSceneItems().clear();
        scene_feier.addSceneItem(wegbeleuchtung_garten, wegbeleuchtung_garten.getButtons().getFirst());
        scene_feier.addSceneItem(esstisch_kueche, esstisch_kueche.getButtons().getFirst());
        scene_feier.addSceneItem(sofa_wohnzimmer, sofa_wohnzimmer.getButtons().getFirst());
        scene_feier.addSceneItem(terrasse_garten, terrasse_garten.getButtons().getFirst());

        scenes.clear();
        scenes.add(scene_kinoabend);
        scenes.add(scene_abendessen);
        scenes.add(scene_feier);
    }

    private static void initRooms() {
        wohnzimmer.getReceivers().clear();
        wohnzimmer.addReceiver(sofa_wohnzimmer);
        wohnzimmer.addReceiver(ecklampe_wohnzimmer);
        wohnzimmer.addReceiver(verstaerker_wohnzimmer);

        schlafzimmer.getReceivers().clear();
        schlafzimmer.addReceiver(decke_schlafzimmer);
        schlafzimmer.addReceiver(fenster_schlafzimmer);
        schlafzimmer.addReceiver(nachttische_schlafzimmer);

        kueche.getReceivers().clear();
        kueche.addReceiver(abzugshaube_kueche);
        kueche.addReceiver(esstisch_kueche);
        kueche.addReceiver(arbeitsflaeche_kueche);
        kueche.addReceiver(kaffeemaschine_kueche);

        kinderzimmer.getReceivers().clear();
        kinderzimmer.addReceiver(decke_kinderzimmer);
        kinderzimmer.addReceiver(nachtlicht_kinderzimmer);

        garten.getReceivers().clear();
        garten.addReceiver(terrasse_garten);
        garten.addReceiver(wegbeleuchtung_garten);
        garten.addReceiver(hinterhaus_garten);
        garten.addReceiver(weihnachtsdeko_garten);

        rooms.clear();
        rooms.add(wohnzimmer);
        rooms.add(schlafzimmer);
        rooms.add(kueche);
        rooms.add(kinderzimmer);
        rooms.add(garten);
    }

    public static ArrayList<Apartment> getApartments() {
        return apartments;
    }

    public static Apartment getActiveApartment() {
        return apartments.get(0);
    }

    /**
     * Get all Timers
     *
     * @return List of Timers
     */
    public ArrayList<Timer> getTimers() {
        ArrayList<Timer> timers = new ArrayList<>();

        ReceiverAction timerReceiverAction = new ReceiverAction(0, heimat.getName(), wohnzimmer, ecklampe_wohnzimmer,
                ecklampe_wohnzimmer.getButtons().getFirst());
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


        ReceiverAction timerReceiverAction2 = new ReceiverAction(0, heimat.getName(), kueche, kaffeemaschine_kueche,
                ecklampe_wohnzimmer.getButtons().getFirst());

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
}

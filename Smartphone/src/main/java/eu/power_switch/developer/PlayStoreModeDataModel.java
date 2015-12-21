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

import java.util.ArrayList;
import java.util.Calendar;

import eu.power_switch.action.Action;
import eu.power_switch.action.ReceiverAction;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.device.intertechno.CMR1000;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;

/**
 * This Class represents a demo Room/Recever/Scene setup for use in Play Store images
 * <p/>
 * Created by Markus on 30.07.2015.
 */
public class PlayStoreModeDataModel {

    private Context context;

    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Scene> scenes = new ArrayList<>();

    // Scenes
    private Scene scene_kinoabend = new Scene((long) 0, "Kinoabend");
    private Scene scene_abendessen = new Scene((long) 1, "Abendessen");
    private Scene scene_feier = new Scene((long) 2, "Feier");
    // Rooms
    private Room wohnzimmer = new Room((long) 0, "Wohnzimmer");
    private Room schlafzimmer = new Room((long) 1, "Schlafzimmer");
    private Room kueche = new Room((long) 2, "Küche");
    private Room kinderzimmer = new Room((long) 3, "Kinderzimmer");
    private Room garten = new Room((long) 4, "Garten");
    // Receiver
    private Receiver sofa_wohnzimmer;
    private Receiver ecklampe_wohnzimmer;
    private Receiver verstaerker_wohnzimmer;
    private Receiver decke_schlafzimmer;
    private Receiver fenster_schlafzimmer;
    private Receiver nachttische_schlafzimmer;
    private Receiver abzugshaube_kueche;
    private Receiver esstisch_kueche;
    private Receiver arbeitsflaeche_kueche;
    private Receiver kaffeemaschine_kueche;
    private Receiver decke_kinderzimmer;
    private Receiver nachtlicht_kinderzimmer;
    private Receiver terrasse_garten;
    private Receiver wegbeleuchtung_garten;
    private Receiver hinterhaus_garten;
    private Receiver weihnachtsdeko_garten;

    /**
     * Default constructor
     */
    public PlayStoreModeDataModel(Context context) {
        this.context = context;
        initReceiver();
        initRooms();
        initScenes();
    }

    /**
     * Get all Gateways
     *
     * @return List of Gateways
     */
    public static ArrayList<Gateway> getGateways() {
        ArrayList<Gateway> gateways = new ArrayList<>();

        gateways.add(new ConnAir((long) 0, true, "AutoDiscovered", "1.0", "192.168.2.125", 49880));
        gateways.add(new ITGW433((long) 1, true, "AutoDiscovered", "1.0", "192.168.2.148", 49880));
        gateways.add(new BrematicGWY433((long) 2, true, "AutoDiscovered", "1.0", "192.168.2.189", 49880));

        return gateways;
    }

    private void initReceiver() {
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

    private void initScenes() {
        scene_kinoabend.addSceneItem(sofa_wohnzimmer, sofa_wohnzimmer.getButtons().getFirst());
        scene_kinoabend.addSceneItem(ecklampe_wohnzimmer, ecklampe_wohnzimmer.getButtons().getLast());
        scene_kinoabend.addSceneItem(verstaerker_wohnzimmer, verstaerker_wohnzimmer.getButtons().getFirst());

        scene_abendessen.addSceneItem(esstisch_kueche, esstisch_kueche.getButtons().getFirst());
        scene_abendessen.addSceneItem(abzugshaube_kueche, abzugshaube_kueche.getButtons().getLast());
        scene_abendessen.addSceneItem(arbeitsflaeche_kueche, arbeitsflaeche_kueche.getButtons().getLast());

        scene_feier.addSceneItem(wegbeleuchtung_garten, wegbeleuchtung_garten.getButtons().getFirst());
        scene_feier.addSceneItem(esstisch_kueche, esstisch_kueche.getButtons().getFirst());
        scene_feier.addSceneItem(sofa_wohnzimmer, sofa_wohnzimmer.getButtons().getFirst());
        scene_feier.addSceneItem(terrasse_garten, terrasse_garten.getButtons().getFirst());

        scenes.add(scene_kinoabend);
        scenes.add(scene_abendessen);
        scenes.add(scene_feier);
    }

    private void initRooms() {
        wohnzimmer.addReceiver(sofa_wohnzimmer);
        wohnzimmer.addReceiver(ecklampe_wohnzimmer);
        wohnzimmer.addReceiver(verstaerker_wohnzimmer);

        schlafzimmer.addReceiver(decke_schlafzimmer);
        schlafzimmer.addReceiver(fenster_schlafzimmer);
        schlafzimmer.addReceiver(nachttische_schlafzimmer);

        kueche.addReceiver(abzugshaube_kueche);
        kueche.addReceiver(esstisch_kueche);
        kueche.addReceiver(arbeitsflaeche_kueche);
        kueche.addReceiver(kaffeemaschine_kueche);

        kinderzimmer.addReceiver(decke_kinderzimmer);
        kinderzimmer.addReceiver(nachtlicht_kinderzimmer);

        garten.addReceiver(terrasse_garten);
        garten.addReceiver(wegbeleuchtung_garten);
        garten.addReceiver(hinterhaus_garten);
        garten.addReceiver(weihnachtsdeko_garten);

        rooms.add(wohnzimmer);
        rooms.add(schlafzimmer);
        rooms.add(kueche);
        rooms.add(kinderzimmer);
        rooms.add(garten);
    }

    /**
     * Get all Rooms
     *
     * @return List of Rooms
     */
    public ArrayList<Room> getRooms() {
        return rooms;
    }

    /**
     * Get all Scenes
     *
     * @return List of Scenes
     */
    public ArrayList<Scene> getScenes() {
        return scenes;
    }

    /**
     * Get all Receivers
     *
     * @return List of Receivers
     */
    public ArrayList<Receiver> getReceivers() {
        ArrayList<Receiver> receivers = new ArrayList<>();
        receivers.add(sofa_wohnzimmer);
        receivers.add(ecklampe_wohnzimmer);
        receivers.add(verstaerker_wohnzimmer);
        receivers.add(decke_schlafzimmer);
        receivers.add(fenster_schlafzimmer);
        receivers.add(nachttische_schlafzimmer);
        receivers.add(abzugshaube_kueche);
        receivers.add(esstisch_kueche);
        receivers.add(arbeitsflaeche_kueche);
        receivers.add(kaffeemaschine_kueche);
        receivers.add(decke_kinderzimmer);
        receivers.add(nachtlicht_kinderzimmer);
        receivers.add(terrasse_garten);
        receivers.add(wegbeleuchtung_garten);
        receivers.add(hinterhaus_garten);
        receivers.add(weihnachtsdeko_garten);

        return receivers;
    }

    /**
     * Get all Timers
     *
     * @return List of Timers
     */
    public ArrayList<Timer> getTimers() {
        ArrayList<Timer> timers = new ArrayList<>();

        ReceiverAction timerReceiverAction = new ReceiverAction(0, wohnzimmer, ecklampe_wohnzimmer,
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


        ReceiverAction timerReceiverAction2 = new ReceiverAction(0, kueche, kaffeemaschine_kueche,
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

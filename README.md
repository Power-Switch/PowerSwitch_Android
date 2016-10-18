# PowerSwitch
PowerSwitch - Funksteckdosen auf dem Smartphone

https://power-switch.eu

# Wieso? Weshalb? Warum?

Schon seit langem gibt es in jedem Baumarkt preiswerte Funksteckdosen, die sich über eine Fernbedienung ein- und ausschalten lassen. Dazu wird eine Fernbedienung mitgeliefert, mit der sich meist ca. 3-4 Steckdosen schalten lassen. Die sieht nicht nur doof aus, sondern man vergisst auch schnell mal wo sie ist und wenn man mehr Steckdosen steuern will, braucht man mehrere Fernbedienungen.

Um das zu vermeiden gibt es "Gateways" (z.B.: Brennenstuhl Brematic Home Automation Gateway oder das Simple Solutions ConnAir [leider nicht mehr verfügbar]), die per Netzwerk (bspw. LAN) Signale empfangen und diese in 433MHz-Funksignale umwandeln. Damit lassen sich per Netzwerk auch Funksteckdosen steuern.

Und wie schickt man die Signale zum Gateway? Eine Möglichkeit ist natürlich das Senden mit dem PC, aber das bedeutet, dass man zum Schalten der Steckdosen auch immer einen PC braucht. Um das ganze praktikabler zu machen, entwickeln wir die PowerSwitch Android App.


Um die Entwicklung der App voran zu treiben und interessierten einen Blick hinter die Kulissen zu gewähren ist der Sourcecode der Android App hier verfügbar.

# Contributing

Github is for social coding: if you want to write code, I encourage contributions through pull requests from forks of this repository. Create Github tickets for bugs and new features and comment on the ones that you are interested in.

# Dependencies
* uk.co.deanwild.materialshowcaseview
* it.sephiroth.android.library.targettooltip:target-tooltip-library
* com.mikepenz:materialdrawer
* com.mikepenz:aboutlibraries
* com.mikepenz:iconics-core
    * com.mikepenz:google-material-typeface
    * com.mikepenz:material-design-iconic-typeface
* various com.android.support.* packages (see /smartphone/build.gradle for more info)
* Google Play Services
* net.lingala.zip4j:zip4j
* com.daimajia.numberprogressbar:library
* me.grantland:autofittextview
* com.crashlytics.sdk.android:crashlytics
* com.crashlytics.sdk.android:answers


# Lizenz
    PowerSwitch by Max Rosin & Markus Ressel
    Copyright (C) 2015  Markus Ressel

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

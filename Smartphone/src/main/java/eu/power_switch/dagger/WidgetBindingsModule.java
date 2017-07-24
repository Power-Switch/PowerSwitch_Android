/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.widget.WidgetIntentReceiver;
import eu.power_switch.widget.activity.ConfigureReceiverWidgetActivity;
import eu.power_switch.widget.activity.ConfigureRoomWidgetActivity;
import eu.power_switch.widget.activity.ConfigureSceneWidgetActivity;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import eu.power_switch.widget.provider.RoomWidgetProvider;
import eu.power_switch.widget.provider.SceneWidgetProvider;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class WidgetBindingsModule {

    @ContributesAndroidInjector
    abstract ConfigureReceiverWidgetActivity configureReceiverWidgetActivity();

    @ContributesAndroidInjector
    abstract ConfigureRoomWidgetActivity configureRoomWidgetActivity();

    @ContributesAndroidInjector
    abstract ConfigureSceneWidgetActivity configureSceneWidgetActivity();

    @ContributesAndroidInjector
    abstract WidgetIntentReceiver widgetIntentReceiver();

    @ContributesAndroidInjector
    abstract ReceiverWidgetProvider receiverWidgetProvider();

    @ContributesAndroidInjector
    abstract RoomWidgetProvider roomWidgetProvider();

    @ContributesAndroidInjector
    abstract SceneWidgetProvider sceneWidgetProvider();

}

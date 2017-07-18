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
import eu.power_switch.wizard.gui.AdvancedFeaturesPage;
import eu.power_switch.wizard.gui.ApartmentsPage;
import eu.power_switch.wizard.gui.BasicPage;
import eu.power_switch.wizard.gui.FinishPage;
import eu.power_switch.wizard.gui.RoomsScenesPage;
import eu.power_switch.wizard.gui.SetupApartmentPage;
import eu.power_switch.wizard.gui.SetupGatewayPage;
import eu.power_switch.wizard.gui.SetupRoomPage;
import eu.power_switch.wizard.gui.SingleLineTextInputPage;
import eu.power_switch.wizard.gui.TimerAlarmClockPage;
import eu.power_switch.wizard.gui.WelcomePage;
import eu.power_switch.wizard.gui.WizardActivity;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class WizardBindingsModule {

    @ContributesAndroidInjector
    abstract WizardActivity wizardActivity();

    @ContributesAndroidInjector
    abstract BasicPage basicPage();

    @ContributesAndroidInjector
    abstract SingleLineTextInputPage singleLineTextInputPage();

    @ContributesAndroidInjector
    abstract WelcomePage welcomePage();

    @ContributesAndroidInjector
    abstract ApartmentsPage apartmentsPage();

    @ContributesAndroidInjector
    abstract RoomsScenesPage roomsScenesPage();

    @ContributesAndroidInjector
    abstract TimerAlarmClockPage timerAlarmClockPage();

    @ContributesAndroidInjector
    abstract AdvancedFeaturesPage advancedFeaturesPage();

    @ContributesAndroidInjector
    abstract SetupApartmentPage setupApartmentPage();

    @ContributesAndroidInjector
    abstract SetupRoomPage setupRoomPage();

    @ContributesAndroidInjector
    abstract SetupGatewayPage setupGatewayPage();

    @ContributesAndroidInjector
    abstract FinishPage finishPage();

}

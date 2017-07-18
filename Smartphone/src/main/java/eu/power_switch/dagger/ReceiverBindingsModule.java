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
import eu.power_switch.gui.dialog.configuration.ConfigureReceiverDialog;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage1Name;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage2Type;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage3Setup;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage4Gateway;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage5Summary;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class ReceiverBindingsModule {

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialog configureReceiverDialog();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage1Name configureReceiverDialogPage1Name();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage2Type configureReceiverDialogPage2Type();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage3Setup configureReceiverDialogPage3Setup();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage4Gateway configureReceiverDialogPage4Gateway();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage5Summary configureReceiverDialogPage5TabbedSummary();

}

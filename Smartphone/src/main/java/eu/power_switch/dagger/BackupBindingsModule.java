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
import eu.power_switch.gui.dialog.CreateBackupDialog;
import eu.power_switch.gui.dialog.CreateBackupProcessingDialog;
import eu.power_switch.gui.dialog.EditBackupDialog;
import eu.power_switch.gui.dialog.RestoreBackupFromFileActivity;
import eu.power_switch.gui.dialog.RestoreBackupProcessingDialog;
import eu.power_switch.gui.fragment.BackupFragment;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class BackupBindingsModule {

    @ContributesAndroidInjector
    abstract BackupFragment backupFragment();

    @ContributesAndroidInjector
    abstract CreateBackupDialog createBackupDialog();

    @ContributesAndroidInjector
    abstract EditBackupDialog editBackupDialog();

    @ContributesAndroidInjector
    abstract CreateBackupProcessingDialog createBackupProcessingDialog();

    @ContributesAndroidInjector
    abstract RestoreBackupProcessingDialog restoreBackupProcessingDialog();

    @ContributesAndroidInjector
    abstract RestoreBackupFromFileActivity restoreBackupFromFileActivity();

}

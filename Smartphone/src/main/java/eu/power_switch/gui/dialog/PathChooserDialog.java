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

package eu.power_switch.gui.dialog;

import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.event.BackupPathChangedEvent;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialog;
import eu.power_switch.gui.dialog.configuration.PageEntry;
import eu.power_switch.gui.dialog.configuration.holder.BackupPathConfigurationHolder;
import eu.power_switch.gui.fragment.settings.PathChooserDialogPage;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import timber.log.Timber;

/**
 * Dialog used to select a Path on SDCard
 * <p/>
 * Created by Markus on 22.11.2015.
 */
public class PathChooserDialog extends ConfigurationDialog<BackupPathConfigurationHolder> {

    public static PathChooserDialog newInstance() {
        Bundle args = new Bundle();

        PathChooserDialog fragment = new PathChooserDialog();
        fragment.setConfiguration(new BackupPathConfigurationHolder());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Used to notify Backup Fragment (this) that Backups have changed
     */
    public static void notifyBackupPathChanged() {
        Timber.d("notifyBackupPathChanged");
        EventBus.getDefault()
                .post(new BackupPathChangedEvent());
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) throws Exception {
        String currentBackupPath = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.BACKUP_PATH);
        getConfiguration().setBackupPath(currentBackupPath);
    }

    @Override
    protected int getDialogTitle() {
        return R.string.path;
    }

    @Override
    protected void saveConfiguration() throws Exception {
        smartphonePreferencesHandler.setValue(SmartphonePreferencesHandler.BACKUP_PATH, getConfiguration().getBackupPath());

        notifyBackupPathChanged();
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        // nothing to do here
    }

    @Override
    protected void addPageEntries(List<PageEntry<BackupPathConfigurationHolder>> list) {
        list.add(new PageEntry<>(R.string.title_backupPath, PathChooserDialogPage.class));
    }

}

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

package eu.power_switch.gui.dialog;

import android.os.AsyncTask;
import android.os.Bundle;

import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.backup.OnZipProgressChangedListener;
import eu.power_switch.backup.ZipHelper;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * Created by Markus on 03.09.2016.
 */
public class UpgradeBackupsDialog extends ProcessingDialog {

    private AsyncTask<Void, Integer, AsyncTaskResult<Void>> processingTask;

    public static UpgradeBackupsDialog newInstance() {
        Bundle args = new Bundle();

        UpgradeBackupsDialog fragment = new UpgradeBackupsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.backups;
    }

    @Override
    protected void onStartProcessing() throws Exception {
        /*
          Upgrades the old backup format to the new one.
          Old: Folder containing "database" and "shared_preferences" folders
          New: Zip file containing "database" and "shared_preferences" folders
         */
        processingTask = new AsyncTask<Void, Integer, AsyncTaskResult<Void>>() {

            @Override
            protected AsyncTaskResult<Void> doInBackground(Void... voids) {
                try {
                    File backupDir = new File(SmartphonePreferencesHandler.<String>get(SmartphonePreferencesHandler.KEY_BACKUP_PATH));

                    FileFilter backupFileFilter = new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.list() == null) {
                                return false;
                            }

                            List<String> subFolders = Arrays.asList(pathname.list());
                            return pathname.isDirectory() && subFolders.contains("shared_prefs") &&
                                    subFolders.contains("databases");
                        }
                    };

                    if (backupDir.exists()) {
                        File[] listFiles = backupDir.listFiles(backupFileFilter);
                        for (int i = 0; i < listFiles.length; i++) {
                            publishProgress(0, i, listFiles.length);

                            File oldBackup = listFiles[i];
                            File target = new File(SmartphonePreferencesHandler.<String>get(SmartphonePreferencesHandler.KEY_BACKUP_PATH) + File.separator + oldBackup.getName() + BackupHandler.BACKUP_FILE_SUFFIX);
                            if (target.exists()) {
                                target.delete();
                            }

                            ZipHelper.createZip(target.getAbsolutePath(),
                                    BackupHandler.BACKUP_PASSWORD,
                                    new OnZipProgressChangedListener() {
                                        @Override
                                        public void onProgressChanged(ProgressMonitor progressMonitor) {
                                            if (progressMonitor.getState() == ProgressMonitor.RESULT_WORKING) {
                                                publishProgress(1, progressMonitor.getPercentDone());
                                            } else if (progressMonitor.getState() == ProgressMonitor.RESULT_SUCCESS) {
                                                publishProgress(1, 100);
                                            }
                                        }
                                    },
                                    oldBackup.getAbsolutePath());

                            // TODO: optionally delete existing backups in old format
                            // oldBackup.delete();

                            Thread.sleep(500);
                        }
                    }

                    return new AsyncTaskResult<>();
                } catch (Exception e) {
                    return new AsyncTaskResult<>(e);
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                if (values[0] == 0) {
                    setMainProgress(100 * values[1] / values[2]);
                    setMainStatusMessage(String.format("Processing %d of %d...", values[1], values[2]));
                } else {
                    setSubProgress(values[1]);
                    setSubStatusMessage(String.valueOf(values[1]));
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<Void> booleanAsyncTaskResult) {
                if (booleanAsyncTaskResult.isSuccess()) {
                    onFinishedSuccess();
                } else {
                    onFinishedFailure(booleanAsyncTaskResult.getException());
                }
            }
        }.execute();
    }

    @Override
    protected void onCancel() {
        if (processingTask != null) {
            processingTask.cancel(true);
        }
    }

}

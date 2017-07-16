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
import eu.power_switch.gui.fragment.BackupFragment;
import timber.log.Timber;

import static eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler.PreferenceItem.KEY_BACKUP_PATH;

/**
 * Backup Upgrade Dialog
 * <p/>
 * Created by Markus on 03.09.2016.
 */
public class UpgradeBackupsProcessingDialog extends ProcessingDialog {

    public static final String KEY_REMOVE_OLD_FORMAT = "removeOldFormat";

    private AsyncTask<Void, Object, AsyncTaskResult<Void>> processingTask;

    public static UpgradeBackupsProcessingDialog newInstance(boolean removeOldFormat) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_REMOVE_OLD_FORMAT, removeOldFormat);

        UpgradeBackupsProcessingDialog fragment = new UpgradeBackupsProcessingDialog();
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
        processingTask = new AsyncTask<Void, Object, AsyncTaskResult<Void>>() {

            @Override
            protected AsyncTaskResult<Void> doInBackground(Void... voids) {
                try {
                    String backupPath = smartphonePreferencesHandler.get(KEY_BACKUP_PATH);
                    File   backupDir  = new File(backupPath);

                    FileFilter backupFileFilter = new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.list() == null) {
                                return false;
                            }

                            List<String> subFolders = Arrays.asList(pathname.list());
                            return pathname.isDirectory() && subFolders.contains("shared_prefs") && subFolders.contains("databases");
                        }
                    };

                    if (backupDir.exists()) {
                        File[] listFiles = backupDir.listFiles(backupFileFilter);
                        for (int i = 0; i < listFiles.length; i++) {
                            publishProgress(0, i, listFiles.length);

                            File oldBackup = listFiles[i];
                            File target    = new File(smartphonePreferencesHandler.get(KEY_BACKUP_PATH) + File.separator + oldBackup.getName() + BackupHandler.BACKUP_FILE_SUFFIX);
                            if (target.exists()) {
                                target.delete();
                            }

                            ZipHelper.createZip(target.getAbsolutePath(), BackupHandler.BACKUP_PASSWORD, new OnZipProgressChangedListener() {
                                @Override
                                public void onProgressChanged(ProgressMonitor progressMonitor) {
                                    if (progressMonitor.getState() == ProgressMonitor.RESULT_WORKING) {
                                        String fileName = progressMonitor.getFileName();
                                        publishProgress(1,
                                                progressMonitor.getPercentDone(),
                                                fileName.substring(fileName.lastIndexOf(File.separator) + 1));
                                    } else if (progressMonitor.getState() == ProgressMonitor.RESULT_SUCCESS) {
                                        publishProgress(1, 100, getString(R.string.done));
                                    }
                                }
                            }, oldBackup.getAbsolutePath());

                            if (getArguments().getBoolean(KEY_REMOVE_OLD_FORMAT)) {
                                BackupHandler.deleteRecursive(oldBackup);
                            }

                            Thread.sleep(200);

                        }
                    }

                    return new AsyncTaskResult<>();
                } catch (Exception e) {
                    return new AsyncTaskResult<>(e);
                }
            }

            @Override
            protected void onProgressUpdate(Object... values) {
                if ((Integer) values[0] == 0) {
                    setMainProgress(100 * (Integer) values[1] / (Integer) values[2]);
                    setMainStatusMessage(getString(R.string.processing_backup_x_of_y, (Integer) values[1] + 1, values[2]));
                } else {
                    setSubProgress((Integer) values[1]);
                    setSubStatusMessage((String) values[2]);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<Void> booleanAsyncTaskResult) {
                if (booleanAsyncTaskResult.isSuccess()) {
                    onFinishedSuccess();
                } else {
                    Timber.e(booleanAsyncTaskResult.getException());
                    onFinishedFailure(booleanAsyncTaskResult.getException());
                }

                BackupFragment.notifyBackupsChanged();
            }
        }.execute();
    }

    @Override
    protected void onCancelProcessing() {
        if (processingTask != null) {
            processingTask.cancel(true);
        }
    }

    @Override
    protected boolean hasSubProcess() {
        return true;
    }

    @Override
    protected boolean startAutomatically() {
        return true;
    }

}

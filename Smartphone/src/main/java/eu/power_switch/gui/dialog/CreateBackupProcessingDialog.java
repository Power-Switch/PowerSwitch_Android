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

import eu.power_switch.R;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.backup.OnZipProgressChangedListener;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.gui.fragment.BackupFragment;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to create a new Backup
 */
public class CreateBackupProcessingDialog extends ProcessingDialog {

    public static final String KEY_NAME = "name";
    public static final String KEY_FORCE = "force";

    private AsyncTask<Void, Object, AsyncTaskResult<Void>> processingTask;

    public static CreateBackupProcessingDialog newInstance(String backupName, boolean force) {
        Bundle args = new Bundle();
        args.putString(KEY_NAME, backupName);
        args.putBoolean(KEY_FORCE, force);

        CreateBackupProcessingDialog fragment = new CreateBackupProcessingDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onStartProcessing() throws Exception {
        processingTask = new AsyncTask<Void, Object, AsyncTaskResult<Void>>() {

            @Override
            protected void onPreExecute() {
                setMainStatusMessage(R.string.creating_backup);
            }

            @Override
            protected AsyncTaskResult<Void> doInBackground(Void... voids) {
                try {
                    BackupHandler backupHandler = new BackupHandler(getActivity());
                    backupHandler.createBackup(false, getArguments().getString(KEY_NAME), getArguments().getBoolean(KEY_FORCE), new OnZipProgressChangedListener() {
                        @Override
                        public void onProgressChanged(ProgressMonitor progressMonitor) {
                            publishProgress(progressMonitor.getPercentDone());
                        }
                    });

                    return new AsyncTaskResult<>();
                } catch (Exception e) {
                    return new AsyncTaskResult<>(e);
                }
            }

            @Override
            protected void onProgressUpdate(Object... values) {
                setMainProgress((Integer) values[0]);
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<Void> voidAsyncTaskResult) {
                if (voidAsyncTaskResult.isSuccess()) {
                    onFinishedSuccess();
                } else {
                    Log.e(CreateBackupProcessingDialog.class, voidAsyncTaskResult.getException());
                    onFinishedFailure(voidAsyncTaskResult.getException());
                }

                BackupFragment.sendBackupsChangedBroadcast(getActivity());
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
    protected int getDialogTitle() {
        return R.string.create_backup;
    }

    @Override
    protected boolean hasSubProcess() {
        return false;
    }

    @Override
    protected boolean startAutomatically() {
        return true;
    }
}
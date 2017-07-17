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

package eu.power_switch.backup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.exception.backup.BackupAlreadyExistsException;
import eu.power_switch.shared.exception.backup.BackupNotFoundException;
import eu.power_switch.shared.exception.backup.CreateBackupException;
import eu.power_switch.shared.exception.backup.RemoveBackupException;
import eu.power_switch.shared.exception.backup.RestoreBackupException;
import timber.log.Timber;

/**
 * Database Handler to access/modify Backups stored on device or external storage
 */
@Singleton
public class BackupHandler {

    /**
     * Default Folder name for storing Backups (on /sdcard/)
     */
    public static final String MAIN_BACKUP_FOLDERNAME = "PowerSwitch_Backup";

    public static final String BACKUP_FILE_SUFFIX = ".psbak";

    public static final char[] BACKUP_PASSWORD = "ps_backup".toCharArray();

    @Inject
    protected SmartphonePreferencesHandler smartphonePreferencesHandler;

    @Inject
    protected StatusMessageHandler statusMessageHandler;

    /**
     * Context
     */
    private Context context;

    /**
     * Constructor
     *
     * @param context any suitable context
     */
    @Inject
    public BackupHandler(Context context) {
        this.context = context;
    }

    /**
     * Checks if Backups using the old format exist in backup directory
     *
     * @return true if old backups exist, false otherwise
     */
    public boolean oldBackupFormatsExist() {
        File backupDir = new File(getBackupPath());

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

        return backupDir.exists() && backupDir.listFiles(backupFileFilter).length > 0;
    }

    /**
     * Deletes a folder/file recursively
     *
     * @param fileOrDirectory
     *
     * @return
     *
     * @throws Exception
     */
    public static boolean deleteRecursive(@NonNull File fileOrDirectory) throws Exception {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }

    /**
     * Get all Backups
     *
     * @return List of Backups
     */
    @NonNull
    public ArrayList<Backup> getBackups() {
        ArrayList<Backup> backups   = new ArrayList<>();
        File              backupDir = new File(getBackupPath());

        FileFilter backupFileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName()
                        .endsWith(BACKUP_FILE_SUFFIX);
            }
        };

        if (backupDir.exists()) {
            for (File file : backupDir.listFiles(backupFileFilter)) {
                backups.add(new Backup(file.getName()
                        .replace(BACKUP_FILE_SUFFIX, ""), new Date(file.lastModified()), backupDir + File.separator + file.getName(), false));
            }
        }
        return backups;
    }

    /**
     * Creates a new Backup
     *
     * @param useExternalStorage use external storage path instead of internal?
     * @param name               name of backup
     * @param force              overwrite existing folders?
     *
     * @throws CreateBackupException
     * @throws BackupAlreadyExistsException
     */
    public void createBackup(boolean useExternalStorage, @NonNull String name, boolean force,
                             @NonNull OnZipProgressChangedListener onZipProgressChangedListener) throws CreateBackupException, BackupAlreadyExistsException {
        if (useExternalStorage) {
            // TODO: kp wie man internen und externen speicher unterscheidet
        } else {
            File dst;

            // check if base backup folder exists
            dst = new File(getBackupPath());
            if (!dst.exists()) {
                dst.mkdirs();
            }

            dst = new File(getBackupPath() + File.separator + name + BACKUP_FILE_SUFFIX);
            if (dst.exists()) {
                if (force) {
                    // remove existing backup
                    try {
                        if (!deleteRecursive(dst)) {
                            throw new CreateBackupException("Error deleting existing Backup");
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                        throw new CreateBackupException(e);
                    }
                } else {
                    throw new BackupAlreadyExistsException();
                }
            }

            try {
                ZipHelper.createZip(getBackupPath() + File.separator + name + BACKUP_FILE_SUFFIX,
                        BACKUP_PASSWORD,
                        onZipProgressChangedListener,
                        context.getFilesDir()
                                .getParent());
            } catch (Exception e) {
                Timber.e(e);
                throw new CreateBackupException(e);
            }
        }
    }

    /**
     * Remove a Backup
     *
     * @param name name of backup
     *
     * @throws BackupNotFoundException
     * @throws RemoveBackupException
     */
    public void removeBackup(@NonNull String name) throws BackupNotFoundException, RemoveBackupException {
        try {
            File backupFolder = new File(getBackupPath() + File.separator + name);

            File backupZipFile = new File(getBackupPath() + File.separator + name + BACKUP_FILE_SUFFIX);

            if (backupFolder.exists() || backupZipFile.exists()) {
                deleteRecursive(backupFolder);
                backupZipFile.delete();
            } else {
                throw new BackupNotFoundException();
            }
        } catch (Exception e) {
            Timber.e(e);
            throw new RemoveBackupException(e);
        }
    }

    /**
     * Rename existing Backup
     *
     * @param oldName old backup name
     * @param newName new backup name
     *
     * @throws BackupNotFoundException
     * @throws BackupAlreadyExistsException
     */
    public void renameBackup(@NonNull String oldName, @NonNull String newName) throws BackupNotFoundException, BackupAlreadyExistsException {
        File oldZipFile = new File(getBackupPath() + File.separator + oldName + BACKUP_FILE_SUFFIX);
        File newFolder  = new File(getBackupPath() + File.separator + newName + BACKUP_FILE_SUFFIX);

        if (!oldZipFile.exists()) {
            throw new BackupNotFoundException();
        }
        if (newFolder.exists()) {
            throw new BackupAlreadyExistsException();
        }
        oldZipFile.renameTo(newFolder);
    }

    /**
     * Restore Backup
     *
     * @param filePath absolute file path of the backup
     *
     * @throws BackupNotFoundException
     * @throws RestoreBackupException
     */
    public void restoreBackup(@NonNull String filePath,
                              @NonNull OnZipProgressChangedListener onZipProgressChangedListener) throws BackupNotFoundException, RestoreBackupException {
        try {
            // create destination path object
            File dst = new File(context.getFilesDir()
                    .getParent());

            // delete existing files
            for (File fileOrFolder : dst.listFiles()) {
                deleteRecursive(fileOrFolder);
            }

            ZipHelper.extractZip(filePath,
                    context.getFilesDir()
                            .getParent(),
                    BACKUP_PASSWORD,
                    onZipProgressChangedListener);
        } catch (Exception e) {
            Timber.e(e);
            throw new RestoreBackupException(e);
        }
    }

    /**
     * Opens a share dialog to share a backup with any suitable application
     *
     * @param context any suitable context
     * @param backup  a valid backup
     */
    public void shareBackup(@NonNull Context context, @NonNull Backup backup) {
        try {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);

            intentShareFile.setType("*/*");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + backup.getPath()));
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.powerswitch_backup_file, backup.getName()));
            intentShareFile.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.powerswitch_backup_file, backup.getName()));

            context.startActivity(Intent.createChooser(intentShareFile, context.getString(R.string.send_to)));
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(context, e);
        }
    }

    private void copyFile(@NonNull File src, @NonNull File dst) throws IOException {
        if (src.exists()) {
            InputStream in = new FileInputStream(src);
            if (dst.exists()) {
                dst.delete();
            }
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int    len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    // If targetLocation does not exist, it will be created.
    private void copyDirectory(@NonNull File sourceLocation, @NonNull File targetLocation) throws Exception {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (String child : children) {
                copyDirectory(new File(sourceLocation, child), new File(targetLocation, child));
            }
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    private String getBackupPath() {
        return smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.BACKUP_PATH);
    }
}

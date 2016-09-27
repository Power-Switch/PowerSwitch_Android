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

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * This class represents a Backup stored on the device.
 * Backups store all app data saved in "shared_preferences" and "database" folder
 */
public class Backup {

    /**
     * Backup name
     */
    private String name;
    /**
     * Backup creation date
     */
    private Date date;
    /**
     * Backup path
     */
    private String path;
    /**
     * true when Backup is stored on external storage (sdcard)
     */
    private boolean externalStorage;

    /**
     * Default constructor
     *
     * @param name            name of Backup
     * @param date            creation date of Backup
     * @param path            path where Backup is stored
     * @param externalStorage true if Backup is stored on external storage (sdcard)
     */
    public Backup(@NonNull String name, @NonNull Date date, @NonNull String path, boolean externalStorage) {
        this.name = name;
        this.date = date;
        this.path = path;
        this.externalStorage = externalStorage;
    }

    /**
     * Get Backup name
     *
     * @return Backup name
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Get Backup creation date
     *
     * @return Backup creation date
     */
    @NonNull
    public Date getDate() {
        return date;
    }

    /**
     * Get full Backup file path
     *
     * @return Backup file path
     */
    @NonNull
    public String getPath() {
        return path;
    }

    /**
     * Checks if this Backup is stored on an external storage (sdcard)
     *
     * @return true if stored on external storage
     */
    public boolean isOnExternalStorage() {
        return externalStorage;
    }

    /**
     * Compare creation (technically modification) date to another Backup
     *
     * @param backup another Backup
     * @return
     */
    public int compareDate(@NonNull Backup backup) {
        return date.compareTo(backup.getDate());
    }
}

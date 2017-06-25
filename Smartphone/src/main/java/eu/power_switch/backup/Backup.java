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

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import lombok.Data;
import lombok.ToString;

/**
 * This class represents a Backup stored on the device.
 * Backups store all app data saved in "shared_preferences" and "database" folder
 */
@Data
@ToString
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
    private int size;

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
     * Compare creation (technically modification) date to another Backup
     *
     * @param backup another Backup
     * @return
     */
    public int compareDate(@NonNull Backup backup) {
        return date.compareTo(backup.getDate());
    }

    /**
     * Get the file size of this Backup in MB
     *
     * @return file size in MB
     */
    public double getSizeInMb() {
        return getSizeInMb(-1);
    }

    /**
     * Get the file size of this Backup in MB
     *
     * @param decimalPlaces decimal places to round the value
     * @return file size in MB
     */
    public double getSizeInMb(int decimalPlaces) {
        try {
            // Get file from file name
            File file = new File(getPath());

            // Get length of file in bytes
            long fileSizeInBytes = file.length();
            // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
            double fileSizeInKB = fileSizeInBytes / 1024;
            // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            double fileSizeInMB = fileSizeInKB / 1024;

            if (decimalPlaces != -1) {
                BigDecimal bd = new BigDecimal(fileSizeInMB);
                bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else {
                return fileSizeInMB;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}

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

package eu.power_switch.log;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;
import java.util.Date;

/**
 * This class handles all Log file/folder related stuff
 * <p/>
 * Created by Markus on 25.08.2015.
 */
public class LogHandler {

    /**
     * Folder name in which logs are saved
     */
    public static final String LOG_FOLDER = "PowerSwitch_Logs";

    private LogHandler() {
    }

    /**
     * Delete Logs older than 14 days
     */
    public static void removeOldLogs() {
        File logFolder = new File(Environment.getExternalStorageDirectory()
                .getPath() + File.separator + LOG_FOLDER);
        File[] logFiles = logFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (dir.getPath()
                        .equals(Environment.getExternalStorageDirectory().getPath() + File.separator + LOG_FOLDER) &&
                        filename.endsWith(".log")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        for (File logFile : logFiles) {
            Calendar lastEdited = Calendar.getInstance();
            lastEdited.setTime(new Date(logFile.lastModified()));

            Calendar limit = Calendar.getInstance();
            limit.setTime(new Date());
            limit.add(Calendar.DAY_OF_YEAR, -14);

            if (lastEdited.before(limit)) {
                logFile.delete();
            }
        }
    }

    /**
     * Checks if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * If necessary this method creates a directory in which logs are saved
     *
     * @return true if directory exists or was successfully created
     */
    public static boolean createLogDirectory() {
        File dst = new File(Environment.getExternalStorageDirectory()
                .getPath() + File.separator + LOG_FOLDER);
        if (!dst.exists()) {
            return dst.mkdirs();
        }
        return true;
    }
}

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

package eu.power_switch.shared.log;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.power_switch.shared.R;
import eu.power_switch.shared.exception.permission.MissingPermissionException;
import eu.power_switch.shared.permission.PermissionHelper;

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

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private LogHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    public static void configureLogger() {
        Log4JConfiguration.configure();
    }

    /**
     * Delete Logs older than 14 days
     */
    public static void removeOldLogs() {
        for (File logFile : getLogFiles()) {
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
     * Get all zip file containing all current log files
     *
     * @return Zip file containing log files
     */
    @Nullable
    public static File getLogsAsZip(@NonNull Context context) throws MissingPermissionException {
        if (!PermissionHelper.checkWriteExternalStoragePermission(context)) {
            throw new MissingPermissionException(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        String tempZipFileName = "logs.zip";
        int bufferSize = 1024;

        // delete previous temp zip file
        File zipFile = new File(Environment.getExternalStorageDirectory()
                .getPath() + File.separator + LOG_FOLDER + File.separator + tempZipFileName);
        if (zipFile.exists()) {
            zipFile.delete();
        }

        BufferedInputStream origin = null;
        FileOutputStream dest = null;
        try {
            dest = new FileOutputStream(Environment.getExternalStorageDirectory()
                    .getPath() + File.separator + LOG_FOLDER + File.separator + tempZipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[bufferSize];

            for (File logFile : getLogFiles()) {
                FileInputStream fi = new FileInputStream(logFile);
                origin = new BufferedInputStream(fi, bufferSize);

                ZipEntry entry = new ZipEntry(logFile.getName());
                out.putNextEntry(entry);

                int count;
                while ((count = origin.read(data, 0, bufferSize)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();

            return zipFile;
        } catch (Exception e) {
            Log.e(e);
        } finally {
            if (origin != null) {
                try {
                    origin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dest != null) {
                try {
                    dest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * Checks if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
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

    /**
     * Get a list of all log files
     *
     * @return list of log files
     */
    @NonNull
    private static List<File> getLogFiles() {
        File logFolder = new File(Environment.getExternalStorageDirectory()
                .getPath() + File.separator + LOG_FOLDER);
        File[] logFiles = logFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return dir.getPath()
                        .equals(Environment.getExternalStorageDirectory().getPath() + File.separator + LOG_FOLDER) &&
                        filename.endsWith(".log");
            }
        });

        return Arrays.asList(logFiles);
    }

    public static void sendLogsAsMail(@NonNull Context context) throws Exception {
        if (!PermissionHelper.checkWriteExternalStoragePermission(context)) {
            throw new MissingPermissionException(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SENDTO);
        emailIntent.setType("*/*");
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@power-switch.eu"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PowerSwitch Logs");
        emailIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.send_logs_template));
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(LogHandler.getLogsAsZip(context)));

        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_to)));
    }

    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

    /**
     * Add indentation to a String with multiple lines
     *
     * @param string
     * @return indented string
     */
    public static String addIndentation(String string) {
        StringBuilder stringBuilder = new StringBuilder();

        Scanner scanner = new Scanner(string);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // process the line
            stringBuilder.append("\t").append(line);

            if (scanner.hasNextLine()) {
                stringBuilder.append("\n");
            }
        }
        scanner.close();

        return stringBuilder.toString();
    }
}

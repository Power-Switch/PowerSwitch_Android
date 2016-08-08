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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.mindpipe.android.logging.log4j.LogConfigurator;
import eu.power_switch.shared.R;
import eu.power_switch.shared.application.ApplicationHelper;
import eu.power_switch.shared.exception.permission.MissingPermissionException;
import eu.power_switch.shared.permission.PermissionHelper;

/**
 * This class handles all Log file/folder related stuff
 * <p/>
 * Created by Markus on 25.08.2015.
 */
public class LogHandler {

    /**
     * Folder name in which logs are saved on external storage (if available)
     */
    public static final String LOG_FOLDER_NAME_EXTERNAL = "PowerSwitch_Logs";

    /**
     * Folder name in which logs are saved on internal storage
     */
    public static final String LOG_FOLDER_NAME_INTERNAL = "logs";

    /**
     * Duration in days to keep log files (file creation date)
     */
    private static final int KEEP_LOGS_DAY_COUNT = 14;

    /**
     * Default E-Mail recipients
     */
    private static final String[] DEFAULT_EMAILS = new String[]{"contact@power-switch.eu"};

    private static Context context;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private LogHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    public static void configureLogger(Context context) {
        LogHandler.context = context;

        configureInternalLogger(context);
        configureExternalLogger();
    }

    public static void configureInternalLogger(Context context) {
        LogConfigurator internalLogConfigurator = new LogConfigurator();
        internalLogConfigurator.setFileName(context.getFilesDir().getParent() + File.separator +
                LOG_FOLDER_NAME_INTERNAL + File.separator + "PowerSwitch__" + getHumanReadableDate() + ".log");
        String filePattern = "%d{dd-MM-yyyy HH:mm:ss,SSS} [%-5p] %m%n";
        internalLogConfigurator.setFilePattern(filePattern);
        String logCatPattern = "[%-5p] %m%n";
        internalLogConfigurator.setLogCatPattern(logCatPattern);
        internalLogConfigurator.setRootLevel(Level.ALL);
        internalLogConfigurator.setImmediateFlush(true);
        internalLogConfigurator.setUseLogCatAppender(true);
        internalLogConfigurator.setUseFileAppender(true);
        internalLogConfigurator.setMaxFileSize(10 * 1024 * 1024); // 10 MB
        try {
            internalLogConfigurator.configure();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            LogHandler.removeOldInternalLogs();
        } catch (Exception e) {
            try {
                Log.e(e);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        }
    }

    private static void configureExternalLogger() {
        if (LogHandler.isExternalStorageReadable() &&
                LogHandler.isExternalStorageWritable() &&
                LogHandler.createExternalLogDirectory()) {
            FileAppender fileAppender = new FileAppender();
            fileAppender.setName("ExternalStorageAppender");
            fileAppender.setFile(Environment.getExternalStorageDirectory() + File.separator +
                    LOG_FOLDER_NAME_EXTERNAL + File.separator + "PowerSwitch__" + getHumanReadableDate() + ".log");
            String filePattern = "%d{dd-MM-yyyy HH:mm:ss,SSS} [%-5p] %m%n";
            fileAppender.setLayout(new PatternLayout(filePattern));
            fileAppender.setThreshold(Level.ALL);
            fileAppender.setAppend(true);
            fileAppender.setImmediateFlush(true);
            fileAppender.activateOptions();

            Logger.getRootLogger().addAppender(fileAppender);

            try {
                LogHandler.removeOldExternalLogs();
            } catch (Exception e) {
                try {
                    Log.e(e);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete internal Logs older than 14 days
     */
    private static void removeOldInternalLogs() {
        for (File logFile : getInternalLogFiles()) {
            Calendar lastEdited = Calendar.getInstance();
            lastEdited.setTime(new Date(logFile.lastModified()));

            Calendar limit = Calendar.getInstance();
            limit.setTime(new Date());
            limit.add(Calendar.DAY_OF_YEAR, -KEEP_LOGS_DAY_COUNT);

            if (lastEdited.before(limit)) {
                logFile.delete();
            }
        }
    }

    /**
     * Delete external Logs older than 14 days
     */
    public static void removeOldExternalLogs() {
        for (File logFile : getExternalLogFiles()) {
            Calendar lastEdited = Calendar.getInstance();
            lastEdited.setTime(new Date(logFile.lastModified()));

            Calendar limit = Calendar.getInstance();
            limit.setTime(new Date());
            limit.add(Calendar.DAY_OF_YEAR, -KEEP_LOGS_DAY_COUNT);

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
    public static File getLogsAsZip() throws MissingPermissionException {
        if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(context)) {
            throw new MissingPermissionException(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        String tempZipFileName = "logs.zip";
        String tempZipFilePath = Environment.getExternalStorageDirectory() + File.separator +
                LOG_FOLDER_NAME_EXTERNAL + File.separator + tempZipFileName;
        int bufferSize = 1024;

        // delete previous temp zip file
        File zipFile = new File(tempZipFilePath);
        if (zipFile.exists()) {
            zipFile.delete();
        }

        BufferedInputStream origin = null;
        FileOutputStream dest = null;
        try {
            dest = new FileOutputStream(tempZipFilePath);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[bufferSize];

            for (File logFile : getInternalLogFiles()) {
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
    public static boolean createExternalLogDirectory() {
        File dst = new File(Environment.getExternalStorageDirectory()
                .getPath() + File.separator + LOG_FOLDER_NAME_EXTERNAL);
        if (!dst.exists()) {
            return dst.mkdirs();
        }
        return true;
    }

    /**
     * Get a list of all external log files
     *
     * @return list of log files
     */
    @NonNull
    private static List<File> getExternalLogFiles() {
        File logFolder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + LOG_FOLDER_NAME_EXTERNAL);
        File[] logFiles = logFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return dir.getPath().equals(
                        Environment.getExternalStorageDirectory().getPath() + File.separator + LOG_FOLDER_NAME_EXTERNAL) &&
                        filename.endsWith(".log");
            }
        });

        return Arrays.asList(logFiles);
    }

    /**
     * Get a list of all internal log files
     *
     * @return list of log files
     */
    @NonNull
    private static List<File> getInternalLogFiles() {
        File logFolder = new File(context.getFilesDir().getParent() + File.separator + LOG_FOLDER_NAME_INTERNAL);
        File[] logFiles = logFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return dir.getPath().equals(
                        context.getFilesDir().getParent() + File.separator + LOG_FOLDER_NAME_INTERNAL) &&
                        filename.endsWith(".log");
            }
        });

        return Arrays.asList(logFiles);
    }

    /**
     * Send Logs to an Email App via Intent
     *
     * @param destinationAddresses destination addresses (or null)
     * @param throwable            an exception that should be used for subject and content text
     * @param timeRaised           time the exception was raised
     */
    public static void sendLogsAsMail(Activity activity, String[] destinationAddresses, Throwable throwable, Date timeRaised) throws Exception {
        if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(context)) {
            throw new MissingPermissionException(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (destinationAddresses == null) {
            destinationAddresses = DEFAULT_EMAILS;
        }

        String subject;
        if (throwable == null) {
            subject = "PowerSwitch Logs";
        } else {
            subject = "Unknown Error - " + throwable.getClass().getSimpleName() +
                    ": " + throwable.getMessage();
        }

        String content;
        if (throwable == null) {
            content = context.getString(R.string.send_logs_template);
        } else {
            content = context.getString(R.string.send_unknown_error_log_template);
            content += "\n\n\n";
            content += "<<<<<<<<<< DEVELOPER INFOS >>>>>>>>>>\n";
            content += "Exception was raised at: " + SimpleDateFormat.getDateTimeInstance().format(timeRaised) + "\n";
            content += "\n";
            content += "PowerSwitch Application Version: " + ApplicationHelper.getAppVersionDescription(activity) + "\n";
            content += "Device API Level: " + android.os.Build.VERSION.SDK_INT + "\n";
            content += "Device OS Version name: " + Build.VERSION.RELEASE + "\n";
            content += "Device brand/model: " + LogHandler.getDeviceName() + "\n";
            content += "\n";
            content += "Exception stacktrace:\n";
            content += "\n";
            content += Log.getStackTraceText(throwable) + "\n";
        }

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SENDTO);
        emailIntent.setType("*/*");
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, destinationAddresses);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(LogHandler.getLogsAsZip()));

        activity.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_to)));
    }

    /**
     * Send Logs to an E-Mail App via Intent
     * This includes an Exception that has been raised just before
     *
     * @param activity   activity context
     * @param throwable  exception
     * @param timeRaised time the exception was raised
     */
    public static void sendLogsAsMail(Activity activity, Throwable throwable, Date timeRaised) throws Exception {
        sendLogsAsMail(activity, null, throwable, timeRaised);
    }

    /**
     * Send Logs to an E-Mail App via Intent
     *
     * @param activity activity context
     */
    public static void sendLogsAsMail(Activity activity) throws Exception {
        sendLogsAsMail(activity, null, null, null);
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

    private static String getHumanReadableDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return simpleDateFormat.format(new Date());
    }
}

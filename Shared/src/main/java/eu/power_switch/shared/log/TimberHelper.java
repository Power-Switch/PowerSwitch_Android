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

package eu.power_switch.shared.log;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.StatusPrinter;
import io.fabric.sdk.android.Fabric;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import timber.log.Timber;

/**
 * Class that provides Timber initialization and custom tree implementations
 * <p>
 * Created by Markus on 01.07.2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimberHelper {

    /**
     * Plants all logger trees based on passed in parameters
     *
     * @param isDebug true if the app is launched in debug mode, false otherwise
     */
    public static void plantTrees(Context context, boolean isDebug, boolean internalFileLoggingOnly) {
        if (isDebug) {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.plant(new FileLoggingTree(context, internalFileLoggingOnly));
        Timber.plant(new CrashlyticsTree());
    }

    public static class FileLoggingTree extends Timber.DebugTree {

        /**
         * Folder name in which logs are saved on external storage (if available)
         */
        static final String LOG_FOLDER_NAME_EXTERNAL = "PowerSwitch_Logs";

        /**
         * Folder name in which logs are saved on internal storage
         */
        private static final String LOG_FOLDER_NAME_INTERNAL = "logs";

        /**
         * Duration in days to keep log files (file creation date)
         */
        private static final int KEEP_LOGS_DAY_COUNT = 14;

        private Context context;
        private Logger  root;

        /**
         * Get a list of all internal log files
         *
         * @return list of log files
         */
        @NonNull
        public static List<File> getInternalLogFiles(final Context context) {
            File logFolder = new File(context.getFilesDir()
                    .getParent() + File.separator + LOG_FOLDER_NAME_INTERNAL);
            File[] logFiles = logFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return dir.getPath()
                            .equals(context.getFilesDir()
                                    .getParent() + File.separator + LOG_FOLDER_NAME_INTERNAL) && filename.endsWith(".log");
                }
            });

            return Arrays.asList(logFiles);
        }

        private FileLoggingTree(Context context, boolean internalOnly) {
            this.context = context;
            configureLogger(internalOnly);
        }

        private void configureLogger(boolean internalOnly) {
            // add the newly created appenders to the root logger;
            // qualify Logger to disambiguate from org.slf4j.Logger
            root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.ALL);

            // reset the default context (which may already have been initialized)
            // since we want to reconfigure it
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.reset();

            if (internalOnly) {
                configureInternalLogger(loggerContext);
                return;
            }
            configureExternalLogger(loggerContext);
        }

        private void configureInternalLogger(LoggerContext loggerContext) {
            configureLogger(loggerContext,
                    context.getFilesDir()
                            .getParent() + File.separator + LOG_FOLDER_NAME_INTERNAL + File.separator);
        }

        private void configureExternalLogger(LoggerContext loggerContext) {
            if (isExternalStorageReadable() && isExternalStorageWritable() && createExternalLogDirectory()) {
                configureLogger(loggerContext,
                        Environment.getExternalStorageDirectory() + File.separator + LOG_FOLDER_NAME_EXTERNAL + File.separator);
            }
        }

        private void configureLogger(LoggerContext loggerContext, String filePath) {
            RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
            rollingFileAppender.setContext(loggerContext);
            rollingFileAppender.setAppend(true);
            rollingFileAppender.setFile(filePath + "PowerSwitch__latest.log");

            SizeAndTimeBasedFNATP<ILoggingEvent> fileNamingPolicy = new SizeAndTimeBasedFNATP<>();
            fileNamingPolicy.setContext(loggerContext);
            fileNamingPolicy.setMaxFileSize("10MB");

            TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
            rollingPolicy.setContext(loggerContext);
            rollingPolicy.setFileNamePattern(context.getFilesDir()
                    .getParent() + File.separator + LOG_FOLDER_NAME_INTERNAL + File.separator + "PowerSwitch__.%d{yyyy-MM-dd}.%i.log");
            rollingPolicy.setMaxHistory(5);
            rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(fileNamingPolicy);
            rollingPolicy.setParent(rollingFileAppender);  // parent and context required!
            rollingPolicy.start();

            // text encoder - very clean pattern, takes up less space
            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(loggerContext);
            encoder.setCharset(Charset.forName("UTF-8"));
            encoder.setPattern("%date %level [%thread] %msg%n");
            encoder.start();

            rollingFileAppender.setRollingPolicy(rollingPolicy);
            rollingFileAppender.setEncoder(encoder);
            rollingFileAppender.start();

            root.addAppender(rollingFileAppender);

            // print any status messages (warnings, etc) encountered in logback config
            StatusPrinter.print(loggerContext);
        }

        /**
         * Delete internal Logs older than 14 days
         */
        private void removeOldInternalLogs() {
            for (File logFile : getInternalLogFiles(context)) {
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
        private void removeOldExternalLogs() {
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
         * Checks if external storage is available for read and write
         */
        private boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state);
        }

        /**
         * Checks if external storage is available to at least read
         */
        private boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        }

        /**
         * If necessary this method creates a directory in which logs are saved
         *
         * @return true if directory exists or was successfully created
         */
        private boolean createExternalLogDirectory() {
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
        private List<File> getExternalLogFiles() {
            File logFolder = new File(Environment.getExternalStorageDirectory()
                    .getPath() + File.separator + LOG_FOLDER_NAME_EXTERNAL);
            File[] logFiles = logFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return dir.getPath()
                            .equals(Environment.getExternalStorageDirectory()
                                    .getPath() + File.separator + LOG_FOLDER_NAME_EXTERNAL) && filename.endsWith(".log");
                }
            });

            return Arrays.asList(logFiles);
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE) {
                return;
            }

            String logMessage = tag + ": " + message;
            switch (priority) {
                case Log.DEBUG:
                case Log.INFO:
                    root.debug(logMessage);
                    break;
                case Log.WARN:
                    root.warn(logMessage);
                    break;
                case Log.ERROR:
                    if (t != null) {
                        root.error(message, t);
                    }
                    root.error(logMessage);
                    break;
            }
        }
    }

    private static class CrashlyticsTree extends Timber.DebugTree {
        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG      = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE  = "message";

        @Override
        protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable t) {
            if (priority == Log.VERBOSE) {
                // ignore verbose messages
                return;
            }

            if (!Fabric.isInitialized()) {
                return;
            }

            Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
            Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);

            if (priority == Log.WARN || priority == Log.INFO || priority == Log.DEBUG) {
                Crashlytics.log(message);
                return;
            }

            Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

            if (t == null) {
                Crashlytics.logException(new Exception(message));
            } else {
                Crashlytics.logException(t);
            }
        }
    }

}

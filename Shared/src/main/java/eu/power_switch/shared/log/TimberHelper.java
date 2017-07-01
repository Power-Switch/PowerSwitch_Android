package eu.power_switch.shared.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.mindpipe.android.logging.log4j.LogConfigurator;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Class that provides Timber initialization and custom tree implementations
 * <p>
 * Created by Markus on 01.07.2017.
 */
public class TimberHelper {

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

            if (internalOnly) {
                configureInternalLogger();
            } else {
                configureLogger();
            }
        }

        private void configureLogger() {
            configureInternalLogger();
            configureExternalLogger();
        }

        private void configureInternalLogger() {
            LogConfigurator internalLogConfigurator = new LogConfigurator();
            internalLogConfigurator.setFileName(context.getFilesDir()
                    .getParent() + File.separator + LOG_FOLDER_NAME_INTERNAL + File.separator + "PowerSwitch__" + getHumanReadableDate() + ".log");
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
                removeOldInternalLogs();
            } catch (Exception e) {
                try {
                    Timber.e(e);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        }

        private void configureExternalLogger() {
            if (isExternalStorageReadable() && isExternalStorageWritable() && createExternalLogDirectory()) {
                FileAppender fileAppender = new FileAppender();
                fileAppender.setName("ExternalStorageAppender");
                fileAppender.setFile(Environment.getExternalStorageDirectory() + File.separator + LOG_FOLDER_NAME_EXTERNAL + File.separator + "PowerSwitch__" + getHumanReadableDate() + ".log");
                String filePattern = "%d{dd-MM-yyyy HH:mm:ss,SSS} [%-5p] %m%n";
                fileAppender.setLayout(new PatternLayout(filePattern));
                fileAppender.setThreshold(Level.ALL);
                fileAppender.setAppend(true);
                fileAppender.setImmediateFlush(true);
                fileAppender.activateOptions();

                Logger.getRootLogger()
                        .addAppender(fileAppender);

                try {
                    removeOldExternalLogs();
                } catch (Exception e) {
                    try {
                        Timber.e(e);
                    } catch (Exception e1) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private String getHumanReadableDate() {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            return simpleDateFormat.format(new Date());
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
                    Log4JLog.d(logMessage);
                    break;
                case Log.WARN:
                    Log4JLog.w(logMessage);
                    break;
                case Log.ERROR:
                    if (t != null) {
                        Log4JLog.e(message, t);
                    }
                    Log4JLog.e(logMessage);
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

            if (priority == Log.DEBUG || priority == Log.INFO) {
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

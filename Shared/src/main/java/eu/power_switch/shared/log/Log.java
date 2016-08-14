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


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import org.apache.log4j.Logger;

import io.fabric.sdk.android.Fabric;

/**
 * Common Logger Class used by all classes in this application
 * <p/>
 * Be careful to import the correct "Log" class when using.
 * <p/>
 * Created by Markus on 11.08.2015.
 */
public class Log {

    public static final Logger LOGGER = Logger.getLogger("Log");

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private Log() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Log Debug
     *
     * @param message any text message
     */
    public static void d(@Nullable String message) {
        LOGGER.debug(message);
        if (Fabric.isInitialized()) {
            Crashlytics.log(message);
        }
    }

    /**
     * Log Debug
     *
     * @param source any object
     */
    public static void d(@Nullable Object source) {
        StringBuilder logMessage = new StringBuilder();
        if (source == null) {
            logMessage.append("null");
        } else if (source instanceof String) {
            logMessage.append(source).append(" : ");
        } else if (source instanceof Class) {
            logMessage.append("{").append(((Class) source).getCanonicalName()).append("}");
        } else if (source instanceof Intent) {
            logMessage.append(getIntentDescription((Intent) source));
        } else {
            logMessage.append(source.getClass());
        }

        LOGGER.debug(logMessage.toString());
        if (Fabric.isInitialized()) {
            Crashlytics.log(logMessage.toString());
        }
    }

    /**
     * Log Debug
     *
     * @param source  a source object
     * @param message any object used as description
     */
    public static void d(@Nullable Object source, @Nullable Object message) {
        StringBuilder logMessage = new StringBuilder();

        if (source != null) {
            logMessage.append("{");
            if (source instanceof String) {
                logMessage.append(source);
            } else if (source instanceof Class) {
                logMessage.append(((Class) source).getCanonicalName());
            } else {
                try {
                    logMessage.append(source.getClass());
                } catch (Exception e) {
                    // do nothing
                }
            }

            logMessage.append("} ");
        }

        if (message instanceof String) {
            logMessage.append(message);
        } else if (message instanceof Intent) {
            logMessage.append(getIntentDescription((Intent) message));
        } else {
            logMessage.append(String.valueOf(message));
        }

        LOGGER.debug(logMessage.toString());
        if (Fabric.isInitialized()) {
            Crashlytics.log(logMessage.toString());
        }
    }

    private static String getIntentDescription(Intent intent) {
        String log = "Action: ";
        log += intent.getAction();
        log += "( ";
        if (intent.getData() != null) {
            log += intent.getData().getScheme();
            log += "://";
            log += intent.getData().getHost();
        }
        log += " ) ";
        Bundle extras = intent.getExtras();
        log += "{ ";
        if (extras != null) {
            for (String extra : extras.keySet()) {
                log += extra + "[" + extras.get(extra) + "], ";
            }
        }
        log += " }";

        return log;
    }

    /**
     * Log Error
     *
     * @param message any text message
     */
    public static void e(@Nullable String message) {
        LOGGER.error(message);
        if (Fabric.isInitialized()) {
            Crashlytics.log(message);
        }
    }

    /**
     * Log Error
     *
     * @param throwable any throwable
     */
    public static void e(@Nullable Throwable throwable) {
        if (throwable != null) {
            LOGGER.error("Error", throwable);
            if (Fabric.isInitialized()) {
                Crashlytics.logException(throwable);
            }
        }
    }

    /**
     * Log Error
     *
     * @param source  source of log message
     * @param message any text message
     */
    public static void e(@Nullable Object source, @Nullable String message) {
        StringBuilder logMessage = new StringBuilder();

        if (source != null) {
            logMessage.append("{");
            if (source instanceof String) {
                logMessage.append(source);
            } else if (source instanceof Class) {
                logMessage.append(((Class) source).getCanonicalName());
            } else {
                try {
                    logMessage.append(source.getClass());
                } catch (Exception e) {
                    // do nothing
                }
            }
            logMessage.append("} ");
        }

        logMessage.append(message);
        LOGGER.error(logMessage.toString());
        if (Fabric.isInitialized()) {
            Crashlytics.log(logMessage.toString());
        }
    }

    /**
     * Log Error
     *
     * @param source    source of log message
     * @param throwable any throwable
     */
    public static void e(@Nullable Object source, @Nullable Throwable throwable) {
        StringBuilder logMessage = new StringBuilder();

        if (source != null) {
            logMessage.append("{");
            if (source instanceof String) {
                logMessage.append(source);
            } else if (source instanceof Class) {
                logMessage.append(((Class) source).getCanonicalName());
            } else {
                try {
                    logMessage.append(source.getClass());
                } catch (Exception e1) {
                    // do nothing
                }
            }
            logMessage.append("} ");
        }

        LOGGER.error(logMessage, throwable);
        if (Fabric.isInitialized()) {
            Crashlytics.logException(throwable);
        }
    }

    /**
     * Log Error
     *
     * @param message   any text message
     * @param throwable any throwable
     */
    public static void e(@Nullable String message, @Nullable Throwable throwable) {
        if (throwable != null) {
            LOGGER.error(message, throwable);
            if (Fabric.isInitialized()) {
                Crashlytics.logException(throwable);
            }
        } else {
            LOGGER.error(message);
            if (Fabric.isInitialized()) {
                Crashlytics.log(message);
            }
        }
    }

    /**
     * Log Warn
     *
     * @param message any text message
     */
    public static void w(@Nullable String message) {
        LOGGER.warn(message);
        if (Fabric.isInitialized()) {
            Crashlytics.log(message);
        }
    }

    /**
     * Returns a human readable String containing the stacktrace of a throwable
     *
     * @param throwable throwable
     * @return StackTrace string
     */
    @NonNull
    public static String getStackTraceText(@Nullable Throwable throwable) {
        return android.util.Log.getStackTraceString(throwable);
    }
}

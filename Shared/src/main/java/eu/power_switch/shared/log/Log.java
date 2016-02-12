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


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.log4j.Logger;

/**
 * Common Logger Class used by all classes in this application
 * <p/>
 * Be careful to import the correct "Log" class when using.
 * <p/>
 * Created by Markus on 11.08.2015.
 */
public class Log {
    private static final Logger log = Logger.getLogger("Log");

    /**
     * Log Debug
     *
     * @param message any text message
     */
    public static void d(@Nullable String message) {
        log.debug(message);
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
        } else if (String.class.equals(source.getClass())) {
            logMessage.append(source).append(" : ");
        } else if (source instanceof Class) {
            logMessage.append("{").append(((Class) source).getCanonicalName()).append("}");
        } else {
            logMessage.append(source.getClass());
        }
        log.debug(logMessage);
    }

    /**
     * Log Debug
     *
     * @param source  a source object
     * @param message any text message
     */
    public static void d(@Nullable Object source, @Nullable String message) {
        StringBuilder logMessage = new StringBuilder();
        if (source != null) {
            logMessage.append("{");
            if (String.class.equals(source.getClass())) {
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
        log.debug(logMessage);
    }

    /**
     * Log Error
     *
     * @param message any text message
     */
    public static void e(@Nullable String message) {
        log.error(message);
    }

    /**
     * Log Error
     *
     * @param throwable any throwable
     */
    public static void e(@Nullable Throwable throwable) {
        if (throwable != null) {
            log.error("Error", throwable);
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
            if (source.getClass().equals(String.class)) {
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
        log.error(logMessage);
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
            if (source.getClass().equals(String.class)) {
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

        log.error(logMessage, throwable);
    }

    /**
     * Log Error
     *
     * @param message   any text message
     * @param throwable any throwable
     */
    public static void e(@Nullable String message, @Nullable Throwable throwable) {
        if (throwable != null) {
            log.error(message, throwable);
        } else {
            log.error(message);
        }
    }

    /**
     * Log Warn
     *
     * @param message any text message
     */
    public static void w(@Nullable String message) {
        log.warn(message);
    }

    /**
     * Returns a human readable String containing the stacktrace of a throwable
     *
     * @param throwable throwable
     * @return StackTrace string
     */
    public static
    @NonNull
    String getStackTraceText(@Nullable Throwable throwable) {
        return android.util.Log.getStackTraceString(throwable);
    }
}

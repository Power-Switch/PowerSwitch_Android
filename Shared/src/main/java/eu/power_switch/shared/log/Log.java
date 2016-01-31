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
    public static void d(String message) {
        log.debug(message);
    }

    /**
     * Log Debug
     *
     * @param source any object
     */
    public static void d(Object source) {
        StringBuilder logMessage = new StringBuilder();
        if (String.class.equals(source.getClass())) {
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
    public static void d(Object source, String message) {
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
    public static void e(String message) {
        log.error(message);
    }

    /**
     * Log Error
     *
     * @param e any throwable
     */
    public static void e(Throwable e) {
        if (e != null) {
            log.error("Error", e);
            e.printStackTrace();
        }
    }

    /**
     * Log Error
     *
     * @param source  source of log message
     * @param message any text message
     */
    public static void e(Object source, String message) {
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
     * @param source source of log message
     * @param e      any throwable
     */
    public static void e(Object source, Throwable e) {
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

        log.error(logMessage, e);
    }

    /**
     * Log Error
     *
     * @param message any text message
     * @param e       any throwable
     */
    public static void e(String message, Throwable e) {
        if (e != null) {
            log.error(message, e);
            e.printStackTrace();
        } else {
            log.error(message);
        }
    }

    /**
     * Log Warn
     *
     * @param message any text message
     */
    public static void w(String message) {
        log.warn(message);
    }
}

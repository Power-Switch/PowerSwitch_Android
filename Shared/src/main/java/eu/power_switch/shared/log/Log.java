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
     * @param source  a source object
     * @param message any text message
     */
    public static void d(Object source, String message) {
        String logMessage = "";
        if (String.class.equals(source.getClass())) {
            logMessage += source + " : ";
        } else if (source instanceof Class) {
            logMessage += source.toString() + " : ";
        } else {
            try {
                logMessage += source.getClass() + " : ";
            } catch (Exception e) {
                // do nothing
            }
        }

        logMessage += message;
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
     * @param source  a source object throwable
     * @param message any text message
     */
    public static void e(Object source, String message) {
        String logMessage = "";

        if (source.getClass().equals(String.class)) {
            logMessage += source + " : ";
        } else if (source instanceof Class) {
            logMessage += source.toString() + " : ";
        } else {
            try {
                logMessage += source.getClass() + " : ";
            } catch (Exception e) {
                // do nothing
            }
        }

        logMessage += message;
        log.error(logMessage);
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
}

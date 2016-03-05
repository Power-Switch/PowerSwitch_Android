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

package eu.power_switch.shared.constants;

/**
 * Created by Markus on 20.02.2016.
 */
public class SleepAsAndroidConstants {

    public static final String PACKAGE_NAME = "com.urbandroid.sleep";

    private static final String ALARM_TRIGGERED_INTENT = "com.urbandroid.sleep.alarmclock.ALARM_ALERT_START";
    private static final String ALARM_SNOOZED_INTENT = "com.urbandroid.sleep.alarmclock.ALARM_SNOOZE_CLICKED_ACTION";
    private static final String ALARM_DISMISSED_INTENT = "com.urbandroid.sleep.alarmclock.ALARM_ALERT_DISMISS";

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private SleepAsAndroidConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    // Sleep As Android
    public enum SLEEP_AS_ANDROID_ALARM_EVENT {
        ALARM_TRIGGERED(0, ALARM_TRIGGERED_INTENT),
        ALARM_SNOOZED(1, ALARM_SNOOZED_INTENT),
        ALARM_DISMISSED(2, ALARM_DISMISSED_INTENT);

        private final int id;
        private final String intentAction;

        SLEEP_AS_ANDROID_ALARM_EVENT(int id, String intentAction) {
            this.id = id;
            this.intentAction = intentAction;
        }

        public static SLEEP_AS_ANDROID_ALARM_EVENT getById(int id) {
            for (SLEEP_AS_ANDROID_ALARM_EVENT e : values()) {
                if (e.getId() == id) {
                    return e;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }

        public String getIntentAction() {
            return intentAction;
        }
    }

}

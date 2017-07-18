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

package eu.power_switch.shared.constants;

import java.util.NoSuchElementException;
import java.util.Set;

import eu.power_switch.shared.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class holding constants related to alarm clock features
 * <p/>
 * Created by Markus on 20.02.2016.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlarmClockConstants {

    public static final int STOCK_TAB_INDEX = 0;
    public static final int SAA_TAB_INDEX   = 1;

    // Stock Android App
    private static final String      STOCK_OLD_TRIGGERED_INTENT   = "com.android.alarmclock.ALARM_ALERT";
    private static final String      STOCK_TRIGGERED_INTENT       = "com.android.deskclock.ALARM_ALERT";
    private static final String      STOCK_ALT_TRIGGERED_INTENT   = "com.google.android.deskclock.ALARM_ALERT";
    private static final String      STOCK_ALT_DISMISS_INTENT     = "com.google.android.deskclock.ALARM_DONE";
    private static final String      STOCK_DISMISS_INTENT         = "com.android.deskclock.ALARM_DISMISS";
    private static final String      STOCK_OLD_DISMISS_INTENT     = "com.android.alarmclock.ALARM_DONE";
    private static final String      STOCK_SNOOZED_INTENT         = "com.android.deskclock.ALARM_SNOOZE";
    public static final  Set<String> ALARM_SNOOZED_INTENTS        = Sets.newHashSet(STOCK_SNOOZED_INTENT);
    // Samsung
    private static final String      SAMSUNG_TRIGGERED_INTENT     = "com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT";
    private static final String      SAMSUNG_DISMISS_INTENT       = "com.samsung.sec.android.clockpackage.alarm.ALARM_DONE";
    // HTC
    private static final String      HTC_TRIGGERED_INTENT         = "com.htc.android.worldclock.ALARM_ALERT";
    private static final String      HTC_DISMISS_INTENT           = "com.htc.android.worldclock.ALARM_DONE";
    // Sony
    private static final String      SONY_TRIGGERED_INTENT        = "com.sonyericsson.alarm.ALARM_ALERT";
    private static final String      SONY_DISMISS_INTENT          = "com.sonyericsson.alarm.ALARM_DONE";
    // ZTE
    private static final String      ZTE_TRIGGERED_INTENT         = "zte.com.cn.alarmclock.ALARM_ALERT";
    // Motorola
    private static final String      MOTOROLA_TRIGGERED_INTENT    = "com.motorola.blur.alarmclock.ALARM_ALERT";
    private static final String      MOTOROLA_DISMISS_INTENT      = "com.motorola.blur.alarmclock.ALARM_DONE";
    // LG
    private static final String      LG_OLD_TRIGGERED_INTENT      = "com.lge.alarm.alarmclocknew.ALARM_ALERT";
    private static final String      LG_TRIGGERED_INTENT          = "com.lge.clock.ALARM_ALERT";
    private static final String      LG_DISMISS_INTENT            = "com.lge.clock.ALARM_DONE";
    // Night Clock
    private static final String      NIGHT_CLOCK_TRIGGERED_INTENT = "com.neddashfox.nightclockdonate.ALARM_ALERT";
    public static final  Set<String> ALARM_TRIGGERED_INTENTS      = Sets.newHashSet(STOCK_OLD_TRIGGERED_INTENT,
            STOCK_TRIGGERED_INTENT,
            STOCK_ALT_TRIGGERED_INTENT,
            SAMSUNG_TRIGGERED_INTENT,
            HTC_TRIGGERED_INTENT,
            SONY_TRIGGERED_INTENT,
            ZTE_TRIGGERED_INTENT,
            MOTOROLA_TRIGGERED_INTENT,
            LG_OLD_TRIGGERED_INTENT,
            LG_TRIGGERED_INTENT,
            NIGHT_CLOCK_TRIGGERED_INTENT);
    private static final String      NIGHT_CLOCK_DISMISS_INTENT   = "com.neddashfox.nightclockdonate.ALARM_DONE";
    public static final  Set<String> ALARM_DISMISSED_INTENTS      = Sets.newHashSet(STOCK_DISMISS_INTENT,
            STOCK_ALT_DISMISS_INTENT,
            STOCK_OLD_DISMISS_INTENT,
            SAMSUNG_DISMISS_INTENT,
            HTC_DISMISS_INTENT,
            SONY_DISMISS_INTENT,
            MOTOROLA_DISMISS_INTENT,
            LG_DISMISS_INTENT,
            NIGHT_CLOCK_DISMISS_INTENT);

    public enum Event {
        ALARM_TRIGGERED(0), ALARM_SNOOZED(1), ALARM_DISMISSED(2);

        private final int id;

        Event(int id) {
            this.id = id;
        }

        public static Event getById(int id) {
            for (Event e : values()) {
                if (e.getId() == id) {
                    return e;
                }
            }
            throw new NoSuchElementException("No Event with id: " + id);
        }

        public int getId() {
            return id;
        }
    }

}

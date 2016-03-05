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

import java.util.Set;

import eu.power_switch.shared.Sets;

/**
 * Created by Markus on 20.02.2016.
 */
public class ClockAppConstants {

    // Stock Android App
    private static final String STOCK_TRIGGERED_INTENT = "com.android.deskclock.ALARM_ALERT";
    private static final String STOCK_DISMISS_INTENT = "com.android.deskclock.ALARM_DISMISS";
    public static final Set<String> ALARM_DISMISSED_INTENTS = Sets.newHashSet(STOCK_DISMISS_INTENT);
    private static final String STOCK_SNOOZED_INTENT = "com.android.deskclock.ALARM_SNOOZE";
    public static final Set<String> ALARM_SNOOZED_INTENTS = Sets.newHashSet(STOCK_SNOOZED_INTENT);
    // Samsung
    private static final String SAMSUNG_TRIGGERED_INTENT = "com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT";
    // HTC
    private static final String HTC_TRIGGERED_INTENT = "com.htc.android.worldclock.ALARM_ALERT";
    // Sony
    private static final String SONY_TRIGGERED_INTENT = "com.sonyericsson.alarm.ALARM_ALERT";
    // ZTE
    private static final String ZTE_TRIGGERED_INTENT = "zte.com.cn.alarmclock.ALARM_ALERT";
    // Motorola
    private static final String MOTOROLA_TRIGGERED_INTENT = "com.motorola.blur.alarmclock.ALARM_ALERT";
    // LG
    private static final String LG_TRIGGERED_INTENT = "com.lge.alarm.alarmclocknew.ALARM_ALERT";
    public static final Set<String> ALARM_TRIGGERED_INTENTS = Sets.newHashSet(STOCK_TRIGGERED_INTENT, SAMSUNG_TRIGGERED_INTENT, HTC_TRIGGERED_INTENT, SONY_TRIGGERED_INTENT, ZTE_TRIGGERED_INTENT, MOTOROLA_TRIGGERED_INTENT, LG_TRIGGERED_INTENT);

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private ClockAppConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

}

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

package eu.power_switch.timer;

import android.support.annotation.Nullable;

import net.e175.klaus.solarpositioning.SPA;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Helper class for interaction with the solarpositioning library
 * <p>
 * Created by Markus on 22.07.2017.
 */
public final class SunHelper {

    private static final int DELTA_T = 68;

    @Nullable
    public static Calendar getSunrise(Calendar calendar, double latitude, double longitude) {
        return getTransitSet(calendar, latitude, longitude)[0];
    }

    @Nullable
    public static Calendar getSunset(Calendar calendar, double latitude, double longitude) {
        return getTransitSet(calendar, latitude, longitude)[2];
    }

    private static GregorianCalendar[] getTransitSet(Calendar calendar, double latitude, double longitude) {
        GregorianCalendar instance = new GregorianCalendar();
        instance.setTimeInMillis(calendar.getTimeInMillis());

        return SPA.calculateSunriseTransitSet(instance, latitude, longitude, DELTA_T);
    }

}

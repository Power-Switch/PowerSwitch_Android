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

package eu.power_switch.special;

import android.content.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;

/**
 * This class is responsible for generating holiday messages
 * <p/>
 * Created by Markus on 29.11.2015.
 */
public class HolidaySpecialHandler {

    /**
     * Shows a Snackbar/Toast if current date is a holiday
     *
     * @param context any suitable context
     */
    public static void showHolidaySpecial(Context context) {
        Calendar currentDate = Calendar.getInstance();
        Calendar easterDate = getEasterDate(currentDate.get(Calendar.YEAR));

        if (Calendar.DECEMBER == currentDate.get(Calendar.MONTH) && currentDate.get(Calendar.DAY_OF_MONTH) == 24) {
            showChristmasMessage(context);
        } else if (Calendar.JANUARY == currentDate.get(Calendar.MONTH) && currentDate.get(Calendar.DAY_OF_MONTH) == 1) {
            showNewYearMessage(context);
        } else if (Calendar.OCTOBER == currentDate.get(Calendar.MONTH) && currentDate.get(Calendar.DAY_OF_MONTH) == 31) {
            showHalloweenMessage(context);
        } else if (easterDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) && easterDate.get(Calendar
                .DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)) {
            showEasterMessage(context);
        }
    }

    private static void showChristmasMessage(Context context) {
        StatusMessageHandler.showStatusMessage(context, R.string.merry_christmas, 5000);
    }

    private static void showHalloweenMessage(Context context) {
        StatusMessageHandler.showStatusMessage(context, R.string.happy_halloween, 5000);
    }

    private static void showEasterMessage(Context context) {
        StatusMessageHandler.showStatusMessage(context, R.string.happy_easter, 5000);
    }

    private static void showNewYearMessage(Context context) {
        StatusMessageHandler.showStatusMessage(context, R.string.happy_new_year, 5000);
    }

    /**
     * Calculate date of easter
     *
     * @param year year to calculate date of easter
     * @return Easter date
     */
    private static Calendar getEasterDate(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int n = (h + l - 7 * m + 114) / 31;
        int p = (h + l - 7 * m + 114) % 31;
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.clear();
        calendar.set(year, n - 1, p + 1);
        return calendar;
    }
}

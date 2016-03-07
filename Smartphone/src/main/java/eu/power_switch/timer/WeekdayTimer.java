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

package eu.power_switch.timer;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.shared.log.LogHandler;

/**
 * Timer based on an execution time and weekdays
 * <p/>
 * Created by Markus on 12.09.2015.
 */
public class WeekdayTimer extends Timer {

    public static final long INTERVAL_DAILY = 1000 * 60 * 60 * 24;

    private ArrayList<Day> executionDays;
    private Calendar executionTime;

    public WeekdayTimer(long id, boolean isActive, String name, Calendar executionTime, ArrayList<Day> executionDays,
                        ArrayList<Action> actions) {
        super(id, isActive, name, EXECUTION_TYPE_WEEKDAY, actions);
        this.executionTime = executionTime;
        this.executionDays = executionDays;
    }

    @Override
    public Calendar getExecutionTime() {
        return executionTime;
    }

    @Override
    public long getExecutionInterval() {
        return INTERVAL_DAILY;
    }

    public ArrayList<Day> getExecutionDays() {
        return executionDays;
    }

    public boolean containsExecutionDay(int positionInWeek) {
        for (WeekdayTimer.Day day : getExecutionDays()) {
            if (positionInWeek == day.positionInWeek) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Timer: ");
        if (active) {
            stringBuilder.append("(enabled) ");
        } else {
            stringBuilder.append("(disabled) ");
        }
        stringBuilder.append(getName())
                .append("(").append(getId()).append(") Time: ")
                .append(getExecutionTime().getTimeInMillis())
                .append(" Days: ");
        for (Day day : getExecutionDays()) {
            stringBuilder.append(day.positionInWeek).append(" ");
        }
        stringBuilder.append(" {\n");

        for (Action action : getActions()) {
            stringBuilder.append(LogHandler.addIndentation(action.toString())).append("\n");
        }

        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    /**
     * Weekday Enum
     */
    public enum Day {
        MONDAY(Calendar.MONDAY), TUESDAY(Calendar.TUESDAY), WEDNESDAY(Calendar.WEDNESDAY), THURSDAY(Calendar.THURSDAY),
        FRIDAY(Calendar.FRIDAY), SATURDAY(Calendar.SATURDAY), SUNDAY(Calendar.SUNDAY);

        public final int positionInWeek;

        Day(int positionInWeek) {
            this.positionInWeek = positionInWeek;
        }

        public static String getWeekdayName(Context context, Day day) {
            switch (day) {
                case MONDAY:
                    return context.getString(R.string.monday);
                case TUESDAY:
                    return context.getString(R.string.tuesday);
                case WEDNESDAY:
                    return context.getString(R.string.wednesday);
                case THURSDAY:
                    return context.getString(R.string.thursday);
                case FRIDAY:
                    return context.getString(R.string.friday);
                case SATURDAY:
                    return context.getString(R.string.saturday);
                case SUNDAY:
                    return context.getString(R.string.sunday);
                default:
                    return "";
            }
        }
    }
}
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

package eu.power_switch.database.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import eu.power_switch.action.Action;
import eu.power_switch.database.table.timer.TimerTable;
import eu.power_switch.database.table.timer.TimerWeekdayTable;
import eu.power_switch.timer.IntervalTimer;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;
import eu.power_switch.timer.alarm.AlarmHandler;

/**
 * Provides database methods for managing Timers
 */
class TimerHandler {

    private ActionHandler      actionHandler;
    private TimerActionHandler timerActionHandler;
    private AlarmHandler       alarmHandler;

    TimerHandler() {
        actionHandler = new ActionHandler();
        timerActionHandler = new TimerActionHandler();
    }

    /**
     * Adds Timer to Database
     *
     * @param timer Timer
     */
    protected Long add(@NonNull SQLiteDatabase database, Timer timer) throws Exception {
        ContentValues values = new ContentValues();
        values.put(TimerTable.COLUMN_ACTIVE, timer.isActive());
        values.put(TimerTable.COLUMN_NAME, timer.getName());
        values.put(TimerTable.COLUMN_EXECUTION_TIME,
                timer.getExecutionTime()
                        .getTimeInMillis());
        values.put(TimerTable.COLUMN_EXECUTION_INTERVAL, timer.getExecutionInterval());
        values.put(TimerTable.COLUMN_EXECUTION_TYPE, timer.getExecutionType());
        values.put(TimerTable.COLUMN_RANDOMIZER_VALUE, timer.getRandomizerValue());

        long timerId = database.insert(TimerTable.TABLE_NAME, null, values);

        if (timerId > -1) {
            if (timer.getExecutionType()
                    .equals(Timer.EXECUTION_TYPE_WEEKDAY)) {
                insertWeekdayDetails(database, (WeekdayTimer) timer, timerId);
            }

            timerActionHandler.add(database, timer.getActions(), timerId);
        } else {
            // throw exception
            throw new RuntimeException();
        }

        // activate alarm
        alarmHandler.createAlarm(get(database, timerId));
        return timerId;
    }

    private void insertWeekdayDetails(@NonNull SQLiteDatabase database, WeekdayTimer weekdayTimer, Long timerId) throws Exception {
        for (WeekdayTimer.Day day : weekdayTimer.getExecutionDays()) {
            ContentValues values = new ContentValues();
            values.put(TimerWeekdayTable.COLUMN_EXECUTION_DAY, day.positionInWeek);
            values.put(TimerWeekdayTable.COLUMN_TIMER_ID, timerId);

            database.insert(TimerWeekdayTable.TABLE_NAME, null, values);
        }
    }

    /**
     * Deletes Timer from Database
     *
     * @param timerId ID of Timer
     */
    protected void delete(@NonNull SQLiteDatabase database, Long timerId) throws Exception {
        AlarmHandler.cancelAlarm(DatabaseHandlerStatic.context, get(database, timerId));

        timerActionHandler.delete(database, timerId);

        deleteWeekdayDetails(database, timerId);

        database.delete(TimerTable.TABLE_NAME, TimerTable.COLUMN_ID + "=" + timerId, null);
    }

    private void deleteWeekdayDetails(@NonNull SQLiteDatabase database, Long timerId) throws Exception {
        database.delete(TimerWeekdayTable.TABLE_NAME, TimerWeekdayTable.COLUMN_TIMER_ID + "=" + timerId, null);
    }

    /**
     * Updates an existing Timer
     *
     * @param timer new Timer Object with same ID as existing one
     */
    protected void update(@NonNull SQLiteDatabase database, Timer timer) throws Exception {
        AlarmHandler.cancelAlarm(DatabaseHandlerStatic.context, get(database, timer.getId()));

        timerActionHandler.update(database, timer);

        deleteWeekdayDetails(database, timer.getId());

        ContentValues values = new ContentValues();
        values.put(TimerTable.COLUMN_ACTIVE, timer.isActive());
        values.put(TimerTable.COLUMN_NAME, timer.getName());
        values.put(TimerTable.COLUMN_EXECUTION_TYPE, timer.getExecutionType());
        values.put(TimerTable.COLUMN_EXECUTION_INTERVAL, timer.getExecutionInterval());
        values.put(TimerTable.COLUMN_EXECUTION_TIME,
                timer.getExecutionTime()
                        .getTimeInMillis());
        values.put(TimerTable.COLUMN_RANDOMIZER_VALUE, timer.getRandomizerValue());

        database.update(TimerTable.TABLE_NAME, values, TimerTable.COLUMN_ID + "=" + timer.getId(), null);

        if (Timer.EXECUTION_TYPE_WEEKDAY.equals(timer.getExecutionType())) {
            insertWeekdayDetails(database, (WeekdayTimer) timer, timer.getId());
        }

        // activate new alarm if timer is active
        if (timer.isActive()) {
            AlarmHandler.createAlarm(DatabaseHandlerStatic.context, timer);
        }
    }

    /**
     * Gets a Timer from Database
     *
     * @param timerId ID of Timer
     *
     * @return Timer
     */
    @NonNull
    protected Timer get(@NonNull SQLiteDatabase database, Long timerId) throws Exception {
        Timer  timer  = null;
        Cursor cursor = database.query(TimerTable.TABLE_NAME, TimerTable.ALL_COLUMNS, TimerTable.COLUMN_ID + "=" + timerId, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.moveToFirst()) {
            timer = dbToTimer(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(String.valueOf(timerId)));
        }

        cursor.close();
        return timer;
    }

    /**
     * Gets all Timers from Database
     *
     * @return List of Timer
     */
    protected List<Timer> getAll(@NonNull SQLiteDatabase database) throws Exception {
        List<Timer> timers = new ArrayList<>();
        Cursor      cursor = database.query(TimerTable.TABLE_NAME, TimerTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            timers.add(dbToTimer(database, cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return timers;
    }

    /**
     * Gets all active/inactive Timers from Database
     *
     * @return List of Timer
     */
    protected List<Timer> getAll(@NonNull SQLiteDatabase database, boolean isActive) throws Exception {
        List<Timer> timers      = new ArrayList<>();
        int         isActiveInt = isActive ? 1 : 0;
        Cursor cursor = database.query(TimerTable.TABLE_NAME,
                TimerTable.ALL_COLUMNS,
                TimerTable.COLUMN_ACTIVE + "=" + isActiveInt,
                null,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            timers.add(dbToTimer(database, cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return timers;
    }

    /**
     * Enables a Timer in Database
     *
     * @param id ID of Timer
     */
    protected void enable(@NonNull SQLiteDatabase database, Long id) throws Exception {
        ContentValues values = new ContentValues();
        values.put(TimerTable.COLUMN_ACTIVE, 1);
        database.update(TimerTable.TABLE_NAME, values, TimerTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Disables a Timer in Database
     *
     * @param id ID of Timer
     */
    protected void disable(@NonNull SQLiteDatabase database, Long id) throws Exception {
        ContentValues values = new ContentValues();
        values.put(TimerTable.COLUMN_ACTIVE, 0);
        database.update(TimerTable.TABLE_NAME, values, TimerTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Creates a Timer Object out of Database information
     *
     * @param c cursor pointing to a Timer database entry
     *
     * @return Timer
     */
    private Timer dbToTimer(@NonNull SQLiteDatabase database, Cursor c) throws Exception {
        Long    timerId   = c.getLong(0);
        int     rawActive = c.getInt(1);
        boolean active;
        active = rawActive > 0;
        String   name             = c.getString(2);
        long     executionTimeRAW = c.getLong(3);
        Calendar executionTime    = Calendar.getInstance();
        executionTime.setTime(new Date(executionTimeRAW));
        int randomizerValue = c.getInt(6);

        long   executionInterval = c.getLong(4);
        String executionType     = c.getString(5);

        ArrayList<Action> actions = timerActionHandler.getByTimerId(database, timerId);

        if (executionType.equals(Timer.EXECUTION_TYPE_WEEKDAY)) {
            ArrayList<WeekdayTimer.Day> weekdays = getWeekdayDetails(database, timerId);

            return new WeekdayTimer(timerId, active, name, executionTime, randomizerValue, weekdays, actions);
        } else if (executionType.equals(Timer.EXECUTION_TYPE_INTERVAL)) {
            return new IntervalTimer(timerId, active, name, executionTime, randomizerValue, executionInterval, actions);
        }

        return null;
    }

    private ArrayList<WeekdayTimer.Day> getWeekdayDetails(@NonNull SQLiteDatabase database, Long timerId) throws Exception {
        return getExecutionDays(database, timerId);
    }

    private ArrayList<WeekdayTimer.Day> getExecutionDays(@NonNull SQLiteDatabase database, Long timerId) throws Exception {
        ArrayList<WeekdayTimer.Day> days = new ArrayList<>();

        String[] columns = {TimerWeekdayTable.COLUMN_EXECUTION_DAY};
        Cursor cursor = database.query(TimerWeekdayTable.TABLE_NAME,
                columns,
                TimerWeekdayTable.COLUMN_TIMER_ID + "=" + timerId,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            switch (cursor.getInt(0)) {
                case Calendar.MONDAY:
                    days.add(WeekdayTimer.Day.MONDAY);
                    break;
                case Calendar.TUESDAY:
                    days.add(WeekdayTimer.Day.TUESDAY);
                    break;
                case Calendar.WEDNESDAY:
                    days.add(WeekdayTimer.Day.WEDNESDAY);
                    break;
                case Calendar.THURSDAY:
                    days.add(WeekdayTimer.Day.THURSDAY);
                    break;
                case Calendar.FRIDAY:
                    days.add(WeekdayTimer.Day.FRIDAY);
                    break;
                case Calendar.SATURDAY:
                    days.add(WeekdayTimer.Day.SATURDAY);
                    break;
                case Calendar.SUNDAY:
                    days.add(WeekdayTimer.Day.SUNDAY);
                    break;
            }
            cursor.moveToNext();
        }

        cursor.close();
        return days;
    }
}
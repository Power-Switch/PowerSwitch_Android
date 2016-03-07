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

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;

import eu.power_switch.action.Action;

/**
 * Timer base class
 * <p/>
 * Created by Markus on 12.09.2015.
 */
public abstract class Timer {

    public static final String EXECUTION_TYPE_WEEKDAY = "weekday_timer";
    public static final String EXECUTION_TYPE_INTERVAL = "interval_timer";

    /**
     * ID of this Timer
     */
    protected long id;

    /**
     * Active state of this Timer
     */
    protected boolean active;

    /**
     * Name of this Timer
     */
    protected String name;

    /**
     * ExecutionType of this Timer
     */
    @ExecutionType
    protected String executionType;

    /**
     * Actions of this Timer
     */
    protected ArrayList<Action> actions;

    /**
     * Constructor
     *
     * @param id            ID of this Timer
     * @param active        "Active" state of this Timer
     * @param name          Name of this Timer
     * @param executionType {@see ExecutionType} of this Timer
     * @param actions       list of actions
     */
    public Timer(long id, boolean active, String name, String executionType, ArrayList<Action> actions) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.executionType = executionType;
        this.actions = actions;
    }

    /**
     * Get ID of this Timer
     *
     * @return ID of this Timer
     */
    public long getId() {
        return id;
    }

    /**
     * Returns if this Timer is active or not
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set "Active" state of this Timer
     *
     * @param active true if active, false otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns name of this Timer
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns execution type of this Timer
     *
     * @return execution type
     */
    @ExecutionType
    public String getExecutionType() {
        return executionType;
    }

    /**
     * Returns all TimerActions associated with this Timer
     *
     * @return List of TimerActions
     */
    public ArrayList<Action> getActions() {
        return actions;
    }

    /**
     * Returns the time when this Timer should execute
     *
     * @return Calender (only Hour/Minute is important)
     */
    public abstract Calendar getExecutionTime();

    /**
     * Returns the interval this Timer should execute regularly
     *
     * @return Interval
     */
    public abstract long getExecutionInterval();


    @Override
    public abstract String toString();

    @StringDef({EXECUTION_TYPE_WEEKDAY, EXECUTION_TYPE_INTERVAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExecutionType {
    }
}
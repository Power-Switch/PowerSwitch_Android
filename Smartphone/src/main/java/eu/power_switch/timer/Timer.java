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

import java.util.ArrayList;
import java.util.Calendar;

import eu.power_switch.timer.action.Action;

/**
 * Timer base class
 * <p/>
 * Created by Markus on 12.09.2015.
 */
public abstract class Timer {

    public static final String EXECUTION_TYPE_WEEKDAY = "weekday_timer";
    public static final String EXECUTION_TYPE_INTERVAL = "interval_timer";

    protected long id;
    protected boolean isActive;
    protected String name;
    protected String executionType;
    protected ArrayList<Action> actions;

    public Timer(long id, boolean isActive, String name, String executionType, ArrayList<Action> actions) {
        this.id = id;
        this.isActive = isActive;
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
     * @return true if active
     */
    public boolean isActive() {
        return isActive;
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
}
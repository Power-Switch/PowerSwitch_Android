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
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import eu.power_switch.action.Action;
import lombok.Data;
import lombok.ToString;

/**
 * Timer base class
 * <p/>
 * Created by Markus on 12.09.2015.
 */
@Data
@ToString
public abstract class Timer {

    public static final String EXECUTION_TYPE_WEEKDAY  = "weekday_timer";
    public static final String EXECUTION_TYPE_INTERVAL = "interval_timer";

    /**
     * ID of this Timer
     */
    protected Long id;

    /**
     * Active state of this Timer
     */
    protected boolean active;

    /**
     * Name of this Timer
     */
    protected String       name;
    /**
     * Randomizer value, determining how much time before/after the actual execution time the actions should be executed
     */
    protected int          randomizerValue;
    /**
     * ExecutionType of this Timer
     */
    @ExecutionType
    protected String       executionType;
    /**
     * Actions of this Timer
     */
    protected List<Action> actions;
    /**
     * Time when this timer should be executed
     */
    private   Calendar     executionTime;


    /**
     * Constructor
     *
     * @param id              ID of this Timer
     * @param active          "Active" state of this Timer
     * @param name            Name of this Timer
     * @param executionTime   time when this timer should be executed
     * @param randomizerValue
     * @param executionType   {@see ExecutionType} of this Timer
     * @param actions         list of actions
     */
    public Timer(long id, boolean active, String name, Calendar executionTime, int randomizerValue, String executionType, List<Action> actions) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.executionTime = executionTime;
        this.randomizerValue = randomizerValue;
        this.executionType = executionType;
        this.actions = actions;
    }

    /**
     * Randomly generate an upcoming execution time based on ExecutionTime and RandomizerValue
     *
     * @return (within boundaries) randomized execution time
     */
    public Calendar getRandomizedExecutionTime() {
        Calendar randomExecutionTime = Calendar.getInstance();
        randomExecutionTime.setTime(getExecutionTime().getTime()); // init with exact time
        randomExecutionTime.add(Calendar.MINUTE, -randomizerValue); // substract highest possible variation (in one direction)
        Random r             = new Random();
        int    randomMinutes = r.nextInt(randomizerValue * 2 + 1); // generate random variation but with double the range (in both directions)
        randomExecutionTime.add(Calendar.MINUTE, randomMinutes); // add randomly generated variation to time
        randomExecutionTime.set(Calendar.SECOND, 0); // seconds and milliseconds can be ignored^
        randomExecutionTime.set(Calendar.MILLISECOND, 0);

        return randomExecutionTime;
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
     * Returns the interval this Timer should execute regularly
     *
     * @return Interval
     */
    public abstract long getExecutionInterval();

    @StringDef({EXECUTION_TYPE_WEEKDAY, EXECUTION_TYPE_INTERVAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExecutionType {
    }
}
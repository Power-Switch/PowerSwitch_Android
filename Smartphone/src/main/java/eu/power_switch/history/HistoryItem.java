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

package eu.power_switch.history;

import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * This Class represents a history element used in history drawer
 * <p/>
 * Created by Markus on 08.12.2015.
 */
public class HistoryItem {

    /**
     * ID of this HistoryItem
     */
    private Long id;

    /**
     * Date/Time of this HistoryItem
     */
    private Calendar time;

    /**
     * Short description of this HistoryItem
     */
    private String shortDescription;

    /**
     * Long description of this HistoryItem
     */
    private String longDescription;

    public HistoryItem(long id, @NonNull Long timeInMilliseconds, @NonNull String shortDescription) {
        this.id = id;
        this.time = Calendar.getInstance();
        this.time.setTimeInMillis(timeInMilliseconds);
        this.shortDescription = shortDescription;
        this.longDescription = "";
    }

    public HistoryItem(long id, @NonNull Long timeInMilliseconds, @NonNull String shortDescription, @NonNull String longDescription) {
        this.id = id;
        this.time = Calendar.getInstance();
        this.time.setTimeInMillis(timeInMilliseconds);
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }

    public HistoryItem(long id, @NonNull Calendar time, @NonNull String shortDescription) {
        this.id = id;
        this.time = time;
        this.shortDescription = shortDescription;
        this.longDescription = "";
    }

    public HistoryItem(long id, @NonNull Calendar time, @NonNull String shortDescription, @NonNull String longDescription) {
        this.id = id;
        this.time = time;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }

    /**
     * Get ID of this HistoryItem
     *
     * @return Id of this HistoryItem
     */
    @NonNull
    public Long getId() {
        return id;
    }

    /**
     * Get shortDescription of this HistoryItem
     *
     * @return shortDescription of this HistoryItem
     */
    @NonNull
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Get longDescription of this HistoryItem
     *
     * @return longDescription of this HistoryItem
     */
    @NonNull
    public String getLongDescription() {
        return longDescription;
    }

    /**
     * Get Date/Time of this HistoryItem
     *
     * @return Date/Time of this HistoryItem
     */
    @NonNull
    public Calendar getTime() {
        return time;
    }
}

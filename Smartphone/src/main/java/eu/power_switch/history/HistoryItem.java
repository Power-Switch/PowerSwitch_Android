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

import java.util.Calendar;

/**
 * This Class represents a history element used in history drawer
 * <p/>
 * Created by Markus on 08.12.2015.
 */
public class HistoryItem {

    private Long id;
    private Calendar time;
    private String description;

    public HistoryItem(Long id, Long timeInMilliseconds, String description) {
        this.id = id;
        this.time = Calendar.getInstance();
        this.time.setTimeInMillis(timeInMilliseconds);
        this.description = description;
    }

    public HistoryItem(Long id, Calendar time, String description) {
        this.id = id;
        this.time = time;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Calendar getTime() {
        return time;
    }
}

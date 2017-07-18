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

package eu.power_switch.widget;

import lombok.Getter;
import lombok.ToString;

/**
 * Container Object for storing information about a Room Widget
 */
@Getter
@ToString
public class RoomWidget {

    /**
     * App ID of this Widget
     */
    private int widgetId;

    /**
     * Room ID this Widget belongs to
     */
    private long roomId;

    /**
     * Constructor
     *
     * @param widgetId ID of this Widget
     * @param roomId   ID of associated Room
     */
    public RoomWidget(int widgetId, long roomId) {
        this.widgetId = widgetId;
        this.roomId = roomId;
    }

}

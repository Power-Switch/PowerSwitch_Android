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

package eu.power_switch.widget;

/**
 * Container Object for storing information about a Scene Widget
 */
public class SceneWidget {

    /**
     * App ID of this Widget
     */
    private int widgetId;

    /**
     * Scene ID this Widget belongs to
     */
    private long sceneId;

    /**
     * Constructor
     *
     * @param widgetId ID of this Widget
     * @param sceneId  ID of associated Scene
     */
    public SceneWidget(int widgetId, long sceneId) {
        this.widgetId = widgetId;
        this.sceneId = sceneId;
    }

    /**
     * Get ID of this Widget
     *
     * @return ID of this Widget
     */
    public int getWidgetId() {
        return widgetId;
    }

    /**
     * Get ID of Scene this Widget is associated with
     *
     * @return ID of associated Scene
     */
    public long getSceneId() {
        return sceneId;
    }

}

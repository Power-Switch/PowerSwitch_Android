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

package eu.power_switch.action;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Action Base Class
 * A Timer can contains a list of Actions
 * <p/>
 * Created by Markus on 24.09.2015.
 */
public abstract class Action {

    public static final String ACTION_TYPE_RECEIVER = "action_type_receiver";
    public static final String ACTION_TYPE_ROOM = "action_type_room";
    public static final String ACTION_TYPE_SCENE = "action_type_scene";

    protected long id;

    /**
     * Get ID of this Action
     *
     * @return ID
     */
    public long getId() {
        return id;
    }

    /**
     * Get ActionType of this Action
     *
     * @return ActionType
     */
    public abstract
    @NonNull
    String getActionType();

    /**
     * Returns a human readable representation of a Action
     *
     * @return Text
     */
    public abstract
    @NonNull
    String toString();

    /**
     * Executes this Action
     *
     * @param context any suitable context
     */
    public abstract void execute(@NonNull Context context);
}

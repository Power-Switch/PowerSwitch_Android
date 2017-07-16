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

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.persistence.PersistenceHandler;
import lombok.Getter;

/**
 * Action Base Class
 * A Timer can contains a list of Actions
 * <p/>
 * Created by Markus on 24.09.2015.
 */
public abstract class Action {

    public static final String ACTION_TYPE_RECEIVER = "action_type_receiver";
    public static final String ACTION_TYPE_ROOM     = "action_type_room";
    public static final String ACTION_TYPE_SCENE    = "action_type_scene";
    public static final String ACTION_TYPE_PAUSE    = "action_type_pause";
    /**
     * ID of this Action
     */
    @Getter
    protected long id;

    /**
     * Get ActionType of this Action
     *
     * @return ActionType
     */
    @ActionType
    @NonNull
    public abstract String getActionType();

    /**
     * Create a human readable text for an action
     *
     * @param action
     * @param persistenceHandler
     *
     * @return
     */
    public static String createReadableString(Action action, PersistenceHandler persistenceHandler) {
        // TODO: this is a huge performance monster :(
        try {
            StringBuilder s = new StringBuilder();
            if (action instanceof ReceiverAction) {
                ReceiverAction receiverAction = (ReceiverAction) action;
                Apartment      apartment      = persistenceHandler.getApartment(receiverAction.getApartmentId());
                Room           room           = apartment.getRoom(receiverAction.getRoomId());
                Receiver       receiver       = room.getReceiver(receiverAction.getReceiverId());
                Button         button         = receiver.getButton(receiverAction.getButtonId());


                s.append(apartment.getName())
                        .append(": ")
                        .append(room.getName())
                        .append(": ")
                        .append(receiver.getName())
                        .append(": ")
                        .append(button.getName());
            } else if (action instanceof RoomAction) {
                RoomAction roomAction = (RoomAction) action;
                Apartment  apartment  = persistenceHandler.getApartment(roomAction.getApartmentId());
                Room       room       = apartment.getRoom(roomAction.getRoomId());

                s.append(apartment.getName())
                        .append(": ")
                        .append(room.getName())
                        .append(": ")
                        .append(roomAction.getButtonName());
            } else if (action instanceof SceneAction) {
                SceneAction sceneAction = (SceneAction) action;
                Apartment   apartment   = persistenceHandler.getApartment(sceneAction.getApartmentId());
                Scene       scene       = apartment.getScene(sceneAction.getSceneId());

                s.append(apartment.getName())
                        .append(": ")
                        .append(scene.getName());
            } else {
                s.append("Unknown");
            }

            return s.toString();
        } catch (Exception e) {
            return "error";
        }
    }

    @StringDef({ACTION_TYPE_RECEIVER, ACTION_TYPE_ROOM, ACTION_TYPE_SCENE, ACTION_TYPE_PAUSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

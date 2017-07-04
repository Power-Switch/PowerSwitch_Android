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

import eu.power_switch.obj.Room;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.action.Action;

/**
 * ReceiverAction that holds a specific room/receiver/button combination to activate on execution
 * <p/>
 * Created by Markus on 24.09.2015.
 */
public class ReceiverAction extends Action {

    //    private Apartment apartment;
    private String apartmentName;
    private Room room;
    private Receiver receiver;
    private Button button;

    public ReceiverAction(long id, @NonNull String apartmentName, @NonNull Room room, @NonNull Receiver receiver, @NonNull Button button) {
        this.id = id;
//        this.apartment = apartment;
        this.apartmentName = apartmentName;
        this.room = room;
        this.receiver = receiver;
        this.button = button;
    }

    @NonNull
    public Room getRoom() {
        return room;
    }

    @NonNull
    public Receiver getReceiver() {
        return receiver;
    }

    @NonNull
    public Button getButton() {
        return button;
    }

    @Override
    @ActionType
    @NonNull
    public String getActionType() {
        return ACTION_TYPE_RECEIVER;
    }

    @Override
    @NonNull
    public String toString() {
        return apartmentName + ": " +
                room.getName() + ": " +
                receiver.getName() + ": " +
                button.getName();
    }

    @Override
    public void execute(@NonNull Context context) {
        ActionHandler.execute(context, receiver, button);
    }
}

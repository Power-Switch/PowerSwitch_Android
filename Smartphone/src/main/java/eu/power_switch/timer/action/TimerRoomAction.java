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

package eu.power_switch.timer.action;

import android.app.PendingIntent;
import android.content.Context;

import eu.power_switch.api.IntentReceiver;
import eu.power_switch.obj.Room;
import eu.power_switch.shared.log.Log;

/**
 * TimerRoomAction that holds a specific room/button combination to activate on execution
 * <p/>
 * Created by Markus on 24.09.2015.
 */
public class TimerRoomAction extends TimerAction {

    private Room room;
    private String buttonName;

    public TimerRoomAction(long id, Room room, String buttonName) {
        this.id = id;
        this.room = room;
        this.buttonName = buttonName;
    }

    public Room getRoom() {
        return room;
    }

    public String getButtonName() {
        return buttonName;
    }

    @Override
    public String getActionType() {
        return ACTION_TYPE_ROOM;
    }

    @Override
    public String toString() {
        return room.getName() + ": " + buttonName;
    }

    @Override
    public void execute(Context context) {
        try {
            PendingIntent pendingIntent = IntentReceiver.buildRoomButtonPendingIntent(context, room.getName(), buttonName, 0);

            if (pendingIntent != null) {
                pendingIntent.send();
                Log.d("Pending intent send");
            }
        } catch (PendingIntent.CanceledException e) {
            Log.e("Pending intent canceled", e);
        }
    }
}

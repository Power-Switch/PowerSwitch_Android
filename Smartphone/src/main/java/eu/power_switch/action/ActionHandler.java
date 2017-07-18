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

package eu.power_switch.action;

import android.support.annotation.NonNull;

import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.AlarmClockConstants;
import eu.power_switch.shared.constants.PhoneConstants;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import eu.power_switch.timer.Timer;

/**
 * Created by Markus on 13.07.2017.
 */

public interface ActionHandler {
    void execute(@NonNull Receiver receiver, @NonNull Button button);

    void execute(@NonNull Room room, @NonNull String buttonName);

    void execute(@NonNull Room room, long buttonId);

    void execute(@NonNull Scene scene);

    void execute(@NonNull Timer timer);

    void execute(@NonNull SleepAsAndroidConstants.Event event);

    void execute(@NonNull AlarmClockConstants.Event event);

    void execute(@NonNull Geofence geofence, @NonNull Geofence.EventType eventType);

    void execute(CallEvent callEvent, @NonNull PhoneConstants.CallType callType);
}

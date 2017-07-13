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

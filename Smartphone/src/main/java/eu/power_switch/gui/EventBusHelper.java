package eu.power_switch.gui;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

import timber.log.Timber;

/**
 * Created by Markus on 02.07.2017.
 */

public class EventBusHelper {

    /**
     * Try to register a subscriber to the default EventBus.
     * No error will be thrown if there are no public methods annotated with @Subscriber.
     *
     * @param subscriber subscriber for events
     */
    public static void tryRegister(Object subscriber) {
        try {
            EventBus.getDefault()
                    .register(subscriber);
        } catch (EventBusException e) {
            if (e.getMessage() != null && e.getMessage()
                    .contains("no public methods")) {
                Timber.i("Couldn't register EventBus subscriber because of missing public methods annotated with @Subscriber");
                // ignore
            } else {
                throw e;
            }
        }
    }

    /**
     * Try to register a subscriber to the default EventBus.
     * No error will be thrown if there are no public methods annotated with @Subscriber.
     *
     * @param subscriber subscriber for events
     */
    public static void tryUnregister(Object subscriber) {
        EventBus.getDefault()
                .unregister(subscriber);
    }

}

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

package eu.power_switch.shared.event;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import timber.log.Timber;

/**
 * Created by Markus on 02.07.2017.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventBusHelper {

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

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

package eu.power_switch.google_play_services.geofence;

import android.content.Intent;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.DaggerIntentService;
import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.gui.fragment.geofences.GeofencesTabFragment;
import eu.power_switch.persistence.PersistanceHandler;
import timber.log.Timber;

/**
 * Intent service used to react to geofence enter/exit events
 * <p/>
 * Created by Markus on 21.12.2015.
 */
public class GeofenceIntentService extends DaggerIntentService {

    @Inject
    ActionHandler actionHandler;

    @Inject
    PersistanceHandler persistanceHandler;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceIntentService() {
        super("GeofenceIntentService");
    }

    /**
     * Handles incoming intents.
     *
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Timber.e("GeofencingError %d", geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the Ids of each geofence that was triggered.
            ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
            for (Geofence geofence : triggeringGeofences) {
                triggeringGeofencesIdsList.add(geofence.getRequestId());
            }
            Timber.d(getTransitionString(geofenceTransition) + ": " + TextUtils.join(", ", triggeringGeofencesIdsList));

            executeGeofences(triggeringGeofences, geofenceTransition);
        } else {
            // Log4JLog the error.
            Timber.e("Unknown Geofence transition: " + geofenceTransition);
        }
    }

    /**
     * Execute Geofence actions
     *
     * @param triggeringGeofences list of triggered Geofences
     * @param geofenceTransition  type of transition
     */
    private void executeGeofences(List<Geofence> triggeringGeofences, int geofenceTransition) {
        eu.power_switch.google_play_services.geofence.Geofence.EventType eventType = null;
        switch (geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                eventType = eu.power_switch.google_play_services.geofence.Geofence.EventType.ENTER;
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                eventType = eu.power_switch.google_play_services.geofence.Geofence.EventType.EXIT;
                break;
        }

        for (Geofence googleGeofence : triggeringGeofences) {
            try {
                Long geofenceId = Long.valueOf(googleGeofence.getRequestId());

                eu.power_switch.google_play_services.geofence.Geofence geofence = persistanceHandler.getGeofence(geofenceId);
                if (geofence.isActive() && geofenceStateChanged(geofence.getState(), eventType)) {
                    actionHandler.execute(geofence, eventType);

                    switch (eventType) {
                        case ENTER:
                            persistanceHandler.updateState(geofenceId, eu.power_switch.google_play_services.geofence.Geofence.STATE_INSIDE);
                            break;
                        case EXIT:
                            persistanceHandler.updateState(geofenceId, eu.power_switch.google_play_services.geofence.Geofence.STATE_OUTSIDE);
                            break;
                        default:
                            persistanceHandler.updateState(geofenceId, eu.power_switch.google_play_services.geofence.Geofence.STATE_NONE);
                            break;
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        GeofencesTabFragment.notifyGeofencesChanged();
    }

    private boolean geofenceStateChanged(@eu.power_switch.google_play_services.geofence.Geofence.State String state,
                                         eu.power_switch.google_play_services.geofence.Geofence.EventType eventType) {
        switch (eventType) {
            case ENTER:
                if (!eu.power_switch.google_play_services.geofence.Geofence.STATE_INSIDE.equals(state)) {
                    return true;
                } else {
                    return false;
                }
            case EXIT:
                if (!eu.power_switch.google_play_services.geofence.Geofence.STATE_OUTSIDE.equals(state)) {
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     *
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "dwell";
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.enter);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.exit);
            default:
                return "Unknown Transition";
        }
    }
}

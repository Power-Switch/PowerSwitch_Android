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

package eu.power_switch.gui.dialog.configuration;

import android.os.Bundle;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.holder.CallConfigurationHolder;
import eu.power_switch.gui.fragment.configure_call_event.ConfigureCallEventDialogPage1Contacts;
import eu.power_switch.gui.fragment.configure_call_event.ConfigureCallEventDialogPage2Actions;
import eu.power_switch.gui.fragment.configure_call_event.ConfigureCallEventDialogPage3Summary;
import eu.power_switch.gui.fragment.phone.CallEventsFragment;
import timber.log.Timber;

/**
 * Dialog to create or modify a Call Event
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class ConfigureCallEventDialog extends ConfigurationDialog<CallConfigurationHolder> {

    /**
     * ID of existing Call Event to Edit
     */
    public static final String CALL_EVENT_ID_KEY = "CallEventId";

    private long callEventId = -1;

    public static ConfigureCallEventDialog newInstance(long callEventId) {
        Bundle args = new Bundle();
        args.putLong(CALL_EVENT_ID_KEY, callEventId);

        ConfigureCallEventDialog fragment = new ConfigureCallEventDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) throws Exception {
        if (arguments != null && arguments.containsKey(CALL_EVENT_ID_KEY)) {
            // init dialog using existing scene
            callEventId = arguments.getLong(CALL_EVENT_ID_KEY);
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_call_event;
    }

    @Override
    protected void addPageEntries(List<PageEntry<CallConfigurationHolder>> pageEntries) {
        pageEntries.add(new PageEntry<>(R.string.contacts, ConfigureCallEventDialogPage1Contacts.class));
        pageEntries.add(new PageEntry<>(R.string.actions, ConfigureCallEventDialogPage2Actions.class));
        pageEntries.add(new PageEntry<>(R.string.summary, ConfigureCallEventDialogPage3Summary.class));
    }

    @Override
    protected void saveConfiguration() throws Exception {
        Timber.d("Saving call event...");

        // TODO:

//        if (callEventId == -1) {
//             create new call event
//            Map<PhoneConstants.CallType, Set<String>> phoneNumbersMap = new HashMap<>();
//            phoneNumbersMap.put(PhoneConstants.CallType.INCOMING, new HashSet<>(currentPhoneNumbers));

//            Map<PhoneConstants.CallType, List<Action>> actionsMap = new HashMap<>();
//            actionsMap.put(PhoneConstants.CallType.INCOMING, currentActions);
//
//            CallEvent newCallEvent = new CallEvent(-1, true, "", phoneNumbersMap, actionsMap);
//            DatabaseHandler.addCallEvent(newCallEvent);
//        } else {
        // modify existing call event
//            CallEvent callEvent = DatabaseHandler.getCallEvent(callEventId);
//
//            callEvent.setPhoneNumbers(PhoneConstants.CallType.INCOMING, new HashSet<>(currentPhoneNumbers));
//            callEvent.setActions(PhoneConstants.CallType.INCOMING, currentActions);
//
//            DatabaseHandler.updateCallEvent(callEvent);
//        }

//        CallEventsFragment.notifyCallEventsChanged();
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        persistenceHandler.deleteCallEvent(callEventId);

        // notify scenes fragment
        CallEventsFragment.notifyCallEventsChanged();
    }

}

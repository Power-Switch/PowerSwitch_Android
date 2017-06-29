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

package eu.power_switch.gui.fragment.configure_call_event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;
import eu.power_switch.gui.dialog.ConfigureCallEventDialog;
import eu.power_switch.gui.fragment.phone.CallEventsFragment;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PhoneConstants;

/**
 * Created by Markus on 05.04.2016.
 */
public class ConfigureCallEventDialogPage3SummaryFragment extends ConfigurationDialogFragment implements ConfigurationDialogTabbedSummaryFragment {

    @BindView(R.id.textView_contacts)
    TextView textViewContacts;
    @BindView(R.id.textView_actions)
    TextView textViewActions;
    private long              callEventId         = -1;
    private ArrayList<String> currentPhoneNumbers = new ArrayList<>();
    private ArrayList<Action> currentActions      = new ArrayList<>();

    private BroadcastReceiver broadcastReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocalBroadcastConstants.INTENT_CALL_EVENT_PHONE_NUMBERS_CHANGED.equals(intent.getAction())) {
                    currentPhoneNumbers = intent.getStringArrayListExtra(ConfigureCallEventDialogPage1ContactsFragment.KEY_PHONE_NUMBERS);
                } else if (LocalBroadcastConstants.INTENT_CALL_EVENT_ACTIONS_CHANGED.equals(intent.getAction())) {
                    currentActions = (ArrayList<Action>) intent.getSerializableExtra(ConfigureCallEventDialogPage2ActionsFragment.KEY_ACTIONS);
                }

                updateUi();

                notifyConfigurationChanged();
            }
        };

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureCallEventDialog.CALL_EVENT_ID_KEY)) {
            callEventId = args.getLong(ConfigureCallEventDialog.CALL_EVENT_ID_KEY);
            initializeCallData(callEventId);
        }

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_call_event_page_3_summary;
    }

    private void initializeCallData(long callEventId) {
        try {
            CallEvent callEvent = DatabaseHandler.getCallEvent(callEventId);

            currentPhoneNumbers.addAll(callEvent.getPhoneNumbers(PhoneConstants.CallType.INCOMING));
            currentActions.addAll(callEvent.getActions(PhoneConstants.CallType.INCOMING));

            updateUi();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    private void updateUi() {
        String phoneNumbers = "";

        Iterator<String> iterator = currentPhoneNumbers.iterator();
        while (iterator.hasNext()) {
            String phoneNumber = iterator.next();

            if (iterator.hasNext()) {
                phoneNumbers += phoneNumber + "\n";
            } else {
                phoneNumbers += phoneNumber;
            }
        }
        textViewContacts.setText(phoneNumbers);

        String actions = "";

        Iterator<Action> iterator1 = currentActions.iterator();
        while (iterator1.hasNext()) {
            Action action = iterator1.next();

            if (iterator1.hasNext()) {
                actions += action.toString() + "\n";
            } else {
                actions += action.toString();
            }
        }

        textViewActions.setText(actions);
    }

    @Override
    public boolean checkSetupValidity() throws Exception {

        if (currentPhoneNumbers == null || currentPhoneNumbers.isEmpty()) {
            return false;
        }

        return !(currentActions == null || currentActions.isEmpty());

    }

    @Override
    public void saveCurrentConfigurationToDatabase() throws Exception {
        if (callEventId == -1) {
            // create new call event
            Map<PhoneConstants.CallType, Set<String>> phoneNumbersMap = new HashMap<>();
            phoneNumbersMap.put(PhoneConstants.CallType.INCOMING, new HashSet<>(currentPhoneNumbers));

            Map<PhoneConstants.CallType, List<Action>> actionsMap = new HashMap<>();
            actionsMap.put(PhoneConstants.CallType.INCOMING, currentActions);

            CallEvent newCallEvent = new CallEvent(-1, true, "", phoneNumbersMap, actionsMap);
            DatabaseHandler.addCallEvent(newCallEvent);
        } else {
            // modify existing call event
            CallEvent callEvent = DatabaseHandler.getCallEvent(callEventId);

            callEvent.setPhoneNumbers(PhoneConstants.CallType.INCOMING, new HashSet<>(currentPhoneNumbers));
            callEvent.setActions(PhoneConstants.CallType.INCOMING, currentActions);

            DatabaseHandler.updateCallEvent(callEvent);
        }

        CallEventsFragment.sendCallEventsChangedBroadcast(getContext());
        StatusMessageHandler.showInfoMessage(getTargetFragment(), R.string.call_event_saved, Snackbar.LENGTH_LONG);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_CALL_EVENT_PHONE_NUMBERS_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_CALL_EVENT_ACTIONS_CHANGED);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

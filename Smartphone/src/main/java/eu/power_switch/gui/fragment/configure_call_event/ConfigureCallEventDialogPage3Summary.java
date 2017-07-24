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

package eu.power_switch.gui.fragment.configure_call_event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.event.ConfigurationChangedEvent;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.ConfigureCallEventDialog;
import eu.power_switch.gui.dialog.configuration.holder.CallConfigurationHolder;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PhoneConstants;

/**
 * Created by Markus on 05.04.2016.
 */
public class ConfigureCallEventDialogPage3Summary extends ConfigurationDialogPage<CallConfigurationHolder> {

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
                    currentPhoneNumbers = intent.getStringArrayListExtra(ConfigureCallEventDialogPage1Contacts.KEY_PHONE_NUMBERS);
                } else if (LocalBroadcastConstants.INTENT_CALL_EVENT_ACTIONS_CHANGED.equals(intent.getAction())) {
                    currentActions = (ArrayList<Action>) intent.getSerializableExtra("actions");
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
            CallEvent callEvent = persistenceHandler.getCallEvent(callEventId);

            currentPhoneNumbers.addAll(callEvent.getPhoneNumbers(PhoneConstants.CallType.INCOMING));
            currentActions.addAll(callEvent.getActions(PhoneConstants.CallType.INCOMING));

            updateUi();
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onConfigurationChanged(ConfigurationChangedEvent e) {
        updateUi();
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

            actions += Action.createReadableString(getContext(), action, persistenceHandler);

            if (iterator1.hasNext()) {
                actions += "\n";
            }
        }

        textViewActions.setText(actions);
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

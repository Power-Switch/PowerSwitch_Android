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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.PhoneNumberRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddPhoneNumberDialog;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.ConfigureCallEventDialog;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PhoneConstants;
import eu.power_switch.shared.event.PhoneNumberAddedEvent;

/**
 * Created by Markus on 05.04.2016.
 */
public class ConfigureCallEventDialogPage1Contacts extends ConfigurationDialogPage {

    public static final String KEY_PHONE_NUMBERS = "phone_numbers";

    @BindView(R.id.recyclerView_phoneNumbers)
    RecyclerView         recyclerViewContacts;
    @BindView(R.id.add_contact_fab)
    FloatingActionButton addContactFAB;

    private long callEventId = -1;

    private ArrayList<String> phoneNumbers = new ArrayList<>();
    private PhoneNumberRecyclerViewAdapter phoneNumberRecyclerViewAdapter;

    /**
     * Used to notify the summary page that some info has changed
     *
     * @param context      any suitable context
     * @param phoneNumbers list of phone numbers
     */
    public static void sendPhoneNumbersChangedBroadcast(Context context, ArrayList<String> phoneNumbers) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_CALL_EVENT_PHONE_NUMBERS_CHANGED);
        intent.putExtra(KEY_PHONE_NUMBERS, phoneNumbers);

        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        phoneNumberRecyclerViewAdapter = new PhoneNumberRecyclerViewAdapter(getActivity(), phoneNumbers);
        recyclerViewContacts.setAdapter(phoneNumberRecyclerViewAdapter);
        phoneNumberRecyclerViewAdapter.setOnDeleteClickListener(new PhoneNumberRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, final int position) {
                new AlertDialog.Builder(getContext()).setTitle(R.string.delete)
                        .setMessage(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    phoneNumbers.remove(position);
                                    phoneNumberRecyclerViewAdapter.notifyDataSetChanged();

                                    sendPhoneNumbersChangedBroadcast(getContext(), phoneNumbers);
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getContentView(), e);
                                }
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();
            }
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewContacts.setLayoutManager(layoutManager);

        addContactFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        final Fragment fragment = this;
        addContactFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPhoneNumberDialog addPhoneNumberDialog = AddPhoneNumberDialog.newInstance(phoneNumbers);
                addPhoneNumberDialog.setTargetFragment(fragment, 0);
                addPhoneNumberDialog.show(getFragmentManager(), null);
            }
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureCallEventDialog.CALL_EVENT_ID_KEY)) {
            callEventId = args.getLong(ConfigureCallEventDialog.CALL_EVENT_ID_KEY);
            initializeCallData(callEventId);
        }

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_call_event_page_1;
    }

    private void initializeCallData(long callEventId) {
        try {
            CallEvent callEvent = DatabaseHandler.getCallEvent(callEventId);

            phoneNumbers.addAll(callEvent.getPhoneNumbers(PhoneConstants.CallType.INCOMING));
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getContentView(), e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onPhoneNumberAdded(PhoneNumberAddedEvent phoneNumberAddedEvent) {
        Set<String> newPhoneNumbers = phoneNumberAddedEvent.getPhoneNumbers();

        for (String number : newPhoneNumbers) {
            if (!phoneNumbers.contains(number)) {
                phoneNumbers.add(number);
            }
        }

        phoneNumberRecyclerViewAdapter.notifyDataSetChanged();
        sendPhoneNumbersChangedBroadcast(getContext(), phoneNumbers);
    }

}

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

package eu.power_switch.gui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.power_switch.R;
import eu.power_switch.gui.dialog.AddAlarmEventActionDialog;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * Fragment containing all settings related to clock alarm handling from supported alarm clock applications like:
 * Sleep As Android
 * <p/>
 * Created by Markus on 08.10.2015.
 */
public class SleepAsAndroidFragment extends Fragment {

    private View rootView;

    private BroadcastReceiver broadcastReceiver;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_sleep_as_android, container, false);

        try {

        } catch (Exception e) {

//        Room markus = DatabaseHandler.getRoom("Markus");

//        ArrayList<Action> actions = new ArrayList<>();
//        ReceiverAction receiverAction = new ReceiverAction(0, markus, markus.getReceiver("Schrank"),
//                markus.getReceiver("Schrank").getButton("On"));
//        actions.add(receiverAction);

//        DatabaseHandler.setAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT.ALARM_DISMISSED, actions);
//
//        DatabaseHandler.getAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT.ALARM_DISMISSED);
        }


        FloatingActionButton addFAB = (FloatingActionButton) rootView.findViewById(R.id.add_action_fab);
        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddAlarmEventActionDialog addAlarmEventActionDialog = new AddAlarmEventActionDialog();
//                addAlarmEventActionDialog.setTargetFragment(fragment, 0);
                addAlarmEventActionDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());

                if (LocalBroadcastConstants.INTENT_ALARM_EVENT_ACTION_ADDED.equals(intent.getAction())) {
                    updateUI();
                }
            }
        };

        return rootView;
    }

    private void updateUI() {


    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_ALARM_EVENT_ACTION_ADDED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

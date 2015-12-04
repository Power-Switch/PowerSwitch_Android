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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.obj.Room;
import eu.power_switch.shared.constants.ExternalAppConstants;
import eu.power_switch.timer.action.Action;
import eu.power_switch.timer.action.ReceiverAction;

/**
 * Fragment containing all settings related to clock alarm handling from supported alarm clock applications like:
 * Sleep As Android
 * <p/>
 * Created by Markus on 08.10.2015.
 */
public class SleepAsAndroidFragment extends Fragment {

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_sleep_as_android, container, false);

        Room markus = DatabaseHandler.getRoom("Markus");

        ArrayList<Action> actions = new ArrayList<>();
        ReceiverAction receiverAction = new ReceiverAction(0, markus, markus.getReceiver("Schrank"),
                markus.getReceiver("Schrank").getButton("On"));
        actions.add(receiverAction);

        DatabaseHandler.setAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT.ALARM_DISMISSED, actions);

        DatabaseHandler.getAlarmActions(ExternalAppConstants.SLEEP_AS_ANDROID_ALARM_EVENT.ALARM_DISMISSED);

        return rootView;
    }

    private void updateUI() {


    }
}

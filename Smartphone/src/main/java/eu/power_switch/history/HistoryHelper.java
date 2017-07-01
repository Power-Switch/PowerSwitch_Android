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

package eu.power_switch.history;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log4JLog;
import timber.log.Timber;

/**
 * Created by Markus on 20.03.2016.
 */
public class HistoryHelper {

    /**
     * Used to notify listening Fragments that History has changed
     *
     * @param context any suitable context
     */
    public static void sendHistoryChangedBroadcast(Context context) {
        Timber.d("sendHistoryChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_HISTORY_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void add(Context context, HistoryItem historyItem) throws Exception {
        DatabaseHandler.addHistoryItem(historyItem);
        sendHistoryChangedBroadcast(context);
    }

    public static void add(Context context, Exception e) throws Exception {
        DatabaseHandler.addHistoryItem(new HistoryItem((long) -1,
                Calendar.getInstance(),
                context.getString(R.string.unknown_error),
                Log4JLog.getStackTraceText(e)));
        sendHistoryChangedBroadcast(context);
    }
}

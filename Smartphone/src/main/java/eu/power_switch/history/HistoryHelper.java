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

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.event.HistoryUpdatedEvent;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.shared.log.LogHelper;
import timber.log.Timber;

/**
 * Created by Markus on 20.03.2016.
 */
public class HistoryHelper {

    /**
     * Used to notify listening Fragments that History has changed
     */
    public static void notifyHistoryChanged() {
        Timber.d("notifyHistoryChanged");
        EventBus.getDefault()
                .post(new HistoryUpdatedEvent());
    }

    public static void add(PersistenceHandler persistenceHandler, HistoryItem historyItem) throws Exception {
        persistenceHandler.addHistoryItem(historyItem);
        notifyHistoryChanged();
    }

    public static void add(Context context, PersistenceHandler persistenceHandler, Exception e) throws Exception {
        persistenceHandler.addHistoryItem(new HistoryItem((long) -1,
                Calendar.getInstance(),
                context.getString(R.string.unknown_error),
                LogHelper.getStackTraceText(e)));
        notifyHistoryChanged();
    }
}

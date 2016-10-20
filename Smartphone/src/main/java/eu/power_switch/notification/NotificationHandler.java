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

package eu.power_switch.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import eu.power_switch.R;

/**
 * Class used to manage any kind of system notifications
 * <p>
 * Created by Markus on 20.10.2016.
 */

public class NotificationHandler {

    private static int LAST_ID = 0;

    /**
     * Create a new notification
     *
     * @param context any suitable context
     * @param title   tile text
     * @param message message text
     * @return ID of notification
     */
    public static int createNotification(@NonNull Context context, @StringRes int title, @StringRes int message) {
        return createNotification(context,
                R.drawable.ic_launcher,
                context.getString(title),
                context.getString(message),
                false,
                NotificationCompat.PRIORITY_DEFAULT,
                null);
    }

    /**
     * Create a new notification
     *
     * @param context any suitable context
     * @param title   tile text
     * @param message message text
     * @return ID of notification
     */
    public static int createNotification(@NonNull Context context, @Nullable String title, @Nullable String message) {
        return createNotification(context, R.drawable.ic_launcher, title, message, false, NotificationCompat.PRIORITY_DEFAULT, null);
    }

    /**
     * Create a new notification
     *
     * @param context   any suitable context
     * @param smallIcon small icon drawable ressource
     * @param title     tile text
     * @param message   message text
     * @param ongoing   ongoing notification
     * @return ID of notification
     */
    public static int createNotification(@NonNull Context context, @DrawableRes int smallIcon, @Nullable String title,
                                         @Nullable String message, boolean ongoing, int priority,
                                         @Nullable List<NotificationCompat.Action> actions) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int id = ++LAST_ID;

        // construct notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(smallIcon)
                .setOngoing(ongoing)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority);
        if (actions != null) {
            for (NotificationCompat.Action action : actions) {
                mBuilder.addAction(action);
            }
        }

        // show notification
        notificationManager.notify(id, mBuilder.build());

        return id;
    }

    /**
     * Update an existing notification
     *
     * @param context any suitable context
     * @param id      ID of notification
     */
    public static void updateNotification(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    /**
     * Dismiss a notification
     *
     * @param context any suitable context
     * @param id      ID of notification
     */
    public static void dismissNotification(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

}

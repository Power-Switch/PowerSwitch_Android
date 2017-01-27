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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.activity.MainActivity;

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
		return createNotification(context, context.getString(title), context.getString(message));
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
		return createNotification(context,
				null,
				R.drawable.icon_notification,
				title,
				message,
				false,
				NotificationCompat.PRIORITY_DEFAULT,
				null,
				null);
	}

	/**
	 * Create a new notification
	 *
	 * @param context   any suitable context
	 * @param largeIcon large icon bitmap
	 * @param smallIcon small icon drawable resource
	 * @param title     tile text
	 * @param message   message text
	 * @param ongoing   ongoing notification
	 * @param priority  notification priority
	 * @param actions   list of actions for this notification
	 * @return ID of notification
	 */
	public static int createNotification(@NonNull Context context, @Nullable Bitmap largeIcon, @DrawableRes int smallIcon, @Nullable String title, @Nullable String message, boolean ongoing, int priority, @Nullable PendingIntent tapAction, @Nullable List<NotificationCompat.Action> actions) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		int id = ++LAST_ID;

		if (smallIcon <= 0) {
			// set default small icon
			smallIcon = R.drawable.icon_notification;
		}
		if (largeIcon == null) {
			// set default large icon
			largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_material);
		}
		if (title == null) {
			// set default title text
			title = context.getString(R.string.powerswitch_app_name);
		}
		if (tapAction == null) {
			// set default tap action (open PowerSwitch app)
			Intent openPowerSwitchIntent = new Intent(context, MainActivity.class);
			tapAction = PendingIntent.getActivity(context, 0, openPowerSwitchIntent, 0);
		}

		// construct notification
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context).setLargeIcon(
				largeIcon)
				.setSmallIcon(smallIcon)
				.setOngoing(ongoing)
				.setContentTitle(title)
				.setContentText(message)
				.setPriority(priority)
				.setContentIntent(tapAction);

		if (actions != null) {
			for (NotificationCompat.Action action : actions) {
				notificationBuilder.addAction(action);
			}
		}

		// show notification
		notificationManager.notify(id, notificationBuilder.build());

		return id;
	}

	/**
	 * Dismiss a notification
	 *
	 * @param context any suitable context
	 * @param id      ID of notification
	 */
	public static void dismissNotification(Context context, int id) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(id);
	}

}

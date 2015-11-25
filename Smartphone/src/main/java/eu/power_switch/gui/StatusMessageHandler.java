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

package eu.power_switch.gui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import eu.power_switch.R;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.shared.log.Log;

/**
 * This is a helper Class to create and show status messages depending on the app state
 * <p/>
 * Created by Markus on 17.11.2015.
 */
public class StatusMessageHandler {

    private static Toast lastToast;
    private static Snackbar lastSnackbar;

    /**
     * Shows a status message on screen, either as Toast if the app is running in the background or as a Snackbar if
     * it is running in the foreground
     * You can pass an actionMessage and a Runnable that will be represented as a button on the Snackbar. The Toast
     * however will not have a button, be mindful about that.
     *
     * @param recyclerViewFragment          recyclerViewFragment this snackbar is shown on (used for
     *                                      the Snackbar and as a context)
     * @param messageResourceId             status message resource id
     * @param actionButtonMessageResourceId message resource id of action button
     * @param runnable                      code that should be executed when activating the action button
     * @param duration                      duration
     */
    public static void showStatusMessage(RecyclerViewFragment recyclerViewFragment, int messageResourceId,
                                         int actionButtonMessageResourceId, Runnable runnable, int duration) {
        Context context = recyclerViewFragment.getContext();

        if (MainActivity.isInForeground()) {
            showSnackbar(recyclerViewFragment.getRecyclerView(),
                    context.getString(messageResourceId),
                    context.getString(actionButtonMessageResourceId), runnable,
                    duration);
        } else {
            showStatusToast(context, context.getString(messageResourceId), duration);
        }
    }

    /**
     * Shows a status message on screen, either as Toast if the app is running in the background or as a Snackbar if
     * it is running in the foreground
     * You can pass an actionMessage and a Runnable that will be represented as a button on the Snackbar. The Toast
     * however will not have a button, be mindful about that.
     *
     * @param context                       any suitable context
     * @param messageResourceId             status message resource id
     * @param actionButtonMessageResourceId message resource id of action button
     * @param runnable                      code that should be executed when activating the action button
     * @param duration                      duration
     */
    public static void showStatusMessage(Context context, int messageResourceId,
                                         int actionButtonMessageResourceId, Runnable runnable, int duration) {
        if (MainActivity.isInForeground()) {
            showSnackbar(MainActivity.getNavigationView(),
                    context.getString(messageResourceId),
                    context.getString(actionButtonMessageResourceId), runnable,
                    duration);
        } else {
            showStatusToast(context, context.getString(messageResourceId), duration);
        }
    }

    /**
     * Shows a status message on screen, either as Toast if the app is running in the background or as a snackbar if
     * it is running in the foreground.
     * The Snackbar will have a "Dismiss" Button by default.
     *
     * @param recyclerViewFragment recyclerViewFragment this snackbar is shown on (used for
     *                             the Snackbar and as a context)
     * @param messageResourceId    status message resource id
     * @param duration             duration
     */
    public static void showStatusMessage(RecyclerViewFragment recyclerViewFragment, int messageResourceId, int duration) {
        Context context = recyclerViewFragment.getContext();

        if (MainActivity.isInForeground()) {
            showStatusSnackbar(recyclerViewFragment.getRecyclerView(), context.getString(messageResourceId), duration);
        } else {
            showStatusToast(context, context.getString(messageResourceId), duration);
        }
    }

    /**
     * Shows a status message on screen, either as Toast if the app is running in the background or as a snackbar if
     * it is running in the foreground.
     * The Snackbar will have a "Dismiss" Button by default.
     *
     * @param context           any suitable context
     * @param messageResourceId status message resource id
     * @param duration          duration
     */
    public static void showStatusMessage(Context context, int messageResourceId, int duration) {
        if (MainActivity.isInForeground()) {
            showStatusSnackbar(MainActivity.getNavigationView(), context.getString(messageResourceId), duration);
        } else {
            showStatusToast(context, context.getString(messageResourceId), duration);
        }
    }

    /**
     * Shows a status message on screen, either as Toast if the app is running in the background or as a snackbar if
     * it is running in the foreground.
     * The Snackbar will have a "Dismiss" Button by default.
     *
     * @param context  any suitable context
     * @param message  status message
     * @param duration duration
     */
    public static void showStatusMessage(Context context, String message, int duration) {
        if (MainActivity.isInForeground()) {
            showStatusSnackbar(MainActivity.getNavigationView(), message, duration);
        } else {
            showStatusToast(context, message, duration);
        }
    }

    /**
     * Show Snackbar with default "Dismiss" action button
     *
     * @param parent   parent view
     * @param message  message
     * @param duration duration
     */
    private static void showStatusSnackbar(View parent, String message, int duration) {
        Log.d("Status Snackbar: " + message);
        final Snackbar snackbar = Snackbar.make(parent, message, duration);

        snackbar.setAction(parent.getContext().getString(R.string.dismiss), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        snackbar.show();
        lastSnackbar = snackbar;
    }

    /**
     * Show Snackbar with custom action button
     *
     * @param parent              parent view
     * @param message             message
     * @param actionButtonMessage action button message
     * @param runnable            action code
     * @param duration            duration
     */
    private static void showSnackbar(View parent, String message, String actionButtonMessage,
                                     final Runnable runnable, int duration) {
        Log.d("Status Snackbar: [" + message + "] with action: [" + actionButtonMessage + "]");

        if (parent == null) {
            parent = MainActivity.getNavigationView();
        }
        Snackbar snackbar = Snackbar.make(parent, message, duration);

        snackbar.setAction(actionButtonMessage, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runnable.run();
            }
        });

        snackbar.show();
        lastSnackbar = snackbar;
    }

    /**
     * Show Toast above all other views
     *
     * @param context  any suitable context
     * @param message  toast message
     * @param duration duration of toast
     */
    private static void showStatusToast(final Context context, final String message, final int duration) {
        Log.d("Status Toast: " + message);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // cancel last toast
                if (lastToast != null) {
                    lastToast.cancel();
                }

                // create and show new toast
                Toast toast = Toast.makeText(context.getApplicationContext(), message, duration);
                toast.show();

                // save toast reference
                lastToast = toast;
            }
        });
    }
}

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import eu.power_switch.R;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.settings.SettingsTabFragment;
import eu.power_switch.shared.constants.SettingsConstants;
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
    public static void showStatusMessage(RecyclerViewFragment recyclerViewFragment, @StringRes int messageResourceId,
                                         @StringRes int actionButtonMessageResourceId, Runnable runnable, int
                                                 duration) {
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
    public static void showStatusMessage(Context context, @StringRes int messageResourceId,
                                         @StringRes int actionButtonMessageResourceId, Runnable runnable, int
                                                 duration) {
        if (MainActivity.isInForeground()) {
            showSnackbar(MainActivity.getMainAppView(),
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
     * @param message              status message
     * @param duration             duration
     */
    public static void showStatusMessage(RecyclerViewFragment recyclerViewFragment, String message, int duration) {
        Context context = recyclerViewFragment.getContext();

        if (MainActivity.isInForeground()) {
            showStatusSnackbar(recyclerViewFragment.getRecyclerView(), message, duration);
        } else {
            showStatusToast(context, message, duration);
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
    public static void showStatusMessage(RecyclerViewFragment recyclerViewFragment, @StringRes int messageResourceId, int
            duration) {
        Context context = recyclerViewFragment.getContext();
        showStatusMessage(recyclerViewFragment, context.getString(messageResourceId), duration);
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
    public static void showStatusMessage(Context context, @StringRes int messageResourceId, int duration) {
        showStatusMessage(context, context.getString(messageResourceId), duration);
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
            showStatusSnackbar(MainActivity.getMainAppView(), message, duration);
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
            parent = MainActivity.getMainAppView();
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

    /**
     * Shows "No active Gateway" Message
     *
     * @param recyclerViewFragment
     */
    public static void showNoActiveGatewayMessage(final RecyclerViewFragment recyclerViewFragment) {
        showStatusMessage(recyclerViewFragment, R.string.no_active_gateway, R.string.open_settings, new Runnable() {
            @Override
            public void run() {
                MainActivity.addToBackstack(SettingsTabFragment.class, recyclerViewFragment.getActivity()
                        .getString(R.string.menu_settings));
                SettingsTabFragment settingsTabFragment = new SettingsTabFragment();
                Bundle arguments = new Bundle();
                arguments.putInt(SettingsTabFragment.TAB_INDEX_KEY, SettingsConstants.GATEWAYS_TAB_INDEX);
                settingsTabFragment.setArguments(arguments);
                recyclerViewFragment.getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim
                                .slide_in_right, R.anim.slide_out_left, android.R.anim
                                .slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.mainContentFrameLayout, settingsTabFragment)
                        .addToBackStack(null).commit();
            }
        }, Snackbar.LENGTH_LONG);
    }

    /**
     * Shows "No active Gateway" Message
     *
     * @param fragmentActivity
     */
    public static void showNoActiveGatewayMessage(final FragmentActivity fragmentActivity) {
        showStatusMessage(fragmentActivity, R.string.no_active_gateway, R.string.open_settings, new Runnable() {
            @Override
            public void run() {
                MainActivity.addToBackstack(SettingsTabFragment.class, fragmentActivity
                        .getString(R.string.menu_settings));
                SettingsTabFragment settingsTabFragment = new SettingsTabFragment();
                Bundle arguments = new Bundle();
                arguments.putInt(SettingsTabFragment.TAB_INDEX_KEY, SettingsConstants.GATEWAYS_TAB_INDEX);
                settingsTabFragment.setArguments(arguments);
                fragmentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim
                                .slide_in_right, R.anim.slide_out_left, android.R.anim
                                .slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.mainContentFrameLayout, settingsTabFragment)
                        .addToBackStack(null).commit();
            }
        }, Snackbar.LENGTH_LONG);
    }
}

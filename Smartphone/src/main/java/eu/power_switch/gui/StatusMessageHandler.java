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

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.Date;

import eu.power_switch.R;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.UnknownErrorDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.settings.SettingsTabFragment;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.permission.PermissionHelper;

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
     * @param view                          parent view this snackbar is shown on (used for
     *                                      the Snackbar and as a context)
     * @param messageResourceId             status message resource id
     * @param actionButtonMessageResourceId message resource id of action button
     * @param runnable                      code that should be executed when activating the action button
     * @param duration                      duration
     */
    public static void showInfoMessage(View view, @StringRes int messageResourceId,
                                       @StringRes int actionButtonMessageResourceId, Runnable runnable, int duration) {
        Context context = view.getContext();

        if (MainActivity.isInForeground()) {
            if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                showSnackbar(recyclerView,
                        context.getString(messageResourceId),
                        context.getString(actionButtonMessageResourceId), runnable,
                        duration);
            } else {
                showSnackbar(view,
                        context.getString(messageResourceId),
                        context.getString(actionButtonMessageResourceId), runnable,
                        duration);
            }
        } else {
            showInfoToast(context, context.getString(messageResourceId), duration);
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
    public static void showInfoMessage(Context context, @StringRes int messageResourceId,
                                       @StringRes int actionButtonMessageResourceId, Runnable runnable, int
                                               duration) {
        if (MainActivity.isInForeground()) {
            showSnackbar(MainActivity.getMainAppView(),
                    context.getString(messageResourceId),
                    context.getString(actionButtonMessageResourceId), runnable,
                    duration);
        } else {
            showInfoToast(context, context.getString(messageResourceId), duration);
        }
    }

    /**
     * Shows a status message on screen, either as Toast if the app is running in the background or as a snackbar if
     * it is running in the foreground.
     * The Snackbar will have a "Dismiss" Button by default.
     *
     * @param fragment          target fragment this snackbar is shown on (used for
     *                          the Snackbar and as a context)
     * @param messageResourceId status message resource id
     * @param duration          duration
     */
    public static void showInfoMessage(Fragment fragment, @StringRes int messageResourceId, int duration) {
        if (fragment instanceof RecyclerViewFragment) {
            RecyclerViewFragment recyclerViewFragment = (RecyclerViewFragment) fragment;
            showInfoMessage(recyclerViewFragment.getRecyclerView(), messageResourceId, duration);
        } else {
            showInfoMessage(fragment.getView(), messageResourceId, duration);
        }
    }

    /**
     * Shows a status message on screen, either as Toast if the app is running in the background or as a snackbar if
     * it is running in the foreground.
     * The Snackbar will have a "Dismiss" Button by default.
     *
     * @param view              view this snackbar is shown on (used for
     *                          the Snackbar and as a context)
     * @param messageResourceId status message resource id
     * @param duration          duration
     */
    public static void showInfoMessage(View view, @StringRes int messageResourceId, int duration) {
        Context context = view.getContext();
        showInfoMessage(view, context.getString(messageResourceId), duration);
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
    public static void showInfoMessage(Context context, @StringRes int messageResourceId, int duration) {
        showInfoMessage(context, context.getString(messageResourceId), duration);
    }

    /**
     * Shows a status message on screen, either as Toast if the app is running in the background or as a snackbar if
     * it is running in the foreground.
     * The Snackbar will have a "Dismiss" Button by default.
     *
     * @param view     view this snackbar is shown on (used for
     *                 the Snackbar and as a context)
     * @param message  status message
     * @param duration duration
     */
    public static void showInfoMessage(View view, String message, int duration) {
        Context context = view.getContext();

        if (MainActivity.isInForeground()) {
            if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                showInfoSnackbar(recyclerView, message, duration);
            } else {
                showInfoSnackbar(view, message, duration);
            }
        } else {
            showInfoToast(context, message, duration);
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
    public static void showInfoMessage(Context context, String message, int duration) {
        if (MainActivity.isInForeground()) {
            showInfoSnackbar(MainActivity.getMainAppView(), message, duration);
        } else {
            showInfoToast(context, message, duration);
        }
    }

    /**
     * Shows an error message on screen, either as Toast if the app is running in the background or as a snackbar if
     * it is running in the foreground.
     * The Snackbar will have a "Details" Button by default, which opens up a dialog showing more infos about the exception.
     * The exception will also be logged, so you dont have to do this in your catch{} blocks yourself.
     *
     * @param fragment target fragment this snackbar is shown on (used for
     *                 the Snackbar and as a context)
     * @param e        throwable
     */
    public static void showErrorMessage(Fragment fragment, final Throwable e) {
        if (fragment instanceof RecyclerViewFragment) {
            RecyclerViewFragment recyclerViewFragment = (RecyclerViewFragment) fragment;
            showErrorMessage(recyclerViewFragment.getRecyclerView(), e);
        } else {
            showErrorMessage(fragment.getView(), e);
        }
    }

    /**
     * Shows an error message on screen, either as Toast if the app is running in the background or as a snackbar if
     * it is running in the foreground.
     * The Snackbar will have a "Details" Button by default, which opens up a dialog showing more infos about the exception.
     * The exception will also be logged, so you dont have to do this in your catch{} blocks yourself.
     *
     * @param view view this snackbar is shown on (used for
     *             the Snackbar and as a context)
     * @param e    throwable
     */
    public static void showErrorMessage(final View view, final Throwable e) {
        Context context = view.getContext();

        if (MainActivity.isInForeground()) {
            if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                showErrorSnackbar(MainActivity.getActivity(), recyclerView, e);
            } else {
                showErrorSnackbar(MainActivity.getActivity(), view, e);
            }
        } else {
            showErrorToast(context, e);
        }
    }

    /**
     * Shows an error message on screen, either as Toast if the app is running in the background or as a snackbar if
     * it is running in the foreground.
     * The Snackbar will have a "Details" Button by default, which opens up a dialog showing more infos about the exception.
     * The exception will also be logged, so you dont have to do this in your catch{} blocks yourself.
     *
     * @param context any suitable context
     * @param e       throwable
     */
    public static void showErrorMessage(Context context, Throwable e) {
        if (MainActivity.isInForeground()) {
            showErrorSnackbar(MainActivity.getActivity(), MainActivity.getMainAppView(), e);
        } else {
            showErrorToast(context, e);
        }
    }

    /**
     * Show Error Dialog
     *
     * @param context            any suitable context
     * @param t                  Throwable
     * @param timeInMilliseconds time when the exception was thrown
     */
    public static void showErrorDialog(Context context, Throwable t, long timeInMilliseconds) {
        context.startActivity(UnknownErrorDialog.getNewInstanceIntent(t, timeInMilliseconds));
    }

    /**
     * Show Error Dialog
     *
     * @param context any suitable context
     * @param t       Throwable
     */
    public static void showErrorDialog(Context context, Throwable t) {
        showErrorDialog(context, t, new Date().getTime());
    }

    /**
     * Shows "No active Gateway" Message
     *
     * @param recyclerViewFragment
     */
    public static void showNoActiveGatewayMessage(final RecyclerViewFragment recyclerViewFragment) {
        showInfoMessage(recyclerViewFragment.getRecyclerView(), R.string.no_active_gateway, R.string.open_settings, new Runnable() {
            @Override
            public void run() {
                MainActivity.addToBackstack(MainActivity.IDENTIFIER_SETTINGS, SettingsTabFragment.class,
                        recyclerViewFragment.getActivity().getString(R.string.menu_settings));

                SettingsTabFragment settingsTabFragment = SettingsTabFragment.newInstance(SettingsConstants.GATEWAYS_TAB_INDEX);
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
        showInfoMessage(fragmentActivity, R.string.no_active_gateway, R.string.open_settings, new Runnable() {
            @Override
            public void run() {
                MainActivity.addToBackstack(MainActivity.IDENTIFIER_SETTINGS, SettingsTabFragment.class, fragmentActivity
                        .getString(R.string.menu_settings));
                SettingsTabFragment settingsTabFragment = SettingsTabFragment.newInstance(SettingsConstants.GATEWAYS_TAB_INDEX);
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

    /**
     * Show missing permission message with "Grant" button to trigger permission request dialog
     * <p/>
     * See {Manifest.permission} constants for more info about permission strings
     *
     * @param activity     activity used for permission changed callbacks
     * @param recyclerView recyclerview to show snackbar on
     * @param permissions  permission constant(s)
     */
    public static void showPermissionMissingMessage(final Activity activity, final RecyclerView recyclerView, final int requestCode, final String... permissions) {
        if (permissions.length == 0) {
            throw new IllegalArgumentException("Missing permission constant(s)");
        }

        int messageResource = PermissionHelper.getPermissionMessage(permissions);

        Snackbar snackbar = Snackbar.make(recyclerView, messageResource, Snackbar.LENGTH_INDEFINITE);
        if (requestCode != -1) {
            snackbar.setAction(R.string.grant, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(activity, permissions, requestCode);
                }
            });
        } else {
            snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // do nothing, just dismiss the snackbar
                }
            });
        }
        snackbar.show();
        lastSnackbar = snackbar;
    }

    /**
     * Show Snackbar with default "Dismiss" action button
     *
     * @param parent   parent view
     * @param message  message
     * @param duration duration
     */
    private static void showInfoSnackbar(View parent, String message, int duration) {
        Log.d("Status Snackbar: " + message);
        final Snackbar snackbar = Snackbar.make(parent, message, duration);

        snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing, just dismiss the snackbar
            }
        });

        snackbar.show();
        lastSnackbar = snackbar;
    }

    /**
     * Show Snackbar with default "Details" action button, which opens a dialog
     * containing the full Exception message
     *
     * @param activity activity used to generate dialog transition
     * @param parent   parent view
     * @param e        throwable
     */
    private static void showErrorSnackbar(final FragmentActivity activity, View parent, final Throwable e) {
        Log.e("Error Snackbar", e);

        // remember the time when the exception was raised
        final Date timeRaised = new Date();
        showSnackbar(parent, activity.getString(R.string.unknown_error), activity.getString(R.string.details),
                new Runnable() {
                    @Override
                    public void run() {
                        StatusMessageHandler.showErrorDialog(activity, e, timeRaised.getTime());
                    }
                }, 15000);
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
        Log.d("Snackbar: [" + message + "] with action: [" + actionButtonMessage + "]");

        if (parent == null) {
            parent = MainActivity.getMainAppView();
        }

        final Snackbar snackbar = Snackbar.make(parent, message, duration);
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
     * Show Status Toast above all other views
     *
     * @param context  any suitable context
     * @param message  toast message
     * @param duration duration of toast
     */
    private static void showInfoToast(final Context context, final String message, final int duration) {
        if (!SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SHOW_TOAST_IN_BACKGROUND)) {
            Log.w("Toast suppressed (disabled): " + message);
            return;
        }

        Log.d("Status Toast: " + message);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // cancel last toast
                dismissCurrentToast();

                // create and show new toast
                Toast toast = Toast.makeText(context.getApplicationContext(), message, duration);
                toast.show();

                // save toast reference
                lastToast = toast;
            }
        });
    }

    /**
     * Show Error Toast above all other views
     *
     * @param context any suitable context
     * @param e       throwable
     */
    private static void showErrorToast(final Context context, final Throwable e) {
        Log.e("Error Toast: ", e);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // cancel last toast
                dismissCurrentToast();

                // create and show new toast
                Toast toast = Toast.makeText(context.getApplicationContext(), e.getClass()
                        .toString(), Toast.LENGTH_LONG);
                toast.show();

                // save toast reference
                lastToast = toast;
            }
        });
    }

    /**
     * Dismisses the currently visible toast.
     * If there is no toast this method will do nothing.
     */
    public static void dismissCurrentToast() {
        try {
            if (lastToast != null) {
                lastToast.cancel();
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    /**
     * Dismisses the currently visible snackbar.
     * If there is no snackbar this method will do nothing.
     */
    public static void dismissCurrentSnackbar() {
        try {
            if (lastSnackbar != null) {
                lastSnackbar.dismiss();
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }
}

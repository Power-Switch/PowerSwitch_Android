/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.api.taskerplugin.bundle;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;

import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.shared.constants.ApiConstants;

/**
 * Class for managing the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} for this plug-in.
 */
public final class PluginBundleManager {
    /**
     * Type: {@code int}.
     * <p/>
     * versionCode of the plug-in that saved the Bundle.
     */
    /*
     * This extra is not strictly required, however it makes backward and forward compatibility significantly
     * easier. For example, suppose a bug is found in how some version of the plug-in stored its Bundle. By
     * having the version, the plug-in can better detect when such bugs occur.
     */
    public static final String BUNDLE_EXTRA_INT_VERSION_CODE =
            "eu.power_switch.extra.INT_VERSION_CODE"; //$NON-NLS-1$

    /**
     * Private constructor prevents instantiation
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PluginBundleManager() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }

    /**
     * Method to verify the content of the bundle are correct.
     * <p/>
     * This method will not mutate {@code bundle}.
     *
     * @param bundle bundle to verify. May be null, which will always return false.
     * @return true if the Bundle is valid, false if the bundle is invalid.
     */
    public static boolean isBundleValid(Context context, StatusMessageHandler statusMessageHandler, final Bundle bundle) {
        if (null == bundle) {
            return false;
        }

        /*
         * Make sure the expected extras exist
         */
        if (!bundle.containsKey(ApiConstants.KEY_APARTMENT)) {
            statusMessageHandler.showInfoMessage(context,
                    String.format("bundle extra %s appears to be missing.", ApiConstants.KEY_APARTMENT),
                    Snackbar.LENGTH_LONG);
            return false;
        } else if (TextUtils.isEmpty(bundle.getString(ApiConstants.KEY_APARTMENT))) {
            statusMessageHandler.showInfoMessage(context,
                    String.format("bundle extra %s appears to be null or empty.  It must be a non-empty string", ApiConstants.KEY_APARTMENT),
                    Snackbar.LENGTH_LONG);
            return false;
        }

        // Receiver Action
        if (bundle.keySet().size() == 3 * 2 + 2 + 1 && bundle.containsKey(ApiConstants.KEY_ROOM) && bundle.containsKey(ApiConstants.KEY_RECEIVER) && bundle.containsKey(ApiConstants.KEY_BUTTON)) {
            if (TextUtils.isEmpty(bundle.getString(ApiConstants.KEY_ROOM))) {
                statusMessageHandler.showInfoMessage(context,
                        String.format("bundle extra %s appears to be null or empty.  It must be a non-empty string", ApiConstants.KEY_ROOM),
                        Snackbar.LENGTH_LONG);
                return false;
            }

            if (TextUtils.isEmpty(bundle.getString(ApiConstants.KEY_RECEIVER))) {
                statusMessageHandler.showInfoMessage(context,
                        String.format("bundle extra %s appears to be null or empty.  It must be a non-empty string", ApiConstants.KEY_RECEIVER),
                        Snackbar.LENGTH_LONG);
                return false;
            }

            if (TextUtils.isEmpty(bundle.getString(ApiConstants.KEY_BUTTON))) {
                statusMessageHandler.showInfoMessage(context,
                        String.format("bundle extra %s appears to be null or empty.  It must be a non-empty string", ApiConstants.KEY_BUTTON),
                        Snackbar.LENGTH_LONG);
                return false;
            }

            return true;
        }

        // Room Action
        if (bundle.keySet().size() == 2 * 2 + 2 + 1 && bundle.containsKey(ApiConstants.KEY_ROOM) && bundle.containsKey(ApiConstants.KEY_BUTTON)) {
            if (TextUtils.isEmpty(bundle.getString(ApiConstants.KEY_ROOM))) {
                statusMessageHandler.showInfoMessage(context,
                        String.format("bundle extra %s appears to be null or empty.  It must be a non-empty string", ApiConstants.KEY_ROOM),
                        Snackbar.LENGTH_LONG);
                return false;
            }

            if (TextUtils.isEmpty(bundle.getString(ApiConstants.KEY_BUTTON))) {
                statusMessageHandler.showInfoMessage(context,
                        String.format("bundle extra %s appears to be null or empty.  It must be a non-empty string", ApiConstants.KEY_BUTTON),
                        Snackbar.LENGTH_LONG);
                return false;
            }

            return true;
        }

        // Scene Action
        if (bundle.keySet().size() == 1 * 2 + 2 + 1 && bundle.containsKey(ApiConstants.KEY_SCENE)) {
            if (TextUtils.isEmpty(bundle.getString(ApiConstants.KEY_SCENE))) {
                statusMessageHandler.showInfoMessage(context,
                        String.format("bundle extra %s appears to be null or empty.  It must be a non-empty string", ApiConstants.KEY_SCENE),
                        Snackbar.LENGTH_LONG);
                return false;
            }

            return true;
        }


//        if (!bundle.containsKey(BUNDLE_EXTRA_INT_VERSION_CODE)) {
//            Timber.e(String.format("bundle must contain extra %s", BUNDLE_EXTRA_INT_VERSION_CODE)); //$NON-NLS-1$
//            return false;
//        }

//        if (bundle.getInt(BUNDLE_EXTRA_INT_VERSION_CODE, 0) != bundle.getInt(BUNDLE_EXTRA_INT_VERSION_CODE, 1)) {
//            Timber.e(String.format("bundle extra %s appears to be the wrong type.  It must be an int", BUNDLE_EXTRA_INT_VERSION_CODE)); //$NON-NLS-1$
//
//            return false;
//        }

        return false;
    }

    /**
     * @param context Application context.
     * @param message The toast message to be displayed by the plug-in. Cannot be null.
     * @return A plug-in bundle.
     */
    public static Bundle generateBundle(final Context context, final String message) {
        final Bundle result = new Bundle();
        try {
            result.putInt(BUNDLE_EXTRA_INT_VERSION_CODE, context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }
}
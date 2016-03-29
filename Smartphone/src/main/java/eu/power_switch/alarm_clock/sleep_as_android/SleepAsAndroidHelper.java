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

package eu.power_switch.alarm_clock.sleep_as_android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import eu.power_switch.shared.log.Log;

/**
 * Helper class for Sleep As Android specific tasks
 * <p/>
 * Created by Markus on 20.02.2016.
 */
public class SleepAsAndroidHelper {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private SleepAsAndroidHelper() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Check if Sleep As Android app is installed on this device
     *
     * @param context any suitable context
     * @return true if installed, false otherwise (or on error)
     */
    public static boolean isInstalled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(SleepAsAndroidConstants.PACKAGE_NAME, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (Exception e) {
            Log.e(e);
            return false;
        }
    }

    /**
     * Open Sleep As Android Play Store page
     *
     * @param context any suitable context
     */
    public static void openPlayStorePage(Context context) {
        final String appPackageName = SleepAsAndroidConstants.PACKAGE_NAME;
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}

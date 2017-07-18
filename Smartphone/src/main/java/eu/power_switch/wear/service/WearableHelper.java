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

package eu.power_switch.wear.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import eu.power_switch.shared.constants.WearableConstants;
import timber.log.Timber;

/**
 * Created by Markus on 26.07.2016.
 */
public class WearableHelper {

    /**
     * Checks if Android Wear App is installed on this system
     *
     * @return true if installed, false otherwise
     */
    public static boolean isAndroidWearInstalled(@NonNull Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo    info           = packageManager.getPackageInfo(WearableConstants.ANDROID_WEAR_PACKAGE_NAME, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (Exception e) {
            Timber.e(e);
            return false;
        }
    }

}

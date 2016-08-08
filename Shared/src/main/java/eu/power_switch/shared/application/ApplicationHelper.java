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

package eu.power_switch.shared.application;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;

import eu.power_switch.shared.log.Log;

/**
 * Created by Markus on 08.08.2016.
 */
public class ApplicationHelper {

    /**
     * Get a text representation of application version name and build number
     *
     * @param context any suitable context
     * @return app version as text (or "unknown" if failed to retrieve), never null
     */
    @NonNull
    public static String getAppVersionDescription(@NonNull Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (Exception e) {
            Log.e(e);
            return "unknown (error while retrieving)";
        }
    }

}

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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.NoSuchElementException;

import eu.power_switch.shared.log.Log;

/**
 * Created by Markus on 08.08.2016.
 */
public class ApplicationHelper {

    /**
     * Get a text representation of application version name and build number
     *
     * @param context any suitable context
     *
     * @return app version as text (or "unknown" if failed to retrieve), never null
     */
    @NonNull
    public static String getAppVersionDescription(@NonNull Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (Exception e) {
            Log.e(e);
            return "unknown (error while retrieving)";
        }
    }

    /**
     * Set the active launcher icon
     *
     * @param ctx  application context
     * @param icon
     */
    public static void setLauncherIcon(Context ctx, LauncherIcon icon) {
        PackageManager  pm = ctx.getPackageManager();
        ActivityManager am = (ActivityManager) ctx.getSystemService(Activity.ACTIVITY_SERVICE);

        ComponentName materialIconComponent = new ComponentName(ctx, "eu.power_switch.gui.activity.MainActivity-MaterialIcon");
        ComponentName oldIconComponent      = new ComponentName(ctx, "eu.power_switch.gui.activity.MainActivity-OldIcon");

        switch (icon) {
            case Old:
                pm.setComponentEnabledSetting(materialIconComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

                pm.setComponentEnabledSetting(oldIconComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                break;
            case Material:
            default:
                pm.setComponentEnabledSetting(materialIconComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                pm.setComponentEnabledSetting(oldIconComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                break;
        }

        // Find launcher and kill it
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolves = pm.queryIntentActivities(i, 0);
        for (ResolveInfo res : resolves) {
            if (res.activityInfo != null) {
                am.killBackgroundProcesses(res.activityInfo.packageName);
            }
        }
    }

    /**
     * Launcher Icons
     */
    public enum LauncherIcon {
        Material, Old;

        public static LauncherIcon valueOf(int ordinal) {
            for (LauncherIcon value : values()) {
                if (value.ordinal() == ordinal) {
                    return value;
                }
            }

            throw new NoSuchElementException();
        }
    }
}

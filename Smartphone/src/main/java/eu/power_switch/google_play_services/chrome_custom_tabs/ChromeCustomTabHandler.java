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

package eu.power_switch.google_play_services.chrome_custom_tabs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.shared.ThemeHelper;

/**
 * Handler for managing Chrome custom tabs
 *
 * Created by Markus on 21.02.2016.
 */
@Singleton
public class ChromeCustomTabHandler {

    public static final String EXTRA_CUSTOM_TABS_SESSION       = "android.support.customtabs.extra.SESSION";
    public static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";

    // Key for the title string for a given custom menu item
    public static final String KEY_CUSTOM_TABS_MENU_TITLE     = "android.support.customtabs.customaction.MENU_ITEM_TITLE";
    // Key that specifies the PendingIntent to launch when the action button
    // or menu item was tapped. Chrome will be calling PendingIntent#send() on
    // taps after adding the url as data. The client app can call
    // Intent#getDataString() to get the url.
    public static final String KEY_CUSTOM_TABS_PENDING_INTENT = "android.support.customtabs.customaction.PENDING_INTENT";

    public static final String EXTRA_CUSTOM_TABS_MENU_ITEMS = "android.support.customtabs.extra.MENU_ITEMS";

    @Inject
    Context context;

    @Inject
    public ChromeCustomTabHandler() {
    }

    /**
     * Opens a chrome custom tab with the specified URL.
     *
     * @param url the url to open
     */
    public void openChromeCustomTab(@NonNull String url) {
        Intent intent = getIntent(url);
        context.startActivity(intent);
    }

    @CheckResult
    private Intent getIntent(@NonNull String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        final int accentColor = ThemeHelper.getThemeAttrColor(context, R.attr.colorAccent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Bundle extras = new Bundle();
            extras.putBinder(EXTRA_CUSTOM_TABS_SESSION, null);
            intent.putExtra(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, accentColor);
            intent.putExtras(extras);

            // Optional. Use an ArrayList for specifying menu related params. There
            // should be a separate Bundle for each custom menu item.
            ArrayList<Bundle> menuItemBundleList = new ArrayList<>();

            // For each menu item do:
//        Bundle menuItem = new Bundle();
//        menuItem.putString(KEY_CUSTOM_TABS_MENU_TITLE, "Share");
//        menuItem.putParcelable(KEY_CUSTOM_TABS_PENDING_INTENT, PendingIntent.getActivity(context, 0, new Intent()));
//        menuItemBundleList.add(menuItem);
//
//        intent.putParcelableArrayListExtra(EXTRA_CUSTOM_TABS_MENU_ITEMS, menuItemBundleList);
        }

        return intent;
    }


}

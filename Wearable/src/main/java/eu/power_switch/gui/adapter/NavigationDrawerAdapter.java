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

package eu.power_switch.gui.adapter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.drawer.WearableNavigationDrawer;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.fragment.RoomsFragment;
import eu.power_switch.gui.fragment.ScenesFragment;
import eu.power_switch.gui.fragment.SettingsFragment;

/**
 * Navigation Drawer implementation
 * <p/>
 * Created by Markus on 07.06.2016.
 */
public class NavigationDrawerAdapter extends WearableNavigationDrawer.WearableNavigationDrawerAdapter {

    private static final int INDEX_ROOMS    = 0;
    private static final int INDEX_SCENES   = 1;
    private static final int INDEX_SETTINGS = 2;

    private final Activity activity;

    public NavigationDrawerAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public String getItemText(int i) {
        switch (i) {
            case INDEX_ROOMS:
                return activity.getString(R.string.rooms);
            case INDEX_SCENES:
                return activity.getString(R.string.scenes);
            case INDEX_SETTINGS:
                return activity.getString(R.string.settings);
            default:
                return "";
        }
    }

    @Override
    public Drawable getItemDrawable(int i) {
        switch (i) {
            case INDEX_ROOMS:
                return IconicsHelper.getRoomsIcon(activity);
            case INDEX_SCENES:
                return IconicsHelper.getScenesIcon(activity);
            case INDEX_SETTINGS:
                return IconicsHelper.getSettingsIcon(activity);
            default:
                return activity.getResources()
                        .getDrawable(R.drawable.wearable_ic_launcher);
        }
    }

    @Override
    public void onItemSelected(int i) {
        Fragment fragment;

        switch (i) {
            case INDEX_ROOMS:
                fragment = new RoomsFragment();
                break;
            case INDEX_SCENES:
                fragment = new ScenesFragment();
                break;
            case INDEX_SETTINGS:
                fragment = new SettingsFragment();
                break;
            default:
                fragment = new RoomsFragment();
                break;
        }

        FragmentManager fragmentManager = activity.getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    @Override
    public int getCount() {
        return 3;
    }
}

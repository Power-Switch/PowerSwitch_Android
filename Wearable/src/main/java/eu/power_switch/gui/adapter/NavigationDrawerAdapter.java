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

package eu.power_switch.gui.adapter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.drawer.WearableNavigationDrawer;

import eu.power_switch.R;
import eu.power_switch.gui.fragment.RoomsFragment;
import eu.power_switch.gui.fragment.ScenesFragment;

/**
 * Navigation Drawer implementation
 * <p/>
 * Created by Markus on 07.06.2016.
 */
public class NavigationDrawerAdapter extends WearableNavigationDrawer.WearableNavigationDrawerAdapter {

    private final Activity activity;

    public NavigationDrawerAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public String getItemText(int i) {
        switch (i) {
            case 0:
                return activity.getString(R.string.rooms);
            case 1:
                return activity.getString(R.string.scenes);
            default:
                return "";
        }
    }

    @Override
    public Drawable getItemDrawable(int i) {
        switch (i) {
            case 0:
                return activity.getResources().getDrawable(R.drawable.wearable_ic_launcher);
            case 1:
                return activity.getResources().getDrawable(R.drawable.wearable_ic_launcher);
            default:
                return activity.getResources().getDrawable(R.drawable.wearable_ic_launcher);
        }
    }

    @Override
    public void onItemSelected(int i) {
        Fragment fragment;

        switch (i) {
            case 0:
                fragment = new RoomsFragment();
                break;
            case 1:
                fragment = new ScenesFragment();
                break;
            default:
                fragment = new RoomsFragment();
                break;
        }

        FragmentManager fragmentManager = activity.getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public int getCount() {
        return 2;
    }
}

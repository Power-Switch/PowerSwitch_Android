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

package eu.power_switch.gui.fragment.main;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.power_switch.R;
import eu.power_switch.shared.constants.SettingsConstants;

/**
 * Fragment holding the room, scene and timer Fragments in a TabLayout
 * <p/>
 * Created by Markus on 25.06.2015.
 */
public class RoomsScenesTimersTabFragment extends Fragment {

    private CustomTabAdapter customTabAdapter;
    private TabLayout tabLayout;
    private ViewPager tabViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tabs_room_scene_timer, container, false);

        // Create the adapter that will return a fragment
        // for each of the two primary sections of the app.
        customTabAdapter = new CustomTabAdapter(getChildFragmentManager(), getActivity());

        // Set up the tabViewPager, attaching the adapter and setting up a listener
        // for when the user swipes between sections.
        tabViewPager = (ViewPager) rootView.findViewById(R.id.tabHost);
        tabViewPager.setAdapter(customTabAdapter);

        tabViewPager.setOffscreenPageLimit(customTabAdapter.getCount());
        tabViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout);
        tabLayout.setTabsFromPagerAdapter(customTabAdapter);
        tabLayout.setupWithViewPager(tabViewPager);

        Bundle args = getArguments();
        if (args != null && args.containsKey("tabIndex")) {
            int tabIndex = args.getInt("tabIndex");
            tabViewPager.setCurrentItem(tabIndex);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private static class CustomTabAdapter extends FragmentPagerAdapter {
        private Context context;

        public CustomTabAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case SettingsConstants.ROOMS_TAB_INDEX:
                    return new RoomsFragment();
                case SettingsConstants.SCENES_TAB_INDEX:
                    return new ScenesFragment();
                case SettingsConstants.TIMERS_TAB_INDEX:
                    return new TimersFragment();
                default:
                    return null;
            }
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case SettingsConstants.ROOMS_TAB_INDEX:
                    return context.getString(R.string.rooms);
                case SettingsConstants.SCENES_TAB_INDEX:
                    return context.getString(R.string.scenes);
                case SettingsConstants.TIMERS_TAB_INDEX:
                    return context.getString(R.string.timers);
                default:
                    return "";
            }
        }
    }
}

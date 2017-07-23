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

package eu.power_switch.gui.fragment.alarm_clock;

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

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.shared.constants.AlarmClockConstants;
import eu.power_switch.tutorial.TutorialHandler;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Fragment holding SAA and Stock alarm clock Fragments in a TabLayout
 * <p/>
 * Created by Markus on 27.03.2016.
 */
public class AlarmClockTabFragment extends Fragment {

    public static final String TAB_INDEX_KEY = "tabIndex";

    private CustomTabAdapter customTabAdapter;
    private TabLayout tabLayout;
    private ViewPager tabViewPager;
    private int currentTab = 0;
    private boolean skipTutorial = false;

    public static AlarmClockTabFragment newInstance(int tabIndex) {
        Bundle args = new Bundle();
        args.putInt(TAB_INDEX_KEY, tabIndex);

        AlarmClockTabFragment fragment = new AlarmClockTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.alarm_clock_tabs, container, false);

        // Create the adapter that will return a fragment
        // for each of the two primary sections of the app.
        customTabAdapter = new CustomTabAdapter(getChildFragmentManager(), getActivity());

        // Set up the tabViewPager, attaching the adapter and setting up a listener
        // for when the user swipes between sections.
        tabViewPager = rootView.findViewById(R.id.tabHost);
        tabViewPager.setAdapter(customTabAdapter);

        tabViewPager.setOffscreenPageLimit(customTabAdapter.getCount());
        tabViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentTab = position;
                if (!skipTutorial) {
                    showTutorial(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        skipTutorial = true;

        tabLayout = rootView.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(tabViewPager);

        Bundle args = getArguments();
        if (args != null && args.containsKey(TAB_INDEX_KEY)) {
            currentTab = args.getInt(TAB_INDEX_KEY);
            tabViewPager.setCurrentItem(currentTab);
        }

        skipTutorial = false;

        return rootView;
    }

    private void showTutorial(int tabIndex) {
        ArrayList<View> views = new ArrayList<>();
        tabLayout.findViewsWithText(views, customTabAdapter.getPageTitle(tabIndex), View.FIND_VIEWS_WITH_TEXT);

        View dummyView;
        if (views.size() > 0) {
            dummyView = views.get(0);
        } else {
            dummyView = new View(getContext());
        }

        String showcaseKey = TutorialHandler.getAlarmClockTabKey(customTabAdapter.getPageTitle(tabIndex)
                .toString());

        String contentText;
        switch (tabIndex) {
            case AlarmClockConstants.STOCK_TAB_INDEX:
                contentText = getString(R.string.tutorial__stock_alarm_clock_explanation);
                break;
            case AlarmClockConstants.SAA_TAB_INDEX:
                contentText = getString(R.string.tutorial__sleep_as_android_explanation);
                break;
            default:
                return;
        }

        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(dummyView)
                .setUseAutoRadius(false)
                .setRadius(64 * 3)
                .setDismissOnTouch(true)
                .setDismissText(getString(R.string.tutorial__got_it))
                .setContentText(contentText)
                .singleUse(showcaseKey)
                .setDelay(500)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        showTutorial(tabViewPager.getCurrentItem());
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
                case AlarmClockConstants.STOCK_TAB_INDEX:
                    return new StockAlarmClockFragment();
                case AlarmClockConstants.SAA_TAB_INDEX:
                    return new SleepAsAndroidFragment();
                default:
                    return new StockAlarmClockFragment();
            }
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case AlarmClockConstants.STOCK_TAB_INDEX:
                    return context.getString(R.string.stock);
                case AlarmClockConstants.SAA_TAB_INDEX:
                    return context.getString(R.string.sleep_as_android);
                default:
                    return "" + position + 1;
            }
        }
    }
}

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

package eu.power_switch.gui.fragment.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.tutorial.TutorialHelper;
import eu.power_switch.wear.service.WearableHelper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Fragment holding all settings related Fragments in a TabLayout
 * <p/>
 * Created by Markus on 30.08.2015.
 */
public class SettingsTabFragment extends Fragment {

    public static final String TAB_INDEX_KEY = "tabIndex";

    private CustomTabAdapter customTabAdapter;
    private TabLayout tabLayout;
    private ViewPager tabViewPager;

    public static SettingsTabFragment newInstance(int tabIndex) {
        Bundle args = new Bundle();
        args.putInt(TAB_INDEX_KEY, tabIndex);

        SettingsTabFragment fragment = new SettingsTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_tabs, container, false);
        setHasOptionsMenu(true);

        // Create the adapter that will return a fragment
        // for each of the two primary sections of the app.
        customTabAdapter = new CustomTabAdapter(getChildFragmentManager(), getActivity());

        // Set up the tabViewPager, attaching the adapter and setting up a listener
        // for when the user swipes between sections.
        tabViewPager = (ViewPager) rootView.findViewById(R.id.tabHost);
        tabViewPager.setAdapter(customTabAdapter);
        tabViewPager.setOffscreenPageLimit(customTabAdapter.getCount());

        tabViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showTutorial(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(tabViewPager);

        Bundle args = getArguments();
        if (args != null && args.containsKey(TAB_INDEX_KEY)) {
            int tabIndex = args.getInt(TAB_INDEX_KEY);
            tabViewPager.setCurrentItem(tabIndex);
        }

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

        String showcaseKey = TutorialHelper.getSettingsTabKey(customTabAdapter.getPageTitle(tabIndex).toString());

        String contentText;
        switch (tabIndex) {
            case SettingsConstants.GENERAL_SETTINGS_TAB_INDEX:
                // No tutorial for general Settings (as of yet),
                // should be self explanatory for each and every item in itself
                return;
            case SettingsConstants.GATEWAYS_TAB_INDEX:
                contentText = getString(R.string.tutorial__gateways_explanation);
                break;
            case SettingsConstants.WEARABLE_TAB_INDEX:
                contentText = getString(R.string.tutorial__wearable_settings_explanation);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(getActivity(), MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
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
                case SettingsConstants.GENERAL_SETTINGS_TAB_INDEX:
                    return new GeneralSettingsPreferenceFragment();
                case SettingsConstants.GATEWAYS_TAB_INDEX:
//                    return new GatewaySettingsFragment();
                    return new GeneralSettingsFragment();
                case SettingsConstants.WEARABLE_TAB_INDEX:
                    return new WearableSettingsFragment();
                default:
                    return new GeneralSettingsFragment();
            }
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            if (WearableHelper.isAndroidWearInstalled(context)) {
                return 3;
            } else {
                return 2;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case SettingsConstants.GENERAL_SETTINGS_TAB_INDEX:
                    return context.getString(R.string.general);
                case SettingsConstants.GATEWAYS_TAB_INDEX:
                    return context.getString(R.string.gateways);
                case SettingsConstants.WEARABLE_TAB_INDEX:
                    return context.getString(R.string.wearable);
                default:
                    return context.getString(R.string.general);
            }
        }
    }
}

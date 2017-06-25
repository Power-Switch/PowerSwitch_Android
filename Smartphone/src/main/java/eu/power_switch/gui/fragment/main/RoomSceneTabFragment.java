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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.dialog.SelectApartmentDialog;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.tutorial.TutorialHelper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Fragment holding the room and scene Fragments in a TabLayout
 * <p/>
 * Created by Markus on 25.06.2015.
 */
public class RoomSceneTabFragment extends Fragment {

    public static final String TAB_INDEX_KEY = "tabIndex";

    private CustomTabAdapter customTabAdapter;
    private TabLayout tabLayout;
    private ViewPager tabViewPager;
    private int currentTab = 0;
    private boolean skipTutorial = false;
    private TextView textView_currentApartmentInfo;
    private BroadcastReceiver broadcastReceiver;

    public static RoomSceneTabFragment newInstance(int tabIndex) {
        Bundle args = new Bundle();
        args.putInt(TAB_INDEX_KEY, tabIndex);

        RoomSceneTabFragment fragment = new RoomSceneTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_tabs, container, false);

        // Create the adapter that will return a fragment
        // for each of the two primary sections of the app.
        customTabAdapter = new CustomTabAdapter(getChildFragmentManager(), getActivity());

        // Set up the tabViewPager, attaching the adapter and setting up a listener
        // for when the user swipes between sections.
        tabViewPager = rootView.findViewById(R.id.tabHost);
        tabViewPager.setAdapter(customTabAdapter);

        LinearLayout linearLayout_currentApartmentInfo = rootView.findViewById(R.id
                .linearLayout_currentApartmentInfo);
        linearLayout_currentApartmentInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectApartmentDialog selectApartmentDialog = new SelectApartmentDialog();
                selectApartmentDialog.show(getFragmentManager(), null);
            }
        });

        textView_currentApartmentInfo = rootView.findViewById(R.id.textView_currentApartmentInfo);
        updateCurrentApartmentInfo();

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

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());
                updateCurrentApartmentInfo();
            }
        };

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

    private void updateCurrentApartmentInfo() {
        try {
            if (DeveloperPreferencesHandler.getPlayStoreMode()) {
                PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getContext());
                textView_currentApartmentInfo.setText(
                        playStoreModeDataModel.getApartments().get(0).getName());
            } else {
                long currentApartmentId = SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID);
                if (currentApartmentId == SettingsConstants.INVALID_APARTMENT_ID) {
                    textView_currentApartmentInfo.setText(" - ");
                } else {
                    String apartmentName = DatabaseHandler.getApartmentName(currentApartmentId);
                    textView_currentApartmentInfo.setText(apartmentName);
                }
            }
        } catch (Exception e) {
            Log.e(e);
            textView_currentApartmentInfo.setText(R.string.unknown_error);
        }
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

        String showcaseKey = TutorialHelper.getMainTabKey(customTabAdapter.getPageTitle(tabIndex).toString());

        String contentText;
        switch (tabIndex) {
            case SettingsConstants.ROOMS_TAB_INDEX:
                contentText = getString(R.string.tutorial__room_explanation);
                break;
            case SettingsConstants.SCENES_TAB_INDEX:
                contentText = getString(R.string.tutorial__scene_explanation);
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

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_APARTMENT_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
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
                default:
                    return new RoomsFragment();
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
                case SettingsConstants.ROOMS_TAB_INDEX:
                    return context.getString(R.string.rooms);
                case SettingsConstants.SCENES_TAB_INDEX:
                    return context.getString(R.string.scenes);
                default:
                    return "";
            }
        }
    }
}

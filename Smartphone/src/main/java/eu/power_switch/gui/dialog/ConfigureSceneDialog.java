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

package eu.power_switch.gui.dialog;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.fragment.configure_scene.ConfigureSceneDialogPage1NameFragment;
import eu.power_switch.gui.fragment.configure_scene.ConfigureSceneDialogPage2SetupFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.gui.fragment.main.TimersFragment;
import eu.power_switch.log.Log;
import eu.power_switch.shared.Constants;
import eu.power_switch.widget.activity.ConfigureSceneWidgetActivity;

/**
 * Dialog to create or modify a Scene
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureSceneDialog extends DialogFragment {

    private BroadcastReceiver broadcastReceiver;

    private View rootView;
    private TabLayout tabLayout;
    private CustomTabAdapter customTabAdapter;
    private ViewPager tabViewPager;

    private long sceneId = -1;

    private ImageButton imageButtonDelete;
    private ImageButton imageButtonCancel;
    private ImageButton imageButtonSave;
    private ImageButton imageButtonNext;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening ConfigureSceneDialog...");
        rootView = inflater.inflate(R.layout.dialog_configure_scene, container, false);

        Bundle args = getArguments();
        if (args != null && args.containsKey("SceneId")) {
            // init dialog using existing scene
            sceneId = args.getLong("SceneId");
            customTabAdapter = new CustomTabAdapter(getActivity(), getChildFragmentManager(), sceneId);
        } else {
            customTabAdapter = new CustomTabAdapter(getActivity(), getChildFragmentManager());
        }

        // Set up the tabViewPager, attaching the adapter and setting up a listener
        // for when the user swipes between sections.
        tabViewPager = (ViewPager) rootView.findViewById(R.id.tabHost_add_scene_dialog);
        tabViewPager.setAdapter(customTabAdapter);
        tabViewPager.setOffscreenPageLimit(customTabAdapter.getCount());

        tabViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                updateUI();
            }

            @Override
            public void onPageSelected(int position) {
                updateUI();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout_configure_scene_dialog);
        tabLayout.setTabsFromPagerAdapter(customTabAdapter);
        tabLayout.setupWithViewPager(tabViewPager);

        imageButtonDelete = (ImageButton) rootView.findViewById(R.id.imageButton_delete);
        // hide if new receiver
        if (sceneId == -1) {
            imageButtonDelete.setVisibility(View.GONE);
        }
        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                        .scene_will_be_gone_forever)
                        .setPositiveButton
                                (android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DatabaseHandler.deleteScene(sceneId);

                                        // notify scenes fragment
                                        ScenesFragment.sendScenesChangedBroadcast(getActivity());
                                        // notify timers fragment
                                        TimersFragment.sendTimersChangedBroadcast(getActivity());

                                        // update scene widgets
                                        ConfigureSceneWidgetActivity.forceWidgetUpdate(getActivity());

                                        // close dialog
                                        getDialog().dismiss();
                                    }
                                }).setNeutralButton(android.R.string.cancel, null).show();
            }
        });

        imageButtonCancel = (ImageButton) rootView.findViewById(R.id.imageButton_cancel);
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ask to really close
                new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getDialog().cancel();
                            }
                        })
                        .setNeutralButton(android.R.string.no, null)
                        .show();
            }
        });

        imageButtonNext = (ImageButton) rootView.findViewById(R.id.imageButton_next);
        imageButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabViewPager.setCurrentItem(tabViewPager.getCurrentItem() + 1, true);
                updateUI();
            }
        });

        imageButtonSave = (ImageButton) rootView.findViewById(R.id.imageButton_save);
        imageButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Saving scene");
                CustomTabAdapter customTabAdapter = (CustomTabAdapter) tabViewPager.getAdapter();
                ConfigureSceneDialogPage2SetupFragment setupFragment =
                        customTabAdapter.getSetupFragment();
                setupFragment.saveCurrentConfigurationToDatabase();
                getDialog().dismiss();
            }
        });
        if (sceneId == -1) {
            setSaveButtonState(false);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    updateUI();
                } catch (Exception e) {
                    setSaveButtonState(false);
                }
            }
        };

        return rootView;
    }

    /**
     * Updates all necessary UI components
     */
    private void updateUI() {
        if (tabViewPager.getCurrentItem() == customTabAdapter.getCount() - 1) {
            imageButtonSave.setVisibility(View.VISIBLE);
            imageButtonNext.setVisibility(View.GONE);
        } else {
            imageButtonSave.setVisibility(View.GONE);
            imageButtonNext.setVisibility(View.VISIBLE);
        }

        CustomTabAdapter customTabAdapter = (CustomTabAdapter) tabViewPager.getAdapter();
        ConfigureSceneDialogPage2SetupFragment setupFragment =
                customTabAdapter.getSetupFragment();
        boolean validity = setupFragment.checkValidity();
        setSaveButtonState(validity);
    }

    private void setSaveButtonState(boolean enabled) {
        if (enabled) {
            imageButtonSave.setColorFilter(getResources().getColor(eu.power_switch.shared.R.color
                    .active_green));
            imageButtonSave.setClickable(true);
        } else {
            imageButtonSave.setColorFilter(getResources().getColor(eu.power_switch.shared.R.color
                    .inactive_gray));
            imageButtonSave.setClickable(false);
        }
    }


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity()) {
            @Override
            public void onBackPressed() {
                // ask to really close
                new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getDialog().cancel();
                            }
                        })
                        .setNeutralButton(android.R.string.no, null)
                        .show();
            }
        };
        dialog.setTitle(R.string.configure_scene);
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.INTENT_SETUP_SCENE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    private static class CustomTabAdapter extends FragmentPagerAdapter {

        private Context context;
        private long sceneId;
        private ConfigureSceneDialogPage2SetupFragment setupFragment;

        public CustomTabAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
            sceneId = -1;
        }

        public CustomTabAdapter(Context context, FragmentManager fm, long id) {
            super(fm);
            this.context = context;
            this.sceneId = id;
        }

        public ConfigureSceneDialogPage2SetupFragment getSetupFragment() {
            return setupFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return context.getString(R.string.name);
                case 1:
                    return context.getString(R.string.setup);
                case 2:
                    return context.getString(R.string.summary);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            bundle.putLong("SceneId", sceneId);

            switch (i) {
                case 0:
                    ConfigureSceneDialogPage1NameFragment configureSceneDialogPage1NameFragment = new
                            ConfigureSceneDialogPage1NameFragment();
                    configureSceneDialogPage1NameFragment.setArguments(bundle);

                    return configureSceneDialogPage1NameFragment;
                case 1:
                    ConfigureSceneDialogPage2SetupFragment configureSceneDialogPage2SetupFragment = new
                            ConfigureSceneDialogPage2SetupFragment();
                    configureSceneDialogPage2SetupFragment.setArguments(bundle);

                    setupFragment = configureSceneDialogPage2SetupFragment;
                    return configureSceneDialogPage2SetupFragment;
                case 2:
                    return null;
            }
            return null;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 2;
        }
    }

}

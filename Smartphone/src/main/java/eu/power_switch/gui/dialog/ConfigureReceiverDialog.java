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
import android.support.design.widget.Snackbar;
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
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage1NameFragment;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage2TypeFragment;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage3SetupFragment;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage4SummaryFragment;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.gui.fragment.main.TimersFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.widget.activity.ConfigureReceiverWidgetActivity;

/**
 * Dialog to create or modify a Receiver
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialog extends DialogFragment {

    private BroadcastReceiver broadcastReceiver;

    private View rootView;
    private TabLayout tabLayout;
    private CustomTabAdapter customTabAdapter;
    private ViewPager tabViewPager;

    private long receiverId = -1;

    private ImageButton imageButtonCancel;
    private ImageButton imageButtonSave;
    private ImageButton imageButtonDelete;
    private ImageButton imageButtonNext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening ConfigureReceiverDialog...");
        rootView = inflater.inflate(R.layout.dialog_configure_receiver, container, false);

        Bundle args = getArguments();
        if (args != null && args.containsKey("ReceiverId")) {
            // init dialog using existing receiver
            receiverId = args.getLong("ReceiverId");
            customTabAdapter = new CustomTabAdapter(getActivity(), getChildFragmentManager(), (RecyclerViewFragment)
                    getTargetFragment(), receiverId);
        } else {
            // Create the adapter that will return a fragment
            // for each of the two primary sections of the app.
            customTabAdapter = new CustomTabAdapter(getActivity(), getChildFragmentManager(), (RecyclerViewFragment)
                    getTargetFragment());
        }

        // Set up the tabViewPager, attaching the adapter and setting up a listener
        // for when the user swipes between sections.
        tabViewPager = (ViewPager) rootView.findViewById(R.id.tabHost_add_receiver_dialog);
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

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout_configure_receiver_dialog);
        tabLayout.setTabsFromPagerAdapter(customTabAdapter);
        tabLayout.setupWithViewPager(tabViewPager);

        imageButtonDelete = (ImageButton) rootView.findViewById(R.id.imageButton_delete);
        // hide if new receiver
        if (receiverId == -1) {
            imageButtonDelete.setVisibility(View.GONE);
        }
        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                        .receiver_will_be_gone_forever)
                        .setPositiveButton
                                (android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DatabaseHandler.deleteReceiver(receiverId);

                                        // notify rooms fragment
                                        RoomsFragment.sendReceiverChangedBroadcast(getActivity());

                                        // scenes could change too if receiver was used in a scene
                                        ScenesFragment.sendScenesChangedBroadcast(getActivity());
                                        // same for timers
                                        TimersFragment.sendTimersChangedBroadcast(getActivity());

                                        // update receiver widgets
                                        ConfigureReceiverWidgetActivity.forceWidgetUpdate(getActivity());

                                        StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                                                R.string.receiver_deleted, Snackbar.LENGTH_LONG);

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
                Log.d("Saving receiver");
                CustomTabAdapter customTabAdapter = (CustomTabAdapter) tabViewPager.getAdapter();
                ConfigureReceiverDialogPage4SummaryFragment summaryFragment =
                        customTabAdapter.getSummaryFragment();
                if (summaryFragment.checkSetupValidity()) {
                    summaryFragment.saveCurrentConfigurationToDatabase();
                    getDialog().dismiss();
                }
            }
        });
        if (receiverId == -1) {
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
        ConfigureReceiverDialogPage4SummaryFragment summaryFragment =
                customTabAdapter.getSummaryFragment();

        boolean validity = summaryFragment.checkSetupValidity();

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
        dialog.setTitle(R.string.configure_receiver);
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
        intentFilter.addAction(LocalBroadcastConstants.INTENT_RECEIVER_SUMMARY_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    private static class CustomTabAdapter extends FragmentPagerAdapter {

        private Context context;
        private long receiverId;
        private ConfigureReceiverDialogPage4SummaryFragment summaryFragment;
        private RecyclerViewFragment recyclerViewFragment;

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment) {
            super(fm);
            this.context = context;
            this.receiverId = -1;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment, long id) {
            super(fm);
            this.context = context;
            this.receiverId = id;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public ConfigureReceiverDialogPage4SummaryFragment getSummaryFragment() {
            return summaryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return context.getString(R.string.name);
                case 1:
                    return context.getString(R.string.type);
                case 2:
                    return context.getString(R.string.setup);
                case 3:
                    return context.getString(R.string.summary);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            bundle.putLong("ReceiverId", receiverId);

            switch (i) {
                case 0:
                    ConfigureReceiverDialogPage1NameFragment configureReceiverDialogPage1NameFragment = new
                            ConfigureReceiverDialogPage1NameFragment();
                    configureReceiverDialogPage1NameFragment.setArguments(bundle);

                    return configureReceiverDialogPage1NameFragment;
                case 1:
                    ConfigureReceiverDialogPage2TypeFragment configureReceiverDialogPage2TypeFragment = new
                            ConfigureReceiverDialogPage2TypeFragment();
                    configureReceiverDialogPage2TypeFragment.setArguments(bundle);

                    return configureReceiverDialogPage2TypeFragment;
                case 2:
                    ConfigureReceiverDialogPage3SetupFragment configureReceiverDialogPage3SetupFragment = new
                            ConfigureReceiverDialogPage3SetupFragment();
                    configureReceiverDialogPage3SetupFragment.setArguments(bundle);

                    return configureReceiverDialogPage3SetupFragment;
                case 3:
                    ConfigureReceiverDialogPage4SummaryFragment configureReceiverDialogPage4SummaryFragment = new
                            ConfigureReceiverDialogPage4SummaryFragment();
                    configureReceiverDialogPage4SummaryFragment.setArguments(bundle);
                    configureReceiverDialogPage4SummaryFragment.setTargetFragment(recyclerViewFragment, 0);

                    summaryFragment = configureReceiverDialogPage4SummaryFragment;
                    return configureReceiverDialogPage4SummaryFragment;
            }
            return null;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 4;
        }
    }

}
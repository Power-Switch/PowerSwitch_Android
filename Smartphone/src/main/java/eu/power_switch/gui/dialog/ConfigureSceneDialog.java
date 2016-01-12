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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.gui.fragment.configure_scene.ConfigureSceneDialogPage1NameFragment;
import eu.power_switch.gui.fragment.configure_scene.ConfigureSceneDialogPage2SetupFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.widget.provider.SceneWidgetProvider;

/**
 * Dialog to create or modify a Scene
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureSceneDialog extends ConfigurationDialogTabbed {

    /**
     * ID of existing Scene to Edit
     */
    public static final String SCENE_ID_KEY = "SceneId";

    private BroadcastReceiver broadcastReceiver;

    private long sceneId = -1;

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setDialogTitle(getString(R.string.configure_scene));

        setDeleteAction(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                        .scene_will_be_gone_forever)
                        .setPositiveButton
                                (android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            DatabaseHandler.deleteScene(sceneId);

                                            // notify scenes fragment
                                            ScenesFragment.sendScenesChangedBroadcast(getActivity());
                                            // notify timers fragment
                                            TimersFragment.sendTimersChangedBroadcast(getActivity());

                                            // update scene widgets
                                            SceneWidgetProvider.forceWidgetUpdate(getActivity());

                                            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                                                    R.string.scene_deleted, Snackbar.LENGTH_LONG);
                                        } catch (Exception e) {
                                            Log.e(e);
                                            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
                                        }

                                        // close dialog
                                        getDialog().dismiss();
                                    }
                                }).setNeutralButton(android.R.string.cancel, null).show();
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyConfigurationChanged();
            }
        };
    }

    @Override
    protected void initExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(SCENE_ID_KEY)) {
            // init dialog using existing scene
            sceneId = arguments.getLong(SCENE_ID_KEY);
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment(), sceneId));
            imageButtonDelete.setVisibility(View.VISIBLE);
            setSaveButtonState(true);
        } else {
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment()));
            imageButtonDelete.setVisibility(View.GONE);
            setSaveButtonState(false);
        }
    }

    @Override
    protected boolean checkValidity() {
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigureSceneDialogPage2SetupFragment setupFragment =
                customTabAdapter.getSetupFragment();
        return setupFragment.checkValidity();
    }

    @Override
    protected void saveCurrentConfigurationToDatabase() {
        Log.d("Saving scene");
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigureSceneDialogPage2SetupFragment setupFragment =
                customTabAdapter.getSetupFragment();
        setupFragment.saveCurrentConfigurationToDatabase();
        getDialog().dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_SETUP_SCENE_CHANGED);
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
        private RecyclerViewFragment recyclerViewFragment;

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment) {
            super(fm);
            this.context = context;
            this.sceneId = -1;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment, long id) {
            super(fm);
            this.context = context;
            this.sceneId = id;
            this.recyclerViewFragment = recyclerViewFragment;
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
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = new ConfigureSceneDialogPage1NameFragment();
                    break;
                case 1:
                    fragment = new ConfigureSceneDialogPage2SetupFragment();
                    fragment.setTargetFragment(recyclerViewFragment, 0);

                    setupFragment = (ConfigureSceneDialogPage2SetupFragment) fragment;
            }

            if (fragment != null && sceneId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong(SCENE_ID_KEY, sceneId);
                fragment.setArguments(bundle);
            }

            return fragment;
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

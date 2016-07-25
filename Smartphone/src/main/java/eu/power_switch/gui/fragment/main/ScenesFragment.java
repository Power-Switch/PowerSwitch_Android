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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.SceneRecyclerViewAdapter;
import eu.power_switch.gui.dialog.ConfigureSceneDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Scene;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;

/**
 * Fragment containing a List of all Scenes
 */
public class ScenesFragment extends RecyclerViewFragment {

    private ArrayList<Scene> scenes = new ArrayList<>();
    private SceneRecyclerViewAdapter sceneRecyclerViewAdapter;
    private RecyclerView recyclerViewScenes;
    private BroadcastReceiver broadcastReceiver;
    private FloatingActionButton fab;

    /**
     * Used to notify Scene Fragment (this) that Scenes have changed
     *
     * @param context any suitable context
     */
    public static void sendScenesChangedBroadcast(Context context) {
        Log.d("ScenesFragment", "sendScenesChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_SCENE_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onCreateViewEvent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scenes, container, false);

        setHasOptionsMenu(true);

        recyclerViewScenes = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        sceneRecyclerViewAdapter = new SceneRecyclerViewAdapter(this, getActivity(), scenes);
        recyclerViewScenes.setAdapter(sceneRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewScenes.setLayoutManager(layoutManager);

        final RecyclerViewFragment recyclerViewFragment = this;
        sceneRecyclerViewAdapter.setOnItemLongClickListener(new SceneRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                final Scene scene = scenes.get(position);

                ConfigureSceneDialog configureSceneDialog = ConfigureSceneDialog.newInstance(scene.getId());
                configureSceneDialog.setTargetFragment(recyclerViewFragment, 0);
                configureSceneDialog.show(getFragmentManager(), null);
            }
        });

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (SettingsConstants.INVALID_APARTMENT_ID == SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID)) {
                        new AlertDialog.Builder(getContext())
                                .setMessage(R.string.please_create_or_activate_apartment_first)
                                .setNeutralButton(android.R.string.ok, null)
                                .show();
                        return;
                    }


                    ConfigureSceneDialog configureSceneDialog = new ConfigureSceneDialog();
                    configureSceneDialog.setTargetFragment(recyclerViewFragment, 0);
                    configureSceneDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("ScenesFragment", "received intent: " + intent.getAction());
                updateUI();
            }
        };
    }

    @Override
    protected void onInitialized() {
        updateUI();
    }

    private void updateUI() {
        Log.d("ScenesFragment", "updateUI");
        updateListContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_scene:
                if (SettingsConstants.INVALID_APARTMENT_ID == SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID)) {
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.please_create_or_activate_apartment_first)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                    return true;
                }

                ConfigureSceneDialog configureSceneDialog = new ConfigureSceneDialog();
                configureSceneDialog.setTargetFragment(this, 0);
                configureSceneDialog.show(getFragmentManager(), null);
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.scene_fragment_menu, menu);
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.create_scene).setIcon(IconicsHelper.getAddIcon(getActivity(), color));

        if (!SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            menu.findItem(R.id.create_scene).setVisible(false).setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_APARTMENT_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_SCENE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewScenes;
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return sceneRecyclerViewAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.scene_grid_span_count);
    }

    @Override
    public List refreshListData() throws Exception {
        scenes.clear();

        if (DeveloperPreferencesHandler.getPlayStoreMode()) {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
            scenes.addAll(playStoreModeDataModel.getActiveApartment().getScenes());
        } else {
            scenes.addAll(DatabaseHandler.getScenes(SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID)));
        }

        return scenes;
    }
}
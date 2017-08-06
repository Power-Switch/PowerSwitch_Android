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

package eu.power_switch.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSnapHelper;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.event.PreferenceChangedEvent;
import eu.power_switch.gui.IconicsHandler;
import eu.power_switch.gui.adapter.SettingsListAdapter;
import eu.power_switch.gui.view.SettingsListLayoutCallback;
import eu.power_switch.network.service.UtilityService;
import eu.power_switch.settings.BooleanSettingsItem;
import eu.power_switch.settings.SettingsItem;
import eu.power_switch.settings.SingleSelectSettingsItem;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;

/**
 * Fragment holding settings related to wearable
 * <p/>
 * Created by Markus on 07.06.2016.
 */
public class SettingsFragment extends FragmentBase {

    @BindView(R.id.wearableRecyclerView)
    WearableRecyclerView wearableRecyclerView;

    private ArrayList<SettingsItem> settings = new ArrayList<>();
    private SettingsListAdapter settingsListAdapter;
    private boolean ownModification = false;

    @Inject
    IconicsHandler iconicsHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);


        SingleSelectSettingsItem item0 = new SingleSelectSettingsItem(getActivity(),
                iconicsHandler.getWearableSettingsListIcon(MaterialDesignIconic.Icon.gmi_tab),
                R.string.title_startupDefaultTab,
                WearablePreferencesHandler.STARTUP_DEFAULT_TAB,
                R.array.wearable_tab_values,
                R.array.wearable_tab_names,
                wearablePreferencesHandler) {

        };

        SettingsItem item1 = new BooleanSettingsItem(getActivity(),
                iconicsHandler.getWearableSettingsListIcon(MaterialDesignIconic.Icon.gmi_photo_size_select_small),
                R.string.title_autoCollapseRooms,
                WearablePreferencesHandler.AUTO_COLLAPSE_ROOMS,
                wearablePreferencesHandler);
        SettingsItem item2 = new BooleanSettingsItem(getActivity(),
                iconicsHandler.getWearableSettingsListIcon(MaterialDesignIconic.Icon.gmi_time_restore),
                R.string.title_highlightLastActivatedButton,
                WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON,
                wearablePreferencesHandler);
        SettingsItem item3 = new BooleanSettingsItem(getActivity(),
                iconicsHandler.getWearableSettingsListIcon(MaterialDesignIconic.Icon.gmi_vibration),
                R.string.title_vibrateOnButtonPress,
                WearablePreferencesHandler.VIBRATE_ON_BUTTON_PRESS,
                wearablePreferencesHandler);

        settings.add(item0);
        settings.add(item1);
        settings.add(item2);
        settings.add(item3);

        settingsListAdapter = new SettingsListAdapter(getActivity(), settings);
        settingsListAdapter.setOnItemClickListener(new SettingsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SettingsListAdapter.ItemViewHolder viewHolder, int position) {
                SettingsItem settingsItem = settings.get(position);
                if (settingsItem instanceof BooleanSettingsItem) {
                    BooleanSettingsItem booleanSettingsItem = (BooleanSettingsItem) settingsItem;
                    booleanSettingsItem.toggle();

                } else if (settingsItem instanceof SingleSelectSettingsItem) {
                    SingleSelectSettingsItem singleSelectSettingsItem = (SingleSelectSettingsItem) settingsItem;
                    singleSelectSettingsItem.showValueSelector();
                }

                viewHolder.value.setText(settingsItem.getCurrentValueDescription());
                ownModification = true;

                // notify listeners
                EventBus.getDefault()
                        .post(new PreferenceChangedEvent<>(settingsItem.getPreferenceItem()));

                // push new data to cloud
                UtilityService.forceWearSettingsUpdate(getActivity());
            }
        });
        wearableRecyclerView.setAdapter(settingsListAdapter);

        wearableRecyclerView.setEdgeItemsCenteringEnabled(true);

        WearableLinearLayoutManager layoutManager = new WearableLinearLayoutManager(getActivity());
        layoutManager.setLayoutCallback(new SettingsListLayoutCallback(getActivity()));
        wearableRecyclerView.setLayoutManager(layoutManager);

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(wearableRecyclerView);

        refreshUI();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_settings;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onPreferenceChanged(PreferenceChangedEvent e) {
        if (!ownModification) {
            refreshUI();
        } else {
            // ignore self made changes
            ownModification = false;
        }
    }

    private void refreshUI() {
        settingsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (dataApiHandler != null) {
            dataApiHandler.connect();
        }
    }

    @Override
    public void onStop() {
        if (dataApiHandler != null) {
            dataApiHandler.disconnect();
        }

        super.onStop();
    }
}

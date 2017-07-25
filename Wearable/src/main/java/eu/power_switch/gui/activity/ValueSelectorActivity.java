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

package eu.power_switch.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SnapHelper;
import android.support.wearable.view.WearableRecyclerView;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.dagger.android.DaggerWearableActivity;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.ValueSelectorListAdapter;
import eu.power_switch.gui.view.SettingsListSnapHelper;
import eu.power_switch.network.service.UtilityService;
import eu.power_switch.settings.SingleSelectSettingsItem;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import timber.log.Timber;

/**
 * Activity used to select a value out of a collection of values
 * <p/>
 * Created by Markus on 18.07.2016.
 */
public class ValueSelectorActivity<T> extends DaggerWearableActivity {

    public static final String KEY_VALUES = "values";

    private List<T> values;

    public static <T extends Serializable> void newInstance(Context context, List<T> values) {
        Intent intent = new Intent(context, ValueSelectorActivity.class);
        intent.putExtra(KEY_VALUES, new ArrayList<>(values));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value_selector);

        // allow always-on screen
        setAmbientEnabled();

        values = (List<T>) getIntent().getExtras()
                .get(KEY_VALUES);

        // TODO: make this universal for any PreferenceItem

        WearableRecyclerView wearableRecyclerView = findViewById(R.id.recyclerView);
        wearableRecyclerView.setCenterEdgeItems(true);

        SnapHelper snapHelper = new SettingsListSnapHelper();
        snapHelper.attachToRecyclerView(wearableRecyclerView);

        final SingleSelectSettingsItem singleSelectSettingsItem = new SingleSelectSettingsItem(this,
                IconicsHelper.getTabsIcon(this),
                R.string.title_startupDefaultTab,
                WearablePreferencesHandler.STARTUP_DEFAULT_TAB,
                R.array.wear_tab_names,
                wearablePreferencesHandler) {
        };
        final ValueSelectorListAdapter listAdapter = new ValueSelectorListAdapter(this, singleSelectSettingsItem);
        listAdapter.setOnItemClickListener(new ValueSelectorListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                T value = values.get(position);

                singleSelectSettingsItem.setValue(position);
                Timber.d("selected value: " + value);

                listAdapter.notifyDataSetChanged();
                UtilityService.forceWearSettingsUpdate(getApplicationContext());
            }
        });

        wearableRecyclerView.setAdapter(listAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO: send selected value as a result
    }

}


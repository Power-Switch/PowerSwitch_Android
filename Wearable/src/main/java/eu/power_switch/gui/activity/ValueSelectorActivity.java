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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSnapHelper;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.markusressel.typedpreferences.PreferenceItem;
import eu.power_switch.R;
import eu.power_switch.event.EventBusWearableActivity;
import eu.power_switch.gui.adapter.ValueSelectorListAdapter;
import eu.power_switch.gui.view.SettingsListLayoutCallback;
import eu.power_switch.network.service.UtilityService;
import timber.log.Timber;

/**
 * Activity used to select a value out of a collection of values
 * <p/>
 * Created by Markus on 18.07.2016.
 */
public class ValueSelectorActivity extends EventBusWearableActivity {

    public static final String KEY_PREF_ITEM_KEY = "KEY_PREF_ITEM_KEY";
    public static final String KEY_VALUES        = "KEY_VALUES";
    public static final String KEY_DESCRIPTIONS  = "KEY_DESCRIPTIONS";

    @BindView(R.id.recyclerView)
    WearableRecyclerView wearableRecyclerView;

    private List<Integer> values;
    private List<String>  descriptions;

    public static void newInstance(Context context, @NonNull PreferenceItem<Integer> preferenceItem, @NonNull List<Integer> allValues,
                                   @NonNull List<String> descriptions) {
        if (allValues.size() != descriptions.size()) {
            throw new IllegalArgumentException("values and description size must match!");
        }

        Intent intent = new Intent(context, ValueSelectorActivity.class);
        intent.putExtra(KEY_PREF_ITEM_KEY, preferenceItem.getKey(context));
        intent.putExtra(KEY_VALUES, new ArrayList<>(allValues));
        intent.putExtra(KEY_DESCRIPTIONS, new ArrayList<>(descriptions));
        context.startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // allow always-on screen
        setAmbientEnabled();

        String key = (String) getIntent().getExtras()
                .get(KEY_PREF_ITEM_KEY);
        values = (List<Integer>) getIntent().getExtras()
                .get(KEY_VALUES);
        descriptions = (List<String>) getIntent().getExtras()
                .get(KEY_DESCRIPTIONS);
        final PreferenceItem<Integer> preferenceItem = wearablePreferencesHandler.getPreferenceItem(key);
        Integer                       currentValue   = wearablePreferencesHandler.getValue(preferenceItem);

        wearableRecyclerView.setEdgeItemsCenteringEnabled(true);

        WearableLinearLayoutManager layoutManager = new WearableLinearLayoutManager(this);
        layoutManager.setLayoutCallback(new SettingsListLayoutCallback(this));
        wearableRecyclerView.setLayoutManager(layoutManager);

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(wearableRecyclerView);

        final ValueSelectorListAdapter listAdapter = new ValueSelectorListAdapter(this, values, descriptions, currentValue);
        listAdapter.setOnItemClickListener(new ValueSelectorListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Integer value = values.get(position);

                wearablePreferencesHandler.setValue(preferenceItem, value);

                Timber.d("selected value: " + value);

                listAdapter.notifyDataSetChanged();
                UtilityService.forceWearSettingsUpdate(getApplicationContext());
            }
        });

        wearableRecyclerView.setAdapter(listAdapter);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_value_selector;
    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO: send selected value as a result
    }
}


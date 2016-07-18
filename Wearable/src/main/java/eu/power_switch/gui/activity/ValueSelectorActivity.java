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

package eu.power_switch.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableListView;

import java.io.Serializable;
import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.ThemeHelper;
import eu.power_switch.gui.adapter.ValueSelectorListAdapter;
import eu.power_switch.network.service.UtilityService;
import eu.power_switch.settings.SelectOneSettingsItem;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Activity used to select a value out of a collection of values
 * <p/>
 * Created by Markus on 18.07.2016.
 */
public class ValueSelectorActivity<T> extends WearableActivity {

    public static final String KEY_VALUES = "values";
    public static final String KEY_SELECTED_VALUE = "selectedValue";

    private ArrayList<T> values;

    public static <T> void newInstance(Context context, ArrayList<T> values, Serializable selectedValue) {
        Intent intent = new Intent(context, ValueSelectorActivity.class);
        intent.putExtra(KEY_VALUES, values);
        intent.putExtra(KEY_SELECTED_VALUE, selectedValue);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // set Theme before anything else in onCreate
        ThemeHelper.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value_selector);

        // allow always-on screen
        setAmbientEnabled();

        values = (ArrayList<T>) getIntent().getExtras().get(KEY_VALUES);
        T selectedValue = (T) getIntent().getExtras().get(KEY_SELECTED_VALUE);

        WearableListView wearableListView = (WearableListView) findViewById(R.id.listView);
        final SelectOneSettingsItem selectOneSettingsItem = new SelectOneSettingsItem(this,
                IconicsHelper.getTabsIcon(this),
                R.string.startup_default_tab,
                WearablePreferencesHandler.KEY_STARTUP_DEFAULT_TAB,
                R.array.wear_tab_names) {
        };
        final ValueSelectorListAdapter<T> listAdapter = new ValueSelectorListAdapter<>(this, selectOneSettingsItem);
        wearableListView.setAdapter(listAdapter);
        wearableListView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                if (viewHolder.getAdapterPosition() == -1) {
                    return; // ignore click while adapter is refreshing data
                }

                ValueSelectorListAdapter.ItemViewHolder holder = (ValueSelectorListAdapter.ItemViewHolder) viewHolder;
                T value = values.get(viewHolder.getAdapterPosition());

                selectOneSettingsItem.setValue(holder.getAdapterPosition());
                Log.d("selected value: " + value);

                listAdapter.notifyDataSetChanged();
                UtilityService.forceWearSettingsUpdate(getApplicationContext());
            }

            @Override
            public void onTopEmptyRegionClick() {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO: send selected value as a result
    }

}


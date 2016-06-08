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

package eu.power_switch.gui.adapter;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.settings.SettingsItem;

/**
 * Created by Markus on 08.06.2016.
 */
public class SettingsListAdapter extends WearableListView.Adapter {
    private final LayoutInflater mInflater;
    private Context context;
    private ArrayList<SettingsItem> settings;

    public SettingsListAdapter(Context context, ArrayList<SettingsItem> settings) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.settings = settings;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item_setting, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
        SettingsItem settingsItem = settings.get(position);

        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;

        itemViewHolder.icon.setImageDrawable(settingsItem.getIcon());
        itemViewHolder.description.setText(settingsItem.getDescription() + ":");
        itemViewHolder.value.setText(settingsItem.getValueDescription());
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    private static class ItemViewHolder extends WearableListView.ViewHolder {
        private CircledImageView icon;
        private TextView description;
        private TextView value;

        public ItemViewHolder(View itemView) {
            super(itemView);
            icon = (CircledImageView) itemView.findViewById(R.id.circle);
            description = (TextView) itemView.findViewById(R.id.description);
            value = (TextView) itemView.findViewById(R.id.value);
        }
    }
}

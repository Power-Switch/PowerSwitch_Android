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
    private ArrayList<SettingsItem> settings;

    public SettingsListAdapter(Context context, ArrayList<SettingsItem> settings) {
        mInflater = LayoutInflater.from(context);
        this.settings = settings;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int i) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item_setting, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder,
                                 int position) {
        SettingsItem settingsItem = settings.get(position);

        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;

        CircledImageView circledView = itemViewHolder.mCircledImageView;
        circledView.setImageDrawable(settingsItem.getIcon());

        TextView textView = itemViewHolder.mItemTextView;
        textView.setText(settingsItem.getDescription() + ": " + String.valueOf(settingsItem.getValue()));
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private CircledImageView mCircledImageView;
        private TextView mItemTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mCircledImageView = (CircledImageView)
                    itemView.findViewById(R.id.circle);
            mItemTextView = (TextView) itemView.findViewById(R.id.name);
        }
    }
}

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

package eu.power_switch.gui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.CircledImageView;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.view.SettingsListItemLayout;
import eu.power_switch.persistence.settings.SettingsItem;

/**
 * Created by Markus on 08.06.2016.
 */
public class SettingsListAdapter extends WearableRecyclerView.Adapter<SettingsListAdapter.ItemViewHolder> {
    private final LayoutInflater      mInflater;
    private       Context             context;
    private       List<SettingsItem>  settings;
    private       OnItemClickListener onItemClickListener;

    public SettingsListAdapter(Context context, List<SettingsItem> settings) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.settings = settings;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item_setting, null));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder viewHolder, int position) {
        SettingsItem settingsItem = settings.get(position);

        viewHolder.icon.setImageDrawable(settingsItem.getIcon());
        viewHolder.description.setText(settingsItem.getDescription() + ":");
        viewHolder.value.setText(settingsItem.getCurrentValueDescription());
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    public interface OnItemClickListener {
        void onItemClick(ItemViewHolder viewHolder, int position);
    }

    public final class ItemViewHolder extends ButterKnifeWearableViewHolder {

        @BindView(R.id.list_item)
        public SettingsListItemLayout listItemLayout;
        @BindView(R.id.circle)
        public CircledImageView       icon;
        @BindView(R.id.description)
        public TextView               description;
        @BindView(R.id.value)
        public TextView               value;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }

                        onItemClickListener.onItemClick(ItemViewHolder.this, getAdapterPosition());
                    }
                }
            });
        }
    }
}

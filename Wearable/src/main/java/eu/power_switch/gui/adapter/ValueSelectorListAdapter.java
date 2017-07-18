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
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.settings.SingleSelectSettingsItem;

/**
 * Created by Markus on 08.06.2016.
 */
public class ValueSelectorListAdapter extends WearableRecyclerView.Adapter {
    private final LayoutInflater           mInflater;
    private       Context                  context;
    private       List<Integer>            values;
    private       SingleSelectSettingsItem settingsItem;
    private       OnItemClickListener      onItemClickListener;

    public ValueSelectorListAdapter(Context context, SingleSelectSettingsItem settingsItem) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.values = settingsItem.getAllValues();
        this.settingsItem = settingsItem;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public WearableRecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item_value_selector, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Integer value = values.get(position);

        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;

        if (value.equals(settingsItem.getValue())) {
            itemViewHolder.checkmark.setImageDrawable(IconicsHelper.getCheckmarkIcon(context));
        } else {
            itemViewHolder.checkmark.setImageDrawable(null);
        }

        itemViewHolder.value.setText(settingsItem.getValueDescription(value));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public final class ItemViewHolder extends WearableRecyclerView.ViewHolder {
        public CircledImageView checkmark;
        public TextView         value;

        public ItemViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }

                        onItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });

            checkmark = itemView.findViewById(R.id.circle);
            value = itemView.findViewById(R.id.value);
        }
    }
}

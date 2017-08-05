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
import android.support.annotation.NonNull;
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
import eu.power_switch.gui.IconicsHelper;

/**
 * Created by Markus on 08.06.2016.
 */
public class ValueSelectorListAdapter extends WearableRecyclerView.Adapter<ValueSelectorListAdapter.ItemViewHolder> {
    private final LayoutInflater mInflater;

    private Context      context;
    private List<String> descriptions;
    private Integer      currentValueIndex;

    private OnItemClickListener onItemClickListener;

    public ValueSelectorListAdapter(@NonNull Context context, @NonNull List<String> descriptions, @NonNull List<Integer> values,
                                    @NonNull Integer currentValue) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.descriptions = descriptions;

        for (int i = 0; i < values.size(); i++) {
            Integer value = values.get(i);
            if (value.equals(currentValue)) {
                currentValueIndex = i;
                break;
            }
        }

        if (currentValueIndex == null) {
            throw new IllegalArgumentException("No matching value in values found for currentValue parameter!");
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item_value_selector, null));
    }

    @Override
    public void onBindViewHolder(ValueSelectorListAdapter.ItemViewHolder itemViewHolder, int position) {
        if (position == currentValueIndex) {
            itemViewHolder.checkmark.setImageDrawable(IconicsHelper.getCheckmarkIcon(context));
        } else {
            itemViewHolder.checkmark.setImageDrawable(null);
        }

        itemViewHolder.description.setText(descriptions.get(position));
    }

    @Override
    public int getItemCount() {
        return descriptions.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public final class ItemViewHolder extends ButterKnifeWearableViewHolder {
        @BindView(R.id.circle)
        CircledImageView checkmark;
        @BindView(R.id.description)
        TextView         description;

        public ItemViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }

                        // mark the currently selected value
                        int oldIndex = currentValueIndex;
                        currentValueIndex = getAdapterPosition();
                        // and update ui
                        notifyItemChanged(oldIndex);
                        notifyItemChanged(currentValueIndex);

                        onItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
        }
    }
}

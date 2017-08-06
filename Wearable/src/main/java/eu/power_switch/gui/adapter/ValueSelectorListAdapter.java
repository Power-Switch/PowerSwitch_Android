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
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.CircledImageView;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;

/**
 * Created by Markus on 08.06.2016.
 */
public class ValueSelectorListAdapter extends WearableRecyclerView.Adapter<ValueSelectorListAdapter.ItemViewHolder> {
    private final LayoutInflater mInflater;

    private final WearableRecyclerView parent;
    private final List<String>         descriptions;
    private       Integer              currentValueIndex;

    private       OnItemClickListener onItemClickListener;
    private final IconicsDrawable     checkMarkIcon;
    private final IconicsDrawable     emptyIcon;

    public ValueSelectorListAdapter(WearableRecyclerView parent, @NonNull List<String> descriptions, @NonNull List<Integer> values,
                                    @NonNull Integer currentValue) {
        Context context = parent.getContext();
        this.parent = parent;
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

        checkMarkIcon = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_checkbox_marked_circle_outline).sizeDp(24)
                .color(Color.WHITE);
        emptyIcon = new IconicsDrawable(context, CommunityMaterial.Icon.cmd_checkbox_blank_circle_outline).sizeDp(24)
                .color(Color.WHITE);
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
            itemViewHolder.checkMark.setImageDrawable(checkMarkIcon);
        } else {
            itemViewHolder.checkMark.setImageDrawable(emptyIcon);
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
        CircledImageView checkMark;
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

                        parent.scrollToPosition(currentValueIndex);

                        onItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
        }
    }
}

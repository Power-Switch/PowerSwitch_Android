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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedList;

import eu.power_switch.R;
import eu.power_switch.history.HistoryItem;

/**
 * Adapter to visualize Action items in RecyclerView
 * <p/>
 * Created by Markus on 04.12.2015.
 */
public class HistoryItemRecyclerViewAdapter extends RecyclerView.Adapter<HistoryItemRecyclerViewAdapter.ViewHolder> {
    private LinkedList<HistoryItem> historyItems;
    private Context context;


    public HistoryItemRecyclerViewAdapter(Context context, LinkedList<HistoryItem> historyItems) {
        this.historyItems = historyItems;
        this.context = context;
    }

    @Override
    public HistoryItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_history_item, parent, false);
        return new HistoryItemRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final HistoryItemRecyclerViewAdapter.ViewHolder holder, final int position) {
        final HistoryItem historyItem = historyItems.get(position);

        holder.time.setText(historyItem.getTime().getTime().toString());
        holder.description.setText(historyItem.getDescription());

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView time;
        public TextView description;
        public LinearLayout footer;

        public ViewHolder(View itemView) {
            super(itemView);
            this.time = (TextView) itemView.findViewById(R.id.txt_time);
            this.description = (TextView) itemView.findViewById(R.id.txt_description);
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);
        }
    }
}
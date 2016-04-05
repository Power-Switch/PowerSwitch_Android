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

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.phone.call.CallEvent;

/**
 * Adapter to visualize Call items in RecyclerView
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class CallRecyclerViewAdapter extends RecyclerView.Adapter<CallRecyclerViewAdapter.ViewHolder> {
    private ArrayList<CallEvent> callEvents;
    private Context context;

    public CallRecyclerViewAdapter(Context context, ArrayList<CallEvent> callEvents) {
        this.callEvents = callEvents;
        this.context = context;
    }

    @Override
    public CallRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_call, parent, false);
        return new CallRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CallRecyclerViewAdapter.ViewHolder holder, int position) {
        final CallEvent callEvent = callEvents.get(holder.getAdapterPosition());
//        holder.description.setText(call.toString());

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return callEvents.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout footer;

        public ViewHolder(View itemView) {
            super(itemView);
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);
        }
    }
}
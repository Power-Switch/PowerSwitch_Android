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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import eu.power_switch.R;
import eu.power_switch.log.Log;
import eu.power_switch.obj.device.Receiver;

/**
 * * Adapter to visualize Receiver items in RecyclerView
 * <p/>
 * Created by Markus on 13.10.2015.
 */
public class ReceiverNameRecyclerViewAdapter extends RecyclerView.Adapter<ReceiverNameRecyclerViewAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {
    private ArrayList<Receiver> receivers;
    private Context context;

    public ReceiverNameRecyclerViewAdapter(Context context, ArrayList<Receiver> receivers) {
        this.receivers = receivers;
        this.context = context;
    }

    @Override
    public ReceiverNameRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_receiver_name, parent, false);
        return new ReceiverNameRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ReceiverNameRecyclerViewAdapter.ViewHolder holder, int position) {
        final Receiver receiver = receivers.get(position);
        holder.receiverName.setText(receiver.getName());
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return receivers.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Log.d("Moved from " + fromPosition + " to " + toPosition);

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(receivers, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(receivers, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        Log.d("Item " + position + " dismissed");
        receivers.remove(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView receiverName;

        public ViewHolder(View itemView) {
            super(itemView);
            this.receiverName = (TextView) itemView.findViewById(R.id.txt_receiver_name);
        }
    }
}
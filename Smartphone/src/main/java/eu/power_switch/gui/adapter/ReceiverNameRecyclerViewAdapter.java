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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.obj.receiver.Receiver;
import timber.log.Timber;

/**
 * * Adapter to visualize Receiver name items in RecyclerView for reordering
 * <p/>
 * Created by Markus on 13.10.2015.
 */
public class ReceiverNameRecyclerViewAdapter extends SwipeDismissRecyclerViewAdapter<ReceiverNameRecyclerViewAdapter.ViewHolder> {
    private List<Receiver> receivers;

    public ReceiverNameRecyclerViewAdapter(Context context, List<Receiver> receivers) {
        super(context);
        this.receivers = receivers;
    }

    @Override
    public ReceiverNameRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.list_item_receiver_name, parent, false);
        return new ReceiverNameRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ReceiverNameRecyclerViewAdapter.ViewHolder holder, int position) {
        final Receiver receiver = receivers.get(position);
        holder.receiverName.setText(receiver.getName());

        super.onBindViewHolder(holder, position);
    }

    @Override
    protected View getDragHandle(ViewHolder viewHolder) {
        return viewHolder.dragHandle;
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return receivers.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(receivers, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(receivers, i, i - 1);
            }
        }

        super.onItemMove(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        Timber.d("Item " + position + " dismissed");
        receivers.remove(position);

        super.onItemDismiss(position);
    }

    public class ViewHolder extends SwipeDismissViewHolder {
        @BindView(R.id.linear_layout_main)
        LinearLayout mainLayout;
        @BindView(R.id.txt_name)
        TextView     receiverName;
        @BindView(R.id.drag_handle)
        ImageView    dragHandle;

        public ViewHolder(View itemView) {
            super(itemView);
            this.dragHandle.setImageDrawable(IconicsHelper.getReorderHandleIcon(context));
        }

        @Override
        protected View getItemLayout() {
            return mainLayout;
        }

    }
}
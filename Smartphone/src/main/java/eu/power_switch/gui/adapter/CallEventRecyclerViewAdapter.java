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
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.shared.constants.PhoneConstants;

/**
 * Adapter to visualize Call items in RecyclerView
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class CallEventRecyclerViewAdapter extends RecyclerView.Adapter<CallEventRecyclerViewAdapter.ViewHolder> {
    private List<CallEvent> callEvents;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public CallEventRecyclerViewAdapter(Context context, List<CallEvent> callEvents) {
        this.callEvents = callEvents;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public CallEventRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_call_event, parent, false);
        return new CallEventRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CallEventRecyclerViewAdapter.ViewHolder holder, int position) {
        final CallEvent callEvent = callEvents.get(holder.getAdapterPosition());

        String phoneNumbers = "";
        Iterator<String> iterator = callEvent.getPhoneNumbers(PhoneConstants.CallType.INCOMING).iterator();
        while (iterator.hasNext()) {
            phoneNumbers += iterator.next();

            if (iterator.hasNext()) {
                phoneNumbers += "\n";
            }
        }
        holder.phoneNumbers.setText(phoneNumbers);

        for (Action action : callEvent.getActions(PhoneConstants.CallType.INCOMING)) {
            AppCompatTextView textViewActionDescription = new AppCompatTextView(context);
            textViewActionDescription.setText(action.toString());
            textViewActionDescription.setPadding(0, 0, 0, 4);
            holder.linearLayoutActions.addView(textViewActionDescription);
        }

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

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout footer;
        public TextView phoneNumbers;
        public LinearLayout linearLayoutActions;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.phoneNumbers = (TextView) itemView.findViewById(R.id.txt_phoneNumbers);
            this.linearLayoutActions = (LinearLayout) itemView.findViewById(R.id.linearLayout_actions);
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, getLayoutPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(itemView, getLayoutPosition());
                    }
                    return true;
                }
            });
        }
    }
}
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
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.phone.sms.SmsEvent;
import eu.power_switch.shared.constants.PhoneConstants;

/**
 * Adapter to visualize SmsEvent items in RecyclerView
 * <p/>
 * Created by Markus on 05.04.2016.
 */
public class SmsEventRecyclerViewAdapter extends RecyclerView.Adapter<SmsEventRecyclerViewAdapter.ViewHolder> {
    private List<SmsEvent> smsEvents;
    private Context        context;

    public SmsEventRecyclerViewAdapter(Context context, List<SmsEvent> smsEvents) {
        this.smsEvents = smsEvents;
        this.context = context;
    }

    @Override
    public SmsEventRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.list_item_sms_event, parent, false);
        return new SmsEventRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SmsEventRecyclerViewAdapter.ViewHolder holder, int position) {
        final SmsEvent smsEvent = smsEvents.get(position);

        String phoneNumbers = "";
        Iterator<String> iterator = smsEvent.getPhoneNumbers(PhoneConstants.SmsType.INCOMING)
                .iterator();
        while (iterator.hasNext()) {
            phoneNumbers += iterator.next();

            if (iterator.hasNext()) {
                phoneNumbers += "\n";
            }
        }
        holder.phoneNumbers.setText(phoneNumbers);

        for (Action action : smsEvent.getActions(PhoneConstants.SmsType.INCOMING)) {
            AppCompatTextView textViewActionDescription = new AppCompatTextView(context);
            // TODO:
//            textViewActionDescription.setText(Action.createReadableString(context, action, persistenceHandler));
            textViewActionDescription.setPadding(0, 0, 0, 4);
            holder.linearLayoutActions.addView(textViewActionDescription);
        }

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return smsEvents.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public class ViewHolder extends ButterKnifeViewHolder {
        @BindView(R.id.list_footer)
        LinearLayout footer;
        @BindView(R.id.txt_phoneNumbers)
        TextView     phoneNumbers;
        @BindView(R.id.linearLayout_actions)
        LinearLayout linearLayoutActions;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
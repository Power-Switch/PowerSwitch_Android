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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.power_switch.R;
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.phone.Contact;

/**
 * Adapter to visualize Contact items in RecyclerView
 * <p/>
 * Created by Markus on 04.12.2015.
 */
public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<Contact> contacts;
    private Set<String> checkedNumbers;

    private CheckBoxInteractionListener checkBoxInteractionListener;

    public ContactRecyclerViewAdapter(Context context, List<Contact> contacts, Set<String> checkedNumbers) {
        this.context = context;
        this.contacts = contacts;
        this.checkedNumbers = checkedNumbers;
    }

    public void setCheckBoxInteractionListener(CheckBoxInteractionListener checkBoxInteractionListener) {
        this.checkBoxInteractionListener = checkBoxInteractionListener;
    }

    @Override

    public ContactRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_contact, parent, false);
        return new ContactRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ContactRecyclerViewAdapter.ViewHolder holder, int position) {
        final Contact contact = contacts.get(position);
        holder.name.setText(contact.getName());

        holder.numbers.removeAllViews();
        Iterator<String> iterator = contact.getPhoneNumbers().iterator();
        while (iterator.hasNext()) {
            final String number = iterator.next();

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            CheckBox checkBox = new CheckBox(context);
            checkBox.setTag(number);
            if (checkedNumbers.contains(number)) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }

            checkBox.setOnTouchListener(checkBoxInteractionListener);
            checkBox.setOnCheckedChangeListener(checkBoxInteractionListener);

            TextView phoneNumber = new TextView(context);
            phoneNumber.setText(number);

            linearLayout.addView(checkBox);
            linearLayout.addView(phoneNumber);

            holder.numbers.addView(linearLayout);
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
        return contacts.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public LinearLayout numbers;
        public LinearLayout footer;

        public ViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.txt_name);
            this.numbers = (LinearLayout) itemView.findViewById(R.id.linearLayout_numbers);
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);
        }
    }
}
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
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * * Adapter to visualize Gateway items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class ApartmentRecyclerViewAdapter extends RecyclerView.Adapter<ApartmentRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Apartment> apartments;
    private Context context;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public ApartmentRecyclerViewAdapter(Context context, ArrayList<Apartment> apartments) {
        this.apartments = apartments;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public ApartmentRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_apartment, parent, false);
        return new ApartmentRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ApartmentRecyclerViewAdapter.ViewHolder holder, int position) {
        final Apartment apartment = apartments.get(position);

        holder.active.setChecked(apartment.isActive());
        holder.active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmartphonePreferencesHandler.setCurrentApartmentId(apartment.getId());

                for (Apartment currentApartment : apartments) {
                    if (currentApartment.getId().equals(apartment.getId())) {
                        currentApartment.setActive(true);
                    } else {
                        currentApartment.setActive(false);
                    }
                }

                ApartmentFragment.sendApartmentChangedBroadcast(context);
            }
        });

        holder.name.setText(apartment.getName());

        String contentSummary = "";
        contentSummary += context.getString(R.string.rooms) + ": " + apartment.getRooms().size() + "\n";
        contentSummary += context.getString(R.string.scenes) + ": " + apartment.getScenes().size() + "\n";
        contentSummary += context.getString(R.string.associated_gateways) + ": " + apartment.getAssociatedGateways()
                .size();
        holder.contentSummary.setText(contentSummary);

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return apartments.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RadioButton active;
        public TextView name;
        public TextView contentSummary;
        public LinearLayout footer;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.active = (RadioButton) itemView.findViewById(R.id.radioButton_active);
            this.name = (TextView) itemView.findViewById(R.id.txt_apartment_name);
            this.contentSummary = (TextView) itemView.findViewById(R.id.txt_content_summary);
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
                    return false;
                }
            });
        }
    }
}

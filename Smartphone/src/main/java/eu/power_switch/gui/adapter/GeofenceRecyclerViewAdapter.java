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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.permission.PermissionHelper;

/**
 * * Adapter to visualize Geofence items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class GeofenceRecyclerViewAdapter extends RecyclerView.Adapter<GeofenceRecyclerViewAdapter.ViewHolder> {
    private GeofenceApiHandler geofenceApiHandler;
    private ArrayList<Geofence> geofences;
    private Context context;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public GeofenceRecyclerViewAdapter(Context context, ArrayList<Geofence> geofences, GeofenceApiHandler geofenceApiHandler) {
        this.geofences = geofences;
        this.context = context;
        this.geofenceApiHandler = geofenceApiHandler;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public GeofenceRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_geofence, parent, false);
        return new GeofenceRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GeofenceRecyclerViewAdapter.ViewHolder holder, int position) {
        final Geofence geofence = geofences.get(position);

        switch (geofence.getState()) {
            case Geofence.STATE_INSIDE:
                holder.geofenceState.setVisibility(View.VISIBLE);
                holder.geofenceState.setText(R.string.inside);
                break;
            case Geofence.STATE_OUTSIDE:
                holder.geofenceState.setVisibility(View.VISIBLE);
                holder.geofenceState.setText(R.string.outside);
                break;
            case Geofence.STATE_NONE:
                holder.geofenceState.setVisibility(View.GONE);
                break;
        }
        holder.geofenceName.setText(geofence.getName());
        holder.geofenceSwitchActive.setChecked(geofence.isActive());
        holder.geofenceSwitchActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // check if user pressed the button
                if (buttonView.isPressed()) {
                    try {
                        if (isChecked) {
                            DatabaseHandler.enableGeofence(geofence.getId());
                            geofenceApiHandler.addGeofence(geofence);
                        } else {
                            DatabaseHandler.disableGeofence(geofence.getId());
                            geofenceApiHandler.removeGeofence(geofence.getId());
                        }
                        geofence.setActive(isChecked);
                    } catch (Exception e) {
                        Log.e(e);
                        StatusMessageHandler.showInfoMessage(context, R.string.error_enabling_geofence, 5000);
                    }
                }
            }
        });
        if (!PermissionHelper.isLocationPermissionAvailable(context)) {
            holder.geofenceSwitchActive.setEnabled(false);
        } else {
            holder.geofenceSwitchActive.setEnabled(true);
        }

        holder.geofenceSnapshot.setImageBitmap(geofence.getSnapshot());

        holder.linearLayoutEnterActions.removeAllViews();
        for (Action action : geofence.getActions(Geofence.EventType.ENTER)) {
            AppCompatTextView textViewActionDescription = new AppCompatTextView(context);
            textViewActionDescription.setText(action.toString());
            textViewActionDescription.setPadding(0, 0, 0, 4);
            holder.linearLayoutEnterActions.addView(textViewActionDescription);
        }

        holder.linearLayoutExitActions.removeAllViews();
        for (Action action : geofence.getActions(Geofence.EventType.EXIT)) {
            AppCompatTextView textViewActionDescription = new AppCompatTextView(context);
            textViewActionDescription.setText(action.toString());
            textViewActionDescription.setPadding(0, 0, 0, 4);
            holder.linearLayoutExitActions.addView(textViewActionDescription);
        }

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return geofences.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView geofenceState;
        public TextView geofenceName;
        public android.support.v7.widget.SwitchCompat geofenceSwitchActive;
        public ImageView geofenceSnapshot;
        public LinearLayout linearLayoutEnterActions;
        public LinearLayout linearLayoutExitActions;

        public LinearLayout footer;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.geofenceState = (TextView) itemView.findViewById(R.id.txt_geofence_state);
            this.geofenceName = (TextView) itemView.findViewById(R.id.txt_geofence_name);
            this.geofenceSwitchActive = (android.support.v7.widget.SwitchCompat) itemView.findViewById(R.id.switch_geofence_active);
            this.geofenceSnapshot = (ImageView) itemView.findViewById(R.id.imageView_locationSnapshot);
            this.linearLayoutEnterActions = (LinearLayout) itemView.findViewById(R.id.linearLayout_enterActions);
            this.linearLayoutExitActions = (LinearLayout) itemView.findViewById(R.id.linearLayout_exitActions);
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(itemView, getAdapterPosition());
                    }
                    return true;
                }
            });
        }
    }
}

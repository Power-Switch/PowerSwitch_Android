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

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.shared.permission.PermissionHelper;
import timber.log.Timber;

/**
 * * Adapter to visualize Geofence items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class GeofenceRecyclerViewAdapter extends RecyclerView.Adapter<GeofenceRecyclerViewAdapter.ViewHolder> {
    private GeofenceApiHandler   geofenceApiHandler;
    private ArrayList<Geofence>  geofences;
    private Context              context;
    private PersistenceHandler   persistenceHandler;
    private StatusMessageHandler statusMessageHandler;

    private OnItemClickListener     onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public GeofenceRecyclerViewAdapter(Context context, ArrayList<Geofence> geofences, GeofenceApiHandler geofenceApiHandler,
                                       PersistenceHandler persistenceHandler, StatusMessageHandler statusMessageHandler) {
        this.geofences = geofences;
        this.context = context;
        this.geofenceApiHandler = geofenceApiHandler;
        this.persistenceHandler = persistenceHandler;
        this.statusMessageHandler = statusMessageHandler;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public GeofenceRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.list_item_geofence, parent, false);
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
                            persistenceHandler.enableGeofence(geofence.getId());
                            geofenceApiHandler.addGeofence(geofence);
                        } else {
                            persistenceHandler.disableGeofence(geofence.getId());
                            geofenceApiHandler.removeGeofence(geofence.getId());
                        }
                        geofence.setActive(isChecked);
                    } catch (Exception e) {
                        Timber.e(e);
                        statusMessageHandler.showInfoMessage(context, R.string.error_enabling_geofence, 5000);
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

    public class ViewHolder extends ButterKnifeViewHolder {
        @BindView(R.id.txt_geofence_state)
        TextView                               geofenceState;
        @BindView(R.id.txt_geofence_name)
        TextView                               geofenceName;
        @BindView(R.id.switch_geofence_active)
        android.support.v7.widget.SwitchCompat geofenceSwitchActive;
        @BindView(R.id.imageView_locationSnapshot)
        ImageView                              geofenceSnapshot;
        @BindView(R.id.linearLayout_enterActions)
        LinearLayout                           linearLayoutEnterActions;
        @BindView(R.id.linearLayout_exitActions)
        LinearLayout                           linearLayoutExitActions;

        @BindView(R.id.list_footer)
        LinearLayout footer;

        public ViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }
                        onItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return false;
                        }
                        onItemLongClickListener.onItemLongClick(itemView, getAdapterPosition());
                    }
                    return true;
                }
            });
        }
    }
}

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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.log.Log;

/**
 * * Adapter to visualize Gateway items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class GatewayRecyclerViewAdapter extends RecyclerView.Adapter<GatewayRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Gateway> gateways;
    private Context context;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public GatewayRecyclerViewAdapter(Context context, ArrayList<Gateway> gateways) {
        this.gateways = gateways;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public GatewayRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_gateway, parent, false);
        return new GatewayRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GatewayRecyclerViewAdapter.ViewHolder holder, final int position) {
        final Gateway gateway = gateways.get(position);

        holder.gatewayName.setText(gateway.getName());
        holder.gatewayModel.setText(gateway.getModelAsString());
        holder.gatewayAddress.setText(gateway.getHost());
        holder.gatewayPort.setText(String.format("%d", gateway.getPort()));
        holder.gatewaySwitchStatus.setChecked(gateway.isActive());
        holder.gatewaySwitchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // check if user pressed the button
                if (buttonView.isPressed()) {
                    try {
                        if (isChecked) {
                            DatabaseHandler.enableGateway(gateway.getId());
                        } else {
                            DatabaseHandler.disableGateway(gateway.getId());
                        }
                        gateway.setActive(isChecked);
                    } catch (Exception e) {
                        Log.e(e);
                        StatusMessageHandler.showStatusMessage(context, R.string.error_enabling_gateway, 5000);
                    }
                }
            }
        });

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return gateways.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView gatewayName;
        public TextView gatewayModel;
        public TextView gatewayAddress;
        public TextView gatewayPort;
        public android.support.v7.widget.SwitchCompat gatewaySwitchStatus;
        public LinearLayout footer;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.gatewayName = (TextView) itemView.findViewById(R.id.txt_gateway_name);
            this.gatewayModel = (TextView) itemView.findViewById(R.id.txt_gateway_model);
            this.gatewayAddress = (TextView) itemView.findViewById(R.id.txt_gateway_address);
            this.gatewayPort = (TextView) itemView.findViewById(R.id.txt_gateway_port);
            this.gatewaySwitchStatus = (android.support.v7.widget.SwitchCompat) itemView.findViewById(R.id.switch_gateway_status);
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

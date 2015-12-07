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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.action.Action;

/**
 * Adapter to visualize Action items in RecyclerView
 * <p/>
 * Created by Markus on 04.12.2015.
 */
public class ActionRecyclerViewAdapter extends RecyclerView.Adapter<ActionRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Action> actions;
    private Context context;
    private OnItemClickListener onItemClickListener;


    public ActionRecyclerViewAdapter(Context context, ArrayList<Action> actions) {
        this.actions = actions;
        this.context = context;
    }

    public void setOnDeleteClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ActionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_action, parent, false);
        return new ActionRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ActionRecyclerViewAdapter.ViewHolder holder, final int position) {
        final Action action = actions.get(position);
        holder.description.setText(action.toString());
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return actions.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView description;
        public FloatingActionButton deleteTimerActionFAB;

        public ViewHolder(View itemView) {
            super(itemView);
            this.description = (TextView) itemView.findViewById(R.id.txt_action_description);
            this.deleteTimerActionFAB = (FloatingActionButton) itemView.findViewById(R.id.delete_timer_action_fab);

            this.deleteTimerActionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(deleteTimerActionFAB, getLayoutPosition());
                    }
                }
            });
        }
    }
}
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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.shared.butterknife.ButterKnifeViewHolder;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import eu.power_switch.shared.wearable.dataevents.SceneDataEvent;

/**
 * Created by Markus on 15.08.2015.
 */
public class SceneRecyclerViewAdapter extends RecyclerView.Adapter<SceneRecyclerViewAdapter.ViewHolder> {

    private Context                    context;
    private List<SceneDataEvent>       scenes;
    private DataApiHandler             dataApiHandler;
    private RecyclerView               parentRecyclerView;
    private WearablePreferencesHandler wearablePreferencesHandler;

    public SceneRecyclerViewAdapter(Context context, RecyclerView parentRecyclerView, List<SceneDataEvent> scenes, DataApiHandler dataApiHandler,
                                    WearablePreferencesHandler wearablePreferencesHandler) {
        this.context = context;
        this.scenes = scenes;
        this.parentRecyclerView = parentRecyclerView;
        this.dataApiHandler = dataApiHandler;
        this.wearablePreferencesHandler = wearablePreferencesHandler;
    }

    @Override
    public SceneRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the custom layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_scene, parent, false);
        // Return a new holder instance
        return new SceneRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SceneRecyclerViewAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        final SceneDataEvent scene = scenes.get(position);

        // Set item views based on the data model
        holder.sceneName.setText(scene.getName());
        holder.buttonActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vibration Feedback
                if (wearablePreferencesHandler.getValue(WearablePreferencesHandler.VIBRATE_ON_BUTTON_PRESS)) {
                    int duration = wearablePreferencesHandler.getValue(WearablePreferencesHandler.VIBRATION_DURATION);
                    VibrationHandler.vibrate(context, duration);
                }

                String actionString = DataApiHandler.buildSceneActionString(scene);
                dataApiHandler.sendSceneActionTrigger(actionString);
            }
        });

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return scenes.size();
    }

    public class ViewHolder extends ButterKnifeViewHolder {

        @BindView(R.id.textView_scene_name)
        TextView              sceneName;
        @BindView(R.id.button_Activate)
        android.widget.Button buttonActivate;
        @BindView(R.id.list_footer)
        LinearLayout          footer;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

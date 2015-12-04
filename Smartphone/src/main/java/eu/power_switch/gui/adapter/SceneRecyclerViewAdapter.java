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
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.api.IntentReceiver;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.settings.SettingsTabFragment;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;

/**
 * * Adapter to visualize Scene items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class SceneRecyclerViewAdapter extends RecyclerView.Adapter<SceneRecyclerViewAdapter.ViewHolder> {
    private RecyclerViewFragment recyclerViewFragment;
    private ArrayList<Scene> scenes;
    private FragmentActivity fragmentActivity;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public SceneRecyclerViewAdapter(RecyclerViewFragment recyclerViewFragment, FragmentActivity fragmentActivity,
                                    ArrayList<Scene> scenes) {
        this.recyclerViewFragment = recyclerViewFragment;
        this.scenes = scenes;
        this.fragmentActivity = fragmentActivity;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public SceneRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(fragmentActivity).inflate(R.layout.list_item_scene, parent, false);
        return new SceneRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SceneRecyclerViewAdapter.ViewHolder holder, final int position) {
        final Scene scene = scenes.get(position);

        String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) fragmentActivity.getSystemService(inflaterString);

        holder.sceneName.setText(scene.getName());
        holder.sceneName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.linearLayoutSceneItems.getVisibility() == View.VISIBLE) {
                    holder.linearLayoutSceneItems.setVisibility(View.GONE);
                } else {
                    holder.linearLayoutSceneItems.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.sceneName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(v, position);
                }
                return true;
            }
        });

        holder.buttonActivateScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SmartphonePreferencesHandler.getVibrateOnButtonPress()) {
                    VibrationHandler.vibrate(fragmentActivity, SmartphonePreferencesHandler.getVibrationDuration());
                }

                List<Gateway> activeGateways = DatabaseHandler.getAllGateways(true);
                if (activeGateways.isEmpty()) {
                    StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.no_active_gateway,
                            R.string.open_settings, new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.addToBackstack(SettingsTabFragment.class, fragmentActivity
                                            .getString(R.string.menu_settings));
                                    SettingsTabFragment settingsTabFragment = new SettingsTabFragment();
                                    Bundle arguments = new Bundle();
                                    arguments.putInt("tabIndex", SettingsConstants.GATEWAYS_TAB_INDEX);
                                    settingsTabFragment.setArguments(arguments);
                                    fragmentActivity.getSupportFragmentManager()
                                            .beginTransaction()
                                            .setCustomAnimations(R.anim
                                                    .slide_in_right, R.anim.slide_out_left, android.R.anim
                                                    .slide_in_left, android.R.anim.slide_out_right)
                                            .replace(R.id.mainContentFrameLayout, settingsTabFragment)
                                            .addToBackStack(null).commit();
                                }
                            }, Snackbar.LENGTH_LONG);
                    return;
                }

                IntentReceiver.parseActionIntent(fragmentActivity, IntentReceiver
                        .createSceneIntent(scene.getName()));
            }
        });

        // clear previous items
        holder.linearLayoutSceneItems.removeAllViews();
        // hide setup by default
        holder.linearLayoutSceneItems.setVisibility(View.GONE);
        // add current setup
        for (final SceneItem sceneItem : scene.getSceneItems()) {
            // create a new receiverRow for our current receiver and add it
            // to our table of all devices of our current room
            // the row will contain the device name and all buttons
            LinearLayout receiverRow = new LinearLayout(fragmentActivity);
            receiverRow.setOrientation(LinearLayout.HORIZONTAL);
            holder.linearLayoutSceneItems.addView(receiverRow);

            // setup TextView to display receiver name
            AppCompatTextView receiverName = new AppCompatTextView(fragmentActivity);
            receiverName.setText(sceneItem.getReceiver().getName());
            receiverName.setTextSize(18);
            receiverName.setGravity(Gravity.CENTER_VERTICAL);
            receiverRow.addView(receiverName, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT, 1.0f));

            TableLayout buttonLayout = new TableLayout(fragmentActivity);
            receiverRow.addView(buttonLayout, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));

            int buttonsPerRow;
            if (sceneItem.getReceiver().getButtons().size() % 3 == 0) {
                buttonsPerRow = 3;
            } else {
                buttonsPerRow = 2;
            }

            int i = 0;
            TableRow buttonRow = null;
            final ArrayList<AppCompatButton> buttonList = new ArrayList<>();
            for (final Button button : sceneItem.getReceiver().getButtons()) {
                final AppCompatButton buttonView = (AppCompatButton) inflater.inflate(R.layout.simple_button,
                        buttonRow, false);
                buttonList.add(buttonView);
                buttonView.setText(button.getName());
                buttonView.setEnabled(false);

                final int accentColor = ContextCompat.getColor(fragmentActivity, R.color.accent_blue_a700);
                if (sceneItem.getActiveButton().equals(button)) {
                    buttonView.setTextColor(accentColor);
                }

                if (i == 0 || i % buttonsPerRow == 0) {
                    buttonRow = new TableRow(fragmentActivity);
                    buttonRow.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT));
                    buttonRow.addView(buttonView);
                    buttonLayout.addView(buttonRow);
                } else {
                    buttonRow.addView(buttonView);
                }

                i++;
            }
        }

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return scenes.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView sceneName;
        public AppCompatButton buttonActivateScene;
        public LinearLayout linearLayoutSceneItems;
        public LinearLayout footer;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.sceneName = (AppCompatTextView) itemView.findViewById(R.id.txt_scene_name);
            this.buttonActivateScene = (AppCompatButton) itemView.findViewById(R.id.btn_activate_scene);
            this.linearLayoutSceneItems = (LinearLayout) itemView.findViewById(R.id.layout_of_scene_items);
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

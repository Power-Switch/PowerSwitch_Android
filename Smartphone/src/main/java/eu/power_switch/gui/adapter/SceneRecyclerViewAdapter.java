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
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
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
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.persistence.sqlite.handler.PersistanceHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;

/**
 * * Adapter to visualize Scene items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class SceneRecyclerViewAdapter extends RecyclerView.Adapter<SceneRecyclerViewAdapter.ViewHolder> {
    private final RecyclerViewFragment recyclerViewFragment;
    private final ArrayList<Scene>     scenes;
    private final FragmentActivity     fragmentActivity;
    private final PersistanceHandler   persistanceHandler;
    private final ActionHandler        actonHandler;

    private OnItemClickListener     onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public SceneRecyclerViewAdapter(RecyclerViewFragment recyclerViewFragment, FragmentActivity fragmentActivity, ArrayList<Scene> scenes,
                                    ActionHandler actionHandler, PersistanceHandler persistanceHandler) {
        this.recyclerViewFragment = recyclerViewFragment;
        this.scenes = scenes;
        this.fragmentActivity = fragmentActivity;
        this.actonHandler = actionHandler;
        this.persistanceHandler = persistanceHandler;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public SceneRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(fragmentActivity)
                .inflate(R.layout.list_item_scene, parent, false);
        return new SceneRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Scene scene = scenes.get(position);

        String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater       = (LayoutInflater) fragmentActivity.getSystemService(inflaterString);

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
                    if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                        return false;
                    }
                    onItemLongClickListener.onItemLongClick(v, holder.getAdapterPosition());
                }
                return true;
            }
        });

        holder.buttonActivateScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS)) {
                    VibrationHandler.vibrate(fragmentActivity,
                            SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION));
                }

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        actonHandler.execute(scene);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            Receiver receiver;
            try {
                receiver = persistanceHandler.getReceiver(sceneItem.getReceiverId());
            } catch (Exception e) {
                throw new RuntimeException();
            }

            // setup TextView to display receiver name
            AppCompatTextView receiverName = new AppCompatTextView(fragmentActivity);
            receiverName.setText(receiver.getName());
            receiverName.setTextSize(18);
            receiverName.setTextColor(ThemeHelper.getThemeAttrColor(fragmentActivity, android.R.attr.textColorPrimary));
            receiverName.setGravity(Gravity.CENTER_VERTICAL);
            receiverRow.addView(receiverName, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1.0f));

            TableLayout buttonLayout = new TableLayout(fragmentActivity);
            receiverRow.addView(buttonLayout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            int buttonsPerRow;
            if (receiver.getButtons()
                    .size() % 3 == 0) {
                buttonsPerRow = 3;
            } else {
                buttonsPerRow = 2;
            }

            int      i         = 0;
            TableRow buttonRow = null;
            for (final Button button : receiver.getButtons()) {
                final android.widget.Button buttonView = (android.widget.Button) inflater.inflate(R.layout.simple_button, buttonRow, false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    buttonView.setElevation(0);
                    buttonView.setStateListAnimator(null);
                }
                buttonView.setText(button.getName());
                buttonView.setEnabled(false);

                final int accentColor   = ThemeHelper.getThemeAttrColor(fragmentActivity, R.attr.colorAccent);
                final int inactiveColor = ThemeHelper.getThemeAttrColor(fragmentActivity, R.attr.textColorInactive);
                if (sceneItem.getButtonId()
                        .equals(button.getId())) {
                    buttonView.setTextColor(accentColor);
                } else {
                    buttonView.setTextColor(inactiveColor);
                }

                if (i == 0 || i % buttonsPerRow == 0) {
                    buttonRow = new TableRow(fragmentActivity);
                    buttonRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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

    public class ViewHolder extends ButterKnifeViewHolder {
        @BindView(R.id.txt_scene_name)
        TextView              sceneName;
        @BindView(R.id.btn_activate_scene)
        android.widget.Button buttonActivateScene;
        @BindView(R.id.layout_of_scene_items)
        LinearLayout          linearLayoutSceneItems;
        @BindView(R.id.list_footer)
        LinearLayout          footer;

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

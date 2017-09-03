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
import android.content.res.ColorStateList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.butterknife.ButterKnifeViewHolder;
import eu.power_switch.shared.constants.DatabaseConstants;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;
import eu.power_switch.shared.wearable.dataevents.ButtonDataEvent;
import eu.power_switch.shared.wearable.dataevents.ReceiverDataEvent;
import eu.power_switch.shared.wearable.dataevents.RoomDataEvent;

/**
 * Created by Markus on 15.08.2015.
 */
public class RoomRecyclerViewAdapter extends RecyclerView.Adapter<RoomRecyclerViewAdapter.ViewHolder> {

    private Context                    context;
    private List<RoomDataEvent>        rooms;
    private DataApiHandler             dataApiHandler;
    private RecyclerView               parentRecyclerView;
    private WearablePreferencesHandler wearablePreferencesHandler;

    public RoomRecyclerViewAdapter(Context context, RecyclerView parentRecyclerView, List<RoomDataEvent> rooms, DataApiHandler dataApiHandler,
                                   WearablePreferencesHandler wearablePreferencesHandler) {
        this.rooms = rooms;
        this.context = context;
        this.parentRecyclerView = parentRecyclerView;
        this.dataApiHandler = dataApiHandler;
        this.wearablePreferencesHandler = wearablePreferencesHandler;
    }

    @Override
    public RoomRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_room, parent, false);
        return new RoomRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RoomRecyclerViewAdapter.ViewHolder holder, int position) {
        final RoomDataEvent room = rooms.get(position);

        String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) parentRecyclerView.getContext()
                .getSystemService(inflaterString);

        holder.roomName.setText(room.getName());

        if (room.isCollapsed()) {
            holder.linearLayoutOfReceivers.setVisibility(View.GONE);
            holder.linearLayout_AllOnOffButtons.setVisibility(View.VISIBLE);
        } else {
            holder.linearLayoutOfReceivers.setVisibility(View.VISIBLE);
            holder.linearLayout_AllOnOffButtons.setVisibility(View.GONE);
        }

        holder.roomName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.linearLayoutOfReceivers.getVisibility() == View.VISIBLE) {
                    room.setCollapsed(true);
                    holder.linearLayoutOfReceivers.setVisibility(View.GONE);
                    holder.linearLayout_AllOnOffButtons.setVisibility(View.VISIBLE);
                } else {
                    room.setCollapsed(false);
                    holder.linearLayoutOfReceivers.setVisibility(View.VISIBLE);
                    holder.linearLayout_AllOnOffButtons.setVisibility(View.GONE);
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) parentRecyclerView.getLayoutManager();
                    linearLayoutManager.smoothScrollToPosition(parentRecyclerView, new RecyclerView.State(), holder.getAdapterPosition());
                }
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vibration Feedback
                giveVibrationFeedback();

                android.widget.Button button = (android.widget.Button) v;

                long buttonId;
                if (button.getId() == R.id.button_AllOn) {
                    buttonId = DatabaseConstants.BUTTON_ON_ID;
                } else if (button.getId() == R.id.button_AllOff) {
                    buttonId = DatabaseConstants.BUTTON_OFF_ID;
                } else {
                    buttonId = -1;
                }

                String actionString = DataApiHandler.buildRoomActionString(room, buttonId);
                dataApiHandler.sendRoomActionTrigger(actionString);

                for (ReceiverDataEvent receiver : room.getReceiverDataEvents()) {
                    for (ButtonDataEvent currentButton : receiver.getButtonDataEvents()) {
                        if (button.getText()
                                .equals(currentButton.getName())) {
                            receiver.setLastActivatedButtonId(currentButton.getId());
                            break;
                        }
                    }
                }

                if (wearablePreferencesHandler.getValue(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
                    notifyDataSetChanged();
                }
            }
        };

        holder.buttonAllOff.setOnClickListener(onClickListener);
        holder.buttonAllOn.setOnClickListener(onClickListener);


        // clear previous items
        holder.linearLayoutOfReceivers.removeAllViews();
        // add items
        for (final ReceiverDataEvent receiver : room.getReceiverDataEvents()) {
            LinearLayout receiverLayout = (LinearLayout) inflater.inflate(R.layout.list_item_receiver, holder.linearLayoutOfReceivers, false);
            holder.linearLayoutOfReceivers.addView(receiverLayout);

            // setup TextView to display device name
            TextView receiverName = receiverLayout.findViewById(R.id.textView_receiver_name);
            receiverName.setText(receiver.getName());
            receiverName.setTextSize(18);

            // Setup Buttons
            TableLayout buttonLayout = receiverLayout.findViewById(R.id.buttonLayout);

            int                                    buttonsPerRow = 2;
            int                                    i             = 0;
            final ArrayList<android.widget.Button> buttonViews   = new ArrayList<>();
            TableRow                               buttonRow     = null;
            for (final ButtonDataEvent button : receiver.getButtonDataEvents()) {
                android.widget.Button buttonView = (android.widget.Button) inflater.inflate(R.layout.standard_button_wear, buttonRow, false);
                buttonViews.add(buttonView);
                final ColorStateList defaultTextColor = buttonView.getTextColors(); //save original colors
                buttonView.setText(button.getName());

                final int accentColor = ThemeHelper.getThemeAttrColor(context, R.attr.colorAccent);

                if (button.getId() == receiver.getLastActivatedButtonId() && wearablePreferencesHandler.getValue(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
                    buttonView.setTextColor(accentColor);
                }

                buttonView.setOnClickListener(new android.widget.Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Vibration Feedback
                        giveVibrationFeedback();

                        // Send Action to Smartphone app
                        String actionString = DataApiHandler.buildReceiverActionString(room, receiver, button);
                        dataApiHandler.sendReceiverActionTrigger(actionString);

                        receiver.setLastActivatedButtonId(button.getId());
                        if (wearablePreferencesHandler.getValue(WearablePreferencesHandler.HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
                            for (android.widget.Button button : buttonViews) {
                                if (button != v) {
                                    button.setTextColor(defaultTextColor);
                                } else {
                                    button.setTextColor(accentColor);
                                }
                            }
                        }
                    }
                });

                if (i == 0 || i % buttonsPerRow == 0) {
                    buttonRow = new TableRow(context);
                    buttonRow.setGravity(Gravity.CENTER);
                    buttonRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    buttonRow.addView(buttonView);
                    buttonLayout.addView(buttonRow);
                } else {
                    buttonRow.addView(buttonView);
                }

                i++;
            }
        }

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    private void giveVibrationFeedback() {
        if (wearablePreferencesHandler.getValue(WearablePreferencesHandler.VIBRATE_ON_BUTTON_PRESS)) {
            int duration = wearablePreferencesHandler.getValue(WearablePreferencesHandler.VIBRATION_DURATION);
            VibrationHandler.vibrate(context, duration);
        }
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class ViewHolder extends ButterKnifeViewHolder {

        @BindView(R.id.textView_room_name)
        TextView              roomName;
        @BindView(R.id.linearLayout_AllOnOffButtons)
        LinearLayout          linearLayout_AllOnOffButtons;
        @BindView(R.id.button_AllOn)
        android.widget.Button buttonAllOn;
        @BindView(R.id.button_AllOff)
        android.widget.Button buttonAllOff;
        @BindView(R.id.layout_of_receivers)
        LinearLayout          linearLayoutOfReceivers;
        @BindView(R.id.list_footer)
        LinearLayout          footer;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

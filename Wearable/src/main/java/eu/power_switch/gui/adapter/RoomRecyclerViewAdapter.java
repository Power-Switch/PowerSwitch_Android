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
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
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

import eu.power_switch.R;
import eu.power_switch.gui.ThemeHelper;
import eu.power_switch.network.DataApiHandler;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Receiver;
import eu.power_switch.obj.Room;
import eu.power_switch.shared.constants.DatabaseConstants;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;
import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Created by Markus on 15.08.2015.
 */
public class RoomRecyclerViewAdapter extends RecyclerView.Adapter<RoomRecyclerViewAdapter.ViewHolder> {

    // Store a member variable for the users
    private ArrayList<Room> rooms;
    private Context context;
    private DataApiHandler dataApiHandler;
    private RecyclerView parentRecyclerView;

    // Pass in the context and users array into the constructor
    public RoomRecyclerViewAdapter(Context context, RecyclerView parentRecyclerView, ArrayList<Room> rooms,
                                   DataApiHandler dataApiHandler) {
        this.rooms = rooms;
        this.context = context;
        this.parentRecyclerView = parentRecyclerView;
        this.dataApiHandler = dataApiHandler;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RoomRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the custom layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_room__round, parent, false);
        // Return a new holder instance
        return new RoomRecyclerViewAdapter.ViewHolder(itemView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final RoomRecyclerViewAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        final Room room = rooms.get(position);

        String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) parentRecyclerView.getContext().getSystemService(inflaterString);

        // Set item views based on the data model
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
                if (WearablePreferencesHandler.getVibrateOnButtonPress()) {
                    VibrationHandler.vibrate(context, WearablePreferencesHandler.getVibrationDuration());
                }

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

                for (Receiver receiver : room.getReceivers()) {
                    for (Button currentButton : receiver.getButtons()) {
                        if (button.getText().equals(currentButton.getName())) {
                            receiver.setLastActivatedButtonId(currentButton.getId());
                            break;
                        }
                    }
                }

                if (WearablePreferencesHandler.getHighlightLastActivatedButton()) {
                    notifyDataSetChanged();
                }
            }
        };

        holder.buttonAllOff.setOnClickListener(onClickListener);
        holder.buttonAllOn.setOnClickListener(onClickListener);


        // clear previous items
        holder.linearLayoutOfReceivers.removeAllViews();
        // add items
        for (final Receiver receiver : room.getReceivers()) {
            LinearLayout receiverLayout = (LinearLayout) inflater.inflate(R.layout.list_item_receiver, holder
                    .linearLayoutOfReceivers, false);
            holder.linearLayoutOfReceivers.addView(receiverLayout);

            // setup TextView to display device name
            TextView receiverName = (TextView) receiverLayout.findViewById(R.id.textView_receiver_name);
            receiverName.setText(receiver.getName());
            receiverName.setTextSize(18);

            // Setup Buttons
            TableLayout buttonLayout = (TableLayout) receiverLayout.findViewById(R.id.buttonLayout);

            int buttonsPerRow = 2;
            int i = 0;
            final ArrayList<android.widget.Button> buttonViews = new ArrayList<>();
            TableRow buttonRow = null;
            for (final Button button : receiver.getButtons()) {
                android.widget.Button buttonView = (android.widget.Button) inflater.inflate(R.layout.standard_button_wear,
                        buttonRow, false);
                buttonViews.add(buttonView);
                final ColorStateList defaultTextColor = buttonView.getTextColors(); //save original colors
                buttonView.setText(button.getName());

                if (button.getId() == receiver.getLastActivatedButtonId()
                        && WearablePreferencesHandler.getHighlightLastActivatedButton()) {
                    buttonView.setTextColor(ThemeHelper.getThemeAttrColor(context, R.attr.colorAccent));
                }

                buttonView.setOnClickListener(new android.widget.Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Vibration Feedback
                        if (WearablePreferencesHandler.getVibrateOnButtonPress()) {
                            VibrationHandler.vibrate(context, WearablePreferencesHandler.getVibrationDuration());
                        }

                        // Send Action to Smartphone app
                        String actionString = DataApiHandler.buildReceiverActionString(room,
                                receiver, button);
                        dataApiHandler.sendReceiverActionTrigger(actionString);

                        receiver.setLastActivatedButtonId(button.getId());
                        if (WearablePreferencesHandler.getHighlightLastActivatedButton()) {
                            for (android.widget.Button button : buttonViews) {
                                if (button != v) {
                                    button.setTextColor(defaultTextColor);
                                } else {
                                    button.setTextColor(ContextCompat.getColor(context, R.color
                                            .color_light_blue_a700));
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

    // Return the total count of items
    @Override
    public int getItemCount() {
        return rooms.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView roomName;
        public LinearLayout linearLayout_AllOnOffButtons;
        public android.widget.Button buttonAllOn;
        public android.widget.Button buttonAllOff;
        public LinearLayout linearLayoutOfReceivers;
        public LinearLayout footer;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            super(itemView);
            this.roomName = (TextView) itemView.findViewById(R.id.textView_room_name);
            this.linearLayout_AllOnOffButtons = (LinearLayout) itemView.findViewById(R.id.linearLayout_AllOnOffButtons);
            this.buttonAllOn = (android.widget.Button) itemView.findViewById(R.id.button_AllOn);
            this.buttonAllOff = (android.widget.Button) itemView.findViewById(R.id.button_AllOff);
            this.linearLayoutOfReceivers = (LinearLayout) itemView.findViewById(R.id.layout_of_receivers);
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);
        }
    }
}

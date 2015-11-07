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
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
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

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.ConfigureReceiverDialog;
import eu.power_switch.gui.dialog.EditRoomDialog;
import eu.power_switch.gui.fragment.settings.SettingsTabFragment;
import eu.power_switch.log.Log;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;

/**
 * * Adapter to visualize Room items (containing Receivers) in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class RoomRecyclerViewAdapter extends RecyclerView.Adapter<RoomRecyclerViewAdapter.ViewHolder> {
    // Store a member variable for the users
    private ArrayList<Room> rooms;
    private FragmentActivity fragmentActivity;
    private SharedPreferencesHandler sharedPreferencesHandler;

    // Pass in the context and users array into the constructor
    public RoomRecyclerViewAdapter(FragmentActivity fragmentActivity, ArrayList<Room> rooms) {
        this.rooms = rooms;
        this.fragmentActivity = fragmentActivity;
        this.sharedPreferencesHandler = new SharedPreferencesHandler(fragmentActivity);
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RoomRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the custom layout
        View itemView = LayoutInflater.from(fragmentActivity).inflate(R.layout.list_item_room, parent, false);
        // Return a new holder instance
        return new RoomRecyclerViewAdapter.ViewHolder(itemView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final RoomRecyclerViewAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        final Room room = rooms.get(position);

        // Set item views based on the data model
        holder.roomName.setText(room.getName());

        final LinearLayout linearLayout = holder.linearLayoutOfReceivers;
        holder.roomName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linearLayout.getVisibility() == View.VISIBLE) {
                    linearLayout.setVisibility(View.GONE);
                } else {
                    linearLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.roomName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EditRoomDialog editRoomDialog = new EditRoomDialog();
                Bundle roomData = new Bundle();
                roomData.putLong("id", room.getId());
                roomData.putString("name", room.getName());
                editRoomDialog.setArguments(roomData);
                editRoomDialog.setTargetFragment(fragmentActivity.getSupportFragmentManager().getFragments().get(0), 0);
                editRoomDialog.show(fragmentActivity.getSupportFragmentManager(), null);
                return false;
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(fragmentActivity);
                if (sharedPreferencesHandler.getVibrateOnButtonPress()) {
                    VibrationHandler.vibrate(fragmentActivity, sharedPreferencesHandler.getVibrationDuration());
                }
                android.widget.Button buttonView = (android.widget.Button) v;
                List<Gateway> activeGateways = DatabaseHandler.getAllGateways(true);

                if (activeGateways.isEmpty()) {
                    Snackbar.make(v, R.string.no_active_gateway, Snackbar.LENGTH_LONG).setAction
                            (R.string.open_settings, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.addToBackstack(SettingsTabFragment.class, "Settings");
                                    fragmentActivity.getSupportFragmentManager()
                                            .beginTransaction()
                                            .setCustomAnimations(R.anim
                                                    .slide_in_right, R.anim.slide_out_left, android.R.anim
                                                    .slide_in_left, android.R.anim.slide_out_right)
                                            .replace(R.id.mainContentFrameLayout, new SettingsTabFragment())
                                            .addToBackStack(null).commit();
                                }
                            }).show();
                    return;
                }

                ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
                for (Receiver receiver : room.getReceivers()) {
                    for (eu.power_switch.obj.Button button : receiver.getButtons()) {
                        if (buttonView.getText().equals(button.getName())) {
                            for (Gateway gateway : activeGateways) {
                                try {
                                    networkPackages.add(receiver.getNetworkPackage(gateway, button.getName()));
                                } catch (Exception e) {
                                    Log.e(e);
                                }
                            }
                        }
                    }
                }

                NetworkHandler networkHandler = new NetworkHandler(fragmentActivity);
                networkHandler.send(networkPackages);

                if (sharedPreferencesHandler.getHighlightLastActivatedButton()) {
                    // update last activated button for all receivers
                    for (Receiver receiver : room.getReceivers()) {
                        for (Button button : receiver.getButtons()) {
                            if (buttonView.getText().equals(button.getName())) {
                                DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
                            }
                        }
                    }
                    updateReceiverViews(holder, room);
                }
            }
        };

        holder.buttonAllOn.setOnClickListener(onClickListener);
        holder.buttonAllOff.setOnClickListener(onClickListener);

        if (!sharedPreferencesHandler.getShowRoomAllOnOff()) {
            holder.buttonAllOn.setVisibility(View.GONE);
            holder.buttonAllOff.setVisibility(View.GONE);
        } else {
            holder.buttonAllOn.setVisibility(View.VISIBLE);
            holder.buttonAllOff.setVisibility(View.VISIBLE);
        }

        updateReceiverViews(holder, room);

        // collapse room
        if (sharedPreferencesHandler.getAutoCollapseRooms()) {
            linearLayout.setVisibility(View.GONE);
        }

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    private void updateReceiverViews(final RoomRecyclerViewAdapter.ViewHolder holder, Room room) {
        String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) fragmentActivity.getSystemService(inflaterString);

        // clear previous items
        holder.linearLayoutOfReceivers.removeAllViews();
        // add items
        for (final Receiver receiver : room.getReceivers()) {
            // create a new receiverRow for our current receiver and add it
            // to
            // our table of all devices of our current room
            // the row will contain the device name and all buttons
            LinearLayout receiverRow = new LinearLayout(fragmentActivity);
            receiverRow.setOrientation(LinearLayout.HORIZONTAL);
            holder.linearLayoutOfReceivers.addView(receiverRow);

            // setup TextView to display device name
            TextView receiverName = new TextView(fragmentActivity);
            receiverName.setText(receiver.getName());
            receiverName.setTextSize(18);
            receiverName.setGravity(Gravity.CENTER_VERTICAL);
            receiverRow.addView(receiverName, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));

            TableLayout buttonLayout = new TableLayout(fragmentActivity);
            receiverRow.addView(buttonLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            int buttonsPerRow;
            if (receiver.getButtons().size() % 3 == 0) {
                buttonsPerRow = 3;
            } else {
                buttonsPerRow = 2;
            }

            int i = 0;
            final ArrayList<android.widget.Button> buttonViews = new ArrayList<>();
            long lastActivatedButtonId = DatabaseHandler.getLastActivatedButtonId(receiver.getId());
            TableRow buttonRow = null;
            for (final Button button : receiver.getButtons()) {
                android.widget.Button buttonView = (android.widget.Button) inflater.inflate(R.layout.standard_button,
                        null, false);
                final ColorStateList defaultTextColor = buttonView.getTextColors(); //save original colors
                buttonViews.add(buttonView);
                buttonView.setText(button.getName());
                if (sharedPreferencesHandler.getHighlightLastActivatedButton() && lastActivatedButtonId != -1 && button.getId
                        () == lastActivatedButtonId) {
                    buttonView.setTextColor(ContextCompat.getColor(fragmentActivity, R.color.accent_blue_a700));
                }
                buttonView.setOnClickListener(new android.widget.Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(fragmentActivity);
                        if (sharedPreferencesHandler.getVibrateOnButtonPress()) {
                            VibrationHandler.vibrate(fragmentActivity, sharedPreferencesHandler.getVibrationDuration());
                        }
                        List<Gateway> activeGateways = DatabaseHandler.getAllGateways(true);
                        if (activeGateways.isEmpty()) {
                            Snackbar.make(v, R.string.no_active_gateway, Snackbar.LENGTH_LONG).setAction
                                    (R.string.open_settings, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            MainActivity.addToBackstack(SettingsTabFragment.class, "Settings");
                                            fragmentActivity.getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .setCustomAnimations(R.anim
                                                            .slide_in_right, R.anim.slide_out_left, android.R.anim
                                                            .slide_in_left, android.R.anim.slide_out_right)
                                                    .replace(R.id.mainContentFrameLayout, new SettingsTabFragment())
                                                    .addToBackStack(null).commit();
                                        }
                                    }).show();
                            return;
                        }
                        ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
                        for (Gateway gateway : activeGateways) {
                            try {
                                networkPackages.add(receiver.getNetworkPackage(gateway, button.getName()));
                            } catch (Exception e) {
                                Log.e(e);
                            }
                        }
                        NetworkHandler networkHandler = new NetworkHandler(fragmentActivity);
                        networkHandler.send(networkPackages);

                        DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
                        if (sharedPreferencesHandler.getHighlightLastActivatedButton()) {
                            for (android.widget.Button button : buttonViews) {
                                if (button != v) {
                                    button.setTextColor(defaultTextColor);
                                } else {
                                    button.setTextColor(ContextCompat.getColor(fragmentActivity, R.color
                                            .accent_blue_a700));
                                }
                            }
                        }
                    }
                });

                if (i == 0 || i % buttonsPerRow == 0) {
                    buttonRow = new TableRow(fragmentActivity);
                    buttonRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    buttonRow.addView(buttonView);
                    buttonLayout.addView(buttonRow);
                } else {
                    buttonRow.addView(buttonView);
                }

                i++;
            }

            receiverRow.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    ConfigureReceiverDialog configureReceiverDialog = new ConfigureReceiverDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLong("ReceiverId", receiver.getId());
                    configureReceiverDialog.setArguments(bundle);
                    configureReceiverDialog.show(fragmentActivity.getSupportFragmentManager(), null);
                    return false;
                }
            });
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
        public android.widget.Button buttonAllOn;
        public android.widget.Button buttonAllOff;
        public LinearLayout linearLayoutOfReceivers;
        public LinearLayout footer;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            super(itemView);
            this.roomName = (TextView) itemView.findViewById(R.id.txt_room_name);
            this.buttonAllOn = (android.widget.Button) itemView.findViewById(R.id.button_AllOn);
            this.buttonAllOff = (android.widget.Button) itemView.findViewById(R.id.button_AllOff);
            this.linearLayoutOfReceivers = (LinearLayout) itemView.findViewById(R.id.layout_of_receivers);
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);
        }
    }
}
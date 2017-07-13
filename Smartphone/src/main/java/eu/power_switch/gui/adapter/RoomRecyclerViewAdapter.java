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
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.action.ActionHandlerImpl;
import eu.power_switch.gui.dialog.configuration.ConfigureReceiverDialog;
import eu.power_switch.gui.dialog.configuration.ConfigureRoomDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.network.NetworkHandlerImpl;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;
import timber.log.Timber;

/**
 * * Adapter to visualize Room items (containing Receivers) in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class RoomRecyclerViewAdapter extends RecyclerView.Adapter<RoomRecyclerViewAdapter.ViewHolder> {
    // Store a member variable for the users
    private       RecyclerViewFragment recyclerViewFragment;
    private       ArrayList<Room>      rooms;
    private       FragmentActivity     fragmentActivity;
    private final ActionHandler        actionHandler;

    // Pass in the context and users array into the constructor
    public RoomRecyclerViewAdapter(RecyclerViewFragment recyclerViewFragment, FragmentActivity fragmentActivity, ArrayList<Room> rooms) {
        this.recyclerViewFragment = recyclerViewFragment;
        this.rooms = rooms;
        this.fragmentActivity = fragmentActivity;
        actionHandler = new ActionHandlerImpl(fragmentActivity, new NetworkHandlerImpl(fragmentActivity));
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RoomRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the custom layout
        View itemView = LayoutInflater.from(fragmentActivity)
                .inflate(R.layout.list_item_room, parent, false);
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
                    room.setCollapsed(true);
                    linearLayout.setVisibility(View.GONE);
                } else {
                    room.setCollapsed(false);
                    linearLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.roomName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ConfigureRoomDialog configureRoomDialog = ConfigureRoomDialog.newInstance(room, recyclerViewFragment);
                configureRoomDialog.setTargetFragment(recyclerViewFragment, 0);
                configureRoomDialog.show(fragmentActivity.getSupportFragmentManager(), null);
                return true;
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS)) {
                    VibrationHandler.vibrate(fragmentActivity,
                            SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION));
                }

                android.widget.Button buttonView = (android.widget.Button) v;
                String buttonName = buttonView.getText()
                        .toString();
                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... buttonNames) {
                        try {
                            String buttonName = buttonNames[0];

                            // send signal
                            actionHandler.execute(room, buttonName);
                        } catch (Exception e) {
                            Timber.e(e);
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        updateReceiverViews(holder, room);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buttonName);
            }
        };

        holder.buttonAllOn.setOnClickListener(onClickListener);
        holder.buttonAllOff.setOnClickListener(onClickListener);

        if (!SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SHOW_ROOM_ALL_ON_OFF)) {
            holder.buttonAllOn.setVisibility(View.GONE);
            holder.buttonAllOff.setVisibility(View.GONE);
        } else {
            holder.buttonAllOn.setVisibility(View.VISIBLE);
            holder.buttonAllOff.setVisibility(View.VISIBLE);
        }

        updateReceiverViews(holder, room);

        // collapse room
        if (room.isCollapsed()) {
            linearLayout.setVisibility(View.GONE);
        } else {
            linearLayout.setVisibility(View.VISIBLE);
        }

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    private void updateReceiverViews(final RoomRecyclerViewAdapter.ViewHolder holder, final Room room) {
        String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater       = (LayoutInflater) fragmentActivity.getSystemService(inflaterString);

        // clear previous items
        holder.linearLayoutOfReceivers.removeAllViews();

        if (room.getReceivers()
                .isEmpty()) {
            holder.empty.setVisibility(View.VISIBLE);
            holder.linearLayoutOfReceivers.setVisibility(View.GONE);
        } else {
            holder.empty.setVisibility(View.GONE);
            holder.linearLayoutOfReceivers.setVisibility(View.VISIBLE);
        }

        // add items
        for (final Receiver receiver : room.getReceivers()) {
            // create a new receiverRow for our current receiver and add it to
            // our table of all devices of our current room
            // the row will contain the device name and all buttons

            LinearLayout receiverLayout = (LinearLayout) inflater.inflate(R.layout.list_item_receiver, holder.linearLayoutOfReceivers, false);
            receiverLayout.setOrientation(LinearLayout.HORIZONTAL);
            holder.linearLayoutOfReceivers.addView(receiverLayout);

            // setup TextView to display device name
            TextView receiverName = receiverLayout.findViewById(R.id.txt_name);
            receiverName.setText(receiver.getName());
            receiverName.setTextSize(18);

            TableLayout buttonLayout = receiverLayout.findViewById(R.id.buttonLayout);
            int         buttonsPerRow;
            if (receiver.getButtons()
                    .size() % 3 == 0) {
                buttonsPerRow = 3;
            } else {
                buttonsPerRow = 2;
            }

            int                                    i                     = 0;
            final ArrayList<android.widget.Button> buttonViews           = new ArrayList<>();
            long                                   lastActivatedButtonId = receiver.getLastActivatedButtonId();
            TableRow                               buttonRow             = null;
            for (final Button button : receiver.getButtons()) {
                final android.widget.Button buttonView       = (android.widget.Button) inflater.inflate(R.layout.simple_button, buttonRow, false);
                final ColorStateList        defaultTextColor = buttonView.getTextColors(); //save original colors
                buttonViews.add(buttonView);
                buttonView.setText(button.getName());
                final int accentColor = ThemeHelper.getThemeAttrColor(fragmentActivity, R.attr.colorAccent);
                if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON) && lastActivatedButtonId != -1 && button.getId() == lastActivatedButtonId) {
                    buttonView.setTextColor(accentColor);
                }
                buttonView.setOnClickListener(new android.widget.Button.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_VIBRATE_ON_BUTTON_PRESS)) {
                            VibrationHandler.vibrate(fragmentActivity,
                                    SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_VIBRATION_DURATION));
                        }

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                // send signal
                                actionHandler.execute(receiver, button);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
                                    for (android.widget.Button button : buttonViews) {
                                        if (button != v) {
                                            button.setTextColor(defaultTextColor);
                                        } else {
                                            button.setTextColor(accentColor);
                                        }
                                    }
                                }
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            receiverLayout.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    ConfigureReceiverDialog configureReceiverDialog = ConfigureReceiverDialog.newInstance(receiver, recyclerViewFragment);
                    configureReceiverDialog.show(fragmentActivity.getSupportFragmentManager(), null);
                    return true;
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
    public class ViewHolder extends ButterKnifeViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        @BindView(R.id.txt_room_name)
        TextView              roomName;
        @BindView(R.id.button_AllOn)
        android.widget.Button buttonAllOn;
        @BindView(R.id.button_AllOff)
        android.widget.Button buttonAllOff;
        @BindView(R.id.layout_of_receivers)
        LinearLayout          linearLayoutOfReceivers;
        @BindView(R.id.textview_empty)
        TextView              empty;
        @BindView(R.id.list_footer)
        LinearLayout          footer;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
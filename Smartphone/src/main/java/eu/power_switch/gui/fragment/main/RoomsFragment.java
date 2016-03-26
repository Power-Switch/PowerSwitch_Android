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

package eu.power_switch.gui.fragment.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.RoomRecyclerViewAdapter;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.gui.dialog.ConfigureReceiverDialog;
import eu.power_switch.gui.dialog.EditRoomOrderDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.wear.service.UtilityService;

/**
 * Fragment containing a List of all Rooms and Receivers
 */
public class RoomsFragment extends RecyclerViewFragment {

    private ArrayList<Room> rooms;

    private BroadcastReceiver broadcastReceiver;
    private FloatingActionButton addReceiverFAB;
    private RoomRecyclerViewAdapter roomsRecyclerViewAdapter;
    private RecyclerView recyclerViewRooms;

    /**
     * Used to notify Room Fragment (this) that Rooms have changed
     *
     * @param context any suitable context
     */
    public static void sendReceiverChangedBroadcast(Context context) {
        Log.d("RoomsFragment", "sendReceiverChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_RECEIVER_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        UtilityService.forceWearDataUpdate(context);
    }

    @Override
    public void onCreateViewEvent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_rooms, container, false);

        setHasOptionsMenu(true);

        rooms = new ArrayList<>();
        recyclerViewRooms = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        roomsRecyclerViewAdapter = new RoomRecyclerViewAdapter(this, getActivity(), rooms);
        recyclerViewRooms.setAdapter(roomsRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.room_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewRooms.setLayoutManager(layoutManager);

        addReceiverFAB = (FloatingActionButton) rootView.findViewById(R.id.add_fab);
        addReceiverFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        final RecyclerViewFragment recyclerViewFragment = this;
        addReceiverFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AnimationHandler.checkTargetApi()) {
//                    Intent intent = new Intent();
//
//                    ActivityOptionsCompat options =
//                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                                    addReceiverFAB,   // The view which starts the transition
//                                    "configureReceiverTransition"    // The transitionName of the view we’re transitioning to
//                            );
//                    ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
                } else {

                }

                if (SettingsConstants.INVALID_APARTMENT_ID == SmartphonePreferencesHandler.getCurrentApartmentId()) {
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.please_create_or_activate_apartment_first)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                    return;
                }

                ConfigureReceiverDialog configureReceiverDialog = new ConfigureReceiverDialog();
                configureReceiverDialog.setTargetFragment(recyclerViewFragment, 0);
                configureReceiverDialog.show(getFragmentManager(), null);
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("RoomsFragment", "received intent: " + intent.getAction());
                updateUI();
            }
        };
    }

    @Override
    protected void onInitialized() {
        updateUI();
    }

    private void updateUI() {
        updateListContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_receiver:
                ConfigureReceiverDialog configureReceiverDialog = new ConfigureReceiverDialog();
                configureReceiverDialog.setTargetFragment(this, 0);
                configureReceiverDialog.show(getFragmentManager(), null);
                break;
            case R.id.reorder_rooms:
                EditRoomOrderDialog editRoomOrderDialog = EditRoomOrderDialog.newInstance(SmartphonePreferencesHandler.getCurrentApartmentId());
                editRoomOrderDialog.setTargetFragment(this, 0);
                editRoomOrderDialog.show(getFragmentManager(), null);
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.room_fragment_menu, menu);
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.create_receiver).setIcon(IconicsHelper.getAddIcon(getActivity(), color));
        menu.findItem(R.id.reorder_rooms).setIcon(IconicsHelper.getReorderIcon(getActivity(), color));

        if (!SmartphonePreferencesHandler.getHideAddFAB()) {
            menu.findItem(R.id.create_receiver).setVisible(false).setEnabled(false);
//            menu.removeItem(R.id.create_receiver);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            addReceiverFAB.setVisibility(View.GONE);
        } else {
            addReceiverFAB.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_APARTMENT_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_RECEIVER_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewRooms;
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return roomsRecyclerViewAdapter;
    }

    @Override
    public List refreshListData() throws Exception {
        rooms.clear();

        if (DeveloperPreferencesHandler.getPlayStoreMode()) {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
            rooms.addAll(playStoreModeDataModel.getActiveApartment().getRooms());
        } else {
            long currentApartmentId = SmartphonePreferencesHandler.getCurrentApartmentId();
            if (currentApartmentId != SettingsConstants.INVALID_APARTMENT_ID) {
                // Get Rooms and Receivers
                rooms.addAll(DatabaseHandler.getRooms(currentApartmentId));
            }
        }

        return rooms;
    }
}
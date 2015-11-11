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
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.adapter.RoomRecyclerViewAdapter;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.gui.dialog.ConfigureReceiverDialog;
import eu.power_switch.log.Log;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.obj.Room;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.wear.service.UtilityService;

/**
 * Fragment containing a List of all Rooms and Receivers
 */
public class RoomsFragment extends Fragment {

    private NetworkHandler networkHandler;
    private ArrayList<Room> rooms;

    private BroadcastReceiver broadcastReceiver;
    private View rootView;
    private FloatingActionButton addReceiverFAB;
    private RoomRecyclerViewAdapter roomsRecyclerViewAdapter;
    private RecyclerView recyclerViewRooms;

    /**
     * Used to notify Room Fragment (this) that Rooms have changed
     *
     * @param context
     */
    public static void sendReceiverChangedBroadcast(Context context) {
        Log.d("RoomsFragment", "sendReceiverChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_RECEIVER_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        UtilityService.forceWearDataUpdate(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_rooms, container, false);
        setHasOptionsMenu(true);

        rooms = new ArrayList<>();
        recyclerViewRooms = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_rooms);
        roomsRecyclerViewAdapter = new RoomRecyclerViewAdapter(getActivity(), rooms);
        recyclerViewRooms.setAdapter(roomsRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.room_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewRooms.setLayoutManager(layoutManager);
        updateUI();

        addReceiverFAB = (FloatingActionButton) rootView.findViewById(R.id.add_receiver_fab);
        final Fragment fragment = this;
        addReceiverFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AddReceiverDialog addReceiverDialog = new AddReceiverDialog();
//                addReceiverDialog.show(getFragmentManager(), null);

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

                ConfigureReceiverDialog configureReceiverDialog = new ConfigureReceiverDialog();
                configureReceiverDialog.setTargetFragment(fragment, 0);
                configureReceiverDialog.show(getFragmentManager(), null);
            }
        });

        networkHandler = new NetworkHandler(getActivity());

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("RoomsFragment", "received intent: " + intent.getAction());
                updateUI();
            }
        };


        return rootView;
    }

    private void updateUI() {
        rooms.clear();

        SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(getActivity());
        if (sharedPreferencesHandler.getPlayStoreMode()) {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
            rooms.addAll(playStoreModeDataModel.getRooms());
        } else {
            fillListWithRooms();
        }

        roomsRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void fillListWithRooms() {
        // Get Rooms and Receivers
        rooms.addAll(DatabaseHandler.getAllRooms());
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
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.room_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(getContext());
        if (sharedPreferencesHandler.getHideAddFAB()) {
            addReceiverFAB.setVisibility(View.GONE);
        } else {
            addReceiverFAB.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_RECEIVER_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

}
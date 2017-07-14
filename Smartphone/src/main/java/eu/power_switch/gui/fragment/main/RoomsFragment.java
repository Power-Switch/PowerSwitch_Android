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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandlerStatic;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.event.ActiveApartmentChangedEvent;
import eu.power_switch.event.ReceiverChangedEvent;
import eu.power_switch.event.RoomChangedEvent;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.RoomRecyclerViewAdapter;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.gui.dialog.EditRoomOrderDialog;
import eu.power_switch.gui.dialog.configuration.ConfigureReceiverDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.SettingsConstants;
import timber.log.Timber;

/**
 * Fragment containing a List of all Rooms and Receivers
 */
public class RoomsFragment extends RecyclerViewFragment<Room> {

    @BindView(R.id.add_fab)
    FloatingActionButton addReceiverFAB;

    @Inject
    ActionHandler actionHandler;

    private ArrayList<Room>            rooms;
    private RoomRecyclerViewAdapter    roomsRecyclerViewAdapter;
    private StaggeredGridLayoutManager layoutManager;

    /**
     * Used to notify Room Fragment (this) that Rooms have changed
     */
    public static void notifyRoomChanged() {
        Timber.d("RoomsFragment", "notifyRoomChanged");
        EventBus.getDefault()
                .post(new RoomChangedEvent());
    }

    /**
     * Used to notify Room Fragment (this) that Receivers have changed
     */
    public static void notifyReceiverChanged() {
        Timber.d("RoomsFragment", "notifyReceiverChanged");
        EventBus.getDefault()
                .post(new ReceiverChangedEvent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        rooms = new ArrayList<>();
        roomsRecyclerViewAdapter = new RoomRecyclerViewAdapter(this, getActivity(), rooms, actionHandler);
        getRecyclerView().setAdapter(roomsRecyclerViewAdapter);
        layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);

        addReceiverFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        final RecyclerViewFragment recyclerViewFragment = this;
        addReceiverFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
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

                    if (SettingsConstants.INVALID_APARTMENT_ID == SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID)) {
                        new AlertDialog.Builder(getContext()).setMessage(R.string.please_create_or_activate_apartment_first)
                                .setNeutralButton(android.R.string.ok, null)
                                .show();
                        return;
                    }

                    ConfigureReceiverDialog configureReceiverDialog = ConfigureReceiverDialog.newInstance(recyclerViewFragment);
                    configureReceiverDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        updateUI();

        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onActiveApartmentChanged(ActiveApartmentChangedEvent activeApartmentChangedEvent) {
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onRoomChanged(RoomChangedEvent roomChangedEvent) {
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onReceiverChanged(ReceiverChangedEvent receiverChangedEvent) {
        updateUI();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_rooms;
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
                if (SettingsConstants.INVALID_APARTMENT_ID == SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID)) {
                    new AlertDialog.Builder(getContext()).setMessage(R.string.please_create_or_activate_apartment_first)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                    return true;
                }

                ConfigureReceiverDialog configureReceiverDialog = ConfigureReceiverDialog.newInstance(this);
                configureReceiverDialog.show(getFragmentManager(), null);
                break;
            case R.id.reorder_rooms:
                EditRoomOrderDialog editRoomOrderDialog = EditRoomOrderDialog.newInstance(SmartphonePreferencesHandler.<Long>get(
                        SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID));
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
        menu.findItem(R.id.create_receiver)
                .setIcon(IconicsHelper.getAddIcon(getActivity(), color));
        menu.findItem(R.id.reorder_rooms)
                .setIcon(IconicsHelper.getReorderIcon(getActivity(), color));

        if (!SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            menu.findItem(R.id.create_receiver)
                    .setVisible(false)
                    .setEnabled(false);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            addReceiverFAB.setVisibility(View.GONE);
        } else {
            addReceiverFAB.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return roomsRecyclerViewAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.room_grid_span_count);
    }

    @Override
    public List<Room> loadListData() throws Exception {
        if (DeveloperPreferencesHandler.getPlayStoreMode()) {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
            return playStoreModeDataModel.getActiveApartment()
                    .getRooms();
        } else {
            long currentApartmentId = SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID);
            if (currentApartmentId != SettingsConstants.INVALID_APARTMENT_ID) {
                // Get Rooms and Receivers
                return DatabaseHandlerStatic.getRooms(currentApartmentId);
            } else {
                return new ArrayList<>();
            }
        }
    }

    @Override
    protected void onListDataChanged(List<Room> list) {
        rooms.clear();
        rooms.addAll(list);
    }
}
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
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.event.ActiveApartmentChangedEvent;
import eu.power_switch.event.ReceiverChangedEvent;
import eu.power_switch.event.RoomChangedEvent;
import eu.power_switch.gui.adapter.RoomRecyclerViewAdapter;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.gui.dialog.ConfigureRoomOrderDialog;
import eu.power_switch.gui.dialog.configuration.ConfigureReceiverDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import timber.log.Timber;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID;


/**
 * Fragment containing a List of all Rooms and Receivers
 */
public class RoomsFragment extends RecyclerViewFragment<Room> {

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rooms = new ArrayList<>();
        roomsRecyclerViewAdapter = new RoomRecyclerViewAdapter(this, rooms, actionHandler, smartphonePreferencesHandler);
        getRecyclerView().setAdapter(roomsRecyclerViewAdapter);
        layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);


        IconicsDrawable icon = iconicsHelper.getFabIcon(MaterialDesignIconic.Icon.gmi_plus);
        addFAB.setImageDrawable(icon);
        final RecyclerViewFragment recyclerViewFragment = this;
        addFAB.setOnClickListener(new View.OnClickListener() {
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

                    long apartmentId = smartphonePreferencesHandler.getValue(KEY_CURRENT_APARTMENT_ID);
                    if (SettingsConstants.INVALID_APARTMENT_ID == apartmentId) {
                        new AlertDialog.Builder(getContext()).setMessage(R.string.please_create_or_activate_apartment_first)
                                .setNeutralButton(android.R.string.ok, null)
                                .show();
                        return;
                    }

                    ConfigureReceiverDialog configureReceiverDialog = ConfigureReceiverDialog.newInstance(recyclerViewFragment);
                    configureReceiverDialog.setTargetFragment(RoomsFragment.this, 0);
                    configureReceiverDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        updateUI();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

        long apartmentId = smartphonePreferencesHandler.getValue(KEY_CURRENT_APARTMENT_ID);
        switch (menuItem.getItemId()) {
            case R.id.create_receiver:

                if (SettingsConstants.INVALID_APARTMENT_ID == apartmentId) {
                    new AlertDialog.Builder(getContext()).setMessage(R.string.please_create_or_activate_apartment_first)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                    return true;
                }

                ConfigureReceiverDialog configureReceiverDialog = ConfigureReceiverDialog.newInstance(this);
                configureReceiverDialog.show(getFragmentManager(), null);
                break;
            case R.id.reorder_rooms:
                ConfigureRoomOrderDialog configureRoomOrderDialog = ConfigureRoomOrderDialog.newInstance(apartmentId);
                configureRoomOrderDialog.setTargetFragment(this, 0);
                configureRoomOrderDialog.show(getFragmentManager(), null);
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.room_fragment_menu, menu);
        IconicsDrawable addIcon = iconicsHelper.getOptionsMenuIcon(MaterialDesignIconic.Icon.gmi_plus);
        menu.findItem(R.id.create_receiver)
                .setIcon(addIcon);
        IconicsDrawable icon = iconicsHelper.getFabIcon(GoogleMaterial.Icon.gmd_reorder)
                .paddingDp(2);
        menu.findItem(R.id.reorder_rooms)
                .setIcon(icon);

        boolean useOptionsMenuOnly = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        if (!useOptionsMenuOnly) {
            menu.findItem(R.id.create_receiver)
                    .setVisible(false)
                    .setEnabled(false);
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
        long currentApartmentId = smartphonePreferencesHandler.getValue(KEY_CURRENT_APARTMENT_ID);
        if (currentApartmentId != SettingsConstants.INVALID_APARTMENT_ID) {
            // Get Rooms and Receivers
            return persistenceHandler.getRooms(currentApartmentId);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected void onListDataChanged(List<Room> list) {
        rooms.clear();
        rooms.addAll(list);
    }
}
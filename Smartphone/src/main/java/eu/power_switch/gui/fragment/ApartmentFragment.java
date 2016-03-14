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

package eu.power_switch.gui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.*;
import android.widget.LinearLayout;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ApartmentRecyclerViewAdapter;
import eu.power_switch.gui.dialog.ConfigureApartmentDialog;
import eu.power_switch.obj.Apartment;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.constants.TutorialConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.wear.service.UtilityService;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Markus on 25.12.2015.
 */
public class ApartmentFragment extends RecyclerViewFragment {

    private RecyclerView recyclerViewApartments;
    private ApartmentRecyclerViewAdapter apartmentArrayAdapter;
    private ArrayList<Apartment> apartments = new ArrayList<>();
    private FloatingActionButton fab;
    private BroadcastReceiver broadcastReceiver;

    /**
     * Used to notify other Fragments that the selected Apartment has changed
     *
     * @param context any suitable context
     */
    public static void sendApartmentChangedBroadcast(Context context) {
        Log.d("ApartmentFragment", "sendApartmentChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_APARTMENT_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        UtilityService.forceWearDataUpdate(context);
    }

    @Override
    public View onCreateViewEvent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_apartment, container, false);
        setHasOptionsMenu(true);

        final RecyclerViewFragment recyclerViewFragment = this;
        recyclerViewApartments = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_apartments);
        apartmentArrayAdapter = new ApartmentRecyclerViewAdapter(getActivity(), apartments);

        recyclerViewApartments.setAdapter(apartmentArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.apartments_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewApartments.setLayoutManager(layoutManager);
        apartmentArrayAdapter.setOnItemClickListener(new ApartmentRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                final Apartment apartment = apartments.get(position);

                SmartphonePreferencesHandler.setCurrentApartmentId(apartment.getId());

                for (Apartment currentApartment : apartments) {
                    if (currentApartment.getId().equals(apartment.getId())) {
                        currentApartment.setActive(true);
                    } else {
                        currentApartment.setActive(false);
                    }
                }

                apartmentArrayAdapter.notifyDataSetChanged();
            }
        });
        apartmentArrayAdapter.setOnItemLongClickListener(new ApartmentRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, final int position) {
                Apartment apartment = apartments.get(position);

                ConfigureApartmentDialog configureApartmentDialog = ConfigureApartmentDialog.newInstance(
                        apartment.getId());
                configureApartmentDialog.setTargetFragment(recyclerViewFragment, 0);
                configureApartmentDialog.show(getFragmentManager(), null);
            }
        });

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_apartment_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ConfigureApartmentDialog configureApartmentDialog = new ConfigureApartmentDialog();
                    configureApartmentDialog.setTargetFragment(recyclerViewFragment, 0);
                    configureApartmentDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(recyclerViewFragment.getRecyclerView(), e);
                }
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this, "received intent: " + intent.getAction());
                refreshUI();
            }
        };


        super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    private void showTutorial() {
        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(fab)
                .setUseAutoRadius(false)
                .setRadius(64 * 3)
                .setDismissOnTouch(true)
                .setDismissText(getString(R.string.tutorial__got_it))
                .setContentText(getString(R.string.tutorial__apartment_explanation))
                .singleUse(TutorialConstants.APARTMENT_KEY)
                .setDelay(500)
                .show();
    }

    private void refreshUI() {
        updateListContent();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            inflater.inflate(R.menu.apartment_fragment_menu, menu);
            if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
                menu.findItem(R.id.create_apartment)
                        .setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
            } else {
                menu.findItem(R.id.create_apartment)
                        .setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.black));
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_apartment:
                try {
                    ConfigureApartmentDialog configureApartmentDialog = new ConfigureApartmentDialog();
                    configureApartmentDialog.setTargetFragment(this, 0);
                    configureApartmentDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                }
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewApartments;
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return apartmentArrayAdapter;
    }

    @Override
    public List refreshListData() throws Exception {
        apartments.clear();

        if (DeveloperPreferencesHandler.getPlayStoreMode()) {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
            apartments.addAll(playStoreModeDataModel.getApartments());
        } else {
            apartments.addAll(DatabaseHandler.getAllApartments());
        }

        return apartments;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }

        showTutorial();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_APARTMENT_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}

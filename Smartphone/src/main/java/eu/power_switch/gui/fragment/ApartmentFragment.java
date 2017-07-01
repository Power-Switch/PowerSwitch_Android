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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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
import java.util.List;

import butterknife.BindView;
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
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.TutorialConstants;
import eu.power_switch.wear.service.UtilityService;
import timber.log.Timber;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Created by Markus on 25.12.2015.
 */
public class ApartmentFragment extends RecyclerViewFragment<Apartment> {

    @BindView(R.id.add_fab)
    FloatingActionButton fab;

    private ApartmentRecyclerViewAdapter apartmentArrayAdapter;
    private ArrayList<Apartment> apartments = new ArrayList<>();


    private BroadcastReceiver broadcastReceiver;

    /**
     * Used to notify other Fragments that the selected Apartment has changed
     *
     * @param context any suitable context
     */
    public static void sendApartmentChangedBroadcast(Context context) {
        Timber.d("ApartmentFragment", "sendApartmentChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_APARTMENT_CHANGED);

        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
        UtilityService.forceWearDataUpdate(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        final RecyclerViewFragment recyclerViewFragment = this;
        apartmentArrayAdapter = new ApartmentRecyclerViewAdapter(getActivity(), apartments);

        getRecyclerView().setAdapter(apartmentArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);
        apartmentArrayAdapter.setOnItemClickListener(new ApartmentRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                try {
                    final Apartment apartment = apartments.get(position);

                    SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID, apartment.getId());

                    for (Apartment currentApartment : apartments) {
                        if (currentApartment.getId()
                                .equals(apartment.getId())) {
                            currentApartment.setActive(true);
                        } else {
                            currentApartment.setActive(false);
                        }
                    }

                    apartmentArrayAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });
        apartmentArrayAdapter.setOnItemLongClickListener(new ApartmentRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, final int position) {
                try {
                    Apartment apartment = apartments.get(position);

                    ConfigureApartmentDialog configureApartmentDialog = ConfigureApartmentDialog.newInstance(apartment.getId());
                    configureApartmentDialog.setTargetFragment(recyclerViewFragment, 0);
                    configureApartmentDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getContext(), android.R.color.white)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ConfigureApartmentDialog configureApartmentDialog = new ConfigureApartmentDialog();
                    configureApartmentDialog.setTargetFragment(recyclerViewFragment, 0);
                    configureApartmentDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("received intent: " + intent.getAction());
                updateUI();
            }
        };

        updateUI();

        return rootView;
    }

    private void showTutorial() {
        new MaterialShowcaseView.Builder(getActivity()).setTarget(fab)
                .setUseAutoRadius(false)
                .setRadius(64 * 3)
                .setDismissOnTouch(true)
                .setDismissText(getString(R.string.tutorial__got_it))
                .setContentText(getString(R.string.tutorial__apartment_explanation))
                .singleUse(TutorialConstants.APARTMENT_KEY)
                .setDelay(500)
                .show();
    }

    private void updateUI() {
        updateListContent();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.apartment_fragment_menu, menu);
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.create_apartment)
                .setIcon(IconicsHelper.getAddIcon(getActivity(), color));

        if (!SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            menu.findItem(R.id.create_apartment)
                    .setVisible(false)
                    .setEnabled(false);
        }
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
                    StatusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return apartmentArrayAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.apartments_grid_span_count);
    }

    @Override
    public List<Apartment> loadListData() throws Exception {
        if (DeveloperPreferencesHandler.getPlayStoreMode()) {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getActivity());
            return playStoreModeDataModel.getApartments();
        } else {
            return DatabaseHandler.getAllApartments();
        }
    }

    @Override
    protected void onListDataChanged(List<Apartment> list) {
        apartments.clear();
        apartments.addAll(list);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
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
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_apartment;
    }
}

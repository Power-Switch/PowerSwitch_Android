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

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.event.ActiveApartmentChangedEvent;
import eu.power_switch.gui.adapter.ApartmentRecyclerViewAdapter;
import eu.power_switch.gui.dialog.configuration.ConfigureApartmentDialog;
import eu.power_switch.obj.Apartment;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.TutorialConstants;
import eu.power_switch.wear.service.UtilityService;
import timber.log.Timber;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID;

/**
 * Created by Markus on 25.12.2015.
 */
public class ApartmentFragment extends RecyclerViewFragment<Apartment> {

    private ApartmentRecyclerViewAdapter apartmentArrayAdapter;
    private ArrayList<Apartment> apartments = new ArrayList<>();

    /**
     * Used to notify other Fragments that the selected Apartment has changed
     *
     * @param context any suitable context
     */
    public static void notifyActiveApartmentChanged(Context context) {
        Timber.d("ApartmentFragment", "notifyActiveApartmentChanged");
        EventBus.getDefault()
                .post(new ActiveApartmentChangedEvent());

        UtilityService.forceWearDataUpdate(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final RecyclerViewFragment recyclerViewFragment = this;
        apartmentArrayAdapter = new ApartmentRecyclerViewAdapter(getActivity(), smartphonePreferencesHandler, apartments);

        getRecyclerView().setAdapter(apartmentArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);
        apartmentArrayAdapter.setOnItemClickListener(new ApartmentRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                try {
                    final Apartment apartment = apartments.get(position);

                    smartphonePreferencesHandler.setValue(KEY_CURRENT_APARTMENT_ID, apartment.getId());

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
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });
        apartmentArrayAdapter.setOnItemLongClickListener(new ApartmentRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, final int position) {
                try {
                    Apartment apartment = apartments.get(position);

                    ConfigureApartmentDialog configureApartmentDialog = ConfigureApartmentDialog.newInstance(apartment, recyclerViewFragment);
                    configureApartmentDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        IconicsDrawable fabIcon = iconicsHelper.getFabIcon(MaterialDesignIconic.Icon.gmi_plus);
        addFAB.setImageDrawable(fabIcon);
        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ConfigureApartmentDialog configureApartmentDialog = ConfigureApartmentDialog.newInstance(recyclerViewFragment);
                    configureApartmentDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        updateListContent();

        return rootView;
    }

    private void showTutorial() {
        new MaterialShowcaseView.Builder(getActivity()).setTarget(addFAB)
                .setUseAutoRadius(false)
                .setRadius(64 * 3)
                .setDismissOnTouch(true)
                .setDismissText(getString(R.string.tutorial__got_it))
                .setContentText(getString(R.string.tutorial__apartment_explanation))
                .singleUse(TutorialConstants.APARTMENT_KEY)
                .setDelay(500)
                .show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.apartment_fragment_menu, menu);

        IconicsDrawable fabIcon = iconicsHelper.getOptionsMenuIcon(MaterialDesignIconic.Icon.gmi_plus);
        menu.findItem(R.id.create_apartment)
                .setIcon(fabIcon);

        boolean useOptionsMenuOnly = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        if (!useOptionsMenuOnly) {
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
                    ConfigureApartmentDialog configureApartmentDialog = ConfigureApartmentDialog.newInstance(this);
                    configureApartmentDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
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
        return persistenceHandler.getAllApartments();
    }

    @Override
    protected void onListDataChanged(List<Apartment> list) {
        apartments.clear();
        apartments.addAll(list);
    }

    @Override
    public void onResume() {
        super.onResume();

        showTutorial();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onActiveApartmentChanged(ActiveApartmentChangedEvent activeApartmentChangedEvent) {
        updateListContent();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_apartment;
    }
}

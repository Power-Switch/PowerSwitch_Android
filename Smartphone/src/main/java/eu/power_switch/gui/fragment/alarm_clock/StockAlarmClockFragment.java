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

package eu.power_switch.gui.fragment.alarm_clock;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.event.AlarmEventActionAddedEvent;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddStockAlarmClockEventActionDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.AlarmClockConstants.Event;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_STOCK_ALARM_CLOCK_ENABLED;

/**
 * Fragment containing all settings related to stock alarm clock event handling
 * <p/>
 * Created by Markus on 27.03.2016.
 */
public class StockAlarmClockFragment extends RecyclerViewFragment<Action> {

    private static Event currentEventType = Event.ALARM_TRIGGERED;

    @BindView(R.id.spinner_sleep_as_android_event)
    Spinner spinnerEventType;
    @BindView(R.id.switch_on_off)
    Switch  switchOnOff;

    @Inject
    PersistenceHandler persistenceHandler;

    private ArrayList<Action> actions = new ArrayList<>();
    private ActionRecyclerViewAdapter recyclerViewAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final RecyclerViewFragment recyclerViewFragment = this;

        boolean enabled = smartphonePreferencesHandler.getValue(KEY_STOCK_ALARM_CLOCK_ENABLED);
        switchOnOff.setChecked(enabled);
        switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    smartphonePreferencesHandler.setValue(KEY_STOCK_ALARM_CLOCK_ENABLED, isChecked);
                }
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.stock_alarm_clock_event_names,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);
        SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateUI();
            }
        };
        spinnerEventType.setOnItemSelectedListener(spinnerInteractionListener);
        spinnerEventType.setOnTouchListener(spinnerInteractionListener);

        recyclerViewAdapter = new ActionRecyclerViewAdapter(getContext(), persistenceHandler, actions);
        recyclerViewAdapter.setOnDeleteClickListener(new ActionRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, final int position) {
                new AlertDialog.Builder(getContext()).setTitle(R.string.delete)
                        .setMessage(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    actions.remove(position);
                                    persistenceHandler.setAlarmActions(currentEventType, actions);
                                    statusMessageHandler.showInfoMessage(recyclerViewFragment.getRecyclerView(),
                                            R.string.action_removed,
                                            Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    statusMessageHandler.showErrorMessage(recyclerViewFragment.getRecyclerView(), e);
                                }

                                recyclerViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();
            }
        });
        getRecyclerView().setAdapter(recyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);

        addFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddStockAlarmClockEventActionDialog addStockAlarmClockEventActionDialog = AddStockAlarmClockEventActionDialog.newInstance(
                        currentEventType.getId());
                addStockAlarmClockEventActionDialog.setTargetFragment(recyclerViewFragment, 0);
                addStockAlarmClockEventActionDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        updateUI();

        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onAlarmEventActionAdded(AlarmEventActionAddedEvent alarmEventActionAddedEvent) {
        updateUI();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_stock_alarm_clock;
    }

    private void updateUI() {
        refreshActions();
    }

    private void refreshActions() {
        switch (spinnerEventType.getSelectedItemPosition()) {
            case 0:
                currentEventType = Event.getById(Event.ALARM_TRIGGERED.getId());
                break;
            case 1:
                currentEventType = Event.getById(Event.ALARM_DISMISSED.getId());
                break;
        }

        updateListContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.add_action:
                AddStockAlarmClockEventActionDialog addAlarmEventActionDialog = AddStockAlarmClockEventActionDialog.newInstance(spinnerEventType.getSelectedItemPosition());
                addAlarmEventActionDialog.setTargetFragment(this, 0);
                addAlarmEventActionDialog.show(getActivity().getSupportFragmentManager(), null);
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sleep_as_android_fragment_menu, menu);
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.add_action)
                .setIcon(IconicsHelper.getAddIcon(getActivity(), color));

        boolean useOptionsMenuOnly = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        if (!useOptionsMenuOnly) {
            menu.findItem(R.id.add_action)
                    .setVisible(false)
                    .setEnabled(false);
        }
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return recyclerViewAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.action_grid_span_count);
    }

    @Override
    public List<Action> loadListData() throws Exception {
        return persistenceHandler.getAlarmActions(currentEventType);
    }

    @Override
    protected void onListDataChanged(List<Action> list) {
        actions.clear();
        actions.addAll(list);
    }
}

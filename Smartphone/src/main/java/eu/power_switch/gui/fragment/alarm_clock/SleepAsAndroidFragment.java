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
import android.support.design.widget.FloatingActionButton;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.mikepenz.iconics.view.IconicsImageView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.alarm_clock.sleep_as_android.SleepAsAndroidHelper;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddSleepAsAndroidAlarmEventActionDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.SleepAsAndroidConstants.Event;
import eu.power_switch.shared.event.AlarmEventActionAddedEvent;

/**
 * Fragment containing all settings related to Sleep As Android alarm clock event handling
 * <p/>
 * Created by Markus on 08.10.2015.
 */
public class SleepAsAndroidFragment extends RecyclerViewFragment<Action> {

    private static Event currentEventType = Event.ALARM_TRIGGERED;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerViewActions;
    ActionRecyclerViewAdapter recyclerViewAdapter;
    @BindView(R.id.spinner_sleep_as_android_event)
    Spinner              spinnerEventType;
    @BindView(R.id.add_fab)
    FloatingActionButton addActionFAB;
    @BindView(R.id.layout_installed)
    LinearLayout         layout_installed;
    @BindView(R.id.layout_not_installed)
    LinearLayout         layout_not_installed;
    @BindView(R.id.get_from_play_store)
    IconicsImageView     getFromPlayStore;
    @BindView(R.id.switch_on_off)
    Switch               switchOnOff;
    private ArrayList<Action> actions = new ArrayList<>();

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        final RecyclerViewFragment recyclerViewFragment = this;

        switchOnOff.setChecked(SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SLEEP_AS_ANDROID_ENABLED));
        switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SLEEP_AS_ANDROID_ENABLED, isChecked);
                }
            }
        });

        getFromPlayStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SleepAsAndroidHelper.openPlayStorePage(getActivity());
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sleep_as_android_event_names,
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

        recyclerViewAdapter = new ActionRecyclerViewAdapter(getContext(), actions);
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
                                    DatabaseHandler.setAlarmActions(currentEventType, actions);
                                    StatusMessageHandler.showInfoMessage(recyclerViewFragment.getRecyclerView(),
                                            R.string.action_removed,
                                            Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(recyclerViewFragment.getRecyclerView(), e);
                                }

                                recyclerViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();
            }
        });
        recyclerViewActions.setAdapter(recyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewActions.setLayoutManager(layoutManager);

        addActionFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        addActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddSleepAsAndroidAlarmEventActionDialog addAlarmEventActionDialog = AddSleepAsAndroidAlarmEventActionDialog.newInstance(
                        currentEventType.getId());
                addAlarmEventActionDialog.setTargetFragment(recyclerViewFragment, 0);
                addAlarmEventActionDialog.show(getActivity().getSupportFragmentManager(), null);
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
        return R.layout.fragment_sleep_as_android;
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
                currentEventType = Event.getById(Event.ALARM_SNOOZED.getId());
                break;
            case 2:
                currentEventType = Event.getById(Event.ALARM_DISMISSED.getId());
                break;
        }

        updateListContent();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            addActionFAB.setVisibility(View.GONE);
        } else {
            addActionFAB.setVisibility(View.VISIBLE);
        }

        if (SleepAsAndroidHelper.isInstalled(getActivity())) {
            layout_installed.setVisibility(View.VISIBLE);
            layout_not_installed.setVisibility(View.GONE);
        } else {
            layout_installed.setVisibility(View.GONE);
            layout_not_installed.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.add_action:
                AddSleepAsAndroidAlarmEventActionDialog addAlarmEventActionDialog = AddSleepAsAndroidAlarmEventActionDialog.newInstance(
                        spinnerEventType.getSelectedItemPosition());
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

        if (!SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            menu.findItem(R.id.add_action)
                    .setVisible(false)
                    .setEnabled(false);
        }
    }


    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewActions;
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return recyclerViewAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.action_grid_span_count);
    }

//    @Override
//    protected int getLoaderId() {
//        return 0;
//    }

    @Override
    public List<Action> loadListData() throws Exception {
        if (DeveloperPreferencesHandler.getPlayStoreMode()) {
            PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getContext());
            return playStoreModeDataModel.getAlarmActions(currentEventType);
        } else {
            return DatabaseHandler.getAlarmActions(currentEventType);
        }
    }

    @Override
    protected void onListDataChanged(List<Action> list) {
        actions.clear();
        actions.addAll(list);
    }
}

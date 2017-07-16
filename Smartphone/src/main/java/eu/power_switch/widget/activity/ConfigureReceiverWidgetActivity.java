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

package eu.power_switch.widget.activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.activity.butterknife.ButterKnifeDialogActivity;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.widget.ReceiverWidget;
import eu.power_switch.widget.WidgetIntentReceiver;
import timber.log.Timber;

import static eu.power_switch.persistence.shared_preferences.SmartphonePreferenceItem.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON;

/**
 * Configuration Activity for Receiver widgets
 */
public class ConfigureReceiverWidgetActivity extends ButterKnifeDialogActivity {

    @BindView(R.id.spinner_widgetApartment)
    Spinner               spinnerApartment;
    @BindView(R.id.spinner_widgetRoom)
    Spinner               spinnerRoom;
    @BindView(R.id.spinner_widgetReceiver)
    Spinner               spinnerReceiver;
    @BindView(R.id.button_widgetSave)
    android.widget.Button buttonSave;

    @Inject
    PersistenceHandler persistenceHandler;

    private List<Apartment> apartmentList = new ArrayList<>();

    private ArrayList<String> apartmentNameList = new ArrayList<>();
    private ArrayList<String> roomNameList      = new ArrayList<>();
    private ArrayList<String> receiverNameList  = new ArrayList<>();

    private ArrayAdapter<String> adapterApartments;
    private ArrayAdapter<String> adapterRooms;
    private ArrayAdapter<String> adapterReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapterApartments = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, apartmentNameList);
        adapterApartments.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApartment.setAdapter(adapterApartments);
        SpinnerInteractionListener apartmentSpinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateRoomList();
            }
        };
        spinnerApartment.setOnItemSelectedListener(apartmentSpinnerInteractionListener);
        spinnerApartment.setOnTouchListener(apartmentSpinnerInteractionListener);

        adapterRooms = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roomNameList);
        adapterRooms.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoom.setAdapter(adapterRooms);
        SpinnerInteractionListener roomSpinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateReceiverList();
            }
        };
        spinnerRoom.setOnItemSelectedListener(roomSpinnerInteractionListener);
        spinnerRoom.setOnTouchListener(roomSpinnerInteractionListener);

        adapterReceiver = new ArrayAdapter<>(ConfigureReceiverWidgetActivity.this, android.R.layout.simple_spinner_dropdown_item, receiverNameList);
        adapterReceiver.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReceiver.setAdapter(adapterReceiver);

        buttonSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentConfiguration();
            }
        });

        updateUI();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.widget_dialog_configure_receiver;
    }

    private void updateUI() {
        new AsyncTask<Void, Void, List<Apartment>>() {
            @Override
            protected List<Apartment> doInBackground(Void... params) {
                try {
                    return persistenceHandler.getAllApartments();
                } catch (Exception e) {
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Apartment> result) {
                apartmentList.clear();
                apartmentList.addAll(result);

                for (Apartment apartment : apartmentList) {
                    apartmentNameList.add(apartment.getName());
                }

                spinnerApartment.setSelection(0);
                adapterApartments.notifyDataSetChanged();

                updateRoomList();

                // Abort if no rooms are defined in main app
                if (receiverNameList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_define_receiver_in_main_app), Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Apartment getSelectedApartment() throws Exception {
        return apartmentList.get(spinnerApartment.getSelectedItemPosition());
    }

    private Room getSelectedRoom() throws Exception {
        return getSelectedApartment().getRoom(spinnerRoom.getSelectedItem()
                .toString());
    }

    private void updateRoomList() {
        roomNameList.clear();

        try {
            for (Room room : getSelectedApartment().getRooms()) {
                roomNameList.add(room.getName());
            }

            spinnerRoom.setSelection(0);

        } catch (Exception e) {
            Timber.e(e);
        }

        adapterRooms.notifyDataSetChanged();

        updateReceiverList();
    }

    private void updateReceiverList() {
        receiverNameList.clear();

        try {
            for (Receiver receiver : getSelectedRoom().getReceivers()) {
                receiverNameList.add(receiver.getName());
            }

            spinnerReceiver.setSelection(0);
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(this, e);
        }

        adapterReceiver.notifyDataSetChanged();
    }

    private void saveCurrentConfiguration() {
        try {
            // First, get the App Widget ID from the Intent that launched the Activity:
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                // Perform your App Widget configuration:
                Apartment selectedApartment = getSelectedApartment();
                Room selectedRoom = selectedApartment.getRoom(spinnerRoom.getSelectedItem()
                        .toString());
                Receiver selectedReceiver = selectedRoom.getReceiver(spinnerReceiver.getSelectedItem()
                        .toString());

                // save new widget data to database
                ReceiverWidget receiverWidget = new ReceiverWidget(appWidgetId, selectedRoom.getId(), selectedReceiver.getId());
                persistenceHandler.addReceiverWidget(receiverWidget);
                // When the configuration is complete, get an instance of
                // the AppWidgetManager by calling getInstance(Context):
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ConfigureReceiverWidgetActivity.this);
                // Update the App Widget with a RemoteViews layout by
                // calling updateAppWidget(int, RemoteViews):
                RemoteViews remoteViews = new RemoteViews(getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.widget_receiver);

                List<Button> buttons = selectedReceiver.getButtons();

                remoteViews.setTextViewText(R.id.textView_receiver_widget_name,
                        selectedApartment.getName() + ": " + selectedRoom.getName() + ": " + selectedReceiver.getName());

                int buttonOffset = 0;
                for (Button button : buttons) {
                    // set button action
                    RemoteViews buttonView = new RemoteViews(getString(eu.power_switch.shared.R.string.PACKAGE_NAME),
                            R.layout.widget_receiver_button_layout);
                    SpannableString s = new SpannableString(button.getName());
                    s.setSpan(new StyleSpan(Typeface.BOLD),
                            0,
                            button.getName()
                                    .length(),
                            0);
                    buttonView.setTextViewText(R.id.button_widget_universal, s);

                    boolean highlightLastButton = smartphonePreferencesHandler.get(KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
                    if (highlightLastButton && selectedReceiver.getLastActivatedButtonId()
                            .equals(button.getId())) {
                        buttonView.setTextColor(R.id.button_widget_universal,
                                ContextCompat.getColor(getApplicationContext(), R.color.color_light_blue_a700));
                    }

                    PendingIntent pendingIntent = WidgetIntentReceiver.buildReceiverWidgetActionPendingIntent(getApplicationContext(),
                            selectedApartment,
                            selectedRoom,
                            selectedReceiver,
                            button,
                            appWidgetId * 15 + buttonOffset);

                    buttonView.setOnClickPendingIntent(R.id.button_widget_universal, pendingIntent);

                    remoteViews.addView(R.id.linearlayout_receiver_widget, buttonView);
                    buttonOffset++;
                }

                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

                // Finally, create the return Intent, set it with the
                // Activity result, and finish the Activity:
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(this, e);
        }
    }
}
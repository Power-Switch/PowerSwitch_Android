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

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.log.Log;
import eu.power_switch.widget.ReceiverWidget;
import eu.power_switch.widget.WidgetIntentReceiver;

/**
 * Configuration Activity for Receiver widgets
 */
public class ConfigureReceiverWidgetActivity extends Activity {

    private Spinner spinnerRoom;
    private Spinner spinnerReceiver;

    private List<Room> roomsList;
    private ArrayList<String> roomNamesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.widget_dialog_configure_receiver);

        spinnerRoom = (Spinner) findViewById(R.id.Spinner_widgetRoom);
        spinnerReceiver = (Spinner) findViewById(R.id.spinner_widgetSwitch);

        try {
            roomsList = DatabaseHandler.getAllRooms();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(this, e);
        }
        roomNamesList = new ArrayList<>();
        for (Room room : roomsList) {
            roomNamesList.add(room.getName());
        }

        // Abort if no rooms are defined in main app
        if (roomNamesList.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_define_receiver_in_main_app), Toast.LENGTH_LONG).show();
            finish();
        }

        ArrayAdapter<String> adapterRooms = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, roomNamesList);
        adapterRooms.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoom.setAdapter(adapterRooms);
        spinnerRoom.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                try {
                    ArrayList<String> receiverList = new ArrayList<>();
                    for (Receiver receiver : DatabaseHandler.getReceiverByRoomId(roomsList.get(arg2).getId())) {
                        receiverList.add(receiver.getName());
                    }
                    ArrayAdapter<String> adapterReceiver = new ArrayAdapter<>(ConfigureReceiverWidgetActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, receiverList);
                    adapterReceiver.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerReceiver.setAdapter(adapterReceiver);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(arg1.getContext(), e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        android.widget.Button save = (android.widget.Button) findViewById(R.id.button_widgetSave);
        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentConfiguration();
            }
        });
    }

    private void saveCurrentConfiguration() {
        // First, get the App Widget ID from the Intent that launched the Activity:
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            // Perform your App Widget configuration:
            Room selectedRoom = roomsList.get(spinnerRoom.getSelectedItemPosition());
            Receiver selectedReceiver = null;
            for (Receiver receiver : selectedRoom.getReceivers()) {
                if (receiver.getName().equals(spinnerReceiver.getSelectedItem().toString())) {
                    selectedReceiver = receiver;
                    break;
                }
            }

            // save new widget data to database
            ReceiverWidget receiverWidget = new ReceiverWidget(appWidgetId, selectedRoom.getId(),
                    selectedReceiver.getId());
            try {
                DatabaseHandler.addReceiverWidget(receiverWidget);
            } catch (Exception e) {
                Log.e(e);
            }
            // When the configuration is complete, get an instance of
            // the AppWidgetManager by calling getInstance(Context):
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(ConfigureReceiverWidgetActivity.this);
            // Update the App Widget with a RemoteViews layout by
            // calling updateAppWidget(int, RemoteViews):
            RemoteViews remoteViews = new RemoteViews(getResources().getString(eu.power_switch.shared.R.string.PACKAGE_NAME),
                    R.layout.widget_receiver);

            LinkedList<Button> buttons = selectedReceiver.getButtons();

            remoteViews.setTextViewText(R.id.textView_receiver_widget_name, selectedRoom.getName() + ": " +
                    selectedReceiver.getName());

            int buttonOffset = 0;
            for (Button button : buttons) {
                // set button action
                RemoteViews buttonView = new RemoteViews(getApplicationContext().getResources()
                        .getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.widget_receiver_button_layout);
                SpannableString s = new SpannableString(button.getName());
                s.setSpan(new StyleSpan(Typeface.BOLD), 0, button.getName().length(), 0);
                buttonView.setTextViewText(R.id.button_widget_universal, s);
                if (SmartphonePreferencesHandler.getHighlightLastActivatedButton() && selectedReceiver
                        .getLastActivatedButtonId().equals(button.getId())) {
                    buttonView.setTextColor(R.id.button_widget_universal,
                            ContextCompat.getColor(getApplicationContext(), R.color.color_light_blue_a700));
                }

                PendingIntent pendingIntent = WidgetIntentReceiver.buildReceiverWidgetActionPendingIntent(getApplicationContext(), selectedRoom,
                        selectedReceiver, button, appWidgetId * 15 + buttonOffset);

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
    }
}
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
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.log.Log;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.widget.ReceiverWidget;
import eu.power_switch.widget.WidgetIntentReceiver;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;

/**
 * Configuration Activity for Receiver widgets
 */
public class ConfigureReceiverWidgetActivity extends Activity {

    private Spinner spinnerRoom;
    private Spinner spinnerReceiver;

    private List<Room> roomsList;

    /**
     * Forces an Update of all Receiver Widgets
     *
     * @param context
     */
    public static void forceWidgetUpdate(Context context) {
        // update receiver widgets
        Intent intent = new Intent(context, ReceiverWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(),
                        ReceiverWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ConfigureReceiverWidgetActivity");
        super.onCreate(savedInstanceState);
        DatabaseHandler.init(this);

        setContentView(R.layout.widget_dialog_configure_receiver);

        spinnerRoom = (Spinner) findViewById(R.id.Spinner_widgetRoom);
        spinnerReceiver = (Spinner) findViewById(R.id.spinner_widgetSwitch);

        roomsList = DatabaseHandler.getAllRooms();

        ArrayList<String> roomNamesList = new ArrayList<>();
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

                ArrayList<String> receiverList = new ArrayList<>();
                for (Receiver receiver : DatabaseHandler.getReceiverByRoomId(roomsList.get(arg2).getId())) {
                    receiverList.add(receiver.getName());
                }
                ArrayAdapter<String> adapterReceiver = new ArrayAdapter<>(ConfigureReceiverWidgetActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, receiverList);
                adapterReceiver.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerReceiver.setAdapter(adapterReceiver);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        Button save = (Button) findViewById(R.id.button_widgetSave);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
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
                    DatabaseHandler.addReceiverWidget(receiverWidget);
                    // When the configuration is complete, get an instance of
                    // the AppWidgetManager by calling getInstance(Context):
                    AppWidgetManager appWidgetManager = AppWidgetManager
                            .getInstance(ConfigureReceiverWidgetActivity.this);
                    // Update the App Widget with a RemoteViews layout by
                    // calling updateAppWidget(int, RemoteViews):
                    RemoteViews remoteViews = new RemoteViews(getResources().getString(eu.power_switch.shared.R.string.PACKAGE_NAME),
                            R.layout.widget_receiver);

                    LinkedList<eu.power_switch.obj.Button> buttons = selectedReceiver.getButtons();

                    remoteViews.setTextViewText(R.id.textView_receiver_widget_name, selectedRoom.getName() + ": " +
                            selectedReceiver.getName());

                    SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());

                    int buttonOffset = 0;
                    for (eu.power_switch.obj.Button button : buttons) {
                        // set button action
                        RemoteViews buttonView = new RemoteViews(getApplicationContext().getResources()
                                .getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.receiver_widget_button_layout);
                        SpannableString s = new SpannableString(button.getName());
                        s.setSpan(new StyleSpan(Typeface.BOLD), 0, button.getName().length(), 0);
                        buttonView.setTextViewText(R.id.button_widget_universal, s);
                        if (sharedPreferencesHandler.getHighlightLastActivatedButton() &&
                                DatabaseHandler.getLastActivatedButtonId(selectedReceiver.getId()) == button.getId()) {
                            buttonView.setTextColor(R.id.button_widget_universal,
                                    ContextCompat.getColor(getApplicationContext(), R.color.accent_blue_a700));
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
        });
    }
}
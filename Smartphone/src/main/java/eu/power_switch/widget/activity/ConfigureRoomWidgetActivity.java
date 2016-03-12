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
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.obj.Room;
import eu.power_switch.shared.log.Log;
import eu.power_switch.widget.RoomWidget;

/**
 * Configuration Activity for Room widgets
 */
public class ConfigureRoomWidgetActivity extends Activity {

    public static final int ROOM_INTENT_ID_OFFSET = 20000;

    private Spinner spinnerRoom;
    private List<Room> roomList = new ArrayList<>();
    private ArrayList<String> roomNamesList = new ArrayList<>();
    private ArrayAdapter<String> adapterRooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.widget_dialog_configure_room);

        spinnerRoom = (Spinner) findViewById(R.id.Spinner_widgetRoom);
        adapterRooms = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, roomNamesList);
        adapterRooms.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoom.setAdapter(adapterRooms);

        Button save = (Button) findViewById(R.id.button_widgetSave);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveCurrentConfiguration();
            }
        });

        updateUI();
    }

    private void updateUI() {
        new AsyncTask<Void, Void, List<Room>>() {
            @Override
            protected List<Room> doInBackground(Void... params) {
                try {
                    return DatabaseHandler.getAllRooms();
                } catch (Exception e) {
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Room> result) {
                roomList.clear();
                roomList.addAll(result);

                if (roomList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_define_room_in_main_app), Toast.LENGTH_LONG).show();
                    finish();
                }

                // Abort if no rooms are defined in main app
                if (roomList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_define_receiver_in_main_app), Toast.LENGTH_LONG).show();
                    finish();
                }

                for (Room room : roomList) {
                    roomNamesList.add(room.getName());
                }

                adapterRooms.notifyDataSetChanged();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void saveCurrentConfiguration() {
        // First, get the App Widget ID from the Intent that launched the Activity:
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            // Perform your App Widget configuration:
            Room room = roomList.get(spinnerRoom.getSelectedItemPosition());
            // save new widget data to database
            RoomWidget roomWidget = new RoomWidget(appWidgetId, room.getId());
            try {
                DatabaseHandler.addRoomWidget(roomWidget);
            } catch (Exception e) {
                Log.e(e);
            }

            // When the configuration is complete, get an instance of
            // the AppWidgetManager by calling getInstance(Context):
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ConfigureRoomWidgetActivity.this);
            // Update the App Widget with a RemoteViews layout by
            // calling updateAppWidget(int, RemoteViews):
            RemoteViews views = new RemoteViews(getResources().getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.widget_room);
            // update UI
            views.setTextViewText(R.id.textView_room_widget_name, room.getName());
            // set button action
//            views.setOnClickPendingIntent(R.id.button_on,
//                    WidgetIntentReceiver.buildRoomWidgetButtonPendingIntent(getApplicationContext(), room,
//                            getString(R.string.on), ROOM_INTENT_ID_OFFSET + appWidgetId));
//            views.setOnClickPendingIntent(R.id.button_off,
//                    WidgetIntentReceiver.buildRoomWidgetButtonPendingIntent(getApplicationContext(),
//                            room, getString(R.string.off), ROOM_INTENT_ID_OFFSET + appWidgetId + 1));
//            views.setViewVisibility(R.id.linearlayout_room_widget, View.VISIBLE);


            appWidgetManager.updateAppWidget(appWidgetId, views);

            // Finally, create the return Intent, set it with the
            // Activity result, and finish the Activity:
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    }
}
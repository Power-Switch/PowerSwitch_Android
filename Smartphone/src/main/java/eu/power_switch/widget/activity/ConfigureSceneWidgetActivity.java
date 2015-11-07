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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import eu.power_switch.log.Log;
import eu.power_switch.obj.Scene;
import eu.power_switch.widget.SceneWidget;
import eu.power_switch.widget.WidgetIntentReceiver;
import eu.power_switch.widget.provider.SceneWidgetProvider;

/**
 * Configuration Activity for Scene widgets
 */
public class ConfigureSceneWidgetActivity extends Activity {

    public static final int SCENE_INTENT_ID_OFFSET = 10000;

    private Spinner spinnerScene;
    private List<Scene> scenesList;

    /**
     * Forces an Update of all Scene Widgets
     *
     * @param context
     */
    public static void forceWidgetUpdate(Context context) {
        // update scene widgets
        Intent intent = new Intent(context, SceneWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(),
                        SceneWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ConfigureSceneWidgetActivity");
        super.onCreate(savedInstanceState);

        DatabaseHandler.init(this);

        setContentView(R.layout.widget_dialog_configure_scene);

        scenesList = DatabaseHandler.getAllScenes();
        spinnerScene = (Spinner) findViewById(R.id.Spinner_widgetScene);

        ArrayList<String> sceneNamesList = new ArrayList<>();
        for (Scene scene : scenesList) {
            sceneNamesList.add(scene.getName());
        }
        if (sceneNamesList.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_define_scene_in_main_app), Toast.LENGTH_LONG).show();
            finish();
        }

        ArrayAdapter<String> adapterScenes = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, sceneNamesList);
        adapterScenes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerScene.setAdapter(adapterScenes);

        Button save = (Button) findViewById(R.id.button_widgetSave);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // First, get the App Widget ID from the Intent that launched the Activity:
                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                    int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                    // Perform your App Widget configuration:
                    Scene scene = scenesList.get(spinnerScene.getSelectedItemPosition());
                    // save new widget data to database
                    SceneWidget sceneWidget = new SceneWidget(appWidgetId, scene.getId());
                    DatabaseHandler.addSceneWidget(sceneWidget);

                    // When the configuration is complete, get an instance of
                    // the AppWidgetManager by calling getInstance(Context):
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ConfigureSceneWidgetActivity.this);
                    // Update the App Widget with a RemoteViews layout by
                    // calling updateAppWidget(int, RemoteViews):
                    RemoteViews views = new RemoteViews(getResources().getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.widget_scene);
                    views.setTextViewText(R.id.buttonActivate_scene_widget, getString(R.string.activate));
                    views.setTextViewText(R.id.textView_scene_widget_name, scene.getName());
                    views.setOnClickPendingIntent(R.id.buttonActivate_scene_widget,
                            WidgetIntentReceiver.buildSceneWidgetPendingIntent(getApplicationContext(), scene, SCENE_INTENT_ID_OFFSET + appWidgetId));
                    appWidgetManager.updateAppWidget(appWidgetId, views);

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
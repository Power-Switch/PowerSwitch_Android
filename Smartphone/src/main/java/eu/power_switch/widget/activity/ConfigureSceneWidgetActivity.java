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

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.butterknife.ButterKnifeDialogActivity;
import eu.power_switch.gui.listener.SpinnerInteractionListener;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Scene;
import eu.power_switch.persistence.PersistanceHandler;
import eu.power_switch.widget.SceneWidget;
import eu.power_switch.widget.WidgetIntentReceiver;
import timber.log.Timber;

/**
 * Configuration Activity for Scene widgets
 */
public class ConfigureSceneWidgetActivity extends ButterKnifeDialogActivity {

    public static final int SCENE_INTENT_ID_OFFSET = 10000;

    @BindView(R.id.spinner_widgetApartment)
    Spinner spinnerApartment;
    @BindView(R.id.spinner_widgetScene)
    Spinner spinnerScene;

    @BindView(R.id.button_widgetSave)
    Button buttonSave;

    @Inject
    PersistanceHandler persistanceHandler;

    private List<Apartment> apartmentList = new ArrayList<>();
    private List<Scene>     sceneList     = new ArrayList<>();

    private ArrayList<String> apartmentNameList = new ArrayList<>();
    private ArrayList<String> sceneNamesList    = new ArrayList<>();

    private ArrayAdapter<String> adapterApartments;
    private ArrayAdapter<String> adapterScenes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapterApartments = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, apartmentNameList);
        adapterApartments.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApartment.setAdapter(adapterApartments);
        SpinnerInteractionListener apartmentSpinnerInteractionListener = new SpinnerInteractionListener() {
            @Override
            public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
                updateSceneList();
            }
        };
        spinnerApartment.setOnItemSelectedListener(apartmentSpinnerInteractionListener);
        spinnerApartment.setOnTouchListener(apartmentSpinnerInteractionListener);

        adapterScenes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sceneNamesList);
        adapterScenes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerScene.setAdapter(adapterScenes);

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
        return R.layout.widget_dialog_configure_scene;
    }

    private void updateUI() {
        new AsyncTask<Void, Void, List<Apartment>>() {
            @Override
            protected List<Apartment> doInBackground(Void... params) {
                try {
                    return persistanceHandler.getAllApartments();
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

                updateSceneList();

                // Abort if no rooms are defined in main app
                if (sceneList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_define_scene_in_main_app), Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        new AsyncTask<Void, Void, List<Scene>>() {
            @Override
            protected List<Scene> doInBackground(Void... params) {
                try {
                    return persistanceHandler.getAllScenes();
                } catch (Exception e) {
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Scene> result) {

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Apartment getSelectedApartment() throws Exception {
        return apartmentList.get(spinnerApartment.getSelectedItemPosition());
    }

    private Scene getSelectedScene() {
        return sceneList.get(spinnerScene.getSelectedItemPosition());
    }

    private void updateSceneList() {
        sceneList.clear();
        sceneNamesList.clear();

        try {
            sceneList.addAll(getSelectedApartment().getScenes());

            for (Scene scene : sceneList) {
                sceneNamesList.add(scene.getName());
            }

            spinnerScene.setSelection(0);
        } catch (Exception e) {
            Timber.e(e);
        }

        adapterScenes.notifyDataSetChanged();
    }

    private void saveCurrentConfiguration() {
        try {
            // First, get the App Widget ID from the Intent that launched the Activity:
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                // Perform your App Widget configuration:
                Apartment apartment = getSelectedApartment();
                Scene     scene     = getSelectedScene();
                // save new widget data to database
                SceneWidget sceneWidget = new SceneWidget(appWidgetId, scene.getId());
                try {
                    persistanceHandler.addSceneWidget(sceneWidget);
                } catch (Exception e) {
                    Timber.e(e);
                }

                // When the configuration is complete, get an instance of
                // the AppWidgetManager by calling getInstance(Context):
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ConfigureSceneWidgetActivity.this);
                // Update the App Widget with a RemoteViews layout by
                // calling updateAppWidget(int, RemoteViews):
                RemoteViews views = new RemoteViews(getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.widget_scene);
                views.setTextViewText(R.id.buttonActivate_scene_widget, getString(R.string.activate));
                views.setTextViewText(R.id.textView_scene_widget_name, apartment.getName() + ": " + scene.getName());
                views.setOnClickPendingIntent(R.id.buttonActivate_scene_widget,
                        WidgetIntentReceiver.buildSceneWidgetPendingIntent(getApplicationContext(),
                                apartment,
                                scene,
                                SCENE_INTENT_ID_OFFSET + appWidgetId));
                appWidgetManager.updateAppWidget(appWidgetId, views);

                // Finally, create the return Intent, set it with the
                // Activity result, and finish the Activity:
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(this, e);
        }
    }
}
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

package eu.power_switch.widget.provider;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Arrays;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import eu.power_switch.R;
import eu.power_switch.database.handler.PersistanceHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Scene;
import eu.power_switch.widget.SceneWidget;
import eu.power_switch.widget.WidgetIntentReceiver;
import eu.power_switch.widget.activity.ConfigureSceneWidgetActivity;
import timber.log.Timber;

/**
 * This class is responsible for updating existing Scene widgets
 */
public class SceneWidgetProvider extends AppWidgetProvider {

    @Inject
    PersistanceHandler persistanceHandler;

    /**
     * Forces an Update of all Scene Widgets
     *
     * @param context any suitable context
     */
    public static void forceWidgetUpdate(Context context) {
        // update scene widgets
        Intent intent = new Intent(context, SceneWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(), SceneWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("Updating Scene Widgets...");
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getResources()
                    .getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.widget_scene);

            try {
                SceneWidget sceneWidget = persistanceHandler.getSceneWidget(appWidgetId);
                // update UI
                try {
                    Scene     scene     = persistanceHandler.getScene(sceneWidget.getSceneId());
                    Apartment apartment = persistanceHandler.getApartment(scene.getApartmentId());

                    remoteViews.setTextViewText(R.id.textView_scene_widget_name, apartment.getName() + ": " + scene.getName());
                    // set button action
                    remoteViews.setOnClickPendingIntent(R.id.buttonActivate_scene_widget,
                            WidgetIntentReceiver.buildSceneWidgetPendingIntent(context,
                                    apartment,
                                    scene,
                                    ConfigureSceneWidgetActivity.SCENE_INTENT_ID_OFFSET + appWidgetId));
                    remoteViews.setViewVisibility(R.id.buttonActivate_scene_widget, View.VISIBLE);
                } catch (NoSuchElementException e) {
                    remoteViews.setTextViewText(R.id.textView_scene_widget_name, context.getString(R.string.scene_not_found));
                    remoteViews.setViewVisibility(R.id.buttonActivate_scene_widget, View.GONE);
                }
            } catch (NoSuchElementException e) {
                remoteViews.setTextViewText(R.id.textView_scene_widget_name, context.getString(R.string.missing_widget_data));
                remoteViews.setViewVisibility(R.id.buttonActivate_scene_widget, View.GONE);
            } catch (Exception e) {
                Timber.e(e);
                remoteViews.setTextViewText(R.id.textView_scene_widget_name, context.getString(R.string.unknown_error));
                remoteViews.setViewVisibility(R.id.buttonActivate_scene_widget, View.GONE);
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Timber.d("Deleting Scene Widgets: " + Arrays.toString(appWidgetIds));

        for (int appWidgetId : appWidgetIds) {
            try {
                persistanceHandler.deleteSceneWidget(appWidgetId);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        super.onDeleted(context, appWidgetIds);
    }
}

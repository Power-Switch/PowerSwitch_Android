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

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Scene;
import eu.power_switch.shared.log.Log;
import eu.power_switch.widget.SceneWidget;
import eu.power_switch.widget.WidgetIntentReceiver;
import eu.power_switch.widget.activity.ConfigureSceneWidgetActivity;

/**
 * This class is responsible for updating existing Scene widgets
 */
public class SceneWidgetProvider extends AppWidgetProvider {

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
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(),
                        SceneWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("Updating Scene Widgets...");
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getResources()
                    .getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.widget_scene);

            try {
                SceneWidget sceneWidget = DatabaseHandler.getSceneWidget(appWidgetId);
                // update UI
                Scene scene = DatabaseHandler.getScene(sceneWidget.getSceneId());
                if (scene != null) {
                    Apartment apartment = DatabaseHandler.getApartment(scene.getApartmentId());

                    remoteViews.setTextViewText(R.id.textView_scene_widget_name, apartment.getName() + ": " + scene.getName());
                    // set button action
                    remoteViews.setOnClickPendingIntent(R.id.buttonActivate_scene_widget,
                            WidgetIntentReceiver.buildSceneWidgetPendingIntent(context, apartment, scene, ConfigureSceneWidgetActivity.SCENE_INTENT_ID_OFFSET + appWidgetId));
                    remoteViews.setViewVisibility(R.id.buttonActivate_scene_widget, View.VISIBLE);
                } else {
                    remoteViews.setTextViewText(R.id.textView_scene_widget_name, context.getString(R.string.scene_not_found));
                    remoteViews.setViewVisibility(R.id.buttonActivate_scene_widget, View.GONE);
                }
            } catch (Exception e) {
                Log.e(e);
                remoteViews.setTextViewText(R.id.textView_scene_widget_name, context.getString(R.string.unknown_error));
                remoteViews.setViewVisibility(R.id.buttonActivate_scene_widget, View.GONE);
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d("Deleting Scene Widgets: " + Arrays.toString(appWidgetIds));

        for (int appWidgetId : appWidgetIds) {
            try {
                DatabaseHandler.deleteSceneWidget(appWidgetId);
            } catch (Exception e) {
                Log.e(e);
            }
        }
        super.onDeleted(context, appWidgetIds);
    }
}

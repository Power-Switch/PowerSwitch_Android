/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog.configuration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.holder.SceneConfigurationHolder;
import eu.power_switch.gui.fragment.configure_scene.ConfigureSceneDialogPage1Name;
import eu.power_switch.gui.fragment.configure_scene.ConfigureSceneDialogTabbedPage2Setup;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.obj.Scene;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.SceneWidgetProvider;
import timber.log.Timber;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID;


/**
 * Dialog to create or modify a Scene
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureSceneDialog extends ConfigurationDialogTabbed<SceneConfigurationHolder> {

    public static ConfigureSceneDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(null, targetFragment);
    }

    public static ConfigureSceneDialog newInstance(Scene scene, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureSceneDialog     fragment                 = new ConfigureSceneDialog();
        SceneConfigurationHolder sceneConfigurationHolder = new SceneConfigurationHolder();
        if (scene != null) {
            sceneConfigurationHolder.setScene(scene);
        }
        fragment.setConfiguration(sceneConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeFromExistingData(Bundle arguments) throws Exception {
        Scene scene = getConfiguration().getScene();

        if (scene != null) {
            // init dialog using existing scene
            getConfiguration().setName(scene.getName());
            getConfiguration().setSceneItems(scene.getSceneItems());
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_scene;
    }

    @Override
    protected void addPageEntries(List<PageEntry<SceneConfigurationHolder>> pageEntries) {
        pageEntries.add(new PageEntry<>(R.string.name, ConfigureSceneDialogPage1Name.class));
        pageEntries.add(new PageEntry<>(R.string.setup, ConfigureSceneDialogTabbedPage2Setup.class));
    }

    @Override
    protected void saveConfiguration() throws Exception {
        Timber.d("Saving Scene...");

        long sceneId = -1;
        if (getConfiguration().getScene() != null) {
            sceneId = getConfiguration().getScene()
                    .getId();
        }

        long  apartmentId = smartphonePreferencesHandler.getValue(KEY_CURRENT_APARTMENT_ID);
        Scene newScene    = new Scene(sceneId, apartmentId, getConfiguration().getName());
        newScene.addSceneItems(getConfiguration().getSceneItems());

        if (getConfiguration().getScene() == null) {
            persistenceHandler.addScene(newScene);
        } else {
            persistenceHandler.updateScene(newScene);
        }

        // notify scenes fragment
        ScenesFragment.notifySceneChanged();

        // update scene widgets
        SceneWidgetProvider.forceWidgetUpdate(getActivity());

        // update wear data
        UtilityService.forceWearDataUpdate(getActivity());
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        persistenceHandler.deleteScene(getConfiguration().getScene()
                .getId());

        // notify scenes fragment
        ScenesFragment.notifySceneChanged();

        // update scene widgets
        SceneWidgetProvider.forceWidgetUpdate(getActivity());

        // update wear data
        UtilityService.forceWearDataUpdate(getActivity());
    }
}

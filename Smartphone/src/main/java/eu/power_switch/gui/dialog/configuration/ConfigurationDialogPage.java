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

package eu.power_switch.gui.dialog.configuration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Constructor;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.fragment.eventbus.EventBusFragment;
import eu.power_switch.shared.event.ConfigurationChangedEvent;
import timber.log.Timber;

/**
 * Created by Markus on 25.03.2016.
 */
public abstract class ConfigurationDialogPage<Configuration extends ConfigurationHolder> extends EventBusFragment {

    @BindView(R.id.contentView)
    @Nullable
    View contentView;

    private ConfigurationDialogTabbed<Configuration> parentDialog;

    /**
     * Use this method to instantiate a page used in a (multipage) configuration dialog
     *
     * @param clazz        the page class that should be instantiated
     * @param parentDialog the parent configuration dialog
     *
     * @return Instance of the configuration dialog page
     */
    public static <T extends ConfigurationDialogPage<Configuration>, Configuration extends ConfigurationHolder> ConfigurationDialogPage newInstance(
            @NonNull Class<T> clazz, @NonNull ConfigurationDialogTabbed<Configuration> parentDialog) {
        Bundle args = new Bundle();

        if (!ConfigurationDialogPage.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Invalid class type! Must be of type " + ConfigurationDialogPage.class.getName() + " or subclass!");
        }

        try {
            Constructor<T>                         constructor = clazz.getConstructor();
            ConfigurationDialogPage<Configuration> fragment    = constructor.newInstance();
            fragment.setParentConfigurationDialog(parentDialog);
            fragment.setArguments(args);
            return fragment;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't instantiate configuration page!", e);
        }
    }

    /**
     * Used to notify parent Dialog that configuration has changed
     */
    public void notifyConfigurationChanged() {
        EventBus.getDefault()
                .post(new ConfigurationChangedEvent());
    }

    /**
     * Get content view of this ConfigurationDialogPage
     * <p/>
     * This view should be declared with the id "contentView" in the layout definition of this
     * content fragment.
     * If no such view can be found it will default to the "getView()" method of the Fragment,
     * which should be the outermost dialog window view.
     *
     * @return view with Id "contentView" if defined, dialog fragment view otherwise
     */
    @Nullable
    public View getContentView() {
        if (contentView == null) {
            Timber.w("ContentView is null! Did you define a view with id \"contentView\" in your layout? Using getView() as fallback.");

            if (getView() == null) {
                Timber.w("View is null!");
            }
            return getView();
        } else {
            return contentView;
        }
    }

    /**
     * Get the configuration for this Dialog
     *
     * @return configuration
     */
    public Configuration getConfiguration() {
        return getParentConfigurationDialog().getConfiguration();
    }

    /**
     * Get the parent dialog of this page
     *
     * @return parent ConfigurationDialogTabbed
     */
    public ConfigurationDialogTabbed<Configuration> getParentConfigurationDialog() {
        if (parentDialog == null) {
            throw new IllegalStateException(
                    "Missing parent dialog! Did you use ConfigurationDialogPage.newInstance(Class<T>, ConfigurationDialogTabbed) to instantiate your page?");
        }
        return parentDialog;
    }

    /**
     * Set the parent dialog of this page
     *
     * @param configurationDialogTabbed Dialog
     */
    public void setParentConfigurationDialog(@NonNull ConfigurationDialogTabbed<Configuration> configurationDialogTabbed) {
        this.parentDialog = configurationDialogTabbed;
    }
}

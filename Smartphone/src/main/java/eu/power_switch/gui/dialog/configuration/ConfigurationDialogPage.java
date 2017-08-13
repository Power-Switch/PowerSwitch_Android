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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.fragment.eventbus.EventBusFragment;
import eu.power_switch.persistence.PersistenceHandler;
import eu.power_switch.tutorial.TutorialHandler;
import timber.log.Timber;

/**
 * Created by Markus on 25.03.2016.
 */
public abstract class ConfigurationDialogPage<Configuration extends ConfigurationHolder> extends EventBusFragment {

    @Inject
    protected PersistenceHandler persistenceHandler;

    @Inject
    protected TutorialHandler tutorialHandler;

    @Inject
    protected IconicsHelper iconicsHelper;

    @BindView(R.id.contentView)
    @Nullable
    View contentView;

    private ConfigurationDialog<Configuration> parentDialog;

    /**
     * Use this method to instantiate a page used in a (multipage) configuration dialog
     *
     * @param clazz        the page class that should be instantiated
     * @param parentDialog the parent configuration dialog
     *
     * @return Instance of the configuration dialog page
     */
    public static <DialogPage extends ConfigurationDialogPage<Configuration>, Configuration extends ConfigurationHolder> ConfigurationDialogPage<Configuration> newInstance(
            @NonNull Class<DialogPage> clazz, @NonNull ConfigurationDialog<Configuration> parentDialog) {
        Bundle args = new Bundle();

        if (!ConfigurationDialogPage.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Invalid class type! Must be of type " + ConfigurationDialogPage.class.getName() + " or subclass!");
        }

        try {
            Constructor<DialogPage>                constructor = clazz.getConstructor();
            ConfigurationDialogPage<Configuration> fragment    = constructor.newInstance();
            fragment.setParentConfigurationDialog(parentDialog);
            fragment.setArguments(args);
            return fragment;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't instantiate configuration page!", e);
        }
    }

    /**
     * {@see ConfigurationDialogTabbed.notifyConfigurationChanged()}
     */
    public void notifyConfigurationChanged() {
        parentDialog.notifyConfigurationChanged();
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
    public ConfigurationDialog<Configuration> getParentConfigurationDialog() {
        if (parentDialog == null) {
            throw new IllegalStateException(
                    "Missing parent dialog! Did you use ConfigurationDialogPage.newInstance(Class<T>, ConfigurationDialogTabbed) to instantiate your page?");
        }
        return parentDialog;
    }

    /**
     * Set the parent dialog of this page
     *
     * @param configurationDialog Dialog
     */
    public void setParentConfigurationDialog(@NonNull ConfigurationDialog<Configuration> configurationDialog) {
        this.parentDialog = configurationDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        onRootViewInflated(inflater, container, savedInstanceState);

        showTutorial();

        return rootView;
    }

    /**
     * This method is called after the rootLayout was inflated and {@code onCreateView()} was executed.
     * If necessary inflate additional views and initialize other fragment data here.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    protected abstract void onRootViewInflated(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    /**
     * This method is called after {@code onCreateView()} and {@code onRootViewInflated()}.
     * Create and show tutorials that should be shown right after fragment creation in this method.
     */
    protected void showTutorial() {
        // Override this if you want to show a tutorial
    }
}

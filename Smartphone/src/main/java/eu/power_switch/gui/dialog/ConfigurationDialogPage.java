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

package eu.power_switch.gui.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import java.lang.reflect.Constructor;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.fragment.ButterKnifeFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import timber.log.Timber;

/**
 * Created by Markus on 25.03.2016.
 */
public abstract class ConfigurationDialogPage extends ButterKnifeFragment {

    protected ConfigurationDialogTabbed configurationDialogTabbed;

    @BindView(R.id.contentView)
    View contentView;

    /**
     * Use this method to instantiate a page used in a (multipage) configuration dialog
     *
     * @param clazz        the page class that should be instantiated
     * @param parentDialog the parent configuration dialog
     *
     * @return Instance of the configuration dialog page
     */
    public static <T extends ConfigurationDialogPage> ConfigurationDialogPage newInstance(@NonNull Class<T> clazz,
                                                                                          ConfigurationDialogTabbed parentDialog) {
        Bundle args = new Bundle();

        if (!ConfigurationDialogPage.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Invalid class type! Must be of type " + ConfigurationDialogPage.class.getName() + " or subclass it!");
        }

        try {
            Constructor<T>          constructor = clazz.getConstructor();
            ConfigurationDialogPage fragment    = constructor.newInstance();
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
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_CONFIGURATION_DIALOG_CHANGED);
        LocalBroadcastManager.getInstance(getActivity())
                .sendBroadcast(intent);
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
     * Get the parent dialog of this page
     *
     * @return parent ConfigurationDialogTabbed
     */
    public ConfigurationDialogTabbed getParentConfigurationDialog() {
        if (configurationDialogTabbed == null) {
            throw new IllegalStateException(
                    "Missing parent dialog! Did you use ConfigurationDialogPage.newInstance(Class<T>, ConfigurationDialogTabbed) to instantiate your page?");
        }
        return configurationDialogTabbed;
    }

    /**
     * Set the parent dialog of this page
     *
     * @param configurationDialogTabbed Dialog
     */
    public void setParentConfigurationDialog(@NonNull ConfigurationDialogTabbed configurationDialogTabbed) {
        this.configurationDialogTabbed = configurationDialogTabbed;
    }
}
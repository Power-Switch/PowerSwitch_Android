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

package eu.power_switch.persistence.settings;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.markusressel.typedpreferences.PreferenceItem;
import eu.power_switch.gui.activity.ValueSelectorActivity;
import eu.power_switch.shared.persistence.preferences.WearablePreferencesHandler;

/**
 * SettingsItem for boolean type settings
 * <p/>
 * Created by Markus on 08.06.2016.
 */
public abstract class SingleSelectSettingsItem extends SettingsItem<Integer> {

    private final List<Integer> values            = new ArrayList<>();
    private final List<String>  valueDescriptions = new ArrayList<>();

    public SingleSelectSettingsItem(Context context, IconicsDrawable iconDrawable, @StringRes int description, PreferenceItem<Integer> preferenceItem,
                                    @ArrayRes int values, @ArrayRes int valueDescriptions,
                                    @NonNull WearablePreferencesHandler wearablePreferencesHandler) {
        super(context, iconDrawable, description, preferenceItem, wearablePreferencesHandler);

        int[] valuesArray = context.getResources()
                .getIntArray(values);
        for (int value : valuesArray) {
            this.values.add(value);
        }

        String[] valueDescriptionsArray = context.getResources()
                .getStringArray(valueDescriptions);
        Collections.addAll(this.valueDescriptions, valueDescriptionsArray);
    }

    @NonNull
    @Override
    public String getCurrentValueDescription() {
        return getValueDescription(getValue());
    }

    @NonNull
    @Override
    public String getValueDescription(Integer value) {
        return valueDescriptions.get(value);
    }

    /**
     * Opens GUI to select a new value from all possible valueDescriptions
     */
    public void showValueSelector() {
        ValueSelectorActivity.newInstance(context, getPreferenceItem(), values, valueDescriptions);
    }

}

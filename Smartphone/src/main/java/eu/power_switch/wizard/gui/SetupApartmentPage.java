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

package eu.power_switch.wizard.gui;

import android.os.Bundle;

import eu.power_switch.R;
import eu.power_switch.wizard.config.ConfigurationHolder;

/**
 * Setup page for entering a Apartment name
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class SetupApartmentPage extends SingleLineTextInputPage {

    public static SetupApartmentPage newInstance(ConfigurationHolder configurationHolder) {
        Bundle             args     = new Bundle();
        SetupApartmentPage fragment = new SetupApartmentPage();
        fragment.setArguments(args);
        fragment.setConfigurationHolder(configurationHolder);
        return fragment;
    }

    @Override
    protected void onSetUiValues() {
        super.onSetUiValues();

        setTitle(R.string.configure_apartment);
        setHint(R.string.wizard_setup_apartment_hint_home);
        setDescription(R.string.wizard_setup_apartment_explanation);
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.md_blue_700);
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        flashBackground(R.color.color_red_a700, 1000);
        showErrorMessage(getString(R.string.please_enter_name));
    }

}

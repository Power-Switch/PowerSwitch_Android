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

package eu.power_switch.gui.fragment.wizard;

import android.os.Bundle;

import eu.power_switch.R;

/**
 * Setup page for entering a Apartment name
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class SetupApartmentPage extends SingleLineTextInputPage {

    private boolean isValid = false;

    public static SetupApartmentPage newInstance() {
        Bundle args = new Bundle();
        SetupApartmentPage fragment = new SetupApartmentPage();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onSetUiValues() {
        super.onSetUiValues();

        setTitle(R.string.configure_apartment);
        setHint("Home");
        setDescription(R.string.tutorial__apartment_explanation);
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.md_blue_700);
    }

    @Override
    public void onInputChanged(CharSequence s, int start, int before, int count) {
        if (s.length() <= 0) {
            isValid = false;
        } else {
            isValid = true;
        }
    }

    @Override
    public boolean isPolicyRespected() {
        return isValid;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        flashBackground(R.color.color_red_a700, 1000);
        showErrorMessage(getString(R.string.please_enter_name));
    }

}

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

package eu.power_switch.wizard.gui;

import android.os.Bundle;

import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import eu.power_switch.R;

/**
 * "Apartments" page explaining the possibilities with apartments
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class ApartmentsPage extends BasicPage {

    public static ApartmentsPage newInstance() {
        Bundle         args     = new Bundle();
        ApartmentsPage fragment = new ApartmentsPage();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onSetUiValues() {
        super.onSetUiValues();
        setIcon(iconicsHelper.getWizardIcon(MaterialDesignIconic.Icon.gmi_home));
        setTitle(R.string.wizard_apartments_title);
        setDescription(R.string.wizard_apartments_description);
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.md_red_700);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.wizard_page_basic;
    }

}

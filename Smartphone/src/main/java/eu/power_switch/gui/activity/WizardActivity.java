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

package eu.power_switch.gui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;

import eu.power_switch.R;
import eu.power_switch.gui.fragment.wizard.BasicPage;
import eu.power_switch.shared.ThemeHelper;

import static eu.power_switch.gui.activity.MainActivity.getActivity;

/**
 * Wizard main activity
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class WizardActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int colorPrimary = ThemeHelper.getThemeAttrColor(getActivity(), R.attr.colorPrimary);
        int colorAccent = ThemeHelper.getThemeAttrColor(getActivity(), R.attr.colorAccent);

        BasicPage welcomePage = BasicPage.newInstance(colorPrimary,
                R.drawable.ic_launcher,
                R.string.powerswitch_app_name,
                R.string.tutorial__first_add_room);
        addSlide(welcomePage);

        BasicPage apartmentsPage = BasicPage.newInstance(colorAccent,
                R.drawable.ic_launcher,
                R.string.apartments,
                R.string.tutorial__apartment_explanation);
        addSlide(apartmentsPage);

        BasicPage roomsPage = BasicPage.newInstance(colorAccent,
                R.drawable.ic_launcher,
                R.string.rooms,
                R.string.tutorial__room_explanation);
        addSlide(roomsPage);

        setWizardMode(true);
        setButtonState(backButton, true); // enable back button
        setBackButtonVisibilityWithDone(true); // show back button on final page
        setColorTransitionsEnabled(true); // enable fancy color transitions

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        // close wizard
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}

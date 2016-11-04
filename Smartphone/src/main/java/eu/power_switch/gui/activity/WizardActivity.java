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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;

import eu.power_switch.gui.fragment.wizard.AdvancedFeaturesPage;
import eu.power_switch.gui.fragment.wizard.ApartmentsPage;
import eu.power_switch.gui.fragment.wizard.RoomsScenesPage;
import eu.power_switch.gui.fragment.wizard.SetupApartmentPage;
import eu.power_switch.gui.fragment.wizard.TimerAlarmClockPage;
import eu.power_switch.gui.fragment.wizard.WelcomePage;

/**
 * Wizard main activity
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class WizardActivity extends AppIntro {

    private static final int INITIAL_SETUP_PAGE_INDEX = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // welcome page
        addSlide(WelcomePage.newInstance());

        // info pages
        addSlide(ApartmentsPage.newInstance());
        addSlide(RoomsScenesPage.newInstance());
        addSlide(TimerAlarmClockPage.newInstance());
        addSlide(AdvancedFeaturesPage.newInstance());

        // initial setup pages
        addSlide(SetupApartmentPage.newInstance());
//        addSlide(SetupRoomPage.newInstance());
//        addSlide(SetupGatewayPage.newInstance());

        // finish page
//        addSlide(DonePage.newInstance());

        setWizardMode(true);
        setButtonState(skipButton, true); // enable skip button
        setColorTransitionsEnabled(true); // enable fancy color transitions
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

        if (pager.getCurrentItem() < getSetupPageIndex()) {
            pager.setCurrentItem(getSetupPageIndex(), true);
        } else {
            pager.setCurrentItem(pager.getChildCount() - 1, true);
        }
    }

    private int getSetupPageIndex() {
        return INITIAL_SETUP_PAGE_INDEX;
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
        if (pager.getCurrentItem() > 0) {
            setButtonState(skipButton, false); // disable skip button
            setButtonState(backButton, true); // disable skip button

        } else {
            setButtonState(skipButton, true); // enable skip button
            setButtonState(backButton, false); // disable skip button
        }
    }

    @Override
    public void onBackPressed() {
        if (pager == null || pager.getCurrentItem() <= 0) {
            super.onBackPressed();
        } else {
            // go back one page at a time
            pager.setCurrentItem(pager.getCurrentItem() - 1, true);
        }
    }

    public static Intent getLaunchIntent(Context context) {
        Intent intent = new Intent(context, WizardActivity.class);
        return intent;
    }
}

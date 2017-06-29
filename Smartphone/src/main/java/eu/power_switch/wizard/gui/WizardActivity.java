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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.github.paolorotolo.appintro.AppIntro;

import eu.power_switch.R;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.wizard.config.ConfigurationHolder;

/**
 * Wizard main activity
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class WizardActivity extends AppIntro {

    private static final int INITIAL_SETUP_PAGE_INDEX = 5;

    private final ConfigurationHolder configurationHolder = new ConfigurationHolder();

    /**
     * Get launch Intent for the Wizard
     *
     * @param context application context
     *
     * @return intent
     */
    public static Intent getLaunchIntent(Context context) {
        Intent intent = new Intent(context, WizardActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        addSlide(TestPage.newInstance());

        // welcome page
        addSlide(WelcomePage.newInstance());

        // info pages
        addSlide(ApartmentsPage.newInstance());
        addSlide(RoomsScenesPage.newInstance());
        addSlide(TimerAlarmClockPage.newInstance());
        addSlide(AdvancedFeaturesPage.newInstance());

        // initial setup pages
        addSlide(SetupApartmentPage.newInstance(configurationHolder));
        addSlide(SetupRoomPage.newInstance(configurationHolder));
        addSlide(SetupGatewayPage.newInstance(configurationHolder));

        // finish page
        addSlide(FinishPage.newInstance());

        setWizardMode(true);
        pager.setOffscreenPageLimit(mPagerAdapter.getCount());
        setButtonState(skipButton, true); // enable skip button
        setColorTransitionsEnabled(true); // enable fancy color transitions
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

        if (pager.getCurrentItem() == 0) {
            pager.setCurrentItem(getSetupPageIndex(), true);
            showSkipButton();
        } else {
            pager.setCurrentItem(mPagerAdapter.getCount() - 1, true);
            showBackButton();
        }
    }

    private int getSetupPageIndex() {
        return INITIAL_SETUP_PAGE_INDEX;
    }

    private void showBackButton() {
        setButtonState(skipButton, false); // disable skip button
        setButtonState(backButton, true); // disable skip button
    }

    private void showSkipButton() {
        setButtonState(skipButton, true); // enable skip button
        setButtonState(backButton, false); // disable skip button
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        configurationHolder.writeToDatabase();

        // disable wizard for future launches
        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SHOULD_SHOW_WIZARD, false);

        // close wizard
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        if (pager.getCurrentItem() == 0 || pager.getCurrentItem() == INITIAL_SETUP_PAGE_INDEX) {
            showSkipButton();
        } else {
            showBackButton();
        }

        if (newFragment instanceof FinishPage) {
            FinishPage finishPage = (FinishPage) newFragment;

            finishPage.onSuccess(R.string.wizard_finish_success);
            finishPage.onFailure(R.string.wizard_finish_failure);
        }
    }

    @Override
    public void onBackPressed() {
        if (pager == null) {
            super.onBackPressed();
        } else if (pager.getCurrentItem() <= 0) {
            new AlertDialog.Builder(this).setTitle(R.string.wizard_close)
                    .setMessage(R.string.wizard_close_message)
                    .setNeutralButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
        } else {
            // go back one page at a time
            pager.setCurrentItem(pager.getCurrentItem() - 1, true);
        }
    }
}

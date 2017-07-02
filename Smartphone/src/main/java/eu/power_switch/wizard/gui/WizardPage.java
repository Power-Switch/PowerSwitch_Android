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

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;

import eu.power_switch.R;
import eu.power_switch.gui.fragment.eventbus.EventBusFragment;
import eu.power_switch.shared.ThemeHelper;

/**
 * Wizard page base class
 * <p>
 * Created by Markus on 04.11.2016.
 */
public abstract class WizardPage extends EventBusFragment implements ISlideBackgroundColorHolder {

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        View mainView = getMainView();
        if (mainView != null) {
            mainView.setBackgroundColor(backgroundColor);
        }
    }

    /**
     * Get the main view of this page
     * This view will be used to set the background color
     *
     * @return view
     */
    public View getMainView() {
        return rootView;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return ThemeHelper.getThemeAttrColor(getActivity(), R.attr.colorPrimary);
    }

    /**
     * Flashes the background
     *
     * @param flashColor           color to flash
     * @param durationMilliseconds time window from normal to flash to normal color in milliseconds
     */
    protected void flashBackground(@ColorRes int flashColor, int durationMilliseconds) {
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(flashColor), getDefaultBackgroundColor());
        colorAnimator.setDuration(durationMilliseconds);
        colorAnimator.setInterpolator(new DecelerateInterpolator());
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getMainView().setBackgroundColor((int) animation.getAnimatedValue());
            }
        });
        colorAnimator.start();
    }

    /**
     * Shows an error message on the page
     *
     * @param messageRes error message resource
     */
    protected void showErrorMessage(@StringRes int messageRes) {
        Snackbar.make(getMainView(), messageRes, Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Shows an error message on the page
     *
     * @param message error message
     */
    protected void showErrorMessage(String message) {
        Snackbar.make(getMainView(), message, Snackbar.LENGTH_LONG)
                .show();
    }
}

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

package eu.power_switch.gui;


import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import eu.power_switch.R;

/**
 * Created by Markus on 08.06.2016.
 */
public class SettingsListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {

    private static final float NO_ALPHA = 1f, PARTIAL_ALPHA = 0.40f;
    private static final float NO_X_TRANSLATION = 0f, X_TRANSLATION = 20f;
    private final int mUnselectedCircleColor, mSelectedCircleColor;
    private final int mUnselectedCircleBorderColor, mSelectedCircleBorderColor;
    private CircledImageView mCircle;
    private float mBigCircleRadius;
    private float mSmallCircleRadius;
    private boolean isCentered = false;

    public SettingsListItemLayout(Context context) {
        this(context, null);
    }

    public SettingsListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mUnselectedCircleColor = Color.parseColor("#434343");
        mSelectedCircleColor = Color.parseColor("#434343");
        mUnselectedCircleBorderColor = Color.parseColor("#FFFFFFFF");
        mSelectedCircleBorderColor = ThemeHelper.getThemeAttrColor(context, R.attr.colorAccent);
        mSmallCircleRadius = getResources().getDimensionPixelSize(R.dimen.small_circle_radius);
        mBigCircleRadius = getResources().getDimensionPixelSize(R.dimen.big_circle_radius);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCircle = (CircledImageView) findViewById(R.id.circle);
        if (isCentered) {
            onCenterPosition(true);
        }
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (animate && !isCentered) {
            animateCircleSelected();
        }

        mCircle.setCircleColor(mSelectedCircleColor);
        isCentered = true;
    }

    private void animateCircleSelected() {
        animate().alpha(NO_ALPHA)
                //.translationX(X_TRANSLATION)
                .start();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(), mUnselectedCircleBorderColor, mSelectedCircleBorderColor);
        colorAnimation.setDuration(250);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircle.setCircleBorderColor((int) animation.getAnimatedValue());
            }
        });

        ValueAnimator radiusAnimator = ValueAnimator.ofObject(
                new FloatEvaluator(), mSmallCircleRadius, mBigCircleRadius);
        radiusAnimator.setDuration(250);
        radiusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircle.setCircleRadius((float) animation.getAnimatedValue());
            }
        });

        colorAnimation.start();
        radiusAnimator.start();
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (animate && isCentered) {
            animateCircleUnselected();
        }

        mCircle.setCircleColor(mUnselectedCircleColor);
        isCentered = false;
    }

    private void animateCircleUnselected() {
        animate().alpha(PARTIAL_ALPHA)
                //.translationX(NO_X_TRANSLATION)
                .start();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(), mSelectedCircleBorderColor, mUnselectedCircleBorderColor);
        colorAnimation.setDuration(250);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircle.setCircleBorderColor((int) animation.getAnimatedValue());
            }
        });

        ValueAnimator radiusAnimator = ValueAnimator.ofObject(
                new FloatEvaluator(), mBigCircleRadius, mSmallCircleRadius);
        radiusAnimator.setDuration(250);
        radiusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircle.setCircleRadius((float) animation.getAnimatedValue());
            }
        });

        radiusAnimator.start();
        colorAnimation.start();
    }
}
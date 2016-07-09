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

package eu.power_switch.gui.view;


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
import eu.power_switch.gui.ThemeHelper;

/**
 * Settings Item View
 * <p/>
 * Used in Settings List to visualize a single setting
 * <p/>
 * Created by Markus on 08.06.2016.
 */
public class SettingsListItemView extends LinearLayout implements WearableListView.OnCenterProximityListener {

    private static final float NO_ALPHA = 1f, PARTIAL_ALPHA = 0.40f;
    private static final int ANIMATION_DURATION = 250;
    private final int mUnselectedCircleColor, mSelectedCircleColor;
    private final int mUnselectedCircleBorderColor, mSelectedCircleBorderColor;
    private CircledImageView mCircle;
    private float mBigCircleRadius;
    private float mSmallCircleRadius;
    private boolean isCentered = false;
    private boolean initialSetup = true;
    private int currentCircleBorderColor;

    public SettingsListItemView(Context context) {
        this(context, null);
    }

    public SettingsListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsListItemView(Context context, AttributeSet attrs, int defStyle) {
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
        initialSetup = true;

        mCircle = (CircledImageView) findViewById(R.id.circle);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (!isCentered || initialSetup) {
            if (animate && !initialSetup) {
                animateCenterPosition();
            } else {
                setAlpha(NO_ALPHA);
                setCircleBorderColor(mSelectedCircleBorderColor);
                mCircle.setCircleRadius(mBigCircleRadius);
            }

            mCircle.setCircleColor(mSelectedCircleColor);

            isCentered = true;
            initialSetup = false;
        }
    }

    private void setCircleBorderColor(int color) {
        currentCircleBorderColor = color;
        mCircle.setCircleBorderColor(color);
    }

    private void animateCenterPosition() {
        ValueAnimator alphaAnimation = ValueAnimator.ofObject(
                new FloatEvaluator(), getAlpha(), NO_ALPHA);
        alphaAnimation.setDuration(ANIMATION_DURATION);
        alphaAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setAlpha((float) animation.getAnimatedValue());
            }
        });

        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(), currentCircleBorderColor, mSelectedCircleBorderColor);
        colorAnimation.setDuration(ANIMATION_DURATION);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setCircleBorderColor((int) animation.getAnimatedValue());
            }
        });

        ValueAnimator radiusAnimator = ValueAnimator.ofObject(
                new FloatEvaluator(), mCircle.getCircleRadius(), mBigCircleRadius);
        radiusAnimator.setDuration(ANIMATION_DURATION);
        radiusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircle.setCircleRadius((float) animation.getAnimatedValue());
            }
        });

        alphaAnimation.start();
        colorAnimation.start();
        radiusAnimator.start();
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (isCentered || initialSetup) {
            if (animate && !initialSetup) {
                animateNonCenterPosition();
            } else {
                setAlpha(PARTIAL_ALPHA);
                setCircleBorderColor(mUnselectedCircleBorderColor);
                mCircle.setCircleRadius(mSmallCircleRadius);
            }

            mCircle.setCircleColor(mUnselectedCircleColor);

            isCentered = false;
            initialSetup = false;
        }
    }

    private void animateNonCenterPosition() {
        ValueAnimator alphaAnimation = ValueAnimator.ofObject(
                new FloatEvaluator(), getAlpha(), PARTIAL_ALPHA);
        alphaAnimation.setDuration(ANIMATION_DURATION);
        alphaAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setAlpha((float) animation.getAnimatedValue());
            }
        });

        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(), currentCircleBorderColor, mUnselectedCircleBorderColor);
        colorAnimation.setDuration(ANIMATION_DURATION);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setCircleBorderColor((int) animation.getAnimatedValue());
            }
        });

        ValueAnimator radiusAnimator = ValueAnimator.ofObject(
                new FloatEvaluator(), mCircle.getCircleRadius(), mSmallCircleRadius);
        radiusAnimator.setDuration(ANIMATION_DURATION);
        radiusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircle.setCircleRadius((float) animation.getAnimatedValue());
            }
        });

        alphaAnimation.start();
        radiusAnimator.start();
        colorAnimation.start();
    }
}
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

package eu.power_switch.shared.gui.view;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import java.util.ArrayList;

import eu.power_switch.shared.R;

/**
 * Page indicator view
 * <p/>
 * Used as a "current page" indicator for a multi page view (i.e. navigation drawer)
 * <p/>
 * Created by Markus on 06.07.2016.
 */
public class PageIndicatorView extends LinearLayout {

    private static final int ANIMATION_DURATION = 250;

    private int currentPage;
    private int pageCount;
    private int activeIndicatorSize;
    private int inactiveIndicatorSize;
    private int indicatorGap;
    private int activeIndicatorFillColor;
    private int activeIndicatorStrokeColor;
    private int inactiveIndicatorFillColor;
    private int inactiveIndicatorStrokeColor;

    private ArrayList<CircleIndicatorView> indicatorViews = new ArrayList<>();

    public PageIndicatorView(Context context) {
        this(context, null);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    @TargetApi(21)
    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs);
    }

    private void readArguments(Context context, AttributeSet attrs) {
        // read XML attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PageIndicatorView,
                0, 0);

        try {
            currentPage = a.getInt(R.styleable.PageIndicatorView_initialPageIndex, 0);
            pageCount = a.getInt(R.styleable.PageIndicatorView_pageCount, 0);
            activeIndicatorSize = a.getDimensionPixelSize(R.styleable.PageIndicatorView_activeIndicatorSize, 7);
            inactiveIndicatorSize = a.getDimensionPixelSize(R.styleable.PageIndicatorView_inactiveIndicatorSize, 5);
            indicatorGap = a.getDimensionPixelSize(R.styleable.PageIndicatorView_indicatorGap, 5);
            activeIndicatorFillColor = a.getColor(R.styleable.PageIndicatorView_activeIndicatorColorFill, Color.WHITE);
            activeIndicatorStrokeColor = a.getColor(R.styleable.PageIndicatorView_activeIndicatorColorStroke, Color.WHITE);
            inactiveIndicatorFillColor = a.getColor(R.styleable.PageIndicatorView_inactiveIndicatorColorFill, Color.WHITE);
            inactiveIndicatorStrokeColor = a.getColor(R.styleable.PageIndicatorView_inactiveIndicatorColorStroke, Color.WHITE);
        } finally {
            a.recycle();
        }
    }

    private void init(Context context, AttributeSet attrs) {
        readArguments(context, attrs);

        createIndicators();
        setGravity(Gravity.CENTER);
    }

    private void createIndicators() {
        removeAllViews();
        indicatorViews.clear();

        for (int i = 0; i < pageCount; i++) {
            if (i == currentPage) {
                addActiveIndicator();
            } else {
                addInactiveIndicator();
            }
        }
    }

    private void addActiveIndicator() {
        CircleIndicatorView activeCircleIndicator = new CircleIndicatorView(getContext());
        activeCircleIndicator.setDiameter(activeIndicatorSize);
        activeCircleIndicator.setFillColor(activeIndicatorFillColor);
        activeCircleIndicator.setStrokeColor(activeIndicatorStrokeColor);

        addView(activeCircleIndicator, activeIndicatorSize + 1, activeIndicatorSize + 1);

        LayoutParams lp = (LayoutParams) activeCircleIndicator.getLayoutParams();
        lp.leftMargin = indicatorGap / 2;
        lp.rightMargin = indicatorGap / 2;
        activeCircleIndicator.setLayoutParams(lp);

        indicatorViews.add(activeCircleIndicator);
    }

    private void addInactiveIndicator() {
        CircleIndicatorView inactiveCircleIndicator = new CircleIndicatorView(getContext());
        inactiveCircleIndicator.setDiameter(inactiveIndicatorSize);
        inactiveCircleIndicator.setFillColor(inactiveIndicatorFillColor);
        inactiveCircleIndicator.setStrokeColor(inactiveIndicatorStrokeColor);

        addView(inactiveCircleIndicator, activeIndicatorSize + 1, activeIndicatorSize + 1);

        LayoutParams lp = (LayoutParams) inactiveCircleIndicator.getLayoutParams();
        lp.leftMargin = indicatorGap / 2;
        lp.rightMargin = indicatorGap / 2;
        inactiveCircleIndicator.setLayoutParams(lp);

        indicatorViews.add(inactiveCircleIndicator);
    }

    private void animateToActiveIndicator(int index) {
        final CircleIndicatorView indicatorView = indicatorViews.get(index);

        ValueAnimator sizeAnimator = ValueAnimator.ofObject(
                new FloatEvaluator(), indicatorView.getDiameter(), activeIndicatorSize);
        sizeAnimator.setDuration(ANIMATION_DURATION);
        sizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indicatorView.setDiameter((float) animation.getAnimatedValue());
            }
        });

        ValueAnimator fillColorAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(), indicatorView.getFillColor(), activeIndicatorFillColor);
        fillColorAnimator.setDuration(ANIMATION_DURATION);
        fillColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indicatorView.setFillColor((int) animation.getAnimatedValue());
            }
        });

        ValueAnimator strokeColorAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(), indicatorView.getStrokeColor(), activeIndicatorStrokeColor);
        strokeColorAnimator.setDuration(ANIMATION_DURATION);
        strokeColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indicatorView.setStrokeColor((int) animation.getAnimatedValue());
            }
        });

        sizeAnimator.start();
        fillColorAnimator.start();
        strokeColorAnimator.start();
    }

    private void animateToInactiveIndicator(int index) {
        final CircleIndicatorView indicatorView = indicatorViews.get(index);

        ValueAnimator sizeAnimator = ValueAnimator.ofObject(
                new FloatEvaluator(), indicatorView.getDiameter(), inactiveIndicatorSize);
        sizeAnimator.setDuration(ANIMATION_DURATION);
        sizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indicatorView.setDiameter((float) animation.getAnimatedValue());
            }
        });

        ValueAnimator fillColorAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(), indicatorView.getFillColor(), inactiveIndicatorFillColor);
        fillColorAnimator.setDuration(ANIMATION_DURATION);
        fillColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indicatorView.setFillColor((int) animation.getAnimatedValue());
            }
        });

        ValueAnimator strokeColorAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(), indicatorView.getStrokeColor(), inactiveIndicatorStrokeColor);
        strokeColorAnimator.setDuration(ANIMATION_DURATION);
        strokeColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indicatorView.setStrokeColor((int) animation.getAnimatedValue());
            }
        });

        sizeAnimator.start();
        fillColorAnimator.start();
        strokeColorAnimator.start();
    }

    /**
     * Get amount of pages
     *
     * @return amount of pages
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * Set amount of pages
     *
     * @param pageCount amount of pages
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
        invalidate();
        requestLayout();
    }

    /**
     * Get currently active page
     *
     * @return index of currently active page
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Set current page (indicator will be updated accordingly)
     *
     * @param index Index of current page
     */
    public void setCurrentPage(int index) {
        animateToInactiveIndicator(currentPage);
        animateToActiveIndicator(index);
        currentPage = index;

        invalidate();
        requestLayout();
    }

}

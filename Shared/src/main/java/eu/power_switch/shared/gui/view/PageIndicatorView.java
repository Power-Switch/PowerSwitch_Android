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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import eu.power_switch.shared.R;

/**
 * Page indicator view
 * <p/>
 * Used as a "current page" indicator for a multi page view (i.e. navigation drawer)
 * <p/>
 * Created by Markus on 06.07.2016.
 */
public class PageIndicatorView extends View {

    private int currentPage;
    private int pageCount;
    private int indicatorSize;
    private int indicatorGap;
    private int activeIndicatorColor;
    private int inactiveIndicatorColor;
    private Paint inactiveCirclePaint;
    private Paint activeCirclePaint;

    public PageIndicatorView(Context context) {
        this(context, null);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // read XML attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PageIndicatorView,
                0, 0);

        try {
            currentPage = a.getInt(R.styleable.PageIndicatorView_initialPageIndex, 0);
            pageCount = a.getInt(R.styleable.PageIndicatorView_pageCount, 0);
            indicatorSize = a.getDimensionPixelSize(R.styleable.PageIndicatorView_indicatorSize, 5);
            indicatorGap = a.getDimensionPixelSize(R.styleable.PageIndicatorView_indicatorGap, 5);
            activeIndicatorColor = a.getColor(R.styleable.PageIndicatorView_activeIndicatorColor, Color.WHITE);
            inactiveIndicatorColor = a.getColor(R.styleable.PageIndicatorView_inactiveIndicatorColor, Color.WHITE);
        } finally {
            a.recycle();
        }

        init();
    }

    @TargetApi(21)
    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        inactiveCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        inactiveCirclePaint.setStyle(Paint.Style.STROKE);
        inactiveCirclePaint.setColor(inactiveIndicatorColor);

        activeCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activeCirclePaint.setStyle(Paint.Style.FILL);
        activeCirclePaint.setColor(activeIndicatorColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Get the width measurement
        int widthSize = View.resolveSize(getDesiredWidth(), widthMeasureSpec);

        //Get the height measurement
        int heightSize = View.resolveSize(getDesiredHeight(), heightMeasureSpec);

        //MUST call this to store the measurements
        setMeasuredDimension(widthSize, heightSize);
    }

    private int getDesiredWidth() {
        return pageCount * (indicatorSize + indicatorGap) - indicatorGap;
    }

    private int getDesiredHeight() {
        return indicatorSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < pageCount; i++) {
            int x = (indicatorSize / 2) + (i * (indicatorSize + indicatorGap));
            int y = getHeight() / 2;

            if (i == currentPage) {
                canvas.drawCircle(x, y, indicatorSize / 2, activeCirclePaint);
            } else {
                canvas.drawCircle(x, y, indicatorSize / 2, inactiveCirclePaint);
            }
        }
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
        this.pageCount = this.pageCount;
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
        currentPage = index;
        invalidate();
        requestLayout();
    }

}

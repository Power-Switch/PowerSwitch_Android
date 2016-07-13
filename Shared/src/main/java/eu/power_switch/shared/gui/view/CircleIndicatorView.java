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
 * Circle indicator for use with PageIndicatorView
 * <p/>
 * Created by Markus on 13.07.2016.
 */
public class CircleIndicatorView extends View {

    private static final int DEFAULT_DIAMETER = 10;
    private static final int DEFAULT_FILL_COLOR = Color.WHITE;
    private static final int DEFAULT_STROKE_COLOR = Color.WHITE;

    private float diameter;
    private int fillColor;
    private int strokeColor;

    private Paint indicatorFillPaint;
    private Paint indicatorStrokePaint;

    public CircleIndicatorView(Context context) {
        this(context, null);
    }

    public CircleIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        readArguments(context, attrs);
        init();
    }

    @TargetApi(21)
    public CircleIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        readArguments(context, attrs);
        init();
    }

    private void readArguments(Context context, AttributeSet attrs) {
        // read XML attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleIndicatorView,
                0, 0);

        try {
            diameter = a.getDimensionPixelSize(R.styleable.CircleIndicatorView_diameter, DEFAULT_DIAMETER);
            fillColor = a.getColor(R.styleable.CircleIndicatorView_fillColor, DEFAULT_FILL_COLOR);
            strokeColor = a.getColor(R.styleable.CircleIndicatorView_strokeColor, DEFAULT_STROKE_COLOR);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        indicatorFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorFillPaint.setColor(fillColor);
        indicatorFillPaint.setStyle(Paint.Style.FILL);

        indicatorStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorStrokePaint.setColor(strokeColor);
        indicatorStrokePaint.setStyle(Paint.Style.STROKE);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        //Get the width measurement
//        int widthSize = View.resolveSize(getDesiredWidth(), widthMeasureSpec);
//
//        //Get the height measurement
//        int heightSize = View.resolveSize(getDesiredHeight(), heightMeasureSpec);
//
//        //MUST call this to store the measurements
//        setMeasuredDimension(widthSize, heightSize);
//    }
//
//    private int getDesiredWidth() {
//        return (int) Math.ceil((double) diameter) + 1;
//    }
//
//    private int getDesiredHeight() {
//        return (int) Math.ceil((double) diameter) + 1;
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float x = getWidth() / 2;
        float y = getHeight() / 2;
        float radius = diameter / 2;

        canvas.drawCircle(x, y, radius, indicatorFillPaint);
        canvas.drawCircle(x, y, radius, indicatorStrokePaint);
    }

    /**
     * Get current diameter value
     *
     * @return current diameter
     */
    public float getDiameter() {
        return diameter;
    }

    /**
     * Set new Diameter of this Indicator
     *
     * @param newDiameter new diameter value
     */
    public void setDiameter(float newDiameter) {
        diameter = newDiameter;

        invalidate();
        requestLayout();
    }

    /**
     * Get current fill color
     *
     * @return fill color
     */
    public int getFillColor() {
        return fillColor;
    }

    /**
     * Set fill color
     *
     * @param fillColor color
     */
    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
        indicatorFillPaint.setColor(fillColor);

        invalidate();
        requestLayout();
    }

    /**
     * Get current stroke color
     *
     * @return stroke color
     */
    public int getStrokeColor() {
        return strokeColor;
    }

    /**
     * Set stroke color
     *
     * @param strokeColor color
     */
    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        indicatorStrokePaint.setColor(strokeColor);

        invalidate();
        requestLayout();
    }
}

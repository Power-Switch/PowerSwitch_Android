/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.view;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.wear.widget.CircledImageView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.power_switch.R;
import eu.power_switch.shared.ThemeHelper;

/**
 * Settings Item View
 * <p/>
 * Used in Settings List to visualize a single setting
 * <p/>
 * Created by Markus on 08.06.2016.
 */
public class SettingsListItemLayout extends LinearLayout {

//    private static final float NO_ALPHA = 1f;

    protected CircledImageView mCircle;
    protected TextView         mValue;
    protected TextView         mDescription;

    @ColorInt
    private final int   mCircleColor;
    @ColorInt
    private final int   mCircleBorderColor;
    private       float mCircleRadius;
    @ColorInt
    private int textColor = Color.WHITE;

    private int currentTextAlpha = 255;

    public SettingsListItemLayout(Context context) {
        this(context, null);
    }

    public SettingsListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mCircleColor = Color.parseColor("#434343");
        mCircleBorderColor = ThemeHelper.getThemeAttrColor(context, R.attr.colorAccent);
        mCircleRadius = getResources().getDimensionPixelSize(R.dimen.big_circle_radius);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCircle = findViewById(R.id.circle);
        mValue = findViewById(R.id.value);
        mDescription = findViewById(R.id.description);

        mValue.setTextColor(textColor);

        mCircle.setCircleBorderWidth(2);
        mCircle.setCircleBorderColor(mCircleBorderColor);
        mCircle.setCircleRadius(mCircleRadius);
        mCircle.setCircleColor(mCircleColor);
    }

    /**
     * @param scale
     */
    public void setIconScale(float scale) {
        mCircle.setScaleX(scale);
        mCircle.setScaleY(scale);
    }

    /**
     * Set the text color for the current value of this SettingsItem
     *
     * @param textColor the color to set
     */
    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        updateColors();
    }

    public void setTextAlpha(int alpha) {
        currentTextAlpha = alpha;
        updateColors();
    }

    private void updateColors() {
        int color = Color.argb(currentTextAlpha, Color.red(textColor), Color.green(textColor), Color.blue(textColor));

        mDescription.setTextColor(color);
        mValue.setTextColor(color);
    }
}
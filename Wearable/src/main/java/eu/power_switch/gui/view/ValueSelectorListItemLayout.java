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
import android.util.AttributeSet;

import eu.power_switch.gui.IconicsHelper;

/**
 * Item layout for value selector
 * <p/>
 * Created by Markus on 18.07.2016.
 */
public class ValueSelectorListItemLayout extends SettingsListItemLayout {

    public ValueSelectorListItemLayout(Context context) {
        this(context, null);
    }

    public ValueSelectorListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ValueSelectorListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCircle.setImageDrawable(IconicsHelper.getCheckmarkIcon(getContext()));
    }

    public void setIconVisibility(int alpha) {
        mCircle.getImageDrawable()
                .setAlpha(alpha);
        mCircle.invalidate();
        invalidate();
    }
}
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

package eu.power_switch.tutorial;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

import de.markusressel.android.library.tutorialtooltip.view.TutorialTooltipView.Gravity;
import lombok.Getter;

/**
 * Convenience class for easy access of TutorialTooltip configuration
 * <p>
 * Created by Markus on 25.07.2017.
 */
@Getter
public class TutorialItem {

    View anchor;

    Gravity anchorGravity;

    int anchorOffsetX;
    int anchorOffsetY;

    @ColorInt
    @Nullable
    Integer indicatorColor;

    @StringRes
    int messageRes;

    Gravity messageGravity;

    int messageOffsetX;
    int messageOffsetY;

    @StringRes
    Integer oneTimeKey;

    public TutorialItem(View anchor, Gravity anchorGravity, int anchorOffsetX, int anchorOffsetY, @Nullable @ColorInt Integer indicatorColor,
                        @StringRes int messageRes, Gravity messageGravity, int messageOffsetX, int messageOffsetY, @StringRes int oneTimeKey) {
        this.anchor = anchor;
        this.anchorGravity = anchorGravity;
        this.anchorOffsetX = anchorOffsetX;
        this.anchorOffsetY = anchorOffsetY;
        this.indicatorColor = indicatorColor;
        this.messageRes = messageRes;
        this.messageGravity = messageGravity;
        this.messageOffsetX = messageOffsetX;
        this.messageOffsetY = messageOffsetY;
        this.oneTimeKey = oneTimeKey;
    }

    public TutorialItem(View anchor, Gravity anchorGravity, int anchorOffsetX, int anchorOffsetY, @StringRes int messageRes, Gravity messageGravity,
                        @StringRes int oneTimeKey) {
        this(anchor, anchorGravity, anchorOffsetX, anchorOffsetY, null, messageRes, messageGravity, 0, 0, oneTimeKey);
    }

    public TutorialItem(View anchor, Gravity anchorGravity, @ColorInt int indicatorColor, @StringRes int messageRes, Gravity messageGravity,
                        @StringRes int oneTimeKey) {
        this(anchor, anchorGravity, 0, 0, indicatorColor, messageRes, messageGravity, 0, 0, oneTimeKey);
    }

    public TutorialItem(View anchor, Gravity anchorGravity, @StringRes int messageRes, Gravity messageGravity, @StringRes int oneTimeKey) {
        this(anchor, anchorGravity, 0, 0, messageRes, messageGravity, oneTimeKey);
    }

    public TutorialItem(View anchor, @StringRes int messageRes, Gravity messageGravity, @StringRes int oneTimeKey) {
        this(anchor, Gravity.CENTER, messageRes, messageGravity, oneTimeKey);
    }

    public TutorialItem(View anchor, @StringRes int messageRes, @StringRes int oneTimeKey) {
        this(anchor, messageRes, Gravity.BOTTOM, oneTimeKey);
    }

}

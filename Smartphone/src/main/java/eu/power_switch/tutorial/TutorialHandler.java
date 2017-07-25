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

package eu.power_switch.tutorial;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.view.View;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.markusressel.android.library.tutorialtooltip.builder.IndicatorBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.MessageBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.TutorialTooltipBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.TutorialTooltipChainBuilder;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnIndicatorClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnMessageClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipIndicator;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipMessage;
import de.markusressel.android.library.tutorialtooltip.view.TooltipId;
import de.markusressel.android.library.tutorialtooltip.view.TutorialTooltipView;
import eu.power_switch.R;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.TutorialConstants;
import it.sephiroth.android.library.tooltip.Tooltip;

/**
 * Helper class for Tutorial specific tasks
 * <p/>
 * Created by Markus on 12.12.2015.
 */
@Singleton
public class TutorialHandler {

    private Context context;

    @Inject
    public TutorialHandler(Context context) {
        this.context = context;
    }

    public static String getMainTabKey(String pageTitle) {
        return TutorialConstants.MAIN_TABS_KEY_PREFIX + pageTitle;
    }

    public static String getSettingsTabKey(String pageTitle) {
        return TutorialConstants.SETTINGS_TABS_KEY_PREFIX + pageTitle;
    }

    public static String getAlarmClockTabKey(String pageTitle) {
        return TutorialConstants.ALARM_CLOCK_TABS_KEY_PREFIX + pageTitle;
    }

    public static String getPhoneTabKey(String pageTitle) {
        return TutorialConstants.PHONE_TABS_KEY_PREFIX + pageTitle;
    }

    public void getTutorialToast(View anchor) {
        Tooltip.make(context,
                new Tooltip.Builder(101).anchor(anchor, Tooltip.Gravity.BOTTOM)
                        .closePolicy(new Tooltip.ClosePolicy().insidePolicy(true, false)
                                .outsidePolicy(true, false), 3000)
                        .activateDelay(800)
                        .showDelay(300)
                        .text(context.getResources(), R.string.tutorial__got_it)
                        .maxWidth(500)
                        .withArrow(true)
                        .withOverlay(true)
//                        .typeface(mYourCustomFont)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build())
                .show();
    }

    /**
     * Show a floating TutorialTooltip
     *
     * @param dialog        the dialog to attach this TutorialTooltip to
     * @param tutorialItems a list of item descriptions for the tooltips
     */
    public void showDefaultTutorialTooltipAsChain(Dialog dialog, TutorialItem... tutorialItems) {
        OnMessageClickedListener onClickListener = new OnMessageClickedListener() {
            @Override
            public void onMessageClicked(TooltipId id, TutorialTooltipView tutorialTooltipView, TutorialTooltipMessage tutorialTooltipMessage,
                                         View view) {
                tutorialTooltipView.remove(true);
            }
        };

        OnIndicatorClickedListener onIndicatorClickedListener = new OnIndicatorClickedListener() {
            @Override
            public void onIndicatorClicked(TooltipId tooltipId, TutorialTooltipView tutorialTooltipView,
                                           TutorialTooltipIndicator tutorialTooltipIndicator, View view) {
                tutorialTooltipView.remove(true);
            }
        };

        TutorialTooltipChainBuilder chainBuilder = new TutorialTooltipChainBuilder();

        for (TutorialItem tutorialItem : tutorialItems) {
            // build indicator
            IndicatorBuilder indicatorBuilder = new IndicatorBuilder().offset(tutorialItem.getAnchorOffsetX(), tutorialItem.getAnchorOffsetY())
                    .onClick(onIndicatorClickedListener);
            if (tutorialItem.getIndicatorColor() != null) {
                indicatorBuilder.color(tutorialItem.getIndicatorColor());
            }
            indicatorBuilder.build();

            @ColorInt int backgroundColor = ThemeHelper.getThemeAttrColor(context, android.R.attr.windowBackground);
//            @ColorInt int backgroundColor = ThemeHelper.getThemeAttrColor(context, R.attr.colorPrimary);
            @ColorInt int textColor = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);

            // build message
            MessageBuilder messageBuilder = new MessageBuilder(dialog.getContext()).text(tutorialItem.getMessageRes())
                    .gravity(tutorialItem.getMessageGravity())
                    .offset(tutorialItem.getMessageOffsetX(), tutorialItem.getMessageOffsetY())
                    .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                    .backgroundColor(backgroundColor)
                    .textColor(textColor)
                    .onClick(onClickListener)
                    .build();

            // build tooltip
            TutorialTooltipBuilder tooltipBuilder = new TutorialTooltipBuilder(dialog.getContext()).attachToDialog(dialog)
                    .anchor(tutorialItem.getAnchor(), tutorialItem.getAnchorGravity())
                    .indicator(indicatorBuilder)
                    .message(messageBuilder)
                    .oneTimeUse(tutorialItem.getOneTimeKey())
                    .build();

            // add to chain
            chainBuilder.addItem(tooltipBuilder);
        }

        // execute chain
        chainBuilder.execute();
    }
}

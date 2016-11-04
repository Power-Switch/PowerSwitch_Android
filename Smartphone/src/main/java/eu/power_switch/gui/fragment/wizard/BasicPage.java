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

package eu.power_switch.gui.fragment.wizard;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import eu.power_switch.R;

/**
 * Basic wizard page consisting of an icon, a title text and a description text
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class BasicPage extends WizardPage {

    private static final String KEY_BACKGROUND_COLOR = "backgroundColor";
    private static final String KEY_ICON = "icon";
    private static final String KEY_TITLE = "titleText";
    private static final String KEY_DESCRIPTION = "descriptionText";

    private View rootView;

    private int backgroundColor;

    private TextView title;
    private TextView description;
    private IconicsImageView icon;

    public static BasicPage newInstance(@ColorInt int color, @DrawableRes int icon, @StringRes int title, @StringRes int description) {
        Bundle args = new Bundle();
        args.putInt(KEY_BACKGROUND_COLOR, color);
        args.putInt(KEY_ICON, icon);
        args.putInt(KEY_TITLE, title);
        args.putInt(KEY_DESCRIPTION, description);
        BasicPage fragment = new BasicPage();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.wizard_page_basic, container, false);

        Bundle arguments = getArguments();
        backgroundColor = arguments.getInt(KEY_BACKGROUND_COLOR);
        setBackgroundColor(backgroundColor);

        @DrawableRes int iconRes = arguments.getInt(KEY_ICON);
        icon = (IconicsImageView) rootView.findViewById(R.id.icon);
        icon.setImageDrawable(getResources().getDrawable(iconRes));

        @StringRes int titleText = arguments.getInt(KEY_TITLE);
        @StringRes int descriptionText = arguments.getInt(KEY_DESCRIPTION);

        title = (TextView) rootView.findViewById(R.id.title);
        setTitle(titleText);

        description = (TextView) rootView.findViewById(R.id.description);
        description.setText(descriptionText);

        return rootView;
    }

    /**
     * Set title text
     *
     * @param title title string resource
     */
    public void setTitle(@StringRes int title) {
        setTitle(getString(title));
    }

    /**
     * Set title text
     *
     * @param title title
     */
    public void setTitle(String title) {
        this.title.setText(title);
    }

    /**
     * Set description text
     *
     * @param description description string resource
     */
    public void setDescription(@StringRes int description) {
        setDescription(getString(description));
    }

    /**
     * Set description text
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description.setText(description);
    }

    @Override
    public int getDefaultBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public View getMainView() {
        return rootView;
    }

}

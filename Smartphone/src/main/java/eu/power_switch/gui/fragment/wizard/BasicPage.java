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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import eu.power_switch.R;
import eu.power_switch.shared.ThemeHelper;

/**
 * Basic wizard page consisting of an icon, a title text and a description text
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class BasicPage extends WizardPage {

    protected static final String KEY_BACKGROUND_COLOR = "defaultBackgroundColor";
    protected static final String KEY_ICON = "icon";
    protected static final String KEY_TITLE = "titleText";
    protected static final String KEY_DESCRIPTION = "descriptionText";

    private int defaultBackgroundColor;

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
        super.onCreateView(inflater, container, savedInstanceState);

        icon = getMainView().findViewById(R.id.icon);
        title = getMainView().findViewById(R.id.title);
        description = getMainView().findViewById(R.id.description);

        onSetUiValues();

        return getMainView();
    }

    /**
     * Set values for all UI elements
     */
    @CallSuper
    protected void onSetUiValues() {
        Bundle arguments = getArguments();
        if (arguments.containsKey(KEY_BACKGROUND_COLOR) && arguments.containsKey(KEY_ICON) && arguments.containsKey(KEY_TITLE) && arguments.containsKey(
                KEY_DESCRIPTION)) {
            defaultBackgroundColor = arguments.getInt(KEY_BACKGROUND_COLOR, -1);
            @DrawableRes int iconRes = arguments.getInt(KEY_ICON, -1);
            @StringRes int titleText = arguments.getInt(KEY_TITLE, -1);
            @StringRes int descriptionText = arguments.getInt(KEY_DESCRIPTION, -1);

            setBackgroundColor(defaultBackgroundColor);
            setIcon(iconRes);
            setTitle(titleText);
            setDescription(descriptionText);
        } else {
            defaultBackgroundColor = ThemeHelper.getThemeAttrColor(getActivity(), R.attr.colorPrimary);
            setIcon(R.drawable.ic_launcher_material);
            setTitle("Title");
            setDescription("Description text...");
        }
    }

    /**
     * Set icon drawable
     *
     * @param drawableRes drawable
     */
    protected void setIcon(@DrawableRes int drawableRes) {
        setIcon(ContextCompat.getDrawable(getContext(), drawableRes));
    }

    /**
     * Set icon drawable
     *
     * @param drawable drawable
     */
    protected void setIcon(Drawable drawable) {
        icon.setImageDrawable(drawable);
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

    @LayoutRes
    @Override
    protected int getLayout() {
        return R.layout.wizard_page_basic;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

}

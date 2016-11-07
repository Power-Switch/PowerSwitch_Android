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
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.paolorotolo.appintro.ISlidePolicy;

import eu.power_switch.R;
import eu.power_switch.shared.ThemeHelper;

/**
 * Basic wizard page consisting of an icon, a title text and a description text
 * <p>
 * Created by Markus on 04.11.2016.
 */
public class SingleLineTextInputPage extends WizardPage implements ISlidePolicy {

    protected static final String KEY_BACKGROUND_COLOR = "defaultBackgroundColor";
    protected static final String KEY_TITLE = "titleText";
    protected static final String KEY_HINT = "titleText";
    protected static final String KEY_DESCRIPTION = "descriptionText";

    private int defaultBackgroundColor;

    private TextView title;
    private TextView description;
    private TextInputEditText input;

    public static SingleLineTextInputPage newInstance(@ColorInt int color, @StringRes int title, @StringRes int hint,
                                                      @StringRes int description) {
        Bundle args = new Bundle();
        args.putInt(KEY_BACKGROUND_COLOR, color);
        args.putInt(KEY_TITLE, title);
        args.putInt(KEY_HINT, hint);
        args.putInt(KEY_DESCRIPTION, description);
        SingleLineTextInputPage fragment = new SingleLineTextInputPage();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        title = (TextView) getMainView().findViewById(R.id.title);
        input = (TextInputEditText) getMainView().findViewById(R.id.input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onInputChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        description = (TextView) getMainView().findViewById(R.id.description);

        onSetUiValues();

        return getMainView();
    }

    /**
     * Set values for all UI elements
     */
    @CallSuper
    protected void onSetUiValues() {
        Bundle arguments = getArguments();
        if (arguments.containsKey(KEY_BACKGROUND_COLOR) && arguments.containsKey(KEY_TITLE) && arguments.containsKey(KEY_HINT) && arguments.containsKey(
                KEY_DESCRIPTION)) {
            defaultBackgroundColor = arguments.getInt(KEY_BACKGROUND_COLOR, -1);
            @StringRes int titleText = arguments.getInt(KEY_TITLE, -1);
            @StringRes int hintText = arguments.getInt(KEY_HINT, -1);
            @StringRes int descriptionText = arguments.getInt(KEY_DESCRIPTION, -1);

            setBackgroundColor(defaultBackgroundColor);
            setHint(hintText);
            setTitle(titleText);
            setDescription(descriptionText);
        } else {
            defaultBackgroundColor = ThemeHelper.getThemeAttrColor(getActivity(), R.attr.colorPrimary);
            setTitle("Title");
            setDescription("Description text...");
            setHint("example");
        }
    }

    /**
     * Set hint text
     *
     * @param hint hint string resource
     */
    public void setHint(@StringRes int hint) {
        setHint(getString(hint));
    }

    /**
     * Set hint text
     *
     * @param hint hint
     */
    public void setHint(String hint) {
        this.input.setHint(hint);
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

    /**
     * Get the text of the input field
     *
     * @return
     */
    public String getInput() {
        return input.getText().toString();
    }

    /**
     * This method is called when the input changes
     * Override it if you want to change this behaviour
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    public void onInputChanged(CharSequence s, int start, int before, int count) {
        // override this
    }

    @LayoutRes
    @Override
    protected int getLayout() {
        return R.layout.wizard_page_single_line_text_input;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    @Override
    public boolean isPolicyRespected() {
        return true;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        flashBackground(R.color.color_red_a700, 1000);
        showErrorMessage(getString(R.string.unknown_error));
    }

}

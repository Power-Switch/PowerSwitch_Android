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

package eu.power_switch.wizard.gui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import butterknife.BindView;
import eu.power_switch.R;

/**
 * "Finished" page, indicating the user has reached the end of the wizard
 * <p>
 * Created by Markus on 05.11.2016.
 */
public class FinishPage extends WizardPage {

    @BindView(R.id.imageView_success)
    IconicsImageView successImage;
    @BindView(R.id.imageView_error)
    IconicsImageView errorImage;
    @BindView(R.id.resultText)
    TextView         resultText;

    public static FinishPage newInstance() {
        Bundle     args     = new Bundle();
        FinishPage fragment = new FinishPage();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        onSuccess(android.R.string.ok);

        return getMainView();
    }

    /**
     * Shows a success message and icon
     *
     * @param successMessage success message resource
     */
    public void onSuccess(@StringRes int successMessage) {
        successImage.setVisibility(View.VISIBLE);
        errorImage.setVisibility(View.GONE);
        resultText.setText(successMessage);
    }

    /**
     * Shows a failure message and icon
     *
     * @param failureMessage failure message resource
     */
    public void onFailure(@StringRes int failureMessage) {
        successImage.setVisibility(View.GONE);
        errorImage.setVisibility(View.VISIBLE);
        resultText.setText(failureMessage);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.wizard_page_finish;
    }
}

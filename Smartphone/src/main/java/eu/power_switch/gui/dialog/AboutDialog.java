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

package eu.power_switch.gui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;

import eu.power_switch.R;
import eu.power_switch.shared.log.Log;

/**
 * Fragment displaying the "About" screen containing all info about the app and version history
 */
public class AboutDialog extends DialogFragment {

    private View rootView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("Opening About Dialog...");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.dialog_about, null);

        AppCompatTextView aboutTextView = (AppCompatTextView) rootView.findViewById(R.id.textView_about);
        aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
        String htmlSource = getString(R.string.version) + ": " + getString(R.string.app_version)
                + "\n\n" + getString(R.string.app_about) + "\n\n" + getString(R.string.app_changelog);
        aboutTextView.setText(Html.fromHtml(htmlSource));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setTitle(R.string.menu_about);
        builder.setNeutralButton(android.R.string.ok, null);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.show();

        return dialog;
    }
}

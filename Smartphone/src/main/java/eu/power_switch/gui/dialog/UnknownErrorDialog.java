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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import eu.power_switch.R;

/**
 * Shows a Dialog with details about an unknown Exception/Error that occurred during runtime
 * <p/>
 * Created by Markus on 05.02.2016.
 */
public class UnknownErrorDialog extends DialogFragment {

    private static final String THROWABLE_KEY = "throwable";
    private static final String TIME_KEY = "time";

    private View rootView;
    private Dialog dialog;
    private Throwable throwable;
    private Date timeRaised;

    /**
     * Create a new instance of this Dialog while providing an argument.
     *
     * @param t                        any throwable
     * @param timeRaisedInMilliseconds time when the throwable was raised
     */
    public static UnknownErrorDialog newInstance(Throwable t, long timeRaisedInMilliseconds) {
        Bundle args = new Bundle();
        args.putSerializable(THROWABLE_KEY, t);
        args.putLong(TIME_KEY, timeRaisedInMilliseconds);

        UnknownErrorDialog fragment = new UnknownErrorDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.dialog_unknown_error, null);
        builder.setView(rootView);


        Bundle args = getArguments();
        if (args.containsKey(THROWABLE_KEY)) {
            throwable = (Throwable) args.getSerializable(THROWABLE_KEY);
        }
        if (args.containsKey(TIME_KEY)) {
            timeRaised = new Date(args.getLong(TIME_KEY));
        }

        Button buttonShare = (Button) rootView.findViewById(R.id.button_share);
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getShareText());
                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@power-switch.eu"});
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Unknown Error: " + throwable.getClass().getSimpleName());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.send_to)));
            }
        });

        TextView textViewErrorDescription = (TextView) rootView.findViewById(R.id.editText_error_description);
        textViewErrorDescription.setText(getErrorDescription());

        builder.setTitle(R.string.unknown_error);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        return dialog;
    }

    private String getShareText() {
        String shareText = "";
        shareText += "An Exception was raised during the execution of PowerSwitch.\n";
        shareText += "\n\n";
        shareText += "Exception was raised at: " + SimpleDateFormat.getDateTimeInstance().format(timeRaised) + "\n";
        shareText += "\n";
        shareText += "Stacktrace:\n";
        shareText += "\n";
        shareText += getErrorDescription();

        return shareText;
    }

    public String getErrorDescription() {
        return android.util.Log.getStackTraceString(throwable);
    }
}

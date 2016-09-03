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
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.mikepenz.iconics.view.IconicsImageView;

import eu.power_switch.R;

/**
 * Dialog for any kind of processing task which takes some time and can show progress while executing
 * <p/>
 * Created by Markus on 03.09.2016.
 */
public abstract class ProcessingDialog extends DialogFragment {

    private View rootView;
    private ProgressBar progressIndicator;
    private NumberProgressBar progressBarMain;
    private TextView textViewMainStatusMessage;
    private NumberProgressBar progressBarSub;
    private TextView textViewSubStatusMessage;
    private IconicsImageView imageViewSuccess;
    private IconicsImageView imageViewError;

    private Button buttonStart;
    private Button buttonClose;
    private Button buttonCancel;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.dialog_processing, null);

        progressIndicator = (ProgressBar) rootView.findViewById(R.id.progressIndicator);

        progressBarMain = (NumberProgressBar) rootView.findViewById(R.id.progressBar_main);
        textViewMainStatusMessage = (TextView) rootView.findViewById(R.id.textView_statusMessage_main);

        progressBarSub = (NumberProgressBar) rootView.findViewById(R.id.progressBar_sub);
        textViewSubStatusMessage = (TextView) rootView.findViewById(R.id.textView_statusMessage_sub);

        imageViewSuccess = (IconicsImageView) rootView.findViewById(R.id.imageView_success);
        imageViewError = (IconicsImageView) rootView.findViewById(R.id.imageView_error);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setTitle(getDialogTitle());
        builder.setPositiveButton(R.string.start, null);
        builder.setNeutralButton(R.string.close, null);
        builder.setNegativeButton(android.R.string.cancel, null);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.show();

        // fetch these after dialog.show()!
        buttonStart = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    onStartProcessing();
                } catch (Exception e) {
                    onFinishedFailure(e);
                }
            }
        });

        buttonClose = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        buttonCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelProcessing();
            }
        });

        buttonStart.setEnabled(true);
        buttonClose.setEnabled(true);
        buttonCancel.setEnabled(false);

        return dialog;
    }

    /**
     * Set status message for the overall progress
     *
     * @param message status message
     */
    @MainThread
    protected void setMainStatusMessage(String message) {
        textViewMainStatusMessage.setText(message);
    }

    /**
     * Set progress value for the overall progress
     *
     * @param progress progress value
     */
    @MainThread
    protected void setMainProgress(int progress) {
        progressBarMain.setProgress(progress);
    }

    /**
     * Set status message for the current sub process
     *
     * @param message status message
     */
    @MainThread
    protected void setSubStatusMessage(String message) {
        textViewSubStatusMessage.setText(message);
    }

    /**
     * Set progress value for the current sub process
     *
     * @param progress progress value
     */
    @MainThread
    protected void setSubProgress(int progress) {
        progressBarSub.setProgress(progress);
    }

    /**
     * Call this method when your task is done and finished without an error
     */
    @MainThread
    protected void onFinishedSuccess() {
        setMainProgress(100);
        setSubProgress(100);

        setMainStatusMessage("Done!");
        setSubStatusMessage("");

        progressIndicator.setVisibility(View.GONE);
        imageViewError.setVisibility(View.GONE);
        imageViewSuccess.setVisibility(View.VISIBLE);

        buttonStart.setEnabled(true);
        buttonClose.setEnabled(true);
        buttonCancel.setEnabled(false);
    }

    /**
     * Call this method when your task couldn't finish without error
     *
     * @param error Error that occurred while executing
     */
    @MainThread
    protected void onFinishedFailure(Exception error) {
        setMainStatusMessage("Error :(");
        setSubStatusMessage(error.getMessage());

        progressIndicator.setVisibility(View.GONE);
        imageViewError.setVisibility(View.VISIBLE);
        imageViewSuccess.setVisibility(View.GONE);

        buttonStart.setEnabled(true);
        buttonClose.setEnabled(true);
        buttonCancel.setEnabled(false);
    }

    /**
     * Provide a String resource for the title of the dialog
     *
     * @return dialog title resource
     */
    @StringRes
    protected abstract int getDialogTitle();

    /**
     * This is where you perform your processing.
     * <p/>
     * Be sure to do your work in an AsyncTask or a similar construct
     * so you can update status messages and progress values while processing your data.
     * <p/>
     * Also:
     * Be sure to call super.onStartProcessing() so the dialog gets initialized correctly.
     * This call should normally be the <b>first</b> line of your code.
     */
    @MainThread
    @CallSuper
    protected void onStartProcessing() throws Exception {
        progressIndicator.setVisibility(View.VISIBLE);
        imageViewError.setVisibility(View.GONE);
        imageViewSuccess.setVisibility(View.GONE);

        buttonStart.setEnabled(false);
        buttonCancel.setEnabled(true);
        buttonClose.setEnabled(false);
    }

    /**
     * This method is called when the user cancels the progress
     * <p/>
     * Be sure to call super.onCancelProcessing() so the dialog gets initialized correctly.
     * This call should normally be the first <b>last</b> of your code.
     */
    @MainThread
    @CallSuper
    protected void onCancelProcessing() {
        setMainProgress(0);
        setMainStatusMessage("Canceled");
        setSubProgress(0);
        setSubStatusMessage("");

        progressIndicator.setVisibility(View.GONE);
        imageViewError.setVisibility(View.VISIBLE);
        imageViewSuccess.setVisibility(View.GONE);

        buttonStart.setEnabled(true);
        buttonClose.setEnabled(true);
        buttonCancel.setEnabled(false);
    }
}

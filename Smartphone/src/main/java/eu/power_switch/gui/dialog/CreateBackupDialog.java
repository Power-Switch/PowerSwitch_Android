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
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.exception.backup.BackupAlreadyExistsException;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.BackupFragment;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to create a new Backup
 */
public class CreateBackupDialog extends DialogFragment {

    private Dialog dialog;
    private EditText name;
    private int defaultTextColor;
    private View rootView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.dialog_create_backup, null);
        builder.setView(rootView);

        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    setPositiveButtonVisibility(true);
                } else {
                    setPositiveButtonVisibility(false);
                }
            }
        };
        name = (EditText) rootView.findViewById(R.id.txt_backup_name);
        name.requestFocus();
        name.addTextChangedListener(textWatcher);

        builder.setTitle(R.string.create_backup);
        builder.setPositiveButton(R.string.create, new OnClickListener() {

            private BackupHandler backupHandler;

            @Override
            public void onClick(DialogInterface dialog, int which) {
                backupHandler = new BackupHandler(getActivity());
                try {
                    backupHandler.createBackup(false, name.getText().toString().trim(), false);
                    BackupFragment.sendBackupsChangedBroadcast(getActivity());

                    StatusMessageHandler.showInfoMessage(((RecyclerViewFragment) getTargetFragment()).getRecyclerView()
                            , R.string.backup_successful, Snackbar.LENGTH_LONG);
                } catch (BackupAlreadyExistsException e) {
                    Log.e(e);
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.backup_already_exists)
                            .setMessage(R.string.do_you_want_to_overwrite)
                            .setPositiveButton(android.R.string.yes, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        backupHandler.createBackup(false, name.getText().toString().trim(), true);
                                        BackupFragment.sendBackupsChangedBroadcast(getActivity());

                                        StatusMessageHandler.showInfoMessage(((RecyclerViewFragment) getTargetFragment())
                                                        .getRecyclerView(),
                                                R.string.backup_successful, Snackbar.LENGTH_LONG);
                                    } catch (Exception e1) {
                                        Log.e(e1);
                                    }
                                }
                            }).setNegativeButton(android.R.string.no, null).create().show();
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(((RecyclerViewFragment) getTargetFragment()).getRecyclerView()
                            , e);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        dialog.show();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        defaultTextColor = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).getTextColors()
                .getDefaultColor();
        setPositiveButtonVisibility(false);

        return dialog;
    }

    private void setPositiveButtonVisibility(boolean visibility) {
        if (dialog != null) {
            if (visibility) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(defaultTextColor);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
            } else {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
            }
        }
    }

    private String getDateTime() {
        String dateTime = "[";
        Calendar c = Calendar.getInstance();
        dateTime += c.get(Calendar.DAY_OF_MONTH) + ".";
        dateTime += (c.get(Calendar.MONTH) + 1) + ".";
        dateTime += c.get(Calendar.YEAR) + " - ";
        dateTime += c.get(Calendar.HOUR_OF_DAY) + "h";
        dateTime += c.get(Calendar.MINUTE) + "m";
        dateTime += c.get(Calendar.SECOND) + "s" + "]";
        return dateTime;
    }
}
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

import android.app.Activity;
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

import eu.power_switch.R;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.exception.backup.BackupAlreadyExistsException;
import eu.power_switch.exception.backup.BackupNotFoundException;
import eu.power_switch.gui.fragment.BackupFragment;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to rename a Backup
 */
public class EditBackupDialog extends DialogFragment {

    private Dialog dialog;
    private int defaultTextColor;
    private View rootView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle roomData = getArguments();
        final String backupName = roomData.getString("name");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.dialog_edit_backup, null);
        builder.setView(rootView);

        // restore name
        final EditText name = (EditText) rootView.findViewById(R.id.editText_backup_name);
        name.setText(backupName);
        name.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        builder.setTitle(R.string.edit_backup);
        builder.setPositiveButton(R.string.save, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BackupHandler backupHandler = new BackupHandler(getActivity());
                try {
                    backupHandler.renameBackup(backupName, name.getText().toString());
                    BackupFragment.sendBackupsChangedBroadcast(getActivity());
                    Snackbar.make(getTargetFragment().getView(), R.string.backup_saved, Snackbar.LENGTH_LONG)
                            .show();
                } catch (BackupAlreadyExistsException e) {
                    Log.e(e);
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.backup_already_exists)
                            .setMessage(R.string.do_you_want_to_overwrite)
                            .setPositiveButton(android.R.string.yes, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNegativeButton(android.R.string.no, null);
                    e.printStackTrace();
                } catch (BackupNotFoundException e) {
                    Log.e(e);
                    e.printStackTrace();
                    Snackbar.make(getTargetFragment().getView(), R.string.backup_not_found, Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });

        builder.setNeutralButton(android.R.string.cancel, null);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        defaultTextColor = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).getTextColors()
                .getDefaultColor();

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
}
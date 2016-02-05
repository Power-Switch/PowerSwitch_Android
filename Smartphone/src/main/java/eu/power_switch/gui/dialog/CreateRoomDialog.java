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
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.LinkedList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage1NameFragment;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.obj.Room;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * Dialog to create a new Room
 */
public class CreateRoomDialog extends DialogFragment {

    private Dialog dialog;
    private int defaultTextColor;
    private View view;
    private EditText name;
    private TextInputLayout floatingName;
    private LinkedList<String> roomNames;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        try {
            List<Room> rooms = DatabaseHandler.getRooms(SmartphonePreferencesHandler.getCurrentApartmentId());
            roomNames = new LinkedList<>();
            for (Room room : rooms) {
                roomNames.add(room.getName());
            }
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_add_room, null);
        builder.setView(view);

        floatingName = (TextInputLayout) view.findViewById(R.id.room_name_text_input_layout);

        name = (EditText) view.findViewById(R.id.editText_room_name);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidity();
            }
        });

        builder.setTitle(R.string.add_room);
        builder.setPositiveButton(R.string.create, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    DatabaseHandler.addRoom(new Room(null, SmartphonePreferencesHandler.getCurrentApartmentId(), getRoomName()));

                    ConfigureReceiverDialogPage1NameFragment.sendRoomAddedBroadcast(getActivity(), getRoomName());
                    RoomsFragment.sendReceiverChangedBroadcast(getActivity());
                    Snackbar.make(getTargetFragment().getView(), R.string.room_saved, Snackbar.LENGTH_LONG)
                            .show();
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                }
            }
        });

        builder.setNeutralButton(android.R.string.cancel, null);

        setPositiveButtonVisibility(false);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.show();

        defaultTextColor = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).getTextColors()
                .getDefaultColor();

        checkValidity();

        return dialog;
    }

    private String getRoomName() {
        return name.getText().toString().trim();
    }

    private void checkValidity() {
        if (getRoomName().length() <= 0) {
            setPositiveButtonVisibility(false);
            floatingName.setError(null);
            floatingName.setErrorEnabled(false);
        } else if (roomNames.contains(getRoomName())) {
            setPositiveButtonVisibility(false);
            floatingName.setError(getString(R.string.room_already_exists));
            floatingName.setErrorEnabled(true);
        } else {
            setPositiveButtonVisibility(true);
            floatingName.setError(null);
            floatingName.setErrorEnabled(false);
        }
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
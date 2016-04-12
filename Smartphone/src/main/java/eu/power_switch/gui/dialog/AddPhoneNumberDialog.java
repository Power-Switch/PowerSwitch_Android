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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ContactRecyclerViewAdapter;
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.phone.Contact;
import eu.power_switch.phone.ContactHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Created by Markus on 08.04.2016.
 */
public class AddPhoneNumberDialog extends DialogFragment {

    public static final String KEY_PHONE_NUMBER = "phoneNumber";

    public static final Comparator<Contact> ALPHABETIC = new Comparator<Contact>() {
        @Override
        public int compare(Contact lhs, Contact rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };

    private Dialog dialog;
    private int defaultTextColor;
    private View contentView;
    private LinearLayout layoutLoading;
    private TextInputEditText editText_phoneNumber;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private ContactRecyclerViewAdapter contactRecyclerViewAdapter;
    private RecyclerView recyclerViewContacts;
    private Set<String> checkedNumbers = new HashSet<>();

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context any suitable context
     */
    public static void sendPhoneNumbersAddedBroadcast(Context context, Set<String> phoneNumbers) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_CALL_PHONE_NUMBER_ADDED);
        intent.putStringArrayListExtra(KEY_PHONE_NUMBER, new ArrayList<>(phoneNumbers));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        contentView = inflater.inflate(R.layout.dialog_add_phone_number, null);
        builder.setView(contentView);

        editText_phoneNumber = (TextInputEditText) contentView.findViewById(R.id.editText_phoneNumber);
        editText_phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidity();
            }
        });

        layoutLoading = (LinearLayout) contentView.findViewById(R.id.layoutLoading);

        recyclerViewContacts = (RecyclerView) contentView.findViewById(R.id.recyclerView_contacts);
        contactRecyclerViewAdapter = new ContactRecyclerViewAdapter(getActivity(), contacts);
        contactRecyclerViewAdapter.setCheckBoxInteractionListener(new CheckBoxInteractionListener() {
            @Override
            public void onCheckedChangedByUser(CompoundButton buttonView, boolean isChecked) {
                String number = (String) buttonView.getTag();

                if (isChecked) {
                    checkedNumbers.add(number);
                } else {
                    if (checkedNumbers.contains(number)) {
                        checkedNumbers.remove(number);
                    }
                }

                checkValidity();
            }
        });
        recyclerViewContacts.setAdapter(contactRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewContacts.setLayoutManager(layoutManager);

        builder.setTitle(R.string.add_phone_nummbers);
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    sendPhoneNumbersAddedBroadcast(getContext(), getSelectedPhoneNumbers());
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getTargetFragment().getView().findViewById(R.id.listView), e);
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

        refreshContacts();

        return dialog;
    }

    private void refreshContacts() {
        contacts.clear();
        contacts.addAll(ContactHelper.getContacts(getActivity()));

        Collections.sort(contacts, ALPHABETIC);

        contactRecyclerViewAdapter.notifyDataSetChanged();
    }

    private Set<String> getSelectedPhoneNumbers() {
        if (!TextUtils.isEmpty(editText_phoneNumber.getText().toString().trim())) {
            checkedNumbers.add(editText_phoneNumber.getText().toString().trim());
        }

        return checkedNumbers;
    }

    private void checkValidity() {
        if (TextUtils.isEmpty(editText_phoneNumber.getText().toString().trim()) &&
                checkedNumbers.isEmpty()) {
            setPositiveButtonVisibility(false);
        } else {
            setPositiveButtonVisibility(true);
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

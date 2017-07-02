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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ContactRecyclerViewAdapter;
import eu.power_switch.gui.dialog.eventbus.EventBusSupportDialogFragment;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.gui.listener.CheckBoxInteractionListener;
import eu.power_switch.phone.Contact;
import eu.power_switch.phone.ContactHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Created by Markus on 08.04.2016.
 */
public class AddPhoneNumberDialog extends EventBusSupportDialogFragment {

    public static final String KEY_PHONE_NUMBERS = "phoneNumbers";

    public static final Comparator<Contact> ALPHABETIC = new Comparator<Contact>() {
        @Override
        public int compare(Contact lhs, Contact rhs) {
            return lhs.getName()
                    .compareToIgnoreCase(rhs.getName());
        }
    };

    @BindView(R.id.layoutLoading)
    LinearLayout      layoutLoading;
    @BindView(R.id.editText_phoneNumber)
    TextInputEditText editText_phoneNumber;
    @BindView(R.id.recyclerView_phoneNumbers)
    RecyclerView      recyclerViewContacts;

    private Dialog dialog;
    private int    defaultTextColor;

    private ArrayList<Contact> contacts = new ArrayList<>();
    private ContactRecyclerViewAdapter contactRecyclerViewAdapter;

    private Set<String> checkedNumbers = new HashSet<>();

    public static AddPhoneNumberDialog newInstance(ArrayList<String> preselectedPhoneNumbers) {
        Bundle args = new Bundle();
        args.putStringArrayList(KEY_PHONE_NUMBERS, preselectedPhoneNumbers);

        AddPhoneNumberDialog fragment = new AddPhoneNumberDialog();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context any suitable context
     */
    public static void sendPhoneNumbersAddedBroadcast(Context context, Set<String> phoneNumbers) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_CALL_EVENT_PHONE_NUMBER_ADDED);
        intent.putStringArrayListExtra(KEY_PHONE_NUMBERS, new ArrayList<>(phoneNumbers));
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);

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

        contactRecyclerViewAdapter = new ContactRecyclerViewAdapter(getActivity(), contacts, checkedNumbers);
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

        builder.setTitle(R.string.add_phone_numbers);
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    sendPhoneNumbersAddedBroadcast(getContext(), getSelectedPhoneNumbers());
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getTargetFragment().getView()
                            .findViewById(R.id.listView), e);
                }
            }
        });

        builder.setNeutralButton(android.R.string.cancel, null);

        setPositiveButtonVisibility(false);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.show();

        defaultTextColor = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                .getTextColors()
                .getDefaultColor();

        checkValidity();

        Bundle args = getArguments();

        if (args != null && args.containsKey(KEY_PHONE_NUMBERS) && args.getStringArrayList(KEY_PHONE_NUMBERS) != null) {
            checkedNumbers.addAll(args.getStringArrayList(KEY_PHONE_NUMBERS));
        }

        refreshContacts();

        return dialog;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_add_phone_number;
    }

    private void refreshContacts() {
        layoutLoading.setVisibility(View.VISIBLE);
        recyclerViewContacts.setVisibility(View.GONE);

        contacts.clear();
        new AsyncTask<Void, Void, AsyncTaskResult<Contact>>() {

            @Override
            protected AsyncTaskResult<Contact> doInBackground(Void... params) {
                try {
                    List<Contact> contactsList = ContactHelper.getContacts(getActivity());

                    Contact[] contactsArray = new Contact[contactsList.size()];
                    contactsList.toArray(contactsArray);

                    return new AsyncTaskResult<>(contactsArray);
                } catch (Exception e) {
                    return new AsyncTaskResult<>(e);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<Contact> asyncTaskResult) {
                if (asyncTaskResult.isSuccess()) {
                    contacts.addAll(asyncTaskResult.getResult());
                    Collections.sort(contacts, ALPHABETIC);
                } else {
                    StatusMessageHandler.showErrorMessage(getActivity(), asyncTaskResult.getException());
                }

                contactRecyclerViewAdapter.notifyDataSetChanged();

                layoutLoading.setVisibility(View.GONE);
                recyclerViewContacts.setVisibility(View.VISIBLE);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Set<String> getSelectedPhoneNumbers() {
        if (!TextUtils.isEmpty(editText_phoneNumber.getText()
                .toString()
                .trim())) {
            checkedNumbers.add(editText_phoneNumber.getText()
                    .toString()
                    .trim());
        }

        return checkedNumbers;
    }

    private void checkValidity() {
        if (TextUtils.isEmpty(editText_phoneNumber.getText()
                .toString()
                .trim()) && checkedNumbers.isEmpty()) {
            setPositiveButtonVisibility(false);
        } else {
            setPositiveButtonVisibility(true);
        }
    }

    private void setPositiveButtonVisibility(boolean visibility) {
        if (dialog != null) {
            if (visibility) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(defaultTextColor);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setClickable(true);
            } else {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(Color.GRAY);
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setClickable(false);
            }
        }
    }

}

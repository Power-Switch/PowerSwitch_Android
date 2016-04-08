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

package eu.power_switch.gui.fragment.configure_call;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.PhoneNumberRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddPhoneNumberDialog;
import eu.power_switch.gui.dialog.ConfigurationDialogFragment;
import eu.power_switch.gui.dialog.ConfigureCallDialog;

/**
 * Created by Markus on 05.04.2016.
 */
public class ConfigureCallDialogPage1ContactsFragment extends ConfigurationDialogFragment {

    private View rootView;

    private long callId = -1;

    private ArrayList<String> phoneNumbers = new ArrayList<>();
    private PhoneNumberRecyclerViewAdapter phoneNumberRecyclerViewAdapter;
    private RecyclerView recyclerViewContacts;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_call_page_1, container, false);

        recyclerViewContacts = (RecyclerView) rootView.findViewById(R.id.recyclerView_contacts);
        phoneNumberRecyclerViewAdapter = new PhoneNumberRecyclerViewAdapter(getActivity(), phoneNumbers);
        recyclerViewContacts.setAdapter(phoneNumberRecyclerViewAdapter);
        phoneNumberRecyclerViewAdapter.setOnDeleteClickListener(new PhoneNumberRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, final int position) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.delete)
                        .setMessage(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    phoneNumbers.remove(position);
                                    phoneNumberRecyclerViewAdapter.notifyDataSetChanged();
                                    notifyConfigurationChanged();
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                                }
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();
            }
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewContacts.setLayoutManager(layoutManager);

        final FloatingActionButton addContactFAB = (FloatingActionButton) rootView.findViewById(R.id.add_contact_fab);
        addContactFAB.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        final Fragment fragment = this;
        addContactFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPhoneNumberDialog addPhoneNumberDialog = new AddPhoneNumberDialog();
                addPhoneNumberDialog.setTargetFragment(fragment, 0);
                addPhoneNumberDialog.show(getFragmentManager(), null);
            }
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureCallDialog.CALL_ID_KEY)) {
            callId = args.getLong(ConfigureCallDialog.CALL_ID_KEY);
            initializeCallData(callId);
        }

        return rootView;
    }

    private void initializeCallData(long callId) {
        try {
//            Call call = DatabaseHandler.getCall(callId);

        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

}

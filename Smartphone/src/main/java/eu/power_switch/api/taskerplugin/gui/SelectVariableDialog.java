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

package eu.power_switch.api.taskerplugin.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.api.taskerplugin.EditActivity;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ButterKnifeDialogFragment;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * Dialog used to quickly select a tasker variable
 * <p/>
 * Created by Markus on 24.05.2016.
 */
public class SelectVariableDialog extends ButterKnifeDialogFragment {

    public static final String KEY_FIELD             = "field";
    public static final String KEY_SELECTED_VARIABLE = "selectedVariable";

    private static final String KEY_RELEVANT_VARIABLES = "relevantVariables";

    private ArrayList<String>  relevantVariables;
    private EditActivity.Field field;

    @BindView(R.id.listview_variable_names)
    ListView listViewApartments;

    public static SelectVariableDialog newInstance(List<String> relevantVariables, EditActivity.Field field) {
        Bundle args = new Bundle();
        args.putStringArrayList(KEY_RELEVANT_VARIABLES, new ArrayList<>(relevantVariables));
        args.putString(KEY_FIELD, field.toString());

        SelectVariableDialog fragment = new SelectVariableDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private static void sendVariableSelectedBroadcast(Context context, String variable, EditActivity.Field field) {
        Log.d(SelectVariableDialog.class, "sendVariableSelectedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_VARIABLE_SELECTED);
        intent.putExtra(KEY_SELECTED_VARIABLE, variable);
        intent.putExtra(KEY_FIELD, field.toString());

        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if (getArguments().containsKey(KEY_RELEVANT_VARIABLES)) {
            relevantVariables = getArguments().getStringArrayList(KEY_RELEVANT_VARIABLES);
        } else {
            relevantVariables = new ArrayList<>();
        }
        field = EditActivity.Field.valueOf(getArguments().getString(KEY_FIELD));

        ArrayAdapter<String> apartmentNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, relevantVariables);
        listViewApartments.setAdapter(apartmentNamesAdapter);
        listViewApartments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    sendVariableSelectedBroadcast(getActivity(), relevantVariables.get(position), field);
                } catch (Exception e) {
                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                }

                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setTitle(getString(R.string.select_variable));
        builder.setNeutralButton(R.string.close, null);

        Dialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_variable_chooser;
    }
}

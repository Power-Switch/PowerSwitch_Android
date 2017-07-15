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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.event.VariableSelectedEvent;
import eu.power_switch.gui.dialog.eventbus.EventBusDialogFragment;
import timber.log.Timber;

/**
 * Dialog used to quickly select a tasker variable
 * <p/>
 * Created by Markus on 24.05.2016.
 */
public class SelectVariableDialog extends EventBusDialogFragment {

    public static final String KEY_FIELD             = "field";
    public static final String KEY_SELECTED_VARIABLE = "selectedVariable";

    private static final String KEY_RELEVANT_VARIABLES = "relevantVariables";
    @BindView(R.id.listview_variable_names)
    ListView listViewApartments;
    private ArrayList<String>           relevantVariables;
    private VariableSelectedEvent.Field field;

    public static SelectVariableDialog newInstance(List<String> relevantVariables, VariableSelectedEvent.Field field) {
        Bundle args = new Bundle();
        args.putStringArrayList(KEY_RELEVANT_VARIABLES, new ArrayList<>(relevantVariables));
        args.putString(KEY_FIELD, field.toString());

        SelectVariableDialog fragment = new SelectVariableDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private static void notifyVariableSelected(String variable, VariableSelectedEvent.Field field) {
        Timber.d("notifyVariableSelected");
        EventBus.getDefault()
                .post(new VariableSelectedEvent(variable, field));
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
        field = VariableSelectedEvent.Field.valueOf(getArguments().getString(KEY_FIELD));

        ArrayAdapter<String> apartmentNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, relevantVariables);
        listViewApartments.setAdapter(apartmentNamesAdapter);
        listViewApartments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    notifyVariableSelected(relevantVariables.get(position), field);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getActivity(), e);
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

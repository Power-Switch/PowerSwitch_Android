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

package eu.power_switch.gui.fragment.configure_receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.ConfigureReceiverDialog;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

import static eu.power_switch.obj.receiver.Receiver.Brand;

/**
 * "Type" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage2TypeFragment extends Fragment {

    private View rootView;

    private ListView brandListView;
    private ArrayAdapter brandNamesAdapter;

    private ListView modelListView;
    private ArrayAdapter modelNamesAdapter;

    private TextView modelTextView;

    /**
     * Used to notify the summary page that some info has changed
     *
     * @param context
     * @param brand   Current selected Brand name
     * @param model   Current selected Model name
     */
    public static void sendBrandModelChangedBroadcast(Context context, String brand, String model) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_BRAND_MODEL_CHANGED);
        intent.putExtra("brand", brand);
        intent.putExtra("model", model);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.dialog_fragment_configure_receiver_page_2, container, false);

        brandListView = (ListView) rootView.findViewById(R.id.listView_brands);
        brandNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, getResources()
                .getStringArray(R.array.brand_array));
        brandListView.setAdapter(brandNamesAdapter);
        brandListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateModelList((String) brandNamesAdapter.getItem(position));
                sendBrandModelChangedBroadcast(getActivity(), getSelectedBrand(), getSelectedModel());
            }
        });

        modelTextView = (TextView) rootView.findViewById(R.id.textView_model);

        modelListView = (ListView) rootView.findViewById(R.id.listView_models);
        modelNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, new ArrayList<String>());
        modelListView.setAdapter(modelNamesAdapter);
        modelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendBrandModelChangedBroadcast(getActivity(), getSelectedBrand(), getSelectedModel());
            }
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey(ConfigureReceiverDialog.RECEIVER_ID_KEY)) {
            long receiverId = args.getLong(ConfigureReceiverDialog.RECEIVER_ID_KEY);
            initializeReceiverData(receiverId);
        }

        return rootView;
    }

    private String getSelectedBrand() {
        try {
            int position = brandListView.getCheckedItemPosition();
            String brand = brandNamesAdapter.getItem(position).toString();
            return brand;
        } catch (Exception e) {
            return null;
        }
    }

    private String getSelectedModel() {
        try {
            int position = modelListView.getCheckedItemPosition();
            return modelNamesAdapter.getItem(position).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void updateModelList(String brandName) {
        Brand brandEnum = Brand.getEnum(brandName);

        switch (brandEnum) {
            case BAT: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_bat_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case BRENNENSTUHL: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_brennenstuhl_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case ELRO: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_elro_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case HAMA: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_hama_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case INTERTECHNO: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_intertechno_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case INTERTEK: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_intertek_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case MUMBI: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_mumbi_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case POLLIN_ELECTRONIC: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_pollin_electronic_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case REV: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_rev_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case ROHRMOTOR24: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_rohrmotor24_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case UNIVERSAL: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_universal_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            case VIVANCO: {
                modelNamesAdapter.clear();
                String[] array = getResources().getStringArray(R.array.model_vivanco_array);
                modelNamesAdapter.addAll(array);

                setModelVisibility(View.VISIBLE);
                break;
            }
            default:
                setModelVisibility(View.GONE);
                break;
        }

        if (modelNamesAdapter.getCount() > modelListView.getSelectedItemPosition()) {
            modelListView.setItemChecked(0, true);
            modelListView.smoothScrollToPosition(0);
        }

        modelNamesAdapter.notifyDataSetChanged();
    }

    private void initializeReceiverData(long receiverId) {
        if (receiverId == -1) {
            // init blank
            brandListView.setItemChecked(0, true);
            updateModelList(brandListView.getSelectedItem().toString());

            modelListView.setItemChecked(0, true);
        } else {
            try {
                // init existing receiver
                final Receiver receiver = DatabaseHandler.getReceiver(receiverId);

                int brandPosition = brandNamesAdapter.getPosition(receiver.getBrand());
                brandListView.setItemChecked(brandPosition, true);
                brandListView.smoothScrollToPosition(brandPosition);
                updateModelList(receiver.getBrand().toString());

                int modelPosition = modelNamesAdapter.getPosition(receiver.getModel());
                modelListView.setItemChecked(modelPosition, true);
                modelListView.smoothScrollToPosition(modelPosition);

            } catch (Exception e) {
                Log.e(e);
                StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
            }
        }
    }

    private void setModelVisibility(int visibility) {
        modelTextView.setVisibility(visibility);
        modelListView.setVisibility(visibility);
    }
}

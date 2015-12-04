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
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

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

    private AppCompatTextView modelTextView;

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
        if (brand.equals(Receiver.BRAND_UNIVERSAL)) {
            model = Receiver.BRAND_UNIVERSAL;
        }
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

        modelTextView = (AppCompatTextView) rootView.findViewById(R.id.textView_model);

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
        if (args != null && args.containsKey("ReceiverId")) {
            long receiverId = args.getLong("ReceiverId");
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
        if (brandName.equals(Receiver.BRAND_BAT)) {
            modelNamesAdapter.clear();
            String[] array = getResources().getStringArray(R.array.model_bat_array);
            for (String string : array) {
                modelNamesAdapter.add(string);
            }

            setModelVisibility(View.VISIBLE);
        } else if (brandName.equals(Receiver.BRAND_BRENNENSTUHL)) {
            modelNamesAdapter.clear();
            String[] array = getResources().getStringArray(R.array.model_brennenstuhl_array);
            for (String string : array) {
                modelNamesAdapter.add(string);
            }

            setModelVisibility(View.VISIBLE);
        } else if (brandName.equals(Receiver.BRAND_ELRO)) {
            modelNamesAdapter.clear();
            String[] array = getResources().getStringArray(R.array.model_elro_array);
            for (String string : array) {
                modelNamesAdapter.add(string);
            }

            setModelVisibility(View.VISIBLE);
        } else if (brandName.equals(Receiver.BRAND_INTERTECHNO)) {
            modelNamesAdapter.clear();
            String[] array = getResources().getStringArray(R.array.model_intertechno_array);
            for (String string : array) {
                modelNamesAdapter.add(string);
            }

            setModelVisibility(View.VISIBLE);
        } else if (brandName.equals(Receiver.BRAND_MUMBI)) {
            modelNamesAdapter.clear();
            String[] array = getResources().getStringArray(R.array.model_mumbi_array);
            for (String string : array) {
                modelNamesAdapter.add(string);
            }

            setModelVisibility(View.VISIBLE);
        } else if (brandName.equals(Receiver.BRAND_POLLIN_ELECTRONIC)) {
            modelNamesAdapter.clear();
            String[] array = getResources().getStringArray(R.array.model_pollin_electronic_array);
            for (String string : array) {
                modelNamesAdapter.add(string);
            }

            setModelVisibility(View.VISIBLE);
        } else if (brandName.equals(Receiver.BRAND_REV)) {
            modelNamesAdapter.clear();
            String[] array = getResources().getStringArray(R.array.model_rev_array);
            for (String string : array) {
                modelNamesAdapter.add(string);
            }

            setModelVisibility(View.VISIBLE);
        } else if (brandName.equals(Receiver.BRAND_ROHRMOTOR24)) {
            modelNamesAdapter.clear();
            String[] array = getResources().getStringArray(R.array.model_rohrmotor24_array);
            for (String string : array) {
                modelNamesAdapter.add(string);
            }

            setModelVisibility(View.VISIBLE);
        } else if (brandName.equals(Receiver.BRAND_UNIVERSAL)) {
            setModelVisibility(View.INVISIBLE);

        } else if (brandName.equals(Receiver.BRAND_VIVANCO)) {
            modelNamesAdapter.clear();
            String[] array = getResources().getStringArray(R.array.model_vivanco_array);
            for (String string : array) {
                modelNamesAdapter.add(string);
            }

            setModelVisibility(View.VISIBLE);
        } else {
            setModelVisibility(View.GONE);
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
            // init existing receiver
            final Receiver receiver = DatabaseHandler.getReceiver(receiverId);

            int brandPosition = brandNamesAdapter.getPosition(receiver.getBrand());
            brandListView.setItemChecked(brandPosition, true);
            brandListView.smoothScrollToPosition(brandPosition);
            updateModelList(receiver.getBrand());

            int modelPosition = modelNamesAdapter.getPosition(receiver.getModel());
            modelListView.setItemChecked(modelPosition, true);
            modelListView.smoothScrollToPosition(modelPosition);
        }
    }

    private void setModelVisibility(int visibility) {
        modelTextView.setVisibility(visibility);
        modelListView.setVisibility(visibility);
    }
}

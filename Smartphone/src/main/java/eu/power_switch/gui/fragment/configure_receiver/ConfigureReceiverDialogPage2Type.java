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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import de.markusressel.android.library.tutorialtooltip.builder.IndicatorBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.MessageBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.TutorialTooltipBuilder;
import de.markusressel.android.library.tutorialtooltip.builder.TutorialTooltipChainBuilder;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnIndicatorClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.OnMessageClickedListener;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipIndicator;
import de.markusressel.android.library.tutorialtooltip.interfaces.TutorialTooltipMessage;
import de.markusressel.android.library.tutorialtooltip.view.TooltipId;
import de.markusressel.android.library.tutorialtooltip.view.TutorialTooltipView;
import eu.power_switch.R;
import eu.power_switch.event.ReceiverBrandOrModelChangedEvent;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.ReceiverConfigurationHolder;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.Brand;

/**
 * "Type" Fragment used in Configure Receiver Dialog
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureReceiverDialogPage2Type extends ConfigurationDialogPage<ReceiverConfigurationHolder> {

    @BindView(R.id.listView_brands)
    ListView brandListView;
    @BindView(R.id.listView_models)
    ListView modelListView;
    @BindView(R.id.textView_model)
    TextView modelTextView;

    private ArrayAdapter<String> brandNamesAdapter;
    private ArrayAdapter<String> modelNamesAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        brandNamesAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_single_choice,
                getResources().getStringArray(R.array.brand_array));
        brandListView.setAdapter(brandNamesAdapter);
        brandListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Brand selectedBrand = getSelectedBrand();
//                updateModelList(Brand.getEnum(brandNamesAdapter.getItem(position)));
                updateModelList(selectedBrand);

                updateConfiguration();
            }
        });

        modelNamesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, new ArrayList<String>());
        modelListView.setAdapter(modelNamesAdapter);
        modelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateConfiguration();
            }
        });

        initializeReceiverData();

        createTutorial();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_receiver_page_2;
    }

    private void createTutorial() {
        OnMessageClickedListener onClickListener = new OnMessageClickedListener() {
            @Override
            public void onMessageClicked(TooltipId id, TutorialTooltipView tutorialTooltipView, TutorialTooltipMessage tutorialTooltipMessage,
                                         View view) {
                tutorialTooltipView.remove(true);
            }
        };

        OnIndicatorClickedListener onIndicatorClickedListener = new OnIndicatorClickedListener() {
            @Override
            public void onIndicatorClicked(TooltipId tooltipId, TutorialTooltipView tutorialTooltipView,
                                           TutorialTooltipIndicator tutorialTooltipIndicator, View view) {
                tutorialTooltipView.remove(true);
            }
        };

        TutorialTooltipBuilder message1 = new TutorialTooltipBuilder(getActivity()).attachToDialog(getParentConfigurationDialog().getDialog())
                .anchor(brandListView, TutorialTooltipView.Gravity.CENTER)
                .indicator(new IndicatorBuilder().onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_receiver_brand_select__text)
                        .gravity(TutorialTooltipView.Gravity.BOTTOM)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_receiver_name__id)
                .build();

        TutorialTooltipBuilder message2 = new TutorialTooltipBuilder(getActivity()).attachToDialog(getParentConfigurationDialog().getDialog())
                .anchor(modelListView, TutorialTooltipView.Gravity.CENTER)
                .indicator(new IndicatorBuilder().onClick(onIndicatorClickedListener)
                        .build())
                .message(new MessageBuilder(getActivity()).text(R.string.tutorial__configure_receiver_model_select__text)
                        .gravity(TutorialTooltipView.Gravity.TOP)
                        .size(MessageBuilder.WRAP_CONTENT, MessageBuilder.WRAP_CONTENT)
                        .onClick(onClickListener)
                        .build())
                .oneTimeUse(R.string.tutorial__configure_receiver_model_select__id)
                .build();

        new TutorialTooltipChainBuilder().addItem(message1)
                .addItem(message2)
                .execute();
    }

    private Brand getSelectedBrand() {
        try {
            int    position = brandListView.getCheckedItemPosition();
            String brand    = brandNamesAdapter.getItem(position);
            return Brand.getEnum(brand);
        } catch (Exception e) {
            return null;
        }
    }

    private String getSelectedModel() {
        try {
            int position = modelListView.getCheckedItemPosition();
            return modelNamesAdapter.getItem(position);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateModelList(Brand brand) {
        switch (brand) {
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

    private void initializeReceiverData() {
        Receiver receiver = getConfiguration().getReceiver();
        if (receiver == null) {
            // init blank
            brandListView.setItemChecked(0, true);
            updateModelList(getSelectedBrand());

            modelListView.setItemChecked(0, true);
        } else {
            try {
                // init existing receiver
                Brand brand         = getConfiguration().getBrand();
                int   brandPosition = brandNamesAdapter.getPosition(brand.getName());
                brandListView.setItemChecked(brandPosition, true);
                brandListView.smoothScrollToPosition(brandPosition);
                updateModelList(brand);

                int modelPosition = modelNamesAdapter.getPosition(getConfiguration().getModel());
                modelListView.setItemChecked(modelPosition, true);
                modelListView.smoothScrollToPosition(modelPosition);

            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    private void updateConfiguration() {
        getConfiguration().setBrand(getSelectedBrand());
        getConfiguration().setModel(getSelectedModel());

        EventBus.getDefault()
                .post(new ReceiverBrandOrModelChangedEvent(getConfiguration().getModel(), getSelectedBrand()));

        notifyConfigurationChanged();
    }

    private void setModelVisibility(int visibility) {
        modelTextView.setVisibility(visibility);
        modelListView.setVisibility(visibility);
    }
}

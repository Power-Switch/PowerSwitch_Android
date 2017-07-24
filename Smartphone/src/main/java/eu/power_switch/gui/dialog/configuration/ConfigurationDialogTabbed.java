/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog.configuration;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.dialog.eventbus.EventBusSupportDialogFragment;
import eu.power_switch.persistence.PersistenceHandler;
import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

/**
 * Abstract class defining a configuration Dialog with multiple tabs
 * <p/>
 * Every configuration Dialog has a bottom bar with 4 Buttons (Delete, Cancel, Next, Save)
 * <p/>
 * Created by Markus on 27.12.2015.
 */
public abstract class ConfigurationDialogTabbed<Configuration extends ConfigurationHolder> extends EventBusSupportDialogFragment {

    @BindView(R.id.imageButton_delete)
    protected ImageButton imageButtonDelete;
    @BindView(R.id.imageButton_cancel)
    protected ImageButton imageButtonCancel;
    @BindView(R.id.imageButton_save)
    protected ImageButton imageButtonSave;

    @BindView(R.id.tabLayout_configure_dialog)
    TabLayout   tabLayout;
    @BindView(R.id.tabHost)
    ViewPager   tabViewPager;
    @BindView(R.id.imageButton_next)
    ImageButton imageButtonNext;

    @Inject
    protected PersistenceHandler persistenceHandler;

    @Getter
    @Setter
    private boolean                                      modified;
    @Getter
    private ConfigurationDialogTabAdapter<Configuration> tabAdapter;

    @Getter
    @Setter
    private Configuration configuration;

    /**
     * NOT YET WORKING
     * <p>
     * Use this method to instantiate a configuration dialog
     *
     * @param clazz the dialog class that should be instantiated
     *
     * @return Instance of the configuration dialog
     */
//    @Deprecated
//    public static <ConfigurationDialog extends ConfigurationDialogTabbed<Configuration>, Configuration extends ConfigurationHolder> ConfigurationDialogTabbed newInstance(
//            @NonNull Class<ConfigurationDialog> clazz, @NonNull Fragment targetFragment) {
//        Bundle args = new Bundle();
//
//        if (!ConfigurationDialogPage.class.isAssignableFrom(clazz)) {
//            throw new IllegalArgumentException("Invalid class type! Must be of type " + ConfigurationDialogTabbed.class.getName() + " or subclass!");
//        }
//
//        try {
//            Constructor<ConfigurationDialog>         constructor = clazz.getConstructor();
//            ConfigurationDialogTabbed<Configuration> dialog      = constructor.newInstance();
//
////            Constructor<Configuration> configConstructor =
//
//            dialog.setTargetFragment(targetFragment, 0);
//            dialog.setArguments(args);
//            return dialog;
//        } catch (Exception e) {
//            throw new RuntimeException("Couldn't instantiate configuration dialog!", e);
//        }
//    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (configuration == null) {
            throw new IllegalStateException("Missing ConfigurationHolder!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getDialogTitle());

        tabViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateBottomBarButtons();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        imageButtonDelete.setImageDrawable(IconicsHelper.getDeleteIcon(getActivity(), ContextCompat.getColor(getActivity(), R.color.delete_color)));
        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteExistingConfigurationFromDatabase();
            }
        });

        imageButtonCancel.setImageDrawable(IconicsHelper.getCancelIcon(getActivity()));
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modified) {
                    // ask to really close
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getDialog().cancel();
                                }
                            })
                            .setNeutralButton(android.R.string.no, null)
                            .setMessage(R.string.all_changes_will_be_lost)
                            .show();
                } else {
                    getDialog().dismiss();
                }
            }
        });

        imageButtonNext.setImageDrawable(IconicsHelper.getNextIcon(getActivity()));
        imageButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabViewPager.setCurrentItem(tabViewPager.getCurrentItem() + 1, true);
                updateBottomBarButtons();
            }
        });

        imageButtonSave.setImageDrawable(IconicsHelper.getSaveIcon(getActivity()));
        imageButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!modified) {
                    getDialog().dismiss();
                } else {
                    try {
                        saveConfiguration();
                    } catch (Exception e) {
                        statusMessageHandler.showErrorMessage(getActivity(), e);
                    }
                    getDialog().dismiss();
                }
            }
        });

        init(inflater, container, savedInstanceState);

        // hide/show delete button if existing data is initialized
        initializeFromExistingData(getArguments());

        setupTabAdapter();

        if (getConfiguration().isValid()) {
            imageButtonDelete.setVisibility(View.VISIBLE);
        } else {
            imageButtonDelete.setVisibility(View.GONE);
        }
        setSaveButtonState(getConfiguration().isValid());

        setModified(false);

        return rootView;
    }

    /**
     * Used to notify parent Dialog that configuration has changed
     * <p>
     * Call this method when the configuration of the dialog has changed and UI has to be updated.
     * This will check the dialog for validity and set the bottom bar buttons accordingly.
     * This will also mark the dialog as modified so a confirmation dialog is shown when aborting.
     */
    public void notifyConfigurationChanged() {
        setModified(true);

        try {
            setSaveButtonState(isValid());
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_configuration_tabbed;
    }

    /**
     * Initialize this dialog
     *
     * @param inflater           LayoutInflater
     * @param container          ViewGroup
     * @param savedInstanceState Bundle
     */
    protected abstract void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * Initialize your dialog in here using passed in arguments
     *
     * @param arguments arguments passed in via setArguments()
     */
    protected abstract void initializeFromExistingData(Bundle arguments);

    @StringRes
    protected abstract int getDialogTitle();

    /**
     * For each page of your ConfigurationDialog add a list item idicating it's name and Class to load
     */
    protected abstract void addPageEntries(List<PageEntry<Configuration>> pageEntries);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // ask to really close
        Dialog dialog = new Dialog(getActivity()) {
            @Override
            public void onBackPressed() {
                if (modified) {
                    // ask to really close
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                            .setPositiveButton(android.R.string.yes, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getDialog().cancel();
                                }
                            })
                            .setNeutralButton(android.R.string.no, null)
                            .setMessage(R.string.all_changes_will_be_lost)
                            .show();
                } else {
                    getDialog().cancel();
                }
            }
        };
        dialog.setTitle(getDialogTitle());
        dialog.setCanceledOnTouchOutside(isCancelableOnTouchOutside());
        dialog.getWindow()
                .setSoftInputMode(getSoftInputMode());

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow()
                .getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow()
                .setAttributes(lp);

        dialog.show();

        return dialog;
    }

    /**
     * Defines if the Dialog is cancelable on touch outside of the dialog
     * <p/>
     * Default: False
     *
     * @return true if cancelable on touch outside of the dialog view, false otherwise
     */
    protected boolean isCancelableOnTouchOutside() {
        return false;
    }

    /**
     * Defines Soft Input Mode
     * <p/>
     * Default:
     * WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
     *
     * @return integer representing the mode
     */
    protected int getSoftInputMode() {
        return WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
    }

    /**
     * Setup a FragmentPagerAdapter for this configuration dialog
     */
    private void setupTabAdapter() {
        // create a list of pageEntries
        List<PageEntry<Configuration>> pages = new ArrayList<>();

        // let the dialog implementation add it's pages
        addPageEntries(pages);

        // and create a tab adapter for those pages
        tabAdapter = new ConfigurationDialogTabAdapter<>(this, getTargetFragment(), pages);

        tabViewPager.setAdapter(tabAdapter);
        tabViewPager.setOffscreenPageLimit(tabAdapter.getCount());

        tabLayout.setupWithViewPager(tabViewPager);

        if (getTabAdapter().getCount() == 1) {
            tabLayout.setVisibility(View.GONE);
            updateBottomBarButtons();
        }
    }

    /**
     * This method is called when the user wants to save the current configuration.
     * Save the current configuration of the entity to your persistence handler.
     * The dialog will be closed automatically.
     */
    protected abstract void saveConfiguration() throws Exception;

    /**
     * This method is called when the user wants to delete the existing configuration.
     * This implies that an existing configuration was opened in the first place.
     * Delete the existing configuration of the entity from your persistence handler in this method.
     * The dialog will be closed automatically.
     */
    protected abstract void deleteExistingConfigurationFromDatabase();

    /**
     * Checks if the current dialog configuration is valid
     *
     * @return true if the current configuration is valid, false otherwise
     */
    private boolean isValid() {
        try {
            return getConfiguration().isValid();
        } catch (Exception e) {
            Timber.e(e);
            return false;
        }
    }

    private void updateBottomBarButtons() {
        if (tabViewPager.getCurrentItem() == tabAdapter.getCount() - 1) {
            imageButtonSave.setVisibility(View.VISIBLE);
            imageButtonNext.setVisibility(View.GONE);
        } else {
            imageButtonSave.setVisibility(View.GONE);
            imageButtonNext.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set the state of the save button in the bottom bar
     *
     * @param enabled true: green and clickable, false: gray and NOT clickable
     */
    private void setSaveButtonState(boolean enabled) {
        if (enabled) {
            imageButtonSave.setColorFilter(ContextCompat.getColor(getActivity(), eu.power_switch.shared.R.color.active_green));
            imageButtonSave.setClickable(true);
        } else {
            imageButtonSave.setColorFilter(ContextCompat.getColor(getActivity(), eu.power_switch.shared.R.color.inactive_gray));
            imageButtonSave.setClickable(false);
        }
    }

}

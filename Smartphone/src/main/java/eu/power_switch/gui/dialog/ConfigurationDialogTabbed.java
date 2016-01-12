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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.shared.log.Log;

/**
 * Abstract class defining a configuration Dialog with multiple tabs
 * <p/>
 * Every configuration Dialog has a bottom bar with 4 Buttons (Delete, Cancel, Next, Save)
 * <p/>
 * Created by Markus on 27.12.2015.
 */
public abstract class ConfigurationDialogTabbed extends DialogFragment {

    protected ImageButton imageButtonDelete;
    protected ImageButton imageButtonCancel;
    protected ImageButton imageButtonSave;

    private String title = "Configuration";
    private boolean modified;
    private View rootView;
    private Runnable deleteAction;
    private TabLayout tabLayout;
    private ViewPager tabViewPager;
    private FragmentPagerAdapter customTabAdapter;
    private ImageButton imageButtonNext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_configuration_tabbed, null);

        tabViewPager = (ViewPager) rootView.findViewById(R.id.tabHost);
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

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout_configure_dialog);

        imageButtonDelete = (ImageButton) rootView.findViewById(R.id.imageButton_delete);
        imageButtonDelete.setImageDrawable(IconicsHelper.getDeleteIcon(getActivity(), R.color.delete_color));
        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteAction != null) {
                    deleteAction.run();
                }
            }
        });

        imageButtonCancel = (ImageButton) rootView.findViewById(R.id.imageButton_cancel);
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

        imageButtonNext = (ImageButton) rootView.findViewById(R.id.imageButton_next);
        imageButtonNext.setImageDrawable(IconicsHelper.getNextIcon(getActivity()));
        imageButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabViewPager.setCurrentItem(tabViewPager.getCurrentItem() + 1, true);
                updateBottomBarButtons();
            }
        });

        imageButtonSave = (ImageButton) rootView.findViewById(R.id.imageButton_save);
        imageButtonSave.setImageDrawable(IconicsHelper.getSaveIcon(getActivity()));
        imageButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!modified) {
                    getDialog().dismiss();
                } else {
                    saveCurrentConfigurationToDatabase();
                    getDialog().dismiss();
                }
            }
        });

        init(inflater, container, savedInstanceState);

        initExistingData(getArguments());

        return rootView;
    }

    protected abstract void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    private void updateBottomBarButtons() {
        if (tabViewPager.getCurrentItem() == customTabAdapter.getCount() - 1) {
            imageButtonSave.setVisibility(View.VISIBLE);
            imageButtonNext.setVisibility(View.GONE);
        } else {
            imageButtonSave.setVisibility(View.GONE);
            imageButtonNext.setVisibility(View.VISIBLE);
        }
    }

    protected abstract void initExistingData(Bundle arguments);

    protected void setDialogTitle(String title) {
        this.title = title;
    }

    public FragmentPagerAdapter getTabAdapter() {
        return customTabAdapter;
    }

    protected void setTabAdapter(FragmentPagerAdapter fragmentPagerAdapter) {
        customTabAdapter = fragmentPagerAdapter;

        tabViewPager.setAdapter(customTabAdapter);
        tabViewPager.setOffscreenPageLimit(customTabAdapter.getCount());

        tabLayout.setTabsFromPagerAdapter(customTabAdapter);
        tabLayout.setupWithViewPager(tabViewPager);
    }

    /**
     * Set a deleteAction
     *
     * @param runnable Runnable containing delete actions
     */
    protected void setDeleteAction(Runnable runnable) {
        this.deleteAction = runnable;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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
        dialog.setTitle(title);
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager
                .LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

        dialog.show();
        return dialog;
    }

    /**
     * Get modification state of this Dialog
     *
     * @return true if modifications (by user or system) have been made
     */
    protected boolean isModified() {
        return modified;
    }

    /**
     * Set the state of this Dialog
     *
     * @param modified true if Dialog has been edited
     */
    protected void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Checks if the current dialog configuration is valid
     *
     * @return true if the current configuration is valid, false otherwise
     */
    protected abstract boolean checkValidity() throws Exception;

    /**
     * Call this method when the configuration of the dialog has changed and UI has to be updated
     * f.ex. bottom bar buttons
     */
    protected void notifyConfigurationChanged() {
        setModified(true);
        try {
            setSaveButtonState(checkValidity());
        } catch (Exception e) {
            Log.e(e);
            setSaveButtonState(false);
        }
    }

    protected void setSaveButtonState(boolean enabled) {
        if (enabled) {
            imageButtonSave.setColorFilter(ContextCompat.getColor(getActivity(), eu.power_switch.shared.R.color
                    .active_green));
            imageButtonSave.setClickable(true);
        } else {
            imageButtonSave.setColorFilter(ContextCompat.getColor(getActivity(), eu.power_switch.shared.R.color
                    .inactive_gray));
            imageButtonSave.setClickable(false);
        }
    }

    /**
     * This method is called when the user wants to save the current configuration to database and close the dialog
     * Save the current configuration of your object to database in this method
     */
    protected abstract void saveCurrentConfigurationToDatabase();
}

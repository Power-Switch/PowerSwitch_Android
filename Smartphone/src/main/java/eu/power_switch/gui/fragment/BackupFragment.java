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

package eu.power_switch.gui.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.backup.Backup;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.event.BackupChangedEvent;
import eu.power_switch.google_play_services.firebase.database.FirebaseDatabaseHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.BackupRecyclerViewAdapter;
import eu.power_switch.gui.dialog.CreateBackupDialog;
import eu.power_switch.gui.dialog.EditBackupDialog;
import eu.power_switch.gui.dialog.PathChooserDialog;
import eu.power_switch.gui.dialog.UpgradeBackupsProcessingDialog;
import eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.constants.TutorialConstants;
import eu.power_switch.shared.event.PermissionChangedEvent;
import eu.power_switch.shared.permission.PermissionHelper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Fragment holding a list of all Backups
 */
public class BackupFragment extends RecyclerViewFragment<Backup> {

    private static final String[]           NEEDED_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final Comparator<Backup> BACKUP_COMPARATOR  = new Comparator<Backup>() {
        @Override
        public int compare(Backup lhs, Backup rhs) {
            return lhs.compareDate(rhs);
        }
    };
    @BindView(R.id.textView_backupPath)
    TextView             textViewBackupPath;
    @BindView(R.id.add_fab)
    FloatingActionButton fab;

    @Inject
    BackupHandler backupHandler;

    private ArrayList<Backup> backups = new ArrayList<>();
    private BackupRecyclerViewAdapter backupArrayAdapter;

    /**
     * Used to notify Backup Fragment (this) that Backups have changed
     */
    public static void notifyBackupsChanged() {
        EventBus.getDefault()
                .post(new BackupChangedEvent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        textViewBackupPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(getActivity())) {
                        PermissionHelper.showMissingPermissionDialog(getActivity(),
                                PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION,
                                NEEDED_PERMISSIONS);
                        return;
                    }

                    PathChooserDialog pathChooserDialog = PathChooserDialog.newInstance();
                    pathChooserDialog.setTargetFragment(BackupFragment.this, 0);
                    pathChooserDialog.show(getActivity().getSupportFragmentManager(), null);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        backupArrayAdapter = new BackupRecyclerViewAdapter(this, getActivity(), backups, backupHandler, statusMessageHandler);
//        backupArrayAdapter.setOnItemClickListener(new BackupRecyclerViewAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View itemView, int position) {
//                try {
//                    final Backup backup = backups.get(position);
//
//                    Uri fileUri = Uri.fromFile(new File(backup.getPath()));
//                    RestoreBackupFromFileActivity.newInstance(getActivity(), fileUri);
//                } catch (Exception e) {
//                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
//                }
//            }
//        });
        backupArrayAdapter.setOnItemLongClickListener(new BackupRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                try {
                    final Backup backup = backups.get(position);

                    EditBackupDialog editBackupDialog = EditBackupDialog.newInstance(backup.getName());
                    editBackupDialog.setTargetFragment(BackupFragment.this, 0);
                    editBackupDialog.show(getActivity().getSupportFragmentManager(), null);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });
        getRecyclerView().setAdapter(backupArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);

        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(getActivity())) {
                        PermissionHelper.showMissingPermissionDialog(getActivity(),
                                PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION,
                                NEEDED_PERMISSIONS);
                        return;
                    }

                    CreateBackupDialog createBackupDialog = new CreateBackupDialog();
                    createBackupDialog.setTargetFragment(BackupFragment.this, 0);
                    createBackupDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        updateUI();

        // TODO: Cloud Backups
        // FirebaseStorageHandler firebaseStorageHandler = new FirebaseStorageHandler(getActivity());

        FirebaseDatabaseHandler firebaseDatabaseHandler = new FirebaseDatabaseHandler();

        for (Backup backup : backups) {

        }

        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onPermissionChanged(PermissionChangedEvent permissionChangedEvent) {
        int   permissionRequestCode = permissionChangedEvent.getRequestCode();
        int[] results               = permissionChangedEvent.getGrantResults();

        if (permissionRequestCode == PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION) {
            if (results[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                updateUI();
            } else {
                // Permission Denied
                statusMessageHandler.showPermissionMissingMessage(getActivity(),
                        getRecyclerView(),
                        PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION,
                        NEEDED_PERMISSIONS);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onBackupChanged(BackupChangedEvent backupChangedEvent) {
        updateUI();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_backup;
    }

    private void showTutorial() {
        new MaterialShowcaseView.Builder(getActivity()).setTarget(fab)
                .setUseAutoRadius(false)
                .setRadius(64 * 3)
                .setDismissOnTouch(true)
                .setDismissText(getString(R.string.tutorial__got_it))
                .setContentText(getString(R.string.tutorial__backup_explanation))
                .singleUse(TutorialConstants.BACKUP_KEY)
                .setDelay(500)
                .show();
    }

    private void updateUI() {
        String backupPath = smartphonePreferencesHandler.get(SmartphonePreferencesHandler.KEY_BACKUP_PATH);
        textViewBackupPath.setText(backupPath);

        if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(getActivity())) {
            showEmpty();
            statusMessageHandler.showPermissionMissingMessage(getActivity(), getRecyclerView(), PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION,
                    NEEDED_PERMISSIONS);
        } else {
            updateListContent();

            if (backupHandler.oldBackupFormatsExist()) {
                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.old_backups_found_title)
                        .setMessage(R.string.old_backups_found_message)
                        .setView(R.layout.dialog_old_backup_format)
                        .setPositiveButton(R.string.convert, null)
                        .setNeutralButton(R.string.close, null)
                        .show();

                dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.checkbox_delete_old_format);

                                UpgradeBackupsProcessingDialog upgradeBackupsProcessingDialog = UpgradeBackupsProcessingDialog.newInstance(checkBox.isChecked());
                                upgradeBackupsProcessingDialog.show(getFragmentManager(), null);

                                dialog.dismiss();
                            }
                        });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_backup:
                try {
                    if (!PermissionHelper.isWriteExternalStoragePermissionAvailable(getActivity())) {
                        PermissionHelper.showMissingPermissionDialog(getActivity(),
                                PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION,
                                NEEDED_PERMISSIONS);
                        break;
                    }

                    CreateBackupDialog createBackupDialog = new CreateBackupDialog();
                    createBackupDialog.setTargetFragment(this, 0);
                    createBackupDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup_fragment_menu, menu);
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.create_backup)
                .setIcon(IconicsHelper.getAddIcon(getActivity(), color));

        boolean useOptionsMenuOnly = smartphonePreferencesHandler.get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        if (!useOptionsMenuOnly) {
            menu.findItem(R.id.create_backup)
                    .setVisible(false)
                    .setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (smartphonePreferencesHandler.get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }

        showTutorial();
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return backupArrayAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.backup_grid_span_count);
    }

    @Override
    public List<Backup> loadListData() throws Exception {
        ArrayList<Backup> backups = new ArrayList<>();

        for (Backup backup : backupHandler.getBackups()) {
            backups.add(backup);
        }

        Collections.sort(backups, BACKUP_COMPARATOR);

        return backups;
    }

    @Override
    protected void onListDataChanged(List<Backup> list) {
        backups.clear();
        backups.addAll(list);
    }
}

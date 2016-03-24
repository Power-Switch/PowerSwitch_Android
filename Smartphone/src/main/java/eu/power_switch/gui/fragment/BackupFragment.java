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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.backup.Backup;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.adapter.BackupRecyclerViewAdapter;
import eu.power_switch.gui.dialog.CreateBackupDialog;
import eu.power_switch.gui.dialog.EditBackupDialog;
import eu.power_switch.gui.dialog.PathChooserDialog;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.constants.TutorialConstants;
import eu.power_switch.shared.exception.backup.BackupNotFoundException;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.permission.PermissionHelper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Fragment holding a list of all Backups
 */
public class BackupFragment extends RecyclerViewFragment {

    private static final Comparator<Backup> backupsComparator = new Comparator<Backup>() {
        @Override
        public int compare(Backup lhs, Backup rhs) {
            return lhs.compareDate(rhs);
        }
    };

    private ArrayList<Backup> backups = new ArrayList<>();
    private RecyclerView recyclerViewBackups;
    private BackupRecyclerViewAdapter backupArrayAdapter;
    private BroadcastReceiver broadcastReceiver;
    private FloatingActionButton fab;
    private TextView textViewBackupPath;

    /**
     * Used to notify Backup Fragment (this) that Backups have changed
     *
     * @param context any suitable context
     */
    public static void sendBackupsChangedBroadcast(Context context) {
        Log.d("AddReceiverDialog", "sendReceiverChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_BACKUP_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onCreateViewEvent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_backup, container, false);

        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            setHasOptionsMenu(true);
        }

        final RecyclerViewFragment recyclerViewFragment = this;

        textViewBackupPath = (TextView) rootView.findViewById(R.id.textView_backupPath);
        textViewBackupPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.checkWriteExternalStoragePermission(getContext())) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.missing_permission)
                            .setMessage(R.string.missing_external_storage_permission)
                            .setNeutralButton(R.string.close, null)
                            .show();
                    return;
                }

                PathChooserDialog pathChooserDialog = PathChooserDialog.newInstance();
                pathChooserDialog.setTargetFragment(recyclerViewFragment, 0);
                pathChooserDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        recyclerViewBackups = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        backupArrayAdapter = new BackupRecyclerViewAdapter(this, getActivity(), backups);
        backupArrayAdapter.setOnItemClickListener(new BackupRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                final Backup backup = backups.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setPositiveButton(getActivity().getString(R.string.restore),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    BackupHandler backupHandler = new BackupHandler(getActivity());
                                    backupHandler.restoreBackup(backup.getName());

                                    // restart app to apply
                                    getActivity().finish();
                                    Intent intent = new Intent(getContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } catch (BackupNotFoundException e) {
                                    Log.e(e);
                                    StatusMessageHandler.showInfoMessage(
                                            recyclerViewFragment.getRecyclerView(),
                                            R.string.backup_not_found, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(
                                            recyclerViewFragment.getRecyclerView(), e);
                                }
                            }
                        }).setNeutralButton(getActivity().getString(android.R.string.cancel), null)
                        .setTitle(getActivity().getString(R.string.are_you_sure))
                        .setMessage(R.string.restore_backup_message);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        backupArrayAdapter.setOnItemLongClickListener(new BackupRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                final Backup backup = backups.get(position);

                EditBackupDialog editBackupDialog = EditBackupDialog.newInstance(backup.getName());
                editBackupDialog.setTargetFragment(recyclerViewFragment, 0);
                editBackupDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });
        recyclerViewBackups.setAdapter(backupArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.backup_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewBackups.setLayoutManager(layoutManager);

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.checkWriteExternalStoragePermission(getContext())) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.missing_permission)
                            .setMessage(R.string.missing_external_storage_permission)
                            .setNeutralButton(R.string.close, null)
                            .show();
                    return;
                }

                CreateBackupDialog createBackupDialog = new CreateBackupDialog();
                createBackupDialog.setTargetFragment(recyclerViewFragment, 0);
                createBackupDialog.show(getFragmentManager(), null);
            }
        });

        // BroadcastReceiver to get notifications from background service if data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("BackupFragment", "received intent: " + intent.getAction());

                switch (intent.getAction()) {
                    case LocalBroadcastConstants.INTENT_PERMISSION_CHANGED:
                        int permissionRequestCode = intent.getIntExtra(PermissionConstants.KEY_REQUEST_CODE, 0);
                        int[] results = intent.getIntArrayExtra(PermissionConstants.KEY_RESULTS);

                        if (permissionRequestCode == PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION) {
                            if (results[0] == PackageManager.PERMISSION_GRANTED) {
                                // Permission Granted
                                updateListContent();
                                StatusMessageHandler.showInfoMessage(getRecyclerView(),
                                        R.string.permission_granted, Snackbar.LENGTH_SHORT);
                            } else {
                                // Permission Denied
                                StatusMessageHandler.showPermissionMissingMessage(getActivity(),
                                        getRecyclerView(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            }
                        }

                        break;
                    case LocalBroadcastConstants.INTENT_BACKUP_CHANGED:
                        updateUI();
                }
            }
        };
    }

    @Override
    protected void onInitialized() {
        updateUI();
    }

    private void showTutorial() {
        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(fab)
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
        textViewBackupPath.setText(SmartphonePreferencesHandler.getBackupPath());

        if (!PermissionHelper.checkWriteExternalStoragePermission(getContext())) {
            showEmpty();
            requestExternalStoragePermission();
        } else {
            updateListContent();
        }
    }

    private void requestExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.d("Displaying storage permission rationale to provide additional context.");

            StatusMessageHandler.showPermissionMissingMessage(getActivity(), getRecyclerView(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            Log.d("Displaying default storage permission dialog to request permission");
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_backup:
                if (!PermissionHelper.checkWriteExternalStoragePermission(getContext())) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.missing_permission)
                            .setMessage(R.string.missing_external_storage_permission)
                            .setNeutralButton(R.string.close, null)
                            .show();
                    break;
                }

                CreateBackupDialog createBackupDialog = new CreateBackupDialog();
                createBackupDialog.setTargetFragment(this, 0);
                createBackupDialog.show(getFragmentManager(), null);
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup_fragment_menu, menu);
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.create_backup).setIcon(IconicsHelper.getAddIcon(getActivity(), color));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }

        showTutorial();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_BACKUP_CHANGED);
        intentFilter.addAction(LocalBroadcastConstants.INTENT_PERMISSION_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return recyclerViewBackups;
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return backupArrayAdapter;
    }

    @Override
    public List refreshListData() throws Exception {
        backups.clear();

        BackupHandler backupHandler = new BackupHandler(getActivity());
        for (Backup backup : backupHandler.getBackups()) {
            backups.add(backup);
        }

        Collections.sort(backups, backupsComparator);

        return backups;
    }
}

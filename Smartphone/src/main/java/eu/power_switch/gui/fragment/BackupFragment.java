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
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
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

import eu.power_switch.R;
import eu.power_switch.backup.Backup;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.exception.backup.BackupNotFoundException;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.adapter.BackupRecyclerViewAdapter;
import eu.power_switch.gui.dialog.CreateBackupDialog;
import eu.power_switch.gui.dialog.EditBackupDialog;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.constants.TutorialConstants;
import eu.power_switch.shared.log.Log;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Fragment holding a list of all Backups
 */
public class BackupFragment extends RecyclerViewFragment {

    private View rootView;
    private ArrayList<Backup> backups;
    private RecyclerView recyclerViewBackups;
    private BackupRecyclerViewAdapter backupArrayAdapter;
    private BroadcastReceiver broadcastReceiver;
    private FloatingActionButton fab;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_backup, container, false);
        setHasOptionsMenu(true);

        TextView textViewBackupPath = (TextView) rootView.findViewById(R.id.textView_backupPath);
        textViewBackupPath.setText(SmartphonePreferencesHandler.getBackupPath());

        backups = new ArrayList<>();
        recyclerViewBackups = (RecyclerView) rootView.findViewById(R.id.recyclerview_list_of_backups);
        backupArrayAdapter = new BackupRecyclerViewAdapter(this, getActivity(), backups);
        final RecyclerViewFragment recyclerViewFragment = this;
        backupArrayAdapter.setOnItemClickListener(new BackupRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                final Backup backup = backups.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setPositiveButton(getActivity().getString(R.string.restore), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            BackupHandler backupHandler = new BackupHandler(getActivity());
                            backupHandler.restoreBackup(backup.getName());
                            // restart app to apply
                            // prepare intent to rerun app shortly after termination
                            Intent mStartActivity = new Intent(getActivity(), MainActivity.class);
                            int mPendingIntentId = 123456;
                            PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId,
                                    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                            // kill app
                            android.os.Process.killProcess(android.os.Process.myPid());
                        } catch (BackupNotFoundException e) {
                            Log.e(e);
                            StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.backup_not_found, Snackbar
                                    .LENGTH_LONG);
                        } catch (Exception e) {
                            Log.e(e);
                            StatusMessageHandler.showStatusMessage(recyclerViewFragment, R.string.unknown_error, Snackbar.LENGTH_LONG);
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

                EditBackupDialog editBackupDialog = new EditBackupDialog();
                Bundle backupData = new Bundle();
                backupData.putString(EditBackupDialog.NAME_KEY, backup.getName());
                editBackupDialog.setArguments(backupData);
                editBackupDialog.setTargetFragment(recyclerViewFragment, 0);
                editBackupDialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });
        recyclerViewBackups.setAdapter(backupArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.backup_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewBackups.setLayoutManager(layoutManager);

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_backup_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateBackupDialog createBackupDialog = new CreateBackupDialog();
                createBackupDialog.setTargetFragment(recyclerViewFragment, 0);
                createBackupDialog.show(getFragmentManager(), null);
            }
        });

        // BroadcastReceiver to get notifications from background service if room data has changed
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("BackupFragment", "received intent: " + intent.getAction());
                refreshBackups();
            }
        };

        refreshBackups();

        showTutorial();

        return rootView;
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
                .show();
    }

    private void refreshBackups() {
        backups.clear();

        if (!checkWriteExternalStoragePermission()) {
            requestExternalStoragePermission();
        } else {
            BackupHandler backupHandler = new BackupHandler(getActivity());
            for (Backup backup : backupHandler.getBackups()) {
                backups.add(backup);
            }

            Collections.sort(backups, new Comparator<Backup>() {
                @Override
                public int compare(Backup lhs, Backup rhs) {
                    return lhs.compareDate(rhs);
                }
            });
        }

        backupArrayAdapter.notifyDataSetChanged();
    }

    private boolean checkWriteExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+
            int hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED;
        } else {
            // Pre-Marshmallow
            return true;
        }
    }

    private void requestExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.d("Displaying storage permission rationale to provide additional context.");

            StatusMessageHandler.showStatusMessage(this, R.string.missing_external_storage_permission,
                    android.R.string.ok, new Runnable() {
                        @Override
                        public void run() {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION);
                        }
                    }, Snackbar.LENGTH_INDEFINITE);
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
        if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
            menu.findItem(R.id.create_backup).setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.white));
        } else {
            menu.findItem(R.id.create_backup).setIcon(IconicsHelper.getAddIcon(getActivity(), android.R.color.black));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkWriteExternalStoragePermission();
        if (SmartphonePreferencesHandler.getHideAddFAB()) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_BACKUP_CHANGED);
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
}

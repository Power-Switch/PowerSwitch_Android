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

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.FolderRecyclerViewAdapter;
import eu.power_switch.gui.fragment.BackupFragment;
import eu.power_switch.settings.SmartphonePreferencesHandler;

/**
 * Dialog used to select a Path on SDCard
 * <p/>
 * Created by Markus on 22.11.2015.
 */
public class PathChooserDialog extends ConfigurationDialog {

    private String currentPath = "";

    private RecyclerView folderRecyclerView;
    private FolderRecyclerViewAdapter folderRecyclerViewAdapter;
    private ArrayList<File> folders;
    private TextView textViewCurrentPath;

    public static PathChooserDialog newInstance() {
        Bundle args = new Bundle();

        PathChooserDialog fragment = new PathChooserDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_path_chooser, container);

        currentPath = SmartphonePreferencesHandler.get(SmartphonePreferencesHandler.KEY_BACKUP_PATH);
        folders = getSubFolders(currentPath);

        textViewCurrentPath = (TextView) rootView.findViewById(R.id.textView_currentPath);
        textViewCurrentPath.setText(currentPath);

        final ImageView imageViewUp = (ImageView) rootView.findViewById(R.id.imageView_up);
        imageViewUp.setImageDrawable(IconicsHelper.getUpIcon(getContext()));
        imageViewUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File currentFolder = new File(currentPath);
                File parentFolder = currentFolder.getParentFile();
                if (parentFolder != null) {
                    if (!currentFolder.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getPath())) {
                        currentPath = parentFolder.getAbsolutePath();
                        notifyConfigurationChanged();
                    }
                }

                updateUI();
            }
        });

        folderRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_folders);
        folderRecyclerViewAdapter = new FolderRecyclerViewAdapter(getContext(), folders);
        folderRecyclerView.setAdapter(folderRecyclerViewAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        folderRecyclerView.setLayoutManager(layoutManager);
        folderRecyclerViewAdapter.setOnItemClickListener(new FolderRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                final File folder = folders.get(position);
                currentPath = folder.getPath();
                notifyConfigurationChanged();

                updateUI();
            }
        });

        return rootView;
    }

    private void updateUI() {
        textViewCurrentPath.setText(currentPath);
        updateSubFolders();
    }

    private void updateSubFolders() {
        folders.clear();

        folders.addAll(getSubFolders(currentPath));
        folderRecyclerViewAdapter.notifyDataSetChanged();
    }

    private ArrayList<File> getSubFolders(String currentPath) {
        ArrayList<File> subFolders = new ArrayList<>();

        File currentFolder = new File(currentPath);
        File[] folders = currentFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        if (folders != null) {
            subFolders.addAll(Arrays.asList(folders));
        }

        return subFolders;
    }

    @Override
    protected boolean initExistingData(Bundle arguments) {
        return false;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.path;
    }

    @Override
    protected boolean isValid() throws Exception {
        return true;
    }

    @Override
    protected void saveCurrentConfigurationToDatabase() {
        SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_BACKUP_PATH, currentPath);

        BackupFragment.sendBackupsChangedBroadcast(getContext());
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        // nothing to do here
    }
}

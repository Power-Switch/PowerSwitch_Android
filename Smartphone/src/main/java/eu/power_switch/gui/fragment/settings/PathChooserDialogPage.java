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

package eu.power_switch.gui.fragment.settings;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.BackupPathConfigurationHolder;
import eu.power_switch.gui.treeview.FolderTreeNode;
import eu.power_switch.gui.treeview.FolderTreeNodeViewHolder;
import eu.power_switch.gui.treeview.TreeItemFolder;

/**
 * Created by Markus on 06.08.2017.
 */
public class PathChooserDialogPage extends ConfigurationDialogPage<BackupPathConfigurationHolder> implements LoaderManager.LoaderCallbacks<View> {

    @BindView(R.id.textView_currentPath)
    TextView     textViewCurrentPath;
    @BindView(R.id.containerView)
    LinearLayout containerView;
    @BindView(R.id.layoutLoading)
    LinearLayout loadingLayout;

    private AndroidTreeView treeView;

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_path_chooser;
    }

    @Override
    protected void onRootViewInflated(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        textViewCurrentPath.setText(getConfiguration().getBackupPath());

        getLoaderManager().initLoader(0, null, this);
    }

    private View getFolderStructure() {
        TreeNode root = TreeNode.root();

        String path = Environment.getExternalStorageDirectory() + "";

        addChildFoldersRecursive(path, root);

        treeView = new AndroidTreeView(getActivity(), root);
        treeView.setDefaultAnimation(true);
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        treeView.setDefaultViewHolder(FolderTreeNodeViewHolder.class);
        treeView.setUseAutoToggle(false);
        treeView.setSelectionModeEnabled(true);
        treeView.setDefaultNodeClickListener(new TreeNode.TreeNodeClickListener() {
            @Override
            public void onClick(TreeNode node, Object value) {
                if (value instanceof TreeItemFolder) {
                    TreeItemFolder treeItemFolder = (TreeItemFolder) value;

                    String selectedPath = treeItemFolder.getPath();
                    getConfiguration().setBackupPath(selectedPath);
                    textViewCurrentPath.setText(selectedPath);

                    treeView.deselectAll();
                    treeView.selectNode(node, true);

                    notifyConfigurationChanged();
                }
            }
        });

        return treeView.getView();
    }

    private void addChildFoldersRecursive(String path, TreeNode parentFolderNode) {
        String currentPath = getConfiguration().getBackupPath();

        for (File folder : getVisibleSubFolders(path)) {
            TreeItemFolder treeItemFolder = new TreeItemFolder(getActivity(), folder.getName(), path);
            FolderTreeNode folderNode     = new FolderTreeNode(treeItemFolder);

            // expand path to currently set directory
            if (currentPath.contains(path + "/" + folder.getName())) {
                folderNode.setExpanded(true);
            }

            // highlight currently set directory
            if (currentPath.equals(path + "/" + folder.getName())) {
                folderNode.setExpanded(false);
                folderNode.setSelected(true);
            }

            addChildFoldersRecursive(path + "/" + folder.getName(), folderNode);

            parentFolderNode.addChild(folderNode);
        }
    }

    private ArrayList<File> getVisibleSubFolders(String currentPath) {
        ArrayList<File> subFolders = new ArrayList<>();

        File currentFolder = new File(currentPath);
        File[] folders = currentFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && !pathname.isHidden();
            }
        });

        if (folders != null) {
            subFolders.addAll(Arrays.asList(folders));
        }

        Collections.sort(subFolders, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return file1.getName()
                        .compareToIgnoreCase(file2.getName());
            }
        });

        return subFolders;
    }

    @Override
    public Loader<View> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<View>(getActivity()) {

            @Override
            protected void onStartLoading() {
                loadingLayout.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public View loadInBackground() {
                return getFolderStructure();
            }

            @Override
            protected void onStopLoading() {
                cancelLoad();
            }

            @Override
            protected void onReset() {
                onStopLoading();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<View> loader, View view) {
        containerView.addView(view);
        loadingLayout.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<View> loader) {
    }

}

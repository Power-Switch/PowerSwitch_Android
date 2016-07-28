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

package eu.power_switch.gui.treeview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;
import com.unnamed.b.atv.model.TreeNode;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;

/**
 * Created by Markus on 28.07.2016.
 */
public class FolderTreeNodeViewHolder extends TreeNode.BaseNodeViewHolder<TreeItemFolder> {

    private IconicsImageView arrowView;

    public FolderTreeNodeViewHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, final TreeItemFolder value) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        final View view = inflater.inflate(R.layout.tree_node_item_folder, null, false);

        arrowView = (IconicsImageView) view.findViewById(R.id.arrow);
        arrowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tView.toggleNode(node);
            }
        });

        if (node.isLeaf()) {
            arrowView.setVisibility(View.INVISIBLE);
        }

        if (node.isExpanded()) {
            arrowView.setImageDrawable(IconicsHelper.getKbArrowDownIcon(context));
        } else {
            arrowView.setImageDrawable(IconicsHelper.getKbArrowRightIcon(context));
        }

        IconicsImageView icon = (IconicsImageView) view.findViewById(R.id.icon);
        icon.setImageDrawable(value.getIcon());

        TextView folderNameView = (TextView) view.findViewById(R.id.folderName);
        folderNameView.setText(value.getLabel());

        return view;
    }

    @Override
    public void toggle(boolean active) {
        if (active) {
            arrowView.setImageDrawable(IconicsHelper.getKbArrowDownIcon(context));
        } else {
            arrowView.setImageDrawable(IconicsHelper.getKbArrowRightIcon(context));
        }
    }

}
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

package eu.power_switch.gui.treeview;

import android.content.Context;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.shared.ThemeHelper;

/**
 * Created by Markus on 28.07.2016.
 */
public class TreeItemFolder extends IconTreeItem {

    private String path;

    public TreeItemFolder(Context context, String folderName, String parentPath) {
        super(context, IconicsHelper.getFolderIcon(context), folderName);
        this.path = parentPath + "/" + folderName;
    }

    public String getPath() {
        return path;
    }

    public void setSelected(boolean selected) {
        final int colorDefault = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        final int colorAccent = ThemeHelper.getThemeAttrColor(context, R.attr.colorAccent);

        if (selected) {
            getIcon().color(colorAccent);
        } else {
            getIcon().color(colorDefault);
        }
    }
}

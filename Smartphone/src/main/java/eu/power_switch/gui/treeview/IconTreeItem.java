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

import com.mikepenz.iconics.IconicsDrawable;

/**
 * Created by Markus on 28.07.2016.
 */
public class IconTreeItem {
    protected Context context;
    protected IconicsDrawable icon;
    protected String label;

    public IconTreeItem(Context context, IconicsDrawable icon, String label) {
        this.context = context;
        this.icon = icon;
        this.label = label;
    }

    public Context getContext() {
        return context;
    }

    public IconicsDrawable getIcon() {
        return icon;
    }

    public void setIcon(IconicsDrawable icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
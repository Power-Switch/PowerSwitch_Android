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

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Markus on 17.02.2016.
 */
class ConfigurationDialogTabAdapter<Configuration extends ConfigurationHolder> extends FragmentPagerAdapter {

    private ConfigurationDialog<Configuration> parentDialog;
    private List<PageEntry<Configuration>>     pages;

    ConfigurationDialogTabAdapter(@NonNull ConfigurationDialog<Configuration> parentDialog, @NonNull List<PageEntry<Configuration>> pageEntries) {
        super(parentDialog.getChildFragmentManager());
        this.parentDialog = parentDialog;
        this.pages = pageEntries;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int pageTitleRes = pages.get(position)
                .getPageTitleRes();

        return parentDialog.getString(pageTitleRes);
    }

    @Override
    public Fragment getItem(int position) {
        PageEntry<Configuration> pageEntry = pages.get(position);

        ConfigurationDialogPage<Configuration> dialogPage = ConfigurationDialogPage.newInstance(pageEntry.getPageClass(), parentDialog);
//        dialogPage.setTargetFragment(parentDialog.getTargetFragment(), 0);

        return dialogPage;
    }

    /**
     * @return the number of pages to display
     */
    @Override
    public int getCount() {
        return pages.size();
    }

}

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

package eu.power_switch.wizard.gui;

import eu.power_switch.wizard.config.ConfigurationHolder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Markus on 27.06.2017.
 */
public abstract class ConfigurationPage extends WizardPage {

    /**
     * Configuration holder where to store the configuration of this page
     */
    @Getter
    @Setter
    protected ConfigurationHolder configurationHolder;

    /**
     * The current validity of this page
     */
    @Getter
    @Setter
    private boolean isValid = false;

}

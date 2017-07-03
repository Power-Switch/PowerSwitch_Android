package eu.power_switch.gui.dialog.configuration;

import lombok.Data;

/**
 * This class is the base class for keeping a record of the current values of a {@code {@link ConfigurationDialog}}
 * <p>
 * Created by Markus on 03.07.2017.
 */
@Data
public abstract class ConfigurationHolder {

    /**
     * Checks if this configuration is valid
     *
     * @return true if valid, false otherwise
     */
    public abstract boolean isValid();

}

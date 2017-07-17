package eu.power_switch.application;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Markus on 17.07.2017.
 */
public class RunConfig {

    @Getter
    @Setter
    private Mode mode;

    public RunConfig(Mode mode) {
        this.mode = mode;
    }

    public enum Mode {
        NORMAL,
        DEMO
    }
}
